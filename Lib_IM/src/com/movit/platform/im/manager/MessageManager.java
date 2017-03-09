package com.movit.platform.im.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.ListCallback;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.db.RecordsManager;
import com.movit.platform.im.module.group.entities.Group;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;

import okhttp3.Call;

/**
 * 消息管理类,发送消息至服务器，发送消息至openfire，保存数据库
 *
 * @author scorpiokara
 */
public class MessageManager {

    private static MessageManager messageManager = null;

    private Context mContext;
    private Handler mHandler;

    public static String SUFFIX;
    public static String GROUP_AT;

    protected RecordsManager imDao;

    private MessageManager(Context context) {
        mContext = context;
        imDao = IMDBFactory.getInstance(mContext).getRecordsManager();
    }

    public static MessageManager getInstance(Context context) {

        if (messageManager == null) {
            messageManager = new MessageManager(context);
        }
        return messageManager;
    }

    public void clean() {
        messageManager = null;
    }

    public void sendMessageToCMS(String msgId, String content, final String msgType,
                                 final int ctype, UserInfo user, String roomId, String subject,
                                 JSONArray jsonArray, final int groupType, final String groupId) {
        Date now = new Date();
        final MessageBean messageDataObj = new MessageBean();

        messageDataObj.setMsgId(msgId);
        messageDataObj.setCuserId(CommConstants.loginConfig
                .getmUserInfo().getEmpAdname());
        messageDataObj.setCtype(ctype);
        messageDataObj.setIsread(CommConstants.MSG_READ);
        messageDataObj.setUnReadCount(0);
        messageDataObj.setMtype(msgType);
        messageDataObj.setRsflag(CommConstants.MSG_SEND);
        String time = DateUtils.date2Str(now, DateUtils.FORMAT_FULL);
        messageDataObj.setFormateTime(time);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", new JSONObject(content));
            jsonObject.put("time", time);
            jsonObject.put("mtype", msgType);
            jsonObject.put("ctype", ctype);
            jsonObject.put("atList", jsonArray);

            //TODO anna 2015/11/26 为实现匿名聊天,增加以下两个参数
            jsonObject.put("groupType", groupType);
            jsonObject.put("groupId", groupId);
            jsonObject.put("msgId", msgId);

            messageDataObj.setContent(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            messageDataObj.setFriendId(user.getEmpAdname());
            messageDataObj.setUserInfo(user);
        } else if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            messageDataObj.setRoomId(roomId);
            messageDataObj.setSubject(subject);
            messageDataObj.setFriendId(CommConstants.loginConfig
                    .getmUserInfo().getEmpAdname());
        }
        onSendMsgProcessListener.onSendMsgStart(messageDataObj);

