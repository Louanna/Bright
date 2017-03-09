
package com.movit.platform.mail.service;

import java.util.Collection;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.bean.Account.FolderMode;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.util.Utility;
import com.movit.platform.mail.preferences.Storage;
import com.movit.platform.mail.preferences.StorageEditor;

import com.fsck.k9.mail.Pusher;


public class MailService extends CoreService {
    private static final String ACTION_CHECK_MAIL = "com.fsck.k9.intent.action.MAIL_SERVICE_WAKEUP";
    private static final String ACTION_RESET = "com.fsck.k9.intent.action.MAIL_SERVICE_RESET";
    private static final String ACTION_RESCHEDULE_POLL = "com.fsck.k9.intent.action.MAIL_SERVICE_RESCHEDULE_POLL";
    private static final String ACTION_CANCEL = "com.fsck.k9.intent.action.MAIL_SERVICE_CANCEL";
    private static final String ACTION_REFRESH_PUSHERS = "com.fsck.k9.intent.action.MAIL_SERVICE_REFRESH_PUSHERS";
    private static final String ACTION_RESTART_PUSHERS = "com.fsck.k9.intent.action.MAIL_SERVICE_RESTART_PUSHERS";
    private static final String CONNECTIVITY_CHANGE = "com.fsck.k9.intent.action.MAIL_SERVICE_CONNECTIVITY_CHANGE";
    private static final String CANCEL_CONNECTIVITY_NOTICE = "com.fsck.k9.intent.action.MAIL_SERVICE_CANCEL_CONNECTIVITY_NOTICE";

    private static long nextCheck = -1;
    private static boolean pushingRequested = false;
    private static boolean pollingRequested = false;
    private static boolean syncBlocked = false;

