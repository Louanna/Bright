package com.movit.platform.sc.module.zone.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.movit.platform.cloud.net.AsyncNetHandler;
import com.movit.platform.cloud.net.INetHandler;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.faceview.FaceViewPage;
import com.movit.platform.framework.widget.SelectPicPopup;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movit.platform.framework.widget.xlistview.XListView.IXListViewListener;
import com.movit.platform.sc.R;
import com.movit.platform.sc.base.BaseFragment;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.entities.Zone;
import com.movit.platform.sc.module.zone.activity.ClipImageActivity;
import com.movit.platform.sc.module.zone.adapter.ZoneAdapter;
import com.movit.platform.sc.module.zone.model.BaseModel;
import com.movit.platform.sc.module.zone.model.Knowledge;
import com.movit.platform.sc.module.zone.model.KnowledgeComment;
import com.movit.platform.sc.module.zone.model.KnowledgeSay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class ZoneFragment extends BaseFragment implements
        IXListViewListener, OnTouchListener, OnClickListener, OnScrollListener {

    private String TAG = ZoneFragment.class.getSimpleName();

    protected XListView zoneListView;
    protected ZoneAdapter adapter;
    protected List<Knowledge> zoneList = new ArrayList<>();

    private String userId;

    private LinearLayout mBottomView;
    private FaceViewPage faceViewPage;
    private boolean mIsFaceShow = false;// 是否显示表情
    private Button mFaceSwitchBtn;// 切换表情的button
    private Button mSendMsgBtn;// 发送消息button
    private EditText mEditText;// 消息输入框
    private InputMethodManager mInputMethodManager;

    private int currentPage = 1;
    private int currentPos;
    private KnowledgeComment curComment;

    private SelectPicPopup popWindow;

    protected IZoneManager zoneManager;
    INetHandler netHandler = null;
    private CompleteReceiver completeReceiver;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    // 上传成功，发送消息
                    String message = (String) msg.obj;
                    try {
                        //去更新同事圈背景图名称
                        toUploadZonePic(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialogUtil.dismiss();
                    } finally {
                        // 删除拍照旋转的图片；
                        File f = new File(takePicturePath);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                    break;
                case 4:
                    progressDialogUtil.dismiss();
                    String uname = (String) msg.obj;
                    MFSPHelper.setString(SCConstants.ZONE_PIC, uname);
                    //更新UI
                    adapter.stopMediaPlay();
                    adapter.notifyDataSetChanged();
                    break;
                case 5:
                    progressDialogUtil.dismiss();
                    ToastUtils.showToast(getActivity(), getActivity().getResources().getString(R.string.bg_sync_fail));
                    break;
            }
        }
    };

    private void toUploadZonePic(final String picName) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("id", MFSPHelper.getString(CommConstants.USERID));
        paramsMap.put("backgroundImage", picName);
        String json = Obj2JsonUtils.map2json(paramsMap);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpUtils.postStringWithToken()
                .url(CommConstants.URL_UPDATE_USER_INFO)
                .content(json)
                .mediaType(JSON)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(5);
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean ok = jsonObject.getBoolean("ok");
                        if (ok) {
                            mHandler.obtainMessage(4, picName).sendToTarget();
                        } else {
                            mHandler.sendEmptyMessage(5);
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    protected Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            ArrayList<String> delList = data.getStringArrayList("delList");
            ArrayList<Zone> newList = (ArrayList<Zone>) data
                    .getSerializable("newList");
            ArrayList<Zone> oldList = (ArrayList<Zone>) data
                    .getSerializable("oldList");
            ArrayList<Zone> topList = (ArrayList<Zone>) data
                    .getSerializable("topList");

            switch (msg.what) {
                case SCConstants.ZONE_LIST_RESULT:
                    DialogUtils.getInstants().dismiss();

//                    if (zoneList.isEmpty()) {
//                        visitTop = true;
//                    }
//
//                    ArrayList<Knowledge> temp = new ArrayList<>();
//                    temp.addAll(zoneList);
//                    for (int i = 0; i < temp.size(); i++) {
//                        if (temp.get(i).getiTop() != null) {
//                            if (temp.get(i).getiTop().equals("1")) {
//                                zoneList.remove(temp.get(i));
//                            }
//                        }
//                    }
//                    temp.clear();
//                    temp.addAll(zoneList);
//                    for (int i = 0; i < currentTop.size(); i++) {
//                        long currTime = DateUtils.str2Date(
//                                currentTop.get(i).getdCreateTime()).getTime();
//                        for (int j = 0; j < temp.size(); j++) {
//                            if (j != 0) {
//                                long time1 = DateUtils.str2Date(
//                                        temp.get(j).getdCreateTime()).getTime();
//                                long time2 = DateUtils.str2Date(
//                                        temp.get(j - 1).getdCreateTime()).getTime();
//                                if (currTime < time2 && currTime > time1) {
//                                    currentTop.get(i).setiTop("0");
//                                    zoneList.add(j, currentTop.get(i));
//                                }
//                            }
//
//                        }
//                    }
//                    temp.clear();
//                    temp.addAll(zoneList);
//                    // 去除当前删除的，然后去除当前置顶的
//                    for (int i = 0; i < temp.size(); i++) {
//                        if (delList.contains(temp.get(i).getcId())) {
//                            zoneList.remove(temp.get(i));
//                        }
//                        if (topList.contains(temp.get(i))) {
//                            zoneList.remove(temp.get(i));
//                        }
//                    }
//                    temp = null;
//                    for (int j = 0; j < zoneList.size(); j++) {
//                        for (int j2 = 0; j2 < oldList.size(); j2++) {
//                            if (zoneList.get(j).equals(oldList.get(j2))) {
//                                zoneList.remove(j);
//                                zoneList.add(j, oldList.get(j2));
//                            }
//                        }
//                    }
//                    zoneList.addAll(0, newList);
//                    zoneList.addAll(0, topList);
//                    currentTop.clear();
//                    currentTop.addAll(topList);
//
//                    // ArrayList<Zone> arrayList = (ArrayList<Zone>) msg.obj;
//                    // zoneList.addAll(0, arrayList);
//                    zoneListView.stopRefresh();
//                    zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
//                    if (adapter != null) {
//                        adapter.stopMediaPlay();
//                        adapter.notifyDataSetChanged();
//                    } else {
//                        setAdapter();
//
//                    }
//                    if (visitTop) {
//                        zoneListView.setSelection(0);
//                        visitTop = false;
//                    }
                    break;
                case SCConstants.ZONE_MORE_RESULT:
//                    ArrayList<Zone> temp2 = new ArrayList<>();
//                    temp2.addAll(zoneList);
//                    for (int i = 0; i < temp2.size(); i++) {
//                        if (temp2.get(i).getiTop() != null) {
//                            if (temp2.get(i).getiTop().equals("1")) {
//                                zoneList.remove(temp2.get(i));
//                            }
//                        }
//                    }
//                    temp2.clear();
//                    temp2.addAll(zoneList);
//                    for (int i = 0; i < currentTop.size(); i++) {
//                        long currTime = DateUtils.str2Date(
//                                currentTop.get(i).getdCreateTime()).getTime();
//                        for (int j = 0; j < temp2.size(); j++) {
//                            if (j != 0) {
//                                long time1 = DateUtils.str2Date(
//                                        temp2.get(j).getdCreateTime()).getTime();
//                                long time2 = DateUtils.str2Date(
//                                        temp2.get(j - 1).getdCreateTime())
//                                        .getTime();
//                                if (currTime < time2 && currTime > time1) {
//                                    Log.v("top", "插入" + j);
//                                    currentTop.get(i).setiTop("0");
//                                    zoneList.add(j, currentTop.get(i));
//                                }
//                            }
//                        }
//                    }
//                    temp2.clear();
//                    temp2.addAll(zoneList);
//                    // 去除当前删除的，然后去除当前置顶的
//                    for (int i = 0; i < temp2.size(); i++) {
//                        if (delList.contains(temp2.get(i).getcId())) {
//                            zoneList.remove(temp2.get(i));
//                        }
//                        if (topList.contains(temp2.get(i))) {
//                            zoneList.remove(temp2.get(i));
//                        }
//                    }
//                    temp2 = null;
//                    for (int j = 0; j < zoneList.size(); j++) {
//                        for (int j2 = 0; j2 < oldList.size(); j2++) {
//                            if (zoneList.get(j).equals(oldList.get(j2))) {
//                                zoneList.remove(j);
//                                zoneList.add(j, oldList.get(j2));
//                            }
//                        }
//                    }
//                    zoneList.addAll(newList);
//                    zoneList.addAll(0, topList);
//                    currentTop.clear();
//                    currentTop.addAll(topList);
//                    // ArrayList<Zone> moreList = (ArrayList<Zone>) msg.obj;
//                    // zoneList.addAll(moreList);
//
//                    zoneListView.stopLoadMore();
//                    adapter.stopMediaPlay();
//                    adapter.notifyDataSetChanged();
                    break;
                case SCConstants.ZONE_CLICK_AVATAR:// click avatar
                    String userId = (String) msg.obj;

                    ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                            .getAttentionPO();

                    try {
                        boolean gozone = getActivity().getPackageManager().getApplicationInfo(
                                getActivity().getPackageName(), PackageManager.GET_META_DATA).metaData
                                .getBoolean("CHANNEL_ATTENTION_SEEZONE", false);
                        if (gozone) {
                            if (StringUtils.notEmpty(userId) && (MFSPHelper.getString(CommConstants.USERID).equals(userId) || (null != idStrings && idStrings.contains(userId)))) {
                                Intent intent = new Intent();
                                intent.putExtra("userId", userId);
                                ((BaseApplication) ZoneFragment.this.getActivity().getApplication()).getUIController().onZoneOwnClickListener(ZoneFragment.this, intent, SCConstants.ZONE_CLICK_AVATAR);
                            } else {
                                UserDao dao = UserDao.getInstance(ZoneFragment.this.getActivity());
                                String empCname = dao.getUserInfoById(userId).getEmpCname();
                                ToastUtils.showToast(ZoneFragment.this.getActivity(), "关注" + empCname + "后才能查看同事圈");
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("userId", userId);
                            ((BaseApplication) ZoneFragment.this.getActivity().getApplication()).getUIController().onZoneOwnClickListener(ZoneFragment.this, intent, SCConstants.ZONE_CLICK_AVATAR);
                        }
                    } catch (PackageManager.NameNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    break;
                case SCConstants.ZONE_NICE_RESULT:
//                    DialogUtils.getInstants().dismiss();
//                    try {
//                        String result = (String) msg.obj;
//                        JSONObject jsonObject = new JSONObject(result);
//                        int code = jsonObject.getInt("code");
//                        if (0 == code) {
//                            int postion = msg.arg1;
//                            int isNice = msg.arg2;
//                            List<KnowledgeLike> likes = zoneList.get(postion).getKnowledgeLike();
//                            if (isNice == 0) {// 赞
//                                if (likes.getLikers() == null) {
//                                    ArrayList<String> likser = new ArrayList<>();
//                                    likser.add(MFSPHelper.getString(CommConstants.USERID));
//                                    likes.setLikers(likser);
//                                } else {
//                                    likes.getLikers().add(0,
//                                            MFSPHelper.getString(CommConstants.USERID));
//                                }
//                            } else if (isNice == 1) {// 取消赞
//                                if (likes.getLikers() != null) {
//                                    likes.getLikers().remove(
//                                            MFSPHelper.getString(CommConstants.USERID));
//                                }
//                            }
//                            adapter.stopMediaPlay();
//                            adapter.notifyDataSetChanged();
//                        } else {
//                            ToastUtils.showToast(getActivity(), "点赞失败！");
//                            adapter.stopMediaPlay();
//                            adapter.notifyDataSetChanged();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        ToastUtils.showToast(getActivity(), "点赞失败！");
//                    }
                    break;
                case SCConstants.ZONE_CLICK_COMMENT:// 评论
                    currentPos = (Integer) msg.obj;
                    int commentLine = msg.arg1;
                    int type = msg.arg2;
                    if (type == 0) {
                        mEditText.setHint("评论");
                        curComment = null;
                    } else if (type == 1) {// 在评论
                        List<KnowledgeComment> comments = zoneList.get(currentPos)
                                .getKnowledgeComment();
                        curComment = comments.get(commentLine);
                        UserDao dao = UserDao.getInstance(getActivity());
                        UserInfo userInfo = dao.getUserInfoById(curComment
                                .getUserId());
                        if (userInfo != null) {
                            mEditText.setHint("回复：" + userInfo.getEmpCname());
                        }
                    }
                    // 弹出输入框
                    mBottomView.setVisibility(View.VISIBLE);
                    mEditText.requestFocus();
                    mInputMethodManager.showSoftInput(mEditText, 0);

                    break;
                case SCConstants.ZONE_COMMENT_RESULT:
//                    DialogUtils.getInstants().dismiss();
//                    try {
//                        String comment_result = (String) msg.obj;
//                        JSONObject jsonObject = new JSONObject(comment_result);
//                        int code = jsonObject.getInt("code");
//                        if (0 == code) {
//                            int postion = msg.arg1;
//                            Zone zone = zoneList.get(postion);
//
//                            JSONObject itemObject = jsonObject
//                                    .getJSONObject("item");
//                            Comment comment = new Comment();
//                            if (itemObject.has("sContent")) {
//                                comment.setContent(itemObject.getString("sContent"));
//                            }
//                            if (itemObject.has("cUserId")) {
//                                comment.setUserId(itemObject.getString("cUserId"));
//                            }
//                            if (itemObject.has("cToUserId")) {
//                                comment.setTouserId(itemObject
//                                        .getString("cToUserId"));
//                            }
//                            if (itemObject.has("cId")) {
//                                comment.setcId(itemObject.getString("cId"));
//                            }
//                            if (itemObject.has("cParentId")) {
//                                String cParentId = itemObject
//                                        .getString("cParentId");
//                                comment.setParnetId(cParentId);
//                            }
//                            if (itemObject.has("cRootId")) {
//                                String cRootId = itemObject.getString("cRootId");
//                                comment.setRootId(cRootId);
//                            }
//                            if (itemObject.has("cSayId")) {
//                                String cSayId = itemObject.getString("cSayId");
//                                comment.setSayId(cSayId);
//                            }
//                            if (zone.getComments() == null) {
//                                ArrayList<Comment> comments = new ArrayList<>();
//                                comments.add(comment);
//                                zone.setComments(comments);
//                            } else {
//                                zone.getComments().add(comment);
//                            }
//                            mInputMethodManager.hideSoftInputFromWindow(
//                                    mEditText.getWindowToken(), 0);
//                            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
//                            mBottomView.setVisibility(View.GONE);
//                            mIsFaceShow = false;
//
//                            mEditText.setText("");
//                            adapter.stopMediaPlay();
//                            adapter.notifyDataSetChanged();
//                        } else {
//                            ToastUtils.showToast(getActivity(), "发表评论失败！");
//                            adapter.stopMediaPlay();
//                            adapter.notifyDataSetChanged();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        ToastUtils.showToast(getActivity(), "发表评论失败！");
//                    }
                    break;
                case SCConstants.ZONE_MESSAGE_COUNT_RESULT:// 轮询 消息提醒
                    break;
                case SCConstants.ZONE_CLICK_COMMENT_TO_DEL:
                    int delCommentLine = msg.arg1;
                    int delPostion = msg.arg2;

                    // 弹出菜单popWindow
                    InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
                    }
                    // 实例化SelectPicPopupWindow
                    popWindow = new SelectPicPopup(getActivity(),
                            new ItemsOnClick(delCommentLine, delPostion));
                    popWindow.showDel();
                    // 显示窗口
                    popWindow.showAtLocation(
                            getActivity().findViewById(R.id.zone_listview),
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置

                    break;
                case SCConstants.ZONE_COMMENT_DEL_RESULT:
//                    DialogUtils.getInstants().dismiss();
//                    try {
//                        String comment_result = (String) msg.obj;
//                        JSONObject jsonObject = new JSONObject(comment_result);
//                        int code = jsonObject.getInt("code");
//                        if (0 == code) {
//                            int postion = msg.arg1;
//                            int delLine = msg.arg2;
//                            Zone zone = zoneList.get(postion);
//                            zone.getComments().remove(delLine);
//                            adapter.stopMediaPlay();
//                            adapter.notifyDataSetChanged();
//                        } else {
//                            ToastUtils.showToast(getActivity(), "删除失败！");
//                            adapter.stopMediaPlay();
//                            adapter.notifyDataSetChanged();
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }

                    break;
                case SCConstants.ZONE_ERROR_RESULT:
                    DialogUtils.getInstants().dismiss();
                    String errorMsg = (String) msg.obj;
                    ToastUtils.showToast(getActivity(), errorMsg);
                    zoneListView.stopRefresh();
                    zoneListView.stopLoadMore();
                    if (adapter == null) {
                        setAdapter();
                    } else {
                        adapter.stopMediaPlay();
                        adapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zoneManager = ((BaseApplication) getActivity().getApplication()).getManagerFactory().getZoneManager();
        userId = MFSPHelper.getString(CommConstants.USERID);
        netHandler = AsyncNetHandler.getInstance(getActivity());

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        getActivity().registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.sc_fragment_zone, null, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPause() {
        adapter.stopMediaPlay();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null!=adapter){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(completeReceiver);
    }

    @Override
    protected void initViews() {
        mInputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(INPUT_METHOD_SERVICE);
        zoneListView = (XListView) findViewById(R.id.zone_listview);
        mBottomView = (LinearLayout) findViewById(R.id.zone_bottom_rootview);
        mFaceSwitchBtn = (Button) findViewById(R.id.zone_input_face);
        mSendMsgBtn = (Button) findViewById(R.id.zone_input_send);
        mEditText = (EditText) findViewById(R.id.zone_input_text);
        mBottomView.setVisibility(View.GONE);
        zoneListView.setOnTouchListener(this);
        mEditText.setOnTouchListener(this);
        mFaceSwitchBtn.setOnClickListener(this);
        mSendMsgBtn.setOnClickListener(this);

        initFacePage();// 初始化表情页面

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
        faceViewPage = new FaceViewPage(mEditText, mRootView);
        faceViewPage.initFacePage();

    }

    @Override
    protected void initDatas() {
        DialogUtils.getInstants().showLoadingDialog(getActivity(), "正在加载...", false);
        zoneList.clear();
        currentPage = 1;

//        UserDao dao = UserDao.getInstance(getActivity());
//        String officeId = dao.getUserInfoById(userId).getOrgId();
//        zoneManager.getZoneList(officeId, "", "", "", "", "", "", handler);
        zoneManager.getKnowledge(userId, currentPage, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ToastUtils.showToast(getActivity(),getString(R.string.get_message_fail));
            }

            @Override
            public void onResponse(String response) throws JSONException {
                BaseModel baseModel = JSON.parseObject(response,BaseModel.class);
                if(null != baseModel){
                    if(baseModel.getOk()){
                        String obj = JSON.toJSONString(baseModel.getObjValue());
                        zoneList = JSON.parseArray(obj,Knowledge.class);
                        setAdapter();
                        DialogUtils.getInstants().dismiss();
                        currentPage++;
                    }else {
                        ToastUtils.showToast(getActivity(),baseModel.getValue());
                    }
                }else {
                    ToastUtils.showToast(getActivity(),getString(R.string.get_message_fail));
                }
            }
        });
    }

    private void setAdapter() {
        adapter = new ZoneAdapter(getActivity(), zoneList,
                handler, ZoneAdapter.TYPE_MAIN,
                userId, DialogUtils.getInstants(), zoneManager);
        zoneListView.setAdapter(adapter);
        if (zoneList.isEmpty()) {
            zoneListView.setPullLoadEnable(false);
        } else {
            zoneListView.setPullLoadEnable(true);
        }
        zoneListView.setPullRefreshEnable(true);
        zoneListView.setXListViewListener(this);
        zoneListView.setOnScrollListener(this);
        zoneListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            }
        });
        adapter.setDownFileListener(new ZoneAdapter.DownFileListener() {
            @Override
            public void downLoad(String listFileName, String docId,String downFilePath) {
                downDocument(listFileName,docId,downFilePath);
            }
        });
    }

    @Override
    protected void resumeDatas() {
        //zoneManager.messagecount(handler);
    }


    @Override
    public void onRefresh() {
        currentPage = 1;
        zoneList.clear();
        zoneManager.getKnowledge(userId, currentPage, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ToastUtils.showToast(getActivity(),getString(R.string.get_message_fail));
                zoneListView.stopRefresh();
                zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
            }

            @Override
            public void onResponse(String response) throws JSONException {
                BaseModel baseModel = JSON.parseObject(response,BaseModel.class);
                if(null != baseModel){
                    if(baseModel.getOk()){
                        String obj = JSON.toJSONString(baseModel.getObjValue());
                        List<Knowledge> list = JSON.parseArray(obj,Knowledge.class);
                        if(list.size() > 0){
                            zoneListView.setPullLoadEnable(true);
                            zoneList.addAll(list);
                            currentPage++;
                        }else {
                            zoneListView.setPullLoadEnable(false);
                        }
                        zoneListView.stopLoadMore();
                        adapter.notifyDataSetChanged();
                        DialogUtils.getInstants().dismiss();
                    }else {
                        ToastUtils.showToast(getActivity(),baseModel.getValue());
                    }
                }else {
                    ToastUtils.showToast(getActivity(),getString(R.string.get_message_fail));
                }
                zoneListView.stopRefresh();
                zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
            }
        });

