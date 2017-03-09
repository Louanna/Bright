package com.movit.platform.mail.ui.messageLoader;


import android.content.AsyncTaskLoader;
import android.content.Context;

import com.fsck.k9.mail.Message;
import com.movit.platform.mail.mailstore.LocalMessageExtractor;
import com.movit.platform.mail.mailstore.MessageViewInfo;
import com.movit.platform.mail.ui.crypto.MessageCryptoAnnotations;


public class DecodeMessageLoader extends AsyncTaskLoader<MessageViewInfo> {
    private final Message message;
    private MessageViewInfo messageViewInfo;
    private MessageCryptoAnnotations annotations;

    public DecodeMessageLoader(Context context, Message message, MessageCryptoAnnotations annotations) {
        super(context);
        this.message = message;
        this.annotations = annotations;
    }

    @Override
    protected void onStartLoading() {
        if (messageViewInfo != null) {
            super.deliverResult(messageViewInfo);
        }

        if (takeContentChanged() || messageViewInfo == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(MessageViewInfo messageViewInfo) {
        this.messageViewInfo = messageViewInfo;
        super.deliverResult(messageViewInfo);
    }

    @Override
    public MessageViewInfo loadInBackground() {
        try {
            return LocalMessageExtractor.decodeMessageForView(getContext(), message, annotations);
        } catch (Exception e) {
            System.out.println("Error while decoding message");
            return null;
        }
    }
}
