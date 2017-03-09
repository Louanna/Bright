package com.movitech.EOP.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.CusRelativeLayout;
import com.movit.platform.framework.widget.CusRelativeLayout.OnSizeChangedListener;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.module.gesture.GestureVerifyActivity;
import com.movitech.EOP.module.qrcode.InputCodeActivity;
import com.movitech.EOP.module.workbench.activity.WebViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

public class LoginActivity extends BaseActivity implements OnClickListener {

    private Button mLoginBtn;
    private EditText mAccountEt, mPasswordEt;
    private CheckBox remenberPwdBtn, autoLoginBtn;
    private LoginInfo loginConfig;
    private TextView regist, version, company;

    private CusRelativeLayout relativeLayout;
    private ScrollView scrollView;
    private Handler h = new Handler();

    private LinearLayout settingLayout;
    private TextView visitorTv, serverTv;
    private RelativeLayout loginBottom;
    private ImageView goInputCode, logo, server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eop_activity_login);

        //很早之前不知道谁这么写的
        //TODO 建议变量的控制由各模块自行控制，待优化
        ((EOPApplication) BaseApplication.getInstance()).clean();

        loginConfig = new CommonHelper(this).getLoginConfig();

        iniView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        HttpManager.getJson(CommConstants.URL_EOP_COMPANY, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
//                iniView();
                otherEvent();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean ok = jsonObject.getBoolean("ok");
                    if (ok) {
                        JSONObject object = jsonObject.getJSONObject("objValue");
                        CommConstants.productName = object.getString("productName");
                        CommConstants.companyName = object.getString("name");
                        CommConstants.companyLogo = object.getString("companyLogo");
                        company.setText(CommConstants.companyName);
//                        aQuery.cache(CommConstants.URL_DOWN + CommConstants.companyLogo, 0);

                        MFAQueryHelper.cacheImage(CommConstants.URL_DOWN + CommConstants.companyLogo);
                    }
                }

