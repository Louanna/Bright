package com.movit.platform.mail.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.movit.platform.mail.R;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.MessageReference;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.controller.MessagingListener;
import com.movit.platform.mail.fragment.ConfirmationDialogFragment;
import com.movit.platform.mail.mailstore.AttachmentViewInfo;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.mailstore.MessageViewInfo;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.ui.crypto.MessageCryptoAnnotations;
import com.movit.platform.mail.ui.crypto.MessageCryptoCallback;
import com.movit.platform.mail.ui.crypto.MessageCryptoHelper;
import com.movit.platform.mail.ui.messageLoader.DecodeMessageLoader;
import com.movit.platform.mail.ui.messageLoader.LocalMessageLoader;
import com.movit.platform.mail.ui.view.MessageContainerView;
import com.movit.platform.mail.util.MessageHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EmailContentActivity extends Activity implements MessageCryptoCallback,
        ConfirmationDialogFragment.ConfirmationDialogFragmentListener {

    private class LocalMessageLoaderCallback implements LoaderManager.LoaderCallbacks<LocalMessage> {
        @Override
        public Loader<LocalMessage> onCreateLoader(int id, Bundle args) {
            setProgress(true);
            return new LocalMessageLoader(EmailContentActivity.this, mController, mAccount, messageReference);
        }

        @Override
        public void onLoadFinished(Loader<LocalMessage> loader, LocalMessage message) {
            setProgress(false);
            mMessage = message;
            getLoaderManager().destroyLoader(LOCAL_MESSAGE_LOADER_ID);
            if (mMessage != null) {
                if (mMessage.isSet(Flag.X_DOWNLOADED_FULL)) {
                    onLoadMessageFromDatabaseFinished(message);
                } else {
                    displayMessageHeader(message);
                    Toast.makeText(EmailContentActivity.this,R.string.loading_email,Toast.LENGTH_SHORT).show();
                    linear_container.removeAllViews();
                    mController.loadMessageForViewRemote(mAccount, messageReference.getFolderName(), messageReference.getUid(),
                            downloadMessageListener);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<LocalMessage> loader) {
            // Do nothing
        }
    }

    private void setProgress(boolean enable) {
        setProgressBarIndeterminateVisibility(enable);
    }

    private void onLoadMessageFromDatabaseFinished(LocalMessage message) {
        displayMessageHeader(message);
        if (!message.isBodyMissing()) {
            messageCryptoHelper.decryptOrVerifyMessagePartsIfNecessary(message);
        }
    }

    private void signReadState(boolean isRead) {
        if (mMessage != null) {
            mController.setFlag(mAccount, mMessage.getFolder().getName(),
                    Collections.singletonList(mMessage), Flag.SEEN, isRead);
        }
    }

    private void displayMessageHeader(LocalMessage message) {
        try {
            setHeaders(message);
        } catch (MessagingException e) {

        }
    }

    private void setHeaders(LocalMessage message) throws MessagingException {
        final CharSequence from = MessageHelper.toFriendly(message.getFrom(), null);
        final CharSequence to = MessageHelper.toFriendly(message.getRecipients(Message.RecipientType.TO), null);
        final CharSequence cc = MessageHelper.toFriendly(message.getRecipients(Message.RecipientType.CC), null);
        String mSubject = message.getSubject();
        if (TextUtils.isEmpty(mSubject)) {
            mSubject = getString(R.string.general_no_subject);
        }
        subject.setText(mSubject);
        if (!TextUtils.isEmpty(from)) {
            sender.setText(from);
        }
        if (!TextUtils.isEmpty(to)) {
            recipients.setText(to);
        }
        if (!TextUtils.isEmpty(cc)) {
            txt_cc.setText(cc);
        } else {
            linear_cc.setVisibility(View.GONE);
        }
        String dateTime = DateUtils.formatDateTime(this,
                message.getSentDate().getTime(),
                DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_ABBREV_ALL
                        | DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_YEAR);
        time.setText(dateTime);
    }

    private class DecodeMessageLoaderCallback implements LoaderManager.LoaderCallbacks<MessageViewInfo> {
        @Override
        public Loader<MessageViewInfo> onCreateLoader(int id, Bundle args) {
            setProgress(true);
            return new DecodeMessageLoader(EmailContentActivity.this, mMessage, messageAnnotations);
        }

        @Override
        public void onLoadFinished(Loader<MessageViewInfo> loader, MessageViewInfo messageViewInfo) {
            setProgress(false);
            EmailContentActivity.this.messageViewInfo = messageViewInfo;
            onDecodeMessageFinished(messageViewInfo);
        }

        @Override
        public void onLoaderReset(Loader<MessageViewInfo> loader) {
            // Do nothing
        }
    }

    private void onDecodeMessageFinished(MessageViewInfo messageViewInfo) {
        if (messageViewInfo != null) {
            showMessage(messageViewInfo);
            // mController.loadMessageForViewRemote(mAccount, messageReference.getFolderName(), messageReference.getUid(),
            //        downloadMessageListener);
        }
    }

    private void showMessage(MessageViewInfo messageViewInfo) {
        try {
            for (MessageViewInfo.MessageViewContainer container : messageViewInfo.containers) {
                MessageContainerView view = (MessageContainerView) LayoutInflater.from(this).inflate(R.layout.message_container,
                        linear_container, false);
                view.displayMessageViewContainer(container);
                if (!container.attachments.isEmpty()) {
                    common_top_right.setVisibility(View.VISIBLE);
                    attachmentViewInfos.addAll(container.attachments);
                }
                linear_container.addView(view);
            }
        } catch (MessagingException e) {
            System.out.println("Error while trying to display message");
        }
    }

    private class DownloadMessageListener extends MessagingListener {
        @Override
        public void loadMessageForViewFinished(Account account, String folder, String uid, final LocalMessage message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onMessageDownloadFinished(message);
                }
            });
        }

        @Override
        public void loadMessageForViewFailed(Account account, String folder, String uid, final Throwable t) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EmailContentActivity.this, R.string.MessageError, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void onMessageDownloadFinished(LocalMessage message) {
        mMessage = message;
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.destroyLoader(LOCAL_MESSAGE_LOADER_ID);
        loaderManager.destroyLoader(DECODE_MESSAGE_LOADER_ID);
        onLoadMessageFromDatabaseFinished(mMessage);
    }

    private static List<AttachmentViewInfo> attachmentViewInfos = new ArrayList<>();

    private static final int ACTIVITY_CHOOSE_FOLDER_MOVE = 1;
    private static final int ACTIVITY_CHOOSE_FOLDER_COPY = 2;
    private static final int ACTIVITY_CHOOSE_DIRECTORY = 3;

    private static final int LOCAL_MESSAGE_LOADER_ID = 1;
    private static final int DECODE_MESSAGE_LOADER_ID = 2;

    public static final String DELETE_ALL_MESSAGE = "delete_email_message";

    private ImageView common_top_right;
    private LinearLayout linear_cc, linear_container;
    private TextView sender, recipients, subject, time, txt_cc;

    private Handler handler = new Handler();
    private Account mAccount;
    private LocalMessage mMessage;
    private int messageNumber;
    private MessageReference messageReference;
    private MessageViewInfo messageViewInfo;
    private MessageController mController;
    private MessageCryptoAnnotations messageAnnotations;
    private MessageCryptoHelper messageCryptoHelper;
    /**
     * Used to temporarily store the destination folder for refile operations if a confirmation
     * dialog is shown.
     */
    private String mDstFolder;
    private DownloadMessageListener downloadMessageListener = new DownloadMessageListener();
    private LoaderManager.LoaderCallbacks<LocalMessage> localMessageLoaderCallback = new LocalMessageLoaderCallback();
    private LoaderManager.LoaderCallbacks<MessageViewInfo> decodeMessageLoaderCallback = new DecodeMessageLoaderCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachmentViewInfos.clear();
        messageNumber = getIntent().getIntExtra("messageNumber", 0);
        System.out.println("messageNumber=" + messageNumber);
        messageReference = getIntent().getParcelableExtra("message");
        mAccount = Preferences.getPreferences(this).getCurrentAccount();
        messageCryptoHelper = new MessageCryptoHelper(this, mAccount, this);
        mController = MessageController.getInstance(this);
        initView();
        startLoadingMessageFromDatabase();
    }

    private void initView() {
        setContentView(R.layout.activity_email_content);
        TextView common_top_title = (TextView) findViewById(R.id.common_top_title);
        common_top_title.setText(R.string.detail);
        ImageView common_top_left = (ImageView) findViewById(R.id.common_top_left);
        common_top_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        common_top_right = (ImageView) findViewById(R.id.common_top_right);
        common_top_right.setImageResource(R.drawable.attachment);
        common_top_right.setVisibility(View.GONE);
        common_top_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmailContentActivity.this, AttachmentActivity.class));
            }
        });
        sender = (TextView) findViewById(R.id.sender);
        recipients = (TextView) findViewById(R.id.recipients);
        subject = (TextView) findViewById(R.id.subject);
        time = (TextView) findViewById(R.id.time);
        txt_cc = (TextView) findViewById(R.id.txt_cc);
        linear_cc = (LinearLayout) findViewById(R.id.linear_cc);
        linear_container = (LinearLayout) findViewById(R.id.message_containers);
        FrameLayout frame_sign = (FrameLayout) findViewById(R.id.frame_sign);
        frame_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMessage != null) {
                    boolean newState = !mMessage.isSet(Flag.SEEN);
                    signReadState(newState);
                    if (newState) {
                        Toast.makeText(EmailContentActivity.this, R.string.sign_read, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EmailContentActivity.this, R.string.sign_unread, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        FrameLayout frame_delete = (FrameLayout) findViewById(R.id.frame_delete);
        frame_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
        FrameLayout frame_reply = (FrameLayout) findViewById(R.id.frame_reply);
        frame_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReplyDialog();
            }
        });
        FrameLayout frame_replyAll = (FrameLayout) findViewById(R.id.frame_replyAll);
        frame_replyAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                replyAll();
            }
        });

        FrameLayout frame_more = (FrameLayout) findViewById(R.id.frame_more);
        frame_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void showDeleteDialog() {
        String title = getString(R.string.dialog_confirm_delete_title);
        String message = getString(R.string.dialog_confirm_delete_message);
        String confirmText = getString(R.string.dialog_confirm_delete_confirm_button);
        String cancelText = getString(R.string.dialog_confirm_delete_cancel_button);
        DialogFragment fragment = ConfirmationDialogFragment.newInstance(R.id.dialog_confirm_delete, title, message,
                confirmText, cancelText, null);
        fragment.show(getFragmentManager(), getDialogTag(R.id.dialog_confirm_delete));
    }

    private void showReplyDialog() {
        String title = getString(R.string.reply_or_refile);
        String message = getString(R.string.dialog_reply_or_refile);
        String confirmText = getString(R.string.refile);
        String cancelText = getString(R.string.reply);
        String neutralText = getString(R.string.cancel);
        DialogFragment fragment = ConfirmationDialogFragment.newInstance(R.id.dialog_reply_or_Refile, title, message,
                confirmText, cancelText, neutralText);
        fragment.show(getFragmentManager(), getDialogTag(R.id.dialog_reply_or_Refile));
    }


    private String getDialogTag(int dialogId) {
        return String.format(Locale.US, "dialog-%d", dialogId);
    }

    private void startLoadingMessageFromDatabase() {
        getLoaderManager().initLoader(LOCAL_MESSAGE_LOADER_ID, null, localMessageLoaderCallback);
    }

    @Override
    public void onCryptoOperationsFinished(MessageCryptoAnnotations annotations) {
        startExtractingTextAndAttachments(annotations);
    }

    private void startExtractingTextAndAttachments(MessageCryptoAnnotations annotations) {
        this.messageAnnotations = annotations;
        getLoaderManager().initLoader(DECODE_MESSAGE_LOADER_ID, null, decodeMessageLoaderCallback);
    }

    public static List<AttachmentViewInfo> getAttachmentViewInfo() {
        return attachmentViewInfos;
    }

    @Override
    public void doPositiveClick(int dialogId) {
        if (dialogId == R.id.dialog_confirm_delete) {
            delete();
        } else if (dialogId == R.id.dialog_reply_or_Refile) {
            forward();
        }
    }

    @Override
    public void doNegativeClick(int dialogId) {
        if (dialogId == R.id.dialog_reply_or_Refile) {
            reply();
        }
    }

    @Override
    public void doNeutralClick(int dialogId) {

    }

    @Override
    public void dialogCancelled(int dialogId) {

    }

    private void delete() {
        if (mMessage != null) {
            messageNumber--;
            LocalMessage messageToDelete = mMessage;
            mController.deleteMessages(Collections.singletonList(messageToDelete), null);
            if (messageNumber == 0) {
                Intent intent = new Intent();
                intent.setAction(DELETE_ALL_MESSAGE);
                sendBroadcast(intent);
            }
            finish();
        }
    }

    private void reply() {
        Intent i = new Intent(this, SendMailActivity.class);
        String body = null;
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_BODY, body);
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_REFERENCE, mMessage.makeMessageReference());
        i.setAction(SendMailActivity.ACTION_REPLY);
        startActivity(i);
    }

    private void replyAll() {
        Intent i = new Intent(this, SendMailActivity.class);
        String body = null;
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_BODY, body);
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_REFERENCE, mMessage.makeMessageReference());
        i.setAction(SendMailActivity.ACTION_REPLY_ALL);
        startActivity(i);
    }

    private void forward() {
        Intent i = new Intent(this, SendMailActivity.class);
        String body = null;
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_BODY, body);
        i.putExtra(SendMailActivity.EXTRA_MESSAGE_REFERENCE, mMessage.makeMessageReference());
        i.setAction(SendMailActivity.ACTION_FORWARD);
        startActivity(i);
    }
}
