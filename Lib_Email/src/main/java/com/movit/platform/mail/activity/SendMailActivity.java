package com.movit.platform.mail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.Loader;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Multipart;
import com.fsck.k9.mail.Part;
import com.fsck.k9.mail.internet.MessageExtractor;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.MimeUtility;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.mail.R;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.Attachment;
import com.movit.platform.mail.bean.Identity;
import com.movit.platform.mail.bean.MessageReference;
import com.movit.platform.mail.bean.Recipient;
import com.movit.platform.mail.controller.MailboxController;
import com.movit.platform.mail.controller.MailboxEntry;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.controller.MessagingListener;
import com.movit.platform.mail.fragment.ProgressDialogFragment;
import com.movit.platform.mail.mailstore.LocalBodyPart;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.message.IdentityField;
import com.movit.platform.mail.message.IdentityHeaderParser;
import com.movit.platform.mail.message.InsertableHtmlContent;
import com.movit.platform.mail.message.MessageBuilder;
import com.movit.platform.mail.message.QuotedTextMode;
import com.movit.platform.mail.message.SimpleMessageBuilder;
import com.movit.platform.mail.message.SimpleMessageFormat;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.provider.AttachmentProvider;
import com.movit.platform.mail.ui.compose.RecipientMvpView;
import com.movit.platform.mail.ui.compose.RecipientPresenter;
import com.movit.platform.mail.ui.messageLoader.AttachmentContentLoader;
import com.movit.platform.mail.ui.messageLoader.AttachmentInfoLoader;
import com.movit.platform.mail.ui.task.SaveMessageTask;
import com.movit.platform.mail.ui.task.SendMessageTask;
import com.movit.platform.mail.ui.view.EolConvertingEditText;
import com.movit.platform.mail.ui.view.MessageWebView;
import com.movit.platform.mail.ui.view.RecipientSelectView;
import com.movit.platform.mail.util.Contacts;
import com.movit.platform.mail.util.HtmlConverter;
import com.movit.platform.mail.util.IdentityHelper;
import com.movit.platform.mail.util.MailTo;
import com.movit.platform.mail.util.QuotedMessageHelper;
import com.movit.platform.mail.util.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class SendMailActivity extends Activity implements MessageBuilder.Callback {

    private LoaderManager.LoaderCallbacks<Attachment> mAttachmentInfoLoaderCallback =
            new LoaderManager.LoaderCallbacks<Attachment>() {
                @Override
                public Loader<Attachment> onCreateLoader(int id, Bundle args) {
                    mNumAttachmentsLoading += 1;
                    Attachment attachment = args.getParcelable(LOADER_ARG_ATTACHMENT);
                    return new AttachmentInfoLoader(SendMailActivity.this, attachment);
                }

                @Override
                public void onLoadFinished(Loader<Attachment> loader, Attachment attachment) {
                    int loaderId = loader.getId();
                    attachment.loaderId = ++mMaxLoaderId;
                    initAttachmentContentLoader(attachment);
                    getLoaderManager().destroyLoader(loaderId);
                }

                @Override
                public void onLoaderReset(Loader<Attachment> loader) {
                    performStalledAction();
                }
            };

    private void initAttachmentContentLoader(Attachment attachment) {
        LoaderManager loaderManager = getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putParcelable(LOADER_ARG_ATTACHMENT, attachment);
        loaderManager.initLoader(attachment.loaderId, bundle, mAttachmentContentLoaderCallback);
    }

    private LoaderManager.LoaderCallbacks<Attachment> mAttachmentContentLoaderCallback =
            new LoaderManager.LoaderCallbacks<Attachment>() {
                @Override
                public Loader<Attachment> onCreateLoader(int id, Bundle args) {
                    Attachment attachment = args.getParcelable(LOADER_ARG_ATTACHMENT);
                    return new AttachmentContentLoader(SendMailActivity.this, attachment);
                }

                @Override
                public void onLoadFinished(Loader<Attachment> loader, Attachment attachment) {
                    int loaderId = loader.getId();
                    if(null!=attachment && StringUtils.notEmpty(attachment.name) && attachment.name.contains(".")){
                        String name = attachment.name.substring(0,attachment.name.lastIndexOf("."));
                        if(!"noname".equals(name)){
                            attachments.add(attachment);
                        }
                    }
                    performStalledAction();
                    getLoaderManager().destroyLoader(loaderId);
                }

                @Override
                public void onLoaderReset(Loader<Attachment> loader) {
                    performStalledAction();
                }
            };

    private class Listener extends MessagingListener {
        @Override
        public void loadMessageForViewStarted(Account account, String folder, String uid) {
            if (mMessageReference == null || !mMessageReference.getUid().equals(uid)) {
                return;
            }
            mHandler.sendEmptyMessage(MSG_PROGRESS_ON);
        }

        @Override
        public void loadMessageForViewFinished(Account account, String folder, String uid, LocalMessage message) {
            if (mMessageReference == null || !mMessageReference.getUid().equals(uid)) {
                return;
            }
            mHandler.sendEmptyMessage(MSG_PROGRESS_OFF);
        }

        @Override
        public void loadMessageForViewBodyAvailable(Account account, String folder, String uid,
                                                    final LocalMessage message) {
            if (mMessageReference == null || !mMessageReference.getUid().equals(uid)) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // We check to see if we've previously processed the source message since this
                    // could be called when switching from HTML to text replies. If that happens, we
                    // only want to update the UI with quoted text (which picks the appropriate
                    // part).
                    loadLocalMessageForDisplay(message, mAction);
                }
            });
        }

        @Override
        public void loadMessageForViewFailed(Account account, String folder, String uid, Throwable t) {
            if (mMessageReference == null || !mMessageReference.getUid().equals(uid)) {
                return;
            }
            mHandler.sendEmptyMessage(MSG_PROGRESS_OFF);
        }

        @Override
        public void messageUidChanged(Account account, String folder, String oldUid, String newUid) {
            // Track UID changes of the source message
            if (mMessageReference != null) {
                final Account sourceAccount = Preferences.getPreferences(SendMailActivity.this)
                        .getAccount(mMessageReference.getAccountUuid());
                final String sourceFolder = mMessageReference.getFolderName();
                final String sourceMessageUid = mMessageReference.getUid();
                if (account.equals(sourceAccount) && (folder.equals(sourceFolder))) {
                    if (oldUid.equals(sourceMessageUid)) {
                        mMessageReference = mMessageReference.withModifiedUid(newUid);
                    }
                }
            }
        }
    }

    public void loadLocalMessageForDisplay(LocalMessage message, Action action) {
        // We check to see if we've previously processed the source message since this
        // could be called when switching from HTML to text replies. If that happens, we
        // only want to update the UI with quoted text (which picks the appropriate
        // part).
        if (mSourceMessageProcessed) {
            try {
                populateUIWithQuotedMessage(message, action);
            } catch (MessagingException e) {
                // Hm, if we couldn't populate the UI after source reprocessing, let's just delete it?
                hideQuoted();
            }
            updateMessageFormat();
        } else {
            processSourceMessage(message);
            mSourceMessageProcessed = true;
        }
    }

    private void populateUIWithQuotedMessage(Message sourceMessage, Action action)
            throws MessagingException {
        String content = sourceMessageBody != null ? sourceMessageBody :
                QuotedMessageHelper.getBodyTextFromMessage(sourceMessage, quotedTextFormat);
        if (quotedTextFormat == SimpleMessageFormat.HTML) {
            // Strip signature.
            // closing tags such as </div>, </span>, </table>, </pre> will be cut off.
            if (action == Action.REPLY || action == Action.REPLY_ALL) {
                content = QuotedMessageHelper.stripSignatureForHtmlMessage(content);
            }
            quotedHtmlContent = QuotedMessageHelper.quoteOriginalHtmlMessage(
                    SendMailActivity.this.getResources(), sourceMessage, content, quoteStyle);
            quoted_html.setText(quotedHtmlContent.getQuotedContent());
            quoted_text.setText(QuotedMessageHelper.quoteOriginalTextMessage(SendMailActivity.this.getResources(), sourceMessage,
                    QuotedMessageHelper.getBodyTextFromMessage(sourceMessage, SimpleMessageFormat.TEXT),
                    quoteStyle, mAccount.getQuotePrefix()));
        } else if (quotedTextFormat == SimpleMessageFormat.TEXT) {
            if (action == Action.REPLY || action == Action.REPLY_ALL) {
                content = QuotedMessageHelper.stripSignatureForTextMessage(content);
            }
            quoted_text.setText(QuotedMessageHelper.quoteOriginalTextMessage(
                    SendMailActivity.this.getResources(), sourceMessage, content, quoteStyle, mAccount.getQuotePrefix()));
        }
    }

    private void processSourceMessage(LocalMessage message) {
        try {
            switch (mAction) {
                case REPLY:
                    processMessageToReplyTo(message);
                    break;
                case REPLY_ALL: {
                    processMessageToReplyAllTo(message);
                    break;
                }
                case FORWARD: {
                    processMessageToForward(message);
                    break;
                }
                case EDIT_DRAFT: {
                    processDraftMessage(message);
                    break;
                }
                default: {
                    break;
                }
            }
        } catch (MessagingException me) {
            /**
             * Let the user continue composing their message even if we have a problem processing
             * the source message. Log it as an error, though.
             */
            System.out.println("Error while processing source message: ");
        } finally {
            mSourceMessageProcessed = true;
            draftNeedsSaving = false;
        }
        updateMessageFormat();
    }

    private void processMessageToReplyTo(Message message) throws MessagingException {
        if (message.getSubject() != null) {
            final String subject = PREFIX.matcher(message.getSubject()).replaceFirst("");

            if (!subject.toLowerCase(Locale.US).startsWith("re:")) {
                edit_subject.setText("Re: " + subject);
            } else {
                edit_subject.setText(subject);
            }
        } else {
            edit_subject.setText("");
        }
         /*
         * If a reply-to was included with the message use that, otherwise use the from
         * or sender address.
         */
        recipientPresenter.initFromReplyToMessage(message);
        if (message.getMessageId() != null && message.getMessageId().length() > 0) {
            mInReplyTo = message.getMessageId();

            String[] refs = message.getReferences();
            if (refs != null && refs.length > 0) {
                mReferences = TextUtils.join("", refs) + " " + mInReplyTo;
            } else {
                mReferences = mInReplyTo;
            }
        } else {
            System.out.println("could not get Message-ID.");
        }
        populateUIWithQuotedMessage(message, mAction);
        if (mAction == Action.REPLY || mAction == Action.REPLY_ALL) {
            Identity useIdentity = IdentityHelper.getRecipientIdentityFromMessage(mAccount, message);
            Identity defaultIdentity = mAccount.getIdentity(0);
            if (useIdentity != defaultIdentity) {
                switchToIdentity(useIdentity);
            }
        }
    }

    private void processMessageToReplyAllTo(Message message) throws MessagingException {
        if (message.getSubject() != null) {
            final String subject = PREFIX.matcher(message.getSubject()).replaceFirst("");

            if (!subject.toLowerCase(Locale.US).startsWith("re:")) {
                edit_subject.setText("Re: " + subject);
            } else {
                edit_subject.setText(subject);
            }
        } else {
            edit_subject.setText("");
        }
         /*
         * If a reply-to was included with the message use that, otherwise use the from
         * or sender address.
         */
        recipientPresenter.initFromReplyAllToMessage(message);
        if (message.getMessageId() != null && message.getMessageId().length() > 0) {
            mInReplyTo = message.getMessageId();

            String[] refs = message.getReferences();
            if (refs != null && refs.length > 0) {
                mReferences = TextUtils.join("", refs) + " " + mInReplyTo;
            } else {
                mReferences = mInReplyTo;
            }
        } else {
            System.out.println("could not get Message-ID.");
        }
        populateUIWithQuotedMessage(message, mAction);
        if (mAction == Action.REPLY || mAction == Action.REPLY_ALL) {
            Identity useIdentity = IdentityHelper.getRecipientIdentityFromMessage(mAccount, message);
            Identity defaultIdentity = mAccount.getIdentity(0);
            if (useIdentity != defaultIdentity) {
                switchToIdentity(useIdentity);
            }
        }
    }

    private void switchToIdentity(Identity identity) {
        mIdentity = identity;
        mIdentityChanged = true;
        draftNeedsSaving = true;
        updateMessageFormat();
        recipientPresenter.onSwitchIdentity(identity);
    }

    private void updateMessageFormat() {
        Account.MessageFormat origMessageFormat = mAccount.getMessageFormat();
        SimpleMessageFormat messageFormat;
        if (origMessageFormat == Account.MessageFormat.TEXT) {
            messageFormat = SimpleMessageFormat.TEXT;
        } else if (origMessageFormat == Account.MessageFormat.AUTO) {
            if (mAction == Action.COMPOSE) {
                messageFormat = SimpleMessageFormat.TEXT;
            } else {
                messageFormat = SimpleMessageFormat.HTML;
            }
        } else {
            messageFormat = SimpleMessageFormat.HTML;
        }
        mMessageFormat = messageFormat;
    }

    private void processMessageToForward(Message message) throws MessagingException {
        String subject = message.getSubject();
        if (subject != null && !subject.toLowerCase(Locale.US).startsWith("fwd:")) {
            edit_subject.setText("Fwd: " + subject);
        } else {
            edit_subject.setText(subject);
        }
        if (!TextUtils.isEmpty(message.getMessageId())) {
            mInReplyTo = message.getMessageId();
            mReferences = mInReplyTo;
        } else {
            System.out.println("could not get Message-ID.");

        }
        quoteStyle = Account.QuoteStyle.HEADER;
        populateUIWithQuotedMessage(message, Action.FORWARD);
        if (!mSourceMessageProcessed) {
            if (message.isSet(Flag.X_DOWNLOADED_PARTIAL) || !loadAttachments(message, 0)) {
                mHandler.sendEmptyMessage(MSG_SKIPPED_ATTACHMENTS);
            }
        }
    }

    private void processDraftMessage(LocalMessage message) throws MessagingException {
        mDraftId = MessageController.getInstance(getApplication()).getId(message);
        edit_subject.setText(message.getSubject());
        recipientPresenter.initFromDraftMessage(message);
        // Read In-Reply-To header from draft
        final String[] inReplyTo = message.getHeader("In-Reply-To");
        if (inReplyTo.length >= 1) {
            mInReplyTo = inReplyTo[0];
        }
        // Read References header from draft
        final String[] references = message.getHeader("References");
        if (references.length >= 1) {
            mReferences = references[0];
        }
        if (!mSourceMessageProcessed) {
            loadAttachments(message, 0);
        }
        // Decode the identity header when loading a draft.
        // See buildIdentityHeader(TextBody) for a detailed description of the composition of this blob.
        Map<IdentityField, String> k9identity = new HashMap<>();
        String[] identityHeaders = message.getHeader(MailboxController.IDENTITY_HEADER);

        if (identityHeaders.length > 0 && identityHeaders[0] != null) {
            k9identity = IdentityHeaderParser.parse(identityHeaders[0]);
        }

        Identity newIdentity = new Identity();
        if (k9identity.containsKey(IdentityField.SIGNATURE)) {
            newIdentity.setSignatureUse(true);
            newIdentity.setSignature(k9identity.get(IdentityField.SIGNATURE));
            mSignatureChanged = true;
        } else {
            newIdentity.setSignatureUse(message.getFolder().getSignatureUse());
            newIdentity.setSignature(mIdentity.getSignature());
        }

        if (k9identity.containsKey(IdentityField.NAME)) {
            newIdentity.setName(k9identity.get(IdentityField.NAME));
            mIdentityChanged = true;
        } else {
            newIdentity.setName(mIdentity.getName());
        }

        if (k9identity.containsKey(IdentityField.EMAIL)) {
            newIdentity.setEmail(k9identity.get(IdentityField.EMAIL));
            mIdentityChanged = true;
        } else {
            newIdentity.setEmail(mIdentity.getEmail());
        }

        if (k9identity.containsKey(IdentityField.ORIGINAL_MESSAGE)) {
            mMessageReference = null;
            try {
                String originalMessage = k9identity.get(IdentityField.ORIGINAL_MESSAGE);
                MessageReference messageReference = new MessageReference(originalMessage);
                // Check if this is a valid account in our database
                Preferences prefs = Preferences.getPreferences(getApplicationContext());
                Account account = prefs.getAccount(messageReference.getAccountUuid());
                if (account != null) {
                    mMessageReference = messageReference;
                }
            } catch (MessagingException e) {
                System.out.println("Could not decode message reference in identity.");
            }
        }
        mIdentity = newIdentity;
        processDraftMessage(message, k9identity);
    }

    public void processDraftMessage(LocalMessage message, Map<IdentityField, String> k9identity)
            throws MessagingException {
        quoteStyle = k9identity.get(IdentityField.QUOTE_STYLE) != null
                ? Account.QuoteStyle.valueOf(k9identity.get(IdentityField.QUOTE_STYLE))
                : mAccount.getQuoteStyle();

        int cursorPosition = 0;
        if (k9identity.containsKey(IdentityField.CURSOR_POSITION)) {
            try {
                cursorPosition = Integer.parseInt(k9identity.get(IdentityField.CURSOR_POSITION));
            } catch (Exception e) {
                System.out.println("Could not parse cursor position for MessageCompose; continuing.");
            }
        }
        int bodyLength = k9identity.get(IdentityField.LENGTH) != null ?
                Integer.valueOf(k9identity.get(IdentityField.LENGTH)) : UNKNOWN_LENGTH;
        int bodyOffset = k9identity.get(IdentityField.OFFSET) != null ?
                Integer.valueOf(k9identity.get(IdentityField.OFFSET)) : UNKNOWN_LENGTH;
        Integer bodyFooterOffset = k9identity.get(IdentityField.FOOTER_OFFSET) != null ?
                Integer.valueOf(k9identity.get(IdentityField.FOOTER_OFFSET)) : null;
        Integer bodyPlainLength = k9identity.get(IdentityField.PLAIN_LENGTH) != null ?
                Integer.valueOf(k9identity.get(IdentityField.PLAIN_LENGTH)) : null;
        Integer bodyPlainOffset = k9identity.get(IdentityField.PLAIN_OFFSET) != null ?
                Integer.valueOf(k9identity.get(IdentityField.PLAIN_OFFSET)) : null;
        String messageFormatString = k9identity.get(IdentityField.MESSAGE_FORMAT);
        Account.MessageFormat messageFormat = null;
        if (messageFormatString != null) {
            try {
                messageFormat = Account.MessageFormat.valueOf(messageFormatString);
            } catch (Exception e) { /* do nothing */ }
        }

        if (messageFormat == null) {
            // This message probably wasn't created by us. The exception is legacy
            // drafts created before the advent of HTML composition. In those cases,
            // we'll display the whole message (including the quoted part) in the
            // composition window. If that's the case, try and convert it to text to
            // match the behavior in text mode.
            message_content.setCharacters(
                    QuotedMessageHelper.getBodyTextFromMessage(message, SimpleMessageFormat.TEXT));
            return;
        }

        if (messageFormat == Account.MessageFormat.HTML) {
            Part part = MimeUtility.findFirstPartByMimeType(message, "text/html");
            if (part != null) { // Shouldn't happen if we were the one who saved it.
                quotedTextFormat = SimpleMessageFormat.HTML;
                String text = MessageExtractor.getTextFromPart(part);
                if (bodyOffset + bodyLength > text.length()) {
                    bodyOffset = 0;
                    bodyLength = 0;
                }
                // Grab our reply text.
                String bodyText = text.substring(bodyOffset, bodyOffset + bodyLength);
                message_content.setCharacters(HtmlConverter.htmlToText(bodyText));

                // Regenerate the quoted html without our user content in it.
                StringBuilder quotedHTML = new StringBuilder();
                quotedHTML.append(text.substring(0, bodyOffset));   // stuff before the reply
                quotedHTML.append(text.substring(bodyOffset + bodyLength));
                if (quotedHTML.length() > 0) {
                    quotedHtmlContent = new InsertableHtmlContent();
                    quotedHtmlContent.setQuotedContent(quotedHTML);
                    // We don't know if bodyOffset refers to the header or to the footer
                    quotedHtmlContent.setHeaderInsertionPoint(bodyOffset);
                    if (bodyFooterOffset != null) {
                        quotedHtmlContent.setFooterInsertionPoint(bodyFooterOffset);
                    } else {
                        quotedHtmlContent.setFooterInsertionPoint(bodyOffset);
                    }
                    quoted_html.setText(quotedHtmlContent.getQuotedContent());
                }
            }
            if (bodyPlainOffset != null && bodyPlainLength != null) {
                processSourceMessageText(message, bodyPlainOffset, bodyPlainLength, false);
            }
        } else if (messageFormat == Account.MessageFormat.TEXT) {
            quotedTextFormat = SimpleMessageFormat.TEXT;
            processSourceMessageText(message, bodyOffset, bodyLength, true);
        }
        try {
            message_content.setSelection(cursorPosition);
        } catch (Exception e) {
            System.out.println("Could not set cursor position in MessageCompose; ignoring.");
        }
    }

    private void processSourceMessageText(Message message, int bodyOffset, int bodyLength, boolean viewMessageContent)
            throws MessagingException {
        Part textPart = MimeUtility.findFirstPartByMimeType(message, "text/plain");
        if (textPart == null) {
            return;
        }
        String messageText = MessageExtractor.getTextFromPart(textPart);
        // If we had a body length (and it was valid), separate the composition from the quoted text
        // and put them in their respective places in the UI.
        if (bodyLength != UNKNOWN_LENGTH) {
            try {
                // Regenerate the quoted text without our user content in it nor added newlines.
                StringBuilder quotedText = new StringBuilder();
                if (bodyOffset == UNKNOWN_LENGTH &&
                        messageText.substring(bodyLength, bodyLength + 4).equals("\r\n\r\n")) {
                    // top-posting: ignore two newlines at start of quote
                    quotedText.append(messageText.substring(bodyLength + 4));
                } else if (bodyOffset + bodyLength == messageText.length() &&
                        messageText.substring(bodyOffset - 2, bodyOffset).equals("\r\n")) {
                    // bottom-posting: ignore newline at end of quote
                    quotedText.append(messageText.substring(0, bodyOffset - 2));
                } else {
                    quotedText.append(messageText.substring(0, bodyOffset));   // stuff before the reply
                    quotedText.append(messageText.substring(bodyOffset + bodyLength));
                }
                quoted_text.setText(quotedText.toString());
                messageText = messageText.substring(bodyOffset, bodyOffset + bodyLength);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("The identity field from the draft contains an invalid bodyOffset/bodyLength");
            }
        }
        if (viewMessageContent) {
            message_content.setCharacters(messageText);
        }
    }

    private boolean loadAttachments(Part part, int depth) throws MessagingException {
        if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart) part.getBody();
            boolean ret = true;
            for (int i = 0, count = mp.getCount(); i < count; i++) {
                if (!loadAttachments(mp.getBodyPart(i), depth + 1)) {
                    ret = false;
                }
            }
            return ret;
        }

        String contentType = MimeUtility.unfoldAndDecode(part.getContentType());
        String name = MimeUtility.getHeaderParameter(contentType, null);
        if (name != null) {
            if (part instanceof LocalBodyPart) {
                LocalBodyPart localBodyPart = (LocalBodyPart) part;
                String accountUuid = localBodyPart.getAccountUuid();
                long attachmentId = localBodyPart.getId();
                Uri uri = AttachmentProvider.getAttachmentUri(accountUuid, attachmentId);
                addOldAttachment(uri);
                return true;
            }
            return false;
        }
        return true;
    }

    public class SendMailBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AddAttachmentActivity.LOADING_ATTACHMENT)) {
                mHandler.sendEmptyMessage(MSG_PERFORM_STALLED_ACTION);
            } else if (intent.getAction().equals(AddAttachmentActivity.SAVE_ATTACHMENT)) {
                draftNeedsSaving = true;
                Attachment attachment = intent.getParcelableExtra("attachment");
                if (attachment != null) {
                    attachments.add(attachment);
                }
            } else if (intent.getAction().equals(AddAttachmentActivity.DELETE_ATTACHMENT)) {
                draftNeedsSaving = true;
                int id = intent.getIntExtra("id", -1);
                if (id != -1) {
                    for (int i = 0; i < attachments.size(); i++) {
                        if (id == attachments.get(i).loaderId) {
                            attachments.remove(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    public enum Action {
        COMPOSE(R.string.compose_title_compose),
        REPLY(R.string.compose_title_reply),
        REPLY_ALL(R.string.compose_title_reply_all),
        FORWARD(R.string.compose_title_forward),
        EDIT_DRAFT(R.string.compose_title_compose);

        private final int titleResource;

        Action(@StringRes int titleResource) {
            this.titleResource = titleResource;
        }

        @StringRes
        public int getTitleResource() {
            return titleResource;
        }
    }

    private Action mAction;

    private static final int DIALOG_CREATE_OR_DISCARD_DRAFT_MESSAGE = 1;
    private static final int DIALOG_CONFIRM_DISCARD_ON_BACK = 2;
    private static final int DIALOG_CHOOSE_IDENTITY = 3;
    private static final int DIALOG_CONFIRM_DISCARD = 4;
    private static final int DIALOG_IF_DISCARD_DRAFT_MESSAGE = 5;
    private static final int DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE = 6;
    private static final int REQUEST_ORG_FOR_RECEIVER = 7;
    private static final int REQUEST_ORG_FOR_CC = 8;
    private static final int REQUEST_ORG_FOR_BCC = 9;


    private static final long INVALID_DRAFT_ID = MessageController.INVALID_MESSAGE_ID;

    public static final String ACTION_COMPOSE = "com.fsck.k9.intent.action.COMPOSE";
    public static final String ACTION_REPLY = "com.fsck.k9.intent.action.REPLY";
    public static final String ACTION_REPLY_ALL = "com.fsck.k9.intent.action.REPLY_ALL";
    public static final String ACTION_FORWARD = "com.fsck.k9.intent.action.FORWARD";
    public static final String ACTION_EDIT_DRAFT = "com.fsck.k9.intent.action.EDIT_DRAFT";

    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_MESSAGE_BODY = "messageBody";
    public static final String EXTRA_MESSAGE_REFERENCE = "message_reference";

    private static final String STATE_KEY_ATTACHMENTS =
            "com.fsck.k9.activity.MessageCompose.attachments";
    private static final String STATE_KEY_SOURCE_MESSAGE_PROCED =
            "com.fsck.k9.activity.MessageCompose.stateKeySourceMessageProced";
    private static final String STATE_KEY_DRAFT_ID = "com.fsck.k9.activity.MessageCompose.draftId";
    private static final String STATE_IDENTITY_CHANGED =
            "com.fsck.k9.activity.MessageCompose.identityChanged";
    private static final String STATE_IDENTITY =
            "com.fsck.k9.activity.MessageCompose.identity";
    private static final String STATE_IN_REPLY_TO = "com.fsck.k9.activity.MessageCompose.inReplyTo";
    private static final String STATE_REFERENCES = "com.fsck.k9.activity.MessageCompose.references";
    private static final String STATE_KEY_READ_RECEIPT = "com.fsck.k9.activity.MessageCompose.messageReadReceipt";
    private static final String STATE_KEY_DRAFT_NEEDS_SAVING = "com.fsck.k9.activity.MessageCompose.draftNeedsSaving";
    private static final String STATE_KEY_NUM_ATTACHMENTS_LOADING = "numAttachmentsLoading";
    private static final String STATE_KEY_WAITING_FOR_ATTACHMENTS = "waitingForAttachments";
    private static final String STATE_ALREADY_NOTIFIED_USER_OF_EMPTY_SUBJECT = "alreadyNotifiedUserOfEmptySubject";

    private static final String LOADER_ARG_ATTACHMENT = "attachment";

    private static final String FRAGMENT_WAITING_FOR_ATTACHMENT = "waitingForAttachment";

    private static final int UNKNOWN_LENGTH = 0;
    private static final int MSG_PROGRESS_ON = 1;
    private static final int MSG_PROGRESS_OFF = 2;
    private static final int MSG_SKIPPED_ATTACHMENTS = 3;
    public static final int MSG_SAVED_DRAFT = 4;
    private static final int MSG_DISCARDED_DRAFT = 5;
    private static final int MSG_PERFORM_STALLED_ACTION = 6;

    private static final int ACTIVITY_REQUEST_PICK_ATTACHMENT = 1;

    private static final int REQUEST_MASK_RECIPIENT_PRESENTER = (1 << 8);
    private static final int REQUEST_MASK_MESSAGE_BUILDER = (2 << 8);

    private EditText edit_subject;
    private RecipientSelectView edit_recipients, edit_cc, edit_bcc;
    private EolConvertingEditText message_content, quoted_text;
    private MessageWebView quoted_html;
    private QuotedTextMode quotedTextMode = QuotedTextMode.SHOW;
    private Account.QuoteStyle quoteStyle;
    private SimpleMessageFormat quotedTextFormat;
    private SimpleMessageFormat mMessageFormat;
    private String sourceMessageBody;
    private InsertableHtmlContent quotedHtmlContent;
    private SendMailBroadcastReceiver sendMailBroadcastReceiver;
    private ArrayList<Attachment> attachments = new ArrayList<>();
    /**
     * Reference to the source message (in case of reply, forward, or edit
     * draft actions).
     */
    private MessageReference mMessageReference;
    private static final Pattern PREFIX = Pattern.compile("^AW[:\\s]\\s*", Pattern.CASE_INSENSITIVE);

    /**
     * The account used for message composition.
     */
    private Account mAccount;
    private Contacts mContacts;
    //Note: This has to be an identity of the account {@link #mAccount}.
    private Identity mIdentity;

    private boolean mIdentityChanged = false;
    private boolean mSignatureChanged = false;
    /**
     * Indicates that the source message has been processed at least once and should not
     * be processed on any subsequent loads. This protects us from adding attachments that
     * have already been added from the restore of the view state.
     */
    private boolean mSourceMessageProcessed = false;
    public static int mMaxLoaderId = 0;
    private Listener mListener = new Listener();
    private RecipientPresenter recipientPresenter;
    private MessageBuilder currentMessageBuilder;
    private boolean mFinishAfterDraftSaved;
    private boolean alreadyNotifiedUserOfEmptySubject = false;


    private boolean mSourceProcessed = false;

    private String mReferences;
    private String mInReplyTo;

    private boolean draftNeedsSaving = false;
    private boolean isInSubActivity = false;

    /**
     * The database ID of this message's draft. This is used when saving drafts so the message in
     * the database is updated instead of being created anew. This property is INVALID_DRAFT_ID
     * until the first save.
     */
    private long mDraftId = INVALID_DRAFT_ID;

    /**
     * Number of attachments currently being fetched.
     */
    public static int mNumAttachmentsLoading = 0;

    private enum WaitingAction {
        NONE,
        SEND,
        SAVE
    }

    /**
     * Specifies what action to perform once attachments have been fetched.
     */
    private WaitingAction mWaitingForAttachments = WaitingAction.NONE;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS_ON:
                    setProgressBarIndeterminateVisibility(true);
                    break;
                case MSG_PROGRESS_OFF:
                    setProgressBarIndeterminateVisibility(false);
                    break;
                case MSG_SKIPPED_ATTACHMENTS:
                    Toast.makeText(
                            SendMailActivity.this,
                            getString(R.string.message_compose_attachments_skipped_toast),
                            Toast.LENGTH_LONG).show();
                    break;
                case MSG_SAVED_DRAFT:
                    mDraftId = (Long) msg.obj;
                    Toast.makeText(
                            SendMailActivity.this,
                            getString(R.string.message_saved_toast),
                            Toast.LENGTH_LONG).show();
                    break;
                case MSG_DISCARDED_DRAFT:
                    if (mAction == Action.EDIT_DRAFT) {
                        Toast.makeText(
                                SendMailActivity.this,
                                getString(R.string.message_discarded_draft),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(
                                SendMailActivity.this,
                                getString(R.string.message_discarded),
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case MSG_PERFORM_STALLED_ACTION:
                    performStalledAction();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void performStalledAction() {
        mNumAttachmentsLoading -= 1;
        WaitingAction waitingFor = mWaitingForAttachments;
        mWaitingForAttachments = WaitingAction.NONE;
        if (waitingFor != WaitingAction.NONE) {
            dismissWaitingForAttachmentDialog();
        }
        switch (waitingFor) {
            case SEND: {
                performSendAfterChecks();
                break;
            }
            case SAVE: {
                performSaveAfterChecks();
                break;
            }
            case NONE:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }
        mMaxLoaderId = 0;
        mNumAttachmentsLoading = 0;
        MessageController.getInstance(getApplication()).addListener(mListener);
        final Intent intent = getIntent();
        mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
        mAccount = Preferences.getPreferences(this).getCurrentAccount();
        if (mAccount == null) {
            finish();
            return;
        }
        if (mIdentity == null) {
            mIdentity = mAccount.getIdentity(0);
        }
        quoteStyle = mAccount.getQuoteStyle();
        Account.MessageFormat origMessageFormat = mAccount.getMessageFormat();
        if (origMessageFormat == Account.MessageFormat.TEXT) {
            quotedTextFormat = SimpleMessageFormat.TEXT;
        } else {
            quotedTextFormat = SimpleMessageFormat.HTML;
        }
        mContacts = Contacts.getInstance(this);
        sourceMessageBody = intent.getStringExtra(EXTRA_MESSAGE_BODY);
        initView();
        if (initFromIntent(intent)) {
            mAction = Action.COMPOSE;
            draftNeedsSaving = true;
        } else {
            String action = intent.getAction();
            if (ACTION_COMPOSE.equals(action)) {
                mAction = Action.COMPOSE;
            } else if (ACTION_REPLY.equals(action)) {
                mAction = Action.REPLY;
            } else if (ACTION_REPLY_ALL.equals(action)) {
                mAction = Action.REPLY_ALL;
            } else if (ACTION_FORWARD.equals(action)) {
                mAction = Action.FORWARD;
            } else if (ACTION_EDIT_DRAFT.equals(action)) {
                mAction = Action.EDIT_DRAFT;
            } else {
                System.out.println("MessageCompose was started with an unsupported action");
                mAction = Action.COMPOSE;
            }
        }
        if (!mSourceMessageProcessed) {
            if (mAction == Action.REPLY || mAction == Action.REPLY_ALL ||
                    mAction == Action.FORWARD || mAction == Action.EDIT_DRAFT) {
                /*
                 * If we need to load the message we add ourself as a message listener here
                 * so we can kick it off. Normally we add in onResume but we don't
                 * want to reload the message every time the activity is resumed.
                 * There is no harm in adding twice.
                 */
                MessageController.getInstance(getApplication()).addListener(mListener);
                final Account account = Preferences.getPreferences(this).getAccount(mMessageReference.getAccountUuid());
                final String folderName = mMessageReference.getFolderName();
                final String sourceMessageUid = mMessageReference.getUid();
                MessageController.getInstance(getApplication()).loadMessageForView(account, folderName, sourceMessageUid, null);
            }
        }

        if (mAction == Action.REPLY || mAction == Action.REPLY_ALL) {
            mMessageReference = mMessageReference.withModifiedFlag(Flag.ANSWERED);
        }

        if (mAction == Action.REPLY || mAction == Action.REPLY_ALL ||
                mAction == Action.EDIT_DRAFT) {
            message_content.requestFocus();
        }
        if (mAction == Action.FORWARD) {
            mMessageReference = mMessageReference.withModifiedFlag(Flag.FORWARDED);
        }
        updateMessageFormat();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AddAttachmentActivity.LOADING_ATTACHMENT);
        filter.addAction(AddAttachmentActivity.SAVE_ATTACHMENT);
        filter.addAction(AddAttachmentActivity.DELETE_ATTACHMENT);
        sendMailBroadcastReceiver = new SendMailBroadcastReceiver();
        registerReceiver(sendMailBroadcastReceiver, filter);
        currentMessageBuilder = (MessageBuilder) getLastNonConfigurationInstance();
        if (currentMessageBuilder != null) {
            setProgressBarIndeterminateVisibility(true);
            currentMessageBuilder.reattachCallback(this);
        }
    }

    private void initView() {
        TextWatcher draftNeedsChangingTextWatcher = new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                draftNeedsSaving = true;
            }
        };
        setContentView(R.layout.activity_send_mail);
        ImageView common_top_left = (ImageView) findViewById(R.id.common_top_left);
        common_top_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBroad();
                if (mAction == Action.EDIT_DRAFT) {
                    showDraftSaveDialog(draftNeedsSaving);
                } else {
                    if (draftNeedsSaving) {
                        showCommonSaveDialog();
                    } else {
                        finish();
                    }
                }
            }
        });
        message_content = (EolConvertingEditText) findViewById(R.id.message_content);
        message_content.getInputExtras(true).putBoolean("allowEmoji", true);
        quoted_text = (EolConvertingEditText) findViewById(R.id.quoted_text);
        edit_recipients = (RecipientSelectView) findViewById(R.id.edit_recipients);
        edit_recipients.setInfos(this, MailboxEntry.getAllRecipient(this));
        edit_cc = (RecipientSelectView) findViewById(R.id.edit_cc);
        edit_bcc = (RecipientSelectView) findViewById(R.id.edit_bcc);
        edit_cc.setInfos(this, MailboxEntry.getAllRecipient(this));
        edit_bcc.setInfos(this, MailboxEntry.getAllRecipient(this));
        edit_subject = (EditText) findViewById(R.id.edit_subject);
        message_content.addTextChangedListener(draftNeedsChangingTextWatcher);
        quoted_text.addTextChangedListener(draftNeedsChangingTextWatcher);
        edit_recipients.addTextChangedListener(draftNeedsChangingTextWatcher);
        edit_cc.addTextChangedListener(draftNeedsChangingTextWatcher);
        edit_bcc.addTextChangedListener(draftNeedsChangingTextWatcher);
        edit_subject.addTextChangedListener(draftNeedsChangingTextWatcher);
        ImageView img_add_attachment = (ImageView) findViewById(R.id.img_add_attachment);
        img_add_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SendMailActivity.this, AddAttachmentActivity.class);
                intent.putParcelableArrayListExtra("list", attachments);
                startActivity(intent);
            }
        });
        ImageView img_add_receiver = (ImageView) findViewById(R.id.img_add_receiver);
        img_add_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MailboxEntry.toOrgActivity(SendMailActivity.this, REQUEST_ORG_FOR_RECEIVER);
            }
        });
        ImageView img_add_cc = (ImageView) findViewById(R.id.img_add_cc);
        img_add_cc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MailboxEntry.toOrgActivity(SendMailActivity.this, REQUEST_ORG_FOR_CC);
            }
        });
        ImageView img_add_bcc = (ImageView) findViewById(R.id.img_add_bcc);
        img_add_bcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MailboxEntry.toOrgActivity(SendMailActivity.this, REQUEST_ORG_FOR_BCC);
            }
        });
        TextView common_top_txt_right = (TextView) findViewById(R.id.common_top_txt_right);
        common_top_txt_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        quoted_html = (MessageWebView) findViewById(R.id.quoted_html);
        quoted_html.configure();
        // Disable the ability to click links in the quoted HTML page. I think this is a nice feature, but if someone
        // feels this should be a preference (or should go away all together), I'm ok with that too. -achen 20101130
        quoted_html.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        if (getIntent().getSerializableExtra("userInfos") != null) {
            ArrayList<UserInfo> userInfos = (ArrayList<UserInfo>) getIntent().getSerializableExtra("userInfos");
            for (int i = 0; i < userInfos.size(); i++) {
                edit_recipients.addRecipients(Recipient.fromUserInfo(userInfos.get(i)));
            }
        }
        RecipientMvpView recipientMvpView = new RecipientMvpView(this);
        recipientPresenter = new RecipientPresenter(this, recipientMvpView, mAccount);
    }

    private void hideKeyBroad() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(message_content.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edit_recipients.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edit_cc.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edit_bcc.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(edit_subject.getWindowToken(), 0);
    }

    private boolean initFromIntent(final Intent intent) {
        boolean startedByExternalIntent = false;
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_SENDTO.equals(action)) {
            /*
             * Someone has clicked a mailto: link. The address is in the URI.
             */
            if (intent.getData() != null) {
                Uri uri = intent.getData();
                if (MailTo.isMailTo(uri)) {
                    MailTo mailTo = MailTo.parse(uri);
                    initializeFromMailto(mailTo);
                }
            }

            /*
             * Note: According to the documentation ACTION_VIEW and ACTION_SENDTO don't accept
             * EXTRA_* parameters.
             * And previously we didn't process these EXTRAs. But it looks like nobody bothers to
             * read the official documentation and just copies wrong sample code that happens to
             * work with the AOSP Email application. And because even big players get this wrong,
             * we're now finally giving in and read the EXTRAs for those actions (below).
             */
        }

        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action) ||
                Intent.ACTION_SENDTO.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            startedByExternalIntent = true;

            /*
             * Note: Here we allow a slight deviation from the documented behavior.
             * EXTRA_TEXT is used as message body (if available) regardless of the MIME
             * type of the intent. In addition one or multiple attachments can be added
             * using EXTRA_STREAM.
             */
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            // Only use EXTRA_TEXT if the body hasn't already been set by the mailto URI
            if (text != null && message_content.getText().length() == 0) {
                message_content.setCharacters(text);
            }

            String type = intent.getType();
            if (Intent.ACTION_SEND.equals(action)) {
                Uri stream = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (stream != null) {
                    addOldAttachment(stream, type);
                }
            } else {
                List<Parcelable> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (list != null) {
                    for (Parcelable parcelable : list) {
                        Uri stream = (Uri) parcelable;
                        if (stream != null) {
                            addOldAttachment(stream, type);
                        }
                    }
                }
            }

            String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            // Only use EXTRA_SUBJECT if the subject hasn't already been set by the mailto URI
            if (subject != null && edit_subject.getText().length() == 0) {
                edit_subject.setText(subject);
            }
            recipientPresenter.initFromSendOrViewIntent(intent);
        }

        return startedByExternalIntent;
    }

    private void initializeFromMailto(MailTo mailTo) {
        recipientPresenter.initFromMailto(mailTo);
        String subject = mailTo.getSubject();
        if (subject != null && !subject.isEmpty()) {
            edit_subject.setText(subject);
        }
        String body = mailTo.getBody();
        if (body != null && !body.isEmpty()) {
            message_content.setCharacters(body);
        }
    }

    private void addOldAttachment(Uri uri) {
        addOldAttachment(uri, null);
    }

    private void addOldAttachment(Uri uri, String contentType) {
        Attachment attachment = new Attachment();
        attachment.state = Attachment.LoadingState.URI_ONLY;
        attachment.uri = uri;
        attachment.contentType = contentType;
        attachment.loaderId = ++mMaxLoaderId;
        initAttachmentInfoLoader(attachment);
    }

    private void initAttachmentInfoLoader(Attachment attachment) {
        LoaderManager loaderManager = getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putParcelable(LOADER_ARG_ATTACHMENT, attachment);
        loaderManager.initLoader(attachment.loaderId, bundle, mAttachmentInfoLoaderCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        MessageController.getInstance(getApplication()).addListener(mListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        MessageController.getInstance(getApplication()).removeListener(mListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sendMailBroadcastReceiver);
        recipientPresenter.onActivityDestroy();
    }

    private void performSaveAfterChecks() {
        currentMessageBuilder = createMessageBuilder(true);
        if (currentMessageBuilder != null) {
            setProgressBarIndeterminateVisibility(true);
            currentMessageBuilder.buildAsync(this);
        }
    }

    @Nullable
    private MessageBuilder createMessageBuilder(boolean isDraft) {
        SimpleMessageBuilder builder;
        recipientPresenter.updateCryptoStatus();
        builder = new SimpleMessageBuilder(getApplicationContext());
        builder.setSubject(edit_subject.getText().toString())
                .setTo(recipientPresenter.getToAddresses())
                .setCc(recipientPresenter.getCcAddresses())
                .setBcc(recipientPresenter.getBccAddresses())
                .setInReplyTo(mInReplyTo)
                .setReferences(mReferences)
                .setRequestReadReceipt(mAccount.isMessageReadReceiptAlways())
                .setIdentity(mIdentity)
                .setMessageFormat(mMessageFormat)
                .setText(message_content.getCharacters())
                .setAttachments(attachments)
                .setSignatureBeforeQuotedText(mAccount.isSignatureBeforeQuotedText())
                .setIdentityChanged(mIdentityChanged)
                .setSignatureChanged(mSignatureChanged)
                .setCursorPosition(message_content.getSelectionStart())
                .setMessageReference(mMessageReference)
                .setDraft(isDraft)
                .setIsPgpInlineEnabled(false);
        builderSetProperties(builder);
        return builder;
    }

    private void builderSetProperties(MessageBuilder builder) {
        builder.setQuoteStyle(quoteStyle)
                .setQuotedText(quoted_text.getCharacters())
                .setQuotedTextMode(quotedTextMode)
                .setQuotedHtmlContent(quotedHtmlContent)
                .setReplyAfterQuote(mAccount.isReplyAfterQuote());
    }

    private void send() {
        if (edit_subject.getText().length() == 0 && !alreadyNotifiedUserOfEmptySubject) {
            Toast.makeText(this, R.string.empty_subject, Toast.LENGTH_LONG).show();
            alreadyNotifiedUserOfEmptySubject = true;
            return;
        }
        if (recipientPresenter.checkRecipientsOkForSending()) {
            return;
        }
        if (mWaitingForAttachments != WaitingAction.NONE) {
            return;
        }
        if (mNumAttachmentsLoading > 0) {
            mWaitingForAttachments = WaitingAction.SEND;
            showWaitingForAttachmentDialog();
            return;
        }
        hideKeyBroad();
        performSendAfterChecks();
    }

    private void showWaitingForAttachmentDialog() {
        String title;
        switch (mWaitingForAttachments) {
            case SEND: {
                title = getString(R.string.fetching_attachment_dialog_title_send);
                break;
            }
            case SAVE: {
                title = getString(R.string.fetching_attachment_dialog_title_save);
                break;
            }
            default: {
                return;
            }
        }
        ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(title,
                getString(R.string.fetching_attachment_dialog_message));
        fragment.show(getFragmentManager(), FRAGMENT_WAITING_FOR_ATTACHMENT);
    }

    private void dismissWaitingForAttachmentDialog() {
        ProgressDialogFragment fragment = (ProgressDialogFragment)
                getFragmentManager().findFragmentByTag(FRAGMENT_WAITING_FOR_ATTACHMENT);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    public void performSendAfterChecks() {
        currentMessageBuilder = createMessageBuilder(false);
        if (currentMessageBuilder != null) {
            draftNeedsSaving = false;
            setProgressBarIndeterminateVisibility(true);
            currentMessageBuilder.buildAsync(this);
        }
    }

    @Override
    public void onMessageBuildSuccess(MimeMessage message, boolean isDraft) {
        if (isDraft) {
            draftNeedsSaving = false;
            currentMessageBuilder = null;
            if (mAction == Action.EDIT_DRAFT && mMessageReference != null) {
                message.setUid(mMessageReference.getUid());
            }
            new SaveMessageTask(getApplicationContext(), mAccount, mContacts, mHandler,
                    message, mDraftId, true).execute();
            if (mFinishAfterDraftSaved) {
                finish();
            } else {
                setProgressBarIndeterminateVisibility(false);
            }
        } else {
            currentMessageBuilder = null;
            new SendMessageTask(getApplicationContext(), mAccount, mContacts, message,
                    mDraftId != INVALID_DRAFT_ID ? mDraftId : null, mMessageReference).execute();
            finish();
        }
    }

    @Override
    public void onMessageBuildCancel() {
        currentMessageBuilder = null;
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onMessageBuildException(MessagingException me) {
        Toast.makeText(this,
                getString(R.string.send_failed_reason, me.getLocalizedMessage()), Toast.LENGTH_LONG).show();
        currentMessageBuilder = null;
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onMessageBuildReturnPendingIntent(PendingIntent pendingIntent, int requestCode) {
        requestCode |= REQUEST_MASK_MESSAGE_BUILDER;
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            System.out.println("Error starting pending intent from builder!");
        }
    }

    private void hideQuoted() {
        quoted_html.setVisibility(View.GONE);
        quoted_text.setVisibility(View.GONE);
    }

    public void launchUserInteractionPendingIntent(PendingIntent pendingIntent, int requestCode) {
        requestCode |= REQUEST_MASK_RECIPIENT_PRESENTER;
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mAction == Action.EDIT_DRAFT) {
            showDraftSaveDialog(draftNeedsSaving);
        } else {
            if (draftNeedsSaving) {
                showCommonSaveDialog();
            } else {
                super.onBackPressed();
//                if (mDraftId == INVALID_DRAFT_ID) {
//                    onDiscard();
//                } else {
//                    super.onBackPressed();
//                }
            }
        }
    }

    private void showDraftSaveDialog(boolean needSave) {
        showDialog(DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE);
//        if (needSave) {
//            showDialog(DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE);
//        } else {
//            showDialog(DIALOG_IF_DISCARD_DRAFT_MESSAGE);
//        }
    }

    private void showCommonSaveDialog() {
//        if (!mAccount.hasDraftsFolder()) {
//            showDialog(DIALOG_CONFIRM_DISCARD_ON_BACK);
//        } else {
        showDialog(DIALOG_CREATE_OR_DISCARD_DRAFT_MESSAGE);
        // }
    }


    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CREATE_OR_DISCARD_DRAFT_MESSAGE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.save_or_discard_draft_message_dlg_title)
                        .setMessage(R.string.save_or_discard_draft_message_instructions_fmt)
                        .setPositiveButton(R.string.save_draft_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_CREATE_OR_DISCARD_DRAFT_MESSAGE);
                                checkToSaveDraftAndSave();
                            }
                        })
                        .setNegativeButton(R.string.discard_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_CREATE_OR_DISCARD_DRAFT_MESSAGE);
                                onDiscard();
                            }
                        })
                        .create();
            case DIALOG_CONFIRM_DISCARD_ON_BACK:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm_discard_draft_message_title)
                        .setMessage(R.string.confirm_discard_draft_message)
                        .setPositiveButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_CONFIRM_DISCARD_ON_BACK);
                            }
                        })
                        .setNegativeButton(R.string.discard_action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_CONFIRM_DISCARD_ON_BACK);
                                Toast.makeText(SendMailActivity.this,
                                        getString(R.string.message_discarded),
                                        Toast.LENGTH_LONG).show();
                                onDiscard();
                            }
                        })
                        .create();
            case DIALOG_IF_DISCARD_DRAFT_MESSAGE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.email_draft)
                        .setMessage(R.string.select_operation_for_email_draft)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_IF_DISCARD_DRAFT_MESSAGE);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_IF_DISCARD_DRAFT_MESSAGE);
                                onDiscard();
                            }
                        })
                        .create();
            case DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.email_draft)
                        .setMessage(R.string.select_operation_for_email_draft)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE);
                                checkToSaveDraftAndSave();
                            }
                        })
                        .setNeutralButton(R.string.ignore, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismissDialog(DIALOG_SAVE_OR_DISCARD_DRAFT_MESSAGE);
                                onDiscard();
                            }
                        })
                        .create();
        }

        return super.onCreateDialog(id);
    }

    private void checkToSaveDraftAndSave() {
        if (!mAccount.hasDraftsFolder()) {
            Toast.makeText(this, R.string.compose_error_no_draft_folder, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mWaitingForAttachments != WaitingAction.NONE) {
            return;
        }
        if (mNumAttachmentsLoading > 0) {
            mWaitingForAttachments = WaitingAction.SAVE;
            showWaitingForAttachmentDialog();
            return;
        }
        mFinishAfterDraftSaved = true;
        performSaveAfterChecks();
    }

    private void onDiscard() {
        if (mDraftId != INVALID_DRAFT_ID) {
            MessageController.getInstance(getApplication()).deleteDraft(mAccount, mDraftId);
            mDraftId = INVALID_DRAFT_ID;
        }
        mHandler.sendEmptyMessage(MSG_DISCARDED_DRAFT);
        draftNeedsSaving = false;
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_ORG_FOR_CC || requestCode == REQUEST_ORG_FOR_RECEIVER ||
                requestCode == REQUEST_ORG_FOR_BCC) {
            if (resultCode == 1) {
                ArrayList<UserInfo> userInfos = (ArrayList<UserInfo>) intent.getSerializableExtra("userInfos");
                if (userInfos != null && userInfos.size() > 0) {
                    for (int i = 0; i < userInfos.size(); i++) {
                        Recipient recipient = Recipient.fromUserInfo(userInfos.get(i));
                        if (requestCode == REQUEST_ORG_FOR_CC) {
                            edit_cc.addRecipients(recipient);
                        } else if (requestCode == REQUEST_ORG_FOR_BCC) {
                            edit_bcc.addRecipients(recipient);
                        }else {
                            edit_recipients.addRecipients(recipient);
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.no_email_address, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
