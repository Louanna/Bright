package com.movit.platform.mail.controller;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.Time;

import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.K9MailLib;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.BinaryTempFileBody;
import com.fsck.k9.mail.ssl.LocalKeyStore;
import com.movit.platform.mail.BuildConfig;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.preferences.Storage;
import com.movit.platform.mail.preferences.StorageEditor;
import com.movit.platform.mail.provider.UnreadWidgetProvider;
import com.movit.platform.mail.service.BootReceiver;
import com.movit.platform.mail.service.MailService;
import com.movit.platform.mail.service.ShutdownReceiver;
import com.movit.platform.mail.service.StorageGoneReceiver;
import com.movit.platform.mail.util.FontSizes;
import com.movit.platform.mail.util.PRNGFixes;
import com.movit.platform.mail.preferences.Preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Jamison on 2016/6/6.
 */
public class MailboxController {

    public static interface ApplicationAware {
        /**
         * Called when the Application instance is available and ready.
         *
         * @param application The application instance. Never <code>null</code>.
         * @throws Exception
         */
        void initializeComponent(Application application);
    }

    public static Application app;
    private static final String DATABASE_VERSION_CACHE = "database_version_cache";
    /**
     * Key used to store the last known database version of the accounts' databases.
     *
     * @see #DATABASE_VERSION_CACHE
     */
    private static final String KEY_LAST_ACCOUNT_DATABASE_VERSION = "last_account_database_version";
    /**
     * Components that are interested in knowing when the K9 instance is
     * available and ready.
     *
     * @see ApplicationAware
     */
    private static final List<ApplicationAware> observers = new ArrayList<ApplicationAware>();

    private static boolean sInitialized = false;

    public enum BACKGROUND_OPS {
        ALWAYS, NEVER, WHEN_CHECKED_AUTO_SYNC
    }

    private static final FontSizes fontSizes = new FontSizes();

    private static BACKGROUND_OPS backgroundOps = BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC;
    /**
     * If this is enabled than logging that normally hides sensitive information
     * like passwords will show that information.
     */
    public static boolean DEBUG_SENSITIVE = false;
    public static final String ERROR_FOLDER_NAME = "K9mail-errors";

    private static final String SAVE_PATH = "/EOPMailDownLoad";

    private static SharedPreferences sDatabaseVersionCache;

    private static boolean mAnimations = true;

    private static boolean mConfirmSpam = false;
    private static boolean mConfirmDeleteFromNotification = true;

    private static NotificationHideSubject sNotificationHideSubject = NotificationHideSubject.NEVER;

    /**
     * Controls when to hide the subject in the notification area.
     */
    public enum NotificationHideSubject {
        ALWAYS,
        WHEN_LOCKED,
        NEVER
    }

    private static NotificationQuickDelete sNotificationQuickDelete = NotificationQuickDelete.NEVER;

    /**
     * Controls behaviour of delete button in notifications.
     */
    public enum NotificationQuickDelete {
        ALWAYS,
        FOR_SINGLE_MSG,
        NEVER
    }

    private static LockScreenNotificationVisibility sLockScreenNotificationVisibility =
            LockScreenNotificationVisibility.MESSAGE_COUNT;

    public enum LockScreenNotificationVisibility {
        EVERYTHING,
        SENDERS,
        MESSAGE_COUNT,
        APP_NAME,
        NOTHING
    }

    /**
     * Controls when to use the message list split view.
     */
    public enum SplitViewMode {
        ALWAYS,
        NEVER,
        WHEN_IN_LANDSCAPE
    }

    private static String mAttachmentDefaultPath = "";
    private static String mQuietTimeStarts = null;
    private static String mQuietTimeEnds = null;

    public static int EmailPreviewLine = 2;
    private static int mMessageListPreviewLines = 2;
    private static int mContactNameColor = 0xff00008f;

    private static boolean mShowCorrespondentNames = true;
    private static boolean mShowContactName = false;
    private static boolean mChangeContactNameColor = false;
    private static boolean mMessageViewFixedWidthFont = false;
    private static boolean mMessageViewReturnToList = false;
    private static boolean mGesturesEnabled = true;
    private static boolean mUseVolumeKeysForNavigation = false;
    private static boolean mUseVolumeKeysForListNavigation = false;
    private static boolean mStartIntegratedInbox = false;
    private static boolean mMeasureAccounts = true;
    private static boolean mAutofitWidth;
    private static boolean mQuietTimeEnabled = false;
    private static boolean mNotificationDuringQuietTimeEnabled = true;
    private static boolean sDatabasesUpToDate = false;
    private static boolean isActive = false;

