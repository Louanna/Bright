package com.movit.platform.mail.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.controller.MessagingListener;
import com.fsck.k9.mail.power.TracingPowerManager;
import com.fsck.k9.mail.power.TracingPowerManager.TracingWakeLock;

import java.util.HashMap;
import java.util.Map;

public class PollService extends CoreService {
    private static String START_SERVICE = "com.fsck.k9.service.PollService.startService";
    private static String STOP_SERVICE = "com.fsck.k9.service.PollService.stopService";

    private Listener mListener = new Listener();

    public static void startService(Context context) {
        Intent i = new Intent();
        i.setClass(context, PollService.class);
        i.setAction(PollService.START_SERVICE);
        addWakeLock(context, i);
        context.startService(i);
    }

    public static void stopService(Context context) {
        Intent i = new Intent();
        i.setClass(context, PollService.class);
        i.setAction(PollService.STOP_SERVICE);
        addWakeLock(context, i);
        context.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setAutoShutdown(false);
    }

    @Override
    public int startService(Intent intent, int startId) {
        if (START_SERVICE.equals(intent.getAction())) {
            MessageController controller = MessageController.getInstance(getApplication());
            Listener listener = (Listener)controller.getCheckMailListener();
            if (listener == null) {
                mListener.setStartId(startId);
                mListener.wakeLockAcquire();
                controller.setCheckMailListener(mListener);
                controller.checkMail(this, null, false, false, mListener);
            } else {
                listener.setStartId(startId);
                listener.wakeLockAcquire();
            }
        } else if (STOP_SERVICE.equals(intent.getAction())) {
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    class Listener extends MessagingListener {
        Map<String, Integer> accountsChecked = new HashMap<String, Integer>();
        private TracingWakeLock wakeLock = null;
        private int startId = -1;

        // wakelock strategy is to be very conservative.  If there is any reason to release, then release
        // don't want to take the chance of running wild
        public synchronized void wakeLockAcquire() {
            TracingWakeLock oldWakeLock = wakeLock;

            TracingPowerManager pm = TracingPowerManager.getPowerManager(PollService.this);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PollService wakeLockAcquire");
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire(MailboxController.WAKE_LOCK_TIMEOUT);

            if (oldWakeLock != null) {
                oldWakeLock.release();
            }

        }
        public synchronized void wakeLockRelease() {
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
            }
        }
        @Override
        public void checkMailStarted(Context context, Account account) {
            accountsChecked.clear();
        }

        @Override
        public void checkMailFailed(Context context, Account account, String reason) {
            release();
        }

        @Override
        public void synchronizeMailboxFinished(
            Account account,
            String folder,
            int totalMessagesInMailbox,
            int numNewMessages) {
            if (account.isNotifyNewMail()) {
                Integer existingNewMessages = accountsChecked.get(account.getUuid());
                if (existingNewMessages == null) {
                    existingNewMessages = 0;
                }
                accountsChecked.put(account.getUuid(), existingNewMessages + numNewMessages);
            }
        }

        private void release() {

            MessageController controller = MessageController.getInstance(getApplication());
            controller.setCheckMailListener(null);
            MailService.saveLastCheckEnd(getApplication());
            MailService.actionReschedulePoll(PollService.this, null);
            wakeLockRelease();
            stopSelf(startId);
        }

        @Override
        public void checkMailFinished(Context context, Account account) {
            release();
        }
        public int getStartId() {
            return startId;
        }
        public void setStartId(int startId) {
            this.startId = startId;
        }
    }

}
