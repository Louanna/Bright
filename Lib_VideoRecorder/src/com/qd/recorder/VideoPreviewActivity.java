package com.qd.recorder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class VideoPreviewActivity extends Activity implements TextureView.SurfaceTextureListener
        , OnClickListener, OnCompletionListener {

    private String path;
    private TextureView surfaceView;
    private MediaPlayer mediaPlayer;
    private ImageView imagePlay;
    private Button videoCancel, videoDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        surfaceView = (TextureView) findViewById(R.id.preview_video);

        RelativeLayout preview_video_parent = (RelativeLayout) findViewById(R.id.preview_video_parent);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) preview_video_parent
                .getLayoutParams();
        layoutParams.width = displaymetrics.widthPixels;
        layoutParams.height = displaymetrics.widthPixels;
        preview_video_parent.setLayoutParams(layoutParams);

        //add by anna 视频方向
        surfaceView.requestLayout();
        surfaceView.invalidate();

        surfaceView.setSurfaceTextureListener(this);
        surfaceView.setOnClickListener(this);

        path = getIntent().getStringExtra("path");

        imagePlay = (ImageView) findViewById(R.id.preview_play);
        imagePlay.setOnClickListener(this);

        RelativeLayout rl_bottom = (RelativeLayout) findViewById(R.id.recorder_bottom);

        videoDelete = (Button) findViewById(R.id.video_delete);
        videoDelete.setOnClickListener(this);
        videoCancel = (Button) findViewById(R.id.video_cancel);
        videoCancel.setOnClickListener(this);

        if (getIntent().getBooleanExtra("isEdit", false)) {
            rl_bottom.setVisibility(View.VISIBLE);
        } else {
            rl_bottom.setVisibility(View.GONE);
        }

        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(path);
        Bitmap bitmap = media.getFrameAtTime(0);
        ImageView previewImage = (ImageView) findViewById(R.id.preview_image);
        previewImage.setImageBitmap(bitmap);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            imagePlay.setVisibility(View.GONE);
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mediaPlayer.isPlaying()) {
            imagePlay.setVisibility(View.VISIBLE);
        }
    }

    private void prepare(Surface surface) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置需要播放的视频
            mediaPlayer.setDataSource(path);
            // 把视频画面输出到Surface
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
            mediaPlayer.setOnPreparedListener(
                    new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            imagePlay.setVisibility(View.GONE);
                        }
                    }
            );
        } catch (Exception e) {
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1,
                                          int arg2) {
        prepare(new Surface(arg0));
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,
                                            int arg2) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.preview_play) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            imagePlay.setVisibility(View.GONE);
        } else if (i == R.id.preview_video) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                imagePlay.setVisibility(View.VISIBLE);
            }
        } else if (i == R.id.video_delete) {
            Intent data = getIntent();
            data.putExtra("isDeleteVideo", true);
            setResult(CONSTANTS.REQUEST_CODE_DELETE_VIDEO_IMAGE, data);
            finish();
        } else if (i == R.id.video_cancel) {
            Intent data = getIntent();
            data.putExtra("isDeleteVideo", false);
            setResult(CONSTANTS.REQUEST_CODE_DELETE_VIDEO_IMAGE, data);
            finish();
        }
    }

    private void stop() {
        mediaPlayer.stop();
        finish();
    }

    @Override
    public void onBackPressed() {
        stop();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
        imagePlay.setVisibility(View.VISIBLE);
    }
}
