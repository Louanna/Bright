package com.movit.platform.cloud.application;

import android.app.Activity;

import com.movit.platform.cloud.model.ProgressButton;
import com.movit.platform.common.application.BaseApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


public class CloudApplication extends BaseApplication {

    private static Stack<Activity> activityStack;
    private static CloudApplication mApplication;

    public static HashMap<Object,String> downloadIdMap = new HashMap<>();
    public static HashMap<String,ProgressButton>  buttonMap = new HashMap<>();
    public static ArrayList<HashMap<String,String>> waitList = new ArrayList<>();

    public synchronized static CloudApplication getInstance() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

    }

    // 添加Activity到容器中
    public static void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    // 遍历所有Activity并finish
    public static void exit() {
        try {
            while (true) {
                if (activityStack != null) {
                    if (activityStack.isEmpty()) {
                        break;
                    }
                    Activity activity = currentActivity();
                    if (activity == null) {
                        break;
                    }
                    popActivity(activity);
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


}