        if (msgType.equals(CommConstants.MSG_TYPE_TEXT) || msgType.equals(CommConstants.MSG_TYPE_FILE_2)) {
            postToSend(messageDataObj, "");
        } else if (msgType.equals(CommConstants.MSG_TYPE_PIC)) {
            try {
                toUploadFile(messageDataObj, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", messageDataObj);
            }
        } else if (msgType.equals(CommConstants.MSG_TYPE_AUDIO)) {
            try {
                toUploadFile(messageDataObj, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", messageDataObj);
            }
        } else if (msgType.equals(CommConstants.MSG_TYPE_METTING)) {
            postToSend(messageDataObj, "");
        } else if (msgType.equals(CommConstants.MSG_TYPE_VIDEO)) {
            postToSend(messageDataObj, "");
        } else if (msgType.equals(CommConstants.MSG_TYPE_FILE_1)) {
            try {
                toUploadFile(messageDataObj, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", messageDataObj);
            }
        } else if (msgType.equals(CommConstants.MSG_TYPE_LOCATION)) {
            try {
                toUploadFile(messageDataObj, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", messageDataObj);
            }
        }
    }

    public void forwardMessage(String msgId, JSONObject content, final String msgType,
                               final int ctype, UserInfo user, String roomId, String subject,
                               JSONArray jsonArray, final int groupType, final String groupId) {
        Date now = new Date();
        final MessageBean messageDataObj = new MessageBean();

        messageDataObj.setMsgId(msgId);
        messageDataObj.setCuserId(CommConstants.loginConfig
                .getmUserInfo().getEmpAdname());
        messageDataObj.setCtype(ctype);
        messageDataObj.setIsread(CommConstants.MSG_READ);
        messageDataObj.setUnReadCount(0);
        messageDataObj.setMtype(msgType);
        messageDataObj.setRsflag(CommConstants.MSG_SEND);
        String time = DateUtils.date2Str(now, DateUtils.FORMAT_FULL);
        messageDataObj.setFormateTime(time);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("content", content);
            jsonObject.put("time", time);
            jsonObject.put("mtype", msgType);
            jsonObject.put("ctype", ctype);
            jsonObject.put("atList", jsonArray);

            //TODO anna 2015/11/26 为实现匿名聊天,增加以下两个参数
            jsonObject.put("groupType", groupType);
            jsonObject.put("groupId", groupId);
            jsonObject.put("msgId", msgId);

            messageDataObj.setContent(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            messageDataObj.setFriendId(user.getEmpAdname());
            messageDataObj.setUserInfo(user);
        } else if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            messageDataObj.setRoomId(roomId);
            messageDataObj.setSubject(subject);
            messageDataObj.setFriendId(CommConstants.loginConfig
                    .getmUserInfo().getEmpAdname());
        }
        onSendMsgProcessListener.onSendMsgStart(messageDataObj);
        postToSend(messageDataObj, "");

    }

    public void reSendMessage(MessageBean bean) {
        String msgType = bean.getMtype();
        if (msgType.equals(CommConstants.MSG_TYPE_TEXT)) {
            postToSend(bean, "");
        } else if (msgType.equals(CommConstants.MSG_TYPE_PIC)) {
            try {
                toUploadFile(bean, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", bean);
            }
        } else if (msgType.equals(CommConstants.MSG_TYPE_AUDIO)) {
            try {
                toUploadFile(bean, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", bean);
            }
        } else if (msgType.equals(CommConstants.MSG_TYPE_FILE_1)
                || msgType.equals(CommConstants.MSG_TYPE_FILE_2)) {
            try {
                toUploadFile(bean, "url");
            } catch (JSONException e) {
                e.printStackTrace();
                sendMsgResult(CommConstants.MSG_SEND_FAIL, "", bean);
            }
        }
    }

    public void postToSend(final MessageBean messageDataObj,
                           final String localPath) {
        if (mHandler == null) {
            initHandler();
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean isSuccess = false;
                if (messageDataObj.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                    isSuccess = sendMessageToXMPP(messageDataObj);
                } else if (messageDataObj.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                    isSuccess = sendGroupMessageToXMPP(messageDataObj);
                }
                if (isSuccess) {
                    sendMsgResult(CommConstants.MSG_SEND_SUCCESS, localPath, messageDataObj);
                } else {
                    sendMsgResult(CommConstants.MSG_SEND_FAIL, "", messageDataObj);
                }
            }
        });
    }

    @SuppressLint("NewApi")
    public boolean sendMessageToXMPP(MessageBean obj) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String content = obj.getContent();
        String toUser = obj.getFriendId();
        Message message = new Message(toUser + SUFFIX, Type.chat);
        message.setBody(content);
        if (XmppManager.getInstance().isConnected()) {
            try {
                ChatManager manager = XmppManager.getInstance()
                        .getConnection().getChatManager();
                Chat chat = manager.createChat(toUser + SUFFIX, null);
                chat.sendMessage(message);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean sendGroupMessageToXMPP(MessageBean obj) {
        String content = obj.getContent();
        String room = obj.getRoomId();
        if (XmppManager.getInstance().isConnected()) {
            try {
                Group group = IMConstants.groupsMap.get(room);
                String roomServerName = CommConstants.roomServerName;
                if (StringUtils.notEmpty(group.getRoomServerName())) {
                    roomServerName = "@" + group.getRoomServerName() + ".";
                }
                String imServerName = group.getImServerName();
                MultiUserChat muc = new MultiUserChat(XmppManager
                        .getInstance().getConnection(), room + roomServerName
                        + imServerName);
                muc.sendMessage(content);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 下面是一个自定义的回调函数，用到回调上传文件是否完成
     */
    public static interface OnSendMsgProcessListener {
        /**
         * 上传响应
         *
         * @param responseCode
         * @param message
         */
        void onSendMsgDone(int responseCode, String message,
                           MessageBean messageDataObj);

        void onSendMsgStart(MessageBean obj);

        void onSendMsgProcess(int fileSize, int uploadSize);

    }

    private OnSendMsgProcessListener onSendMsgProcessListener;

    public void setOnSendMsgProcessListener(
            OnSendMsgProcessListener onSendMsgProcessListener) {
        this.onSendMsgProcessListener = onSendMsgProcessListener;
    }

    /**
     * 发送上传结果
     *
     * @param responseCode
     * @param responseMessage
     */
    private void sendMsgResult(int responseCode, String responseMessage,
                               MessageBean messageDataObj) {
        if (responseCode == CommConstants.MSG_SEND_FAIL) {
            //更新本地聊天记录，只修改发送状态
            imDao.updateRecord(null, CommConstants.MSG_SEND_FAIL, messageDataObj.getMsgId());
        }
        onSendMsgProcessListener.onSendMsgDone(responseCode, responseMessage,
                messageDataObj);
    }

    private void toUploadFile(final MessageBean messageDataObj, String fileUrl) throws JSONException {
        if (mHandler == null) {
            initHandler();
        }

        JSONObject content = new JSONObject(messageDataObj.getContent()).getJSONObject("content");
        String filePath = content.getString(fileUrl);
        File file = new File(filePath);
        HttpManager.uploadFile(CommConstants.URL_UPLOAD, null, file, "file", new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {

                    JSONObject obj = new JSONObject(response).getJSONObject("response");
                    JSONArray aryObj= obj.getJSONArray("message");
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = 1;
                    Bundle data = new Bundle();
                    data.putString("message", aryObj.getJSONObject(0).getString("uname"));
                    data.putSerializable("messageDataObj", messageDataObj);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            public void inProgress(float progress) {
                onSendMsgProcessListener.onSendMsgProcess(100, (int) progress * 100);
            }

        });
    }

    private void initHandler() {
        mHandler = new Handler() {

            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                MessageBean messageDataObj = (MessageBean) bundle
                        .getSerializable("messageDataObj");
                String message = bundle.getString("message");

                switch (msg.what) {
                    case 1:
                        // 上传成功，发送消息
                        try {
                            String uname = URLEncoder.encode(message, "utf-8");
                            // 再去发送消息 图片和语音
                            JSONObject remoteJson = new JSONObject();
                            JSONObject object = new JSONObject(messageDataObj.getContent());
                            // 图片语音文件名修改为 uname；
                            JSONObject content = object.getJSONObject("content");
                            String localPath = content.getString("url");
                            if (messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_PIC)) {
                                String json = content.getString("size");
                                remoteJson.put("url", URLDecoder.decode(uname, "utf-8"));
                                remoteJson.put("size", json);
                            } else if (messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_LOCATION)) {
                                String uName = URLDecoder.decode(uname, "utf-8");
                                String latitude = content.getString("latitude");
                                String longitude = content.getString("longitude");
                                String name = content.getString("name");
                                remoteJson.put("url", uName);
                                remoteJson.put("latitude", latitude);
                                remoteJson.put("longitude", longitude);
                                remoteJson.put("name", name);
                            } else if (messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_AUDIO)) {
                                boolean copy = FileUtils.copyFile(localPath, CommConstants.SD_DATA_AUDIO + uname);
                                if (copy) {
                                    String timelength = content.getString("timeLength");
                                    remoteJson.put("url", URLDecoder.decode(uname, "utf-8"));
                                    remoteJson.put("timeLength", timelength);
                                } else {
                                    sendMsgResult(CommConstants.MSG_SEND_FAIL, "文件上传失败", messageDataObj);
                                    return;
                                }
                            } else if (messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_VIDEO)) {
                                if (content.has("needUploadImage")
                                        && content.getString("needUploadImage").equalsIgnoreCase("Y")) {//如果视频的第一帧图片还未上传
                                    localPath = content.getString("url");
                                    boolean copy = FileUtils.copyFile(localPath, CommConstants.SD_DATA_VIDEO + uname);
                                    if (copy) {
                                        String imageUrl = content.getString("imageUrl");
                                        remoteJson.put("url", URLDecoder.decode(uname, "utf-8"));
                                        remoteJson.put("imageUrl", imageUrl);
                                        remoteJson.put("needUploadImage", "N");
                                        object.put("content", remoteJson);
                                        messageDataObj.setContent(object.toString());
                                        //更新本地数据库表中图片/音频文件路径
                                        RecordsManager recordsManager = IMDBFactory.getInstance(mContext).getRecordsManager();
                                        recordsManager.updateRecord(messageDataObj.getContent(), messageDataObj.getMsgId());

                                        File file = new File(localPath);
                                        if (file.exists()) {
                                            file.delete();
                                        }
                                    } else {
                                        sendMsgResult(CommConstants.MSG_SEND_FAIL, "视频上传失败", messageDataObj);
                                        return;
                                    }
                                    toUploadFile(messageDataObj, "imageUrl");
                                    return;
                                } else {
                                    localPath = content.getString("imageUrl");
                                    boolean copy = FileUtils.copyFile(localPath, CommConstants.SD_DATA_VIDEO + uname);
                                    if (copy) {
                                        LogUtils.v("onUploadDone", content.getString("url") + " 复制文件 "
                                                + CommConstants.SD_DATA_VIDEO + uname);

                                        String url = content.getString("url");
                                        String flag = content.getString("needUploadImage");
                                        remoteJson.put("url", url);
                                        remoteJson.put("imageUrl", URLDecoder.decode(uname, "utf-8"));
                                        remoteJson.put("needUploadImage", flag);
                                    } else {
                                        sendMsgResult(CommConstants.MSG_SEND_FAIL, "视频预览图上传失败", messageDataObj);
                                        return;
                                    }
                                }

                            } else if (messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_FILE_1)) {

                                String uName = URLDecoder.decode(uname, "utf-8");
                                String path = CommConstants.SD_DATA_FILE + uName;
                                String filePath = path.substring(0, path.lastIndexOf(File.separator));
                                File file = new File(filePath);
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                boolean copy = FileUtils.copyFile(localPath, path);
                                if (copy) {
                                    String json = content.getString("fileSize");
                                    String name = content.getString("name");
                                    remoteJson.put("url", uName);
                                    remoteJson.put("fileSize", json);
                                    remoteJson.put("name", name);
                                } else {
                                    sendMsgResult(CommConstants.MSG_SEND_FAIL, "文件上传失败", messageDataObj);
                                    return;
                                }

                            }
                            object.put("content", remoteJson);
                            messageDataObj.setContent(object.toString());
                            LogUtils.v("sendMessageToXMPP", messageDataObj.toString());

                            //更新本地数据库表中图片/音频文件路径
                            RecordsManager recordsManager = IMDBFactory.getInstance(mContext).getRecordsManager();
                            recordsManager.updateRecord(messageDataObj.getContent(), messageDataObj.getMsgId());

                            postToSend(messageDataObj, localPath);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            sendMsgResult(CommConstants.MSG_SEND_FAIL, mContext.getString(R.string.upload_fail), messageDataObj);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        // 上传失败
                        sendMsgResult(CommConstants.MSG_SEND_FAIL, mContext.getString(R.string.upload_fail), messageDataObj);
                        break;
                }
            }
        };
    }

}
