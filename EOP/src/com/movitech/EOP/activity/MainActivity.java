package com.movitech.EOP.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.helper.ServiceHelper;
import com.movit.platform.im.module.record.activity.ChatRecordsActivity;
import com.movit.platform.mail.controller.MailboxEntry;
import com.movit.platform.mail.util.MyEmailBroadcast;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.broadcast.OffLineReceiver;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.home.fragment.HomeFragemnt;
import com.movitech.EOP.module.mine.activity.MyActivity;
import com.movitech.EOP.poll.PollingService;
import com.movitech.EOP.poll.PollingUtils;
import com.movitech.EOP.view.MyRadioButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.Call;

public class MainActivity extends EOPBaseActivity {

    private TextView mainPoint;
    private TextView txt_dian1;
    private RadioGroup rgs;
    public HomeFragemnt homeFragemnt;

    private MyRadioButton radioTxl, radio_im;
    private MyRadioButton radioWo;

    private OffLineReceiver offLineReceiver;

    public final static String UNREAD_PROCESS = "receiver_unread_process";
    private final BroadcastReceiver UnreadNumReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UNREAD_PROCESS)) {
                if (homeFragemnt != null) {
                    homeFragemnt.setProcessUnreadNum(intent.getIntExtra("oaUnreadNum", 0), intent.getIntExtra("erpUnreadNum", 0));
                }
            }
        }
    };

    @Override
    public void setRedPoint(int point) {
        if (0 == point) {
            mainPoint.setVisibility(View.GONE);
        } else {
            mainPoint.setText(point + "");
            mainPoint.setVisibility(View.VISIBLE);
            //APP图标数字提醒
//                BadgeUtils.setBadgeNum(MainActivity.this, MainActivity.class, true, "" + point, true);
        }
    }

    private final BroadcastReceiver EmailBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(MyEmailBroadcast.UNREAD_EMAIL)) {
                int i = intent.getIntExtra("unread", 0);
                System.out.println("i=" + i);
                //tab菜单不显示数字提醒
//                if (i <= 0) {
//                    txt_dian1.setVisibility(View.INVISIBLE);
//                } else {
//                    txt_dian1.setVisibility(View.VISIBLE);
//                    txt_dian1.setText(String.valueOf(i));
//                }
                if (homeFragemnt != null) {
                    homeFragemnt.getUnread(i);
                }
            }
        }
    };

    private CompleteReceiver completeReceiver;

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String loginState = intent.getStringExtra("loginState");
            if ("Success".equals(loginState)) {
                MailboxEntry.tryEnter();
            }
