package com.movit.platform.cloud.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;

import com.movit.platform.cloud.R;
import com.movit.platform.cloud.application.CloudApplication;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.tencent.android.tpush.XGPushManager;

/**
 * Actity 工具支持类
 *
 * @author Potter.Tao
 */
public class BaseActivity extends FragmentActivity {

    protected Context context = null;
    protected CommonHelper tools;
    protected DialogUtils progressDialogUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        tools = new CommonHelper(context);
        progressDialogUtil = DialogUtils.getInstants();

        CloudApplication.addActivity(this);
    }

    @Override
    public void onBackPressed() {
        CloudApplication.popActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            if (layout != null)
                layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
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
