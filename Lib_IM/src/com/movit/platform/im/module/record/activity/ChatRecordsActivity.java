package com.movit.platform.im.module.record.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.swipeLayout.implments.SwipeItemMangerImpl;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.db.SessionManager;
import com.movit.platform.im.manager.IMManager;
import com.movit.platform.im.module.group.activity.GroupChatActivity;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.msg.activity.SystemMsgActivity;
import com.movit.platform.im.module.msg.helper.MsgListComparator;
import com.movit.platform.im.module.record.adapter.ChatRecordsAdapter;
import com.movit.platform.im.utils.BuildQueryString;
import com.movit.platform.im.widget.CurPopup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ChatRecordsActivity extends IMBaseActivity implements SwipeRefreshLayout.OnRefreshListener, CurPopup.PopupListener {

    private ImageView back;
    private View headerView;
    private ListView mSwipeListView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private TextView refreshTime, tipsText;

    private EditText searchKey;
    private ImageView searchClear;

    private SessionManager sessionManager;
    private InputMethodManager inputmanger;
    private ChatRecordsAdapter recentAdapter;

    private List<MessageBean> tempDatas = new ArrayList<>();
    private Set<String> markUnReadIds, markReadIds;

    public static final int POP_MORE_BTN = 1;
    public static final int POP_DEL_BTN = 2;
    public static final int MARK_READ_STATUS = 6;
    public static final int LEAVE_SESSION = 7;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case POP_DEL_BTN:
                    int postion = (Integer) msg.obj;
                    if (swipeRefreshLayout.isRefreshing()) {
                        postion = postion - 1;
                    }
                    if (postion >= tempDatas.size()) {
                        recentAdapter = null;
                        sortFreshData(searchKey.getText().toString());
                        return;
                    }
                    if (tempDatas.get(postion).getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
//                        sessionManager.deleteSession(tempDatas.get(postion).getFriendId());
                        // 离开单人聊天会话：
                        IMManager.leavePrivateSession(tempDatas.get(postion).getFriendId());
                    } else if (tempDatas.get(postion).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
//                        sessionManager.deleteSession(tempDatas.get(postion).getRoomId());
                        // 离开群组聊天会话
                        IMManager.leaveGroupSession(tempDatas.get(postion).getRoomId());
                    } else if (tempDatas.get(postion).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                        IMConstants.sysMsgList.clear();
                    }
                    sessionManager.closeDb();
                    IMConstants.contactListDatas.remove(tempDatas.get(postion));
                    setPoint();
                    recentAdapter = null;
                    sortFreshData(searchKey.getText().toString());
                    break;
                case POP_MORE_BTN:
                    break;
                case MARK_READ_STATUS:
                    int position = (Integer) msg.obj;
                    MessageBean bean = tempDatas.get(position);

                    if (bean.getMarkReadStatus() == -1) {
                        if (bean.getUnReadCount() == 0) {
                            bean.setMarkReadStatus(1);
                        } else {
                            bean.setMarkReadStatus(0);
                            handler.obtainMessage(
                                    LEAVE_SESSION,
                                    position).sendToTarget();
                        }
                    } else if (bean.getMarkReadStatus() == 0) {
                        bean.setMarkReadStatus(1);
                    } else {
                        bean.setMarkReadStatus(0);
                    }
                    recentAdapter.notifyDataSetChanged();

                    if (bean.getMarkReadStatus() == 1) {
                        saveReadStatus(bean, 1);
                    } else {
                        saveReadStatus(bean, 0);
                    }

                    setPoint();

                    break;
                case 5:
                    if (recentAdapter != null) {
                        setPoint();
                        sortFreshData(searchKey.getText().toString());
                    }
                    break;
                case LEAVE_SESSION:
                    int leave_session_position = (Integer) msg.obj;
                    IMConstants.contactListDatas.get(leave_session_position).setUnReadCount(0);
                    if (tempDatas.get(leave_session_position).getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                        // 离开单人聊天会话：
                        IMManager.leavePrivateSession(tempDatas.get(leave_session_position).getFriendId());
                    } else if (tempDatas.get(leave_session_position).getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                        // 离开群组聊天会话
                        IMManager.leaveGroupSession(tempDatas.get(leave_session_position).getRoomId());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //保存消息的已读未读状态
    private void saveReadStatus(MessageBean bean, int type) {
        if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
//            if (bean.getRsflag() == 1) {
//                addSet(bean.getCuserId(), type);
//            } else {
            addSet(bean.getFriendId(), type);
//            }
        } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
            addSet(bean.getRoomId(), type);
        }

        MFSPHelper.setStringSet(CommConstants.MARK_UNREAD_IDS, markUnReadIds);
        MFSPHelper.setStringSet(CommConstants.MARK_READ_IDS, markReadIds);
    }

    private void addSet(String id, int type) {
        if (type == 0) {
            markReadIds.add(id);
            markUnReadIds.remove(id);
        } else {
            markUnReadIds.add(id);
            markReadIds.remove(id);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_fragment_chat_recent);

        inputmanger = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        sessionManager = IMDBFactory.getInstance(this).getSessionManager();

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(CommConstants.ACTION_CONTACT_LIST);
        registerReceiver(contactListReceiver, filter2);

        initViews();
    }

    @Override
    public void receiveNewMessage(MessageBean messageBean) {
        if (messageBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
            clearReadStatus(messageBean.getFriendId());
        } else if (messageBean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
            clearReadStatus(messageBean.getRoomId());
        }
    }

    @SuppressLint("InlinedApi")
    protected void initViews() {

        initTitleBar();

        mSwipeListView = (ListView) findViewById(R.id.recent_listview);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        headerView = LayoutInflater.from(this).inflate(
                R.layout.im_header_recent_list, null);
        refreshTime = (TextView) headerView.findViewById(R.id.onrefresh_time);
        // 设置刷新时动画的颜色，可以设置4个
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(this);

        //初始化搜索框
        initSearchLayout();
        //判断是否需要显示提示语句
        //首次安装，首次使用IM模块时，提示下拉刷新可以查看更多聊天记录，否则不提示。
        showTips();
    }

    private void initSearchLayout() {
        searchKey = (EditText) findViewById(R.id.search_key);
        searchClear = (ImageView) findViewById(R.id.search_clear);
        searchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchKey.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setEnabled(true);
                inputmanger.hideSoftInputFromWindow(searchKey.getWindowToken(), 0);
                sortFreshData("");
            }
        });

        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        searchKey.setOnKeyListener(new View.OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = searchKey.getText().toString();
                    if (content != null
                            && !"".equalsIgnoreCase(content)) {
                        searchClear.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setEnabled(false);
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                        swipeRefreshLayout.setEnabled(true);
                        inputmanger.hideSoftInputFromWindow(
                                searchKey.getWindowToken(), 0);
                    }
                    sortFreshData(content);
                    inputMethodManager.hideSoftInputFromWindow(
                            searchKey.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    private void showTips() {
        tipsText = (TextView) findViewById(R.id.tips_text);
        boolean isShowTips = MFSPHelper.getBoolean("isShowTips", true);

        if (!isShowTips) {
            try {
                int nowVersion = getPackageManager()
                        .getPackageInfo(getPackageName(),
                                PackageManager.GET_META_DATA).versionCode;
                int originalVersion = MFSPHelper
                        .getInteger(CommConstants.ORIGINAL_VERSION);
                if (nowVersion > originalVersion) {
                    isShowTips = true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (isShowTips) {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    tipsText.setVisibility(View.GONE);
                    MFSPHelper.setBoolean("isShowTips", false);
                }
            }, 5 * 1000);
        } else {
            tipsText.setVisibility(View.GONE);

            IMManager.getContactList(this, new IMManager.CallBack() {
                @Override
                public void refreshUI() {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                        mSwipeListView.removeHeaderView(headerView);
                    }
                }
            });
        }
    }

    private PopupWindow popupWindow;

    protected void initTitleBar() {

        back = (ImageView) findViewById(R.id.common_top_img_left);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(getResources().getString(R.string.top_chat));
        ImageView groupAdd = (ImageView) findViewById(R.id.common_top_img_right);
        groupAdd.setImageResource(R.drawable.group_add);

        View popupView = LayoutInflater.from(this).inflate(
                R.layout.popup_for_group_chat, null);
        popupWindow = new CurPopup(popupView, this, this);

        groupAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    popupWindow.showAsDropDown(v);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        markUnReadIds = MFSPHelper.getStringSet(CommConstants.MARK_UNREAD_IDS);
        markReadIds = MFSPHelper.getStringSet(CommConstants.MARK_READ_IDS);
        setPoint();
        if (!MFSPHelper.getBoolean("isShowTips", true)) {
            sortFreshData(searchKey.getText().toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(contactListReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tempDatas.clear();
        tempDatas = null;
    }

    //执行移除Member响应
    @Override
    public void afterKicked(String roomName, String displayName) {
        handler.sendEmptyMessage(5);
    }

    //执行加入群组的响应
    @Override
    public void afterInvited(String roomName, String inviter, String invitee, Group group) {
        handler.sendEmptyMessage(5);
    }

    //执行Member Change响应
    @Override
    public void afterMemberChanged(String roomName, String affecteds, String displayName, String type) {
        handler.sendEmptyMessage(5);
    }

    //执行Group Name Change响应
    @Override
    public void afterGroupNameChanged(String roomName, String displayName) {
        handler.sendEmptyMessage(5);
    }

    //执行Group Dissolved响应
    @Override
    public void afterGroupDisolved(String roomName, String displayName) {
        handler.sendEmptyMessage(5);
    }

    @Override
    public void updateGroup() {
        IMManager.getContactList(this, new IMManager.CallBack() {
            @Override
            public void refreshUI() {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    mSwipeListView.removeHeaderView(headerView);
                }
            }
        });
    }

    protected void sortFreshData(String content) {

        tempDatas.clear();
        UserInfo userInfo;
        content = content.trim().toUpperCase();

        if (IMConstants.contactListDatas != null) {
            if (StringUtils.notEmpty(content)) {
                for (MessageBean messageBean : IMConstants.contactListDatas) {
                    if (messageBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                        userInfo = messageBean.getUserInfo();
                        String query = new BuildQueryString()
                                .buildQueryName(userInfo);
                        if (userInfo != null && query.contains(content)) {
                            tempDatas.add(messageBean);
                        }
                    } else if (messageBean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                        String subjectName = (messageBean.getSubject() + BuildQueryString
                                .buildQueryName(messageBean.getSubject()))
                                .toUpperCase();
                        if (messageBean.getSubject() != null
                                && subjectName.contains(content)) {
                            tempDatas.add(messageBean);
                        }
                    }
                }
            } else {
                tempDatas.addAll(IMConstants.contactListDatas);
            }
            for (MessageBean mb : tempDatas) {
                if (mb.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                    setReadStatus(mb, mb.getFriendId());
                } else if (mb.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                    setReadStatus(mb, mb.getRoomId());
                } else {
                    mb.setMarkReadStatus(-1);
                }
            }
        }

        if (recentAdapter == null) {
            setAdapter();
        } else {
            List<Integer> openItems = recentAdapter.getOpenItems();
            if (openItems.get(0) != -1) {
                recentAdapter.closeItem(openItems.get(0));
            }
            recentAdapter.notifyDataSetChanged();
        }
    }

    private void setReadStatus(MessageBean mb, String id) {
        if (markUnReadIds.contains(id)) {
            mb.setMarkReadStatus(1);
        } else if (markReadIds.contains(id)) {
            mb.setMarkReadStatus(0);
        } else {
            mb.setMarkReadStatus(-1);
        }
    }

    public void setAdapter() {

        recentAdapter = new ChatRecordsAdapter(this, tempDatas, handler);
        mSwipeListView.setAdapter(recentAdapter);
        recentAdapter.setMode(SwipeItemMangerImpl.Mode.Single);
        mSwipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (swipeRefreshLayout.isRefreshing()) {
                    position = position - 1;
                }
                List<Integer> openItems = recentAdapter.getOpenItems();
                if (openItems.get(0) != -1) {
                    recentAdapter.closeItem(openItems.get(0));
                    return;
                }

                if (position < 0)
                    return;

                MessageBean bean = tempDatas.get(position);
                if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                    //进入聊天页面
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userInfo", bean.getUserInfo());
                    if (bean.getUnReadCount() == 0 && bean.getMarkReadStatus() != 0) {
                        bundle.putBoolean("hasNewMes", false);
                    } else {
                        bundle.putBoolean("hasNewMes", true);
                    }
                    clearReadStatus(bean.getFriendId());
                    ((BaseApplication) ChatRecordsActivity.this.getApplication()).getUIController().startPrivateChat(ChatRecordsActivity.this, bundle);

                } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP
                        || CommConstants.GROUP_ADMIN.equalsIgnoreCase(bean.getFriendId())) {

                    Intent intent = new Intent(ChatRecordsActivity.this,
                            GroupChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("room", bean.getRoomId());
                    bundle.putString("subject", bean.getSubject());
                    if (bean.getUnReadCount() == 0 && bean.getMarkReadStatus() != 1) {
                        bundle.putBoolean("hasNewMes", false);
                    } else {
                        bundle.putBoolean("hasNewMes", true);
                    }
                    bundle.putInt(CommConstants.KEY_GROUP_TYPE, StringUtils.notEmpty(bean.getGroupType()) ? Integer.valueOf(bean.getGroupType()) : -1);
                    intent.putExtras(bundle);
                    clearReadStatus(bean.getRoomId());
                    startActivity(intent);
                } else if (bean.getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    startActivityForResult(new Intent(ChatRecordsActivity.this, SystemMsgActivity.class), 99);
                }
            }
        });
    }

    public void clearReadStatus(String id) {
        markReadIds.remove(id);
        markUnReadIds.remove(id);
        MFSPHelper.setStringSet(CommConstants.MARK_UNREAD_IDS, markUnReadIds);
        MFSPHelper.setStringSet(CommConstants.MARK_READ_IDS, markReadIds);

        sortFreshData(searchKey.getText().toString());
    }

    public void sortMsgs() {
        Collections.sort(IMConstants.contactListDatas, new MsgListComparator());
    }

    public void setPoint() {
        Intent intent = new Intent(CommConstants.ACTION_SET_REDPOINT);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 99) {
            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    IMConstants.contactListDatas.remove(i);
                    break;
                }
            }
        }
        handler.sendEmptyMessage(5);
    }

    private BroadcastReceiver contactListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context cont, final Intent intent) {
            if (CommConstants.ACTION_CONTACT_LIST.equals(intent.getAction())) {

                // 下拉刷新获取全部数据
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    mSwipeListView.removeHeaderView(headerView);
                } else {
                    //客户端新增标记为已读、标记为未读功能，该功能为纯客户端行为
                    //服务器端API getContactList返回的是全部的sessionList
                    //故此时需要过滤掉被客户端标记为未读的sessionList，这部分不需要显示，否则客户端行为将出错
//                    filterContactList();
                }

                if (!IMConstants.sysMsgList.isEmpty()) {
                    IMConstants.contactListDatas.add(0, IMConstants.sysMsgList.get(0));
                }
                sortMsgs();
                setPoint();
                recentAdapter = null;
                sortFreshData(searchKey.getText().toString());
                CommConstants.isCome = true;
            }
        }
    };

    //客户端新增标记为已读、标记为未读功能，该功能为纯客户端行为
    //服务器端API getContactList返回的是全部的sessionList
    //故此时需要过滤掉被客户端标记为未读的sessionList，这部分不需要显示，否则客户端行为将出错
    private void filterContactList() {
        try {
            ArrayList<String> sessionListDatas = sessionManager.getSessionList();
            if (null != sessionListDatas && sessionListDatas.size() > 0) {
                List<MessageBean> tempList = new ArrayList<>();
                for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                    MessageBean bean = IMConstants.contactListDatas.get(i);
                    if (bean.getUnReadCount() == 0) {
                        if (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                            if (sessionListDatas.contains(bean.getFriendId()
                                    .toLowerCase())) {
                                tempList.add(bean);
                            }
                        } else if (bean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                            if (sessionListDatas.contains(bean.getRoomId().toLowerCase())) {
                                tempList.add(bean);
                            }
                        }
                    } else {
                        tempList.add(bean);
                    }
                }

                if (tempList.size() > 0) {
                    sessionManager.deleteAllSession();
                    for (int i = 0; i < tempList.size(); i++) {
                        sessionManager.insertSession(tempList.get(i));
                    }
                    IMConstants.contactListDatas.clear();
                    IMConstants.contactListDatas.addAll(tempList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sessionManager.closeDb();
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        mSwipeListView.setAdapter(null);
        mSwipeListView.addHeaderView(headerView);
        mSwipeListView.setAdapter(recentAdapter);
        refreshTime
                .setText(getString(R.string.refresh_time) + DateUtils.date2Str(new Date(), "HH:mm:ss"));
        IMManager.getContactList(this, new IMManager.CallBack() {
            @Override
            public void refreshUI() {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    mSwipeListView.removeHeaderView(headerView);
                }
            }
        });
    }

    @Override
    public void onWindowItemClickListener(final int viewId) {
        Intent intent = new Intent();
        if (viewId == R.id.pop_linearlayout_1) {
            intent.putExtra("ACTION", "GROUP").putExtra("TITLE", getString(R.string.group_chat)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
        } else if (viewId == R.id.pop_linearlayout_2) {
            intent.putExtra("ACTION", "GROUP").putExtra("TITLE", getString(R.string.nick_group_chat)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_ANS);
        } else if (viewId == R.id.pop_linearlayout_3) {
            intent.putExtra("ACTION", "MEETING").putExtra("TITLE", getString(R.string.video_meeting)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
        }
        ((BaseApplication) getApplication()).getUIController().onIMOrgClickListener(this, intent, 0);
    }
}
