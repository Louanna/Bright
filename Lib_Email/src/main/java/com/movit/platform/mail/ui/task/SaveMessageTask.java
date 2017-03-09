package com.movit.platform.mail.ui.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.activity.SendMailActivity;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.util.Contacts;
import com.fsck.k9.mail.Message;

public class SaveMessageTask extends AsyncTask<Void, Void, Void> {
    Context context;
    Account account;
    Contacts contacts;
    Handler handler;
    Message message;
    long draftId;
    boolean saveRemotely;

    public SaveMessageTask(Context context, Account account, Contacts contacts,
                           Handler handler, Message message, long draftId, boolean saveRemotely) {
        this.context = context;
        this.account = account;
        this.contacts = contacts;
        this.handler = handler;
        this.message = message;
        this.draftId = draftId;
        this.saveRemotely = saveRemotely;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final MessageController messagingController = MessageController.getInstance(context);
        Message draftMessage = messagingController.saveDraft(account, message, draftId, saveRemotely);
        draftId = messagingController.getId(draftMessage);
        android.os.Message msg = android.os.Message.obtain(handler, SendMailActivity.MSG_SAVED_DRAFT, draftId);
        handler.sendMessage(msg);
        return null;
    }
}
