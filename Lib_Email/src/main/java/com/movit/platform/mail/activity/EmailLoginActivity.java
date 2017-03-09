package com.movit.platform.mail.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.mail.R;
import com.movit.platform.mail.controller.MailboxEntry;

/**
 * Created by air on 16/10/14.
 * email login
 */

public class EmailLoginActivity extends Activity{

    private String TAG = EmailLoginActivity.class.getSimpleName();

    private Context context;
    private String emailAddress;
    private TextView error_tip;
    private Button confirm;

    protected DialogUtils progressDialogUtil;
    private CompleteReceiver completeReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_input_password);
        context = this;
        emailAddress = getIntent().getStringExtra("emailAddress");
        showInputPassword(emailAddress);
        progressDialogUtil = DialogUtils.getInstants();
        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        IntentFilter filter = new IntentFilter();
        filter.addAction(MailboxEntry.CHECKLOGIN);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(completeReceiver,filter);
    }

    //输入密码框
    public void showInputPassword(String emailAddress){
        TextView account = (TextView) findViewById(R.id.tv_email_account);
        account.setText(emailAddress);
        final EditText etPassword = (EditText) findViewById(R.id.et_email_password);
        error_tip = (TextView) findViewById(R.id.tv_email_error);
        confirm = (Button) findViewById(R.id.bt_email);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                if(!TextUtils.isEmpty(password)){
                    progressDialogUtil.showLoadingDialog(context,getString(R.string.pull_to_refresh_footer_refreshing_label),false);
                    error_tip.setVisibility(View.GONE);
                    confirm.setClickable(false);
                    MFSPHelper.setString(CommConstants.EMAILPASSWORD,password);
                    MailboxEntry.needSendBroadcast = true;
                    MailboxEntry.CheckEmailAccountTask();
                }else {
                    ToastUtils.showToast(context,context.getString(R.string.email_password_empty_error));
                }
            }
        });
    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialogUtil.dismiss();
            confirm.setClickable(true);
            abortBroadcast();
            String loginState = intent.getStringExtra("loginState");
            if("Success".equals(loginState)){
                finish();
//                MailboxEntry.tryEnter();
                setResult(MailboxEntry.CHECKLOGIN_REQUESTCODE,loginState,null);
            }else if("error".equals(loginState)){
                error_tip.setVisibility(View.VISIBLE);
            }else {
                if(!TextUtils.isEmpty(loginState)){
                    ToastUtils.showToast(context,loginState);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(completeReceiver);
    }
}
