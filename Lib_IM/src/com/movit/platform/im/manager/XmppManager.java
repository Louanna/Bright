package com.movit.platform.im.manager;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;

/**
 * XMPP服务器连接工具类.
 *
 * @author Potter.Tao
 */
public class XmppManager {
    private XMPPConnection connection;
    private ConnectionConfiguration connectionConfig;
    private static XmppManager xmppManager;

    //需在代码前静态加载ReconnectionManager，重连才能正常工作
    static {
        try {
            Class.forName("org.jivesoftware.smack.ReconnectionManager");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private XmppManager() {
    }

    public static XmppManager getInstance() {
        if (xmppManager == null) {
            xmppManager = new XmppManager();
        }
        return xmppManager;
    }

    /**
     * 返回一个有效的xmpp连接,如果无效则返回空.
     *
     * @return
     * @author Potter.Tao
     * @update 2012-7-4 下午6:54:31
     */
    public XMPPConnection getConnection() {
        return connection;
    }

    /**
     * 销毁xmpp连接.
     *
     * @author Potter.Tao
     * @update 2012-7-4 下午6:55:03
     */
    public void disconnect() {
        if (connection != null) {
            try {
                if (connection.isConnected()) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    /**
     * 返回一个有效的xmpp连接,如果无效则返回空.
     *
     * @return
     * @author Potter.Tao
     * @update 2012-7-4 下午6:54:31
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public XMPPConnection
    initialize(String host, int port) throws Exception {
        connectionConfig = new ConnectionConfiguration(host, port);
        configure(ProviderManager.getInstance());
        connectionConfig.setDebuggerEnabled(true);
        // 允许自动连接
        connectionConfig.setReconnectionAllowed(true);
        // 允许登陆成功后更新在线状态
        connectionConfig.setSendPresence(true);
        connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        connectionConfig.setCompressionEnabled(false);
        connectionConfig.setSelfSignedCertificateEnabled(false);
        connectionConfig.setVerifyChainEnabled(false);
        connectionConfig.setSASLAuthenticationEnabled(false);

        connection = new XMPPConnection(connectionConfig);
        connection.connect();
        initFeatures(connection);

        connection.addConnectionListener(new ConnectionListener() {

            @Override
            public void reconnectionSuccessful() {
                //当网络断线了，重新连接上服务器触发的事件
                System.out.println("reconnectionSuccessful");
            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                //重新连接失败
                System.out.println("reconnectionFailed");
            }

            @Override
            public void reconnectingIn(int arg0) {
                //重新连接的动作正在进行的动作，里面的参数arg0是一个倒计时的数字，
                //如果连接失败的次数增多，数字会越来越大，开始的时候是14
                System.out.println("reconnectingIn");
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                System.out.println("connectionClosedOnError");

                // 这里就是网络不正常或者被挤掉断线激发的事件
                if (e.getMessage().contains("conflict")) { // 被挤掉线
                    // 关闭连接，由于是被人挤下线，可能是用户自己，所以关闭连接，让用户重新登录是一个比较好的选择
                    disconnect();
                    // 接下来你可以通过发送一个广播，提示用户被挤下线，重连很简单，就是重新登录
                } else if (e.getMessage().contains("Connection timed out")) {// 连接超时
                    // 不做任何操作，会实现自动重连

                }
            }

            @Override
            public void connectionClosed() {
                //这里是正常关闭连接的事件
                System.out.println("connectionClosed");
            }
        });
        return connection;
    }

    private static void configure(ProviderManager pm) {
        // Service Discovery # Items
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
                new DiscoverItemsProvider());
        // Service Discovery # Info
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
                new DiscoverInfoProvider());

        // Service Discovery # Items
//        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
//                new DiscoverItemsProvider());
        // Service Discovery # Info
//        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
//                new DiscoverInfoProvider());

        // Offline Message Requests
        pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageRequest.Provider());
        // Offline Message Indicator
        pm.addExtensionProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageInfo.Provider());

        // vCard
        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        // FileTransfer
        pm.addIQProvider("si", "http://jabber.org/protocol/si",
                new StreamInitiationProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
                new BytestreamsProvider());
        // pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
        // new IBBProviders.Open());
        // pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
        // new IBBProviders.Close());
        // pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
        // new IBBProviders.Data());
        // Data Forms
        pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
        // Html
        pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
                new XHTMLExtensionProvider());
        // Ad-Hoc Command
        pm.addIQProvider("command", "http://jabber.org/protocol/commands",
                new AdHocCommandDataProvider());
        // Chat State
        ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();
        pm.addExtensionProvider("active",
                "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("composing",
                "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("paused",
                "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("inactive",
                "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("gone",
                "http://jabber.org/protocol/chatstates", chatState);
        // MUC User,Admin,Owner
        pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
                new MUCUserProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
                new MUCAdminProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
                new MUCOwnerProvider());

//        pm.addIQProvider("xmlns","urn:xmpp:carbons:2",new CarbonManagerProvider());
    }

    public static void initFeatures(XMPPConnection xmppConnection) {
        ServiceDiscoveryManager.setIdentityName("Android_IM");
        ServiceDiscoveryManager.setIdentityType("phone");
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
                .getInstanceFor(xmppConnection);
        if (sdm == null) {
            sdm = new ServiceDiscoveryManager(xmppConnection);
        }
        //TODO 新增的
//        sdm.addFeature("urn:xmpp:carbons:2");
//        sdm.addFeature("urn:xmpp:forwarded:0");

        //原来的
        sdm.addFeature("http://jabber.org/protocol/disco#info");
        sdm.addFeature("http://jabber.org/protocol/caps");
        sdm.addFeature("urn:xmpp:avatar:metadata");
        sdm.addFeature("urn:xmpp:avatar:metadata+notify");
        sdm.addFeature("urn:xmpp:avatar:data");
        sdm.addFeature("http://jabber.org/protocol/nick");
        sdm.addFeature("http://jabber.org/protocol/nick+notify");
        sdm.addFeature("http://jabber.org/protocol/xhtml-im");
        sdm.addFeature("http://jabber.org/protocol/muc");
        sdm.addFeature("http://jabber.org/protocol/commands");
        sdm.addFeature("http://jabber.org/protocol/si/profile/file-transfer");
        sdm.addFeature("http://jabber.org/protocol/si");
        sdm.addFeature("http://jabber.org/protocol/bytestreams");
        sdm.addFeature("http://jabber.org/protocol/ibb");
        sdm.addFeature("http://jabber.org/protocol/feature-neg");
        sdm.addFeature("jabber:iq:privacy");
    }

}
