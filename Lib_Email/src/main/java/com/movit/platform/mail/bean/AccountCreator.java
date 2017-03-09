package com.movit.platform.mail.bean;

import android.content.Context;

import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.NetworkType;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.Transport;
import com.fsck.k9.mail.store.RemoteStore;
import com.fsck.k9.mail.store.imap.ImapStoreSettings;
import com.movit.platform.mail.bean.Account.DeletePolicy;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings.Type;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.util.PortUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Deals with logic surrounding account creation.
 * <p/>
 * TODO Move much of the code from com.fsck.k9.activity.setup.* into here
 */
public class AccountCreator {

    public static DeletePolicy getDefaultDeletePolicy(Type type) {
        switch (type) {
            case IMAP: {
                return DeletePolicy.ON_DELETE;
            }
            case POP3: {
                return DeletePolicy.NEVER;
            }
            case WebDAV: {
                return DeletePolicy.ON_DELETE;
            }
            case SMTP: {
                throw new IllegalStateException("Delete policy doesn't apply to SMTP");
            }
        }

        throw new AssertionError("Unhandled case: " + type);
    }

    public static int getDefaultPort(ConnectionSecurity securityType, Type storeType) {
        switch (securityType) {
            case NONE:
            case STARTTLS_REQUIRED: {
                return storeType.defaultPort;
            }
            case SSL_TLS_REQUIRED: {
                return storeType.defaultTlsPort;
            }
        }
        throw new AssertionError("Unhandled ConnectionSecurity type encountered: " + securityType);
    }

    public static Account createAccount(Context context, String address, String password, Type type, String receivePath, String sendPath, boolean isSSL, boolean isSSLSend,String portReceive,String portSend) {
        String name = address.split("@")[0];
        Account account = Preferences.getPreferences(context).CreateAccount(name);
        account.setName(name);
        account.setEmail(address);
        ServerSettings receiveServerSetting = getReceiveServerSettings(address, password, type, receivePath, isSSL, portReceive);
        account.setStoreUri(RemoteStore.createStoreUri(receiveServerSetting));
        ServerSettings sendServerSetting = getSendServiceSetting(address, password, type, sendPath, isSSLSend,portSend);
        account.setTransportUri(Transport.createTransportUri(sendServerSetting));
        account.setCompression(NetworkType.MOBILE, true);
        account.setCompression(NetworkType.WIFI, true);
        account.setCompression(NetworkType.OTHER, true);
        return account;
    }

    private static ServerSettings getReceiveServerSettings(String address, String password, Type type, String receivePath, boolean isSSL,String portString) {
        //not use exchange,but use imap
        if (type == ServerSettings.Type.WebDAV) {
            type = ServerSettings.Type.IMAP;
            isSSL = true;
        }
        ConnectionSecurity securityType;
        if (isSSL) {
            securityType = ConnectionSecurity.SSL_TLS_REQUIRED;
        } else {
            securityType = ConnectionSecurity.NONE;
        }
        Map<String, String> extra = null;
        if (ServerSettings.Type.IMAP == type) {
            extra = new HashMap<>();
            extra.put(ImapStoreSettings.AUTODETECT_NAMESPACE_KEY, Boolean.toString(true));
            extra.put(ImapStoreSettings.PATH_PREFIX_KEY, "");
        }
        int port;
        if(portString != null){
            port = Integer.valueOf(portString);
        }else{
            port = PortUtil.getDefaultReceivePort(type, isSSL);
        }
        return new ServerSettings(type, receivePath, port,
                securityType, AuthType.PLAIN, address, password, null, extra);
    }

    private static ServerSettings getSendServiceSetting(String address, String password, Type type, String sendPath, boolean isSSL,String portString) {
        if (type == ServerSettings.Type.WebDAV) {
            isSSL = false;
        }
        ConnectionSecurity securityType;
        if (isSSL) {
            securityType = ConnectionSecurity.SSL_TLS_REQUIRED;
        } else {
            securityType = ConnectionSecurity.NONE;
        }
        int port;
        if(portString != null){
            port = Integer.valueOf(portString);
        }else{
            port = PortUtil.getDefaultSendPort(isSSL);
        }
        return new ServerSettings(Type.SMTP, sendPath, port,
                securityType, AuthType.PLAIN, address, password, null);
    }

    public static interface BaseAccount {
        public String getEmail();

        public void setEmail(String email);

        public String getDescription();

        public void setDescription(String description);

        public String getUuid();
    }

}
