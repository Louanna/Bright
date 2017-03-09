package com.movitech.EOP.base;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.tencent.android.tpush.XGPushManager;

/**
 * Actity 工具支持类
 *
 * @author Potter.Tao
 */
public class EOPBaseActivity extends IMBaseActivity {

    protected Context context = null;
    protected CommonHelper tools;
    protected DialogUtils progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!CommConstants.IS_RUNNING) {
            EOPApplication.restartApp(getApplicationContext());
            return;
        }
        super.onCreate(savedInstanceState);
        context = this;
        tools = new CommonHelper(context);
        progressDialogUtil = DialogUtils.getInstants();

        EOPApplication.addActivity(this);
    }

    @Override
    public void onBackPressed() {
        EOPApplication.popActivity(this);
    }

    @Override
    protected void onResume() {

        if (!CommConstants.IS_RUNNING) {
            EOPApplication.restartApp(getApplicationContext());
            return;
        }

        super.onResume();

        try {
            NotificationManager notiManager = (NotificationManager) context
                    .getSystemService(context.NOTIFICATION_SERVICE);
            notiManager.cancelAll();

            XGPushManager.onActivityStarted(this);// 某些手机进去之后会跳到登陆界面
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }

    @Override
    protected void onPause() {
        super.onPause();
        XGPushManager.onActivityStoped(this);
    }

    @Override
    public void onDestroy() {
        progressDialogUtil.dismiss();
        super.onDestroy();
    }

    public Context getContext() {
        return context;
    }

}
