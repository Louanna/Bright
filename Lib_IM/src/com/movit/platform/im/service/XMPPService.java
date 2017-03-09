package com.movit.platform.im.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.entities.SerializableObj;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.db.RecordsManager;
import com.movit.platform.im.db.SessionManager;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.utils.JSONConvert;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * 聊天服务.
 * 切换到后台的时候，需要重新登录XMPP才能接收到消息，
 * 但是在断网和网络重连的时候不能去登录，会报错
 */
public class XMPPService extends Service {

    private static final String TAG = "XMPPService";

    private Context context;
    private SessionManager sessionManager;

    private int joinCount = 0;
    private Handler myHandler;

    public static Pattern pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*");

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        myHandler = new Handler();
        sessionManager = IMDBFactory.getInstance(context).getSessionManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //连接XMPP成功后，初始化监听各个Listener
            initXMPPListener();
            //连接XMPP成功后，需要join in groups，这样Group才能收到新消息
            doJoinGroup(100);
        } catch (Exception e) {
            e.printStackTrace();
            XmppManager.getInstance().disconnect();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (null != XmppManager.getInstance().getConnection()) {
                XmppManager.getInstance().getConnection().removePacketListener(pListener);
                XmppManager.getInstance().getConnection()
                        .removePacketListener(pListenerGroup);
                XmppManager.getInstance().getConnection()
                        .removePacketListener(pListenerNormal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 保存会话列表
            saveContactList();
            XmppManager.getInstance().disconnect();
        }
        super.onDestroy();
    }

    //循环遍历，若页面上存在新的聊天记录，其在数据库中不存在，则将其保存到数据库中
    //IMConstants.contactListDatas:页面上存在的聊天记录
    private void saveContactList() {
        try {
            ArrayList<String> list = sessionManager.getSessionList();
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                MessageBean bean = IMConstants.contactListDatas.get(i);
                if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                    if (!list.contains(bean.getFriendId().toLowerCase())) {
                        sessionManager.insertSession(IMConstants.contactListDatas
                                .get(i));
                    }
                } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                    if (!list.contains(bean.getRoomId())) {
                        sessionManager.insertSession(IMConstants.contactListDatas
                                .get(i));
                    }
                }
            }
            sessionManager.closeDb();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sessionManager.closeDb();
        }
    }

    private void initXMPPListener() {
        XMPPConnection conn = XmppManager.getInstance().getConnection();

        PacketFilter chatFilter = new MessageTypeFilter(Message.Type.chat);
        PacketFilter groupChatFilter = new MessageTypeFilter(Message.Type.groupchat);
        PacketFilter normalFilter = new MessageTypeFilter(Message.Type.normal);

        if (null != conn) {
            conn.addPacketListener(pListener, chatFilter);
            conn.addPacketListener(pListenerGroup, groupChatFilter);
            conn.addPacketListener(pListenerNormal, normalFilter);
        }
    }

    PacketListener pListenerNormal = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            final Message message = (Message) packet;
            Log.v(TAG, "pListenerNormal：" + message.toXML().toString());

            String body = message.getBody();
            if (body.startsWith("<authMessage")) {
                try {
                    InputStream inputStream = new ByteArrayInputStream(
                            body.getBytes());
                    // 创建解析
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser saxParser = spf.newSAXParser();
                    XMLSimpleLoginContentHandler handler = new XMLSimpleLoginContentHandler();
                    saxParser.parse(inputStream, handler);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (body.startsWith("<ReceiptMessage")) {

                //2015/12/11 增加本地存储聊天记录，为同步聊天记录发送状态，服务器端特增加该接口，供APP重置聊天记录状态
                body = body.substring(16, body.length() - 17);
                //更新数据库中聊天记录状态
                RecordsManager imDao = IMDBFactory.getInstance(context).getRecordsManager();
                try {

                    //记录需要修改发送状态的聊天记录
                    Map<String, Integer> recordsMap = new HashMap<String, Integer>();

                    JSONObject messageBean = new JSONObject(body);

                    String timestamp = messageBean.getString("st");
                    if (null != timestamp && !"".equalsIgnoreCase(timestamp)) {
                        //更新数据库中聊天记录状态
                        imDao.updateRecord(timestamp, CommConstants.MSG_SEND_SUCCESS, messageBean.getString("msgId"));
                        recordsMap.put(messageBean.getString("msgId"), CommConstants.MSG_SEND_SUCCESS);
                    } else {
                        //更新数据库中聊天记录状态
                        imDao.updateRecord(timestamp, CommConstants.MSG_SEND_FAIL, messageBean.getString("msgId"));
                        recordsMap.put(messageBean.getString("msgId"), CommConstants.MSG_SEND_FAIL);
                    }

                    //发送广播，更新当前界面ListView中聊天记录的发送状态
                    Intent intent = new Intent(CommConstants.MSG_UPDATE_SEND_STATUS_ACTION);
                    SerializableObj obj = new SerializableObj();
                    obj.setMap(recordsMap);
                    intent.putExtra("recordsMap", obj);
                    context.sendBroadcast(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    PacketListener pListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            final Message message = (Message) packet;
            Log.v(TAG, "PacketListener：" + message.toXML().toString());

            String body = message.getBody();

            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                body = body.substring(23);
            }

            if (body.startsWith("<customInvite")) {
                Log.v(TAG, "customInvite");
                try {
                    InputStream inputStream = new ByteArrayInputStream(
                            body.getBytes());
                    // 创建解析
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser saxParser = spf.newSAXParser();
                    XMLInviteContentHandler handler = new XMLInviteContentHandler();
                    saxParser.parse(inputStream, handler);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (body.startsWith("<customKick")) {
                Log.v(TAG, "customKick");
                try {
                    InputStream inputStream = new ByteArrayInputStream(
                            body.getBytes());
                    // 创建解析
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser saxParser = spf.newSAXParser();
                    XMLKickContentHandler handler = new XMLKickContentHandler();
                    saxParser.parse(inputStream, handler);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (body.startsWith("<roomBroadcast")) {
                Log.v(TAG, "roomBroadcast");
                try {
                    InputStream inputStream = new ByteArrayInputStream(
                            body.getBytes());
                    // 创建解析
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser saxParser = spf.newSAXParser();
                    XMLBrocastContentHandler handler = new XMLBrocastContentHandler();
                    saxParser.parse(inputStream, handler);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (body.startsWith("<sessionMessageList")) {
                //Enter Session的返回结果
                try {
                    Log.v(TAG, "sessionMessageList");

                    String json = body.subSequence(20, body.length() - 21).toString();
                    final Map<String, Object> responsemap = JSONConvert.json2MessageBean(json, context);
                    final ArrayList<MessageBean> beans = (ArrayList<MessageBean>) responsemap.get("messageBean");

                    //保存聊天消息到db中
                    RecordsManager recordsManager = IMDBFactory.getInstance(context).getRecordsManager();
                    recordsManager.insertRecords(beans, new RecordsManager.RecordsCallback() {
                        @Override
                        public void sendBroadcast() {
                            //向页面发送广播，通知页面刷新数据
                            Intent intent = new Intent(CommConstants.ACTION_SESSION_MESSAGE_LIST);
                            intent.putExtra("sessionMessageList", beans);
                            intent.putExtra("tipsAtMessage", responsemap.containsKey("atMessageContent") ? (String) responsemap.get("atMessageContent") : "");
                            intent.setPackage(context.getPackageName());
                            context.sendBroadcast(intent);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //New message的回调
                String tag = "";
                if (body.startsWith("{")) {
                    // 即时消息
                    // 如果有delay的就是openfire发送的，我们不需要
                    DelayInformation inf = (DelayInformation) message
                            .getExtension("x", "jabber:x:delay");
                    if (inf != null) {
                        Log.v(TAG, "这是一条离线消息:" + message.getBody());
                        return;
                    }
                    Log.v(TAG, message.toXML());
                    readXMLByDOM("<xml>" + message.toXML() + "</xml>", tag);
                }
            }
        }
    };

    PacketListener pListenerGroup = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            final Message message = (Message) packet;
            Log.v(TAG, "PacketListener-Group:" + message.toXML());
            String body = message.getBody();
            Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                body = body.substring(23);
            }

            String tag = "";

            if (body.startsWith("{")) {
                readXMLByDOM("<xml>" + message.toXML() + "</xml>", tag);
            } else if (body.startsWith("<")) {
                readXMLByDOM("<body>" + body + "</body>", tag);
            }
        }
    };

    public class XMLInviteContentHandler extends DefaultHandler {

        // localName表示元素的本地名称（不带前缀）；qName表示元素的限定名（带前缀）；atts 表示元素的属性集�
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("customInvite")) {
                String inviter = getNnameByJid(attributes.getValue("inviter"));
                String invitee = getNnameByJid(attributes.getValue("invitee"));
                String roomName = getNnameByJid(attributes.getValue("roomName"));
                Log.v(TAG, inviter + "邀请" + invitee + "加入" + roomName);
                if (inviter.equals(invitee)) {
                    // 自己发给自己的时候
                    return;
                }
                GroupManager.getInstance(context).getGroupInfoForInvite(
                        roomName, inviter, invitee);
            }
        }
    }

    public class XMLKickContentHandler extends DefaultHandler {
        // localName表示元素的本地名称（不带前缀）；qName表示元素的限定名（带前缀）；atts 表示元素的属性集�
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("customKick")) {
                String kicker = getNnameByJid(attributes.getValue("kicker"));
                String kicked = getNnameByJid(attributes.getValue("kicked"));
                String roomName = getNnameByJid(attributes.getValue("roomName"));
                String reason = attributes.getValue("reason");

                Log.v(TAG, kicker + "将" + kicked + "踢出房间" + roomName + "因为:" + reason);
                String roomJid = attributes.getValue("roomName");
                String kickedJid = attributes.getValue("kicked");
                myHandler.postDelayed(new LeaveRunnable(roomJid, kickedJid), 200);

                List<Group> groups = IMConstants.groupListDatas;
                Group group = IMConstants.groupsMap.get(roomName);
                String displayName = group.getDisplayName();
                groups.remove(group);
                IMConstants.groupsMap.remove(roomName);

                Intent intent = new Intent(CommConstants.ACTION_MY_KICKED);
                intent.putExtra("roomName", roomName);
                intent.putExtra("displayName", displayName);
                intent.putExtra("reason", reason);
                intent.putExtra("kicker", kicker);
                intent.putExtra("kicked", kicked);
                intent.setPackage(context.getPackageName());

                context.sendBroadcast(intent);
                Log.v(TAG, "customKick----" + "sendBroadcast");

                Intent intent2 = new Intent(
                        CommConstants.ACTION_GROUP_MEMBERS_CHANGES);
                intent2.setPackage(context.getPackageName());
                context.sendBroadcast(intent2);
            }
        }
    }

    class LeaveRunnable implements Runnable {
        String roomJid;
        String kicked;

        public LeaveRunnable(String roomJid, String kicked) {
            super();
            this.roomJid = roomJid;
            this.kicked = kicked;
        }

        @Override
        public void run() {
            try {
                Presence presence = new Presence(Presence.Type.unavailable);
                presence.setFrom(kicked);
                presence.setTo(roomJid);
                XmppManager.getInstance().getConnection()
                        .sendPacket(presence);
                Log.v(TAG, "MultiUserChat," + kicked + "已退出" + roomJid);
            } catch (Exception e) {
                e.printStackTrace();
                myHandler.postDelayed(new LeaveRunnable(roomJid, kicked), 1000);
            }
        }
    }

    public class XMLBrocastContentHandler extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("roomBroadcast")) {
                String roomName = getNnameByJid(attributes.getValue("roomName"));
                String displayName = attributes.getValue("displayName");
                String type = attributes.getValue("type");
                String affecteds = attributes.getValue("affecteds");
                /**
                 * 0表示新增成员通知; 1表示踢出成员通知; 2表示变更displayName通知; 3解散群通知;4用户退群通知
                 */
                String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                if (type.equals("0")) {
                    if (affecteds.equalsIgnoreCase(adname)) {
                        return;
                    }
                    GroupManager.getInstance(context).getGroupInfo(
                            roomName, displayName, type, affecteds);
                } else if (type.equals("1")) {
                    GroupManager.getInstance(context).getGroupInfo(
                            roomName, displayName, type, affecteds);
                } else if (type.equals("4")) {
                    if (affecteds.equalsIgnoreCase(adname)) {
                        // 我自己退出
                        // String kickedJid = affecteds + MessageManager.SUFFIX;
                        // handler.postDelayed(new LeaveRunnable(roomJid,
                        // kickedJid), 200);
                        // List<Group> groups = GroupListManager.getInstance(
                        // context).getmDatas();
                        // Group group = GroupListManager.getInstance(context)
                        // .getGroupsMap().get(roomName);
                        // groups.remove(group);
                        // GroupListManager.getInstance(context).getGroupsMap()
                        // .remove(roomName);
                        return;
                    } else {
                        // 从本地删除数据
                        List<Group> groups = IMConstants.groupListDatas;
                        for (int i = 0; i < groups.size(); i++) {
                            if (groups.get(i).getGroupName().equalsIgnoreCase(roomName)) {

                                List<UserInfo> members = groups.get(i)
                                        .getMembers();
                                for (int j = 0; j < members.size(); j++) {
                                    if (members.get(j).getEmpAdname()
                                            .equalsIgnoreCase(affecteds)) {
                                        members.remove(j);
                                        break;
                                    }
                                }
                                IMConstants.groupsMap.put(roomName, groups.get(i));
                                break;
                            }
                        }
                    }

                    Log.v(TAG, "getGroupInfo，" + "sendBroadcast");
                    Intent intent = new Intent(
                            CommConstants.ACTION_GROUP_MEMBERS_CHANGES);
                    intent.putExtra("type", type);
                    intent.putExtra("groupName", roomName);
                    intent.putExtra("displayName", displayName);
                    intent.putExtra("affecteds", affecteds);
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);

                } else if (type.equals("2")) {
                    List<Group> groups = IMConstants.groupListDatas;
                    for (int i = 0; i < groups.size(); i++) {
                        if (groups.get(i).getGroupName().equalsIgnoreCase(roomName)) {
                            groups.get(i).setDisplayName(displayName);
                            IMConstants.groupsMap.get(roomName)
                                    .setDisplayName(displayName);
                            break;
                        }
                    }

                    Intent intent = new Intent(
                            CommConstants.ACTION_GROUP_DISPALYNAME_CHANGES);
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("displayName", displayName);
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);
                } else if (type.equals("3")) {
                    List<Group> groups = IMConstants.groupListDatas;
                    for (int i = 0; i < groups.size(); i++) {
                        if (groups.get(i).getGroupName().equalsIgnoreCase(roomName)) {
                            groups.remove(i);
                            // 放到前面去处理
                            break;
                        }
                    }
                    Intent intent = new Intent(
                            CommConstants.ACTION_GROUP_DISSOLVE_CHANGES);
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("displayName", displayName);
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);
                }
            }
        }
    }

    public class XMLSimpleLoginContentHandler extends DefaultHandler {
        // localName表示元素的本地名称（不带前缀）；qName表示元素的限定名（带前缀）；atts 表示元素的属性集�
        StringBuffer body;

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("authMessage")) {
                body = new StringBuffer();
                // <message to="potter.tao@win-80baetatp2v"
                // from="admin@win-80baetatp2v"><body>&lt;authMessage&gt;该账号已经在其他设备登录,请重新登录&lt;/authMessage&gt;</body>
                // <thread>KPboL3</thread></message>
            }

        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
            body.append(ch, start, length); // 将读取的字符数组追加到builder中
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals("authMessage")) {
                Log.v(TAG, "body=" + body.toString());
                Intent intent = new Intent(CommConstants.SIMPLE_LOGIN_ACTION);
                intent.putExtra("body", body.toString());
                intent.putExtra("type", CommConstants.TYPE_JUST_TIPS);
                intent.setPackage(context.getPackageName());
                sendBroadcast(intent);
            }
        }
    }

    public void readXMLByDOM(String xml, String tag) {
        try {
            int unReadCount = 1;
            int isread = CommConstants.MSG_UNREAD;
            String msgId = "";
            String groupType = "";
            String cuserId = "";
            String friendId = "";
            int rsflag = 0;
            String roomId = "";
            int ctype = -1;
            int isSend = CommConstants.MSG_SEND_SUCCESS;
            String timestamp = "";
            String content = null, time = null, formatTime = null, mtype = null;
            String subject = "";
            boolean isATMessage = false;

            boolean fromWeChat = false;

            cuserId = MFSPHelper.getString(CommConstants.EMPADNAME);

            InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(inputStream);
            Element root = dom.getDocumentElement();
            NodeList items = root.getElementsByTagName("message");// 查找所有message节点
            for (int i = 0; i < items.getLength(); i++) {
                // 得到第一个message节点
                Element messageNode = (Element) items.item(i);
                String type = messageNode.getAttribute("type");
                String from = messageNode.getAttribute("from");
                String to = messageNode.getAttribute("to");
                if (type.equals("groupchat")) {
                    ctype = CommConstants.CHAT_TYPE_GROUP;
                    friendId = from.substring(from.lastIndexOf("/") + 1);// 发送者id
                    roomId = from.substring(0, from.indexOf("@"));
                    if (friendId.equalsIgnoreCase(cuserId)) {// 收到的是自己发出的消息
                        rsflag = CommConstants.MSG_SEND;
                        return;
                    } else {
                        rsflag = CommConstants.MSG_RECEIVE;
                    }
                } else if (type.equals("chat")) {
                    ctype = CommConstants.CHAT_TYPE_SINGLE;
                    String fromWho = from.substring(0, from.indexOf("@"));
                    String toWho = to.substring(0, to.indexOf("@"));
                    if (fromWho.equalsIgnoreCase(cuserId)) {
                        friendId = toWho;
                        rsflag = CommConstants.MSG_SEND;
                    } else {
                        friendId = fromWho;
                        rsflag = CommConstants.MSG_RECEIVE;
                    }
                }

                // 获取message节点下的所有子节点(标签之间的空白节点和body/自定义标签元素)
                NodeList childsNodes = messageNode.getChildNodes();
                try {
                    for (int j = 0; j < childsNodes.getLength(); j++) {
                        Node node = (Node) childsNodes.item(j); // 判断是否为元素类型
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element childNode = (Element) node;
                            // 判断是否body元素
                            if ("body".equals(childNode.getNodeName())) {
                                // 获取body元素下Text节点,然后从Text节点获取数据
                                String body = childNode.getFirstChild()
                                        .getNodeValue();
                                Matcher matcher = pattern.matcher(body);
                                if (matcher.find()) {
                                    timestamp = body.substring(0, 23);
                                    body = body.substring(23);
                                }
                                content = body;

                                JSONObject object = new JSONObject(body);
                                mtype = object.getString("mtype");// T or A or P
                                formatTime = object.getString("time");
                                JSONObject contentObject = object
                                        .getJSONObject("content");
                                if (contentObject.has("fromWechatUser")) {
                                    friendId = contentObject
                                            .getString("fromWechatUser");
                                    fromWeChat = true;
                                }
                                if (object.has("msgId")) {
                                    msgId = object.getString("msgId");
                                }
                                if (object.has("groupType")) {
                                    groupType = object.getString("groupType");
                                }
                                String curUserId = MFSPHelper.getString(CommConstants.USERID);

                                //增加是否为ATMessage的判断
                                isATMessage = object.has("atList")
                                        && null != object.getJSONArray("atList")
                                        && object.getJSONArray("atList").toString().contains(curUserId);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                UserDao dao = UserDao.getInstance(context);
                UserInfo user = dao.getUserInfoByADName(friendId);
                if (!CommConstants.GROUP_ADMIN.equalsIgnoreCase(friendId)
                        && !fromWeChat) {
                    if (user == null) {
                        continue;
                    }
                }
                if (ctype == CommConstants.CHAT_TYPE_GROUP) {
                    Group group;
                    try {
                        group = IMConstants.groupsMap.get(roomId);
                        subject = group.getDisplayName();
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                }

                MessageBean obj = new MessageBean(msgId, cuserId, friendId, content,
                        formatTime, mtype, ctype, rsflag, unReadCount, isread,
                        isSend, user, roomId, subject, tag, timestamp);

                obj.setMsgId(msgId);
                obj.setGroupType(groupType);
                obj.setIsATMessage(isATMessage);

                if (fromWeChat) {
                    obj.setFromWechatUser(true);
                }
                // 普通消息
                // 广播通知界面更新
                if (IMConstants.contactListDatas.isEmpty()) {
                    IMConstants.contactListDatas.add(obj);
                } else {
                    boolean isHasMessage = false;
                    for (int j = 0; j < IMConstants.contactListDatas.size(); j++) {
                        MessageBean bean = IMConstants.contactListDatas.get(j);
                        if (ctype == CommConstants.CHAT_TYPE_SINGLE
                                && bean.getCtype() == ctype) {
                            if (bean.getFriendId().toLowerCase().equalsIgnoreCase(
                                    obj.getFriendId().toLowerCase())) {
                                int count = bean.getUnReadCount();
                                obj.setUnReadCount(count + 1);
                                IMConstants.contactListDatas.remove(j);
                                IMConstants.contactListDatas.add(0, obj);
                                isHasMessage = true;
                                break;
                            }
                        } else if (ctype == CommConstants.CHAT_TYPE_GROUP
                                && bean.getCtype() == ctype) {
                            if (bean.getRoomId().equalsIgnoreCase(obj.getRoomId())) {
                                int count = bean.getUnReadCount();
                                obj.setUnReadCount(count + 1);
                                IMConstants.contactListDatas.remove(j);
                                IMConstants.contactListDatas.add(0, obj);
                                isHasMessage = true;
                                break;
                            }
                        }
                    }
                    if (!isHasMessage) {
                        IMConstants.contactListDatas.add(0, obj);
                    }
                }
                final MessageBean messageBean = obj;
                IMDBFactory.getInstance(context).getRecordsManager().insertRecord(obj, RecordsManager.MESSAGE_TYPE_RECEIVE, new RecordsManager.RecordsCallback() {
                    @Override
                    public void sendBroadcast() {
                        Intent intent = new Intent(CommConstants.ACTION_NEW_MESSAGE);
                        intent.putExtra("messageDataObj", messageBean);
                        intent.setPackage(context.getPackageName());
                        context.sendBroadcast(intent);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    Handler JoinGrouphandler = new Handler();

    private void doJoinGroup(long delayMillis) {
        if (joinCount > 3) {
            joinCount = 0;
            return;
        }
        JoinGrouphandler.removeCallbacks(joinGroupRunnable);
        JoinGrouphandler.postDelayed(joinGroupRunnable, delayMillis);
        joinCount++;
    }

    Runnable joinGroupRunnable = new Runnable() {

        @Override
        public void run() {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                    List<Group> groups = IMConstants.groupListDatas;
                    XMPPConnection connection = XmppManager.getInstance().getConnection();
                    if (connection == null || !connection.isConnected()) {
                        doJoinGroup(1500);
                        return;
                    }

                    MultiUserChat muChat = null;
                    for (int i = 0; i < groups.size(); i++) {
                        try {
                            final Group group = groups.get(i);
                            String roomServerName = CommConstants.roomServerName;
                            if (StringUtils.notEmpty(group.getRoomServerName())) {
                                roomServerName = "@"
                                        + group.getRoomServerName() + ".";
                            }
                            String imServerName = group.getImServerName();

                            muChat = new MultiUserChat(XmppManager
                                    .getInstance().getConnection(), group
                                    .getGroupName()
                                    + roomServerName
                                    + imServerName);
                            // 聊天室服务将会决定要接受的历史记录数�
                            DiscussionHistory history = new DiscussionHistory();
                            history.setMaxStanzas(0);
                            muChat.join(adname, "", history,
                                    SmackConfiguration.getPacketReplyTimeout());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    };

    public String getNnameByJid(String jid) {
        if (StringUtils.notEmpty(jid)) {
            int k = jid.indexOf("@");
            return jid.substring(0, k);
        }
        return jid;
    }

}
