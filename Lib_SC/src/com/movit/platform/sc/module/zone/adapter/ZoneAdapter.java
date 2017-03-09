package com.movit.platform.sc.module.zone.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.cloud.utils.FileType;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.PopupUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.tree.ViewHeightBasedOnChildren;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.framework.widget.CircleImageView;
import com.movit.platform.framework.widget.CusGridView;
import com.movit.platform.framework.widget.RoundImageView;
import com.movit.platform.framework.widget.SelectPicPopup;
import com.movit.platform.sc.R;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.movit.platform.sc.module.zone.model.Knowledge;
import com.movit.platform.sc.module.zone.model.KnowledgeComment;
import com.movit.platform.sc.module.zone.model.KnowledgeLike;
import com.movit.platform.sc.view.clipview.ClickedRelativeLayout;
import com.movit.platform.sc.view.clipview.ClickedSpanListener;
import com.movit.platform.sc.view.clipview.ClickedSpanTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

public class ZoneAdapter extends BaseAdapter {


    private Context context;
    private LayoutInflater mInflater;
    private List<Knowledge> mData;
    private Handler handler;
    private AQuery aq;
    private float width;
    private DialogUtils proDialogUtil;
    public static final int TYPE_MAIN = 0;
    public static final int TYPE_DETAIL = 1;
    public static final int TYPE_OTHER = 2;
    private int adapterType;
    private String userId;
    private IZoneManager zoneManager;

    private Map<String,MediaPlayer> mediaPlayers = new HashMap<>();

    private DownFileListener downFileListener = null;

    public ZoneAdapter(Context context, List<Knowledge> mData, Handler handler, int adapterType,
                       String userId, DialogUtils dialogUtil, IZoneManager zoneManager) {
        super();
        this.zoneManager = zoneManager;
        this.context = context;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(context);
        this.handler = handler;

        Display display = ((Activity) context).getWindowManager()
                .getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;// 得到宽度
        aq = new AQuery(context);
        this.adapterType = adapterType;
        this.userId = userId;
        this.proDialogUtil = dialogUtil;

    }

    public String getUserId() {
        return userId;
    }

    @Override
    public int getCount() {
//        if (adapterType == TYPE_MAIN) {
//            return mData.size() + 1;
//        } else if (adapterType == TYPE_DETAIL) {
//            return mData.size();
//        } else if (adapterType == TYPE_OTHER) {
//            return mData.size() + 1;
//        } else {
//            return mData.size();
//        }
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int postion, View converView, ViewGroup arg2) {
        ViewHolder holder = null;
        holder = new ViewHolder();
//        if (adapterType == TYPE_MAIN || adapterType == TYPE_OTHER) {
//            if (postion == 0) {
//                converView = initTopView(arg2, holder);
//            } else {
//                converView = initListView(arg2, holder);
//            }
//        } else if (adapterType == TYPE_DETAIL) {
//            converView = initListView(arg2, holder);
//        }

        converView = initListView(arg2, holder);

        converView.setTag(holder);

        AQuery aQuery = aq.recycle(converView);
//        if (adapterType == TYPE_MAIN || adapterType == TYPE_OTHER) {
//            if (postion == 0) {
//                initTopData(holder, aQuery);
//            } else {
//                initListData(holder, converView, arg2, postion - 1, aQuery);
//            }
//        } else if (adapterType == TYPE_DETAIL) {
//            initListData(holder, converView, arg2, postion, aQuery);
//        }

        initListData(holder, converView, arg2, postion, aQuery);

        return converView;
    }