//                iniView();
                otherEvent();
            }
        });
    }

    @Override
    protected boolean isShowGuidePage() {
        return false;
    }

    private void otherEvent() {
        // 校验SD卡
        ActivityUtils.checkMemoryCard(this);
        // 检测网络和版本
        boolean hasNetWork = ActivityUtils.hasNetWorkConection(this);
        if (!hasNetWork) {
            ActivityUtils.openWirelessSet(this);
        }

        // 判断跳转手势密码
        String code = MFSPHelper.getString("GestureCode");
        if (StringUtils.notEmpty(code)) {
            if (!CommConstants.isGestureOK) {
                startActivity(new Intent(LoginActivity.this,
                        GestureVerifyActivity.class).putExtra("type", "verify"));
            } else {
                mPasswordEt.setText(loginConfig.getPassword());
                remenberPwdBtn.setChecked(true);
                autoLoginBtn.setChecked(true);
                loginEOPServer(CommConstants.URL_LOGIN_VERIFY);
            }
        } else {
            boolean isAutoLogin = MFSPHelper.getBoolean(CommConstants.IS_AUTOLOGIN,
                    false);
            boolean isRemember = MFSPHelper.getBoolean(CommConstants.IS_REMEMBER, false);
            if (isRemember) {
                mPasswordEt.setText(loginConfig.getPassword());
                remenberPwdBtn.setChecked(true);
            } else {
                remenberPwdBtn.setChecked(false);
            }
            if (isAutoLogin) {
                autoLoginBtn.setChecked(true);
                loginEOPServer(CommConstants.URL_LOGIN_VERIFY);
            } else {
                autoLoginBtn.setChecked(false);
            }
        }
    }

    private void iniView() {

        isAllowConfiguration();

        mAccountEt = (EditText) findViewById(R.id.account_input);
        mPasswordEt = (EditText) findViewById(R.id.password);
        mLoginBtn = (Button) findViewById(R.id.login);
        mAccountEt.setText(loginConfig.getmUserInfo().getEmpAdname());
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        relativeLayout = (CusRelativeLayout) findViewById(R.id.rl);
        version = (TextView) findViewById(R.id.version);
        remenberPwdBtn = (CheckBox) findViewById(R.id.remenber_pwd);
        autoLoginBtn = (CheckBox) findViewById(R.id.auto_login);
        regist = (TextView) findViewById(R.id.regist);
        goInputCode = (ImageView) findViewById(R.id.login_input_code);
        company = (TextView) findViewById(R.id.company);
        logo = (ImageView) findViewById(R.id.logo);
        server = (ImageView) findViewById(R.id.iv_server);
        loginBottom = (RelativeLayout) findViewById(R.id.login_bottom);

        try {

            boolean canScan = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_SCAN", false);
            boolean canRegist = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_REGIST", false);
            boolean allowConfigurationTop = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("ALLOW_CONFIGURATION_TOP", false);
            String versionname = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getString("CHANNEL_VERSION_NAME");

            if (canScan) {
                goInputCode.setVisibility(View.VISIBLE);
            } else {
                goInputCode.setVisibility(View.GONE);
            }
            if (canRegist) {
                regist.setVisibility(View.VISIBLE);
            } else {
                regist.setVisibility(View.GONE);
            }
            if (allowConfigurationTop) {
                server.setVisibility(View.VISIBLE);
            } else {
                server.setVisibility(View.GONE);
            }

            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String versionName = info.versionName;

            version.setText(versionname + " v" + versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (StringUtils.notEmpty(CommConstants.companyName)) {
            company.setText(CommConstants.companyName);
        }
        if (StringUtils.notEmpty(CommConstants.companyLogo)) {
//            aQuery.id(logo).image(
//                    aQuery.getCachedImage(CommConstants.URL_DOWN
//                            + CommConstants.companyLogo));

            MFAQueryHelper.setImageViewFromCache(R.id.logo,CommConstants.URL_DOWN + CommConstants.companyLogo);
        }
        remenberPwdBtn
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        String code = MFSPHelper.getString("GestureCode");
                        if (StringUtils.empty(code)) {
                            if (isChecked) {
                                MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, true);
                            } else {
                                MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, false);
                            }
                        }
                    }
                });
        autoLoginBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                String code = MFSPHelper.getString("GestureCode");
                if (StringUtils.empty(code)) {
                    if (isChecked) {
                        MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, true);
                        MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, true);
                        remenberPwdBtn.setChecked(true);
                    } else {
                        MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                    }
                }

            }
        });

        mLoginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEOPServer(CommConstants.URL_LOGIN_VERIFY);
            }
        });
        mAccountEt.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeScrollView();
                return false;
            }
        });
        mPasswordEt.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                changeScrollView();
                return false;
            }
        });

        regist.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        WebViewActivity.class);
                intent.putExtra("URL", CommConstants.URL_EOP_REGISTER);
                intent.putExtra("TITLE", getResources().getString(R.string.regist));
                startActivity(intent);
            }
        });

        goInputCode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,
                        InputCodeActivity.class));
            }
        });

        relativeLayout.setOnSizeChangedListener(new OnSizeChangedListener() {

            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if (oldh > h) {
                    loginBottom.setVisibility(View.GONE);
                } else if (h > oldh) {
                    LoginActivity.this.h.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            loginBottom.setVisibility(View.VISIBLE);
                        }
                    }, 100);
                }
            }
        });
        server.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ServerListActivity.class));
            }
        });
    }

    private void isAllowConfiguration() {

        settingLayout = (LinearLayout) findViewById(R.id.ll_setting);

        if (CommConstants.ALLOW_CONFIGURATION) {
            settingLayout.setVisibility(View.VISIBLE);

            visitorTv = (TextView) findViewById(R.id.tv_visitor);
            serverTv = (TextView) findViewById(R.id.tv_server);
            visitorTv.setOnClickListener(this);
            serverTv.setOnClickListener(this);

        } else {
            settingLayout.setVisibility(View.GONE);
        }

    }

    /**
     * 使ScrollView指向底部
     */
    private void changeScrollView() {
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, scrollView.getHeight());
            }
        }, 300);
    }

    private void loginEOPServer(String url) {
        String name = mAccountEt.getText().toString().trim();
        String password = mPasswordEt.getText().toString().trim();
        loginEOPServer(url,name, password);
    }

    @Override
    public void onDestroy() {
        DialogUtils.getInstants().dismiss();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        CommConstants.isExit = true;
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_visitor:
                loginEOPServer(CommConstants.URL_EOP_API + "r/sys/rest/visitorLogin");
                break;
            case R.id.tv_server:
                startActivity(new Intent(this, ServerListActivity.class));
                break;
        }
    }
}
