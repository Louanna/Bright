package com.movit.platform.mail.notification;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.bean.MessageReference;
import com.movit.platform.mail.controller.MessageController;
import com.fsck.k9.mail.Flag;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.service.CoreService;


public class NotificationActionService extends CoreService {
    private final static String ACTION_MARK_AS_READ = "ACTION_MARK_AS_READ";
    private final static String ACTION_DELETE = "ACTION_DELETE";
    private final static String ACTION_ARCHIVE = "ACTION_ARCHIVE";
    private final static String ACTION_SPAM = "ACTION_SPAM";
    private final static String ACTION_DISMISS = "ACTION_DISMISS";

    private final static String EXTRA_ACCOUNT_UUID = "accountUuid";
    private final static String EXTRA_MESSAGE_REFERENCE = "messageReference";
    private final static String EXTRA_MESSAGE_REFERENCES = "messageReferences";


    static Intent createMarkMessageAsReadIntent(Context context, MessageReference messageReference) {
        String accountUuid = messageReference.getAccountUuid();
        ArrayList<MessageReference> messageReferences = createSingleItemArrayList(messageReference);

        return createMarkAllAsReadIntent(context, accountUuid, messageReferences);
    }

    static Intent createMarkAllAsReadIntent(Context context, String accountUuid,
            ArrayList<MessageReference> messageReferences) {
        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_MARK_AS_READ);
        intent.putExtra(EXTRA_ACCOUNT_UUID, accountUuid);
        intent.putExtra(EXTRA_MESSAGE_REFERENCES, messageReferences);