    private static Account.SortType mSortType;
    private static Map<Account.SortType, Boolean> mSortAscending = new HashMap<Account.SortType, Boolean>();
    private static SplitViewMode sSplitViewMode = SplitViewMode.NEVER;
    /**
     * For use when displaying that no folder is selected
     */
    public static final String FOLDER_NONE = "-NONE-";
    public static final String LOCAL_UID_PREFIX = "K9LOCAL:";
    public static final String IDENTITY_HEADER = K9MailLib.IDENTITY_HEADER;

    /**
     * Specifies how many messages will be shown in a folder by default. This number is set
     * on each new folder and can be incremented with "Load more messages..." by the
     * VISIBLE_LIMIT_INCREMENT
     */
    public static final int DEFAULT_VISIBLE_LIMIT = 25;

    /**
     * The maximum size of an attachment we're willing to download (either View or Save)
     * Attachments that are base64 encoded (most) will be about 1.375x their actual size
     * so we should probably factor that in. A 5MB attachment will generally be around
     * 6.8MB downloaded but only 5MB saved.
     */
    public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (128 * 1024 * 1024);


    /* How many times should K-9 try to deliver a message before giving up
     * until the app is killed and restarted
     */

    public static final int MAX_SEND_ATTEMPTS = 5;

    /**
     * Max time (in millis) the wake lock will be held for when background sync is happening
     */
    public static final int WAKE_LOCK_TIMEOUT = 600000;

    public static final int MANUAL_WAKE_LOCK_TIMEOUT = 120000;

    public static final int PUSH_WAKE_LOCK_TIMEOUT = K9MailLib.PUSH_WAKE_LOCK_TIMEOUT;

    public static final int MAIL_SERVICE_WAKE_LOCK_TIMEOUT = 60000;

    public static final int BOOT_RECEIVER_WAKE_LOCK_TIMEOUT = 60000;


    public static class Intents {

        public static class EmailReceived {
            public static final String ACTION_EMAIL_RECEIVED = BuildConfig.APPLICATION_ID + ".intent.action.EMAIL_RECEIVED";
            public static final String ACTION_EMAIL_DELETED = BuildConfig.APPLICATION_ID + ".intent.action.EMAIL_DELETED";
            public static final String ACTION_REFRESH_OBSERVER = BuildConfig.APPLICATION_ID + ".intent.action.REFRESH_OBSERVER";
            public static final String EXTRA_ACCOUNT = BuildConfig.APPLICATION_ID + ".intent.extra.ACCOUNT";
            public static final String EXTRA_FOLDER = BuildConfig.APPLICATION_ID + ".intent.extra.FOLDER";
            public static final String EXTRA_SENT_DATE = BuildConfig.APPLICATION_ID + ".intent.extra.SENT_DATE";
            public static final String EXTRA_FROM = BuildConfig.APPLICATION_ID + ".intent.extra.FROM";
            public static final String EXTRA_TO = BuildConfig.APPLICATION_ID + ".intent.extra.TO";
            public static final String EXTRA_CC = BuildConfig.APPLICATION_ID + ".intent.extra.CC";
            public static final String EXTRA_BCC = BuildConfig.APPLICATION_ID + ".intent.extra.BCC";
            public static final String EXTRA_SUBJECT = BuildConfig.APPLICATION_ID + ".intent.extra.SUBJECT";
            public static final String EXTRA_FROM_SELF = BuildConfig.APPLICATION_ID + ".intent.extra.FROM_SELF";
        }

        public static class Share {
            /*
             * We don't want to use EmailReceived.EXTRA_FROM ("com.fsck.k9.intent.extra.FROM")
             * because of different semantics (String array vs. string with comma separated
             * email addresses)
             */
            public static final String EXTRA_FROM = BuildConfig.APPLICATION_ID + ".intent.extra.SENDER";
        }
    }

