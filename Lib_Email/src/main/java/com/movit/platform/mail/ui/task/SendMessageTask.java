package com.movit.platform.mail.ui.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fsck.k9.mail.Message;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.MessageReference;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.util.Contacts;

/**
 * Created by M on 2016/6/25.
 */
public class SendMessageTask extends AsyncTask<Void, Void, Void> {
    final Context context;
    final Account account;
    final Contacts contacts;
    final Message message;
    final Long draftId;
    final MessageReference messageReference;

    public SendMessageTask(Context context, Account account, Contacts contacts, Message message,
                           Long draftId, MessageReference messageReference) {
        this.context = context;
        this.account = account;
        this.contacts = contacts;
        this.message = message;
        this.draftId = draftId;
        this.messageReference = messageReference;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
//            contacts.markAsContacted(message.getRecipients(Message.RecipientType.TO));
//            contacts.markAsContacted(message.getRecipients(Message.RecipientType.CC));
//            contacts.markAsContacted(message.getRecipients(Message.RecipientType.BCC));
            updateReferencedMessage();
        } catch (Exception e) {
            System.out.println("Failed to mark contact as contacted.");
        }

        MessageController.getInstance(context).sendMessage(account, message, null);
        if (draftId != null) {
            // TODO set draft id to invalid in MessageCompose!
            MessageController.getInstance(context).deleteDraft(account, draftId);
        }

        return null;
    }

    /**
     * Set the flag on the referenced message(indicated we replied / forwarded the message)
     **/
    private void updateReferencedMessage() {
        if (messageReference != null && messageReference.getFlag() != null) {
            final Account account = Preferences.getPreferences(context)
                    .getAccount(messageReference.getAccountUuid());
            final String folderName = messageReference.getFolderName();
            final String sourceMessageUid = messageReference.getUid();
            MessageController.getInstance(context).setFlag(account, folderName,
                    sourceMessageUid, messageReference.getFlag(), true);
        }
    }
}