//            else if("error".equals(loginState)){
//                String emailAddress = MFSPHelper.getString(CommConstants.EMAIL_ADDRESS);
//                if(!TextUtils.isEmpty(emailAddress)){
//                    Intent intent1 = new Intent(context,EmailLoginActivity.class);
//                    intent1.putExtra("emailAddress",emailAddress);
//                    startActivityForResult(intent1, MailboxEntry.CHECKLOGIN_REQUESTCODE);
//                }
//            }else {
//                if(!TextUtils.isEmpty(loginState)){
////                    ToastUtils.showToast(context,loginState);
//                    Log.d("MainActivity", "loginState: "+loginState);
//                }
//            }
        }
    }

    private IntentFilter filter;

    BitmapDrawable tab_contact_normal, tab_contact_press, tab_home_normal, tab_home_press,
            tab_mine_normal, tab_mine_press;


    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String value = (String) msg.obj;
                    Intent intent = new Intent(CommConstants.SIMPLE_LOGIN_ACTION);
                    intent.putExtra("body", value);
                    intent.putExtra("type", CommConstants.TYPE_JUST_TIPS);
                    intent.setPackage(context.getPackageName());
                    sendBroadcast(intent);
                    break;
                case 99:
                    break;
                case 4:
                    setGPSAlarm();
                    break;
                default:
                    break;
            }
        }
    };

    public void setGPSAlarm() {
        String upTime = MFSPHelper.getString("upTime");
        String downTime = MFSPHelper.getString("downTime");
        String alarmUpTime = MFSPHelper.getString("alarmUpTime");
        String alarmDownTime = MFSPHelper.getString("alarmDownTime");
        if ("".equals(alarmUpTime) || "".equals(alarmDownTime)) {

            if ("".equals(alarmUpTime) && StringUtils.notEmpty(upTime)) {
                Calendar c = DateUtils.str2Calendar(upTime, "HH:mm:ss");
                if (c != null) {
                    c.add(Calendar.MINUTE, -5);
                    String timeString = DateUtils.date2Str(c, "HH:mm");
                    alarmUpTime = timeString;
                }
            }
            if ("".equals(alarmDownTime) && StringUtils.notEmpty(downTime)) {
                Calendar c2 = DateUtils.str2Calendar(downTime, "HH:mm:ss");
                String timeString2 = DateUtils.date2Str(c2, "HH:mm");
                alarmDownTime = timeString2;
            }
            MFSPHelper.setString("alarmUpTime", alarmUpTime);
            MFSPHelper.setString("alarmDownTime", alarmDownTime);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eop_activity_main);
        MailboxEntry.init(this);
        mainPoint = (TextView) findViewById(R.id.main_dian);
        txt_dian1 = (TextView) findViewById(R.id.txt_dian1);
        rgs = (RadioGroup) findViewById(R.id.main_radiogroup);
//        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
//            String skinType = MFSPHelper.getString(BaseApplication.SKINTYPE);
//            File dir = context.getDir("theme", Context.MODE_PRIVATE);
//            File skinDir = new File(dir, skinType);
//            BitmapDrawable tab_button_bg = new BitmapDrawable(getResources(),
//                    skinDir + "/tab_button_bg.png");
//            tab_contact_normal = new BitmapDrawable(getResources(), skinDir
//                    + "/tab_contact_normal.png");
//            tab_contact_press = new BitmapDrawable(getResources(), skinDir
//                    + "/tab_contact_press.png");
//            tab_home_normal = new BitmapDrawable(getResources(), skinDir
//                    + "/tab_home_normal.png");
//            tab_home_press = new BitmapDrawable(getResources(), skinDir
//                    + "/tab_home_press.png");
//            tab_mine_normal = new BitmapDrawable(getResources(), skinDir
//                    + "/tab_mine_normal.png");
//            tab_mine_press = new BitmapDrawable(getResources(), skinDir
//                    + "/tab_mine_press.png");
//            rgs.setBackground(tab_button_bg);
//            ((MyRadioButton) rgs.getChildAt(0)).setTextColor(Color
//                    .parseColor(BaseApplication.TOP_COLOR));
//            ((MyRadioButton) rgs.getChildAt(0))
//                    .setCompoundDrawablesWithIntrinsicBounds(null,
//                            tab_home_press, null, null);
//            ((MyRadioButton) rgs.getChildAt(1))
//                    .setCompoundDrawablesWithIntrinsicBounds(null,
//                            tab_contact_normal, null, null);
//            ((MyRadioButton) rgs.getChildAt(2))
//                    .setCompoundDrawablesWithIntrinsicBounds(null,
//                            tab_mine_normal, null, null);
//
//            ((MyRadioButton) rgs.getChildAt(0))
//                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView,
//                                                     boolean isChecked) {
//
//                        }
//                    });
//            ((MyRadioButton) rgs.getChildAt(1))
//                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView,
//                                                     boolean isChecked) {
//
//                        }
//                    });
//            ((MyRadioButton) rgs.getChildAt(2))
//                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                        @Override
//                        public void onCheckedChanged(CompoundButton buttonView,
//                                                     boolean isChecked) {
//                        }
//                    });
//        }

        radioTxl = (MyRadioButton) findViewById(R.id.radio_txl);
        radioTxl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, OrgActivity.class);
                i.putExtra("IS_FROM_ORG", "Y");
                startActivity(i);
            }
        });

        radio_im = (MyRadioButton) findViewById(R.id.radio_im);
        radio_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ChatRecordsActivity.class);
                startActivity(i);
            }
        });


        radioWo = (MyRadioButton) findViewById(R.id.radio_wo);
        radioWo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyActivity.class));
            }
        });


        homeFragemnt = new HomeFragemnt();
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.add(R.id.main_frame, homeFragemnt);
        ft.commit();

        // 检查license是否过期
        checkLicense();

        super.registReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(CommConstants.SIMPLE_LOGIN_ACTION);
        offLineReceiver = new OffLineReceiver();
        this.registerReceiver(offLineReceiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyEmailBroadcast.UNREAD_EMAIL);
        registerReceiver(EmailBroadcastReceiver, intentFilter);

        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UNREAD_PROCESS);
        registerReceiver(UnreadNumReceiver, intentFilter2);

        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(MailboxEntry.CHECKLOGINMAIN);
        filter2.setPriority(1);
        registerReceiver(completeReceiver, filter2);

        //Start polling service
        PollingUtils.startPollingService(this, 60, PollingService.class, PollingService.ACTION);

        OAManager.getBpiURL();
    }

    // 检查license是否过期
    private void checkLicense() {

        OkHttpUtils.getWithToken()
                .url(CommConstants.URL_CHECK_LICENSE)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean ok = jsonObject.getBoolean("ok");
                        if (!ok) {
                            String value = jsonObject.getString("objValue");
                            handler.obtainMessage(1, value).sendToTarget();
                        }
                    }
                });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("action.intent.push.in".equals(action)) {
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(UnreadNumReceiver);
        unregisterReceiver(EmailBroadcastReceiver);
        unregisterReceiver(offLineReceiver);
        unregisterReceiver(completeReceiver);

        super.unRegistReceiver();

        MailboxEntry.destroy();

        //Stop polling service
        PollingUtils.stopPollingService(this, PollingService.class, PollingService.ACTION);

        super.onDestroy();
    }

    boolean backFlag = false;

    @Override
    public void onBackPressed() {
        if (!backFlag) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.clicked_again_for_exit), Toast.LENGTH_LONG).show();
        } else {
            CommConstants.isExit = true;
            new ServiceHelper().stopService();
            ((EOPApplication) BaseApplication.getInstance()).clean();
            EOPApplication.exit();// activity finish并且断开xmpp连接
            // android.os.Process.killProcess(android.os.Process.myPid());
        }
        backFlag = true;
        backHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                backFlag = false;
            }
        }, 1500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    Handler backHandler = new Handler();

    public RadioGroup getRgs() {
        return rgs;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
