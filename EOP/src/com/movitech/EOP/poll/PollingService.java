package com.movitech.EOP.poll;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.movitech.EOP.manager.OAManager;

/**
 * Created by Administrator on 2016/11/17.
 */

public class PollingService extends Service {
    public static final String ACTION = "com.movitech.service.PollingService";

//    private Notification mNotification;
//    private NotificationManager mManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
//        initNotifiManager();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        new PollingThread().start();
    }

//    //初始化通知栏配置
//    private void initNotifiManager() {
//        mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        int icon = R.drawable.ic_launcher;
//        mNotification = new Notification();
//        mNotification.icon = icon;
//        mNotification.tickerText = "New Message";
//        mNotification.defaults |= Notification.DEFAULT_SOUND;
//        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
//    }

//    //弹出Notification
//    private void showNotification() {
//        mNotification.when = System.currentTimeMillis();
//        //Navigator to the new activity when click the notification title
//        Intent i = new Intent(this, MessageActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
//                Intent.FLAG_ACTIVITY_NEW_TASK);
//        mNotification.setLatestEventInfo(this,
//                getResources().getString(R.string.app_name), "You have new message!", pendingIntent);
//        mManager.notify(0, mNotification);
//    }

    /**
     * Polling thread
     * 模拟向Server轮询的异步线程
     */
    class PollingThread extends Thread {
        @Override
        public void run() {
            OAManager.getOATodoNum();
            OAManager.getERPTodoNum();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