//        if (zoneList.isEmpty()) {
//            UserDao dao = UserDao.getInstance(getActivity());
//            String officeId = dao.getUserInfoById(userId).getOrgId();
//
//            zoneManager.getZoneList(officeId, MFSPHelper.getString("refreshTime"), "",
//                    "", "", "", "", handler);
//
//        } else {
//            String tCreateTime = "";
//            for (int i = 0; i < zoneList.size(); i++) {
//                if (zoneList.get(i).getiTop() != null) {
//                    if (zoneList.get(i).getiTop().equals("0")) {
//                        tCreateTime = zoneList.get(i).getdCreateTime();
//                        break;
//                    }
//                }
//            }
//
//            UserDao dao = UserDao.getInstance(getActivity());
//            String officeId = dao.getUserInfoById(userId).getOrgId();
//
//            zoneManager.getZoneList(officeId, MFSPHelper.getString("refreshTime"),
//                    tCreateTime, zoneList.get(zoneList.size() - 1)
//                            .getdCreateTime(), "0", "", "", handler);
//        }
    }

    @Override
    public void onLoadMore() {
//        String tCreateTime = "";
//        for (int i = 0; i < zoneList.size(); i++) {
//            if (zoneList.get(i).getiTop() != null) {
//                if (zoneList.get(i).getiTop().equals("0")) {
//                    tCreateTime = zoneList.get(i).getdCreateTime();
//                    break;
//                }
//            }
//        }
//        if (!zoneList.isEmpty()) {
//
//            UserDao dao = UserDao.getInstance(getActivity());
//            String officeId = dao.getUserInfoById(userId).getOrgId();
//
//            zoneManager.getZoneList(officeId, MFSPHelper.getString("refreshTime"),
//                    tCreateTime, zoneList.get(zoneList.size() - 1)
//                            .getdCreateTime(), "1", "", "", handler);
//        } else {
//            zoneListView.stopLoadMore();
//        }
        Log.d(TAG, "onLoadMore: currentPage"+currentPage);
        zoneManager.getKnowledge(userId, currentPage, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ToastUtils.showToast(getActivity(),getString(R.string.get_message_fail));
            }

            @Override
            public void onResponse(String response) throws JSONException {
                BaseModel baseModel = JSON.parseObject(response,BaseModel.class);
                if(null != baseModel){
                    if(baseModel.getOk()){
                        String obj = JSON.toJSONString(baseModel.getObjValue());
                        List<Knowledge> list = JSON.parseArray(obj,Knowledge.class);
                        if(list.size() > 0){
                            zoneListView.setPullLoadEnable(true);
                            zoneList.addAll(list);
                            currentPage++;
                        }else {
                            zoneListView.setPullLoadEnable(false);
                        }
                        zoneListView.stopLoadMore();
                        adapter.notifyDataSetChanged();
                        DialogUtils.getInstants().dismiss();
                    }else {
                        ToastUtils.showToast(getActivity(),baseModel.getValue());
                    }
                }else {
                    ToastUtils.showToast(getActivity(),getString(R.string.get_message_fail));
                }
            }
        });
    }

    private String takePicturePath = "";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1) {// 发表成功
                onRefresh();// 滑到top
        } else if (requestCode == SCConstants.ZONE_CLICK_AVATAR) {
            if (resultCode == SCConstants.ZONE_SAY_DEL_RESULT) {// 删除成功
                onRefresh();
            }else {
                // 取得裁剪后的图片
                if (data != null) {
                    takePicturePath = data.getStringExtra("takePicturePath");
                    if (StringUtils.notEmpty(takePicturePath)) {
                        progressDialogUtil.showDownLoadingDialog(getActivity(),
                                "正在上传...", false);

                        // 上传图片
                        zoneManager.uploadFile(takePicturePath,mHandler);
                    }
                }
            }
        }else {
            String currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    // 如果是直接从相册获取
                    case 1:
                        // 从相册中直接获取文件的真是路径，然后上传
                        String picPath = PicUtils
                                .getPicturePath(data, getActivity());
                        startActivityForResult(new Intent(getActivity(),
                                ClipImageActivity.class).putExtra(
                                "takePicturePath", picPath), 3);

                        break;
                    // 如果是调用相机拍照时
                    case 2:
                        boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg");
                        new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                        if (copy) {
                            String path = CommConstants.SD_CARD_IMPICTURES + currentTime + ".jpg";
                            startActivityForResult(new Intent(getActivity(),
                                    ClipImageActivity.class).putExtra(
                                    "takePicturePath", path), 3);
                            PicUtils.scanImages(getActivity(), path);
                        }
                        break;
                    default:
                        break;
                }
            } else {
                if (StringUtils.notEmpty(currentTime)) {
                    String path = CommConstants.SD_CARD_IMPICTURES + currentTime
                            + ".jpg";
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    String newPathString = PicUtils.getTempPicPath(path);
                    File f = new File(newPathString);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
        }
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
            final String content = mEditText.getText().toString().trim();
            if ("".equals(content)) {
                return;
            }
            DialogUtils.getInstants()
                    .showLoadingDialog(getActivity(), "发表评论...", false);
//            Zone zone = zoneList.get(currentPos);
//            if (curComment == null) {
//                zoneManager.comment(zone.getcId(),
//                        userId, "0", content, "0",
//                        "0", currentPos, handler);
//            } else {
//                zoneManager.comment(zone.getcId(),
//                        userId,
//                        curComment.getUserId(), content, curComment.getcId(),
//                        "0", currentPos, handler);
//            }
            final KnowledgeSay knowledgeSay = zoneList.get(currentPos).getKlnowledgeSay();
            zoneManager.saveKnowledgeComment(knowledgeSay.getId(), userId,
                    content, new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            DialogUtils.getInstants().dismiss();
                            ToastUtils.showToast(getActivity(), "发表评论失败！");
                        }

                        @Override
                        public void onResponse(String response) throws JSONException {

                            KnowledgeComment comment = new KnowledgeComment(DateUtils.getCurDateStr()
                                    ,knowledgeSay.getId(),userId
                                    ,MFSPHelper.getString(CommConstants.USERNAME)
                                    ,content);
                            zoneList.get(currentPos).getKnowledgeComment().add(comment);

                            DialogUtils.getInstants().dismiss();
                            mInputMethodManager.hideSoftInputFromWindow(
                                    mEditText.getWindowToken(), 0);
                            faceViewPage.getmFaceRoot().setVisibility(View.GONE);
                            mBottomView.setVisibility(View.GONE);
                            mIsFaceShow = false;

                            mEditText.setText("");
                            adapter.notifyDataSetChanged();
                        }
                    });
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex + 2; // 加上底部的loadMoreView项 和顶部刷新的
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex) {
            // 如果是自动加载,可以在这里放置异步加载数据的代码
            zoneListView.startLoadMore();
        }
    }

    int visibleLastIndex;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private class ItemsOnClick implements OnClickListener {
        int delCommentLine;
        int delPostion;

        ItemsOnClick(int delCommentLine, int currentPos) {
            super();
            this.delCommentLine = delCommentLine;
            this.delPostion = currentPos;
        }

        @Override
        public void onClick(View v) {
            popWindow.dismiss();
            int id = v.getId();
            if (id == R.id.btn_del) {
//                Zone zone = zoneList.get(delPostion);
//                DialogUtils.getInstants().showLoadingDialog(getActivity(), getString(R.string.waiting),
//                        false);
//                zoneManager.commentdel(zone.getComments().get(delCommentLine)
//                        .getcId(), delPostion, delCommentLine, handler);
            }
        }
    }

    /**
     * 下载文档
     */
    private void downDocument(String listFileName,String docId,String downFilePath) {

        progressDialogUtil.showLoadingDialog(getActivity(),getString(R.string.pull_to_refresh_footer_refreshing_label),false);

        String destFileDir = CommConstants.SD_DOCUMENT;
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String url = CommConstants.URL_DOWN+downFilePath;

        HttpManager.downloadFile(url, new FileCallBack(destFileDir,listFileName) {
            @Override
            public void inProgress(float progress, long total) {

            }

            @Override
            public void onError(Call call, Exception e) {

                progressDialogUtil.dismiss();
                Toast.makeText(ZoneFragment.this.getActivity(), com.movit.platform.cloud.R.string.sky_drive_download_failed, Toast.LENGTH_LONG).show();
                //down failed
//                if(!TextUtils.isEmpty(fileName)){
//                    File file = new File(fileName);
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                }
            }

            @Override
            public void onResponse(File response) throws JSONException {

                progressDialogUtil.dismiss();

                //纪录下载文档日志
                FileUtils fileUtils = new FileUtils();
                Intent intent1 = new Intent();
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.setAction(Intent.ACTION_VIEW);
                intent1.setDataAndType(Uri.fromFile(response), fileUtils.getMIMEType(response.getName()));
                ZoneFragment.this.getActivity().startActivity(intent1);

            }
        });