    private View initTopView(ViewGroup arg2, final ViewHolder holder) {
        View converView = mInflater.inflate(R.layout.sc_item_zone_0, arg2, false);
        holder.avatar = (CircleImageView) converView
                .findViewById(R.id.zone_avatar);
        holder.name = (TextView) converView.findViewById(R.id.zone_name);

        holder.zone_bg = (ImageView) converView.findViewById(R.id.zone_bg);

        //只有在自己的同事圈和个人主页才能进行背景图修改
        String myUserId = MFSPHelper.getString(CommConstants.USERID);
        if (myUserId.equalsIgnoreCase(userId)) {
            holder.zone_bg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(holder.zone_bg.getWindowToken(), 0);
                    }
                    // 实例化SelectPicPopupWindow
                    popWindow = new SelectPicPopup((Activity) context, itemsOnClick);
                    // 显示窗口
                    popWindow.showAtLocation(holder.avatar,
                            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                }
            });
        }

        return converView;
    }

    private SelectPicPopup popWindow;
    private Uri imageUri;// The Uri to store the big

    private OnClickListener itemsOnClick = new OnClickListener() {

        public void onClick(View v) {
            popWindow.dismiss();
            int i = v.getId();
            if (i == R.id.btn_take_photo) {// 跳转相机拍照
                imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                if (imageUri == null) {
                    return;
                }
                String sdStatus = Environment.getExternalStorageState();
                if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(context, context.getString(R.string.can_not_find_sd), Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                ((Activity) context).startActivityForResult(intent2, 2);

            } else if (i == R.id.btn_pick_photo) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                ((Activity) context).startActivityForResult(intent, 1);

            }
        }
    };

    private View initListView(ViewGroup arg2, ViewHolder holder) {
        View converView = mInflater.inflate(R.layout.sc_item_zone, arg2, false);
        holder.videoLayout = (RelativeLayout) converView
                .findViewById(R.id.zone_list_item_video);

        holder.attachmentsLayout = (LinearLayout) converView.findViewById(R.id.ll_attachments);
        holder.ivFileType = (ImageView) converView.findViewById(R.id.iv_file_type);
        holder.tvFileName = (TextView) converView.findViewById(R.id.tv_file_name);

        holder.loadingPic = (ImageView) converView.findViewById(R.id.loading_img);
        holder.videoPic = (RoundImageView) converView
                .findViewById(R.id.zone_list_item_video_pic);
        holder.videoPlay = (ImageView) converView
                .findViewById(R.id.zone_list_item_video_play);
        holder.video = (TextureView) converView
                .findViewById(R.id.preview_video);

        holder.avatar = (ImageView) converView
                .findViewById(R.id.zone_list_item_avatar);
        holder.name = (TextView) converView
                .findViewById(R.id.zone_list_item_name);
        holder.comment = (TextView) converView
                .findViewById(R.id.zone_list_item_comment);
        holder.more = (TextView) converView
                .findViewById(R.id.zone_list_item_comment_more);
        holder.link = (TextView) converView
                .findViewById(R.id.zone_list_item_link);
        holder.time = (TextView) converView
                .findViewById(R.id.zone_list_item_time);
        holder.delImageView = (ImageView) converView
                .findViewById(R.id.zone_list_item_del_img);
        holder.likeImageView = (ImageView) converView
                .findViewById(R.id.zone_list_item_like_img);
        holder.commentImageView = (ImageView) converView
                .findViewById(R.id.zone_list_item_comment_img);
        holder.gridView = (CusGridView) converView
                .findViewById(R.id.zone_list_item_gridview);
        holder.commentLinear = (LinearLayout) converView
                .findViewById(R.id.zone_list_item_comment_linear);
        holder.likersLine = converView
                .findViewById(R.id.zone_list_item_likers_line);
        holder.pic = (ImageView) converView
                .findViewById(R.id.zone_list_item_pic);
        holder.top = (ImageView) converView
                .findViewById(R.id.zone_list_item_top);
        return converView;
    }

    private void initTopData(ViewHolder holder, AQuery aQuery) {
        holder.flag = "0";

        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(userId);

        if (userInfo == null) {
            return;
        }

        int picId = R.drawable.avatar_male;
        if ("男".equals(userInfo.getGender())) {
            picId = R.drawable.avatar_male;
        } else if ("女".equals(userInfo.getGender())) {
            picId = R.drawable.avatar_female;
        }

        String uname = MFSPHelper.getString(CommConstants.AVATAR);
        String myUserId = MFSPHelper.getString(CommConstants.USERID);

        String avatarName = userInfo.getAvatar();
        String avatarUrl = "";

        if (StringUtils.notEmpty(avatarName)) {
            avatarUrl = avatarName;
        }
        if (myUserId.equalsIgnoreCase(userId) && StringUtils.notEmpty(uname)) {
            avatarUrl = uname;
        }

        //设置背景图
        setZoneBackgroudPic(aQuery, holder);

        // 这边的图片不做缓存处理 这边的是圆的
        if (StringUtils.notEmpty(avatarUrl)) {

            //为了适配其他项目
            if (avatarUrl.startsWith("http")) {
                aQuery.id(holder.avatar).image(avatarUrl,
                        false, true, 128, picId);
            } else {
                aQuery.id(holder.avatar).image(CommConstants.URL_DOWN + avatarUrl,
                        false, true, 128, picId);
            }

        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(
                    context.getResources(), picId);
            holder.avatar.setImageBitmap(bitmap);
        }
        //TODO anna 景瑞和EOP判断标准不一致：分别适用域帐号和普通帐号
        if (null != userInfo.getEmpCname() && null != userInfo.getEmpAdname()) {

            //EOP---域帐号
            if (userInfo.getEmpAdname().contains(".")) {
                holder.name.setText(userInfo.getEmpCname()
                        + "   "
                        + userInfo.getEmpAdname().substring(0,
                        userInfo.getEmpAdname().indexOf(".")));
            } else {
                //景瑞---普通帐号
                holder.name.setText(userInfo.getEmpCname());
            }
        }

        holder.avatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(SCConstants.ZONE_CLICK_AVATAR,
                        adapterType, 0, userId).sendToTarget();
            }
        });
    }

    private void setZoneBackgroudPic(final AQuery aQuery, final ViewHolder holder){

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final int width = wm.getDefaultDisplay().getWidth();

        HttpManager.getJsonWithToken(CommConstants.URL_USER_BG_IMAGE + userId, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {

                if(StringUtils.notEmpty(response)){
                    JSONObject resObj = new JSONObject(response);
                    if(resObj.getBoolean("ok") && StringUtils.notEmpty(resObj.getJSONObject("objValue"))&& StringUtils.notEmpty(resObj.getJSONObject("objValue").getString("backgroundImage"))){
                        aQuery.id(holder.zone_bg).image(CommConstants.URL_DOWN + resObj.getJSONObject("objValue").getString("backgroundImage"),
                                false, true, width, R.drawable.zone_banner_bg);
                    }
                }
            }
        });
    }

    private void initListData(final ViewHolder holder, View converView,
                             ViewGroup arg2, final int postion, AQuery aQuery) {
        final Knowledge zone = (Knowledge) getItem(postion);
        holder.flag = zone.getKlnowledgeSay().getId();
        String userid = zone.getKlnowledgeSay().getCreateBy();
        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(userid);

        if (userInfo != null) {
            holder.name.setText(userInfo.getEmpCname());
            int picId = R.drawable.avatar_male;
            if ("男".equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if ("女".equals(userInfo.getGender())) {
                picId = R.drawable.avatar_female;
            }
            String uname = MFSPHelper.getString(CommConstants.AVATAR);
            String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
            String avatarName = userInfo.getAvatar();
            String avatarUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (adname.equalsIgnoreCase(userInfo.getEmpAdname())
                    && StringUtils.notEmpty(uname)) {
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
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(context, picId,
                        10);
                holder.avatar.setImageBitmap(bitmap);
            }
//            holder.avatar.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    handler.obtainMessage(SCConstants.ZONE_CLICK_AVATAR,
//                            adapterType, 0, zone.getcUserId()).sendToTarget();
//                }
//            });
//            holder.name.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    handler.obtainMessage(SCConstants.ZONE_CLICK_AVATAR,
//                            adapterType, 0, zone.getcUserId()).sendToTarget();
//                }
//            });
        }


        if (StringUtils.notEmpty(zone.getKlnowledgeSay().getContent())) {
            holder.comment.setText(StringUtils
                    .convertNormalStringToSpannableString(context,
                            zone.getKlnowledgeSay().getContent(), true,
                            (int) holder.comment.getTextSize() + 10));

            //增加复制粘贴功能
            final String tempStr = holder.comment.getText().toString();
            holder.comment.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopupUtils.showPopupWindow(context, view,tempStr,null);
                    return false;
                }
            });
        } else {
            holder.comment.setVisibility(View.GONE);
        }

        holder.time.setText(zone.getKlnowledgeSay().getCreateAppDate());

        List<KnowledgeComment> comments = zone.getKnowledgeComment();
        final List<KnowledgeLike> likers = zone.getKnowledgeLike();
        holder.likers = (ClickedSpanTextView) mInflater.inflate(
                R.layout.sc_item_zone_clickspan_text, null);

        holder.commentLinear.setVisibility(View.GONE);
        holder.commentLinear.removeAllViews();
        holder.commentLinear.setGravity(Gravity.CENTER_VERTICAL);
        if (likers != null && !likers.isEmpty()) {
            holder.commentLinear.setVisibility(View.VISIBLE);

            holder.likers.setText(getClickableSpan(likers));
            holder.likers.setMovementMethod(LinkMovementMethod.getInstance());
            if (isMyLike(likers,MFSPHelper.getString(CommConstants.USERID))) {
                holder.likeImageView
                        .setImageResource(R.drawable.zone_ico_like_pressed);
            } else {
                holder.likeImageView
                        .setImageResource(R.drawable.zone_ico_like_normal);
            }

            if (comments != null && !comments.isEmpty()) {
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setMargins(0, 5, 0, 0);
                holder.likers.setLayoutParams(layout);
            }
            holder.commentLinear.addView(holder.likers);
        }

        if (comments != null && !comments.isEmpty() && likers != null
                && !likers.isEmpty()) {
            holder.likersLine.setVisibility(View.VISIBLE);
            holder.commentLinear.addView(holder.likersLine);
        }

        if (comments != null && !comments.isEmpty()) {
            holder.commentLinear.setVisibility(View.VISIBLE);
            for (int i = 0; i < comments.size(); i++) {
                ClickedRelativeLayout rLayout = (ClickedRelativeLayout) mInflater
                        .inflate(R.layout.sc_item_zone_rich_text, null);
                ClickedSpanTextView textView = (ClickedSpanTextView) rLayout
                        .findViewById(R.id.rich_text);
                final KnowledgeComment comment = comments.get(i);
                textView.setText(getCommentSpan(comment, textView));
                textView.setTextColor(0xff333333);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                if (i == comments.size() - 1) {
                    layout.setMargins(0, 5, 0, 5);
                } else {
                    layout.setMargins(0, 5, 0, 0);
                }
                rLayout.setLayoutParams(layout);
                final int k = i;
//                rLayout.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        // 如果是自己 则删除，别人则评论
//						if (comment.getUserId().equals(
//								MFSPHelper.getString(CommConstants.USERID))) {
//							handler.obtainMessage(
//                                    SCConstants.ZONE_CLICK_COMMENT_TO_DEL,
//									k, postion).sendToTarget();
//						} else {
//                        handler.obtainMessage(
//                                SCConstants.ZONE_CLICK_COMMENT, k, 1,
//                                postion).sendToTarget();
//						}
//                    }
//                });
                holder.commentLinear.addView(rLayout);
            }
        }

        //判断是否有图片
        holder.pic.setVisibility(View.GONE);
        holder.gridView.setVisibility(View.GONE);
        String image_str = zone.getKlnowledgeSay().getImages();
        if(!TextUtils.isEmpty(image_str)){
            String[] images = image_str.split(",");
            final List<String> imageNames = Arrays.asList(images);
//        ArrayList<String> imageSizes = zone.getImageSizes();
            if (imageNames.size() > 0) {
                if (imageNames.size() == 1) {
                    holder.pic.setVisibility(View.VISIBLE);

//                String size = imageSizes.get(0);
//                String sizeStr = size.substring(1, size.length() - 1);
//                float w = Float.parseFloat(sizeStr.split(",")[0].trim());
//                float h = Float.parseFloat(sizeStr.split(",")[1].trim());
//                float ratio = h / w;
                    ViewGroup.LayoutParams para = holder.pic.getLayoutParams();
                    Bitmap bitmap;
//                if (w > h) { // 横着的
//                    para.width = (int) width / 2;
//                    para.height = (int) (width / 2 * ratio);
//                    bitmap = BitmapFactory.decodeResource(
//                            context.getResources(),
//                            R.drawable.zone_pic_default_2);
//                } else if (w < h) {
//                    para.width = (int) (width / 2 / ratio);
//                    para.height = (int) (width / 2);
//                    bitmap = BitmapFactory.decodeResource(
//                            context.getResources(),
//                            R.drawable.zone_pic_default_1);
//                } else {
                    para.width = (int) (width / 2);
                    para.height = (int) (width / 2);
                    bitmap = BitmapFactory
                            .decodeResource(context.getResources(),
                                    R.drawable.zone_pic_default);
//                }
                    Log.v("pic", para.width + "--" + para.height);
                    final Bitmap bitmap2 = PicUtils.zoomImage(bitmap, para.width,
                            para.height);
                    holder.pic.setLayoutParams(para);
                    BitmapAjaxCallback callback = new BitmapAjaxCallback() {

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
//                callback.ratio(ratio);
                    callback.preset(bitmap2);
//                    final String midName = imageNames.get(0).replace(".", "_m.");
                    final String midName = imageNames.get(0);
//                    aQuery.id(holder.pic).image(CommConstants.URL_DOWN + midName, true,
//                            true, 256, 0, callback);

                    MFAQueryHelper.setImageView(holder.pic,CommConstants.URL_DOWN + midName,R.drawable.zone_pic_default);

                    holder.pic.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            ZonePublishActivity.selectImagesList.clear();
                            ZonePublishActivity.selectImagesList.add(imageNames
                                    .get(0));
                            ArrayList<String> preset = new ArrayList<>();
                            preset.add(midName);
                            Intent intent = new Intent(context,
                                    ImageViewPagerActivity.class);
                            int[] location = new int[2];
                            v.getLocationOnScreen(location);
                            intent.putExtra("locationX", location[0]);
                            intent.putExtra("locationY", location[1]);
                            intent.putExtra("width", v.getWidth());
                            intent.putExtra("height", v.getHeight());
                            intent.putStringArrayListExtra("selectedImgs",
                                    ZonePublishActivity.selectImagesList);
                            intent.putStringArrayListExtra("presetImgs", preset);
                            intent.putExtra("postion", 0);
                            context.startActivity(intent);
                            ((Activity) context).overridePendingTransition(0, 0);
                        }
                    });
                } else {
                    // 九宫格
                    holder.gridView.setVisibility(View.VISIBLE);
                    int w;
                    ViewHeightBasedOnChildren basedOnChildren = new ViewHeightBasedOnChildren(
                            context);
                    if (imageNames.size() == 4) {
                        holder.gridView.setNumColumns(2);
                        w = (int) (width / 4) * 2
                                + basedOnChildren.dip2px(context, 3);
                    } else {
                        holder.gridView.setNumColumns(3);
                        w = (int) (width / 4) * 3
                                + basedOnChildren.dip2px(context, 3) * 2;
                    }
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                            w, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, basedOnChildren.dip2px(context, 5), 0, 0);
                    holder.gridView.setLayoutParams(params);
                    holder.gridView.setAdapter(new ZoneItemGridAdapter(imageNames,
                            context, aQuery));
                }

            }
        }

        holder.link.setVisibility(View.GONE);
        holder.delImageView.setVisibility(View.GONE);
        holder.more.setVisibility(View.GONE);

