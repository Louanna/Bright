package com.movit.platform.im.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.androidquery.AQuery;
import com.movit.platform.common.api.IGroupManager;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.BitmapCallback;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.utils.ChatTools;
import com.movit.platform.im.utils.JSONConvert;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class GroupManager implements IGroupManager {

    private Context mContext;
    private Handler handler = new Handler();
    private int joinCount = 0;

    private static GroupManager manager;
//    private List<Bitmap> memberBitmaps;
    private AQuery aq;

    private GroupManager(Context mContext) {
        this.mContext = mContext;
//        memberBitmaps = new ArrayList<>();
        aq = new AQuery(mContext);
    }

    public static GroupManager getInstance(Context mContext) {
        if (manager == null) {
            manager = new GroupManager(mContext);
        }
        return manager;
    }

    public void clean() {
        IMConstants.groupListDatas.clear();
        IMConstants.groupsMap.clear();
    }

    /**
     * 添加Group成员
     **/
    @Override
    public void addMembers(final String groupId, final String memberIds,
                           final Handler handler) {

        OkHttpUtils.postWithToken()
                .url(CommConstants.URL_IM_ADD_MEMBERS)
                .addParams("userId", MFSPHelper.getString(CommConstants.USERID))
                .addParams("groupId", groupId)
                .addParams("memberIds", memberIds)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(5);
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            boolean ok = jsonObject.getBoolean("ok");
                            if (ok) {
                                handler.sendEmptyMessage(4);
                            } else {
                                handler.sendEmptyMessage(5);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(5);
                        }
                    }
                });
    }

    /**
     * 退出Group
     **/
    public void logoutGroup(final String groupId, final Handler handler) {

        String url = String.format(CommConstants.URL_IM_BOWOUT,
                MFSPHelper.getString(CommConstants.USERID), groupId);
        OkHttpUtils.postWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            boolean ok = jsonObject.getBoolean("ok");
                            if (ok) {
                                handler.sendEmptyMessage(5);
                            } else {
                                handler.sendEmptyMessage(3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(3);
                        }
                    }
                });
    }

    /**
     * 解散Group
     **/
    public void dissolveGroup(final String groupId, final Handler handler,
                              final int postion) {

        String url = String.format(CommConstants.URL_IM_DISSOLVE,
                MFSPHelper.getString(CommConstants.USERID), groupId);
        OkHttpUtils.postWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            boolean ok = jsonObject.getBoolean("ok");
                            if (ok) {
                                handler.obtainMessage(4, postion).sendToTarget();
                            } else {
                                handler.sendEmptyMessage(3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(3);
                        }
                    }
                });
    }

    public void getGroupList() {
//        clean();
        String url = String.format(CommConstants.URL_IM_GROUP_LIST, MFSPHelper.getString(CommConstants.USERID));
        HttpManager.getJsonWithToken(url, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {

                if (StringUtils.notEmpty(response)) {

                    List<Group> groupDatas = new ArrayList<>();
                    //Key：groupName
                    Map<String, Group> groupMap = new HashMap<>();

                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject objValues = jsonObject.getJSONObject("objValue");
                    JSONArray personalArray = objValues.getJSONArray("personalGroups");
                    JSONArray orgArray = objValues.getJSONArray("orgGroups");
                    JSONArray adminArray = objValues.getJSONArray("adminGroups");
                    JSONArray taskArray = objValues.getJSONArray("taskGroups");
                    JSONArray nickGroupArray = objValues.getJSONArray("nickNameGroups");
                    try {
                        for (int i = 0; i < personalArray.length(); i++) {
                            Group group = JSONConvert.getGroupFromJson(personalArray.get(i).toString(), mContext);
                            if (!groupDatas.contains(group)) {
                                groupDatas.add(group);
                            }
                            groupMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);
                        }
                        for (int i = 0; i < orgArray.length(); i++) {
                            Group group = JSONConvert.getGroupFromJson(orgArray
                                    .get(i).toString(), mContext);
                            if (!groupDatas.contains(group)) {
                                groupDatas.add(group);
                            }
                            groupMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);
                        }
                        for (int i = 0; i < adminArray.length(); i++) {
                            Group group = JSONConvert.getGroupFromJson(adminArray
                                    .get(i).toString(), mContext);
                            if (!groupDatas.contains(group)) {
                                groupDatas.add(group);
                            }
                            groupMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);
                        }
                        for (int i = 0; i < taskArray.length(); i++) {
                            Group group = JSONConvert.getGroupFromJson(taskArray
                                    .get(i).toString(), mContext);
                            if (!groupDatas.contains(group)) {
                                groupDatas.add(group);
                            }
                            groupMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);
                        }
                        for (int i = 0; i < nickGroupArray.length(); i++) {
                            Group group = JSONConvert.getGroupFromJson(nickGroupArray
                                    .get(i).toString(), mContext);
                            if (!groupDatas.contains(group)) {
                                groupDatas.add(group);
                            }
                            groupMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    IMConstants.groupListDatas = groupDatas;
                    IMConstants.groupsMap = groupMap;

                    //很多地方会接收这个广播
                    // 1、通讯录中group列表会刷新
                    // 2、聊天列表group列表也会刷新
                    Intent intent = new Intent(
                            CommConstants.ACTION_GROUP_LIST_RESPONSE);
                    intent.setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(intent);
                }
            }
        });
    }

    /**
     * 创建Group
     **/
    @Override
    public void createGroup(final String memberIds, final String displayName,
                            final String description, final int type, final Handler handler) {

        XMPPConnection connection = XmppManager
                .getInstance().getConnection();
        if (connection == null || !connection.isConnected()) {
            handler.sendEmptyMessage(3);
        }

        OkHttpUtils.postWithToken()
                .url(CommConstants.URL_IM_CREATE)
                .addParams("userId", MFSPHelper.getString(CommConstants.USERID))
                .addParams("displayName", displayName)
                .addParams("description", description)
                .addParams("type", "" + type)
                .addParams("memberIds", memberIds)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject objValues = jsonObject.getJSONObject("objValue");
                            Group group = JSONConvert.getGroupFromJson(objValues
                                    .toString(), mContext);

                            //下载成员头像
                            downloadMemberAvatar(group);

                            // 更新群组
                            IMConstants.groupListDatas.add(0, group);
                            IMConstants.groupsMap.put(group.getGroupName(), group);

                            // 加入群组
                            String roomServerName = CommConstants.roomServerName;
                            if (StringUtils.notEmpty(group.getRoomServerName())) {
                                roomServerName = "@" + group.getRoomServerName() + ".";
                            }
                            String imServerName = group.getImServerName();
                            final String roomJid = group.getGroupName()
                                    + roomServerName + imServerName;
                            String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                            handler.postDelayed(
                                    new JoinRunnable(roomJid, adname, group, handler), 200);

                            String[] arr = new String[]{group.getGroupName(),
                                    group.getDisplayName()};
                            handler.obtainMessage(2, arr).sendToTarget();
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(3);
                        }
                    }
                });
    }

    private void downloadMemberAvatar(final Group group) {
//        List<UserInfo> members = new ArrayList<>();
//        int count = 0;
//        for (UserInfo member : group.getMembers()) {
//            if (9 == count++) {
//                break;
//            }
//            if (StringUtils.notEmpty(member.getAvatar())) {
//                members.add(member);
//            }
//        }
//
//        if (members.size() > 0) {
//            for (int i = 0; i < members.size(); i++) {
//                String destFileName = members.get(i).getEmpId();
//                HttpManager.downloadBitmap(CommConstants.URL_DOWN + members.get(i).getAvatar(), new AvatarCallBack(i, members.size(), destFileName, group));
//            }
//        } else {
//            //生成群组头像
//            ChatTools.createAvatar(mContext, group.getId(), getBitmaps(group));
//        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatTools.createAvatar(mContext, group.getId(), getBitmaps(group));
            }
        }).start();