//        long downloadId = netHandler.downKnowDocument(listFileName, destFileDir.replace(CommConstants.SD_CARD, ""),downFilePath);
//        Log.d(TAG, "downDocument --> downloadId : "+downloadId);
        Log.d(TAG, "downDocument --> downFilePath : "+downFilePath);
//        CloudApplication.downloadIdMap.put(downloadId,listFileName);
//        downloadFileNameMap.put(downloadId,listFileName);

    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get complete download id
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d(TAG, "onReceive: "+completeDownloadId);
            DownloadManager downloadManager = (DownloadManager)getActivity().getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(completeDownloadId);
            Cursor c = null;
            try {
                c = downloadManager.query(query);
                if (c != null && c.moveToFirst()) {
                    int downloadStatus = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    int fileNameIdx =c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                    String fileName = c.getString(fileNameIdx);
                    // TODO Do something with the file.
                    //Log.d(TAG, fileName);
                    Log.d(TAG, "onReceive: --> downloadStatus: "+downloadStatus);

                    if(downloadStatus == DownloadManager.STATUS_SUCCESSFUL){
                        //纪录下载文档日志
                        FileUtils fileUtils = new FileUtils();
                        Intent intent1 = new Intent();
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.setAction(Intent.ACTION_VIEW);
                        intent1.setDataAndType(Uri.fromFile(new File(fileName)), fileUtils.getMIMEType(fileName));
                        context.startActivity(intent1);

                    }else if(downloadStatus == DownloadManager.STATUS_FAILED){
                        // to do here


                        Toast.makeText(context, com.movit.platform.cloud.R.string.sky_drive_download_failed, Toast.LENGTH_LONG).show();
                        //down failed
                        if(!TextUtils.isEmpty(fileName)){
                            File file = new File(fileName);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            progressDialogUtil.dismiss();
        }
    }

}