//        if (StringUtils.notEmpty(userid) && adapterType == TYPE_OTHER
//                && userid.equals(MFSPHelper.getString(CommConstants.USERID))) {
//            holder.delImageView.setVisibility(View.VISIBLE);
//            holder.delImageView.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    proDialogUtil.showLoadingDialog(context, context.getString(R.string.waiting),
//                            false);
//                    zoneManager.saydel(zone.getcId(), postion, handler);
//                }
//            });
//        }

        holder.likeImageView.setTag("likeImageView" + postion);
        holder.likeImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                proDialogUtil.showLoadingDialog(context, context.getString(com.movit.platform.common.R.string.waiting), false);
                String knowledgeId = zone.getKlnowledgeSay().getId();
                String userId = MFSPHelper.getString(CommConstants.USERID);
                if (likers != null && !likers.isEmpty()
                        && isMyLike(likers,userId)){
                    zoneManager.delKnowledgeLike(knowledgeId,userId,
                            new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                    ToastUtils.showToast(context, "取消点赞失败！");
                                    DialogUtils.getInstants().dismiss();
                                }

                                @Override
                                public void onResponse(String response) throws JSONException {
//                                    handler.obtainMessage(SCConstants.ZONE_NICE_RESULT,
//                                            postion, 1, response).sendToTarget();
                                    delMyLike(postion,likers,MFSPHelper.getString(CommConstants.USERID));
                                    notifyDataSetChanged();
                                    DialogUtils.getInstants().dismiss();
                                }
                            });
                } else {
//                    zoneManager.nice(zone.getcId(), zone.getcUserId(), "",
//                            postion, handler);
                    zoneManager.saveKnowlegeLike(knowledgeId, userId, userId, new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            ToastUtils.showToast(context, "点赞失败！");
                            DialogUtils.getInstants().dismiss();
                        }

                        @Override
                        public void onResponse(String response) throws JSONException {
//                            handler.obtainMessage(SCConstants.ZONE_NICE_RESULT,
//                                    postion, 0, response).sendToTarget();

                            KnowledgeLike like = new KnowledgeLike();
                            like.setIdLike(MFSPHelper.getString(CommConstants.USERID));
                            like.setName(MFSPHelper.getString(CommConstants.USERNAME));
                            mData.get(postion).getKnowledgeLike().add(like);

                            notifyDataSetChanged();
                            DialogUtils.getInstants().dismiss();
                        }
                    });
                }

            }
        });
        holder.commentImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(SCConstants.ZONE_CLICK_COMMENT, -1,
                        0, postion).sendToTarget();
            }
        });

        //判断是否有附件
        holder.attachmentsLayout.setVisibility(View.GONE);
        String attachments_str = zone.getKlnowledgeSay().getAttachments();
        if(!TextUtils.isEmpty(attachments_str)){
            holder.attachmentsLayout.setVisibility(View.VISIBLE);
            String[] attachments = attachments_str.split(",");
            final String listFileName = attachments[attachments.length-1].substring(attachments[attachments.length-1].lastIndexOf("/")+1);
            FileType.getFileTYpe(holder.ivFileType,listFileName);
            holder.tvFileName.setText(listFileName);
            holder.attachmentsLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String klnowledgeId = zone.getKlnowledgeSay().getId();
                    final String path = CommConstants.SD_DOCUMENT+klnowledgeId+listFileName;
                    File dir = new File(path);
                    if(dir.exists()){
                        //do open
                        FileUtils fileUtils = new FileUtils();
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(path)), fileUtils.getMIMEType(path));
                        //intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
                        context.startActivity(intent);
                    }else {
                        if(null != downFileListener){
                            downFileListener.downLoad(klnowledgeId+listFileName,klnowledgeId,zone.getKlnowledgeSay().getAttachmentsUrl());
                        }
                    }
                }
            });
        }


        holder.video.setVisibility(View.GONE);
        holder.videoLayout.setVisibility(View.GONE);
        //判断是否为视频说说
