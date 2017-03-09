package com.qd.recorder;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.widget.RoundImageView;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2016/5/16.
 */
public class VideoLayout extends LinearLayout implements View.OnClickListener, VideoPlayTextureView.MediaStateLitenser {

    private Context context;
    private String videoPath;

    private ImageView videoPlay,loadingPic;
    private RoundImageView videoPic;
    private VideoPlayTextureView videoView;

    public VideoLayout(Context context) {
        super(context);
        initView(context);
    }

    public VideoLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        View converView = LayoutInflater.from(context).inflate(R.layout.layout_video, null);
        loadingPic = (ImageView) converView.findViewById(R.id.loading_img);
        videoPlay = (ImageView) converView.findViewById(R.id.video_play);
        videoPic = (RoundImageView) converView.findViewById(R.id.video_pic);
        videoView = (VideoPlayTextureView) converView.findViewById(R.id.preview_video);

        videoPic.setOnClickListener(this);
        videoView.setOnClickListener(this);
        videoView.setMediaStateLitenser(this);

        videoView.setClickable(false);

        addView(converView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void reset() {
        videoPlay.setVisibility(View.VISIBLE);
        videoPic.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setVideoPicPath(String videoPicPath) {

        AQuery aQuery = new AQuery(context);

        if ((new File(CommConstants.SD_DATA_VIDEO + videoPicPath)).exists()) {
            BitmapAjaxCallback callback = new BitmapAjaxCallback();
            callback.animation(AQuery.FADE_IN_NETWORK);
            callback.rotate(true);
            AQuery aq = aQuery.recycle(videoPic);
            aq.id(videoPic).image(new File(videoPicPath), true, 128, callback);
        } else {
            BitmapAjaxCallback callback = new BitmapAjaxCallback() {
                @Override
                protected void callback(String url, ImageView iv,
                                        Bitmap bm, AjaxStatus status) {
                    super.callback(url, iv, bm, status);
                    if (status.getCode() == 200) {
                        iv.setImageBitmap(bm);
                    }
                }
            };
            aQuery.id(videoPic).image(CommConstants.URL_DOWN + videoPicPath, true, true, 256, 0, callback);
        }
    }

    public void stopVideoPlay() {
        videoView.stop();
    }

    @Override
    public void onClick(View v) {

        if (R.id.video_pic == v.getId()) {
            //判断sd卡是否存在对应的video，若不存在则先去服务器端下载，然后再播放
            if (new File(CommConstants.SD_DATA_VIDEO + videoPath).exists()) {
                videoView.prepare(CommConstants.SD_DATA_VIDEO + videoPath);
                videoView.play();
                videoView.setClickable(true);
            } else {
                downloadFile(CommConstants.URL_DOWN + videoPath, videoPath);
            }

        } else if (R.id.preview_video == v.getId()) {
            Intent intent = new Intent(context, VideoPreviewActivity.class);
            intent.putExtra("path", CommConstants.SD_DATA_VIDEO + videoPath);
            context.startActivity(intent);
        }
    }

    private void downloadFile(String url, final String fileName) {
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
                        loadingPic.startAnimation(hyperspaceJumpAnimation);
                        loadingPic.setVisibility(View.VISIBLE);
                        videoPlay.setVisibility(View.GONE);
                    }

                    @Override
                    public void inProgress(float progress, long total) {
                        if(100==(int)progress*100){
                            loadingPic.setVisibility(View.GONE);
                            loadingPic.clearAnimation();
                            videoView.setClickable(true);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(File file) {
                        videoView.prepare(CommConstants.SD_DATA_VIDEO + videoPath);
                        videoView.play();
                    }
                });
    }

    @Override
    public void OnCompletionListener() {
        videoPlay.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
    }

    @Override
    public void OnPrepareListener() {

    }

    @Override
    public void OnPauseListener() {
        videoPlay.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
    }

    @Override
    public void OnPlayListener() {
        videoPlay.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnDownLoadingListener() {

    }
}
