package com.movit.platform.im.module.single.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.cloud.utils.FileType;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.PopupUtils;
import com.movit.platform.framework.utils.ScreenUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movit.platform.im.R;
import com.movit.platform.im.base.ChatBaseActivity;
import com.movit.platform.im.base.ChatClickListener;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.location.MapViewLookActivity;
import com.movit.platform.im.utils.DownloadFiles;
import com.movit.platform.im.widget.CircleImageView;
import com.movit.platform.im.widget.popuplist.PopupList;
import com.movit.platform.im.widget.popuplist.PopupListAdapter;
import com.qd.recorder.VideoLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Request;

public class ChatAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    List<MessageBean> datas;
    protected MediaPlayer mMediaPlayer;
    XListView listView;
    private final int ATTACHMENT_DOWNLOAD_END = 0;
    private final int ADJUST_LAYOUT_FOR_ATTACHMENT = 1;
    public static final int ERROR_DOWNLOAD_ATTACHMENT = 2;
    private final int SHOW_MY_PROGRESS = 3;
    private final int CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT = 4;
    private final int SHOW_DIALOG_BEFORE_DOWNLOAD = 5;
    private final int SHOW_DIALOG_BEFORE_APKLOAD = 6;
    private String fileLink;
    ProgressDialog progressDialog;
    private ProgressBar pb = null;
    String isVoicing = "";
    int voicingType = -1;

    ChatClickListener chatClickListener;
    private ShareFileListener shareFileListener = null;

    private Map<String, Integer> recordMap;

    public void setRecordMap(Map<String, Integer> recordMap) {
        this.recordMap = recordMap;
    }

    AQuery aq;
    float width;
    Handler mainHandler;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int type = msg.arg1;
                    int count = msg.arg2;
                    String time = (String) msg.obj;
                    ImageView voice = (ImageView) listView.findViewWithTag(time);
                    if (voice == null) {
                        return;
                    }
                    if (type == CommConstants.MSG_SEND) {
                        if (count == 1) {
                            voice.setImageResource(R.drawable.chatto_voice_playing_f1);
                        } else if (count == 2) {
                            voice.setImageResource(R.drawable.chatto_voice_playing_f2);
                        } else if (count == 0) {
                            voice.setImageResource(R.drawable.chatto_voice_playing_f3);
                        }
                    } else {
                        if (count == 1) {
                            voice.setImageResource(R.drawable.chatfrom_voice_playing_f1);
                        } else if (count == 2) {
                            voice.setImageResource(R.drawable.chatfrom_voice_playing_f2);
                        } else if (count == 0) {
                            voice.setImageResource(R.drawable.chatfrom_voice_playing_f3);
                        }
                    }
                    break;

                default:
                    break;
            }
        }

    };
    Timer timer;
    private int groupType;

    public ChatAdapter(Context mContext, List<MessageBean> mDatas,
                       XListView mXListView, Handler mainHandler, ChatClickListener chatClickListener, int groupType) {
        super();
        this.groupType = groupType;
        this.chatClickListener = chatClickListener;
        this.mContext = mContext;
        this.mInflater = LayoutInflater.from(mContext);
        this.datas = mDatas;
        this.listView = mXListView;
        pb = new ProgressBar(mContext);
        pb.setVisibility(View.GONE);

        Display display = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;// 得到宽度
        aq = new AQuery(mContext);
        this.mainHandler = mainHandler;

    }

    public void refreshChatData(List<MessageBean> datas) {
        this.datas = datas;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        try {
            return datas.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            ((Activity) mContext).finish();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void setAvatar(MessageBean message, final ImageView iv_avatar, AQuery aQuery) {
        int picId = R.drawable.avatar_male;
        if (!CommConstants.GROUP_ADMIN.equals(message.getFriendId())
                && !message.isFromWechatUser()) {
            if (mContext.getString(R.string.boy).equals(message.getUserInfo().getGender())) {
                picId = R.drawable.avatar_male;
            } else if (mContext.getString(R.string.girl).equals(message.getUserInfo().getGender())) {
                picId = R.drawable.avatar_female;
            }
        }
        if (CommConstants.GROUP_ADMIN.equals(message.getFriendId())) {
            aQuery.id(iv_avatar).image(R.drawable.group_admin);
        } else if (message.isFromWechatUser()) {
            aQuery.id(iv_avatar).image(R.drawable.wechat_icon);
        } else {
            String avatar = message.getUserInfo().getAvatar();

            final Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId, 10);
            if (StringUtils.notEmpty(avatar)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                //为了适配其他项目
                if (avatar.startsWith("http")) {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(avatar).memCache(true)
                            .fileCache(true).targetWidth(128);
                } else {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(CommConstants.URL_DOWN + avatar).memCache(true)
                            .fileCache(true).targetWidth(128);
                }

//                aQuery.id(iv_avatar).image(callback);
                aQuery.id(iv_avatar).image(CommConstants.URL_DOWN + avatar,true,true,128,picId,
                        new BitmapAjaxCallback(){
                            @Override
                            protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                                super.callback(url, iv, bm, status);
                                if( null == bm || bm.getByteCount() <= 0){
                                    iv_avatar.setImageBitmap(bitmap);
                                }
                            }
                        });
            } else {
//                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext,
//                        picId, 10);
                iv_avatar.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public View getView(final int postion, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        final MessageBean message = (MessageBean) getItem(postion);

        if (message == null) {
            return convertView;
        }
        final int type = message.getRsflag();
        int key = Integer.parseInt(postion + "" + type);
        /**
         * 0:接受 1：发送
         */
        if (convertView == null
                || convertView.getTag(R.drawable.icon + key) == null) {
            holder = new ViewHolder();
            if (type == CommConstants.MSG_SEND) {
                convertView = mInflater.inflate(R.layout.im_item_chat_right, null);
            } else {
                convertView = mInflater.inflate(R.layout.im_item_chat_left, null);
                holder.groupUserName = (TextView) convertView
                        .findViewById(R.id.chat_group_user_name);
            }
            holder.content = (TextView) convertView
                    .findViewById(R.id.chat_textView);
            holder.time = (TextView) convertView
                    .findViewById(R.id.chat_datetime);
            holder.avatar = (ImageView) convertView
                    .findViewById(R.id.chat_avatar);
            holder.picture = (ImageView) convertView
                    .findViewById(R.id.chat_picture);
            holder.progressBar = (ProgressBar) convertView
                    .findViewById(R.id.chat_progressBar);
            holder.voice = (ImageView) convertView
                    .findViewById(R.id.chat_voice_img);
            holder.voiceTimes = (TextView) convertView
                    .findViewById(R.id.chat_voice_times);
            holder.statusWarning = (ImageView) convertView
                    .findViewById(R.id.chat_status_warning);
            holder.relativeLayout = (RelativeLayout) convertView
                    .findViewById(R.id.relativeLayout);
            // TODO: 2016/3/3 meeting
            holder.meetingLayout = (LinearLayout) convertView
                    .findViewById(R.id.chat_layout_meeting);
            holder.meetingPic = (ImageView) convertView
                    .findViewById(R.id.chat_meeting_pic);
            holder.meetingName = (TextView) convertView
                    .findViewById(R.id.chat_meeting_name);
            holder.meetingUrl = (TextView) convertView
                    .findViewById(R.id.chat_meeting_url);

            // group-admin left view
            holder.linearLayoutAdmin = (LinearLayout) convertView
                    .findViewById(R.id.linearLayout_admin);
            holder.chatAdminImg = (ImageView) convertView
                    .findViewById(R.id.chat_admin_img);
            holder.chatAdminTextView = (TextView) convertView
                    .findViewById(R.id.chat_admin_textView);

            //add By Reed.Qiu
            holder.video_layout = (VideoLayout) convertView.findViewById(R.id.video_layout);

            //add By Zoro.Qian
            holder.chatFileLayout = (RelativeLayout) convertView.findViewById(R.id.chat_layout_file);
            holder.chatFileAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            holder.chatFileName = (TextView) convertView.findViewById(R.id.tv_file_name);
            holder.chatFileSize = (TextView) convertView.findViewById(R.id.tv_file_size);
            holder.chatFileStatus = (TextView) convertView.findViewById(R.id.tv_file_status);

            //add By Zoro.Qian
            holder.chatLocationLayout = (RelativeLayout) convertView.findViewById(R.id.rl_location);
            holder.chatLocationAvatar = (CircleImageView) convertView.findViewById(R.id.iv_location_avatar);
            holder.chatLocationAddStr = (TextView) convertView.findViewById(R.id.tv_location_add_str);


            convertView.setTag(R.drawable.icon + key, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.drawable.icon + key);
        }
        if (holder.linearLayoutAdmin != null) {
            holder.linearLayoutAdmin.setVisibility(View.GONE);
        }
        holder.relativeLayout.setVisibility(View.VISIBLE);

        holder.picture.setVisibility(View.GONE);
        holder.content.setText(" ");

        holder.voice.setVisibility(View.GONE);
        holder.voiceTimes.setVisibility(View.GONE);

        holder.meetingLayout.setVisibility(View.GONE);

        holder.video_layout.setVisibility(View.GONE);

        holder.chatFileLayout.setVisibility(View.GONE);

        holder.chatLocationLayout.setVisibility(View.GONE);

        final AQuery aQuery = aq.recycle(convertView);
        if (postion != 0) {
            long data = (DateUtils.str2Date(message.getFormateTime())).getTime();
            long data2 = (DateUtils.str2Date(datas.get(postion - 1)
                    .getFormateTime())).getTime();
            if (data - data2 > 60 * 1000) {
                holder.time.setVisibility(View.VISIBLE);
            } else {
                holder.time.setVisibility(View.GONE);
            }
        } else {
            holder.time.setVisibility(View.VISIBLE);
        }

        holder.time.setText(DateUtils.getFormateDateWithTime(message
                .getFormateTime()));

        holder.progressBar.setVisibility(View.GONE);
        holder.statusWarning.setVisibility(View.GONE);
        holder.voice.setTag(message.getMsgId());

        if (type == CommConstants.MSG_RECEIVE) {
            if (message.getCtype() == CommConstants.CHAT_TYPE_GROUP) {
                holder.groupUserName.setVisibility(View.VISIBLE);
                if (CommConstants.GROUP_ADMIN.equalsIgnoreCase(message.getFriendId())) {
                    holder.groupUserName.setText(message.getFriendId());
                    setAvatar(message, holder.avatar, aQuery);
                } else if (message.isFromWechatUser()) {
                    holder.groupUserName.setText(message.getFriendId());
                    setAvatar(message, holder.avatar, aQuery);
                } else {
                    switch (groupType) {
                        case CommConstants.CHAT_TYPE_GROUP_ANS:
                            //匿名群组
                            holder.groupUserName.setText(IMConstants.ansGroupMembers.get(message.getRoomId() + "," + message.getUserInfo().getId()));
                            Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, R.drawable.avatar_ans, 10);
                            holder.avatar.setImageBitmap(bitmap);
                            break;
                        case CommConstants.CHAT_TYPE_GROUP_PERSON:
                            //实名群组
                            if (null != message.getUserInfo()) {
                                holder.groupUserName.setText(message.getUserInfo()
                                        .getEmpCname().split("\\.")[0]);
                            }
                            setAvatar(message, holder.avatar, aQuery);
                            break;
                    }
                }
            } else {
                //单聊
                setAvatar(message, holder.avatar, aQuery);
            }

            if (message.getMtype().equals(CommConstants.MSG_TYPE_FILE_1)
                    || message.getMtype().equals(CommConstants.MSG_TYPE_FILE_2)) {
                String json = message.getContent();
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject content = jsonObject.getJSONObject("content");
                    String path;
                    String name;
                    if (message.getMtype().equals(CommConstants.MSG_TYPE_FILE_2)) {
                        String uuid = content.getString("uuid");
                        String fileName = content.getString("name");
                        String fileSuffix = "";
                        int index = fileName.lastIndexOf(".");
                        if (index > 0) {
                            fileSuffix = fileName.substring(index, fileName.length()).toLowerCase();
                        }
                        name = uuid + fileSuffix;
                        path = CommConstants.SD_DOCUMENT + name;
                    } else {
                        name = content.getString("url");
                        path = CommConstants.SD_DATA_FILE + name;
                    }

                    File file = new File(path);
                    if (file.exists()) {
                        holder.chatFileStatus.setText(mContext.getString(R.string.chat_file_download));
                        if (ChatBaseActivity.msgIdMap.containsKey(message.getMsgId())) {
                            ChatBaseActivity.msgIdMap.remove(message.getMsgId());
                        }
                        holder.progressBar.setVisibility(View.GONE);
                    } else {
                        if (ChatBaseActivity.msgIdMap.containsKey(message.getMsgId())) {
                            holder.chatFileStatus.setText(mContext.getString(R.string.chat_file_pop_downloading));
                            holder.progressBar.setVisibility(View.VISIBLE);
                        } else {
                            holder.chatFileStatus.setText(mContext.getString(R.string.chat_file_no_download));
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.chatFileStatus.setText(mContext.getString(R.string.chat_file_no_download));
                }
            }
        } else {

            if (message.getMtype().equals(CommConstants.MSG_TYPE_LOCATION)) {
                holder.relativeLayout.setBackgroundResource(android.R.color.transparent);
            } else {
                holder.relativeLayout.setBackgroundResource(R.drawable.chat_right_selector);
            }

            // 我发送的就用自己的图片
            String uname = MFSPHelper.getString(CommConstants.AVATAR);
            String gender = MFSPHelper.getString(CommConstants.GENDER);
            int picId = R.drawable.avatar_male;
            if (mContext.getString(R.string.boy).equals(gender)) {
                picId = R.drawable.avatar_male;
            } else if (mContext.getString(R.string.girl).equals(gender)) {
                picId = R.drawable.avatar_female;
            }
            String avatarUrl = "";
            if (StringUtils.notEmpty(uname)) {
                avatarUrl = uname;
            }
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();

                //为了适配其他项目
                if (avatarUrl.startsWith("http")) {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(avatarUrl).memCache(true)
                            .fileCache(true).targetWidth(128);
                } else {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                            .fileCache(true).targetWidth(128);
                }

                aQuery.id(holder.avatar).image(callback);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId,
                        10);
                holder.avatar.setImageBitmap(bitmap);
            }

            if (null != recordMap && recordMap.containsKey(message.getMsgId())) {
                //显示服务器端同步过来的聊天记录发送状态
                setFileMessageStatus(recordMap.get(message.getMsgId()), holder);
            } else {
                //设置聊天记录发送状态
                setFileMessageStatus(message.getIsSend(), holder);
            }

        }

        holder.avatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                switch (groupType) {
                    case CommConstants.CHAT_TYPE_GROUP_ANS:
                        //匿名群组,不可以点击头像
                        ToastUtils.showToast(mContext, mContext.getString(R.string.nick_can_not_see_user_detail));
                        break;
                    default:
                        chatClickListener.onAvatarClickListener(message, type);
                        break;
                }
            }
        });

        holder.statusWarning.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mainHandler.obtainMessage(CommConstants.MSG_SEND_RESEND, postion)
                        .sendToTarget();
            }
        });

        if (message.getMtype().equals(CommConstants.MSG_TYPE_TEXT)) {
            String json = message.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                String contentText = "";
                if (jsonObject.has("content")) {
                    JSONObject content = jsonObject.getJSONObject("content");
                    contentText = content.getString("text");
                } else {
                    contentText = jsonObject.getString("text");
                }
                final String text = contentText;
                CharSequence charSeq = StringUtils
                        .convertNormalStringToSpannableString(mContext, text,
                                true, (int) holder.content.getTextSize() + 10);
                if (CommConstants.GROUP_ADMIN.equalsIgnoreCase(message.getFriendId())) {
                    holder.content.setText(text);
                } else {
                    holder.content.setText(charSeq);
                }
                // holder.content.setSpanText(handler, text, runMap);
                holder.content.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (CommConstants.GROUP_ADMIN.equalsIgnoreCase(message.getFriendId())) {
                            String type = FileUtils.getInstance().getMIMEType(
                                    text);
                            if (!StringUtils.empty(type)) {
                                openAttachmentWebView(getUrls(text), "", "");
                            }
                        }
                    }
                });

                //增加复制粘贴功能
                final String tempStr = holder.content.getText().toString();
                holder.content.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        PopupUtils.showPopupWindow(mContext, view, tempStr, new MyPopupClickEvent(message));
                        return false;
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                holder.content.setText("内容不和谐");
            }

        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_LOCATION)) {
            holder.chatLocationLayout.setVisibility(View.VISIBLE);
            LayoutParams para = holder.chatLocationLayout.getLayoutParams();
            int width = (int) (ScreenUtils.getScreenWidth(mContext) * 0.6);
            para.width = width;
            para.height = (int) (width * 0.6);
            holder.chatLocationLayout.setLayoutParams(para);
            String json = message.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                final String uname = content.getString("url");
                final String name = content.getString("name");
                final double latitude = content.getDouble("latitude");
                final double longitude = content.getDouble("longitude");
                holder.chatLocationAddStr.setText(name);

                if (message.getIsSend() == CommConstants.MSG_SEND_SUCCESS) {
                    aQuery.id(holder.chatLocationAvatar).image(
                            CommConstants.URL_DOWN + uname, true, true, 0, 0);
                } else {
                    File file = new File(uname);
                    aQuery.id(holder.chatLocationAvatar).image(file, true, 0, null);

                }

                holder.chatLocationLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, MapViewLookActivity.class);
                        intent.putExtra("addrStr", name);
                        intent.putExtra("latitude", latitude);
                        intent.putExtra("longitude", longitude);
                        mContext.startActivity(intent);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                holder.picture.setVisibility(View.GONE);
                holder.content.setText(mContext.getString(R.string.content_error));
            }
        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_PIC)) {
            holder.picture.setVisibility(View.VISIBLE);
            String json = message.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                final String picUrl = content.getString("url");
                String size = content.getString("size");
                String sizeStr = size.substring(1, size.length() - 1);

                float w = Float.parseFloat(sizeStr.split(",")[0].trim());
                float h = Float.parseFloat(sizeStr.split(",")[1].trim());
                float ratio = h / w;
                LayoutParams para = holder.picture.getLayoutParams();
                Bitmap bitmap;
                if (w > h) { // 横着的
                    para.width = (int) width / 2;
                    para.height = (int) (width / 2 * ratio);
                    bitmap = aQuery
                            .getCachedImage(R.drawable.chat_pic_default_2);
                } else if (w < h) {
                    para.width = (int) (width / 2 / ratio);
                    para.height = (int) (width / 2);
                    bitmap = aQuery
                            .getCachedImage(R.drawable.chat_pic_default_1);
                } else {
                    para.width = (int) (width / 2);
                    para.height = (int) (width / 2);
                    bitmap = aQuery.getCachedImage(R.drawable.chat_pic_default);
                }
                holder.picture.setLayoutParams(para);
                holder.picture.setScaleType(ImageView.ScaleType.FIT_XY);
                final Bitmap bitmap2 = PicUtils.zoomImage(bitmap, para.width,
                        para.height);
                final BitmapAjaxCallback callback = new BitmapAjaxCallback() {

                    @Override
                    protected void callback(String url, ImageView iv,
                                            Bitmap bm, AjaxStatus status) {
                        super.callback(url, iv, bm, status);
                        if (status.getCode() != 200) {
                            iv.setImageBitmap(bitmap2);
                        }
                    }
                };
                callback.animation(AQuery.FADE_IN_NETWORK);
                callback.rotate(true);
                callback.ratio(ratio);
                callback.preset(bitmap2);
                File file = null;
                //服务器端剪切过的小图
                final String midName = picUrl.replace(".", "_m.");

                File midPicFile = aq.getCachedFile(CommConstants.URL_DOWN + midName);
                if (null != midPicFile && midPicFile.exists()) {
                    aQuery.id(holder.picture).image(midPicFile, false, 256, callback);
                } else {
                    aQuery.id(holder.picture).image(
                            CommConstants.URL_DOWN + midName, true, true, 256, 0, callback);
                }
                holder.picture.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ArrayList<String> selectImagesList = new ArrayList<String>();
                        selectImagesList.clear();
                        ArrayList<String> preset = new ArrayList<String>();
                        for (int i = 0; i < datas.size(); i++) {
                            if (datas.get(i).getMtype()
                                    .equals(CommConstants.MSG_TYPE_PIC)) {
                                String json = datas.get(i).getContent();
                                try {
                                    JSONObject jsonObject = new JSONObject(json);
                                    JSONObject content = jsonObject
                                            .getJSONObject("content");
                                    String picUname = content.getString("url");
                                    String picMidName = picUname.replace(".",
                                            "_s.");
                                    selectImagesList.add(picUname);
                                    preset.add(picMidName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Intent intent = new Intent(mContext, ImageViewPagerActivity.class);
                        int[] location = new int[2];
                        v.getLocationOnScreen(location);
                        intent.putExtra("locationX", location[0]);
                        intent.putExtra("locationY", location[1]);
                        intent.putExtra("width", v.getWidth());
                        intent.putExtra("height", v.getHeight());
                        intent.putStringArrayListExtra("selectedImgs", selectImagesList);
                        intent.putStringArrayListExtra("presetImgs", preset);
                        intent.putExtra("postion", selectImagesList.indexOf(picUrl));
                        intent.putExtra("itemPosition", postion);
                        ((Activity) mContext).startActivityForResult(intent,((ChatBaseActivity) mContext).REQUEST_CODE_VIEWPAGER_PAGE);
                        ((Activity) mContext).overridePendingTransition(0, 0);
                    }
                });

                holder.picture.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        PopupUtils.showPopupWindow(mContext, view, null, new MyPopupClickEvent(message));
                        return false;
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                holder.picture.setVisibility(View.GONE);
                holder.content.setText(mContext.getString(R.string.content_error));
            }

        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_AUDIO)) {
            holder.voice.setVisibility(View.VISIBLE);
            if (message.getIsSend() == CommConstants.MSG_SEND_PROGRESS) {
                holder.voiceTimes.setVisibility(View.GONE);
            } else if (message.getIsSend() == CommConstants.MSG_SEND_SUCCESS) {
                holder.voiceTimes.setVisibility(View.VISIBLE);
            } else if (message.getIsSend() == CommConstants.MSG_SEND_FAIL) {
                holder.voiceTimes.setVisibility(View.GONE);
            }

            String json = message.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                String timelength = content.getString("timeLength");
                holder.voiceTimes.setText(timelength + "''");
                int ems = 2;
                if (Integer.parseInt(timelength) >= 10) {
                    ems = 10;
                } else if (Integer.parseInt(timelength) >= 3
                        && Integer.parseInt(timelength) < 10) {
                    ems = Integer.parseInt(timelength);
                }
                holder.content.setEms(ems);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.relativeLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (message.getMtype().equals(CommConstants.MSG_TYPE_TEXT)) {

                    } else if (message.getMtype().equals(
                            CommConstants.MSG_TYPE_AUDIO)) {
                        String json = message.getContent();
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            JSONObject content = jsonObject
                                    .getJSONObject("content");
                            final String uname = content.getString("url");
                            final String encodeUame = URLEncoder.encode(uname,
                                    "utf-8");

                            final String filePath = CommConstants.SD_DATA_AUDIO
                                    + FileUtils.getInstance()
                                    .getFileName(encodeUame);

                            File file = new File(filePath);
                            if (file.exists()) {
                                // 播放语音
                                doVoiceClickEvent(filePath,
                                        message.getMsgId(),
                                        message.getRsflag());
                            } else {
                                // 去下载语音
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        int result = FileUtils.getInstance()
                                                .downfile(handler,
                                                        CommConstants.URL_DOWN
                                                                + uname,
                                                        CommConstants.SD_DATA_AUDIO,
                                                        encodeUame);
                                        if (result == 1) {
                                            // 播放语音
                                            doVoiceClickEvent(filePath,
                                                    message.getMsgId(),
                                                    message.getRsflag());
                                        } else if (result == 0) {
                                            // 播放语音
                                            doVoiceClickEvent(filePath,
                                                    message.getMsgId(),
                                                    message.getRsflag());
                                        } else if (result == -1) {
                                            Log.v("语音下载", "下载错误");
                                        }
                                    }
                                }).start();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_ADMIN)) {
            // group-admin left view
            if (holder.linearLayoutAdmin != null) {
                holder.linearLayoutAdmin.setVisibility(View.VISIBLE);
                holder.relativeLayout.setVisibility(View.GONE);
                holder.chatAdminImg.setImageResource(R.drawable.pdf_icon);
                String json = message.getContent();
                holder.voice.setVisibility(View.VISIBLE);
                holder.voice.setImageResource(R.drawable.icon);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONObject content = jsonObject.getJSONObject("content");
                    // {"content":{"url":"http://172.18.50.78:8080/app/MessageFormat.pdf","name":"环境保护你知道吗？.pdf"},"time":"2014-11-05 14:34:23","mtype":"Z","ctype":1}
                    // {"content":{"text":"你好"},"ctype":1,"time":"2014-11-05 14:34:23","mtype":"T"}
                    final String text = content.getString("name");
                    final String url = content.getString("url");
                    CharSequence charSeq = StringUtils
                            .convertNormalStringToSpannableString(mContext,
                                    text, true,
                                    (int) holder.content.getTextSize() + 10);
                    holder.chatAdminTextView.setText(charSeq);
                    holder.linearLayoutAdmin
                            .setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    String type = FileUtils.getInstance()
                                            .getMIMEType(url);
                                    if (!StringUtils.empty(type)) {
                                        openAttachmentWebView(getUrls(url), "",
                                                "");
                                    }
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.content.setText(mContext.getString(R.string.content_error));
                }
            }
        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_METTING)) {
            holder.meetingLayout.setVisibility(View.VISIBLE);
            String json = message.getContent();
            String url = "";
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                url = content.getString("url");
                holder.meetingUrl.setText(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String meetingUrl = url;
            holder.meetingLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2016/3/22 先判断是否已安装IBM meeting APP
                    boolean flag = ActivityUtils.openThirdApplicationWithPackageName(
                            mContext, "com.ibm.android.sametime.meetings");

                    if (!flag) {

                        showDialog();

                    } else {
                        Uri uri = Uri.parse(meetingUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        mContext.startActivity(intent);
                    }
                }
            });
        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_VIDEO)) {
            holder.video_layout.setVisibility(View.VISIBLE);
            String json = message.getContent();
            int width = ScreenUtils.getScreenWidth(mContext) / 2;
            ViewGroup.LayoutParams params = holder.video_layout.getLayoutParams();
            params.width = width;
            params.height = width;
            holder.video_layout.setLayoutParams(params);
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                final String imageUrl = content.getString("imageUrl");
                String videoUrl = content.getString("url");
                holder.video_layout.setVideoPath(videoUrl);
                holder.video_layout.setVideoPicPath(imageUrl);
            } catch (JSONException e) {
                e.printStackTrace();
                holder.picture.setVisibility(View.GONE);
                holder.content.setText(mContext.getString(R.string.content_error));
            }
        } else if (message.getMtype().equals(CommConstants.MSG_TYPE_FILE_1)
                || message.getMtype().equals(CommConstants.MSG_TYPE_FILE_2)) {
            holder.chatFileLayout.setVisibility(View.VISIBLE);

            holder.chatFileLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPop(v, postion, message);
                    return false;
                }
            });

            String json = message.getContent();
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONObject content = jsonObject.getJSONObject("content");
                String fileName = content.getString("name");
                double size_b = content.getDouble("fileSize");

                String size;
                if (size_b > 1024) {
                    double size_kb = size_b / 1024;
                    if (size_kb > 1024) {
                        double size_m = size_kb / 1024;
                        DecimalFormat df = new DecimalFormat("#.#");
                        double get_double = Double.parseDouble(df.format(size_m));
                        size = get_double + "M";
                    } else {
                        size = (int) Math.rint(size_kb) + "KB";
                    }
                } else {
                    size = (int) Math.rint(size_b) + "B";
                }

                holder.chatFileSize.setText(size);
                holder.chatFileName.setText(fileName);

                FileType.getFileTYpe(holder.chatFileAvatar, fileName);

