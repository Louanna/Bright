package com.movit.platform.im.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.im.service.XMPPService;
import com.movit.platform.im.utils.ChatTools;

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {

        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            CommConstants.isGestureOK = false;
            ChatTools.leaveChat();
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 聊天服务
                    Intent chatServer = new Intent(context,XMPPService.class);
                    context.stopService(chatServer);
                }
            }, 1000);
        }
    }
}