//        if (StringUtils.notEmpty(zone.getVideoPath()) && StringUtils.notEmpty(zone.getVideoPicPath())) {
//
//            LayoutParams para = holder.videoLayout.getLayoutParams();
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            int width = wm.getDefaultDisplay().getWidth();
//            para.width = width / 2;
//            para.height = para.width;
//
//            holder.videoLayout.setLayoutParams(para);
//            holder.videoLayout.setVisibility(View.VISIBLE);
//
//            final Bitmap bitmap = BitmapFactory
//                    .decodeResource(context.getResources(),
//                            R.drawable.zone_pic_default);
//            BitmapAjaxCallback callback = new BitmapAjaxCallback() {
//
//                @Override
//                protected void callback(String url, ImageView iv,
//                                        Bitmap bm, AjaxStatus status) {
//                    super.callback(url, iv, bm, status);
//                    if (status.getCode() != 200) {
//                        iv.setImageBitmap(bitmap);
//                    }
//                }
//            };
//            callback.animation(AQuery.FADE_IN_NETWORK);
//            callback.rotate(true);
//            callback.preset(bitmap);
//            aQuery.id(holder.videoPic).image(CommConstants.URL_DOWN + zone.getVideoPicPath(), true,
//                    true, 256, 0, callback);
//
//            //add by anna 视频方向
//            holder.video.requestLayout();
//            holder.video.invalidate();
//
//            final MediaPlayer mediaPlayer = new MediaPlayer();
//
//            holder.video.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//                @Override
//                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                    prepare(mediaPlayer, new Surface(surface), zone.getVideoPath());
//                }
//
//                @Override
//                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//                }
//
//                @Override
//                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                    mediaPlayer.stop();
//                    surface.release();
//                    return true;
//                }
//
//                @Override
//                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//                }
//            });
//            holder.video.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            });
//
//            holder.videoPic.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    //判断sd卡是否存在对应的video，若不存在则先去服务器端下载，然后再播放
//                    if (new File(CommConstants.SD_DATA_VIDEO + zone.getVideoPath()).exists()) {
//                        playVideo(CommConstants.SD_DATA_VIDEO + zone.getVideoPath(), mediaPlayer, holder);
//                    } else {
//                        downloadFile(CommConstants.URL_DOWN + zone.getVideoPath(), zone.getVideoPath(), mediaPlayer, holder);
//                    }
//                }
//            });
//        } else {
//            holder.videoLayout.setVisibility(View.GONE);
//        }
    }

    private void playVideo(final String MediaPath,final MediaPlayer mediaPlayer, final ViewHolder holder){

        //注意：下面的代码不要随意更改代码顺序，否则onSurfaceTextureAvailable很可能不执行
        holder.video.setVisibility(View.VISIBLE);
        holder.videoPlay.setVisibility(View.GONE);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp != null) {
                    mp.seekTo(0);
                    mediaPlayers.remove(MediaPath);
                }
                holder.videoPlay.setVisibility(View.VISIBLE);
                holder.video.setVisibility(View.GONE);
            }
        });

        if (null!=mediaPlayer && !mediaPlayer.isPlaying()) {
            mediaPlayers.put(MediaPath,mediaPlayer);
            mediaPlayer.start();
        }
    }

    public void stopMediaPlay(){
        if(mediaPlayers.size()>0){
            for(MediaPlayer mediaPlayer:mediaPlayers.values()){
                if(null!=mediaPlayer&&mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
            }
        }
    }

    private void downloadFile(String url, final String fileName,final MediaPlayer mediaPlayer, final ViewHolder holder) {
        OkHttpUtils.getWithToken()
                .url(url)
                .build()
                .execute(new FileCallBack(CommConstants.SD_DATA_VIDEO+fileName.substring(0,fileName.lastIndexOf("/")), fileName.substring(fileName.lastIndexOf("/")+1)) {

                    @Override
                    public void onBefore(Request request) {
                        super.onBefore(request);

                        // 加载动画
                        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                                context, R.anim.m_loading);
                        // 使用ImageView显示动画
                        holder.loadingPic.startAnimation(hyperspaceJumpAnimation);
                        holder.loadingPic.setVisibility(View.VISIBLE);
                        holder.videoPlay.setVisibility(View.GONE);
                    }

                    @Override
                    public void inProgress(float progress, long total) {
                        if(100==(int)progress*100){
                            holder.loadingPic.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(File file) {
                        playVideo(CommConstants.SD_DATA_VIDEO + fileName,mediaPlayer, holder);
                    }
                });
    }

    private void prepare(final MediaPlayer mediaPlayer, Surface surface, String videoPath) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置需要播放的视频
            mediaPlayer.setDataSource(CommConstants.SD_DATA_VIDEO + videoPath);
            mediaPlayer.setVolume(0,0);

            // 把视频画面输出到Surface
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(false);

            mediaPlayer.prepare();

            mediaPlayer.setOnPreparedListener(
                    new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    }
            );
        } catch (Exception e) {
        }
    }

    public final class ViewHolder {
        public String flag;
        public ImageView zone_bg;
        public ImageView avatar;
        public TextView name;
        public TextView comment;
        public TextView more;
        public TextView link;
        public TextView time;
        public ImageView delImageView;
        public ImageView likeImageView;
        public ImageView commentImageView;

        public RelativeLayout videoLayout;
        public RoundImageView videoPic;
        public ImageView loadingPic;
        public ImageView videoPlay;
        public TextureView video;

        public LinearLayout attachmentsLayout;
        public ImageView ivFileType;
        public TextView tvFileName;

        public LinearLayout commentLinear;
        public ClickedSpanTextView likers;
        public View likersLine;
        public CusGridView gridView;
        public ImageView pic;
        public ImageView top;
    }

    private boolean isMyLike(List<KnowledgeLike> likes,String userId){
        for (KnowledgeLike like:likes) {
            if(like.getIdLike().equals(userId)){
                return true;
            }
        }
        return false;
    }

    private boolean delMyLike(int postion,List<KnowledgeLike> likes,String userId){
        for (KnowledgeLike like:likes) {
            if(like.getIdLike().equals(userId)){
                mData.get(postion).getKnowledgeLike().remove(like);
                return true;
            }
        }
        return false;
    }

    private SpannableString getClickableSpan(List<KnowledgeLike> userIds) {

        String name = "[] ";
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            UserDao dao = UserDao.getInstance(context);
            UserInfo userInfo = dao.getUserInfoById(userIds.get(i).getIdLike());
            if (null == userInfo) {
                continue;
            }
            names.add(userInfo.getEmpCname());
            name += userInfo.getEmpCname() + "、";
        }
        name = name.substring(0, name.length() - 1);
        SpannableString spanableInfo = new SpannableString(name +" " + names.size()
                + context.getString(R.string.like_over_people));
        spanableInfo.setSpan(new ImageSpan(context,
                        R.drawable.zone_ico_like_small), 0, 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        int start = 3;
//        for (int i = 0; i < names.size(); i++) {
//            int end = start + names.get(i).length();
//            spanableInfo.setSpan(new MyClickedSpanListener(names.get(i),
//                            userIds.get(i).getUserId(), context), start, end,
//                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            start = end + 1;
//        }

        return spanableInfo;
    }

    private SpannableString getCommentSpan(KnowledgeComment comment, TextView textView) {
        UserDao dao = UserDao.getInstance(context);
        UserInfo user = dao.getUserInfoById(comment.getUserId());

        UserInfo toUser = null;
        if (user == null) {
            return new SpannableString(context.getString(R.string.commented_is_del));
        }
        StringBuffer text = new StringBuffer();
        text.append(user.getEmpCname());
//        if (StringUtils.notEmpty(comment.getUserId())
//                && !"0".equals(comment.getUserId())) {
//            toUser = dao.getUserInfoById(comment.getUserId());
//
//            if (toUser == null) {
//                return new SpannableString(context.getString(R.string.commented_is_del));
//            }
//            text.append(context.getString(R.string.reply) + toUser.getEmpCname());
//        }
        text.append("： " + comment.getContent());

        CharSequence charSeq = StringUtils
                .convertNormalStringToSpannableString(context, text.toString(),
                        true, (int) textView.getTextSize() + 8);
        SpannableString spanableInfo = new SpannableString(charSeq);

        int start = 0;
        int end = user.getEmpCname().length();
//        spanableInfo.setSpan(
//                new MyClickedSpanListener(user.getEmpCname(), user.getId(),
//                        context), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (toUser != null) {
            start = end + 2;
            end = start + toUser.getEmpCname().length();
            spanableInfo.setSpan(new MyClickedSpanListener(
                            toUser.getEmpCname(), toUser.getId(), context), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spanableInfo;
    }

    class MyClickedSpanListener extends ClickedSpanListener {

        String userId;

        public MyClickedSpanListener(String nameString, String userId,
                                     Context context) {
            super(nameString, context);
            this.userId = userId;
        }

        @Override
        public void onClick(View v) {
            handler.obtainMessage(SCConstants.ZONE_CLICK_AVATAR,
                    adapterType, 0, userId).sendToTarget();
        }

        @Override
        public void onLongClick(View view) {
            super.onLongClick(view);
        }

    }

    public void setDownFileListener(DownFileListener downFileListener){
        this.downFileListener = downFileListener;
    }

    public interface DownFileListener{
        void downLoad(String listFileName,String docId,String downFilePath);
    }
}