//                /* 取得扩展名 */
//                String fileSuffix = fileName.substring(fileName.lastIndexOf("."),
//                        fileName.length()).toLowerCase();
//                if(fileSuffix.contains("pdf")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.pdf);
//                }else if(fileSuffix.contains("xls")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.excel);
//                }else if(fileSuffix.contains("ppt")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.ppt);
//                }else if(fileSuffix.contains("mp3")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_mp3);
//                }else if(fileSuffix.contains("mp4")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_mp4);
//                }else if(fileSuffix.contains("jpg")||fileSuffix.contains("jpeg")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_jpg);
//                }else if(fileSuffix.contains("png")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_png);
//                }else if(fileSuffix.contains("gif")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_gif);
//                }else if(fileSuffix.contains("zip")||fileSuffix.contains("rar")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_zip);
//                }else if(fileSuffix.contains("doc")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_word);
//                }else if(fileSuffix.contains("txt")){
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_txt);
//                }else {
//                    holder.chatFileAvatar.setImageResource(R.drawable.icon_null);
//                    //isNull = true;
//                }

            } catch (JSONException e) {
                e.printStackTrace();
                holder.picture.setVisibility(View.GONE);
                holder.content.setText(mContext.getString(R.string.content_error));
            }
        }
        if (null != recordMap && recordMap.containsKey(message.getMsgId())) {
            //显示服务器端同步过来的聊天记录发送状态
//            setMessageStatus(recordMap.get(message.getMsgId()), holder);
            setFailMessageStatus(recordMap.get(message.getMsgId()), holder);
        } else {
            //设置聊天记录发送状态
//            setMessageStatus(message.getIsSend(), holder);
            setFailMessageStatus(message.getIsSend(), holder);
        }

        return convertView;
    }

    private class MyPopupClickEvent implements PopupUtils.PopupClickEvent {

        private MessageBean messageBean;

        public MyPopupClickEvent(MessageBean messageBean) {
            this.messageBean = messageBean;
        }

        @Override
        public void onPopupItemClicked(TextView tv) {
            if (R.id.tv_resend == tv.getId()) {
                ((ChatBaseActivity) mContext).message = messageBean;
                Intent intent = new Intent();
                intent.putExtra("TITLE", "选择联系人");
                intent.putExtra("ACTION", "forward");
                ((BaseApplication) ((Activity) mContext).getApplication()).getUIController().
                        onIMOrgClickListener((Activity) mContext, intent, IMConstants.REQUEST_CODE_RESEND_MES);
            }
        }
    }

    private void showDialog() {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.im_dialog_download_apk, null);// 得到加载view

        final Dialog mDialog = new Dialog(mContext, R.style.ImageloadingDialogStyle);// 创建自定义样式dialog
        mDialog.setContentView(view);// 设置布局
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        final TextView mTextView = (TextView) view.findViewById(R.id.tv_tips);
        final LinearLayout btnLayout = (LinearLayout) view.findViewById(R.id.ll_btn);
        final LinearLayout proLayout = (LinearLayout) view.findViewById(R.id.ll_pro);

        final ProgressBar progressbar = (ProgressBar) view.findViewById(R.id.progressbar);
        final TextView mProView = (TextView) view.findViewById(R.id.tv_pro);

        TextView btnCancel = (TextView) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        TextView btnConfirm = (TextView) view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mTextView.setText("正在下载IBM-Meeting-APP...");
                proLayout.setVisibility(View.VISIBLE);
                btnLayout.setVisibility(View.GONE);
                mDialog.setCanceledOnTouchOutside(false);

                downloadFile(mDialog, progressbar, mProView);
            }
        });

        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mDialog.show();
    }

    private void downloadFile(final Dialog mDialog, final ProgressBar mProgressBar, final TextView mProView) {
        String url = IMConstants.DOWNLOAD_MEETING_APK;
        OkHttpUtils//
                .getWithToken()//
                .url(url)//
                .build()//
                .execute(new FileCallBack(CommConstants.SD_CARD, "IBM_Meeting.apk")//
                {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);
                    }

                    @Override
                    public void inProgress(float progress, long total) {

                        int pro = (int) (100 * progress);
                        mProgressBar.setProgress(pro);
                        mProView.setText(pro + "/100");

                        if (mProgressBar.getProgress() == 100) {
                            mDialog.dismiss();
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(File file) {

                        String command = "chmod " + "777" + " " + file.getAbsolutePath();
                        Runtime runtime = Runtime.getRuntime();
                        try {
                            runtime.exec(command);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "application/vnd.android.package-archive");
                        mContext.startActivity(intent);

                    }
                });
    }

