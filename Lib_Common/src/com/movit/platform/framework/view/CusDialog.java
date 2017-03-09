package com.movit.platform.framework.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.movit.platform.common.R;

public class CusDialog {
    Dialog customDialog;
    TextView title;
    TextView txt;
    TextView content;
    Button cancle;
    Button confirm;

    private static CusDialog instance;
    private CusDialog (){}

    public static CusDialog getInstance() {
        if (instance == null) {
            instance = new CusDialog();
        }
        return instance;
    }

    public void showCustomDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(
                R.layout.comm_dialog_for_others, null);
        title = (TextView) dialogView.findViewById(R.id.dialog_title);
        txt = (TextView) dialogView.findViewById(R.id.dialog_txt);
        content = (TextView) dialogView.findViewById(R.id.dialog_txt_detail);
        confirm = (Button) dialogView.findViewById(R.id.dialog_btn);
        cancle = (Button) dialogView.findViewById(R.id.dialog_cancel_btn);

        customDialog = new Dialog(context, R.style.ImageloadingDialogStyle);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(dialogView);
        customDialog.setCancelable(false);
        customDialog.setCanceledOnTouchOutside(false);

        customDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        customDialog.show();
    }

    public void showVersionDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(
                R.layout.comm_dialog_for_version, null);
        title = (TextView) dialogView.findViewById(R.id.dialog_title);
        txt = (TextView) dialogView.findViewById(R.id.dialog_txt);
        content = (TextView) dialogView.findViewById(R.id.dialog_txt_detail);
        confirm = (Button) dialogView.findViewById(R.id.dialog_btn);
        cancle = (Button) dialogView.findViewById(R.id.dialog_cancel_btn);

        customDialog = new Dialog(context, R.style.ImageloadingDialogStyle);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(dialogView);
        customDialog.setCancelable(false);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();
    }

    public void setCancleClickListener(View.OnClickListener clickListener) {
        cancle.setOnClickListener(clickListener);
    }

    public void setConfirmClickListener(View.OnClickListener clickListener) {
        confirm.setOnClickListener(clickListener);
    }

    public boolean isShowing() {
        return customDialog.isShowing();
    }

    public void dismiss() {
        customDialog.dismiss();
    }

    public void setUpdateDialog(String newChanges, String forceUpdate) {
        txt.setText("您有新版本更新,请点击更新获得更好体验.");
        if (newChanges != null) {
            content.setText(newChanges.toString());
        }
        if ("Y".equalsIgnoreCase(forceUpdate)) {
            cancle.setVisibility(View.GONE);
        } else {
            cancle.setVisibility(View.VISIBLE);
        }
        confirm.setText("更新");
        cancle.setText("忽略");
    }

    public void setSimpleDialog(String str) {
        title.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);
        cancle.setVisibility(View.GONE);
        content.setText(str);
    }

    public void setWebDialog(String str) {
        title.setVisibility(View.GONE);
        txt.setVisibility(View.GONE);
        content.setText(str);
    }
    public void setTitleDialog(String str) {
        title.setText(str);
        txt.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
        cancle.setVisibility(View.GONE);
    }
}
