package com.movit.platform.im.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.entities.Group;

import java.util.Date;

/**
 * Created by Administrator on 2015/12/22.
 */
public class SystemReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (CommConstants.ACTION_MY_KICKED.equals(action)) {
            // 群成员退出
            String roomName = intent.getStringExtra("roomName");
            String displayName = intent.getStringExtra("displayName");
            IMConstants.Dingdong(context);
            doKicked(context, roomName, displayName);
            IMConstants.sysCallback.afterKicked(roomName, displayName);
        } else if (CommConstants.ACTION_MY_INVITE.equals(action)) {
            //被邀请加入群聊
            String roomName = intent.getStringExtra("roomName");
            String inviter = intent.getStringExtra("inviter");
            String invitee = intent.getStringExtra("invitee");
            Group group = (Group) intent.getSerializableExtra("group");
            IMConstants.Dingdong(context);
            doInvited(context, roomName, inviter, invitee, group);
            IMConstants.sysCallback.afterInvited(roomName, inviter, invitee, group);
        } else if (CommConstants.ACTION_GROUP_MEMBERS_CHANGES.equals(action)) {
            // 群成员变动
            String roomName = intent.getStringExtra("groupName");
            String affecteds = intent.getStringExtra("affecteds");
            String displayName = intent.getStringExtra("displayName");
            String type = intent.getStringExtra("type");
            doMemberChanged(context,roomName, affecteds, displayName, type);
            IMConstants.sysCallback.afterMemberChanged(roomName, affecteds, displayName, type);
        } else if (CommConstants.ACTION_GROUP_DISPALYNAME_CHANGES.equals(action)) {
            // 群名称修改
            String displayName = intent.getStringExtra("displayName");
            String roomName = intent.getStringExtra("roomName");
            IMConstants.Dingdong(context);
            doGroupNameChanged(roomName, displayName);
            IMConstants.sysCallback.afterGroupNameChanged(roomName, displayName);
        } else if (CommConstants.ACTION_GROUP_DISSOLVE_CHANGES.equals(action)) {
            // 群解散
            String roomName = intent.getStringExtra("roomName");
            String displayName = intent.getStringExtra("displayName");
            IMConstants.Dingdong(context);
            doGroupDisolved(context,roomName, displayName);
            IMConstants.sysCallback.afterGroupDisolved(roomName, displayName);
        }

        //更新首页tab菜单聊天右上角未读数
        setPoint(context);
    }

    private void setPoint(Context context) {
        Intent intent = new Intent(CommConstants.ACTION_SET_REDPOINT);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    private void doKicked(Context context, String roomName, String displayName) {
        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
            if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                    && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                IMConstants.contactListDatas.remove(i);
                break;
            }
        }

        // 再添加系统消息
        MessageBean messageBean = new MessageBean();
        messageBean.setRoomId(roomName);

        Date d = new Date();
        messageBean.setTimestamp(DateUtils.date2Str(d,
                DateUtils.FORMAT_FULL));
        messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
        messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);
        messageBean.setContent(context.getString(R.string.admin_removed_you));
        messageBean.setMtype(CommConstants.MSG_TYPE_KICK);
        messageBean.setUnReadCount(1);
        messageBean.setSubject(displayName);
        if (!IMConstants.sysMsgList.isEmpty()) {
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                    messageBean.setUnReadCount(count + 1);
                    IMConstants.contactListDatas.remove(i);
                    break;
                }
            }
        }
        IMConstants.contactListDatas.add(0, messageBean);
        IMConstants.sysMsgList.add(0, messageBean);
    }

    private void doInvited(Context context, String roomName, String inviter, String invitee, Group group) {
        MessageBean messageBean = new MessageBean();
        messageBean.setRoomId(roomName);
        String cinviter = getCName(context, inviter, messageBean);
        String cinvitee = getCName(context, invitee, messageBean);

        Date d = new Date();
        messageBean.setTimestamp(DateUtils.date2Str(d,
                DateUtils.FORMAT_FULL));
        messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
        messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);
        messageBean.setContent(cinviter + context.getString(R.string.invite_join_group));
        messageBean.setCuserId(cinvitee);
        messageBean.setFriendId(cinviter);
        messageBean.setMtype(CommConstants.MSG_TYPE_INVITE);
        messageBean.setUnReadCount(1);
        messageBean.setSubject(group.getDisplayName());

        if (!IMConstants.sysMsgList.isEmpty()) {
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                    messageBean.setUnReadCount(count + 1);
                    IMConstants.contactListDatas.remove(i);
                    break;
                }
            }
        }
        IMConstants.contactListDatas.add(0, messageBean);
        IMConstants.sysMsgList.add(0, messageBean);
    }

    private void doMemberChanged(Context context, String roomName, String affecteds, String displayName, String type) {

        if (null == type) {
            //APP登录时，getGroupList是异步获取的，获取成功后会发送广播ACTION_GROUP_MEMBERS_CHANGES
            //这个时候Intent是无对象传递的
            //收到广播后，刷新列表页面，保证列表页面的groupName是有值的
//            if(null!=recentAdapter){
//                recentAdapter.notifyDataSetChanged();
//            }
            return;
        }
        IMConstants.Dingdong(context);
        String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
        String userId = MFSPHelper.getString(CommConstants.USERID);
        if (type.equals("0")) {
            // 自己加入群组的时候
            if (affecteds.equalsIgnoreCase(adname)) {
                return;
            }
        } else if (type.equals("1")) {
            // 有人被踢了
            // 管理员踢人的时候，不需要收到消息
            Group group = IMConstants.groupsMap.get(roomName);
            if (userId.equals(group.getCreaterId())) {
                return;
            }
        } else if (type.equals("4")) {
            // 自己退出
            if (affecteds.equalsIgnoreCase(adname)) {
                // 先删除原来的群组消息
                for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                    if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                            && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                        IMConstants.contactListDatas.remove(i);
                        break;
                    }
                }
                IMConstants.sysCallback.afterMemberChanged(roomName, affecteds, displayName, type);
                return;
            }
        }
        boolean flag = affecteds.contains(adname);

        MessageBean messageBean = new MessageBean();
        Date d = new Date();
        messageBean.setTimestamp(DateUtils.date2Str(d,
                DateUtils.FORMAT_FULL));
        messageBean.setFormateTime(DateUtils.date2Str(d, DateUtils.FORMAT_FULL));
        messageBean.setRoomId(roomName);
        messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);

        String names = "";
        String[] members = affecteds.split(",");
        int num = members.length;
        if (flag) {
            if (adname.equals(members[0])) {
                names = getCName(context, members[1], messageBean);
            } else {
                names = getCName(context, members[0], messageBean);
            }
            num = members.length - 1;
            if (members.length > 2) {
                names = names + context.getString(R.string.etc) + num + context.getString(R.string.unit_person);
            }
        } else {
            names = getCName(context, members[0], messageBean);
            if (members.length > 1) {
                names = names + context.getString(R.string.etc) + num + context.getString(R.string.unit_person);
            }
        }

        /**
         * 0表示新增成员通知; 1表示踢出成员通知; 2表示变更displayName通知; 3解散群通知;4用户退群通知
         */
        if (type.equals("0")) {
            messageBean.setContent(names + context.getString(R.string.join) + displayName);
            messageBean.setRsflag(0);// 使用rsflag属性做判断
        } else if (type.equals("1")) {
            messageBean.setContent(names + context.getString(R.string.beremoved_from_group));
            messageBean.setRsflag(1);
        } else if (type.equals("4")) {
            messageBean.setContent(names + context.getString(R.string.exited) + displayName);
            messageBean.setRsflag(4);
        }
        messageBean.setMtype(CommConstants.MSG_TYPE_MEMBERS_CHANGE);
        messageBean.setUnReadCount(1);
        messageBean.setSubject(displayName);
        messageBean.setFriendId(names);

        if (!IMConstants.sysMsgList.isEmpty()) {
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                    messageBean.setUnReadCount(count + 1);
                    IMConstants.contactListDatas.remove(i);
                    break;
                }
            }
        }
        IMConstants.contactListDatas.add(0, messageBean);
        IMConstants.sysMsgList.add(0, messageBean);
    }

    private void doGroupNameChanged(String roomName, String displayName) {
        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
            if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                    && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                IMConstants.contactListDatas.get(i).setSubject(displayName);
            }
        }
    }

    private void doGroupDisolved(Context context,String roomName, String displayName){
        // // 先删除原来的群组消息
        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
            if (roomName.equalsIgnoreCase(IMConstants.contactListDatas.get(i).getRoomId())
                    && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                IMConstants.contactListDatas.remove(i);
                break;
            }
        }

        IMConstants.groupsMap.remove(roomName);

        // 再添加系统消息
        MessageBean messageBean = new MessageBean();
        Date d = new Date();
        messageBean.setTimestamp(DateUtils.date2Str(d,
                DateUtils.FORMAT_FULL));
        messageBean.setFormateTime(DateUtils.date2Str(d,DateUtils.FORMAT_FULL));
        messageBean.setRoomId(roomName);
        messageBean.setCtype(CommConstants.CHAT_TYPE_SYSTEM);
        messageBean.setContent(context.getString(R.string.admin_dissovled) + displayName);
        messageBean.setMtype(CommConstants.MSG_TYPE_DISSOLVE);
        messageBean.setUnReadCount(1);
        messageBean.setSubject(displayName);

        if (!IMConstants.sysMsgList.isEmpty()) {
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    int count = IMConstants.contactListDatas.get(i).getUnReadCount();
                    messageBean.setUnReadCount(count + 1);
                    IMConstants.contactListDatas.remove(i);
                    break;
                }
            }
        }
        IMConstants.contactListDatas.add(0, messageBean);
        IMConstants.sysMsgList.add(0, messageBean);
    }

    //不同的界面响应不同
    public interface CallBack {
        //执行移除Member响应
        public void afterKicked(String roomName, String displayName);

        //执行加入群组的响应
        public void afterInvited(String roomName, String inviter, String invitee, Group group);

        //执行Member Change响应
        public void afterMemberChanged(String roomName, String affecteds, String displayName, String type);

        //执行Group Name Change响应
        public void afterGroupNameChanged(String roomName, String displayName);

        //执行Group Dissolved响应
        public void afterGroupDisolved(String roomName, String displayName);
    }

    protected String getCName(Context context, String adname, MessageBean messageBean) {

        if (StringUtils.notEmpty(adname)) {

            UserDao dao = UserDao.getInstance(context);
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
