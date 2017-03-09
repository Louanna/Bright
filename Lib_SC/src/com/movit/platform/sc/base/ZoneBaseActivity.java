package com.movit.platform.sc.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.view.faceview.FaceViewPage;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.widget.SelectPicPopup;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movit.platform.framework.widget.xlistview.XListView.IXListViewListener;
import com.movit.platform.sc.R;
import com.movit.platform.sc.activity.SCBaseActivity;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.entities.Comment;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.module.zone.adapter.ZoneAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class ZoneBaseActivity extends SCBaseActivity implements
        OnTouchListener, OnClickListener, IXListViewListener {
    protected TextView title;
    protected ImageView topLeft, topRight;

    protected TextView noSay;

    protected XListView zoneListView;
    protected ZoneAdapter adapter;
    protected List<Zone> zoneList = new ArrayList<Zone>();

    protected IZoneManager zoneManager;

    protected LinearLayout mBottomView;
    protected FaceViewPage faceViewPage;
    private boolean mIsFaceShow = false;// 是否显示表情
    private Button mFaceSwitchBtn;// 切换表情的button
    private Button mSendMsgBtn;// 发送消息button
    private EditText mEditText;// 消息输入框
    private InputMethodManager mInputMethodManager;
    AQuery aQuery;
    int currentPos;
    private Comment curComment;

    private SelectPicPopup popWindow;

    private Context context;

    protected Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCConstants.ZONE_CLICK_AVATAR:// click avatar
                    String userId = (String) msg.obj;
                    if (userId.equals(adapter.getUserId()) || userId.equals(CommConstants.loginConfig.getmUserInfo().getId())) {
                        UserDao dao = UserDao.getInstance(ZoneBaseActivity.this);
                        UserInfo userInfo = dao.getUserInfoById(userId);
                        if (userInfo == null) {
                            return;
                        }
                        Intent intent = new Intent();
                        intent.putExtra("userInfo", userInfo);
                        ((BaseApplication) ZoneBaseActivity.this.getApplication()).getUIController().onOwnHeadClickListener(ZoneBaseActivity.this, intent, SCConstants.ZONE_CLICK_AVATAR);
                    } else {
                        UserDao dao = UserDao.getInstance(ZoneBaseActivity.this);
                        ArrayList<String> poList = CommConstants.loginConfig.getmUserInfo().getAttentionPO();
                        String empCName = dao.getUserInfoById(userId).getEmpCname();

                        try {
                            boolean gozone = getPackageManager().getApplicationInfo(
                                    getPackageName(), PackageManager.GET_META_DATA).metaData
                                    .getBoolean("CHANNEL_ATTENTION_SEEZONE", false);
                            if (gozone) {
                                if (null != poList && poList.contains(adapter.getUserId())) {
                                    ((BaseApplication) ZoneBaseActivity.this.getApplication()).getUIController().onZoneOwnClickListener(ZoneBaseActivity.this, new Intent().putExtra("userId", userId), SCConstants.ZONE_CLICK_AVATAR);
                                } else {
                                    ToastUtils.showToast(ZoneBaseActivity.this, String.format(getString(R.string.attention_to_look),empCName));
                                }
                            } else {
                                ((BaseApplication) ZoneBaseActivity.this.getApplication()).getUIController().onZoneOwnClickListener(ZoneBaseActivity.this, new Intent().putExtra("userId", userId), SCConstants.ZONE_CLICK_AVATAR);

                            }
                        } catch (PackageManager.NameNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                    break;
                case SCConstants.ZONE_NICE_RESULT:
                    DialogUtils.getInstants().dismiss();
                    try {
                        String result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            int postion = msg.arg1;
                            int isNice = msg.arg2;
                            Zone zone = zoneList.get(postion);
                            if (isNice == 0) {// 赞
                                if (zone.getLikers() == null) {
                                    ArrayList<String> likser = new ArrayList<String>();
                                    likser.add(MFSPHelper.getString(CommConstants.USERID));
                                    zone.setLikers(likser);
                                } else {
                                    zone.getLikers().add(0,
                                            MFSPHelper.getString(CommConstants.USERID));
                                }
                            } else if (isNice == 1) {// 取消赞
                                if (zone.getLikers() != null) {
                                    zone.getLikers().remove(
                                            MFSPHelper.getString(CommConstants.USERID));
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showToast(ZoneBaseActivity.this, getString(R.string.like_fail));
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(ZoneBaseActivity.this, getString(R.string.like_fail));
                    }
                    break;
                case 5:// 评论
                    currentPos = (Integer) msg.obj;
                    int commentLine = msg.arg1;
                    int type = msg.arg2;
                    if (type == 0) {
                        mEditText.setHint(getString(R.string.comment));
                        curComment = null;
                    } else if (type == 1) {// 在评论
                        List<Comment> comments = zoneList.get(currentPos)
                                .getComments();
                        curComment = comments.get(commentLine);

                        UserDao dao = UserDao.getInstance(context);
                        UserInfo userInfo = dao.getUserInfoById(curComment.getUserId());

                        mEditText.setHint(getString(R.string.reply) + userInfo.getEmpCname());
                    }
                    // 弹出输入框
                    mBottomView.setVisibility(View.VISIBLE);
                    mEditText.requestFocus();
                    mInputMethodManager.showSoftInput(mEditText, 0);
                    break;
                case SCConstants.ZONE_COMMENT_RESULT:
                    DialogUtils.getInstants().dismiss();
                    try {
                        String comment_result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(comment_result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            int postion = msg.arg1;
                            Zone zone = zoneList.get(postion);

                            JSONObject itemObject = jsonObject
                                    .getJSONObject("item");
                            Comment comment = new Comment();
                            if (itemObject.has("sContent")) {
                                comment.setContent(itemObject.getString("sContent"));
                            }
                            if (itemObject.has("cUserId")) {
                                comment.setUserId(itemObject.getString("cUserId"));
                            }
                            if (itemObject.has("cToUserId")) {
                                comment.setTouserId(itemObject
                                        .getString("cToUserId"));
                            }
                            if (itemObject.has("cId")) {
                                comment.setcId(itemObject.getString("cId"));
                            }
                            if (itemObject.has("cParentId")) {
                                String cParentId = itemObject
                                        .getString("cParentId");
                                comment.setParnetId(cParentId);
                            }
                            if (itemObject.has("cRootId")) {
                                String cRootId = itemObject.getString("cRootId");
                                comment.setRootId(cRootId);
                            }
                            if (itemObject.has("cSayId")) {
                                String cSayId = itemObject.getString("cSayId");
                                comment.setSayId(cSayId);
                            }
                            if (zone.getComments() == null) {
                                ArrayList<Comment> comments = new ArrayList<Comment>();
                                comments.add(comment);
                                zone.setComments(comments);
                            } else {
                                zone.getComments().add(comment);
                            }
                            mInputMethodManager.hideSoftInputFromWindow(
                                    mEditText.getWindowToken(), 0);
                            mBottomView.setVisibility(View.GONE);
                            mIsFaceShow = false;
                            mEditText.setText("");
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showToast(ZoneBaseActivity.this,
                                    getString(R.string.comment_fail));
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(ZoneBaseActivity.this, getString(R.string.comment_fail));
                    }
                    break;

                case SCConstants.ZONE_CLICK_COMMENT_TO_DEL:
                    int delCommentLine = msg.arg1;
                    int delPostion = msg.arg2;

                    // 弹出菜单popWindow
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(zoneListView.getWindowToken(),
                                0);
                    }
                    // 实例化SelectPicPopupWindow
                    popWindow = new SelectPicPopup((Activity) context,
                            new ItemsOnClick(delCommentLine, delPostion));
                    popWindow.showDel();
                    // 显示窗口
                    popWindow.showAtLocation(findViewById(R.id.zone_listview),
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

                    break;
                case SCConstants.ZONE_COMMENT_DEL_RESULT:
                    DialogUtils.getInstants().dismiss();
                    try {
                        String comment_result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(comment_result);
                        int code = jsonObject.getInt("code");
                        if (0 == code) {
                            int postion = msg.arg1;
                            int delLine = msg.arg2;
                            Zone zone = zoneList.get(postion);
                            zone.getComments().remove(delLine);
                            adapter.notifyDataSetChanged();
                        } else {
                            ToastUtils.showToast(context, getString(R.string.del_fail));
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case SCConstants.ZONE_ERROR_RESULT:
                    DialogUtils.getInstants().dismiss();
                    String errorString = (String) msg.obj;
                    ToastUtils.showToast(ZoneBaseActivity.this, errorString);
                    zoneListView.stopRefresh();
                    zoneListView.stopLoadMore();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        setAdapter();
                    }
                    break;
                case SCConstants.ZONE_ERROR_NO_SAY:
                    DialogUtils.getInstants().dismiss();
                    zoneListView.stopRefresh();
                    zoneListView.stopLoadMore();
                    noSay.setVisibility(View.VISIBLE);
                    break;
                default:
                    dealHandlers(msg);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_zone_detail);
        zoneManager = ((BaseApplication) this.getApplication()).getManagerFactory().getZoneManager();
        context = this;
        aQuery = new AQuery(this);
        initView();
        initFacePage();// 初始化表情页面
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDatas();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);

        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
        mInputMethodManager = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);
        zoneListView = (XListView) findViewById(R.id.zone_listview);
        mBottomView = (LinearLayout) findViewById(R.id.zone_bottom_rootview);
        mFaceSwitchBtn = (Button) findViewById(R.id.zone_input_face);
        mSendMsgBtn = (Button) findViewById(R.id.zone_input_send);
        mEditText = (EditText) findViewById(R.id.zone_input_text);
        noSay = (TextView) findViewById(R.id.noSay);
        mBottomView.setVisibility(View.GONE);
        zoneListView.setOnTouchListener(this);
        mEditText.setOnTouchListener(this);
        mFaceSwitchBtn.setOnClickListener(this);
        mSendMsgBtn.setOnClickListener(this);

        initTopView();
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mEditText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mIsFaceShow) {
                        faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                        mIsFaceShow = false;
                        mFaceSwitchBtn
                                .setBackgroundResource(R.drawable.m_chat_emotion_selector);
                        return true;
                    }
                }
                return false;
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    mSendMsgBtn.setEnabled(true);
                } else {
                    mSendMsgBtn.setEnabled(false);
                }
            }
        });
    }

    private void initFacePage() {
        faceViewPage = new FaceViewPage(mEditText, mBottomView);
        faceViewPage.initFacePage();
    }

    protected abstract void initTopView();

    protected abstract void initDatas();

    protected abstract void refreshData();

    protected abstract void setAdapter();

    protected abstract void dealHandlers(Message msg);

    @Override
    public void onRefresh() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.zone_input_face) {
            if (!mIsFaceShow) {
                handler.postDelayed(new Runnable() {
                    // 解决此时界面会变形，有闪烁的现象
                    @Override
                    public void run() {
                        mFaceSwitchBtn
                                .setBackgroundResource(R.drawable.m_chat_keyboard_selector);
                        faceViewPage.getmFaceRoot().setVisibility(View.VISIBLE);
                        mIsFaceShow = true;
                    }
                }, 80);
                mInputMethodManager.hideSoftInputFromWindow(
                        mEditText.getWindowToken(), 0);
            } else {
                mEditText.requestFocus();
                mInputMethodManager.showSoftInput(mEditText, 0);
                mFaceSwitchBtn
                        .setBackgroundResource(R.drawable.m_chat_emotion_selector);
                faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                mIsFaceShow = false;
            }
        } else if (id == R.id.zone_input_send) {
            String content = mEditText.getText().toString().trim();
            if ("".equals(content)) {
                return;
            }
            DialogUtils.getInstants().showLoadingDialog(ZoneBaseActivity.this,
                    getString(R.string.send_comment), false);
            Zone zone = zoneList.get(currentPos);
            if (curComment == null) {
                zoneManager.comment(zone.getcId(),
                        MFSPHelper.getString(CommConstants.USERID), "0", content, "0",
                        "0", currentPos, handler);
            } else {
                zoneManager.comment(zone.getcId(),
                        MFSPHelper.getString(CommConstants.USERID),
                        curComment.getUserId(), content, curComment.getcId(),
                        "0", currentPos, handler);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.zone_input_text) {
            mInputMethodManager.showSoftInput(mEditText, 0);
            mFaceSwitchBtn
                    .setBackgroundResource(R.drawable.m_chat_emotion_selector);
            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
            mIsFaceShow = false;
        } else if (id == R.id.zone_listview) {
            mInputMethodManager.hideSoftInputFromWindow(
                    mEditText.getWindowToken(), 0);
            mFaceSwitchBtn
                    .setBackgroundResource(R.drawable.m_chat_emotion_selector);
            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
            mBottomView.setVisibility(View.GONE);
            mIsFaceShow = false;
        }
        return false;
    }

    protected class ItemsOnClick implements OnClickListener {
        int delCommentLine;
        int delPostion;

        public ItemsOnClick(int delCommentLine, int currentPos) {
            super();
            this.delCommentLine = delCommentLine;
            this.delPostion = currentPos;
        }

        @Override
        public void onClick(View v) {
            popWindow.dismiss();
            int id = v.getId();
            if (id == R.id.btn_del) {
                Zone zone = zoneList.get(delPostion);
                DialogUtils.getInstants().showLoadingDialog(context, getString(com.movit.platform.common.R.string.waiting), false);
                zoneManager.commentdel(zone.getComments().get(delCommentLine)
                        .getcId(), delPostion, delCommentLine, handler);
            }
        }
    }
}
