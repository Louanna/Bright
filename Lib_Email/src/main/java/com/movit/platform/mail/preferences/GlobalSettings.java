package com.movit.platform.mail.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.os.Environment;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.util.FontSizes;
import com.movit.platform.mail.controller.MailboxController.NotificationHideSubject;
import com.movit.platform.mail.controller.MailboxController.NotificationQuickDelete;
import com.movit.platform.mail.controller.MailboxController.SplitViewMode;
import com.movit.platform.mail.R;
import com.movit.platform.mail.bean.Account.SortType;
import com.movit.platform.mail.preferences.Settings.*;

import static com.movit.platform.mail.controller.MailboxController.LockScreenNotificationVisibility;

public class GlobalSettings {
    public static final Map<String, TreeMap<Integer, SettingsDescription>> SETTINGS;
    // UPDATES not use here
    public static final Map<Integer, SettingsUpgrader> UPGRADERS = new HashMap<Integer, SettingsUpgrader>();

    static {
        Map<String, TreeMap<Integer, SettingsDescription>> s =
            new LinkedHashMap<String, TreeMap<Integer, SettingsDescription>>();

        /**
         * When adding new settings here, be sure to increment {@link Settings.VERSION}
         * and use that for whatever you add here.
         */
        s.put("animations", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("attachmentdefaultpath", Settings.versions(
                new V(1, new DirectorySetting(Environment.getExternalStorageDirectory())),
                new V(41, new DirectorySetting(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS)))
            ));
        s.put("backgroundOperations", Settings.versions(
                new V(1, new EnumSetting<MailboxController.BACKGROUND_OPS>(
                        MailboxController.BACKGROUND_OPS.class, MailboxController.BACKGROUND_OPS.WHEN_CHECKED_AUTO_SYNC))
            ));
        s.put("changeRegisteredNameColor", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("confirmDelete", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("confirmDeleteStarred", Settings.versions(
                new V(2, new BooleanSetting(false))
            ));
        s.put("confirmSpam", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("countSearchMessages", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("enableDebugLogging", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("enableSensitiveLogging", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("fontSizeAccountDescription", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeAccountName", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeFolderName", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeFolderStatus", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageComposeInput", Settings.versions(
                new V(5, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageListDate", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageListPreview", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageListSender", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageListSubject", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewAdditionalHeaders", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewCC", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewContent", Settings.versions(
                new V(1, new WebFontSizeSetting(3)),
                new V(31, null)
            ));
        s.put("fontSizeMessageViewDate", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewSender", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewSubject", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewTime", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("fontSizeMessageViewTo", Settings.versions(
                new V(1, new FontSizeSetting(FontSizes.FONT_DEFAULT))
            ));
        s.put("gesturesEnabled", Settings.versions(
                new V(1, new BooleanSetting(true)),
                new V(4, new BooleanSetting(false))
            ));
        s.put("hideSpecialAccounts", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("keyguardPrivacy", Settings.versions(
                new V(1, new BooleanSetting(false)),
                new V(12, null)
            ));
        s.put("measureAccounts", Settings.versions(
                new V(1, new BooleanSetting(true))
            ));
        s.put("messageListCheckboxes", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("messageListPreviewLines", Settings.versions(
                new V(1, new IntegerRangeSetting(1, 100, 2))
            ));
        s.put("messageListStars", Settings.versions(
                new V(1, new BooleanSetting(true))
            ));
        s.put("messageViewFixedWidthFont", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("messageViewReturnToList", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("messageViewShowNext", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("quietTimeEnabled", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("quietTimeEnds", Settings.versions(
                new V(1, new TimeSetting("7:00"))
            ));
        s.put("quietTimeStarts", Settings.versions(
                new V(1, new TimeSetting("21:00"))
            ));
        s.put("registeredNameColor", Settings.versions(
                new V(1, new ColorSetting(0xFF00008F))
            ));
        s.put("showContactName", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("showCorrespondentNames", Settings.versions(
                new V(1, new BooleanSetting(true))
            ));
        s.put("sortTypeEnum", Settings.versions(
                new V(10, new EnumSetting<SortType>(SortType.class, Account.DEFAULT_SORT_TYPE))
            ));
        s.put("sortAscending", Settings.versions(
                new V(10, new BooleanSetting(Account.DEFAULT_SORT_ASCENDING))
            ));
        s.put("startIntegratedInbox", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("useVolumeKeysForListNavigation", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("useVolumeKeysForNavigation", Settings.versions(
                new V(1, new BooleanSetting(false))
            ));
        s.put("wrapFolderNames", Settings.versions(
                new V(22, new BooleanSetting(false))
            ));
        s.put("notificationHideSubject", Settings.versions(
                new V(12, new EnumSetting<NotificationHideSubject>(
                        NotificationHideSubject.class, NotificationHideSubject.NEVER))
            ));
        s.put("useBackgroundAsUnreadIndicator", Settings.versions(
                new V(19, new BooleanSetting(true))
            ));
        s.put("threadedView", Settings.versions(
                new V(20, new BooleanSetting(true))
            ));
        s.put("splitViewMode", Settings.versions(
                new V(23, new EnumSetting<SplitViewMode>(SplitViewMode.class, SplitViewMode.NEVER))
            ));
        s.put("fixedMessageViewTheme", Settings.versions(
                new V(24, new BooleanSetting(true))
            ));
        s.put("showContactPicture", Settings.versions(
                new V(25, new BooleanSetting(true))
            ));
        s.put("autofitWidth", Settings.versions(
                new V(28, new BooleanSetting(true))
            ));
        s.put("colorizeMissingContactPictures", Settings.versions(
                new V(29, new BooleanSetting(true))
            ));
        s.put("messageViewDeleteActionVisible", Settings.versions(
                new V(30, new BooleanSetting(true))
            ));
        s.put("messageViewArchiveActionVisible", Settings.versions(
                new V(30, new BooleanSetting(false))
            ));
        s.put("messageViewMoveActionVisible", Settings.versions(
                new V(30, new BooleanSetting(false))
            ));
        s.put("messageViewCopyActionVisible", Settings.versions(
                new V(30, new BooleanSetting(false))
            ));
        s.put("messageViewSpamActionVisible", Settings.versions(
                new V(30, new BooleanSetting(false))
            ));
        s.put("fontSizeMessageViewContentPercent", Settings.versions(
                new V(31, new IntegerRangeSetting(40, 250, 100))
            ));
        s.put("hideUserAgent", Settings.versions(
                new V(32, new BooleanSetting(false))
            ));
        s.put("hideTimeZone", Settings.versions(
                new V(32, new BooleanSetting(false))
            ));
        s.put("lockScreenNotificationVisibility", Settings.versions(
                new V(37, new EnumSetting<LockScreenNotificationVisibility>(LockScreenNotificationVisibility.class,
                        LockScreenNotificationVisibility.MESSAGE_COUNT))
            ));
        s.put("confirmDeleteFromNotification", Settings.versions(
                new V(38, new BooleanSetting(true))
            ));
        s.put("messageListSenderAboveSubject", Settings.versions(
                new V(38, new BooleanSetting(false))
            ));
        s.put("notificationQuickDelete", Settings.versions(
                new V(38, new EnumSetting<NotificationQuickDelete>(NotificationQuickDelete.class,
                        NotificationQuickDelete.NEVER))
            ));
        s.put("notificationDuringQuietTimeEnabled", Settings.versions(
                new V(39, new BooleanSetting(true))
            ));
        s.put("confirmDiscardMessage", Settings.versions(
                new V(40, new BooleanSetting(true))
            ));
        SETTINGS = Collections.unmodifiableMap(s);
    }

    public static Map<String, Object> validate(int version, Map<String, String> importedSettings) {
        return Settings.validate(version, SETTINGS, importedSettings, false);
    }

    public static Set<String> upgrade(int version, Map<String, Object> validatedSettings) {
        return Settings.upgrade(version, UPGRADERS, SETTINGS, validatedSettings);
    }

    public static Map<String, String> convert(Map<String, Object> settings) {
        return Settings.convert(settings, SETTINGS);
    }

    public static Map<String, String> getGlobalSettings(Storage storage) {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : SETTINGS.keySet()) {
            String value = storage.getString(key, null);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }
    /**
     * A time setting.
     */
    public static class TimeSetting extends SettingsDescription {
        public TimeSetting(String defaultValue) {
            super(defaultValue);
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            if (!value.matches(TimePickerPreference.VALIDATION_EXPRESSION)) {
                throw new InvalidSettingValueException();
            }
            return value;
        }
    }

    /**
     * A directory on the file system.
     */
    public static class DirectorySetting extends SettingsDescription {
        public DirectorySetting(File defaultPath) {
            super(defaultPath.toString());
        }

        @Override
        public Object fromString(String value) throws InvalidSettingValueException {
            try {
                if (new File(value).isDirectory()) {
                    return value;
                }
            } catch (Exception e) { /* do nothing */ }

            throw new InvalidSettingValueException();
        }
    }
}