    public static void actionReset(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESET);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static void actionRestartPushers(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESTART_PUSHERS);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static void actionReschedulePoll(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_RESCHEDULE_POLL);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static void actionCancel(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.ACTION_CANCEL);
        addWakeLockId(context, i, wakeLockId, false); // CK:Q: why should we not create a wake lock if one is not already existing like for example in actionReschedulePoll?
        context.startService(i);
    }

    public static void connectivityChange(Context context, Integer wakeLockId) {
        Intent i = new Intent();
        i.setClass(context, MailService.class);
        i.setAction(MailService.CONNECTIVITY_CHANGE);
        addWakeLockId(context, i, wakeLockId, false); // CK:Q: why should we not create a wake lock if one is not already existing like for example in actionReschedulePoll?
        context.startService(i);
    }

    @Override
    public int startService(Intent intent, int startId) {
        long startTime = System.currentTimeMillis();
        boolean oldIsSyncDisabled = isSyncDisabled();
        boolean doBackground = true;

        final boolean hasConnectivity = Utility.hasConnectivity(getApplication());
        boolean autoSync = ContentResolver.getMasterSyncAutomatically();

        MailboxController.BACKGROUND_OPS bOps = MailboxController.getBackgroundOps();

        switch (bOps) {
            case NEVER:
                doBackground = false;
                break;
            case ALWAYS:
                doBackground = true;
                break;
            case WHEN_CHECKED_AUTO_SYNC:
                doBackground = autoSync;
                break;
        }
        syncBlocked = !(doBackground && hasConnectivity);
        // MessagingController.getInstance(getApplication()).addListener(mListener);
        if (ACTION_CHECK_MAIL.equals(intent.getAction())) {
            if (hasConnectivity && doBackground) {
                PollService.startService(this);
            }
            reschedulePollInBackground(hasConnectivity, doBackground, startId, false);
        } else if (ACTION_CANCEL.equals(intent.getAction())) {
            cancel();
        } else if (ACTION_RESET.equals(intent.getAction())) {
            rescheduleAllInBackground(hasConnectivity, doBackground, startId);
        } else if (ACTION_RESTART_PUSHERS.equals(intent.getAction())) {
            reschedulePushersInBackground(hasConnectivity, doBackground, startId);
        } else if (ACTION_RESCHEDULE_POLL.equals(intent.getAction())) {
            reschedulePollInBackground(hasConnectivity, doBackground, startId, true);
        } else if (ACTION_REFRESH_PUSHERS.equals(intent.getAction())) {
            refreshPushersInBackground(hasConnectivity, doBackground, startId);
        } else if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            rescheduleAllInBackground(hasConnectivity, doBackground, startId);
        } else if (CANCEL_CONNECTIVITY_NOTICE.equals(intent.getAction())) {
            /* do nothing */
        }
        if (isSyncDisabled() != oldIsSyncDisabled) {
            MessageController.getInstance(getApplication()).systemStatusChanged();
        }
        return START_NOT_STICKY;
    }

    private void cancel() {
        Intent i = new Intent(this, MailService.class);
        i.setAction(ACTION_CHECK_MAIL);
        BootReceiver.cancelIntent(this, i);
    }

    private final static String PREVIOUS_INTERVAL = "MailService.previousInterval";
    private final static String LAST_CHECK_END = "MailService.lastCheckEnd";

    public static void saveLastCheckEnd(Context context) {
        long lastCheckEnd = System.currentTimeMillis();
        Preferences prefs = Preferences.getPreferences(context);
        Storage storage = prefs.getStorage();
        StorageEditor editor = storage.edit();
        editor.putLong(LAST_CHECK_END, lastCheckEnd);
        editor.commit();
    }

    private void rescheduleAllInBackground(final boolean hasConnectivity,
                                           final boolean doBackground, Integer startId) {

        execute(getApplication(), new Runnable() {
            @Override
            public void run() {
                reschedulePoll(hasConnectivity, doBackground, true);
                reschedulePushers(hasConnectivity, doBackground);
            }
        }, MailboxController.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
    }

    private void reschedulePollInBackground(final boolean hasConnectivity,
                                            final boolean doBackground, Integer startId, final boolean considerLastCheckEnd) {

        execute(getApplication(), new Runnable() {
            public void run() {
                reschedulePoll(hasConnectivity, doBackground, considerLastCheckEnd);
            }
        }, MailboxController.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
    }

    private void reschedulePushersInBackground(final boolean hasConnectivity,
                                               final boolean doBackground, Integer startId) {

        execute(getApplication(), new Runnable() {
            public void run() {
                reschedulePushers(hasConnectivity, doBackground);
            }
        }, MailboxController.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
    }

    private void refreshPushersInBackground(boolean hasConnectivity, boolean doBackground,
                                            Integer startId) {

//        if (hasConnectivity && doBackground) {
//            execute(getApplication(), new Runnable() {
//                public void run() {
//                     refreshPushers();
//                      schedulePushers();
//                }
//            }, MailboxController.MAIL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
//        }
    }

    private void reschedulePoll(final boolean hasConnectivity, final boolean doBackground,
                                boolean considerLastCheckEnd) {

        if (!(hasConnectivity && doBackground)) {
            nextCheck = -1;
            cancel();
            return;
        }
        Preferences prefs = Preferences.getPreferences(MailService.this);
        Storage storage = prefs.getStorage();
        int previousInterval = storage.getInt(PREVIOUS_INTERVAL, -1);
        long lastCheckEnd = storage.getLong(LAST_CHECK_END, -1);

        if (lastCheckEnd > System.currentTimeMillis()) {
            System.out.println("The database claims that the last time mail was checked was in " +
                    "the future (" + lastCheckEnd + "). To try to get things back to normal, " +
                    "the last check time has been reset to: " + System.currentTimeMillis());
            lastCheckEnd = System.currentTimeMillis();
        }

        int shortestInterval = -1;
        Account account = Preferences.getPreferences(MailService.this).getCurrentAccount();
        if (null!= account && account.getAutomaticCheckIntervalMinutes() > shortestInterval &&
                account.getFolderSyncMode() != FolderMode.NONE) {
            shortestInterval = account.getAutomaticCheckIntervalMinutes();
        }
        StorageEditor editor = storage.edit();
        editor.putInt(PREVIOUS_INTERVAL, shortestInterval);
        editor.commit();
        if (shortestInterval == -1) {
            nextCheck = -1;
            pollingRequested = false;
            cancel();
        } else {
            long delay = (shortestInterval * (60 * 1000));
            long base = (previousInterval == -1 || lastCheckEnd == -1 ||
                    !considerLastCheckEnd ? System.currentTimeMillis() : lastCheckEnd);
            long nextTime = base + delay;
            nextCheck = nextTime;
            pollingRequested = true;
            try {
                System.out.println("Next check for package " +
                        getApplication().getPackageName() + " scheduled for " +
                        new Date(nextTime));
            } catch (Exception e) {
                // I once got a NullPointerException deep in new Date();
            }
            Intent i = new Intent(this, MailService.class);
            i.setAction(ACTION_CHECK_MAIL);
            BootReceiver.scheduleIntent(MailService.this, nextTime, i);
        }
    }

    public static boolean isSyncDisabled() {
        return syncBlocked || (!pollingRequested && !pushingRequested);
    }

    private void stopPushers() {
        MessageController.getInstance(getApplication()).stopAllPushing();
        PushService.stopService(MailService.this);
    }

    private void reschedulePushers(boolean hasConnectivity, boolean doBackground) {
        stopPushers();
        if (!(hasConnectivity && doBackground)) {
            System.out.println("Not scheduling pushers:  connectivity? " + hasConnectivity +
                    " -- doBackground? " + doBackground);
            return;
        }

        setupPushers();
        schedulePushers();
    }


    private void setupPushers() {
        boolean pushing = false;
        Account account = Preferences.getPreferences(MailService.this).getCurrentAccount();
        pushing |= MessageController.getInstance(getApplication()).setupPushing(account);
        if (pushing) {
            PushService.startService(MailService.this);
        }
        pushingRequested = pushing;
    }

    private void refreshPushers() {
        try {
            long nowTime = System.currentTimeMillis();
            Collection<Pusher> pushers = MessageController.getInstance(getApplication()).getPushers();
            for (Pusher pusher : pushers) {
                long lastRefresh = pusher.getLastRefresh();
                int refreshInterval = pusher.getRefreshInterval();
                long sinceLast = nowTime - lastRefresh;
                if (sinceLast + 10000 > refreshInterval) { // Add 10 seconds to keep pushers in sync, avoid drift
                    pusher.refresh();
                    pusher.setLastRefresh(nowTime);
                }
            }
            // Whenever we refresh our pushers, send any unsent messages
            MessageController.getInstance(getApplication()).sendPendingMessages(null);

        } catch (Exception e) {
            System.out.println("Exception while refreshing pushers");
        }
    }

    private void schedulePushers() {
        int minInterval = -1;

        Collection<Pusher> pushers = MessageController.getInstance(getApplication()).getPushers();
        for (Pusher pusher : pushers) {
            int interval = pusher.getRefreshInterval();
            if (interval > 0 && (interval < minInterval || minInterval == -1)) {
                minInterval = interval;
            }
        }
        if (minInterval > 0) {
            long nextTime = System.currentTimeMillis() + minInterval;
            Intent i = new Intent(this, MailService.class);
            i.setAction(ACTION_REFRESH_PUSHERS);
            BootReceiver.scheduleIntent(MailService.this, nextTime, i);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Unused
        return null;
    }

    public static long getNextPollTime() {
        return nextCheck;
    }
}
