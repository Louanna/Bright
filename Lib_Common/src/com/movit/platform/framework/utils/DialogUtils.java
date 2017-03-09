package com.movit.platform.framework.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movit.platform.common.R;
import com.movit.platform.framework.view.progress.DownLoadProcessListener;
import com.movit.platform.framework.view.progress.NumberProgressBar;

public class DialogUtils implements DownLoadProcessListener {
    public Dialog loadingDialog;
    public static final int progressHandlerIndex = 111;

    private NumberProgressBar progressBar;
    private static final DialogUtils progressDialogUtil = new DialogUtils();

    public static DialogUtils getInstants() {
        return progressDialogUtil;
    }

    public DialogUtils() {
        super();
    }

    public void showLoadingDialog(Context ctx, String title, boolean cancelable) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            dismiss();
        }

        loadingDialog = new Dialog(ctx, R.style.ImageloadingDialogStyle);// 创建自定义样式dialog
        loadingDialog.setContentView(R.layout.comm_view_loading);// 设置布局
        loadingDialog.setCanceledOnTouchOutside(cancelable);

        ImageView spaceshipImage = (ImageView) loadingDialog.findViewById(R.id.loading_img);
        TextView tipTextView = (TextView) loadingDialog.findViewById(R.id.loading_title);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                ctx, R.anim.m_loading);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(title);// 设置加载信息

        loadingDialog.show();
    }

    public void showDownLoadingDialog(Context ctx, String title,
                                      boolean cancelable) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            dismiss();
        }
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View v = inflater.inflate(R.layout.comm_dialog_for_loading, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        TextView tipTextView = (TextView) v.findViewById(R.id.loading_title);// 提示文字
        progressBar = (NumberProgressBar) v
                .findViewById(R.id.loading_progressBar);
        tipTextView.setText(title);// 设置加载信息

        loadingDialog = new Dialog(ctx, R.style.ImageloadingDialogStyle);// 创建自定义样式dialog

        loadingDialog.setCanceledOnTouchOutside(cancelable);
        // loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局

        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog.show();
    }

    public void dismiss() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

    public Dialog getLoadingDialog() {
        return loadingDialog;
    }

    @Override
    public void downLoadProcess(Handler handler, int fileSize, int downSize) {
        handler.obtainMessage(progressHandlerIndex, fileSize, downSize).sendToTarget();
    }

    public void setDownLoadProcess(int fileSize, int downSize) {
        try {
            int progress = (int) (downSize * 100 / fileSize);
            progressBar.setProgress(progress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDownLoadProcess(float progress) {
        try {
            progressBar.setProgress((int)progress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
