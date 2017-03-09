package com.movit.platform.im.module.single.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.base.ChatBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.manager.IMManager;
import com.movit.platform.im.manager.MessageManager;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.im.module.detail.activity.ChatDetailActivity;
import com.movit.platform.im.module.single.adapter.ChatAdapter;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;

public class ChatActivity extends ChatBaseActivity {

    private String msgTxt;
    private UserInfo userInfo;
    private MessageBean message;

    @Override
    public void onDestroy() {
        IMManager.leavePrivateSession(sessionObjId.toLowerCase());
        super.onDestroy();
    }

    @Override
    protected void initData() {

        mContext = this;
        ctype = CommConstants.CHAT_TYPE_SINGLE;
        message_pool = new ArrayList<>();

        MessageManager.getInstance(this).setOnSendMsgProcessListener(this);

        Intent intent = getIntent();
        userInfo = (UserInfo) intent.getExtras().getSerializable("userInfo");
        if (userInfo != null) {
            sessionObjId = userInfo.getEmpAdname();
        } else {
            finish();
            return;
        }

        IMConstants.CHATTING_ID = sessionObjId;
        IMConstants.CHATTING_TYPE = "chat";

        message = (MessageBean) intent.getExtras().getSerializable("messageBean");

        //进入chat界面，先从数据库中取本地聊天记录
        getDBRecords();

        //只有H5发起单聊的时候，才会传递msgTxt
        //是一个提示语句
        msgTxt = intent.getStringExtra("msgTxt");
        if (!TextUtils.isEmpty(msgTxt)) {
            sendTxtMessage(msgTxt);
        }

    }

    // 进入单人聊天会话
    @Override
    protected void sendEnterSession() {
        try {
            //和pill讨论了多次，为了获取中间丢失的聊天记录，需要取startTime与endTime之间的数据
            //最后实现时，又被pill毙掉了endTime
            String startTime = IMDBFactory.getInstance(this).getRecordsManager().getStartTimeAndEndTime(sessionObjId, MFSPHelper.getString(CommConstants.EMPADNAME));
            String endTime = "";
            if (StringUtils.notEmpty(startTime)) {
                startTime = startTime.split(",")[0];
                if (startTime.split(",").length == 2) {
                    endTime = startTime.split(",")[1];
                }
            }

            //通知XMPP已上线
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "enterSessionDialog");
            jsonObject.put("toJID", sessionObjId.toLowerCase());
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);
            jsonObject.put("pageSize", 200);
            ChatManager manager = XmppManager.getInstance()
                    .getConnection().getChatManager();
            Chat chat = manager.createChat("admin" + MessageManager.SUFFIX, null);
            chat.sendMessage(jsonObject.toString());

