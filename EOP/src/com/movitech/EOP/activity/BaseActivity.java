package com.movitech.EOP.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.broadcast.DownloadOrgReceiver;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.task.LoginTask;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.SkinManager;
import com.movit.platform.framework.manager.UpgradeManager;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.qrcode.InputCodeActivity;

/**
 * Created by Administrator on 2016/7/21.
 */
public abstract class BaseActivity extends Activity {

    private DownloadOrgReceiver receiver;
    protected TelephonyManager mTelephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        //注册广播
        IntentFilter filter = new IntentFilter(CommConstants.ACTION_ORGUNITION_DONE);
        receiver = new DownloadOrgReceiver(this, new DownloadOrgReceiver.DownloadOrgCallBack() {
            // 初始化结束后的操作
            @Override
            public void afterInitCommon() {

                OAManager.createToken(null);
                startActivity(new Intent(BaseActivity.this, MainActivity.class));
                finish();
            }
        });
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAPKVersion();
    }

    private void checkAPKVersion() {
        UpgradeManager.checkAPKVersion(this, new UpgradeManager.Callback() {
            @Override
            public void onError() {
//                loadSkin();
                mHandler.sendEmptyMessage(1);
            }

            //检查完毕
            public void onAfter() {
//                loadSkin();
                mHandler.sendEmptyMessage(1);
            }

            //忽略此次版本升级
            public void onCancel() {
//                loadSkin();
                mHandler.sendEmptyMessage(1);
            }

            //升级版本
            public void doUpgrade(final Context context, final String downloadUrl) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)));
            }
        });
    }

    private void loadSkin() {
        SkinManager.loadSkin(this, new SkinManager.Callback() {
            @Override
            public void onError() {
                if (MFSPHelper.getString(BaseApplication.SKINTYPE).isEmpty()) {
                    MFSPHelper.setString(BaseApplication.SKINTYPE, "default");
                }
                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void onDownloadError() {
                if (MFSPHelper.getString(BaseApplication.SKINTYPE).isEmpty()) {
                    MFSPHelper.setString(BaseApplication.SKINTYPE, "default");
                }
                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void onUpzipError() {
                if (MFSPHelper.getString(BaseApplication.SKINTYPE).isEmpty()) {
                    MFSPHelper.setString(BaseApplication.SKINTYPE, "default");
                }
                mHandler.sendEmptyMessage(1);
            }

            @Override
            public void toLoadSkin() {
                mHandler.sendEmptyMessage(1);
            }
        });
    }

    protected Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mHandler.postDelayed(gotoLoginAct, 200);
                    break;
                case 5:
                    MFSPHelper.setBoolean(CommConstants.IS_SHOW_TOUR, false);
                    try {
                        MFSPHelper.setInteger(
                                CommConstants.ORIGINAL_VERSION,
                                getPackageManager().getPackageInfo(
                                        getPackageName(),
                                        PackageManager.GET_META_DATA).versionCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!"LoginActivity".equalsIgnoreCase(BaseActivity.this.getClass().getSimpleName())) {
                        startActivity(new Intent(BaseActivity.this,
                                LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        BaseActivity.this.finish();
                    }
                    break;
                case 6:// autoLogin
                    LoginInfo loginConfig = new CommonHelper(BaseActivity.this).getLoginConfig();
                    String name = loginConfig.getmUserInfo().getEmpAdname();
                    String password = loginConfig.getPassword();
                    loginEOPServer(CommConstants.URL_LOGIN_VERIFY, name, password);
                    break;
                default:
                    break;
            }
        }
    };

    protected abstract boolean isShowGuidePage();

    private Runnable gotoLoginAct = new Runnable() {

        @Override
        public void run() {
            if (!isShowGuidePage()) {
                String ip = MFSPHelper.getString("ip");
                if (StringUtils.empty(ip)) {
                    startActivity(new Intent(BaseActivity.this,
                            InputCodeActivity.class));
                    finish();
                } else {
                    boolean isAutoLogin = MFSPHelper.getBoolean(CommConstants.IS_AUTOLOGIN,
                            false);
                    if (isAutoLogin) {
                        mHandler.sendEmptyMessageDelayed(6, 500);
                    } else {
                        mHandler.sendEmptyMessageDelayed(5, 1000);
                    }
                }
            }
        }
    };

    protected void loginEOPServer(String url, String name, String password) {

        if (CommConstants.URL_LOGIN_VERIFY.equalsIgnoreCase(url)) {
            //如果是账号登陆，则需要判断用户名密码是否为空
            //如果是游客登录，用户名密码默认为空
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
                MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, false);
                ToastUtils.showToast(this, getResources().getString(R.string.username_and_password_can_not_null));
                return;
            }
        }

        LoginTask loginTask = new LoginTask(this, name, password);
        loginTask.loginEOPServer(url, name, password, mTelephonyManager);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
