package com.movit.platform.framework.core.push;

import android.content.Context;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

/**
 * Created by Administrator on 2016/8/5.
 */
public abstract class MFPush extends XGPushBaseReceiver {

    private static final String LogTag = "MFPush";

    @Override
    public void onRegisterResult(Context context, int errorCode, XGPushRegisterResult xgPushRegisterResult) {
        if (context == null || xgPushRegisterResult == null) {
            return;
        }
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            Log.d(LogTag, xgPushRegisterResult + "注册成功");
            Log.d(LogTag, "xgPushRegisterResult=" + xgPushRegisterResult.toString());
        } else {
            Log.d(LogTag, xgPushRegisterResult + "注册失败，错误码：" + errorCode);
        }

    }

    @Override
    public void onUnregisterResult(Context context, int errorCode) {
        if (context == null) {
            return;
        }
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            Log.d(LogTag, "反注册成功");
        } else {
            Log.d(LogTag, "反注册失败" + errorCode);
        }
    }

    @Override
    public void onSetTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            Log.d(LogTag, tagName + "设置成功");
        } else {
            Log.d(LogTag, tagName + "设置失败,错误码：" + errorCode);
        }
    }

    @Override
    public void onDeleteTagResult(Context context, int errorCode, String tagName) {
        if (context == null) {
            return;
        }
        if (errorCode == XGPushBaseReceiver.SUCCESS) {
            Log.d(LogTag, tagName + "删除成功");
        } else {
            Log.d(LogTag, tagName + "删除失败,错误码：" + errorCode);
        }
    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        if (context == null || xgPushTextMessage == null) {
            return;
        }
        Log.d(LogTag, "收到消息:" + xgPushTextMessage.toString());
        // APP自主处理消息的过程...
        onMessageReceived(context, xgPushTextMessage.getTitle(), xgPushTextMessage.getContent(), xgPushTextMessage.getCustomContent());
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        if (context == null || xgPushClickedResult == null) {
            return;
        }

        if (XGPushClickedResult.NOTIFACTION_CLICKED_TYPE == xgPushClickedResult.getActionType()) {
            Log.d(LogTag, "通知被打开 :" + xgPushClickedResult.toString());
            // 通知在通知栏被点击啦。。。。。
            // APP自己处理点击的相关动作
            // 这个动作可以在activity的onResume也能监听
            onNotifactionClicked(context, xgPushClickedResult.getTitle(), xgPushClickedResult.getContent(), xgPushClickedResult.getCustomContent());
        } else if (XGPushClickedResult.NOTIFACTION_DELETED_TYPE == xgPushClickedResult.getActionType()) {
            Log.d(LogTag, "通知被清除 :" + xgPushClickedResult.toString());
            // 通知被清除啦。。。。
            // APP自己处理通知被清除后的相关动作

        }
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        if (context == null || xgPushShowedResult == null) {
            return;
        }
        Log.d(LogTag, "xgPushShowedResult=" + xgPushShowedResult.toString());
        // APP自主处理通知的过程...
        onNotifactionReceived(context, xgPushShowedResult.getTitle(), xgPushShowedResult.getContent(), xgPushShowedResult.getCustomContent());
    }

    //App收到消息
    public abstract void onMessageReceived(Context context, String title, String content, String customContent);

    //App收到通知
    public abstract void onNotifactionReceived(Context context, String title, String content, String customContent);

    //App点击消息通知
    public abstract void onNotifactionClicked(Context context, String title, String content, String customContent);
}