//        getBitmaps(group);
    }

    private class AvatarCallBack extends BitmapCallback {

        private Group group;
        private int order, count;
        private String destFileName;

        public AvatarCallBack(int order, int count, String destFileName, Group group) {
            this.order = order;
            this.count = count;
            this.group = group;
            this.destFileName = destFileName;
        }

        @Override
        public void onError(Call call, Exception e) {

        }

        @Override
        public void onResponse(Bitmap response) throws JSONException {

            if(null!=response){
                PicUtils.compressImageAndSave(CommConstants.SD_DATA_PIC + destFileName + "_s.jpg", response, 100);
            }

        }

        @Override
        public void onAfter() {
            if (1 == count - order) {
                //生成群组头像
                ChatTools.createAvatar(mContext, group.getId(), getBitmaps(group));
            }
        }
    }

    private List<Bitmap> getBitmaps(final Group group) {
//        memberBitmaps.clear();
        List<Bitmap> memberBitmaps = new ArrayList<>();
        int count = 0;
        for (final UserInfo member : group.getMembers()) {
            if (9 == count++) {
                break;
            }
//            aq.id(new ImageView(mContext)).image(new BitmapAjaxCallback(){
//                @Override
//                protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
//                    super.callback(url, iv, bm, status);
//                    Log.d("async", "got artwork "+bm.getHeight()+" from "+url);
//                    if(bm.getByteCount() > 0){
//                        memberBitmaps.add(bm);
//                    }else {
//                        int picId = R.drawable.avatar_male;
//                        if ("男".equals(member.getGender())) {
//                            picId = R.drawable.avatar_male;
//                        } else if ("女".equals(member.getGender())) {
//                            picId = R.drawable.avatar_female;
//                        }
//                        Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId, 10);
//                        memberBitmaps.add(bitmap);
//                    }
//                    if(member.getId().equals(group.getMembers().get(group.getMembers().size()).getId())){
//                        ChatTools.createAvatar(mContext, group.getId(), memberBitmaps);
//                    }
//                }
//            });
//            new BitmapAjaxCallback() {
//                @Override
//                protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
//
//                    Log.d("async", "got artwork "+bm.getHeight()+" from "+url);
//                    if(bm.getByteCount() > 0){
//                        memberBitmaps.add(bm);
//                    }else {
//                        int picId = R.drawable.avatar_male;
//                        if ("男".equals(member.getGender())) {
//                            picId = R.drawable.avatar_male;
//                        } else if ("女".equals(member.getGender())) {
//                            picId = R.drawable.avatar_female;
//                        }
//                        Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId, 10);
//                        memberBitmaps.add(bitmap);
//                    }
//                    if(member.getId().equals(group.getMembers().get(group.getMembers().size()).getId())){
//                        ChatTools.createAvatar(mContext, group.getId(), memberBitmaps);
//                    }
//                }
//            }.imageView(new ImageView(mContext))
//                    .url(CommConstants.URL_DOWN + (!TextUtils.isEmpty(member.getAvatar()) ? member.getAvatar() : ""))
//                    .async(mContext);

            if (StringUtils.notEmpty(member.getAvatar())) {
                Bitmap bitmap = getBitmapFromURL(CommConstants.URL_DOWN + member.getAvatar());
                if(null != bitmap){
                    memberBitmaps.add(bitmap);
                }else {
                    int picId = R.drawable.avatar_male;
                    if ("男".equals(member.getGender())) {
                        picId = R.drawable.avatar_male;
                    } else if ("女".equals(member.getGender())) {
                        picId = R.drawable.avatar_female;
                    }
                    bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId, 10);
                    memberBitmaps.add(bitmap);
                }
            } else {
                int picId = R.drawable.avatar_male;
                if ("男".equals(member.getGender())) {
                    picId = R.drawable.avatar_male;
                } else if ("女".equals(member.getGender())) {
                    picId = R.drawable.avatar_female;
                }
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId, 10);
                memberBitmaps.add(bitmap);
            }
        }
        return memberBitmaps;
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
            return null;
        }
    }

    public void getGroupInfoForInvite(final String roomName,
                                      final String inviter, final String invitee) {

        String url = String.format(CommConstants.URL_IM_GROUP,
                MFSPHelper.getString(CommConstants.USERID), roomName);
        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {


                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject objValues = jsonObject.getJSONObject("objValue");
                            final Group group = JSONConvert.getGroupFromJson(objValues
                                    .toString(), mContext);

                            // 更新群组
                            IMConstants.groupListDatas.remove(group);
                            IMConstants.groupListDatas.add(0, group);
                            IMConstants.groupsMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);

                            // join
                            String roomServerName = CommConstants.roomServerName;
                            if (StringUtils.notEmpty(group.getRoomServerName())) {
                                roomServerName = "@" + group.getRoomServerName() + ".";
                            }
                            String imServerName = group.getImServerName();
                            final String roomJid = group.getGroupName()
                                    + roomServerName + imServerName;
                            String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                            handler.postDelayed(
                                    new JoinRunnable(roomJid, adname, group, handler), 200);

                            Intent intent = new Intent(CommConstants.ACTION_MY_INVITE);
                            intent.putExtra("inviter", inviter);
                            intent.putExtra("invitee", invitee);
                            intent.putExtra("roomName", roomName);
                            intent.putExtra("group", group);
                            intent.setPackage(mContext.getPackageName());
                            mContext.sendBroadcast(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    class JoinRunnable implements Runnable {
        String roomJid;
        String adname;
        Group group;
        Handler handler;

        public JoinRunnable(String roomJid, String adname, Group group, Handler handler) {
            super();
            this.roomJid = roomJid;
            this.adname = adname;
            this.group = group;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                MultiUserChat muChat = new MultiUserChat(XmppManager
                        .getInstance().getConnection(), roomJid);
                DiscussionHistory history = new DiscussionHistory();
                history.setMaxStanzas(0);
                muChat.join(adname, "", history,
                        SmackConfiguration.getPacketReplyTimeout());
                LogUtils.v("Join", "【" + adname + "】加入" + group.getDisplayName()
                        + "成功。。");

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.v("Join", "【" + adname + "】加入" + group.getDisplayName()
                        + "失败。。");
                if (joinCount > 3) {
                    joinCount = 0;
                    return;
                }
                handler.postDelayed(new JoinRunnable(roomJid, adname, group, handler),
                        1000);
                joinCount++;
            }
        }
    }

    public void getGroupInfo(final String groupName, final String displayName,
                             final String type, final String affecteds) {
        String url = String.format(CommConstants.URL_IM_GROUP,
                MFSPHelper.getString(CommConstants.USERID), groupName);
        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject objValues = jsonObject.getJSONObject("objValue");
                            Group group = JSONConvert.getGroupFromJson(objValues
                                    .toString(), mContext);
                            // 更新群组
                            IMConstants.groupListDatas.remove(group);
                            IMConstants.groupListDatas.add(0, group);
                            IMConstants.groupsMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);

                            Intent intent = new Intent(
                                    CommConstants.ACTION_GROUP_MEMBERS_CHANGES);
                            intent.putExtra("type", type);
                            intent.putExtra("groupName", groupName);
                            intent.putExtra("displayName", displayName);
                            intent.putExtra("affecteds", affecteds);
                            intent.setPackage(mContext.getPackageName());
                            mContext.sendBroadcast(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 任务日程中获取群组
     *
     * @param groupName
     * @param displayName
     */
    public void getGroupInfo(final String groupName, final String displayName,
                             final Handler handler) {

        final String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
        String url = String.format(CommConstants.URL_IM_GROUP
                , MFSPHelper.getString(CommConstants.USERID), groupName);

        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(2);
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject objValues = jsonObject.getJSONObject("objValue");
                            final Group group = JSONConvert.getGroupFromJson(objValues
                                    .toString(), mContext);
                            // 更新群组
                            IMConstants.groupListDatas.remove(group);
                            IMConstants.groupListDatas.add(0, group);
                            IMConstants.groupsMap.put(group.getGroupName(), group);
                            //下载成员头像
                            downloadMemberAvatar(group);

                            // join
                            String roomServerName = CommConstants.roomServerName;
                            if (StringUtils.notEmpty(group.getRoomServerName())) {
                                roomServerName = "@" + group.getRoomServerName() + ".";
                            }
                            String imServerName = group.getImServerName();
                            final String roomJid = group.getGroupName()
                                    + roomServerName + imServerName;

                            handler.postDelayed(
                                    new JoinRunnable(roomJid, adname, group, handler), 200);

                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    handler.obtainMessage(1, group).sendToTarget();
                                }
                            }, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(2);

                        }
                    }
                });
    }

    public void delMembers(final String groupId, final String memberIds,
                           final Handler handler, final int postion) {
        String url = String.format(CommConstants.URL_IM_DEL_MEMBERS,
                MFSPHelper.getString(CommConstants.USERID), groupId, memberIds);

        OkHttpUtils.postWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onResponse(String result) throws JSONException {
                        JSONObject jsonObject = new JSONObject(result);
                        boolean ok = jsonObject.getBoolean("ok");
                        if (ok) {
                            handler.obtainMessage(2, postion).sendToTarget();
                        } else {
                            handler.sendEmptyMessage(3);
                        }
                    }
                });
    }

}
