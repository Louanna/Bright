package com.movit.platform.im.base;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
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
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.cloud.activity.SkyDriveActivity;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.common.entities.SerializableObj;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.Audio2Mp3Utils;
import com.movit.platform.framework.utils.Base64Utils;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.faceview.FacePageAdeapter;
import com.movit.platform.framework.view.faceview.FaceViewPage;
import com.movit.platform.framework.view.pageIndicator.CirclePageIndicator;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movit.platform.framework.widget.xlistview.XListView.IXListViewListener;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.db.IMDBFactory;
import com.movit.platform.im.db.RecordsManager;
import com.movit.platform.im.helper.ServiceHelper;
import com.movit.platform.im.manager.MessageManager;
import com.movit.platform.im.manager.XmppManager;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.location.MapViewActivity;
import com.movit.platform.im.module.single.activity.ChatActivity;
import com.movit.platform.im.module.single.adapter.ChatAdapter;
import com.movit.platform.im.utils.JSONConvert;
import com.movit.platform.im.widget.CurEditText;
import com.movit.platform.im.widget.TextChangedListener;
import com.qd.recorder.FFmpegRecorderActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

@SuppressLint("InflateParams")
@SuppressWarnings({"unchecked", "deprecation"})
public abstract class ChatBaseActivity extends IMBaseActivity implements
        OnTouchListener, OnClickListener, IXListViewListener, ChatClickListener, TextChangedListener.CurKeyClickedListener, MessageManager.OnSendMsgProcessListener {

    //获取相册图片
    protected final int REQUEST_CODE_SEND_PIC_ALBUM = 1;
    //通过相机拍照获取图片
    protected final int REQUEST_CODE_SEND_PIC_CAMERA = REQUEST_CODE_SEND_PIC_ALBUM + 1;
    //获取视频
    protected final int REQUEST_CODE_SEND_VIDEO = REQUEST_CODE_SEND_PIC_CAMERA + 1;
    //获取文件
    protected final int REQUEST_CODE_SEND_FILE = REQUEST_CODE_SEND_VIDEO + 1;
    //获取文档管理文件
    protected final int REQUEST_CODE_SEND_FILE_2 = REQUEST_CODE_SEND_FILE + 1;
    //分享文档
    protected final int REQUEST_CODE_SHARE_FILE = REQUEST_CODE_SEND_FILE_2 + 1;
    //定位
    protected final int REQUEST_CODE_LOCATION = REQUEST_CODE_SHARE_FILE + 1;
    //选取相册图片
    protected final int REQUEST_CODE_SELECTED_PIC = REQUEST_CODE_LOCATION + 1;
    //跳转到聊天详情页面
    protected final int REQUEST_CODE_CHAT_DETAIL_PAGE = REQUEST_CODE_SELECTED_PIC + 1;
    //跳转到ImageViewPager页面
    public final int REQUEST_CODE_VIEWPAGER_PAGE = REQUEST_CODE_CHAT_DETAIL_PAGE + 1;

    //记录当前聊天窗口中，从db获取到的状态为isSending的聊天记录
    protected static JSONArray curUnSendRecords;

    // 对话ListView
    protected XListView mMsgListView;
    //群聊界面@提示
    protected TextView tv_tips;

    private LinearLayout mFaceRoot;// 表情父容器
    private FaceViewPage mfaceViewPage;

    private LinearLayout mMenuRoot;// 菜单父容器
    private ViewPager mMenuViewPager;// 菜单选择ViewPager

    private boolean mIsFaceShow = false;// 是否显示表情
    private boolean mIsVoiceShow = false;// 是否语音输入
    private boolean mIsMenuShow = false;// 是否显示菜单

    private Button mFaceSwitchBtn;// 切换表情的button
    private Button mSendMsgBtn;// 发送消息button
    private Button mVoiceBtn;// 语音
    private Button mAddMoreBtn;// 更多，上传图片等
    private Button mVoiceSpeakBtn;// 按住 说话
    protected CurEditText mChatEditText;// 消息输入框

    protected TextView mTopTitle;// 标题栏
    protected ImageView mTopLeftImage, mTopRightImage;

    private WindowManager.LayoutParams mWindowNanagerParams;
    private InputMethodManager mInputMethodManager;

    private LinearLayout voice_rcd_hint_rcding, voice_rcd_hint_tooshort,
            voice_rcd_hint_cancle;
    private View chat_voice_popup;

    private Audio2Mp3Utils _2Mp3util = null;
    private String voiceFileNameRaw;
    private String voiceFileNameMp3;
    private int flag = 1;
    private long startVoiceT, endVoiceT;
    protected ImageView volume;
    protected ImageView scImage;

    private Uri imageUri;// The Uri to store the big
    protected String currentTime;

    //如果是单聊，则sessionObjId为friendId
    //如果是群聊，则sessionObjId为groupId
    protected String sessionObjId;
    //是否需要刷新页面列表
    protected boolean isRefresh = false;
    protected List<MessageBean> message_pool = new ArrayList<>();
    protected ChatAdapter chatAdapter;
    protected int ctype;
    protected int groupType;

    protected Context mContext;
    protected ServiceHelper tools;
    protected DialogUtils proDialogUtil;
    protected RecordsManager recordsManager;

    private CompleteReceiver completeReceiver;

    public MessageBean message;

    public static Map<Long, String> downloadMap = new HashMap<>();
    public static Map<String, String> msgIdMap = new HashMap<>();

    //增加耳边听语音
    private Sensor mSensor;
    private AudioManager mAudioManager;
    private SensorManager mSensorManager;
    private SensorEventListener mEventListener;

    private File mVideoFile;
    private File mThumbnailFile;
    static final String VIDEO_FILE_EXTENSION = ".mp4";
    static final String VIDEO_FILE_POSTFIX = "temp_video" + VIDEO_FILE_EXTENSION;
    static final String THUMBNAIL_FILE_EXTENSION = ".jpg";
    static final String THUMBNAIL_FILE_POSTFIX = "temp_thumbnail" + THUMBNAIL_FILE_EXTENSION;

    private boolean isRoll= false;

    @SuppressLint("HandlerLeak")
    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommConstants.MSG_SEND_SUCCESS:
                    // 刷新界面
                    MessageBean messageDataObj = (MessageBean) msg.obj;
                    for (int i = message_pool.size() - 1; i >= 0; i--) {
                        if (message_pool.get(i).getMsgId()
                                .equals(messageDataObj.getMsgId())
                                && message_pool.get(i).getIsSend() == CommConstants.MSG_SEND_PROGRESS) {
                            message_pool.get(i)
                                    .setIsSend(CommConstants.MSG_SEND_SUCCESS);
                            message_pool.get(i).setContent(
                                    messageDataObj.getContent());
                            chatAdapter.refreshChatData(message_pool);
                        }
                    }
                    boolean flag = false;
                    for (int j = 0; j < IMConstants.contactListDatas.size(); j++) {
                        MessageBean bean = IMConstants.contactListDatas.get(j);
                        if ((bean.getFriendId().equalsIgnoreCase(sessionObjId) && bean.getCtype() == ctype && ctype == 0) || (bean.getRoomId().equalsIgnoreCase(sessionObjId) && bean.getCtype() == ctype && ctype == 1)) {
                            IMConstants.contactListDatas.remove(j);
                            IMConstants.contactListDatas.add(0,
                                    message_pool.get(message_pool.size() - 1));
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        IMConstants.contactListDatas
                                .add(0, message_pool.get(message_pool.size() - 1));
                    }
                    break;
                case CommConstants.MSG_SEND_FAIL:
                    // 刷新界面
                    MessageBean messageDataObj2 = (MessageBean) msg.obj;
                    for (int i = message_pool.size() - 1; i >= 0; i--) {
                        if (message_pool.get(i).getMsgId()
                                .equals(messageDataObj2.getMsgId())
                                && message_pool.get(i).getIsSend() == CommConstants.MSG_SEND_PROGRESS) {
                            message_pool.get(i).setIsSend(CommConstants.MSG_SEND_FAIL);
                            chatAdapter.refreshChatData(message_pool);
                        }
                    }
                    //更新本地聊天记录，只修改发送状态
                    recordsManager.updateRecord(null, CommConstants.MSG_SEND_FAIL, messageDataObj2.getMsgId());
                    break;
                case CommConstants.MSG_SEND_RESEND:
                    final int postion = (Integer) msg.obj;
                    try {
                        if (XmppManager.getInstance().isConnected()) {
                            MessageBean failedMessageBean = message_pool.get(postion);

                            if (failedMessageBean.getIsSend() == CommConstants.MSG_SEND_FAIL) {
                                //更新UI
                                failedMessageBean.setIsSend(CommConstants.MSG_SEND_PROGRESS);
                                chatAdapter.refreshChatData(message_pool);
                                //重新发送
                                MessageManager.getInstance(mContext).reSendMessage(failedMessageBean);
                                //更新本地聊天记录，只修改发送状态
                                recordsManager.updateRecord(null, CommConstants.MSG_SEND_PROGRESS, failedMessageBean.getMsgId());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private boolean hasNewMes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_activity_chat_base);
        hasNewMes = getIntent().getBooleanExtra("hasNewMes", false);
        tools = new ServiceHelper();
        proDialogUtil = DialogUtils.getInstants();

        recordsManager = IMDBFactory.getInstance(this).getRecordsManager();

        initView();// 初始化view
        initFacePage();// 初始化表情页面
        initMenuPage();// 初始化菜单页面

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // 初始化数据
        initData();
        initTopBar();
        initListAdapter();

        chatAdapter.setShareFileListener(new ChatAdapter.ShareFileListener() {
            @Override
            public void shareFile(MessageBean messageBean) {
                message = messageBean;
                Intent intent = new Intent();
                intent.putExtra("TITLE", getString(R.string.select_contacts));
                intent.putExtra("ACTION", "forward");
                ((BaseApplication) getApplication()).getUIController().
                        onIMOrgClickListener(ChatBaseActivity.this, intent, REQUEST_CODE_SHARE_FILE);
            }
        });

        mMsgListView.setAdapter(chatAdapter);
        mMsgListView.setSelection(chatAdapter.getCount() - 1);
        mMsgListView.setPullRefreshEnable(true);
        mMsgListView.setPullLoadEnable(false);

        initAudioAndSensor();
    }

    private void initAudioAndSensor() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //TYPE_PROXIMITY是距离传感器类型，当然你还可以换成其他的，比如光线传感器
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float mProximiny = event.values[0];
                if (mProximiny == mSensor.getMaximumRange()) {
                    //扬声器播放模式
                    setModeNormal();
                } else {
                    //听筒播放模式
                    setInCallBySdk();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUnReadCount();
        mSensorManager.registerListener(mEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (XmppManager.getInstance().isConnected()&&XmppManager.getInstance().getConnection().isAuthenticated()) {
            sendEnterSession();
        } else {
            /**
             * add by zoro.qian
             * 延迟2秒，等待Xmpp连接成功
             */
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendEnterSession();
                }
            }, 2 * 1000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegistListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(completeReceiver);

        IMConstants.CHATTING_ID = "";
        IMConstants.CHATTING_TYPE = "";
    }

    @Override
    public void onBackPressed() {
        IMConstants.CHATTING_ID = "";
        IMConstants.CHATTING_TYPE = "";

        if (null != chatAdapter) {
            chatAdapter.stopPlayVoice();
        }

        finish();
    }

    //听筒播放模式
    private void setInCallBySdk() {
        if (mAudioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mAudioManager.getMode() != AudioManager.MODE_IN_COMMUNICATION) {
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
            try {
                Class clazz = Class.forName("android.media.AudioSystem");
                Method m = clazz.getMethod("setForceUse", new Class[]{int.class, int.class});
                m.invoke(null, 1, 1);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            if (mAudioManager.getMode() != AudioManager.MODE_IN_CALL) {
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            }
        }
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(false);
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    //扬声器播放模式
    private void setModeNormal() {
        if (mAudioManager == null) {
            return;
        }
        mAudioManager.setSpeakerphoneOn(true);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);

        if (!mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(true);

            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    private void unRegistListener() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mEventListener);
        }
    }

    protected abstract void initData();

    protected abstract void initTopBar();

    protected abstract void initListAdapter();

    protected abstract void sendMessageIfNotNull();

    protected abstract void sendVoiceMessage(String content);

    protected abstract void sendPicMessage(String content);

    protected abstract void sendEmail();

    protected abstract void sendEnterSession();

    protected abstract void sendMeeting();

    protected abstract void sendVideoMessage(String content);

    protected abstract void sendFile(String content, String msgType);

    protected abstract void sendLocation(String content);

    protected abstract void showAtTips(MessageBean msgBean, String atMessage);

    protected abstract String getGetMessageListURL(MessageBean bean);

    protected abstract List<MessageBean> getMessageListFromLocalDB(MessageBean messageBean);

    @Override
    public void onRefresh() {
        if (isRefresh)
            return;
        if (!message_pool.isEmpty()) {
            List<MessageBean> messageBeans = getMessageListFromLocalDB(message_pool.get(0));
            if ((null != messageBeans && messageBeans.size() == 20) || (null != messageBeans && messageBeans.size() < 20 && messageBeans.size() > 0 && !ActivityUtils.hasNetWorkConection(this))) {
                List<MessageBean> records = new ArrayList<>();
                //判断本地是否存在聊天记录
                //如果存在，则补全MessageBean的信息，并将其显示在当前界面
                records.addAll(completeMessageBean(messageBeans));
                refreshList(records);
                refreshUnReadCount();
            } else if (ActivityUtils.hasNetWorkConection(this)) {
                getHistoryMsgListFromAPI();
            } else {
                Toast.makeText(this, "没有更早的记录了", Toast.LENGTH_SHORT).show();
                mMsgListView.stopRefresh();
                mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
            }
        } else {
            Toast.makeText(this, "没有更早的记录了", Toast.LENGTH_SHORT).show();
            mMsgListView.stopRefresh();
            mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
        }
    }

    private void getHistoryMsgListFromAPI() {
        int index = 0;
        //循环获取timestamp不为空的记录。
        while (index < message_pool.size()) {
            MessageBean bean = message_pool.get(index++);

            if (StringUtils.notEmpty(bean.getTimestamp())) {
                getMessageList(bean);
                return;
            }

            if (index == message_pool.size()) {
                mMsgListView.stopRefresh();
                mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
            }
        }
    }

    private void getMessageList(MessageBean bean) {
        HttpManager.getJsonWithToken(getGetMessageListURL(bean), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                mMsgListView.stopRefresh();
                mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    if (StringUtils.notEmpty(new JSONObject(response).get("objValue"))) {
                        try {
                            Map<String, Object> responseMap = JSONConvert.json2MessageBean(new JSONObject(response).getString("objValue"), mContext);
                            IMDBFactory.getInstance(ChatBaseActivity.this).getRecordsManager().insertRecords((List<MessageBean>) responseMap.get("messageBean"), null);
                            refreshList((ArrayList<MessageBean>) responseMap.get("messageBean"));
                            refreshUnReadCount();
                        } catch (Exception e) {
                            e.printStackTrace();
                            mMsgListView.stopRefresh();
                            mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
                        }
                    }
                }
            }
        });
    }

    public class EnterSessionCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e) {

        }

        @Override
        public void onResponse(String response) throws JSONException {
            if (StringUtils.notEmpty(response) && StringUtils.notEmpty(new JSONObject(response).get("objValue"))) {
                try {
                    final Map<String, Object> responsemap = JSONConvert.json2MessageBean(new JSONObject(response).getString("objValue"), mContext);
                    final ArrayList<MessageBean> beans = (ArrayList<MessageBean>) responsemap.get("messageBean");

                    //保存聊天消息到db中
                    RecordsManager recordsManager = IMDBFactory.getInstance(mContext).getRecordsManager();
                    recordsManager.insertRecords(beans, new RecordsManager.RecordsCallback() {
                        @Override
                        public void sendBroadcast() {
                            //向页面发送广播，通知页面刷新数据
                            Intent intent = new Intent(CommConstants.ACTION_SESSION_MESSAGE_LIST);
                            intent.putExtra("sessionMessageList", beans);
                            intent.putExtra("tipsAtMessage", responsemap.containsKey("atMessageContent") ? (String) responsemap.get("atMessageContent") : "");
                            intent.setPackage(mContext.getPackageName());
                            mContext.sendBroadcast(intent);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLoadMore() {

    }

    protected List<MessageBean> getDBRecords() {
        //首次进入chat界面，先从数据库中取本地聊天记录
        RecordsManager recordsManager = IMDBFactory.getInstance(this).getRecordsManager();
        List<MessageBean> dbRecords = null;

        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            dbRecords = recordsManager.getRecordsByFriendId(sessionObjId, MFSPHelper.getString(CommConstants.EMPADNAME));
        } else if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            dbRecords = recordsManager.getRecordsByRoomId(sessionObjId);
        }
        if (null != dbRecords && dbRecords.size() > 0) {
            //判断本地是否存在聊天记录
            //如果存在，则补全MessageBean的信息，并将其显示在当前界面
            message_pool.addAll(completeMessageBean(dbRecords));
        }
        return message_pool;
    }

    // 与服务器端同步本地聊天记录状态
    private void synDbRecords(JSONArray curUnSendRecords) {

        String url = CommConstants.URL_EOP_IM + "im/logExists";
        HttpManager.postJsonWithToken(url, curUnSendRecords.toString(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response) && StringUtils.notEmpty(new JSONObject(response).get("objValue"))) {

                    //更新数据库中聊天记录状态
                    RecordsManager imDao = IMDBFactory.getInstance(mContext).getRecordsManager();

                    //记录需要修改发送状态的聊天记录
                    Map<String, Integer> recordsMap = new HashMap<>();

                    JSONArray messageBeans = new JSONObject(response).getJSONArray("objValue");

                    for (int i = 0; i < messageBeans.length(); i++) {
                        JSONObject messageBean = messageBeans.getJSONObject(i);

                        String timestamp = messageBean.getString("timestamp");
                        if (null != timestamp && !"".equalsIgnoreCase(timestamp)) {
                            //更新数据库中聊天记录状态
                            imDao.updateRecord(timestamp, CommConstants.MSG_SEND_SUCCESS, messageBean.getString("msgId"));
                            recordsMap.put(messageBean.getString("msgId"), CommConstants.MSG_SEND_SUCCESS);
                        } else {
                            //更新数据库中聊天记录状态
                            imDao.updateRecord(timestamp, CommConstants.MSG_SEND_FAIL, messageBean.getString("msgId"));
                            recordsMap.put(messageBean.getString("msgId"), CommConstants.MSG_SEND_FAIL);
                        }
                    }

                    //发送广播，更新当前界面ListView中聊天记录的发送状态
                    Intent intent = new Intent(CommConstants.MSG_UPDATE_SEND_STATUS_ACTION);
                    SerializableObj obj = new SerializableObj();
                    obj.setMap(recordsMap);
                    intent.putExtra("recordsMap", obj);
                    mContext.sendBroadcast(intent);
                }
            }
        });
    }

    @Override
    public void onSendMsgStart(MessageBean obj) {
        // 开始发送消息 刷新界面
        obj.setIsSend(CommConstants.MSG_SEND_PROGRESS);
        message_pool.add(obj);
        chatAdapter.refreshChatData(message_pool);
        mMsgListView.setSelection(message_pool.size() - 1);
        boolean flag = false;
        for (int j = 0; j < IMConstants.contactListDatas.size(); j++) {
            MessageBean bean = IMConstants.contactListDatas.get(j);
            if (isCurrentSession(bean)) {
                IMConstants.contactListDatas.remove(j);
                IMConstants.contactListDatas.add(0, obj);
                flag = true;
                break;
            }
        }
        if (!flag) {
            IMConstants.contactListDatas.add(0, obj);
        }
        //保存聊天记录到本地数据库
        recordsManager.insertRecord(obj, RecordsManager.MESSAGE_TYPE_SEND, new RecordsManager.RecordsCallback() {
            @Override
            public void sendBroadcast() {

            }
        });
    }

    @Override
    public void onSendMsgProcess(int fileSize, int uploadSize) {
        LogUtils.v("onSendMsgProcess", fileSize + "---" + uploadSize);
    }

    @Override
    public void onSendMsgDone(int responseCode, String message,
                              MessageBean messageDataObj) {

        //消息发送完,将本次@XX置为空
        if (messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_TEXT)) {
            IMConstants.atMembers.clear();
        }

        if (responseCode == CommConstants.MSG_SEND_SUCCESS && !"".equals(message)) {
            if (!messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_FILE_1)
                    && !messageDataObj.getMtype().equals(CommConstants.MSG_TYPE_FILE_2)) {
                File file = new File(message);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        Message msg = Message.obtain();
        msg.what = responseCode;
        msg.obj = messageDataObj;
        handler.sendMessage(msg);
    }

    protected void refreshUnReadCount() {
        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
            MessageBean bean = IMConstants.contactListDatas.get(i);
            if (bean.getCtype() == ctype) {
                if ((bean.getCtype() == CommConstants.CHAT_TYPE_GROUP && sessionObjId.equalsIgnoreCase(bean.getRoomId())) || (bean.getCtype() == CommConstants.CHAT_TYPE_SINGLE && bean.getFriendId().equalsIgnoreCase(sessionObjId))) {
                    bean.setIsread(CommConstants.MSG_READ);
                    bean.setUnReadCount(0);
                }
            }
        }
    }

    private boolean isCurrentSession(MessageBean msgBean) {

        if (msgBean.getCtype() == ctype) {
            if (msgBean.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                return msgBean.getRoomId().equalsIgnoreCase(sessionObjId);
            } else if (msgBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE) {
                return msgBean.getFriendId().equalsIgnoreCase(sessionObjId);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //刷新列表
    protected void refreshList(List<MessageBean> pullList) {
        if (pullList != null && !pullList.isEmpty()) {
            message_pool.addAll(0, pullList);
            if (!isRefresh) {
                for (int i = 0; i < IMConstants.failedMsgList.size(); i++) {
                    if (isCurrentSession(IMConstants.failedMsgList.get(i))) {
                        message_pool.add(IMConstants.failedMsgList.get(i));
                    }
                }
            }
            isRefresh = false;
            mMsgListView.setEnabled(false);
            chatAdapter.refreshChatData(message_pool);
            mMsgListView.setSelection(pullList.size());
            mMsgListView.stopRefresh();
            mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mMsgListView.setEnabled(true);
                }
            }, 500);
        } else {
            ToastUtils.showToast(this, getResources().getString(R.string.pull_to_refresh_footer_nomore));
            isRefresh = false;
            mMsgListView.stopRefresh();
            mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
        }
    }

    protected List<MessageBean> completeMessageBean(List<MessageBean> dbRecords) {

        //判断本地是否存在聊天记录
        if (null != dbRecords && dbRecords.size() > 0) {

            //如果存在，则补全MessageBean的信息，并将其显示在当前界面
            List<MessageBean> records = new ArrayList<>();

            //清空unSend数据
            curUnSendRecords = new JSONArray();

            for (int i = 0; i < dbRecords.size(); i++) {
                try {
                    MessageBean record = JSONConvert.getMessageBean(dbRecords.get(i));

                    record.setRoomId(sessionObjId);
                    Group group = IMConstants.groupsMap.get(sessionObjId);
                    record.setSubject(null != group ? group.getDisplayName() : "");

                    if (hasNewMes && record.isATMessage()) {
                        showAtTips(record, "");
                        hasNewMes = false;
                    }

                    //记录状态为发送中的聊天记录
                    if (CommConstants.MSG_SEND_PROGRESS == record.getIsSend()) {
                        /**
                         * 当消息发送中，被中断，再进入聊天页面就需要重新请求查看消息是否发送成功，来更新状态
                         * 当发送文件时，首先发送给cms,还没上传成功就退出页面，此时就不需要去检查状态了。
                         */
                        JSONObject msgObj = new JSONObject();
                        msgObj.put("msgId", record.getMsgId());
                        msgObj.put("chatType", record.getCtype());
                        curUnSendRecords.put(msgObj);
                    }
                    records.add(i, record);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (curUnSendRecords.length() > 0) {
                // 与服务器端同步本地聊天记录状态
                synDbRecords(curUnSendRecords);
            }

            return records;
        }
        return null;
    }

    private void initView() {
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mWindowNanagerParams = getWindow().getAttributes();

        mTopTitle = (TextView) findViewById(R.id.common_top_title);
        mTopLeftImage = (ImageView) findViewById(R.id.common_top_img_left);
        mTopRightImage = (ImageView) findViewById(R.id.common_top_img_right);
        mMsgListView = (XListView) findViewById(R.id.msg_listView);

        tv_tips = (TextView) findViewById(R.id.tv_tips);

        mVoiceBtn = (Button) findViewById(R.id.chat_voice);
        mFaceSwitchBtn = (Button) findViewById(R.id.chat_face);
        mAddMoreBtn = (Button) findViewById(R.id.chat_addmore);
        mSendMsgBtn = (Button) findViewById(R.id.chat_send);
        mChatEditText = (CurEditText) findViewById(R.id.chat_inputtext);
        mVoiceSpeakBtn = (Button) findViewById(R.id.chat_voice_speak_btn);

        chat_voice_popup = findViewById(R.id.chat_voice_popup);
        voice_rcd_hint_rcding = (LinearLayout) findViewById(R.id.voice_rcd_hint_rcding);
        voice_rcd_hint_cancle = (LinearLayout) findViewById(R.id.voice_rcd_hint_cancle);
        voice_rcd_hint_tooshort = (LinearLayout) findViewById(R.id.voice_rcd_hint_tooshort);
        volume = (ImageView) this.findViewById(R.id.volume);
        scImage = (ImageView) findViewById(R.id.sc_img1);

        mFaceRoot = (LinearLayout) findViewById(R.id.face_ll);

        mMenuRoot = (LinearLayout) findViewById(R.id.menu_ll);
        mMenuViewPager = (ViewPager) findViewById(R.id.menu_pager);

        // 触摸ListView隐藏表情和输入法
        mMsgListView.setOnTouchListener(this);
        mMsgListView.setPullLoadEnable(false);
        mMsgListView.setXListViewListener(this);

        mChatEditText.setOnTouchListener(this);

        mVoiceBtn.setOnClickListener(this);
        mFaceSwitchBtn.setOnClickListener(this);
        mAddMoreBtn.setOnClickListener(this);
        mSendMsgBtn.setOnClickListener(this);
        mVoiceSpeakBtn.setOnTouchListener(this);

        mTopLeftImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mChatEditText.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mWindowNanagerParams.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                            || mIsFaceShow) {
                        mFaceRoot.setVisibility(View.GONE);
                        mMenuRoot.setVisibility(View.GONE);
                        mIsFaceShow = false;
                        mIsMenuShow = false;
                        return true;
                    }
                }
                return false;
            }
        });

        mChatEditText.addTextChangedListener(new TextChangedListener(this));
    }

    protected void isShowAtTips(boolean isShowTips, final String tipsText) {
        if (isShowTips && tv_tips.getVisibility() != View.VISIBLE) {
            tv_tips.setVisibility(View.VISIBLE);
            tv_tips.setText(tipsText);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_tips.setVisibility(View.GONE);
                }
            }, 3 * 1000);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.chat_voice) {
            if (mIsFaceShow) {
                // 隐藏表情
                mIsFaceShow = false;
                mFaceRoot.setVisibility(View.GONE);
                mFaceSwitchBtn.setBackgroundResource(R.drawable.chat_emotion_selector);
            }
            if (mIsMenuShow) {
                // 隐藏菜单
                mIsMenuShow = false;
                mMenuRoot.setVisibility(View.GONE);
                mAddMoreBtn.setBackgroundResource(R.drawable.chat_addmore_selector);
            }
            if (!mIsVoiceShow) {
                // 隐藏键盘，显示语音输入
                mVoiceBtn.setBackgroundResource(R.drawable.chat_keyboard_selector);
                mChatEditText.setVisibility(View.GONE);
                mVoiceSpeakBtn.setVisibility(View.VISIBLE);
                mInputMethodManager.hideSoftInputFromWindow(
                        mChatEditText.getWindowToken(), 0);
                mIsVoiceShow = true;

            } else {
                mVoiceBtn.setBackgroundResource(R.drawable.chat_voice_selector);
                mChatEditText.setVisibility(View.VISIBLE);
                mVoiceSpeakBtn.setVisibility(View.GONE);
                mChatEditText.requestFocus();
                mInputMethodManager.showSoftInput(mChatEditText, 0);
                mIsVoiceShow = false;
            }
            mMsgListView.setSelection(mMsgListView.getBottom());
        } else if (id == R.id.chat_face) {
            if (mIsVoiceShow) {
                mIsVoiceShow = false;
                mVoiceBtn.setBackgroundResource(R.drawable.chat_voice_selector);
                mChatEditText.setVisibility(View.VISIBLE);
                mVoiceSpeakBtn.setVisibility(View.GONE);
            }
            if (mIsMenuShow) {
                // 隐藏菜单
                mIsMenuShow = false;
                mMenuRoot.setVisibility(View.GONE);
                mAddMoreBtn
                        .setBackgroundResource(R.drawable.chat_addmore_selector);
            }
            if (!mIsFaceShow) {
                handler.postDelayed(new Runnable() {
                    // 解决此时界面会变形，有闪烁的现象
                    @Override
                    public void run() {
                        mFaceSwitchBtn
                                .setBackgroundResource(R.drawable.chat_keyboard_selector);
                        mFaceRoot.setVisibility(View.VISIBLE);
                        mIsFaceShow = true;
                        mChatEditText.requestFocus();
                    }
                }, 80);
                mInputMethodManager.hideSoftInputFromWindow(
                        mChatEditText.getWindowToken(), 0);
            } else {
                mFaceRoot.setVisibility(View.GONE);
                mChatEditText.requestFocus();
                mInputMethodManager.showSoftInput(mChatEditText, 0);
                mFaceSwitchBtn
                        .setBackgroundResource(R.drawable.chat_emotion_selector);
                mIsFaceShow = false;
            }
            mMsgListView.setSelection(mMsgListView.getBottom());
        } else if (id == R.id.chat_addmore) {
            if (mIsVoiceShow) {
                mIsVoiceShow = false;
                mVoiceBtn.setBackgroundResource(R.drawable.chat_voice_selector);
                mChatEditText.setVisibility(View.VISIBLE);
                mVoiceSpeakBtn.setVisibility(View.GONE);
            }
            if (mIsFaceShow) {
                // 隐藏表情
                mIsFaceShow = false;
                mFaceRoot.setVisibility(View.GONE);
                mFaceSwitchBtn
                        .setBackgroundResource(R.drawable.chat_emotion_selector);
            }
            if (!mIsMenuShow) {
                handler.postDelayed(new Runnable() {
                    // 解决此时界面会变形，有闪烁的现象
                    @Override
                    public void run() {
                        mAddMoreBtn
                                .setBackgroundResource(R.drawable.chat_keyboard_selector);
                        mMenuRoot.setVisibility(View.VISIBLE);
                        mIsMenuShow = true;
                        mChatEditText.requestFocus();
                    }
                }, 80);
                mInputMethodManager.hideSoftInputFromWindow(
                        mChatEditText.getWindowToken(), 0);
            } else {
                mMenuRoot.setVisibility(View.GONE);
                mChatEditText.requestFocus();
                mInputMethodManager.showSoftInput(mChatEditText, 0);
                mAddMoreBtn
                        .setBackgroundResource(R.drawable.chat_addmore_selector);
                mIsMenuShow = false;
            }
            mMsgListView.setSelection(mMsgListView.getBottom());
        } else if (id == R.id.chat_send) {
            sendMessageIfNotNull();
        } else {
        }
    }

    private void start(String nameraw, String namemp3) {

        if (_2Mp3util == null) {
            _2Mp3util = new Audio2Mp3Utils(null, nameraw, namemp3);
        }
        boolean result = _2Mp3util.startRecording();
        if (result) {
            handler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    }

    private void stop(boolean cleanMp3) {
        handler.removeCallbacks(mSleepTask);
        handler.removeCallbacks(mPollTask);
        boolean result = _2Mp3util.stopRecordingAndConvertFile();
        if (result) {
            // 获得生成的文件路径
            voiceFileNameRaw = _2Mp3util.getFilePath(Audio2Mp3Utils.RAW);
            voiceFileNameMp3 = _2Mp3util.getFilePath(Audio2Mp3Utils.MP3);
            // 清理掉源文件
            _2Mp3util.cleanFile(Audio2Mp3Utils.RAW);
            if (cleanMp3) {
                _2Mp3util.cleanFile(Audio2Mp3Utils.MP3);
            }
        }
        _2Mp3util.close();
        volume.setImageResource(R.drawable.amp1);
        _2Mp3util = null;
    }

    private static final int POLL_INTERVAL = 300;
    private Runnable mSleepTask = new Runnable() {
        public void run() {
            stop(false);
        }
    };
    private Runnable mPollTask = new Runnable() {
        public void run() {
            int amp = _2Mp3util.getAmplitude();
            updateDisplay(amp);
            handler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

    private void updateDisplay(int signalEMA) {
        if (signalEMA >= 0 && signalEMA < 30) {
            volume.setImageResource(R.drawable.amp1);
        } else if (signalEMA >= 30 && signalEMA < 60) {
            volume.setImageResource(R.drawable.amp2);
        } else if (signalEMA >= 60 && signalEMA < 100) {
            volume.setImageResource(R.drawable.amp3);
        } else if (signalEMA >= 100 && signalEMA < 150) {
            volume.setImageResource(R.drawable.amp4);
        } else if (signalEMA >= 150 && signalEMA < 200) {
            volume.setImageResource(R.drawable.amp5);
        } else if (signalEMA >= 200 && signalEMA < 270) {
            volume.setImageResource(R.drawable.amp6);
        } else if (signalEMA >= 270) {
            volume.setImageResource(R.drawable.amp7);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.msg_listView) {
            mInputMethodManager.hideSoftInputFromWindow(
                    mChatEditText.getWindowToken(), 0);
            mFaceSwitchBtn
                    .setBackgroundResource(R.drawable.chat_emotion_selector);
            mFaceRoot.setVisibility(View.GONE);
            mIsFaceShow = false;
            mAddMoreBtn.setBackgroundResource(R.drawable.chat_addmore_selector);
            mMenuRoot.setVisibility(View.GONE);
            mIsMenuShow = false;
        } else if (id == R.id.chat_inputtext) {
            mInputMethodManager.showSoftInput(mChatEditText, 0);
            mFaceSwitchBtn
                    .setBackgroundResource(R.drawable.chat_emotion_selector);
            mFaceRoot.setVisibility(View.GONE);
            mIsFaceShow = false;
            mAddMoreBtn.setBackgroundResource(R.drawable.chat_addmore_selector);
            mMenuRoot.setVisibility(View.GONE);
            mIsMenuShow = false;
            mMsgListView.setSelection(message_pool.size() - 1);
        }
        // 按下语音录制按钮时返回false执行父类OnTouch
        if (mIsVoiceShow) {
            int[] location = new int[2];
            mVoiceSpeakBtn.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标

            int btn_rc_Y = location[1];
            int btn_rc_X = location[0];
            int[] del_location = new int[2];
            voice_rcd_hint_cancle.getLocationInWindow(del_location);
            int del_Y = del_location[1];
            int del_x = del_location[0];
            if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) {

                if (event.getRawY() > btn_rc_Y && event.getRawX() > btn_rc_X) {
                    // 判断手势按下的位置是否是语音录制按钮的范围内
                    // 如果当前正在播放语音，则停止
                    chatAdapter.stopVoice();

                    mVoiceSpeakBtn.setText("松开 发送");
                    chat_voice_popup.setVisibility(View.VISIBLE);

                    voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
                    voice_rcd_hint_cancle.setVisibility(View.GONE);
                    voice_rcd_hint_tooshort.setVisibility(View.GONE);

                    startVoiceT = System.currentTimeMillis();

                    File cacheDir = FileUtils.getInstance().getCacheFileDir();
                    String str = DateUtils.date2Str(new Date(startVoiceT),
                            "yyyyMMddHHmmss");
                    voiceFileNameRaw = cacheDir.getAbsolutePath()
                            + File.separator + str + ".raw";
                    voiceFileNameMp3 = cacheDir.getAbsolutePath()
                            + File.separator + str + ".mp3";
                    start(voiceFileNameRaw, voiceFileNameMp3);
                    flag = 2;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {// 松开手势时执行录制完成
                mVoiceSpeakBtn.setText(getString(R.string.clicked_speak));
                if (event.getRawY() < btn_rc_Y) {
                    // 在取消发送中 抬起，就取消发送
                    chat_voice_popup.setVisibility(View.GONE);
                    stop(true);
                    flag = 1;
                } else {
                    endVoiceT = System.currentTimeMillis();
                    flag = 1;
                    int time = (int) ((endVoiceT - startVoiceT) / 1000);// 秒
                    if (time < 1) {
                        stop(true);
                        // 说话时间太短
                        voice_rcd_hint_rcding.setVisibility(View.GONE);
                        voice_rcd_hint_cancle.setVisibility(View.GONE);
                        voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
                        handler.postDelayed(new Runnable() {
                            public void run() {

                                voice_rcd_hint_tooshort
                                        .setVisibility(View.GONE);
                                chat_voice_popup.setVisibility(View.GONE);
                            }
                        }, 500);
                        return false;
                    }
                    stop(false);
                    // 发送消息
                    voice_rcd_hint_rcding.setVisibility(View.GONE);
                    chat_voice_popup.setVisibility(View.GONE);
                    JSONObject localVoiceJson = new JSONObject();
                    try {
                        localVoiceJson.put("url", voiceFileNameMp3);
                        localVoiceJson.put("timeLength", time + "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendVoiceMessage(localVoiceJson.toString());
                }
            }
            if (event.getRawY() < btn_rc_Y) {// 手势按下的位置不在语音录制按钮的范围内
                // 显示 取消发送
                Animation mLitteAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.cancel_rc);
                Animation mBigAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.cancel_rc2);
                voice_rcd_hint_rcding.setVisibility(View.GONE);
                voice_rcd_hint_cancle.setVisibility(View.VISIBLE);
                if (event.getRawY() >= del_Y
                        && event.getRawY() <= del_Y
                        + voice_rcd_hint_cancle.getHeight()
                        && event.getRawX() >= del_x
                        && event.getRawX() <= del_x
                        + voice_rcd_hint_cancle.getWidth()) {
                    scImage.startAnimation(mLitteAnimation);
                    scImage.startAnimation(mBigAnimation);
                }
            } else {
                voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
                voice_rcd_hint_cancle.setVisibility(View.GONE);
            }
        }
        return false;
    }

    private void initFacePage() {
        mfaceViewPage = new FaceViewPage(mChatEditText, mFaceRoot);
        mfaceViewPage.initFacePage();
    }

    private void initMenuPage() {
        List<View> lv = new ArrayList<>();
        int count = getMenuGridView(lv);
        FacePageAdeapter adapter = new FacePageAdeapter(lv);
        mMenuViewPager.setAdapter(adapter);
        mMenuViewPager.setCurrentItem(0);
        if (count > 0) {
            CirclePageIndicator indicator = (CirclePageIndicator) mMenuRoot
                    .findViewById(R.id.menu_indicator);
            indicator.setViewPager(mMenuViewPager);
            indicator.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
        mMenuRoot.setVisibility(View.GONE);
    }

    private int getMenuGridView(List<View> lv) {

        boolean meeting = false;
        boolean recordVideo = false;
        try {
            meeting = getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getBoolean(
                    "CHANNEL_MEETING", false);
            recordVideo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getBoolean("CHANNEL_VIDEO", false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        List<Integer> imgIds = new ArrayList<Integer>();
        List<String> titles = new ArrayList<String>();

        imgIds.add(R.drawable.chat_menu_pic);
        titles.add(getResources().getString(R.string.chat_menu_pic));

        imgIds.add(R.drawable.chat_menu_camera);
        titles.add(getResources().getString(R.string.chat_menu_camera));

        if (recordVideo) {
            imgIds.add(R.drawable.chat_menu_vedio);
            titles.add(getString(R.string.chat_menu_vedio));
        }

        imgIds.add(R.drawable.ico_mail);
        titles.add(getResources().getString(R.string.chat_menu_mail));

        imgIds.add(R.drawable.icon_file);
        titles.add(getResources().getString(R.string.chat_menu_file));

        if (meeting) {
            imgIds.add(R.drawable.icon_meeting_msg);
            titles.add(getResources().getString(R.string.chat_menu_metting));
        }

        imgIds.add(R.drawable.icon_map);
        titles.add(getResources().getString(R.string.chat_menu_send_location));

        int count;
        if (titles.size() <= 4) {
            count = 0;
        } else {
            count = titles.size() / 4;
        }

        for (int i = 0; i <= count; i++) {
            int end = (i + 1) * 4;
            if (end > titles.size()) {
                end = titles.size();
            }
            List<Integer> imgIdList = imgIds.subList(i * 4, end);
            final List<String> titlesList = titles.subList(i * 4, end);

            GridView gv = new GridView(this);
            gv.setNumColumns(4);
            gv.setSelector(new ColorDrawable(Color.TRANSPARENT));
            // 屏蔽GridView默认点击效果
            gv.setBackgroundColor(Color.TRANSPARENT);
            gv.setCacheColorHint(Color.TRANSPARENT);
            gv.setHorizontalSpacing(0);
            gv.setVerticalSpacing(0);
            gv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            gv.setGravity(Gravity.CENTER | Gravity.BOTTOM);

            gv.setAdapter(new MenuAdapter(this, imgIdList, titlesList));
            gv.setOnTouchListener(forbidenScroll());
            gv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    String txt = titlesList.get(position);
                    if (txt.equals(getResources().getString(R.string.chat_menu_pic))) {// pic
                        Intent intent = new Intent(Intent.ACTION_PICK, null);
                        intent.setDataAndType(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                "image/*");
                        startActivityForResult(intent, REQUEST_CODE_SEND_PIC_ALBUM);
                    } else if (txt.equals(getResources().getString(R.string.chat_menu_camera))) {// camera
                        currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                        imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);

                        // 跳转相机拍照
                        String sdStatus = Environment.getExternalStorageState();
                        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                            Toast.makeText(ChatBaseActivity.this, getString(R.string.can_not_find_sd), Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(intent2, REQUEST_CODE_SEND_PIC_CAMERA);
                    } else if (txt.equals(getResources().getString(R.string.chat_menu_mail))) {// mail
                        if (CommConstants.CHAT_TYPE_GROUP_ANS == groupType) {
                            ToastUtils.showToast(ChatBaseActivity.this, getString(R.string.can_not_send_email));
                        } else {
                            sendEmail();
                        }
                    } else if (txt.equals(getResources().getString(R.string.chat_menu_file))) {// send file
                        showPop();
                    } else if (txt.equals(getResources().getString(R.string.chat_menu_vedio))) {// recorder video
                        Intent intent3 = new Intent();
                        intent3.setClass(ChatBaseActivity.this, FFmpegRecorderActivity.class);
                        startActivityForResult(intent3, REQUEST_CODE_SEND_VIDEO);
                    } else if (txt.equals(getResources().getString(R.string.chat_menu_metting))) {// metting
                        sendMeeting();
                    } else if (txt.equals(getResources().getString(R.string.chat_menu_send_location))) {// location
                        Intent intent = new Intent();
                        intent.setClass(ChatBaseActivity.this, MapViewActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_LOCATION);
                    }
                }
            });
            lv.add(gv);
        }
        return count;
    }

    // 防止乱pageview乱滚动
    private OnTouchListener forbidenScroll() {
        return new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //返回聊天窗口页面，默认隐藏输入框
        mMenuRoot.setVisibility(View.GONE);
        mIsMenuShow = false;

        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_CODE_VIEWPAGER_PAGE:
                        isRoll = true;
                        mMsgListView.smoothScrollToPosition(data.getIntExtra("BACK_FROM_PAGER",mMsgListView.getCount()-1));
                        break;
                    case REQUEST_CODE_CHAT_DETAIL_PAGE:
                        finish();
                        break;
                    // 如果是直接从相册获取
                    case REQUEST_CODE_SEND_PIC_ALBUM:
                        // 从相册中直接获取文件的真实路径，然后上传
//                        final String picPath = PicUtils.getPicturePath(data,ChatBaseActivity.this);
                        final String picPath = FileUtils.getPath(ChatBaseActivity.this, data.getData());
                        startActivityForResult(new Intent(ChatBaseActivity.this,
                                SelectedImageActivity.class).putExtra(
                                "takePicturePath", picPath), REQUEST_CODE_SELECTED_PIC);
                        break;
                    case REQUEST_CODE_SELECTED_PIC:
                        sendPicMessage(data.getStringExtra("takePicturePath"));
                        break;
                    // 如果是调用相机拍照时
                    case REQUEST_CODE_SEND_PIC_CAMERA:
                        if (imageUri != null) {
                            boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                                    + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                                    + currentTime + ".jpg");
                            new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                            if (copy) {
                                String pathString = CommConstants.SD_CARD_IMPICTURES + currentTime + ".jpg";
                                PicUtils.scanImages(ChatBaseActivity.this,
                                        pathString);
                                String takePicturePath = "";
                                try {
                                    takePicturePath = PicUtils
                                            .getSmallImageFromFileAndRotaing(pathString);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                JSONObject localPicJson2 = new JSONObject();
                                try {
                                    localPicJson2.put("url", takePicturePath);
                                    localPicJson2.put("size",
                                            PicUtils.getPicSizeJson(pathString));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                sendPicMessage(localPicJson2.toString());
                            }
                        }
                        break;
                    case REQUEST_CODE_SEND_VIDEO:
                        //发送视频
                        String videoPath = data.getStringExtra("videoPath");
                        String imagePath = data.getStringExtra("firstImagePath");
                        if (FFmpegRecorderActivity.useVideo && StringUtils.notEmpty(imagePath) && StringUtils.notEmpty(videoPath)) {
                            FFmpegRecorderActivity.useVideo = false;

                            List<String> filePaths = new ArrayList<>();
                            filePaths.add(imagePath);
                            filePaths.add(videoPath);

                            HttpManager.multiFileUpload(CommConstants.URL_UPLOAD, filePaths, "fn", new MyListCallback());
                        }
                        break;
                    case REQUEST_CODE_SEND_FILE:
                        //发送文件
                        Uri uri = data.getData();
                        String filePath = FileUtils.getPath(ChatBaseActivity.this, uri);
                        JSONObject localPicJson2 = new JSONObject();
                        try {
                            File f = new File(filePath);
                            long size = f.length();
                            if (size > 1024 * 1024 * 20) {
                                ToastUtils.showToast(getApplicationContext(), getString(R.string.can_not_more_than_20m));
                                return;
                            }
                            int index = filePath.lastIndexOf(".");
                            if (index >= 0) {
                                /* 取得扩展名 */
                                String fileSuffix = filePath.substring(index,
                                        filePath.length()).toLowerCase();
                                if (fileSuffix.contains("jpg") || fileSuffix.contains("jpeg")
                                        || fileSuffix.contains("png") || fileSuffix.contains("gif")) {
                                    if (size > 1024 * 1024 * 4) {
                                        ToastUtils.showToast(getApplicationContext(), getString(R.string.can_not_more_than_4m));
                                        return;
                                    }
                                }
                            }
                            localPicJson2.put("url", filePath);
                            localPicJson2.put("fileSize", size);
                            localPicJson2.put("name", filePath.substring(filePath.lastIndexOf("/") + 1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendFile(localPicJson2.toString(), CommConstants.MSG_TYPE_FILE_1);
                        break;
                    case REQUEST_CODE_SEND_FILE_2:
                        String uuid = data.getStringExtra("uuid");
                        double size = data.getDoubleExtra("fileSize", 0d);
                        String name = data.getStringExtra("name");
                        String serverPath = data.getStringExtra("serverPath");
                        JSONObject localPicJson3 = new JSONObject();
                        try {
                            localPicJson3.put("uuid", uuid);
                            localPicJson3.put("fileSize", size);
                            localPicJson3.put("name", name);
                            localPicJson3.put("serverPath", serverPath);
                            UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
                            String credential = Base64Utils.getBase64(MFSPHelper.getString(CommConstants.USERID)
                                    + ":" + userInfo.getEmpAdname());
                            localPicJson3.put("token", credential);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendFile(localPicJson3.toString(), CommConstants.MSG_TYPE_FILE_2);
                        break;
                    case REQUEST_CODE_LOCATION://发送位置
                        String filePath1 = data.getStringExtra("filePath");
                        String addStr = data.getStringExtra("addStr");
                        double latitude = data.getDoubleExtra("latitude", 0);
                        double longitude = data.getDoubleExtra("longitude", 0);
                        JSONObject localPicJson4 = new JSONObject();
                        try {
                            localPicJson4.put("url", filePath1);
                            localPicJson4.put("name", addStr);
                            localPicJson4.put("latitude", latitude);
                            localPicJson4.put("longitude", longitude);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendLocation(localPicJson4.toString());
                        break;
                    default:
                        break;
                }
            }else if (resultCode == 1) {
                switch (requestCode) {
                    case REQUEST_CODE_SHARE_FILE://share file
                        if (data != null) {
                            UserInfo userInfo = (UserInfo) data.getSerializableExtra("atUserInfos");
                            if (null != userInfo) {
                                this.finish();
                                Intent intent = new Intent(ChatBaseActivity.this, ChatActivity.class);
                                intent.putExtra("userInfo", userInfo);
                                intent.putExtra("messageBean", message);
                                startActivity(intent);
                            }
                        }
                        break;
                    case IMConstants.REQUEST_CODE_RESEND_MES:
                        if (data != null) {
                            UserInfo userInfo = (UserInfo) data.getSerializableExtra("atUserInfos");
                            if (null != userInfo) {
                                this.finish();
                                Intent intent = new Intent(ChatBaseActivity.this, ChatActivity.class);
                                intent.putExtra("userInfo", userInfo);
                                intent.putExtra("messageBean", message);
                                startActivity(intent);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyListCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e) {

        }

        @Override
        public void onResponse(String response) throws JSONException {
            if (StringUtils.notEmpty(response)) {

                JSONArray obj = new JSONObject(response).getJSONObject("response").getJSONArray("message");

                JSONObject contentObj = new JSONObject();
                for (int i = 0; i < obj.length(); i++) {
                    //获取到服务器端返回的mp4文件地址
                    if (VIDEO_FILE_EXTENSION.equalsIgnoreCase(obj.getJSONObject(i).getString("suffix"))) {
                        contentObj.put("url", obj.getJSONObject(i).getString("uname"));
                    } else {
                        contentObj.put("imageUrl", obj.getJSONObject(i).getString("uname"));
                    }
                }
                //发送video msg
                sendVideoMessage(contentObj.toString());
            }
        }
    }

    @Override
    public void onAvatarClickListener(MessageBean message, int type) {
        if (!CommConstants.GROUP_ADMIN.equalsIgnoreCase(message.getFriendId())
                && !message.isFromWechatUser()) {
            // 跳转详情
            if (type == CommConstants.MSG_RECEIVE) {
                // 跳转他人详情
                Intent intent = new Intent();
                intent.putExtra("userInfo", message.getUserInfo());
                ((BaseApplication) this.getApplication()).getUIController().onOwnHeadClickListener(this, intent, 0);
            } else {
                // 跳转自己
                String userid = MFSPHelper.getString(CommConstants.USERID);
                UserDao dao = UserDao.getInstance(this);
                UserInfo me = dao.getUserInfoById(userid);

                Intent intent = new Intent();
                intent.putExtra("userInfo", me);
                ((BaseApplication) this.getApplication()).getUIController().onOwnHeadClickListener(this, intent, 0);
            }
        }
    }

    @Override
    public void onClickedListener(String curText) {
        if (curText.length() > 0) {
            mSendMsgBtn.setVisibility(View.VISIBLE);
            mSendMsgBtn.setEnabled(true);
            mAddMoreBtn.setVisibility(View.GONE);
        } else {
            mSendMsgBtn.setEnabled(false);
            mSendMsgBtn.setVisibility(View.INVISIBLE);
            mAddMoreBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showPop() {
        // 加载popupWindow的布局文件
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.layout_file_chose, null);


        GridView gv = (GridView) contentView.findViewById(R.id.gv_file);

        List<Integer> imgIds = new ArrayList<Integer>();
        List<String> titles = new ArrayList<String>();
        imgIds.add(R.drawable.icon_mobile_file);
        imgIds.add(R.drawable.icon_wendang);
        titles.add(getResources().getString(R.string.chat_menu_moblie_file));
        titles.add(getResources().getString(R.string.chat_menu_document));

        gv.setAdapter(new MenuAdapter(this, imgIds, titles));
        gv.setOnTouchListener(forbidenScroll());

        final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true);
        // 为弹出框设定自定义的布局
        popupWindow.setContentView(contentView);
        // 设置点击其他地方 popupWindow消失
        popupWindow.setOutsideTouchable(true);
    /*
     * 必须设置背景 响应返回键必须的语句。设置 BackgroundDrawable 并不会改变你在配置文件中设置的背景颜色或图像 ，未知原因
     */
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(mMsgListView, Gravity.CENTER, 0, 0);

        // 点击自身popupWindow消失
        contentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        gv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://本地文件
                        // 打开系统文件浏览功能
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, REQUEST_CODE_SEND_FILE);
                        break;
                    case 1://文档管理文件
                        Intent intent2 = new Intent(ChatBaseActivity.this, SkyDriveActivity.class);
                        intent2.putExtra("chat", "chat");
                        startActivityForResult(intent2, REQUEST_CODE_SEND_FILE_2);
                        break;
                    default:
                        break;
                }
                popupWindow.dismiss();
            }
        });
    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // get complete download id
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            String msgId = ChatBaseActivity.downloadMap.get(completeDownloadId);
            if (ChatBaseActivity.msgIdMap.containsKey(msgId)) {
                ChatBaseActivity.msgIdMap.remove(msgId);
            }
            ChatBaseActivity.downloadMap.remove(completeDownloadId);
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setRedPoint(int point) {

    }

    @Override
    public void updateRecordList(Map<String, Integer> recordMap) {
        //刷新listview
        chatAdapter.setRecordMap(recordMap);
        chatAdapter.refreshChatData(message_pool);
    }

    @Override
    public void receiveNewMessage(MessageBean messageBean) {
        if ((messageBean.getCtype() == CommConstants.CHAT_TYPE_GROUP && sessionObjId.equalsIgnoreCase(messageBean.getRoomId())) || (messageBean.getCtype() == CommConstants.CHAT_TYPE_SINGLE && messageBean.getFriendId().equalsIgnoreCase(sessionObjId))) {
            messageBean.setIsread(CommConstants.MSG_READ);
            message_pool.add(messageBean);
            chatAdapter.refreshChatData(message_pool);
            mMsgListView.setSelection(message_pool.size() - 1);

            showAtTips(messageBean, "");

            refreshUnReadCount();
            IMDBFactory.getInstance(this).getRecordsManager().updateMsgInsertFlag(messageBean.getMsgId(), 1);
        }
    }

    @Override
    public void receiveSessionList(List<MessageBean> messageBeans, String atMessage) {
        showAtTips(null, atMessage);
        //messageBeans 不为空，说明有新消息返回
        if (!messageBeans.isEmpty()) {
            //警告：又一个坑
            //如果enterSession时，增加参数endTime，则去掉message_pool.clear();这一句
            message_pool.clear();
            message_pool = getDBRecords();
            mMsgListView.setEnabled(false);
            chatAdapter.refreshChatData(message_pool);
            if(!isRoll){
                mMsgListView.setSelection(message_pool.size() - 1);
            }
            mMsgListView.stopRefresh();
            mMsgListView.setRefreshTime(DateUtils.date2Str(new Date()));
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mMsgListView.setEnabled(true);
                }
            }, 500);
        }
    }

}
