package com.movit.platform.im.module.detail.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.view.tree.ViewHeightBasedOnChildren;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.manager.MessageManager;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.im.module.detail.adapter.ChatDetailAdapter;
import com.movit.platform.im.module.group.activity.GroupAllMembersActivity;
import com.movit.platform.im.module.group.activity.RenameGroupActivity;
import com.movit.platform.im.module.group.adapter.GroupAllMembersAdapter;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.group.fragment.GroupAllMembersFragment;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends IMBaseActivity {
    private GridView gridView;
    private TextView title;
    private ImageView topLeft;
    private LinearLayout showAllMembers;
    private TextView memberCount;
    private LinearLayout clean_ll;
    private LinearLayout search_ll;
    private Button delButton;
    private ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();
    private ArrayList<UserInfo> showInfos = new ArrayList<UserInfo>();//显示部分成员
    private String[] adminIds;
    private ChatDetailAdapter gridAdapter;
    int ctype, groupType;

    private String groupId = "";
    private String groupName = "";
    private Group group;
    private LinearLayout renameRoom;
    private TextView tv_roomName;
    private CusDialog dialogUtil;
    private String userId;
    boolean isMyDisslove = false;
    boolean useEoopApi = false;//是否使用eoopApi
    public static GroupAllMembersAdapter groupAllMembersAdapter = null;
    public static GroupAllMembersAdapter groupSearchResultAdapter = null;
    /**
     * 部分显示的群组成员数
     */
    public static final int SHOW_MEMBER_NUM = 20;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int position = (Integer) msg.obj;
                    // 调服务端成员退出接口
                    UserInfo userInfo = null;
                    if (groupAllMembersAdapter != null) {
                        if (GroupAllMembersFragment.listView != null
                                && GroupAllMembersFragment.listView.getVisibility() == View.GONE) {//搜索状态
                            userInfo = groupSearchResultAdapter.getUserInfos().get(position);
                        } else {
                            userInfo = groupAllMembersAdapter.getUserInfos().get(position);
                        }
                    } else {
                        userInfo = userInfos.get(position);
                    }
                    DialogUtils.getInstants().showLoadingDialog(ChatDetailActivity.this,
                            getString(R.string.waiting), false);
                    GroupManager.getInstance(ChatDetailActivity.this).delMembers(groupId,
                            userInfo.getId(), handler, position);
                    break;
                case 2:
                    int pos = (Integer) msg.obj;
                    DialogUtils.getInstants().dismiss();
                    if (groupAllMembersAdapter != null) {
                        UserInfo user = null;
                        if (GroupAllMembersFragment.listView != null
                                && GroupAllMembersFragment.listView.getVisibility() == View.GONE) {//搜索状态
                            user = groupSearchResultAdapter.getUserInfos().get(pos);
                            if (groupSearchResultAdapter != null && user != null) {
                                groupSearchResultAdapter.getUserInfos().remove(user);
                                groupSearchResultAdapter.notifyDataSetChanged();
                            }
                        } else {
                            user = groupAllMembersAdapter.getUserInfos().get(pos);
                        }
                        groupAllMembersAdapter.getUserInfos().remove(user);
                        groupAllMembersAdapter.notifyDataSetChanged();
                        if (GroupAllMembersActivity.groupAllMembersActivity != null) {
                            GroupAllMembersActivity.groupAllMembersActivity.refreshTitleText(groupAllMembersAdapter.getUserInfos());
                        }
                        if (tv_roomName != null && group != null && memberCount != null) {
                            tv_roomName.setText(group.getDisplayName());
                            memberCount.setText(getString(R.string.all) + group.getMembers().size() + getString(R.string.person));
                        }
                    }
                    userInfos.remove(pos);
                    gridAdapter.notifyDataSetChanged();
                    ViewHeightBasedOnChildren basedOnChildren = new ViewHeightBasedOnChildren(
                            ChatDetailActivity.this);
                    basedOnChildren.setListViewHeightBasedOnChildren(gridView);
                    break;
                case 3:
                    DialogUtils.getInstants().dismiss();
                    ToastUtils.showToast(ChatDetailActivity.this, getString(R.string.failed_try_again));
                    break;
                case 4:// 解散群
                    DialogUtils.getInstants().dismiss();
                    iniDialog(getString(R.string.dissolve_success));
                    break;
                case 5:
                    // 自己退出群
                    DialogUtils.getInstants().dismiss();
                    String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                    // 加入群组
                    String roomServerName = CommConstants.roomServerName;
                    if (StringUtils.notEmpty(group.getRoomServerName())) {
                        roomServerName = "@" + group.getRoomServerName() + ".";
                    }
                    String imServerName = group.getImServerName();
                    final String roomJid = group.getGroupName() + roomServerName
                            + imServerName;
                    // 我自己退出
                    String kickedJid = adname + MessageManager.SUFFIX;
                    handler.postDelayed(new LeaveRunnable(roomJid, kickedJid), 200);
                    List<Group> groups = IMConstants.groupListDatas;
                    groups.remove(group);
                    IMConstants.groupsMap.remove(groupName);
                    // 先删除原来的群组消息
                    for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                        if (groupName.equalsIgnoreCase(IMConstants.contactListDatas.get(i)
                                .getRoomId())
                                && IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            IMConstants.contactListDatas.remove(i);
                            break;
                        }
                    }
                    iniDialog(getString(R.string.exit_success));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_activity_chat_detail);

        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        useEoopApi = appInfo.metaData.getBoolean("USE_EOOP_API", false);

        Intent intent = getIntent();
        ctype = intent.getIntExtra("ctype", -1);
        groupType = intent.getIntExtra(CommConstants.KEY_GROUP_TYPE, -1);
        groupName = intent.getStringExtra("groupName");
        userId = MFSPHelper.getString(CommConstants.USERID);
        iniData();
        iniView();
        setAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialogUtil != null && dialogUtil.isShowing()) {
            dialogUtil.dismiss();
        }
    }

    private void iniData() {

        if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            group = IMConstants.groupsMap.get(groupName);
            groupId = group.getId();
            adminIds = group.getAdminIds();
            List<UserInfo> members = group.getMembers();
            userInfos.clear();
            int size = members.size();
            for (int i = 0; i < size; i++) {
                if (members.get(i).getId().equals(group.getCreaterId())) {
                    userInfos.add(0, members.get(i));
                } else {
                    userInfos.add(members.get(i));
                }
            }
            if (userId.equals(group.getCreaterId())) {
                userInfos.add(new UserInfo());
                userInfos.add(new UserInfo());
            } else {
                if (groupType != CommConstants.CHAT_TYPE_GROUP_ANS) {
                    userInfos.add(new UserInfo());
                }
            }

        } else if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            userInfos = (ArrayList<UserInfo>) getIntent().getSerializableExtra(
                    "userInfos");
            userInfos.add(new UserInfo());
        }
    }

    private void iniView() {
        gridView = (GridView) findViewById(R.id.chat_detail_gridview);
        clean_ll = (LinearLayout) findViewById(R.id.chat_detail_clean_ll);
        search_ll = (LinearLayout) findViewById(R.id.chat_detail_search_ll);
        delButton = (Button) findViewById(R.id.chat_detail_del_btn);
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        title.setText(getString(R.string.chat_detail));
        renameRoom = (LinearLayout) findViewById(R.id.rename_room);
        tv_roomName = (TextView) findViewById(R.id.room_name);

        //查看所有群组成员
        showAllMembers = (LinearLayout) findViewById(R.id.show_allmembers);
        memberCount = (TextView) findViewById(R.id.member_count);
        if (useEoopApi) {
            showAllMembers.setVisibility(View.VISIBLE);//隐藏显示全员item
        }

        topLeft.setImageResource(R.drawable.top_back);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            delButton.setVisibility(View.GONE);
            renameRoom.setVisibility(View.GONE);
            showAllMembers.setVisibility(View.GONE);
        } else {
            tv_roomName.setText(group.getDisplayName());
            memberCount.setText(getString(R.string.all) + group.getMembers().size() + getString(R.string.person));
            renameRoom.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    i.putExtra("group_name", groupName);
                    i.putExtra("group_object", group);
                    i.putExtra("type", "group");
                    i.setClass(ChatDetailActivity.this,
                            RenameGroupActivity.class);
                    ChatDetailActivity.this.startActivityForResult(i, IMConstants.REQUEST_CODE_RENAME_GROUP);
                }
            });

            showAllMembers.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("groupInfo", group);
                    intent.putExtra("userInfos", userInfos);
                    intent.putExtra("groupType", groupType);
                    GroupAllMembersActivity.chatDetailActivityHandler = handler;
                    intent.setClass(ChatDetailActivity.this, GroupAllMembersActivity.class);
                    ChatDetailActivity.this.startActivity(intent);
                }
            });

            if (group.getCreaterId().equals(userId)) {
                delButton.setText(getString(R.string.dissolve_group));
            } else {
                delButton.setText(getString(R.string.delete_and_exit));
            }

            delButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogUtil = CusDialog.getInstance();
                    dialogUtil.showCustomDialog(ChatDetailActivity.this);
                    if (group.getCreaterId().equals(userId)) {
                        dialogUtil.setWebDialog(getString(R.string.are_you_sure_to_dissolve_group));
                    } else {
                        dialogUtil.setWebDialog(getString(R.string.are_you_sure_to_exit_group));
                    }
                    dialogUtil.setCancleClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialogUtil.dismiss();
                        }
                    });
                    dialogUtil.setConfirmClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // 调服务端成员退出接口，如果是管理员那就是解散群了
                            dialogUtil.dismiss();
                            DialogUtils.getInstants().showLoadingDialog(
                                    ChatDetailActivity.this, getString(R.string.waiting), false);
                            if (group.getCreaterId().equals(userId)) {
                                isMyDisslove = true;
                                GroupManager.getInstance(ChatDetailActivity.this)
                                        .dissolveGroup(groupId, handler, -1);
                            } else {
                                // 调服务端成员退出接口
                                GroupManager.getInstance(ChatDetailActivity.this).logoutGroup(
                                        groupId, handler);
                            }
                        }
                    });
                }
            });
        }

    }

    public void setAdapter() {

        if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            //add by Reed.Qiu
            int size = 0;
            if (group != null && group.getMembers() != null) {
                size = group.getMembers().size();
            }
            if (useEoopApi && size > SHOW_MEMBER_NUM) {
                showInfos.clear();
                for (int i = 0; i < SHOW_MEMBER_NUM; i++) {
                    showInfos.add(userInfos.get(i));
                }
                if (userId.equals(group.getCreaterId())) {
                    showInfos.add(new UserInfo());
                    showInfos.add(new UserInfo());
                } else {
                    if (groupType != CommConstants.CHAT_TYPE_GROUP_ANS) {
                        showInfos.add(new UserInfo());
                    }
                }
                gridAdapter = new ChatDetailAdapter(this, showInfos, handler,
                        gridView, ctype, adminIds, group.getCreaterId(), groupType);
            }//end
            else {
                gridAdapter = new ChatDetailAdapter(this, userInfos, handler,
                        gridView, ctype, adminIds, group.getCreaterId(), groupType);
            }
        } else {
            gridAdapter = new ChatDetailAdapter(this, userInfos, handler,
                    gridView, ctype, null, "", groupType);
        }

        gridView.setAdapter(gridAdapter);
        ViewHeightBasedOnChildren basedOnChildren = new ViewHeightBasedOnChildren(
                this);
        basedOnChildren.setListViewHeightBasedOnChildren(gridView);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
                    if (position == userInfos.size() - 1) {// add
                        Intent intent = new Intent();
                        intent.putExtra("ctype", ctype);
                        intent.putExtra("userInfos", userInfos);
                        intent.putExtra("groupId", groupId);
                        intent.putExtra("groupName", groupName);
                        intent.putExtra("ACTION", "GROUP");
                        intent.putExtra("TITLE", getString(R.string.group_chat));
                        intent.putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);

                        ((BaseApplication) ChatDetailActivity.this.getApplication()).getUIController().onIMOrgClickListener(ChatDetailActivity.this, intent, 1);
                    } else {// 跳转用户详情
                        Intent intent = new Intent(ChatDetailActivity.this,
                                UserDetailActivity.class);
                        intent.putExtra("userInfo", userInfos.get(position));
                        startActivity(intent);
                    }
                } else if (ctype == CommConstants.CHAT_TYPE_GROUP) {
                    int size = 0;
                    if (useEoopApi && userInfos.size() > SHOW_MEMBER_NUM) {
                        size = showInfos.size();
                    } else {
                        size = userInfos.size();
                    }
                    if (group.getCreaterId().equals(userId)) {
                        if (position == size - 1) {// del
                            if (gridAdapter.isDeling()) {
                                gridAdapter.setDeling(false);
                            } else {
                                gridAdapter.setDeling(true);
                            }
                            gridAdapter.notifyDataSetChanged();
                        } else if (position == size - 2) {// add
                            Intent intent = new Intent();
                            intent.putExtra("ctype", ctype);
                            intent.putExtra("userInfos", userInfos);
                            intent.putExtra("groupId", groupId);
                            intent.putExtra("groupName", groupName);
                            intent.putExtra("ACTION", "GROUP");
                            intent.putExtra("TITLE", getString(R.string.group_chat));

                            ((BaseApplication) ChatDetailActivity.this.getApplication()).getUIController().onIMOrgClickListener(ChatDetailActivity.this, intent, 1);
                        } else {// 跳转用户详情
                            switch (groupType) {
                                case CommConstants.CHAT_TYPE_GROUP_ANS:
                                    //匿名群组不允许查看用户详情
                                    ToastUtils.showToast(ChatDetailActivity.this, getString(R.string.can_not_see_user_detail));
                                    break;
                                default:
                                    Intent intent = new Intent(ChatDetailActivity.this,
                                            UserDetailActivity.class);
                                    intent.putExtra("userInfo", userInfos.get(position));
                                    startActivity(intent);
                                    break;
                            }
                        }

                    } else {
                        // 跳转用户详情
                        switch (groupType) {
                            case CommConstants.CHAT_TYPE_GROUP_ANS:
                                //匿名群组不允许查看用户详情
                                ToastUtils.showToast(ChatDetailActivity.this, getString(R.string.can_not_see_user_detail));
                                break;
                            default:
                                if (position == size - 1) {// add
                                    Intent intent = new Intent();
                                    intent.putExtra("ctype", ctype);
                                    intent.putExtra("userInfos", userInfos);
                                    intent.putExtra("groupId", groupId);
                                    intent.putExtra("groupName", groupName);
                                    intent.putExtra("ACTION", "GROUP");
                                    intent.putExtra("TITLE", getString(R.string.group_chat));
                                    ((BaseApplication) ChatDetailActivity.this.getApplication()).getUIController().onIMOrgClickListener(ChatDetailActivity.this, intent, 1);
                                } else {
                                    Intent intent = new Intent(ChatDetailActivity.this,
                                            UserDetailActivity.class);
                                    intent.putExtra("userInfo", userInfos.get(position));
                                    startActivity(intent);
                                }
                                break;
                        }

                    }
                }
            }
        });
    }

    @Override
    public void updateGroup() {
        iniData();
        iniView();
        setAdapter();
    }

    @Override
    public void afterKicked(String roomName, String displayName) {
        if (ctype == CommConstants.CHAT_TYPE_GROUP && groupName.equalsIgnoreCase(roomName)) {
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

    @Override
    public void afterMemberChanged(String roomName, String affecteds, String displayName, String type) {

        if(ctype == CommConstants.CHAT_TYPE_GROUP && groupName.equalsIgnoreCase(roomName)) {
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
                        // iniDialog("您已退出群组!");
                    } else {
//								IMApplication.showToast(context, names
//										+ "退出群组!");
                    }
                }
                group = IMConstants.groupsMap.get(groupName);
                memberCount.setText("(" + group.getMembers().size() + "人)");
                if (groupAllMembersAdapter != null) {
                    List<UserInfo> tempMembers = group.getMembers();
                    List<UserInfo> groupAllMembers = new ArrayList<UserInfo>();
                    int size = tempMembers.size();
                    for (int i = 0; i < size; i++) {
                        if (tempMembers.get(i).getId().equals(group.getCreaterId())) {
                            groupAllMembers.add(0, tempMembers.get(i));
                        } else {
                            groupAllMembers.add(tempMembers.get(i));
                        }
                    }
                    groupAllMembersAdapter.setUserInfos(groupAllMembers);
                    groupAllMembersAdapter.notifyDataSetChanged();
                    if (GroupAllMembersActivity.groupAllMembersActivity != null) {
                        GroupAllMembersActivity.groupAllMembersActivity.refreshTitleText(groupAllMembers);
                    }
                }

                iniData();
                setAdapter();
        }
    }

    @Override
    public void afterGroupNameChanged(String roomName, String displayName) {
        if(ctype == CommConstants.CHAT_TYPE_GROUP && groupName.equalsIgnoreCase(roomName)) {
            if (roomName.equalsIgnoreCase(groupName)) {
                tv_roomName.setText(displayName);
            }
        }
    }

    @Override
    public void afterGroupDisolved(String roomName, String displayName) {
        if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            if (isMyDisslove) {
                iniDialog(getString(R.string.dissolve_success));
            } else {
                if (groupName.equalsIgnoreCase(roomName)) {
                    iniDialog(getString(R.string.admin_dissolve_success));
                }
            }
        }
    }

    public String getCName(String adname) {
        UserDao dao = UserDao.getInstance(this);
        UserInfo userInfo = dao.getUserInfoByADName(adname);
        String cname = userInfo.getEmpCname().split("\\.")[0];
        return cname;
    }

    public void iniDialog(String title) {
        dialogUtil = CusDialog.getInstance();
        dialogUtil.showVersionDialog(this);
        dialogUtil.setTitleDialog(title);
        dialogUtil.setConfirmClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogUtil.dismiss();
                ((BaseApplication) ChatDetailActivity.this.getApplication()).getUIController().startMainActivity(ChatDetailActivity.this, new Intent(), 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);

        if (arg1 == this.RESULT_OK && arg0 == IMConstants.REQUEST_CODE_RENAME_GROUP) {
            tv_roomName.setText(arg2.getStringExtra(IMConstants.KEY_GROUP_NAME));
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
                LogUtils.v("MultiUserChat", kicked + "已退出" + roomJid);
            } catch (Exception e) {
                e.printStackTrace();
                handler.postDelayed(new LeaveRunnable(roomJid, kicked), 1000);
            }
        }
    }

}
