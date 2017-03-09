package com.movitech.EOP.broadcast;

import android.content.Context;
import android.content.Intent;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.badge.BadgeUtils;
import com.movit.platform.framework.core.push.MFPush;
import com.movitech.EOP.activity.MainActivity;
import com.movitech.EOP.activity.SplashActivity;
import com.movitech.EOP.application.EOPApplication;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/8/5.
 */
public class XGPushReceiver extends MFPush {
    @Override
    public void onMessageReceived(Context context, String title, String content, String customContent) {
        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                if (!obj.isNull("unreadSize")) {
                    String value = obj.getString("unreadSize");
                    //APP图标数字提醒
                    BadgeUtils.setBadgeNum(context, MainActivity.class, true, "" + value, true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotifactionReceived(Context context, String title, String content, String customContent) {

        if (customContent != null && customContent.length() != 0) {
            try {
                JSONObject obj = new JSONObject(customContent);
                if (!obj.isNull("unreadSize")) {
                    String value = obj.getString("unreadSize");
                    //APP图标数字提醒
                    BadgeUtils.setBadgeNum(context, MainActivity.class, true, "" + value, true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotifactionClicked(Context context, String title, String content, String customContent) {
        try {
            if (CommConstants.allUserInfos != null && CommConstants.allOrgunits != null && CommConstants.IS_LOGIN_EOP_SERVER) {
                Intent intent = new Intent("action.intent.push.in");
                intent.setClass(context.getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
            } else {
                EOPApplication.exit();
                Intent notificationIntent = new Intent(context, SplashActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(notificationIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