//    private void setMessageStatus(final int status, ViewHolder holder) {

    private void setFileMessageStatus(final int status, ViewHolder holder) {

        switch (status) {
            case CommConstants.MSG_SEND_PROGRESS:
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.chatFileStatus.setText(mContext.getString(R.string.chat_sending));
                break;
            case CommConstants.MSG_SEND_SUCCESS:
                holder.progressBar.setVisibility(View.GONE);
                holder.chatFileStatus.setText(mContext.getString(R.string.chat_send_success));
                break;
            case CommConstants.MSG_SEND_FAIL:
                holder.progressBar.setVisibility(View.GONE);
//              holder.statusWarning.setVisibility(View.VISIBLE);
//              holder.voiceTimes.setVisibility(View.GONE);
                holder.chatFileStatus.setText(mContext.getString(R.string.chat_send_fail));
                break;
            default:
                break;
        }
    }

    private void setFailMessageStatus(final int status, ViewHolder holder) {

        switch (status) {
            case CommConstants.MSG_SEND_FAIL:
                holder.statusWarning.setVisibility(View.VISIBLE);
                holder.voiceTimes.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    public String getUrls(String text) {
        Matcher m = Pattern.compile("(?i)http://[^\u4e00-\u9fa5]+").matcher(
                text);
        while (m.find()) {
            return m.group();
        }
        return "";
    }

    private static class ViewHolder {
        TextView content;
        TextView time;
        ImageView avatar;
        ImageView picture;
        ProgressBar progressBar;
        TextView voiceTimes;
        ImageView voice;
        RelativeLayout relativeLayout;
        ImageView statusWarning;
        TextView groupUserName;

        // TODO: 2016/3/3 meeting
        LinearLayout meetingLayout;
        TextView meetingName;
        TextView meetingUrl;
        ImageView meetingPic;

        // group-admin left view
        LinearLayout linearLayoutAdmin;
        ImageView chatAdminImg;
        TextView chatAdminTextView;

        //add by Reed.Qiu
        VideoLayout video_layout;

        // add by Zoro.qian send file
        RelativeLayout chatFileLayout;
        ImageView chatFileAvatar;
        TextView chatFileName;
        TextView chatFileSize;
        TextView chatFileStatus;

        //add by Zoro.Qian send location
        RelativeLayout chatLocationLayout;
        CircleImageView chatLocationAvatar;
        TextView chatLocationAddStr;
    }

    public class MyTimerTask extends TimerTask {
        String tag;
        int type;
        int count = 1;

        public MyTimerTask(String tag, int type) {
            super();
            this.tag = tag;
            this.type = type;
        }

        @Override
        public void run() {
            handler.obtainMessage(1, type, count % 3, tag).sendToTarget();
            count++;
        }
    }

    public void stopVoice() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            if (timer != null) {
                timer.cancel();
            }
            if (!isVoicing.equals("") && voicingType != -1) {
                ImageView voicing = (ImageView) listView
                        .findViewWithTag(isVoicing);
                if (voicingType == 1) {
                    voicing.setImageResource(R.drawable.chatto_voice_playing);
                } else {
                    voicing.setImageResource(R.drawable.chatfrom_voice_playing);
                }
            }
        }
    }

    protected void doVoiceClickEvent(String path, String time, final int type) {

        final ImageView voice = (ImageView) listView.findViewWithTag(time);

        File voiceFile = new File(path);

        if (voiceFile.exists()) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                timer.cancel();

                if (type == CommConstants.MSG_SEND) {
                    voice.setImageResource(R.drawable.chatto_voice_playing);
                } else {
                    voice.setImageResource(R.drawable.chatfrom_voice_playing);
                }
                ImageView voicing = (ImageView) listView
                        .findViewWithTag(isVoicing);
                if (voicing != null) {
                    if (voicingType == 1) {
                        voicing.setImageResource(R.drawable.chatto_voice_playing);
                    } else {
                        voicing.setImageResource(R.drawable.chatfrom_voice_playing);
                    }
                }
            } else {
                isVoicing = time;
                voicingType = type;
                playVoice(path, voice, type);

                timer = new Timer(true);
                timer.schedule(new MyTimerTask(time, type), 100, 400);
            }
        }
    }

    private void playVoice(String filePath, final ImageView view, final int type) {
        stopPlayVoice();

        mMediaPlayer = new MediaPlayer();
        // AudioManager am = (AudioManager) mContext
        // .getSystemService(Context.AUDIO_SERVICE);
        // am.setStreamVolume(AudioManager.STREAM_MUSIC,
        // am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
        // AudioManager.FLAG_PLAY_SOUND);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                timer.cancel();

                if (type == 1) {
                    view.setImageResource(R.drawable.chatto_voice_playing);
                } else {
                    view.setImageResource(R.drawable.chatfrom_voice_playing);
                }

            }
        });
        mMediaPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void stopPlayVoice() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                timer.cancel();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case ATTACHMENT_DOWNLOAD_END:
                    progressDialog.dismiss();
                    choseThirdPartySoftwareToOpenAttachment();
                    break;
                case ADJUST_LAYOUT_FOR_ATTACHMENT:
                    // AdjustLayoutForAttachment();
                    break;
                case ERROR_DOWNLOAD_ATTACHMENT:
                    Toast toast = Toast.makeText(mContext, mContext.getString(R.string.network_break_please_check), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                case SHOW_MY_PROGRESS:
                    // mProgressDialogBeforeShowPercent.dismiss();
                    // pb.setVisibility(View.VISIBLE);
                    // pb.setMax(fileSize);
                    // pb.setProgress(1);
                    break;
                // 条件不满足时dismiss 该进度条.
                case CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT:
                    progressDialog.dismiss();
                    break;
                case SHOW_DIALOG_BEFORE_DOWNLOAD:
                    progressDialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.loading_attach),
                            true, true);
                    break;
                case SHOW_DIALOG_BEFORE_APKLOAD:
                    progressDialog = ProgressDialog.show(mContext, "",
                            mContext.getString(R.string.downloading_app), true, true);
                    break;
                default:
                    pb.setProgress(msg.what);
                    break;
            }
        }

    };

    private void toDownloadAttachment(final String username,
                                      final String password) {
        Message message = mHandler.obtainMessage();
        message.what = SHOW_MY_PROGRESS;
        mHandler.sendMessage(message);

        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                DownloadFiles.downFile(fileLink, CommConstants.SD_DOWNLOAD,
                        username, password, new UpdataBarListernerImpl());
                Message message = mHandler.obtainMessage();
                message.what = ATTACHMENT_DOWNLOAD_END;
                mHandler.sendMessage(message);
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void openAttachmentWebView(String fileLink, String user,
                                      String password) {
        Message message = mHandler.obtainMessage();
        if (fileLink.indexOf(".apk") != -1) {
            message.what = SHOW_DIALOG_BEFORE_APKLOAD;
        } else {
            message.what = SHOW_DIALOG_BEFORE_DOWNLOAD;
        }
        mHandler.sendMessage(message);
        this.fileLink = fileLink;
        if (FileUtils.getInstance().existSoftwareForTheFile(mContext,
                this.fileLink)) {
            if (!Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED)) {
                Message msg = mHandler.obtainMessage();
                msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                mHandler.sendMessage(msg);
                Toast.makeText(mContext, mContext.getString(R.string.sdcard_not_ready), Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            if (DownloadFiles.isNetAvailable(mContext)) {
                // 在下载文件的方法里判断： 下载之前先判断附件是否存在，文件是否在server 端已经更新了？
                // File.lastModified() 好象不行，一会再看看。
                this.toDownloadAttachment(user, password);

                // 附件下载到手机以后，列出支持该附件的第三方软件，供用户选择使用。
                // this.choseThirdPartySoftwareToOpenAttachment();
            } else {
                Message msg = mHandler.obtainMessage();
                msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
                mHandler.sendMessage(msg);
                Toast.makeText(mContext, mContext.getString(R.string.network_break_please_check), Toast.LENGTH_SHORT).show();
            }

        } else {
            Message msg = mHandler.obtainMessage();
            msg.what = CLOSE_MPROGRESS_DIALOG_BEFORE_SHOW_PERCENT;
            mHandler.sendMessage(msg);
            Toast.makeText(mContext,
                    mContext.getString(R.string.please_download_office_app_first), Toast.LENGTH_SHORT).show();
        }

    }

    protected void choseThirdPartySoftwareToOpenAttachment() {
        File file = FileUtils.getInstance().getFileFromSDByFileLink(
                CommConstants.SD_DOWNLOAD, this.fileLink);
        // Use for debug
        // boolean exists = file.exists();
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 设置intent的Action属性
        intent.setAction(android.content.Intent.ACTION_VIEW);
        // 不能用直接使用wps打开的附件刚自行选择软件
        try {
            // 获取文件file的MIME类型
            String type = FileUtils.getInstance().getMIMEType(this.fileLink);
            // 设置intent的data和Type属性。
            intent.setDataAndType(Uri.fromFile(file), type);
            // 跳转
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, mContext.getString(R.string.download_office_app_first), Toast.LENGTH_SHORT).show();
        }

    }

    public class UpdataBarListernerImpl implements FileUtils.UpdataBarListerner {

        @Override
        public void onUpdate(int value, int status) {
            Message message = mHandler.obtainMessage();
            message.what = value;
            mHandler.sendMessage(message);
        }

        @Override
        public void onError(int value, int status) {
            Message message = mHandler.obtainMessage();
            message.what = value;
            mHandler.sendMessage(message);
        }
    }

    //长按文档显示下载，转发功能。

    /**
     * @param message MessageBean
     */
    private void showPop(View view, int position, final MessageBean message) {
        List<String> popupMenuItemList = new ArrayList<>();
        String json = message.getContent();
        try {
            JSONObject jsonObject = new JSONObject(json);
            final JSONObject content = jsonObject.getJSONObject("content");
            final String name;
            final String path;
            if (message.getMtype().equals(CommConstants.MSG_TYPE_FILE_2)) {
                String uuid = content.getString("uuid");
                String fileName = content.getString("name");
                String fileSuffix = "";
                int index = fileName.lastIndexOf(".");
                if (index > 0) {
                    fileSuffix = fileName.substring(index, fileName.length()).toLowerCase();
                }
                name = uuid + fileSuffix;
                path = CommConstants.SD_DOCUMENT + name;
            } else {
                name = content.getString("url");
                path = CommConstants.SD_DATA_FILE + name;
            }


            final File file = new File(path);
            if (file.exists()) {
                popupMenuItemList.add(mContext.getString(R.string.chat_file_pop_open));
            } else {
                if (ChatBaseActivity.msgIdMap.containsKey(message.getMsgId())) {
                    popupMenuItemList.add(mContext.getString(R.string.chat_file_pop_downloading));
                } else {
                    popupMenuItemList.add(mContext.getString(R.string.chat_file_pop_download));
                }

            }

            popupMenuItemList.add(mContext.getString(R.string.chat_file_pop_forward));
            //popupMenuItemList.add(mContext.getString(R.string.chat_file_pop_collection));
            PopupList.getInstance().initPopupList(mContext, view, position, listView, popupMenuItemList, new PopupListAdapter.OnPopupListClickListener() {
                @Override
                public void onPopupListItemClick(View contextView, int contextPosition, View view, int position) {
                    switch (position) {
                        case 0:
                            if (file.exists()) {//open
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.fromFile(file),
                                        FileUtils.getInstance().getMIMEType(path));
                                mContext.startActivity(intent);
                            } else {// download
                                if (!ChatBaseActivity.msgIdMap.containsKey(message.getMsgId())) {
                                    if (message.getMtype().equals(CommConstants.MSG_TYPE_FILE_2)) {
                                        try {
                                            String uuid = content.getString("uuid");
                                            String token = content.getString("token");
                                            String url = CommConstants.URL_DOWN_FILE + "?docId=" + uuid + "&token=" + token;
                                            String filePath = CommConstants.SD_DOCUMENT;
                                            downLoadFile(url, filePath, name, message);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        String url = CommConstants.URL_DOWN + name;
                                        String filePath = path.substring(0, path.lastIndexOf(File.separator) + 1);
                                        String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
                                        downLoadFile(url, filePath, fileName, message);
                                    }

                                }
                            }
                            break;
                        case 1:// forward
                            if (message.getIsSend() != CommConstants.MSG_SEND_FAIL) {
                                if (null != shareFileListener) {
                                    shareFileListener.shareFile(message);
                                }
                            } else {
                                Toast.makeText(mContext, mContext.getString(R.string.send_failed_can_not_resend), Toast.LENGTH_SHORT).show();
                            }

                            break;
                        case 2:
                            break;
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setShareFileListener(ShareFileListener shareFileListener) {
        this.shareFileListener = shareFileListener;
    }

    public interface ShareFileListener {
        void shareFile(MessageBean message);
    }

    //下载文档
    private void downLoadFile(String url, String filePath, String fileName, MessageBean message) {
        if(!FileUtils.isDownloadManagerAvailable(mContext)){
            FileUtils.goToDownloadManagerSetting(mContext);
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(filePath.replace(CommConstants.SD_CARD, ""), fileName);
        // request.setTitle("MeiLiShuo");
        // request.setDescription("MeiLiShuo desc");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        // request.setMimeType("application/cn.trinea.download.file");
        long downloadId = downloadManager.enqueue(request);
        ChatBaseActivity.downloadMap.put(downloadId, message.getMsgId());
        ChatBaseActivity.msgIdMap.put(message.getMsgId(), "downloading");
        notifyDataSetChanged();
    }

}
