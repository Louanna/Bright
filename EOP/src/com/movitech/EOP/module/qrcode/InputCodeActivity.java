package com.movitech.EOP.module.qrcode;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.module.qrcode.zxing.view.ViewfinderView;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.Base64Utils;
import com.movit.platform.framework.view.CusDialog;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.CompanyInfoable;
import com.movitech.EOP.activity.LoginActivity;

public class InputCodeActivity extends MipcaActivity {

    private ImageView back;
    private ImageView topRight;
    private TextView title;
    private RelativeLayout topRelativeLayout;

    LinearLayout chageCodeLayout;
    RelativeLayout inputLayout;
    LinearLayout tips;
    TextView changeToInput;
    TextView changeToCode;
    TextView done;
    EditText inputEditText;

    private InputMethodManager mInputMethodManager;
    Pattern pattern = Pattern
            .compile("^((0|(?:[1-9]\\d{0,1})|(?:1\\d{2})|(?:2[0-4]\\d)|(?:25[0-5]))\\.){3}((?:[1-9]\\d{0,1})|(?:1\\d{2})|(?:2[0-4]\\d)|(?:25[0-5]))$");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_capture);
        mInputMethodManager = (InputMethodManager) this
                .getSystemService(INPUT_METHOD_SERVICE);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        topRelativeLayout = (RelativeLayout) findViewById(R.id.common_top_layout);
        back = (ImageView) findViewById(R.id.common_top_left);
        title = (TextView) findViewById(R.id.common_top_title);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        tips = (LinearLayout) findViewById(R.id.tips);
        viewfinderView.setViewStyle(1);
        title.setText("APP授权码");
        topRelativeLayout.setBackgroundColor(Color.TRANSPARENT);
        back.setImageResource(R.drawable.close);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight.setImageResource(R.drawable.ico_help);
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(InputCodeActivity.this,
                        ManualActivity.class));
            }
        });

        chageCodeLayout = (LinearLayout) findViewById(R.id.login_capture_change_to_input_layout);
        inputLayout = (RelativeLayout) findViewById(R.id.login_capture_input_layout);
        inputEditText = (EditText) findViewById(R.id.login_capture_input);
        changeToInput = (TextView) findViewById(R.id.login_capture_change_to_input);
        changeToCode = (TextView) findViewById(R.id.login_capture_change_to_code);
        done = (TextView) findViewById(R.id.login_capture_done);

        changeToInput.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                inputLayout.setVisibility(View.VISIBLE);
                viewfinderView.setVisibility(View.GONE);
                tips.setVisibility(View.GONE);
                inputEditText.requestFocus();
                mInputMethodManager.showSoftInput(inputEditText, 0);
                chageCodeLayout.setVisibility(View.GONE);
            }
        });
        changeToCode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                inputLayout.setVisibility(View.GONE);
                viewfinderView.setVisibility(View.VISIBLE);
                tips.setVisibility(View.VISIBLE);
                mInputMethodManager.hideSoftInputFromWindow(
                        inputEditText.getWindowToken(), 0);
                chageCodeLayout.setVisibility(View.VISIBLE);
            }
        });
        done.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDialogUtil.showLoadingDialog(context,  getString(R.string.waiting), false);
                String result = "";
                String input = inputEditText.getText().toString();
                if (input.startsWith("http://")) {
                    result = input.substring(7);
                } else {
                    try {
                        byte[] bytes = Base64Utils.decode(input);
                        String json = new String(bytes);
                        JSONObject object = new JSONObject(json);
                        result = object.getString("ip");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.v("ip", result);
                Matcher matcher = pattern.matcher(result);
                if (matcher.matches()) {
                    mHandler.obtainMessage(1, result).sendToTarget();
                } else {
                    new Thread(new checkUrlCanOpen(result, 1)).start();
                }
            }
        });
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String result = (String) msg.obj;
                    MFSPHelper.setString("ip", result);
                    MFSPHelper.setString("port", "");
                    MFSPHelper.setString("cmsPort", "");
                    CommConstants.initHost(InputCodeActivity.this);
                    mInputMethodManager.hideSoftInputFromWindow(
                            inputEditText.getWindowToken(), 0);
                    new Thread(new CompanyInfoable(context, mHandler)).start();
                    break;
                case 2:
                    String decodeResult = (String) msg.obj;
                    MFSPHelper.setString("ip", decodeResult);
                    MFSPHelper.setString("port", "");
                    MFSPHelper.setString("cmsPort", "");
                    CommConstants.initHost(InputCodeActivity.this);
                    new Thread(new CompanyInfoable(context, mHandler)).start();
                    break;
                case 3:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, "您输入的APP授权码有误！");
                    break;
                case 4:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, "扫描的APP授权码有误！");
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (handler != null) {
                                handler.sendEmptyMessage(R.id.restart_preview);
                            }
                        }
                    }, 1000);
                    break;
                case 5:
                    progressDialogUtil.dismiss();
                    CusDialog dialogUtil = CusDialog.getInstance();
                    dialogUtil.showCustomDialog(context);
                    dialogUtil.setSimpleDialog("服务器验证通过，请联系管理员获取登录账号完成登录！");
                    dialogUtil.setConfirmClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(InputCodeActivity.this,
                                    LoginActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            InputCodeActivity.this.finish();
                        }
                    });
                    break;
                case 6:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, "请检查APP授权码的有效性！");
                    break;
                default:
                    break;
            }
        }

    };

    /**
     * 处理扫描结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        if (resultString.equals("")) {
            Toast.makeText(InputCodeActivity.this, "扫描出错啦！",
                    Toast.LENGTH_SHORT).show();
        } else {
            progressDialogUtil.showLoadingDialog(context,  getString(R.string.waiting), false);
            String decodeResult = "";
            try {
                byte[] bytes = Base64Utils.decode(resultString);
                String json = new String(bytes);
                JSONObject object = new JSONObject(json);
                decodeResult = object.getString("ip");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v("ip", decodeResult);
            Matcher matcher = pattern.matcher(decodeResult);
            if (matcher.matches()) {
                MFSPHelper.setString("ip", decodeResult);
                MFSPHelper.setString("port", "");
                MFSPHelper.setString("cmsPort", "");
                CommConstants.initHost(this);
                mHandler.obtainMessage(2, decodeResult).sendToTarget();
            } else {
                new Thread(new checkUrlCanOpen(decodeResult, 2)).start();
            }
        }
    }

    class checkUrlCanOpen implements Runnable {
        String result;
        int type;

        public checkUrlCanOpen(String result, int type) {
            super();
            this.result = result;
            this.type = type;
        }

        @Override
        public void run() {
            String url = "http://" + result;
            try {
                URL url2 = new URL(url);
                url2.openStream();
                mHandler.obtainMessage(type, result).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(type + 2);
            }
        }
    }

}