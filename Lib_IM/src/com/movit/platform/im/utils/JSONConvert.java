package com.movit.platform.im.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.Json2ObjUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.db.RecordsManager;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.group.entities.GroupLog;
import com.movit.platform.im.service.XMPPService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class JSONConvert {

    /**
     * 解析获取最近会话列表
     */
    public static Map<String, Object> json2MessageBean(String json, Context context) throws Exception {

        Map<String, Object> resultMap = new HashMap<>();

        ArrayList<MessageBean> messageBeans = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String content = "";
            if (jsonObject.has("content")) {
                content = jsonObject.getString("content");
            }
            String to = "";
            if (jsonObject.has("to")) {
                to = jsonObject.getString("to");
            }
            String timestamp = "";
            if (jsonObject.has("timestamp")) {
                timestamp = jsonObject.getString("timestamp");
            }
            String unreadCount = "0";
            if (jsonObject.has("unreadCount")) {
                unreadCount = jsonObject.getString("unreadCount");
            }
            String from = "";
            if (jsonObject.has("from")) {
                from = jsonObject.getString("from");
            }
            String type = "";
            if (jsonObject.has("type")) {
                type = jsonObject.getString("type");
            }

            String groupType = "";
            if (jsonObject.has("groupType")) {
                groupType = jsonObject.getString("groupType");
            }

            String msgId;

            String cuserId = MFSPHelper.getString(CommConstants.EMPADNAME);
            String friendId = "";
            String roomId = "";
            int rsflag = 0;
            int ctype = -1;
            String mtype;
            String formatTime;
            String subject = "";
            boolean fromWeChat = false;
            boolean isAtMessage = false;
            UserDao userDao = UserDao.getInstance(context);
            UserInfo userInfo;
            try {
                if (type.equals("1")) {
                    ctype = CommConstants.CHAT_TYPE_GROUP;
                    friendId = from;// 发送者id
                    roomId = to;
                    if (friendId.equalsIgnoreCase(cuserId)) {// 收到的是自己发出的消息
                        rsflag = CommConstants.MSG_SEND;
                    } else {
                        rsflag = CommConstants.MSG_RECEIVE;
                    }
                } else if (type.equals("0")) {
                    ctype = CommConstants.CHAT_TYPE_SINGLE;
                    String fromWho = from;
                    String toWho = to;
                    if (fromWho.equalsIgnoreCase(cuserId)) {
                        friendId = toWho;
                        rsflag = CommConstants.MSG_SEND;
                    } else {
                        friendId = fromWho;
                        rsflag = CommConstants.MSG_RECEIVE;
                    }
                }

                Matcher matcher = XMPPService.pattern.matcher(content);
                if (matcher.find()) {
                    content = content.substring(23);
                }
                JSONObject object = new JSONObject(content);
                mtype = object.getString("mtype");// T or A or P
                formatTime = object.getString("time");
                msgId = object.getString("msgId");

                if (object.has("groupType") && StringUtils.notEmpty(object.getInt("groupType"))) {
                    groupType = String.valueOf(object.getInt("groupType"));
                }

                JSONObject contentObject = object.getJSONObject("content");
                if (contentObject.has("fromWechatUser")) {
                    friendId = contentObject.getString("fromWechatUser");
                    fromWeChat = true;
                }
                userInfo = userDao.getUserInfoByADName(friendId);
                if(null != userInfo){
                    String userCName = userInfo.getEmpCname();
                    String userId = userInfo.getId();
                    //判断是否为NewMessage，如果是NewMessage才显示@提示
                    RecordsManager recordsManager = IMDBFactory.getInstance(context).getRecordsManager();
                    if (!recordsManager.isExisted(msgId)) {
                        if (object.has("atList") && null != object.getJSONArray("atList") && object.getJSONArray("atList").toString().contains(MFSPHelper.getString(CommConstants.USERID))) {
                            isAtMessage = true;
                            if (CommConstants.CHAT_TYPE_GROUP_ANS == object.getInt("groupType")) {
                                resultMap.put("atMessageContent", IMConstants.ansGroupMembers.get(roomId + "," + userId) + "提到了你");
                            } else {
                                resultMap.put("atMessageContent", userCName + "提到了你");
                            }
                        } else {
                            isAtMessage = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            if (!CommConstants.GROUP_ADMIN.equalsIgnoreCase(friendId) && !fromWeChat) {
                if (userInfo == null) {
                    continue;
                }
            }
            if (ctype == CommConstants.CHAT_TYPE_GROUP) {
                Group group;
                try {
                    group = IMConstants.groupsMap.get(roomId);
                    if (null != group) {
                        subject = group.getDisplayName();
                    } else {
                        if (jsonObject.has("groupName")) {
                            subject = jsonObject.getString("groupName");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            MessageBean bean = new MessageBean();
            bean.setGroupType(groupType);
            bean.setMsgId(msgId);
            bean.setContent(content);
            bean.setCtype(ctype);
            bean.setMtype(mtype);
            bean.setRsflag(rsflag);
            bean.setFormateTime(formatTime);
            bean.setTimestamp(timestamp);
            int count = Integer.parseInt(unreadCount);
            bean.setUnReadCount(count);
            if (count == 0) {
                bean.setIsread(CommConstants.MSG_READ);
            } else {
                bean.setIsread(CommConstants.MSG_UNREAD);
            }
            bean.setCuserId(cuserId);
            bean.setFriendId(friendId);
            bean.setRoomId(roomId);
            bean.setUserInfo(userInfo);
            bean.setSubject(subject);
            bean.setIsSend(CommConstants.MSG_SEND_SUCCESS);

            bean.setIsATMessage(isAtMessage);

            if (fromWeChat) {
                bean.setFromWechatUser(true);
            }
            messageBeans.add(bean);
        }
//        Collections.sort(messageBeans,new SortByST());
        resultMap.put("messageBean", messageBeans);
        return resultMap;
    }

   private static class SortByST implements Comparator {
        public int compare(Object o1, Object o2) {
            MessageBean s1 = (MessageBean) o1;
            MessageBean s2 = (MessageBean) o2;
            return s1.getTimestamp().compareTo(s2.getTimestamp());
        }
    }

    /**
     * 解析获取最近会话列表
     */
    public static MessageBean getMessageBean(MessageBean messageBean) throws Exception {

        JSONObject jsonObject = new JSONObject(messageBean.getContent());
        String unreadCount = "0",groupType="";
        if (jsonObject.has("unreadCount")) {
            unreadCount = jsonObject.getString("unreadCount");
        }
        if (jsonObject.has("groupType")) {
            groupType = jsonObject.getString("groupType");
        }

        String roomId = "";
        int ctype = -1;
        String mtype = "";
        String formatTime = "";
        String subject = "";
        boolean fromWeChat = false;
        boolean isAtMessage;

        try {
            mtype = jsonObject.getString("mtype");// T or A or P
            formatTime = jsonObject.getString("time");
            JSONObject contentObject = jsonObject.getJSONObject("content");
            if (contentObject.has("fromWechatUser")) {
                fromWeChat = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isAtMessage = jsonObject.has("atList")
                && null != jsonObject.getJSONArray("atList")
                && jsonObject.getJSONArray("atList").toString().contains(MFSPHelper.getString(CommConstants.USERID));

        if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            Group group;
            try {
                group = IMConstants.groupsMap.get(roomId);
                subject = group.getDisplayName();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        MessageBean bean = messageBean;
        bean.setMtype(mtype);
        bean.setGroupType(groupType);
        bean.setFormateTime(formatTime);
        int count = Integer.parseInt(unreadCount);
        bean.setUnReadCount(count);
        if (count == 0) {
            bean.setIsread(CommConstants.MSG_READ);
        } else {
            bean.setIsread(CommConstants.MSG_UNREAD);
        }
//        bean.setRoomId(roomId);
        bean.setSubject(subject);
        bean.setIsATMessage(isAtMessage);

        if (StringUtils.notEmpty(bean.getTimestamp())) {
            bean.setIsSend(CommConstants.MSG_SEND_SUCCESS);
        }

        if (fromWeChat) {
            bean.setFromWechatUser(true);
        }
        return bean;
    }

    public static Group getGroupFromJson(String json, Context context) throws Exception {
        boolean useEoopApi = false;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            useEoopApi = appInfo.metaData.getBoolean("USE_EOOP_API", false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Group group = new Group();
        JSONObject jsonObject = new JSONObject(json);
        String delflg = "";
        if (jsonObject.has("delflg")) {
            delflg = jsonObject.getString("delflg");
        }
        String groupName = "";
        if (jsonObject.has("groupName")) {
            groupName = jsonObject.getString("groupName");
        }
        String createrId = "";
        if (jsonObject.has("createrId")) {
            createrId = jsonObject.getString("createrId");
        }
        String[] adminIds = null;
        if (jsonObject.has("adminIds")) {
            JSONArray array = jsonObject.getJSONArray("adminIds");
            if (array != null) {
                adminIds = new String[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    adminIds[i] = array.getString(i);
                }
            }
        }
        String roomServerName = "";
        if (jsonObject.has("roomServerName")) {
            roomServerName = jsonObject.getString("roomServerName");
        }
        String imServerName = "";
        if (jsonObject.has("imServerName")) {
            imServerName = jsonObject.getString("imServerName");
        }
        String id = "";
        if (jsonObject.has("id")) {
            id = jsonObject.getString("id");
        }
        int type = 0;
        if (jsonObject.has("type")) {
            type = jsonObject.getInt("type");
        }
        String displayName = "";
        if (jsonObject.has("displayName")) {
            displayName = jsonObject.getString("displayName");
        }
        String description = "";
        if (jsonObject.has("description")) {
            description = jsonObject.getString("description");
        }
        String dissolveDate = "";
        if (jsonObject.has("dissolveDate")) {
            dissolveDate = jsonObject.getString("dissolveDate");
        }
        String createDate = "";
        if (jsonObject.has("createDate")) {
            createDate = jsonObject.getString("createDate");
        }

        List<UserInfo> userInfos = new ArrayList<>();
        if (jsonObject.has("members")) {
            JSONArray arry = jsonObject.getJSONArray("members");
            for (int i = 0; i < arry.length(); i++) {
                if (useEoopApi) {
                    JSONObject info = arry.getJSONObject(i);
                    if (info.has("id")) {
                        UserDao dao = UserDao.getInstance(context);
                        UserInfo userInfo = dao.getUserInfoById(info.getString("id"));
                        if (userInfo != null) {
                            userInfos.add(userInfo);
                            if (info.has("nickName")) {
                                userInfo.setNickName(info.getString("nickName"));
                            }
                            IMConstants.ansGroupMembers.put(groupName + "," + userInfo.getId(), userInfo.getNickName());
                        }
                    }
                } else {
                    UserInfo userInfo = Json2ObjUtils.getUserInfoFromJson(arry.getJSONObject(i)
                            .toString());
                    userInfos.add(userInfo);
                    IMConstants.ansGroupMembers.put(groupName + "," + userInfo.getId(), userInfo.getNickName());
                }
            }
        }
        List<GroupLog> groupLogs = new ArrayList<>();
        if (jsonObject.has("joinGroupLog")) {
            JSONArray arry = jsonObject.getJSONArray("joinGroupLog");
            for (int i = 0; i < arry.length(); i++) {
                GroupLog groupLog = new GroupLog();
                JSONObject object = arry.getJSONObject(i);

                String groupId = "";
                if (object.has("groupId")) {
                    groupId = object.getString("groupId");
                }
                String leaveTime = "";
                if (object.has("leaveTime")) {
                    leaveTime = object.getString("leaveTime");
                }
                String userId = "";
                if (object.has("userId")) {
                    userId = object.getString("userId");
                }
                String joinTime = "";
                if (object.has("joinTime")) {
                    joinTime = object.getString("joinTime");
                }
                String id2 = "";
                if (object.has("id")) {
                    id2 = object.getString("id");
                }
                groupLog.setGroupId(groupId);
                groupLog.setId(id2);
                groupLog.setJoinTime(joinTime);
                groupLog.setLeaveTime(leaveTime);
                groupLog.setUserId(userId);
                groupLogs.add(groupLog);
            }
        }
        group.setAdminIds(adminIds);
        group.setCreateDate(createDate);
        group.setCreaterId(createrId);
        group.setDelflg(delflg);
        group.setDescription(description);
        group.setDisplayName(displayName);
        group.setDissolveDate(dissolveDate);
        group.setGroupName(groupName);
        group.setId(id);
        group.setImServerName(imServerName);
        group.setJoinGroupLog(groupLogs);
        group.setMembers(userInfos);
        group.setRoomServerName(roomServerName);
        group.setType(type);
        return group;
    }

}
