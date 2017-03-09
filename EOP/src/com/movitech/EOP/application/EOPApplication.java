package com.movitech.EOP.application;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;
import com.baidu.mapapi.SDKInitializer;
import com.facebook.stetho.Stetho;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.module.workbench.constants.LanguageType;

import java.util.Locale;
import java.util.Stack;

import cn.sharesdk.framework.ShareSDK;

public class EOPApplication extends BaseApplication {

    private static Stack<Activity> activityStack;
    private static boolean useEoopApi = false;
    public static Locale LOCALE;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;

        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        useEoopApi = appInfo.metaData.getBoolean("USE_EOOP_API", false);

        setUIController(new EOPUIController());
        setManagerFactory(new ManagerFactory(this));

        //百度地图
        SDKInitializer.initialize(this);

        Stetho.initializeWithDefaults(this);

        //分享
        ShareSDK.initSDK(this);

        String lan = MFSPHelper.getString("language");
        if(TextUtils.isEmpty(lan)){
            LOCALE = getResources().getConfiguration().locale;
        }else {
            switch (lan) {
                case LanguageType.CHINESE:
                    LOCALE = Locale.SIMPLIFIED_CHINESE;
                    break;
                case LanguageType.TAIWAN:
                    LOCALE = Locale.TAIWAN;
                    break;
                case LanguageType.ENGLISH:
                    LOCALE = Locale.ENGLISH;
                    break;
                case LanguageType.JAPANESE:
                    LOCALE = Locale.JAPANESE;
                    break;
                default:
                    LOCALE = Locale.SIMPLIFIED_CHINESE;
                    break;
            }
        }

        setLanguage();
    }

    private void setLanguage(){
        Resources resources = getResources();// 获得res资源对象
        Configuration config = resources
                .getConfiguration();// 获得设置对象
        DisplayMetrics dm = resources
                .getDisplayMetrics();// 获得屏幕参数：主要是分辨率，像素等。
        config.locale = LOCALE;
        resources.updateConfiguration(config, dm);
    }

    public void clean() {
        CommConstants.isExit = false;
        if (CommConstants.allUserInfos != null) {
            CommConstants.allUserInfos.clear();
        }
        if (CommConstants.allOrgunits != null) {
            CommConstants.allOrgunits.clear();
        }
    }

    // 添加Activity到容器中
    public static void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    public static void restartApp(Context context) {
//        Intent intent = new Intent();
//        intent.setClass(context, SplashActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        context.startActivity(intent);
    }

    // 遍历所有Activity并finish
    public static void exit() {
        try {
            while (true) {
                if (null != activityStack  && !activityStack.isEmpty() && null !=currentActivity()) {
                    popActivity(currentActivity());
                }else{
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void popActivity(Activity activity) {
        if (activityStack != null) {
            if (activity != null) {
                activity.finish();
                activityStack.remove(activity);
            }
        } else {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    public static Activity currentActivity() {
        Activity activity = null;
        try {
            if (activityStack != null) {
                activity = activityStack.lastElement();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return activity;
    }

    /**
     * 封装Toast提示框信息，显示在中间
     *
     * @param context
     * @param words
     */
    public static void showToast(Context context, String words) {
        Toast toast = Toast.makeText(context, words, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 获取IMEI号，IESI号，手机型号
     */
    public void getPhoneInfo() {
        TelephonyManager mTm = (TelephonyManager) this
                .getSystemService(TELEPHONY_SERVICE);
        String mtype = android.os.Build.MODEL; // 手机型号
        String mtyb = android.os.Build.BRAND;// 手机品牌
        CommConstants.PHONEBRAND = mtyb + " " + mtype;
        CommConstants.PHONEVERSION = android.os.Build.VERSION.RELEASE;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        BitmapAjaxCallback.clearCache();
    }

}
