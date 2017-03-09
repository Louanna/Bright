package com.movit.platform.im.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.im.R;
import com.movit.platform.im.broadcast.HomeKeyEventReceiver;
import com.movit.platform.im.broadcast.MessageReceiver;
import com.movit.platform.im.broadcast.ScreenReceiver;
import com.movit.platform.im.broadcast.SystemReceiver;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.helper.XmppHelper;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.im.module.group.entities.Group;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/9.
 */

public class IMBaseActivity extends FragmentActivity implements MessageReceiver.CallBack, SystemReceiver.CallBack {

    private ScreenReceiver screenReceiver;
    private HomeKeyEventReceiver mHomeKeyEventReceiver;

    private MessageReceiver messageReceiver;
    private SystemReceiver systemReceiver;

    private CusDialog dialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHomeKeyEventReceiver = new HomeKeyEventReceiver();
        //注册广播
        registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        screenReceiver = new ScreenReceiver();
        //注册广播
        registerReceiver(screenReceiver, new IntentFilter(
                Intent.ACTION_SCREEN_OFF));

        //消息接收广播
        IntentFilter filter = new IntentFilter();
        messageReceiver = new MessageReceiver(this);
        filter.addAction(CommConstants.ACTION_NEW_MESSAGE);
        filter.addAction(CommConstants.ACTION_SET_REDPOINT);
        filter.addAction(CommConstants.MSG_UPDATE_SEND_STATUS_ACTION);
        filter.addAction(CommConstants.ACTION_SESSION_MESSAGE_LIST);
        filter.addAction(CommConstants.ACTION_GROUP_LIST_RESPONSE);
        registerReceiver(messageReceiver, filter);
    }

    protected void registReceiver(){

        //IM系统消息广播
        systemReceiver = new SystemReceiver();
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(CommConstants.ACTION_MY_INVITE);
        filter3.addAction(CommConstants.ACTION_MY_KICKED);
        filter3.addAction(CommConstants.ACTION_GROUP_DISPALYNAME_CHANGES);
        filter3.addAction(CommConstants.ACTION_GROUP_DISSOLVE_CHANGES);
        filter3.addAction(CommConstants.ACTION_GROUP_MEMBERS_CHANGES);
        registerReceiver(systemReceiver, filter3);
    }

    protected void unRegistReceiver(){
        unregisterReceiver(systemReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!XmppManager.getInstance().isConnected()) {
            XmppHelper xmppHelper = new XmppHelper(getApplicationContext());
            xmppHelper.loginXMPP();
        }

        IMConstants.sysCallback = this;

        //以下代码与IM无关,EOP中换肤才需要,其他项目不需要可以删除
        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(com.movit.platform.cloud.R.id.common_top_layout);
            if (layout != null)
                layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mHomeKeyEventReceiver);
        unregisterReceiver(screenReceiver);
        unregisterReceiver(messageReceiver);

        if (dialogUtil != null && dialogUtil.isShowing()) {
            dialogUtil.dismiss();
        }

        super.onDestroy();
    }


    @Override
    public void setRedPoint(int point) {

    }

    @Override
    public void updateRecordList(Map<String, Integer> recordMap) {

    }

    @Override
    public void receiveNewMessage(MessageBean messageBean) {

    }

    @Override
    public void receiveSessionList(List<MessageBean> messageBeans, String atMessage) {

    }

    @Override
    public void updateGroup() {

    }

    @Override
    public void afterKicked(String roomName, String displayName) {
        iniDialog(getString(R.string.admin_removed_you));
    }

    @Override
    public void afterInvited(String roomName, String inviter, String invitee, Group group) {
        MessageBean messageBean = new MessageBean();
        messageBean.setRoomId(roomName);
        String cinviter = getCName(inviter, messageBean);
        ToastUtils.showToast(this, cinviter+getString(R.string.invite_join_group));
    }

    @Override
    public void afterMemberChanged(String roomName, String affecteds, String displayName, String type) {
        if (StringUtils.notEmpty(roomName) && StringUtils.notEmpty(affecteds) && StringUtils.notEmpty(type)){
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
        }
    }

    @Override
    public void afterGroupNameChanged(String roomName, String displayName) {
        ToastUtils.showToast(this, getString(R.string.group_name_changed));
    }

    @Override
    public void afterGroupDisolved(String roomName, String displayName) {
//        iniDialog(getString(R.string.admin_dissolve_success));
        ToastUtils.showToast(this, getString(R.string.admin_dissolve_success));
    }

    public void iniDialog(String title) {
        dialogUtil = CusDialog.getInstance();
        dialogUtil.showVersionDialog(this);
        dialogUtil.setTitleDialog(title);
        dialogUtil.setConfirmClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogUtil.dismiss();
            }
        });
    }

    protected String getUserId(String adname) {
        UserDao dao = UserDao.getInstance(this);
        UserInfo userInfo = dao.getUserInfoByADName(adname);
        return null != userInfo ? userInfo.getId() : "";
    }

    protected String getCName(String adname) {
        UserDao dao = UserDao.getInstance(this);
        UserInfo userInfo = dao.getUserInfoByADName(adname);
        String cname = userInfo.getEmpCname().split("\\.")[0];
        return cname;
    }

    protected String getCName(String adname, MessageBean messageBean) {

        if (StringUtils.notEmpty(adname)) {

            UserDao dao = UserDao.getInstance(this);
            UserInfo userInfo = dao.getUserInfoByADName(adname);

            String cname = "";

            switch (IMConstants.groupsMap.get(messageBean.getRoomId()).getType()) {
                case CommConstants.CHAT_TYPE_GROUP_PERSON:
                    cname = userInfo.getEmpCname().split("\\.")[0];
                    break;
                case CommConstants.CHAT_TYPE_GROUP_ANS:
                    cname = IMConstants.ansGroupMembers.get(messageBean.getRoomId() + "," + userInfo.getId());
                    break;
                default:
                    break;
            }

            return cname;
        }
        return "";
    }
}
