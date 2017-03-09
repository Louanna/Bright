package com.movit.platform.im.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.im.helper.XmppHelper;
import com.movit.platform.im.manager.XmppManager;

/**
 * 重连接服务.
 *
 * @author shimiso
 */
public class ReConnectService extends Service {

    @Override
    public void onCreate() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(reConnectXmpp, mFilter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(reConnectXmpp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver reConnectXmpp = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (ActivityUtils.hasNetWorkConection(context)) {
                    if (!XmppManager.getInstance().isConnected()) {
                        //proDialogUtils.showLoadingDialog(context, "请稍候", false);
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                XmppManager.getInstance().disconnect();
                                //XMPP连接已断，重新login xmpp
                                XmppHelper XmppHelper = new XmppHelper(getApplicationContext());
                                XmppHelper.loginXMPP();
                            }
                        }, 2000);
                    }

                } else {
                    Toast.makeText(context, "网络断开,用户已离线!", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // 聊天服务
                            Intent chatServer = new Intent(context, XMPPService.class);
                            context.stopService(chatServer);
                        }
                    }, 100);
                }
            }
        }
    };

}
