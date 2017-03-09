package com.movit.platform.im.module.group.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
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
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.single.adapter.ChatAdapter;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;

public class GroupChatActivity extends ChatBaseActivity {

    private String subject = "";// 主题
    private String groupId;// 组id
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        IMManager.leaveGroupSession(sessionObjId.toLowerCase());
        IMConstants.atMembers.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        IMConstants.atMembers.clear();
        super.onBackPressed();
    }

    @Override
    protected void initData() {
        mContext = this;
        ctype = CommConstants.CHAT_TYPE_GROUP;
        MessageManager.getInstance(this).setOnSendMsgProcessListener(this);
        message_pool = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        sessionObjId = bundle.getString("room");

        group = IMConstants.groupsMap.get(sessionObjId);
        groupId = null != group ? group.getId() : sessionObjId;
        groupType = null != group ? group.getType() : bundle.getInt(CommConstants.KEY_GROUP_TYPE);
        subject = null != group ? group.getDisplayName() : bundle.getString("subject");

        IMConstants.CHATTING_ID = sessionObjId;
        IMConstants.CHATTING_TYPE = "group";

        //进入chat界面，先从数据库中取本地聊天记录
        getDBRecords();

        //从其他窗口发起视频会议时，需做如下处理
        if (bundle.getBoolean("meeting", false)) {
            sendMeeting();
        }
    }

    @Override
    protected void onResume() {
        group = IMConstants.groupsMap.get(sessionObjId);
        super.onResume();
    }

    @Override
    protected void sendEnterSession() {
        // 进入群组聊天会话
        try {
            String startTime = IMDBFactory.getInstance(this).getRecordsManager().getStartTimeAndEndTime(sessionObjId);
            String endTime = "";
            if (StringUtils.notEmpty(startTime)) {
                startTime = startTime.split(",")[0];
                if (startTime.split(",").length == 2) {
                    endTime = startTime.split(",")[1];
                }
            }

            //通知XMPP已上线
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "enterRoomSessionDialog");
            jsonObject.put("roomID", sessionObjId.toLowerCase());
            jsonObject.put("startTime", startTime);
            jsonObject.put("endTime", endTime);
            jsonObject.put("pageSize", 200);
            ChatManager manager = XmppManager.getInstance()
                    .getConnection().getChatManager();
            Chat chat = manager.createChat("admin" + MessageManager.SUFFIX,
                    null);
            chat.sendMessage(jsonObject.toString());

            StringBuilder sb = new StringBuilder();
            sb.append("roomName=").append(sessionObjId.toLowerCase()).append("&startTime=").append(startTime).append("&size=").append(200);

            String url = CommConstants.URL_EOP_IM + "im/groupChatLog/getGroupChatLogs?" + sb.toString();
            HttpManager.getJsonWithToken(url, new EnterSessionCallback());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initTopBar() {
        mTopTitle.setText(subject);
//        mTopLeftImage.setBackgroundResource(R.drawable.top_back);
        mTopRightImage.setImageResource(R.drawable.top_more);
        mTopRightImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(GroupChatActivity.this,
                        ChatDetailActivity.class).putExtra("ctype",
                        CommConstants.CHAT_TYPE_GROUP).putExtra("groupName",
                        group.getGroupName()).putExtra(CommConstants.KEY_GROUP_TYPE, groupType),REQUEST_CODE_CHAT_DETAIL_PAGE);
            }
        });
    }

    @Override
    protected void initListAdapter() {
        switch (groupType) {
            case CommConstants.CHAT_TYPE_GROUP_ANS:
                //匿名聊天
                chatAdapter = new ChatAdapter(this, message_pool, mMsgListView,
                        handler, this, CommConstants.CHAT_TYPE_GROUP_ANS);
                break;
            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                //实名群组
                chatAdapter = new ChatAdapter(this, message_pool, mMsgListView,
                        handler, this, CommConstants.CHAT_TYPE_GROUP_PERSON);
                break;
            default:
                break;
        }
    }

    @Override
    protected void sendMessageIfNotNull() {
        // 保存消息并发送
        String content = mChatEditText.getText().toString().trim();
        mChatEditText.setText("");
        if (content != null && !content.equals("")) {
            JSONObject textJsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            try {
                textJsonObject.put("text", content);
                //判断是否为@XX
                if (IMConstants.atMembers.size() > 0) {

                    for (String key : IMConstants.atMembers.keySet()) {
                        JSONObject itemObj = new JSONObject();
                        itemObj.put("userId", IMConstants.atMembers.get(key).getId());
                        itemObj.put("empAdname", IMConstants.atMembers.get(key).getEmpAdname());
                        jsonArray.put(itemObj);
                    }

                } else {
                    jsonArray = null;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            MessageManager.getInstance(this).sendMessageToCMS(
                    UUID.randomUUID().toString(), textJsonObject.toString(), CommConstants.MSG_TYPE_TEXT, ctype,
                    null, sessionObjId, subject, jsonArray, groupType, groupId);
        }
    }

    @Override
    protected void sendVoiceMessage(String content) {
        MessageManager.getInstance(GroupChatActivity.this)
                .sendMessageToCMS(UUID.randomUUID().toString(), content, CommConstants.MSG_TYPE_AUDIO, ctype,
                        null, sessionObjId, subject, null, groupType, groupId);
    }

    @Override
    protected void sendPicMessage(String content) {
        MessageManager.getInstance(GroupChatActivity.this)
                .sendMessageToCMS(UUID.randomUUID().toString(), content, CommConstants.MSG_TYPE_PIC, ctype, null,
                        sessionObjId, subject, null, groupType, groupId);
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
                                String mettingName = "百度会议";
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("url", meetingURL);
                                    jsonObject.put("name", mettingName);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                MessageManager.getInstance(GroupChatActivity.this).sendMessageToCMS(
                                        UUID.randomUUID().toString(), jsonObject.toString(), CommConstants.MSG_TYPE_METTING, ctype,
                                        null, sessionObjId, subject, null, groupType, groupId);
                            } else {
                                ToastUtils.showToast(GroupChatActivity.this, "创建会议失败");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void sendVideoMessage(String json) {
        MessageManager.getInstance(GroupChatActivity.this)
                .sendMessageToCMS(UUID.randomUUID().toString(), json, CommConstants.MSG_TYPE_VIDEO, ctype, null,
                        sessionObjId, subject, null, groupType, groupId);
    }

    @Override
    protected void sendFile(String content, String msgType) {
        MessageManager.getInstance(this).sendMessageToCMS(UUID.randomUUID().toString(), content, msgType, ctype, null,
                sessionObjId, subject, null, groupType, groupId);
    }

    @Override
    protected void sendLocation(String content) {
        MessageManager.getInstance(this).sendMessageToCMS(UUID.randomUUID().toString(), content,
                CommConstants.MSG_TYPE_LOCATION, ctype, null,
                sessionObjId, subject, null, groupType, groupId);
    }

    @Override
    protected void showAtTips(MessageBean msgBean, String atMessage) {

        if (StringUtils.notEmpty(msgBean)) {
            switch (groupType) {
                case CommConstants.CHAT_TYPE_GROUP_ANS:
                    //匿名群组
                    isShowAtTips(msgBean.isATMessage(), IMConstants.ansGroupMembers.get(msgBean.getRoomId() + "," + msgBean.getUserInfo().getId()) + getString(R.string.call_you));
                    break;
                case CommConstants.CHAT_TYPE_GROUP_PERSON:
                    //实名群组
                    isShowAtTips(msgBean.isATMessage(), msgBean.getUserInfo().getEmpCname() + getString(R.string.call_you));
                    break;
            }
        } else {
            isShowAtTips(atMessage.length() != 0, atMessage);
        }
    }

    @Override
    protected String getGetMessageListURL(MessageBean bean) {
        return CommConstants.URL_EOP_IM + "im/groupChatLog/getHistoryGroupChatLogs?"
                + "roomName=" + sessionObjId + "&endTime=" + bean.getTimestamp()
                + "&size=" + IMConstants.NUM_GET_GROUP_CHAT_MESSAGE_EVERY_TIME;
    }

    @Override
    protected List<MessageBean> getMessageListFromLocalDB(MessageBean messageBean) {
        return IMDBFactory.getInstance(this).getRecordsManager().getRoomHistoryRecords(messageBean);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void sendEmail() {
        try {
            if (group.getMembers() == null || group.getMembers().isEmpty()) {
                return;
            }
            ArrayList<UserInfo> userInfs = new ArrayList<>();
            String mEmail = MFSPHelper.getString(CommConstants.EMAIL_ADDRESS);
            if (mEmail == null) {
                mEmail = "";
            }
            for (UserInfo u : group.getMembers()) {
                if (!StringUtils.empty(u.getMail()) && !u.getMail().equals("null") && !u.getMail().equals(mEmail)) {
                    userInfs.add(u);
                }
            }
            if (userInfs.size() > 0) {
                Intent intent = new Intent();
                intent.putExtra("userInfos", userInfs);
                BaseApplication application = (BaseApplication) getApplication();
                application.getUIController().onSendEmailClickListener(this, intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onATClickedListener() {
        //跳转到选人页面
        Intent intent = new Intent(this, ATMemberActivity.class);
        intent.putExtra(IMConstants.KEY_GROUP, group);
        this.startActivityForResult(intent, IMConstants.REQUEST_CODE_MEMBER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case IMConstants.REQUEST_CODE_MEMBER:
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    //记录@XX
                    UserInfo member = (UserInfo) bundle.getSerializable(IMConstants.KEY_MEMBER);
                    String member_name = "";

                    switch (group.getType()) {
                        case CommConstants.CHAT_TYPE_GROUP_PERSON:
                            member_name = member.getEmpCname();
                            break;
                        case CommConstants.CHAT_TYPE_GROUP_ANS:
                            member_name = IMConstants.ansGroupMembers.get(group.getGroupName() + "," + member.getId());
                            break;
                        default:
                            break;
                    }

                    StringBuffer indexKey = new StringBuffer();

                    //此处加一：name长度+@字符的总长
                    //'#'为分隔符，前后都要加
                    for (int i = 0; i < member_name.length() + 1; i++) {
                        indexKey.append("#").append(mChatEditText.getSelectionStart() + i);
                    }
                    indexKey.append("#");
                    IMConstants.atMembers.put(indexKey.toString(), member);

                    //显示@XX
                    mChatEditText.append(member_name + " ");
                    mChatEditText.setSelection(mChatEditText.getText().length());
                }else {
                    mChatEditText.setText("");

//                    String txt = mChatEditText.getText().toString();
//                    mChatEditText.setText(txt.substring(0,txt.length()-1));
//                    mChatEditText.setSelection(mChatEditText.getText().length());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void afterKicked(String roomName, String displayName) {
        if (sessionObjId.equalsIgnoreCase(roomName)) {
            finish();
        }
    }

    @Override
    public void afterMemberChanged(String roomName, String affecteds, String displayName, String type) {
        if (sessionObjId.equalsIgnoreCase(roomName)) {
            String names = "";
            String[] members = affecteds.split(",");
            names = getCName(members[0]);
            if (members.length > 1) {
                names = names + "等" + members.length + "人";
            }
            if (type.equals("0")) {

                switch (IMConstants.groupsMap.get(roomName).getType()) {
                    case CommConstants.CHAT_TYPE_GROUP_ANS:
                        names = IMConstants.ansGroupMembers.get(roomName + "," + getUserId(members[0]));
                        break;
                    default:
                        break;
                }

                ToastUtils.showToast(this, names + getString(R.string.join_group));
            } else if (type.equals("1")) {

                switch (IMConstants.groupsMap.get(roomName).getType()) {
                    case CommConstants.CHAT_TYPE_GROUP_ANS:
                        names = IMConstants.ansGroupMembers.get(roomName + "," + getUserId(members[0]));
                        break;
                    default:
                        break;
                }
                ToastUtils.showToast(this, names + getString(R.string.remove_group));
            } else if (type.equals("4")) {
                if (affecteds.equalsIgnoreCase(MFSPHelper
                        .getString(CommConstants.EMPADNAME))) {
                    // IMApplication.showToast(context, "您已退出群组!");
                } else {

                    switch (IMConstants.groupsMap.get(roomName).getType()) {
                        case CommConstants.CHAT_TYPE_GROUP_ANS:
                            names = IMConstants.ansGroupMembers.get(roomName + "," + getUserId(members[0]));
                            break;
                        default:
                            break;
                    }
                    ToastUtils.showToast(this, names + getString(R.string.exit_group));
                }
            }
            group = IMConstants.groupsMap.get(sessionObjId);
        }
    }

    @Override
    public void afterGroupNameChanged(String roomName, String displayName) {
        if (roomName.equalsIgnoreCase(group.getGroupName())) {
            ToastUtils.showToast(this, getString(R.string.group_name_changed));
            mTopTitle.setText(displayName);
            subject = displayName;
        }
    }

    @Override
    public void afterGroupDisolved(String roomName, String displayName) {
        if (sessionObjId.equalsIgnoreCase(roomName)) {
            iniDialog(getString(R.string.admin_dissolve_success));
        }
    }

}
