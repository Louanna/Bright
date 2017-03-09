package com.movitech.EOP.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.pageIndicator.MyPageIndicator;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.module.qrcode.InputCodeActivity;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity {
    private boolean isShowTour = false;
    private List<Integer> imageIds1 = new ArrayList<Integer>();
    private List<Integer> textValue1 = new ArrayList<Integer>();
    private List<Integer> textValue2 = new ArrayList<Integer>();
    private List<Integer> textValue3 = new ArrayList<Integer>();
    private List<View> mListViews = new ArrayList<View>();
    private ViewPager viewPager;
    private MyPageIndicator mIndicator;
    private LoginReceiver loginReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//            //申请WRITE_EXTERNAL_STORAGE权限
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE},
//                    EOPConstants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
//        } else {
        goNext();
//        }

        IntentFilter filter2 = new IntentFilter(CommConstants.ACTION_LOGIN_DONE);
        loginReceiver = new LoginReceiver();
        registerReceiver(loginReceiver, filter2);
    }

    //监听登录情况
    private class LoginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != intent) {
                String action = intent.getAction();
                if (CommConstants.ACTION_LOGIN_DONE.equals(action)) {
                    int result = intent.getIntExtra("result", 0);
                    if (result != CommConstants.LOGIN_SECCESS) {//登录不成功，跳转到登录页
                        MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == EOPConstants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
//                // Permission Granted
//                goNext();
//            } else {
//                // Permission Denied
//                Toast.makeText(this, "APP未获得您的全部授权，APP无法运行。", Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean isShowGuidePage() {

        if (isShowTour) {
            FrameViewsAdapter topNewsAdapter = new FrameViewsAdapter(mListViews);
            viewPager.setAdapter(topNewsAdapter);
            mIndicator.setViewPager(viewPager);
        }

        return isShowTour;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(loginReceiver);
    }

    private void goNext() {

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        CommConstants.IS_RUNNING = true;
        new FileUtils();// 创建目录
        isShowTour = MFSPHelper.getBoolean(CommConstants.IS_SHOW_TOUR, true);

        if (!isShowTour) {
            try {
                int nowVersion = getPackageManager().getPackageInfo(
                        getPackageName(), PackageManager.GET_META_DATA).versionCode;
                int originalVersion = MFSPHelper
                        .getInteger(CommConstants.ORIGINAL_VERSION);
                if (nowVersion > originalVersion) {
                    isShowTour = true;
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        setContentView(R.layout.activity_splash);
        ((EOPApplication) BaseApplication.getInstance()).getPhoneInfo();

        imageIds1.add(R.drawable.frame_1_1);
        imageIds1.add(R.drawable.frame_2_1);
        imageIds1.add(R.drawable.frame_3_1);
        imageIds1.add(R.drawable.frame_4_1);
        imageIds1.add(R.drawable.frame_5_1);

        textValue1.add(R.string.frame_1_2);
        textValue1.add(R.string.frame_2_2);
        textValue1.add(R.string.frame_3_2);
        textValue1.add(R.string.frame_4_2);
        textValue1.add(R.string.frame_5_2);

        textValue2.add(R.string.frame_1_3);
        textValue2.add(R.string.frame_2_3);
        textValue2.add(R.string.frame_3_3);
        textValue2.add(R.string.frame_4_3);
        textValue2.add(R.string.frame_5_3);

        textValue3.add(R.string.frame_1_4);
        textValue3.add(R.string.frame_1_4);
        textValue3.add(R.string.frame_1_4);
        textValue3.add(R.string.frame_1_4);
        textValue3.add(R.string.frame_1_4);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndicator = (MyPageIndicator) findViewById(R.id.indicator);

        mIndicator.setMyHandler(mHandler);

        for (int i = 0; i < imageIds1.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);

            if(i%2!=0){
                View view = inflater.inflate(
                        R.layout.dialog_welcome_viewpager_item_2, null);
                mListViews.add(view);
            }else{
                View view = inflater.inflate(
                        R.layout.dialog_welcome_viewpager_item, null);
                mListViews.add(view);
            }

        }
        if ("".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            MFSPHelper.setString(BaseApplication.SKINTYPE, "default");
        }
    }

    class FrameViewsAdapter extends PagerAdapter {
        private List<View> mListViews;

        public FrameViewsAdapter(List<View> mListViews) {
            super();
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));// 删除页卡
        }

        @Override
        public int getCount() {
            return mListViews.size();// 返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方提示这样写
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);// 添加页卡
            View view = (View) mListViews.get(position);
            ImageView imageView1 = (ImageView) view
                    .findViewById(R.id.view_pager_img1);
            TextView textView1 = (TextView) view
                    .findViewById(R.id.view_pager_tv1);
            TextView textView2 = (TextView) view
                    .findViewById(R.id.view_pager_tv2);
            TextView textView3 = (TextView) view
                    .findViewById(R.id.view_pager_tv3);

            Button button = (Button) view.findViewById(R.id.view_pager_btn);
            imageView1.setImageResource(imageIds1.get(position));
            textView1.setText(textValue1.get(position));
            textView2.setText(textValue2.get(position));
            textView3.setText(textValue3.get(position));

            if(0==position){
                textView3.setVisibility(View.VISIBLE);
            }else{
                textView3.setVisibility(View.GONE);
            }

//            if (position == mListViews.size() - 1) {
//                button.setVisibility(View.VISIBLE);
//                button.setOnClickListener(new OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        mHandler.postDelayed(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                String ip = MFSPHelper.getString("ip");
//                                if (StringUtils.empty(ip)) {
//                                    startActivity(new Intent(
//                                            SplashActivity.this,
//                                            InputCodeActivity.class));
//                                    SplashActivity.this.finish();
//                                } else {
//                                    mHandler.sendEmptyMessageDelayed(5, 1000);
//                                }
//                            }
//                        }, 50);
//
//                    }
//                });
//            }
            return mListViews.get(position);
        }
    }

}