    /**
     * Called throughout the application when the number of accounts has changed. This method
     * enables or disables the Compose activity, the boot receiver and the service based on
     * whether any accounts are configured.
     */
    public static void setServicesEnabled(Context context) {
        setServicesEnabled(context, true, null);
    }

    private static void setServicesEnabled(Context context, boolean enabled, Integer wakeLockId) {

        PackageManager pm = context.getPackageManager();

        if (!enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            /*
             * If no accounts now exist but the service is still enabled we're about to disable it
             * so we'll reschedule to kill off any existing alarms.
             */
            MailService.actionReset(context, wakeLockId);
        }
        //enable or disable component
        Class<?>[] classes = {BootReceiver.class, MailService.class};

        for (Class<?> clazz : classes) {

            boolean alreadyEnabled = pm.getComponentEnabledSetting(new ComponentName(context, clazz)) ==
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

            if (enabled != alreadyEnabled) {
                pm.setComponentEnabledSetting(
                        new ComponentName(context, clazz),
                        enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        }

        if (enabled && pm.getComponentEnabledSetting(new ComponentName(context, MailService.class)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            /*
             * And now if accounts do exist then we've just enabled the service and we want to
             * schedule alarms for the new accounts.
             */
            MailService.actionReset(context, wakeLockId);
        }

    }

    public static void registerReceivers() {
        final StorageGoneReceiver receiver = new StorageGoneReceiver();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");

        final BlockingQueue<Handler> queue = new SynchronousQueue<Handler>();

        // starting a new thread to handle unmount events
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    queue.put(new Handler());
                } catch (InterruptedException e) {
                }
                Looper.loop();
            }

        }, "Unmount-thread").start();

