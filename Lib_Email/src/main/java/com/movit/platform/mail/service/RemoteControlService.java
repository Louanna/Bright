package com.movit.platform.mail.service;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.preferences.Storage;
import com.movit.platform.mail.preferences.StorageEditor;

import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.R;
import com.movit.platform.mail.bean.Account.FolderMode;
import com.movit.platform.mail.controller.MailboxController.BACKGROUND_OPS;

import com.movit.platform.mail.remotecontrol.K9RemoteControl;
import static com.movit.platform.mail.remotecontrol.K9RemoteControl.*;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;

public class RemoteControlService extends CoreService {
    private final static String RESCHEDULE_ACTION = "com.fsck.k9.service.RemoteControlService.RESCHEDULE_ACTION";
    private final static String PUSH_RESTART_ACTION = "com.fsck.k9.service.RemoteControlService.PUSH_RESTART_ACTION";

    private final static String SET_ACTION = "com.fsck.k9.service.RemoteControlService.SET_ACTION";

    public static void set(Context context, Intent i, Integer wakeLockId) {
        //  Intent i = new Intent();
        i.setClass(context, RemoteControlService.class);
        i.setAction(RemoteControlService.SET_ACTION);
        addWakeLockId(context, i, wakeLockId, true);
        context.startService(i);
    }

    public static final int REMOTE_CONTROL_SERVICE_WAKE_LOCK_TIMEOUT = 20000;

    @Override
    public int startService(final Intent intent, final int startId) {
        final Preferences preferences = Preferences.getPreferences(this);
        if (RESCHEDULE_ACTION.equals(intent.getAction())) {
            MailService.actionReschedulePoll(this, null);
        }
        if (PUSH_RESTART_ACTION.equals(intent.getAction())) {
            MailService.actionRestartPushers(this, null);
        } else if (RemoteControlService.SET_ACTION.equals(intent.getAction())) {
            execute(getApplication(), new Runnable() {
                public void run() {
                    try {
                        boolean needsReschedule = false;
                        boolean needsPushRestart = false;
                        String uuid = intent.getStringExtra(K9_ACCOUNT_UUID);
                        boolean allAccounts = intent.getBooleanExtra(K9_ALL_ACCOUNTS, false);
                        List<Account> accounts = preferences.getAccounts();
                        for (Account account : accounts) {
                            //warning: account may not be isAvailable()
                            if (allAccounts || account.getUuid().equals(uuid)) {
                                String notificationEnabled = intent.getStringExtra(K9_NOTIFICATION_ENABLED);
                                String ringEnabled = intent.getStringExtra(K9_RING_ENABLED);
                                String vibrateEnabled = intent.getStringExtra(K9_VIBRATE_ENABLED);
                                String pushClasses = intent.getStringExtra(K9_PUSH_CLASSES);
                                String pollClasses = intent.getStringExtra(K9_POLL_CLASSES);
                                String pollFrequency = intent.getStringExtra(K9_POLL_FREQUENCY);

                                if (notificationEnabled != null) {
                                    account.setNotifyNewMail(Boolean.parseBoolean(notificationEnabled));
                                }
                                if (ringEnabled != null) {
                                    account.getNotificationSetting().setRing(Boolean.parseBoolean(ringEnabled));
                                }
                                if (vibrateEnabled != null) {
                                    account.getNotificationSetting().setVibrate(Boolean.parseBoolean(vibrateEnabled));
                                }
                                if (pushClasses != null) {
                                    needsPushRestart |= account.setFolderPushMode(FolderMode.valueOf(pushClasses));
                                }
                                if (pollClasses != null) {
                                    needsReschedule |= account.setFolderSyncMode(FolderMode.valueOf(pollClasses));
                                }
                                if (pollFrequency != null) {
                                    String[] allowedFrequencies = getResources().getStringArray(R.array.account_settings_check_frequency_values);
                                    for (String allowedFrequency : allowedFrequencies) {
                                        if (allowedFrequency.equals(pollFrequency)) {
                                            Integer newInterval = Integer.parseInt(allowedFrequency);
                                            needsReschedule |= account.setAutomaticCheckIntervalMinutes(newInterval);
                                        }
                                    }
                                }
                                account.save(Preferences.getPreferences(RemoteControlService.this));
                            }
                        }

                        String backgroundOps = intent.getStringExtra(K9_BACKGROUND_OPERATIONS);
                        if (K9RemoteControl.K9_BACKGROUND_OPERATIONS_ALWAYS.equals(backgroundOps)
                                || K9RemoteControl.K9_BACKGROUND_OPERATIONS_NEVER.equals(backgroundOps)
                                || K9RemoteControl.K9_BACKGROUND_OPERATIONS_WHEN_CHECKED_AUTO_SYNC.equals(backgroundOps)) {
                            BACKGROUND_OPS newBackgroundOps = BACKGROUND_OPS.valueOf(backgroundOps);
                            boolean needsReset = MailboxController.setBackgroundOps(newBackgroundOps);
                            needsPushRestart |= needsReset;
                            needsReschedule |= needsReset;
                        }
                        Storage storage = preferences.getStorage();
                        StorageEditor editor = storage.edit();
                        MailboxController.save(editor);
                        editor.commit();

                        if (needsReschedule) {
                            Intent i = new Intent(RemoteControlService.this, RemoteControlService.class);
                            i.setAction(RESCHEDULE_ACTION);
                            long nextTime = System.currentTimeMillis() + 10000;
                            BootReceiver.scheduleIntent(RemoteControlService.this, nextTime, i);
                        }
                        if (needsPushRestart) {
                            Intent i = new Intent(RemoteControlService.this, RemoteControlService.class);
                            i.setAction(PUSH_RESTART_ACTION);
                            long nextTime = System.currentTimeMillis() + 10000;
                            BootReceiver.scheduleIntent(RemoteControlService.this, nextTime, i);
                        }
                    } catch (Exception e) {
                        Toast toast = Toast.makeText(RemoteControlService.this, e.getMessage(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }
            , RemoteControlService.REMOTE_CONTROL_SERVICE_WAKE_LOCK_TIMEOUT, startId);
        }

        return START_NOT_STICKY;
    }

}