        return intent;
    }

    static Intent createDismissMessageIntent(Context context, MessageReference messageReference) {
        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_DISMISS);
        intent.putExtra(EXTRA_ACCOUNT_UUID, messageReference.getAccountUuid());
        intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);

        return intent;
    }

    static Intent createDismissAllMessagesIntent(Context context, Account account) {
        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_DISMISS);
        intent.putExtra(EXTRA_ACCOUNT_UUID, account.getUuid());

        return intent;
    }

    static Intent createDeleteMessageIntent(Context context, MessageReference messageReference) {
        String accountUuid = messageReference.getAccountUuid();
        ArrayList<MessageReference> messageReferences = createSingleItemArrayList(messageReference);

        return createDeleteAllMessagesIntent(context, accountUuid, messageReferences);
    }

    public static Intent createDeleteAllMessagesIntent(Context context, String accountUuid,
            ArrayList<MessageReference> messageReferences) {

        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_ACCOUNT_UUID, accountUuid);
        intent.putExtra(EXTRA_MESSAGE_REFERENCES, messageReferences);

        return intent;
    }

    static Intent createArchiveMessageIntent(Context context, MessageReference messageReference) {
        ArrayList<MessageReference> messageReferences = createSingleItemArrayList(messageReference);

        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_ARCHIVE);
        intent.putExtra(EXTRA_ACCOUNT_UUID, messageReference.getAccountUuid());
        intent.putExtra(EXTRA_MESSAGE_REFERENCES, messageReferences);

        return intent;
    }

    static Intent createArchiveAllIntent(Context context, Account account,
            ArrayList<MessageReference> messageReferences) {
        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_ARCHIVE);
        intent.putExtra(EXTRA_ACCOUNT_UUID, account.getUuid());
        intent.putExtra(EXTRA_MESSAGE_REFERENCES, messageReferences);

        return intent;
    }

    static Intent createMarkMessageAsSpamIntent(Context context, MessageReference messageReference) {
        Intent intent = new Intent(context, NotificationActionService.class);
        intent.setAction(ACTION_SPAM);
        intent.putExtra(EXTRA_ACCOUNT_UUID, messageReference.getAccountUuid());
        intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);

        return intent;
    }

    private static ArrayList<MessageReference> createSingleItemArrayList(MessageReference messageReference) {
        ArrayList<MessageReference> messageReferences = new ArrayList<MessageReference>(1);
        messageReferences.add(messageReference);

        return messageReferences;
    }

    @Override
    public int startService(Intent intent, int startId) {
        String accountUuid = intent.getStringExtra(EXTRA_ACCOUNT_UUID);
        Preferences preferences = Preferences.getPreferences(this);
        Account account = preferences.getAccount(accountUuid);

        if (account == null) {
            System.out.println("Could not find account for notification action.");
            return START_NOT_STICKY;
        }

        MessageController controller = MessageController.getInstance(getApplication());

        String action = intent.getAction();
        if (ACTION_MARK_AS_READ.equals(action)) {
            markMessagesAsRead(intent, account, controller);
        } else if (ACTION_DELETE.equals(action)) {
            deleteMessages(intent, controller);
        } else if (ACTION_ARCHIVE.equals(action)) {
            archiveMessages(intent, account, controller);
        } else if (ACTION_SPAM.equals(action)) {
            markMessageAsSpam(intent, account, controller);
        } else if (ACTION_DISMISS.equals(action)) {
        }

        cancelNotifications(intent, account, controller);

        return START_NOT_STICKY;
    }

    private void markMessagesAsRead(Intent intent, Account account, MessageController controller) {
        List<MessageReference> messageReferences = intent.getParcelableArrayListExtra(EXTRA_MESSAGE_REFERENCES);
        for (MessageReference messageReference : messageReferences) {
            controller.setFlag(account, messageReference.getFolderName(), messageReference.getUid(), Flag.SEEN, true);
        }
    }

    private void deleteMessages(Intent intent, MessageController controller) {
        List<MessageReference> messageReferences = intent.getParcelableArrayListExtra(EXTRA_MESSAGE_REFERENCES);
        List<LocalMessage> messages = getLocalMessages(messageReferences);
        controller.deleteMessages(messages, null);
    }

    private void archiveMessages(Intent intent, Account account, MessageController controller) {

        String archiveFolderName = account.getArchiveFolderName();
        if (archiveFolderName == null ||
                (archiveFolderName.equals(account.getSpamFolderName()) && MailboxController.confirmSpam()) ||
                !isMovePossible(controller, account, archiveFolderName)) {
            System.out.println("Can not archive messages");
            return;
        }

        List<MessageReference> messageReferences = intent.getParcelableArrayListExtra(EXTRA_MESSAGE_REFERENCES);
        for (MessageReference messageReference : messageReferences) {
            LocalMessage message = messageReference.restoreToLocalMessage(this);
            if (controller.isMoveCapable(message)) {
                String sourceFolderName = message.getFolder().getName();
                controller.moveMessage(account, sourceFolderName, message, archiveFolderName, null);
            }
        }
    }

    private void markMessageAsSpam(Intent intent, Account account, MessageController controller) {

        MessageReference messageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
        LocalMessage message = messageReference.restoreToLocalMessage(this);

        String spamFolderName = account.getSpamFolderName();
        if (spamFolderName != null && !MailboxController.confirmSpam() &&
                isMovePossible(controller, account, spamFolderName)) {
            String sourceFolderName = message.getFolder().getName();
            controller.moveMessage(account, sourceFolderName, message, spamFolderName, null);
        }
    }

    private void cancelNotifications(Intent intent, Account account, MessageController controller) {
        if (intent.hasExtra(EXTRA_MESSAGE_REFERENCE)) {
            MessageReference messageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
            controller.cancelNotificationForMessage(account, messageReference);
        } else if (intent.hasExtra(EXTRA_MESSAGE_REFERENCES)) {
            List<MessageReference> messageReferences = intent.getParcelableArrayListExtra(EXTRA_MESSAGE_REFERENCES);
            for (MessageReference messageReference : messageReferences) {
                controller.cancelNotificationForMessage(account, messageReference);
            }
        } else {
            controller.cancelNotificationsForAccount(account);
        }
    }

    private List<LocalMessage> getLocalMessages(List<MessageReference> messageReferences) {
        List<LocalMessage> messages = new ArrayList<LocalMessage>(messageReferences.size());

        for (MessageReference messageReference : messageReferences) {
            LocalMessage message = messageReference.restoreToLocalMessage(this);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    private boolean isMovePossible(MessageController controller, Account account,
            String destinationFolderName) {
        boolean isSpecialFolderConfigured = !MailboxController.FOLDER_NONE.equalsIgnoreCase(destinationFolderName);

        return isSpecialFolderConfigured && controller.isMoveCapable(account);
    }
}