        try {
            final Handler storageGoneHandler = queue.take();
            app.registerReceiver(receiver, filter, null, storageGoneHandler);
        } catch (InterruptedException e) {
            System.out.println("Unable to register unmount receiver");
        }
        app.registerReceiver(new ShutdownReceiver(), new IntentFilter(Intent.ACTION_SHUTDOWN));
    }

    public static void save(StorageEditor editor) {

        editor.putString("backgroundOperations", backgroundOps.name());
        editor.putBoolean("animations", mAnimations);
        editor.putBoolean("gesturesEnabled", mGesturesEnabled);
        editor.putBoolean("useVolumeKeysForNavigation", mUseVolumeKeysForNavigation);
        editor.putBoolean("useVolumeKeysForListNavigation", mUseVolumeKeysForListNavigation);
        editor.putBoolean("autofitWidth", mAutofitWidth);
        editor.putBoolean("quietTimeEnabled", mQuietTimeEnabled);
        editor.putBoolean("notificationDuringQuietTimeEnabled", mNotificationDuringQuietTimeEnabled);
        editor.putString("quietTimeStarts", mQuietTimeStarts);
        editor.putString("quietTimeEnds", mQuietTimeEnds);
        editor.putBoolean("startIntegratedInbox", mStartIntegratedInbox);
        editor.putBoolean("measureAccounts", mMeasureAccounts);
        editor.putInt("messageListPreviewLines", mMessageListPreviewLines);
        editor.putBoolean("showCorrespondentNames", mShowCorrespondentNames);
        editor.putBoolean("showContactName", mShowContactName);
        editor.putBoolean("changeRegisteredNameColor", mChangeContactNameColor);
        editor.putInt("registeredNameColor", mContactNameColor);
        editor.putBoolean("messageViewFixedWidthFont", mMessageViewFixedWidthFont);
        editor.putBoolean("messageViewReturnToList", mMessageViewReturnToList);
        editor.putBoolean("confirmSpam", mConfirmSpam);
        editor.putBoolean("confirmDeleteFromNotification", mConfirmDeleteFromNotification);
        editor.putString("sortTypeEnum", mSortType.name());
        editor.putBoolean("sortAscending", mSortAscending.get(mSortType));
        editor.putString("notificationHideSubject", sNotificationHideSubject.toString());
        editor.putString("notificationQuickDelete", sNotificationQuickDelete.toString());
        editor.putString("lockScreenNotificationVisibility", sLockScreenNotificationVisibility.toString());
        editor.putString("attachmentdefaultpath", mAttachmentDefaultPath);
        editor.putString("splitViewMode", sSplitViewMode.name());
        fontSizes.save(editor);
    }

    public static void init(Application application) {
        app = application;
        PRNGFixes.apply();
        checkCachedDatabaseVersion();
        Preferences prefs = Preferences.getPreferences(app);
        loadPrefs(prefs);
        initLoadPath();
        /*
         * We have to give MimeMessage a temp directory because File.createTempFile(String, String)
         * doesn't work in Android and MimeMessage does not have access to a Context.
         */
        BinaryTempFileBody.setTempDirectory(app.getCacheDir());
        LocalKeyStore.setKeyStoreLocation(app.getDir("KeyStore", Context.MODE_PRIVATE).toString());
        registerReceivers();
        MessageController.getInstance(app).addListener(new MessagingListener() {
            private void broadcastIntent(String action, Account account, String folder, Message message) {
                try {
                    Uri uri = Uri.parse("email://messages/" + account.getAccountNumber() + "/" + Uri.encode(folder) + "/" + Uri.encode(message.getUid()));
                    Intent intent = new Intent(action, uri);
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_ACCOUNT, account.getDescription());
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_FOLDER, folder);
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_SENT_DATE, message.getSentDate());
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_FROM, Address.toString(message.getFrom()));
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_TO, Address.toString(message.getRecipients(Message.RecipientType.TO)));
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_CC, Address.toString(message.getRecipients(Message.RecipientType.CC)));
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_BCC, Address.toString(message.getRecipients(Message.RecipientType.BCC)));
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_SUBJECT, message.getSubject());
                    intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_FROM_SELF, account.isAnIdentity(message.getFrom()));
                    app.sendBroadcast(intent);
                } catch (MessagingException e) {
                    System.out.println("Error: action=" + action
                            + " account=" + account.getDescription()
                            + " folder=" + folder
                            + " message uid=" + message.getUid()
                    );
                }
            }

            private void updateUnreadWidget() {
                try {
                    UnreadWidgetProvider.updateUnreadCount(app);
                } catch (Exception e) {

                }
            }

            @Override
            public void synchronizeMailboxRemovedMessage(Account account, String folder, Message message) {
                broadcastIntent(MailboxController.Intents.EmailReceived.ACTION_EMAIL_DELETED, account, folder, message);
                updateUnreadWidget();
            }

            @Override
            public void messageDeleted(Account account, String folder, Message message) {
                broadcastIntent(MailboxController.Intents.EmailReceived.ACTION_EMAIL_DELETED, account, folder, message);
                updateUnreadWidget();
            }

            @Override
            public void synchronizeMailboxNewMessage(Account account, String folder, Message message) {
                broadcastIntent(MailboxController.Intents.EmailReceived.ACTION_EMAIL_RECEIVED, account, folder, message);
                updateUnreadWidget();
            }

            @Override
            public void folderStatusChanged(Account account, String folderName,
                                            int unreadMessageCount) {

                updateUnreadWidget();

                // let observers know a change occurred
                Intent intent = new Intent(MailboxController.Intents.EmailReceived.ACTION_REFRESH_OBSERVER, null);
                intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_ACCOUNT, account.getDescription());
                intent.putExtra(MailboxController.Intents.EmailReceived.EXTRA_FOLDER, folderName);
                app.sendBroadcast(intent);
            }

        });
        notifyObservers();
    }

    private static void initLoadPath() {
        if (!TextUtils.isEmpty(mAttachmentDefaultPath)) {
            File file = new File(mAttachmentDefaultPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public static void checkCachedDatabaseVersion() {
        sDatabaseVersionCache = app.getSharedPreferences(DATABASE_VERSION_CACHE, Context.MODE_PRIVATE);

        int cachedVersion = sDatabaseVersionCache.getInt(KEY_LAST_ACCOUNT_DATABASE_VERSION, 0);

        if (cachedVersion >= LocalStore.DB_VERSION) {
            setDatabasesUpToDate(false);
        }
    }


    public static void loadPrefs(Preferences prefs) {
        Storage storage = prefs.getStorage();
        DEBUG_SENSITIVE = storage.getBoolean("enableSensitiveLogging", false);
        mAnimations = storage.getBoolean("animations", true);
        mGesturesEnabled = storage.getBoolean("gesturesEnabled", false);
        mUseVolumeKeysForNavigation = storage.getBoolean("useVolumeKeysForNavigation", false);
        mUseVolumeKeysForListNavigation = storage.getBoolean("useVolumeKeysForListNavigation", false);
        mStartIntegratedInbox = storage.getBoolean("startIntegratedInbox", false);
        mMeasureAccounts = storage.getBoolean("measureAccounts", true);
        mMessageListPreviewLines = storage.getInt("messageListPreviewLines", 2);
        mAutofitWidth = storage.getBoolean("autofitWidth", true);
        mQuietTimeEnabled = storage.getBoolean("quietTimeEnabled", false);
        mNotificationDuringQuietTimeEnabled = storage.getBoolean("notificationDuringQuietTimeEnabled", true);
        mQuietTimeStarts = storage.getString("quietTimeStarts", "21:00");
        mQuietTimeEnds = storage.getString("quietTimeEnds", "7:00");
        mShowCorrespondentNames = storage.getBoolean("showCorrespondentNames", true);
        mShowContactName = storage.getBoolean("showContactName", false);
        mChangeContactNameColor = storage.getBoolean("changeRegisteredNameColor", false);
        mContactNameColor = storage.getInt("registeredNameColor", 0xff00008f);
        mMessageViewFixedWidthFont = storage.getBoolean("messageViewFixedWidthFont", false);
        mMessageViewReturnToList = storage.getBoolean("messageViewReturnToList", false);
        mConfirmSpam = storage.getBoolean("confirmSpam", false);
        mConfirmDeleteFromNotification = storage.getBoolean("confirmDeleteFromNotification", true);
        mAttachmentDefaultPath = storage.getString("attachmentdefaultpath",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + SAVE_PATH);
        try {
            String value = storage.getString("sortTypeEnum", Account.DEFAULT_SORT_TYPE.name());
            mSortType = Account.SortType.valueOf(value);
        } catch (Exception e) {
            mSortType = Account.DEFAULT_SORT_TYPE;
        }
        boolean sortAscending = storage.getBoolean("sortAscending", Account.DEFAULT_SORT_ASCENDING);
        mSortAscending.put(mSortType, sortAscending);
        String notificationHideSubject = storage.getString("notificationHideSubject", null);
        if (notificationHideSubject == null) {
            // If the "notificationHideSubject" setting couldn't be found, the app was probably
            // updated. Look for the old "keyguardPrivacy" setting and map it to the new enum.
            sNotificationHideSubject = (storage.getBoolean("keyguardPrivacy", false)) ?
                    NotificationHideSubject.WHEN_LOCKED : NotificationHideSubject.NEVER;
        } else {
            sNotificationHideSubject = NotificationHideSubject.valueOf(notificationHideSubject);
        }
        String notificationQuickDelete = storage.getString("notificationQuickDelete", null);
        if (notificationQuickDelete != null) {
            sNotificationQuickDelete = NotificationQuickDelete.valueOf(notificationQuickDelete);
        }
        String lockScreenNotificationVisibility = storage.getString("lockScreenNotificationVisibility", null);
        if (lockScreenNotificationVisibility != null) {
            sLockScreenNotificationVisibility = LockScreenNotificationVisibility.valueOf(lockScreenNotificationVisibility);
        }
        String splitViewMode = storage.getString("splitViewMode", null);
        if (splitViewMode != null) {
            sSplitViewMode = SplitViewMode.valueOf(splitViewMode);
        }
        fontSizes.load(storage);
        try {
            setBackgroundOps(BACKGROUND_OPS.valueOf(storage.getString(
                    "backgroundOperations",
                    BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC.name())));
        } catch (Exception e) {
            setBackgroundOps(BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC);
        }
    }

    /**
     * since Android invokes Application.onCreate() only after invoking all
     * other components' onCreate(), here is a way to notify interested
     * component that the application is available and ready
     */
    public static void notifyObservers() {
        synchronized (observers) {
            for (final ApplicationAware aware : observers) {
                try {
                    aware.initializeComponent(app);
                } catch (Exception e) {

                }
            }
            sInitialized = true;
            observers.clear();
        }
    }

    public static void registerApplicationAware(final ApplicationAware component) {
        synchronized (observers) {
            if (sInitialized) {
                component.initializeComponent(app);
            } else if (!observers.contains(component)) {
                observers.add(component);
            }
        }
    }

    public static BACKGROUND_OPS getBackgroundOps() {
        return backgroundOps;
    }

    public static boolean setBackgroundOps(BACKGROUND_OPS backgroundOps) {
        BACKGROUND_OPS oldBackgroundOps = MailboxController.backgroundOps;
        MailboxController.backgroundOps = backgroundOps;
        return backgroundOps != oldBackgroundOps;
    }

    public static boolean setBackgroundOps(String nbackgroundOps) {
        return setBackgroundOps(BACKGROUND_OPS.valueOf(nbackgroundOps));
    }

    public static boolean gesturesEnabled() {
        return mGesturesEnabled;
    }

    public static void setGesturesEnabled(boolean gestures) {
        mGesturesEnabled = gestures;
    }

    public static boolean useVolumeKeysForNavigationEnabled() {
        return mUseVolumeKeysForNavigation;
    }

    public static void setUseVolumeKeysForNavigation(boolean volume) {
        mUseVolumeKeysForNavigation = volume;
    }

    public static boolean useVolumeKeysForListNavigationEnabled() {
        return mUseVolumeKeysForListNavigation;
    }

    public static void setUseVolumeKeysForListNavigation(boolean enabled) {
        mUseVolumeKeysForListNavigation = enabled;
    }

    public static boolean autofitWidth() {
        return mAutofitWidth;
    }

    public static void setAutofitWidth(boolean autofitWidth) {
        mAutofitWidth = autofitWidth;
    }

    public static boolean getQuietTimeEnabled() {
        return mQuietTimeEnabled;
    }

    public static void setQuietTimeEnabled(boolean quietTimeEnabled) {
        mQuietTimeEnabled = quietTimeEnabled;
    }

    public static boolean isNotificationDuringQuietTimeEnabled() {
        return mNotificationDuringQuietTimeEnabled;
    }

    public static void setNotificationDuringQuietTimeEnabled(boolean notificationDuringQuietTimeEnabled) {
        mNotificationDuringQuietTimeEnabled = notificationDuringQuietTimeEnabled;
    }

    public static String getQuietTimeStarts() {
        return mQuietTimeStarts;
    }

    public static void setQuietTimeStarts(String quietTimeStarts) {
        mQuietTimeStarts = quietTimeStarts;
    }

    public static String getQuietTimeEnds() {
        return mQuietTimeEnds;
    }

    public static void setQuietTimeEnds(String quietTimeEnds) {
        mQuietTimeEnds = quietTimeEnds;
    }


    public static boolean isQuietTime() {
        if (!mQuietTimeEnabled) {
            return false;
        }

        Time time = new Time();
        time.setToNow();
        Integer startHour = Integer.parseInt(mQuietTimeStarts.split(":")[0]);
        Integer startMinute = Integer.parseInt(mQuietTimeStarts.split(":")[1]);
        Integer endHour = Integer.parseInt(mQuietTimeEnds.split(":")[0]);
        Integer endMinute = Integer.parseInt(mQuietTimeEnds.split(":")[1]);

        Integer now = (time.hour * 60) + time.minute;
        Integer quietStarts = startHour * 60 + startMinute;
        Integer quietEnds = endHour * 60 + endMinute;

        // If start and end times are the same, we're never quiet
        if (quietStarts.equals(quietEnds)) {
            return false;
        }


        // 21:00 - 05:00 means we want to be quiet if it's after 9 or before 5
        if (quietStarts > quietEnds) {
            // if it's 22:00 or 03:00 but not 8:00
            if (now >= quietStarts || now <= quietEnds) {
                return true;
            }
        }

        // 01:00 - 05:00
        else {

            // if it' 2:00 or 4:00 but not 8:00 or 0:00
            if (now >= quietStarts && now <= quietEnds) {
                return true;
            }
        }

        return false;
    }


    public static boolean showAnimations() {
        return mAnimations;
    }

    public static void setAnimations(boolean animations) {
        mAnimations = animations;
    }

    public static boolean showCorrespondentNames() {
        return mShowCorrespondentNames;
    }

    public static void setShowCorrespondentNames(boolean showCorrespondentNames) {
        mShowCorrespondentNames = showCorrespondentNames;
    }

    public static boolean showContactName() {
        return mShowContactName;
    }

    public static void setShowContactName(boolean showContactName) {
        mShowContactName = showContactName;
    }

    public static boolean changeContactNameColor() {
        return mChangeContactNameColor;
    }

    public static void setChangeContactNameColor(boolean changeContactNameColor) {
        mChangeContactNameColor = changeContactNameColor;
    }

    public static int getContactNameColor() {
        return mContactNameColor;
    }

    public static void setContactNameColor(int contactNameColor) {
        mContactNameColor = contactNameColor;
    }

    public static boolean messageViewFixedWidthFont() {
        return mMessageViewFixedWidthFont;
    }

    public static void setMessageViewFixedWidthFont(boolean fixed) {
        mMessageViewFixedWidthFont = fixed;
    }

    public static FontSizes getFontSizes() {
        return fontSizes;
    }

    public static boolean measureAccounts() {
        return mMeasureAccounts;
    }

    public static void setMeasureAccounts(boolean measureAccounts) {
        mMeasureAccounts = measureAccounts;
    }

    public static boolean confirmSpam() {
        return mConfirmSpam;
    }

    public static void setConfirmSpam(final boolean confirm) {
        mConfirmSpam = confirm;
    }

    public static boolean confirmDeleteFromNotification() {
        return mConfirmDeleteFromNotification;
    }

    public static void setConfirmDeleteFromNotification(final boolean confirm) {
        mConfirmDeleteFromNotification = confirm;
    }

    public static NotificationHideSubject getNotificationHideSubject() {
        return sNotificationHideSubject;
    }

    public static void setNotificationHideSubject(final NotificationHideSubject mode) {
        sNotificationHideSubject = mode;
    }

    public static NotificationQuickDelete getNotificationQuickDeleteBehaviour() {
        return sNotificationQuickDelete;
    }

    public static void setNotificationQuickDeleteBehaviour(final NotificationQuickDelete mode) {
        sNotificationQuickDelete = mode;
    }

    public static LockScreenNotificationVisibility getLockScreenNotificationVisibility() {
        return sLockScreenNotificationVisibility;
    }

    public static void setLockScreenNotificationVisibility(final LockScreenNotificationVisibility visibility) {
        sLockScreenNotificationVisibility = visibility;
    }

    public static synchronized boolean isSortAscending(Account.SortType sortType) {
        if (mSortAscending.get(sortType) == null) {
            mSortAscending.put(sortType, sortType.isDefaultAscending());
        }
        return mSortAscending.get(sortType);
    }

    public static synchronized void setSortAscending(Account.SortType sortType, boolean sortAscending) {
        mSortAscending.put(sortType, sortAscending);
    }

    public static String getAttachmentDefaultPath() {
        return mAttachmentDefaultPath;
    }

    public static void setAttachmentDefaultPath(String attachmentDefaultPath) {
        mAttachmentDefaultPath = attachmentDefaultPath;
    }

    /**
     * Check if we already know whether all databases are using the current database schema.
     * <p/>
     * <p>
     * This method is only used for optimizations. If it returns {@code true} we can be certain that
     * getting a {@link LocalStore} instance won't trigger a schema upgrade.
     * </p>
     *
     * @return {@code true}, if we know that all databases are using the current database schema.
     * {@code false}, otherwise.
     */
    public static synchronized boolean areDatabasesUpToDate() {
        return sDatabasesUpToDate;
    }

    /**
     * Remember that all account databases are using the most recent database schema.
     *
     * @param save Whether or not to write the current database version to the
     *             {@code SharedPreferences} {@link #DATABASE_VERSION_CACHE}.
     * @see #areDatabasesUpToDate()
     */
    public static synchronized void setDatabasesUpToDate(boolean save) {
        sDatabasesUpToDate = true;

        if (save) {
            SharedPreferences.Editor editor = sDatabaseVersionCache.edit();
            editor.putInt(KEY_LAST_ACCOUNT_DATABASE_VERSION, LocalStore.DB_VERSION);
            editor.commit();
        }
    }


    public static boolean isActive() {
        return isActive;
    }

    public static void setActive(boolean val) {
        isActive = val;
    }
}