            //警告：这里是个坑
            //94服务器：服务器端说他user不区分大小写，故要求加上.toLowerCase()
            //如果有项目user区分大小写，请留意这个坑
            StringBuilder sb = new StringBuilder();
            sb.append("sender=").append(sessionObjId.toLowerCase()).append("&receiver=").append(MFSPHelper.getString(CommConstants.EMPADNAME).toLowerCase()).append("&startTime=").append(startTime).append("&size=").append(200);
            String url = CommConstants.URL_EOP_IM+"im/chatLog/getChatLogs?" + sb.toString();
            HttpManager.getJsonWithToken(url, new EnterSessionCallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initTopBar() {
        mTopTitle.setText(userInfo.getEmpCname());
        mTopLeftImage.setImageResource(R.drawable.top_back);
        mTopRightImage.setImageResource(R.drawable.top_user);
        mTopRightImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();

                UserDao dao = UserDao.getInstance(ChatActivity.this);
                UserInfo me = dao.getUserInfoById(userInfo.getId());
                userInfos.add(me);
                startActivity(new Intent(ChatActivity.this,
                        ChatDetailActivity.class).putExtra("userInfos",
                        userInfos).putExtra("ctype", CommConstants.CHAT_TYPE_SINGLE));
            }
        });
    }

    @Override
    protected void initListAdapter() {
        chatAdapter = new ChatAdapter(this, message_pool, mMsgListView,
                handler, this, CommConstants.CHAT_TYPE_SINGLE);

        //转发聊天消息时，执行如下代码逻辑
        //目前只有文件消息转发
        if (null != message) {
            String json = message.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                MessageManager.getInstance(ChatActivity.this).forwardMessage(UUID.randomUUID().toString(), content,
                        message.getMtype(), ctype, userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void sendMessageIfNotNull() {
        // 保存消息并发送
        String content = mChatEditText.getText().toString().trim();
        mChatEditText.setText("");
        sendTxtMessage(content);
    }

    private void sendTxtMessage(String content) {
        if (!TextUtils.isEmpty(content)) {
            JSONObject textJsonObject = new JSONObject();
            try {
                textJsonObject.put("text", content);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MessageManager.getInstance(this).sendMessageToCMS(
                    UUID.randomUUID().toString(), textJsonObject.toString(), CommConstants.MSG_TYPE_TEXT, ctype,
                    userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
        }
    }

    @Override
    protected void sendVoiceMessage(String content) {
        MessageManager.getInstance(ChatActivity.this).sendMessageToCMS(UUID.randomUUID().toString(), content,
                CommConstants.MSG_TYPE_AUDIO, ctype, userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
    }

    @Override
    protected void sendPicMessage(String content) {
        MessageManager.getInstance(ChatActivity.this).sendMessageToCMS(UUID.randomUUID().toString(), content,
                CommConstants.MSG_TYPE_PIC, ctype, userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
    }

    @Override
    protected void sendFile(String content, String msgType) {
        MessageManager.getInstance(ChatActivity.this).sendMessageToCMS(UUID.randomUUID().toString(), content,
                msgType, ctype, userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
    }

    @Override
    protected void sendLocation(String content) {
        MessageManager.getInstance(ChatActivity.this).sendMessageToCMS(UUID.randomUUID().toString(), content,
                CommConstants.MSG_TYPE_LOCATION, ctype, userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");

    }

    @Override
    protected void showAtTips(MessageBean msgBean, String atMessage) {
        //单聊没有@提醒
    }

    @Override
    protected void sendMeeting() {

        String url = CommConstants.BASE_URL + ":7004/IBMTest?method=getRoomUrl";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss sss");//设置日期格式
        String roomName = df.format(new Date());

        String userName = MFSPHelper.getString(CommConstants.EMPADNAME);
        String password = MFSPHelper.getString(CommConstants.PASSWORD);

        StringBuffer sb = new StringBuffer();
        sb.append("&").append("roomName=").append(roomName);
        sb.append("&").append("userName=").append(userName);
        sb.append("&").append("password=").append(password);

        OkHttpUtils
                .getWithToken()
                .url(url + sb.toString())
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            boolean ok = object.getBoolean("result");
                            String meetingURL = object.getString("message");
                            if (ok) {
                                //先获取会议地址，在发送消息
                                String meetingName = "百度会议";
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("url", meetingURL);
                                    jsonObject.put("name", meetingName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                MessageManager.getInstance(ChatActivity.this).sendMessageToCMS(
                                        UUID.randomUUID().toString(), jsonObject.toString(), CommConstants.MSG_TYPE_METTING, ctype,
                                        userInfo, "", "", null, ChatActivity.this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
                            } else {
                                ToastUtils.showToast(ChatActivity.this, "创建会议失败");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void sendVideoMessage(String content) {
        MessageManager.getInstance(ChatActivity.this).sendMessageToCMS(UUID.randomUUID().toString(), content,
                CommConstants.MSG_TYPE_VIDEO, ctype, userInfo, "", "", null, this.getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), "");
    }

    @Override
    protected String getGetMessageListURL(MessageBean bean) {

        return CommConstants.URL_EOP_IM + "im/chatLog/getHistoryChatLogs?"
                + "receiver=" + bean.getCuserId()
                + "&sender=" + bean.getFriendId()
                + "&endTime=" + bean.getTimestamp()
                + "&size=" + IMConstants.NUM_GET_SINGLE_CHAT_MESSAGE_EVERY_TIME;
    }

    @Override
    protected List<MessageBean> getMessageListFromLocalDB(MessageBean messageBean) {
        return IMDBFactory.getInstance(this).getRecordsManager().getHistoryRecords(messageBean);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void sendEmail() {
        ArrayList<UserInfo> userInfs = new ArrayList<>();
        if( !TextUtils.isEmpty(userInfo.getMail()) && !userInfo.getMail().equals("null")){
            userInfs.add(userInfo);
            Intent intent = new Intent();
            intent.putExtra("userInfos", userInfs);
            BaseApplication application = (BaseApplication) getApplication();
            application.getUIController().onSendEmailClickListener(this, intent);
        }
    }

    @Override
    public void onATClickedListener() {

    }
}
