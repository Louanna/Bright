package com.movit.platform.mail.ui.compose;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.Message.RecipientType;
import com.fsck.k9.mail.MessagingException;
import com.movit.platform.mail.activity.compose.ComposeCryptoStatus;
import com.movit.platform.mail.activity.compose.ComposeCryptoStatus.ComposeCryptoStatusBuilder;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.bean.Identity;
import com.movit.platform.mail.bean.Recipient;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.message.ComposePgpInlineDecider;
import com.movit.platform.mail.ui.messageLoader.RecipientLoader;
import com.movit.platform.mail.util.MailTo;
import com.movit.platform.mail.util.ReplyToParser;
import com.movit.platform.mail.util.Utility;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpApi.PermissionPingCallback;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.openintents.openpgp.util.OpenPgpServiceConnection.OnBound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipientPresenter implements PermissionPingCallback {
    private static final String STATE_KEY_CC_SHOWN = "state:ccShown";
    private static final String STATE_KEY_BCC_SHOWN = "state:bccShown";
    private static final String STATE_KEY_LAST_FOCUSED_TYPE = "state:lastFocusedType";
    private static final String STATE_KEY_CURRENT_CRYPTO_MODE = "state:currentCryptoMode";
    private static final String STATE_KEY_CRYPTO_ENABLE_PGP_INLINE = "state:cryptoEnablePgpInline";

    private static final int CONTACT_PICKER_TO = 1;
    private static final int CONTACT_PICKER_CC = 2;
    private static final int CONTACT_PICKER_BCC = 3;
    private static final int OPENPGP_USER_INTERACTION = 4;


    // transient state, which is either obtained during construction and initialization, or cached
    private final Context context;
    private final RecipientMvpView recipientMvpView;
    private final ComposePgpInlineDecider composePgpInlineDecider;
    private Account account;
    private String cryptoProvider;
    private Boolean hasContactPicker;
    private ComposeCryptoStatus cachedCryptoStatus;
    private PendingIntent pendingUserInteractionIntent;
    private CryptoProviderState cryptoProviderState = CryptoProviderState.UNCONFIGURED;
    private OpenPgpServiceConnection openPgpServiceConnection;


    // persistent state, saved during onSaveInstanceState
    private RecipientType lastFocusedType = RecipientType.TO;
    // TODO initialize cryptoMode to other values under some circumstances, e.g. if we reply to an encrypted e-mail
    private CryptoMode currentCryptoMode = CryptoMode.OPPORTUNISTIC;
    private boolean cryptoEnablePgpInline = false;


    public RecipientPresenter(Context context, RecipientMvpView recipientMvpView, Account account) {
        this.recipientMvpView = recipientMvpView;
        this.context = context;
        composePgpInlineDecider = new ComposePgpInlineDecider();
        recipientMvpView.setPresenter(this);
        onSwitchAccount(account);
        updateCryptoStatus();
    }

    public List<Address> getToAddresses() {
        return recipientMvpView.getToAddresses();
    }

    public List<Address> getCcAddresses() {
        return recipientMvpView.getCcAddresses();
    }

    public List<Address> getBccAddresses() {
        return recipientMvpView.getBccAddresses();
    }
    public List<Recipient> getAllRecipients() {
        ArrayList<Recipient> result = new ArrayList<>();
        result.addAll(recipientMvpView.getToRecipients());
        result.addAll(recipientMvpView.getCcRecipients());
        result.addAll(recipientMvpView.getBccRecipients());
        return result;
    }

    public boolean checkRecipientsOkForSending() {
        if (recipientMvpView.recipientToHasUncompletedText()) {
            recipientMvpView.showToUncompletedError();
            return true;
        }

        if (recipientMvpView.recipientCcHasUncompletedText()) {
            recipientMvpView.showCcUncompletedError();
            return true;
        }

        if (getToAddresses().isEmpty() && getCcAddresses().isEmpty()) {
            recipientMvpView.showNoRecipientsError();
            return true;
        }
        return false;
    }

    public void initFromReplyAllToMessage(Message message) {

        Address myAddress = new Address(account.getEmail(),account.getName());
        Address[] replyToAddresses = ReplyToParser.getRecipientsToReplyAllTo(myAddress,message);

        try {
            // if we're replying to a message we sent, we probably meant
            // to reply to the recipient of that message
            if (account.isAnIdentity(replyToAddresses)) {
                // FIXME: 16/10/18 by zoro  Tag1:目前需求 '已发送'文件 回复邮件也只回给发件人
                //replyToAddresses = message.getRecipients(RecipientType.TO);
            }

            addRecipientsFromAddresses(RecipientType.TO, replyToAddresses);
// FIXME: 16/10/18 by zoro  Tag1
//            if (message.getReplyTo().length > 0) {
//                for (Address address : message.getFrom()) {
//                    if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                        addRecipientsFromAddresses(RecipientType.TO, address);
//                    }
//                }
//            }
// FIXME: 16/10/18 by zoro  Tag1
//            for (Address address : message.getRecipients(RecipientType.TO)) {
//                if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                    addToAddresses(address);
//                }
//
//            }

            if (message.getRecipients(RecipientType.CC).length > 0) {
                for (Address address : message.getRecipients(RecipientType.CC)) {
                    if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
                        addCcAddresses(address);
                    }

                }
            }

//            if (message.getRecipients(RecipientType.BCC).length > 0) {
//                          for (Address address : message.getRecipients(RecipientType.BCC)) {
//                              if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                                  addcAddresses(address);
//                              }
//
//                         }
//                   }
            boolean shouldSendAsPgpInline = composePgpInlineDecider.shouldReplyInline(message);
            if (shouldSendAsPgpInline) {
                cryptoEnablePgpInline = true;
            }

        } catch (Exception e) {
            // can't happen, we know the recipient types exist
            throw new AssertionError(e);
        }
    }

    public void initFromReplyToMessage(Message message) {
        Address[] replyToAddresses = ReplyToParser.getRecipientsToReplyTo(message);

        try {
            // if we're replying to a message we sent, we probably meant
            // to reply to the recipient of that message
            if (account.isAnIdentity(replyToAddresses)) {
                // FIXME: 16/10/18 by zoro  Tag1:目前需求 '已发送'文件 回复邮件也只回给发件人
                //replyToAddresses = message.getRecipients(RecipientType.TO);
            }

            addRecipientsFromAddresses(RecipientType.TO, replyToAddresses);
// FIXME: 16/10/18 by zoro  Tag1
//            if (message.getReplyTo().length > 0) {
//                for (Address address : message.getFrom()) {
//                    if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                        addRecipientsFromAddresses(RecipientType.TO, address);
//                    }
//                }
//            }
// FIXME: 16/10/18 by zoro  Tag1
//            for (Address address : message.getRecipients(RecipientType.TO)) {
//                if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                    addToAddresses(address);
//                }
//
//            }
// FIXME: 16/10/18 by zoro  Tag1
//            if (message.getRecipients(RecipientType.CC).length > 0) {
//                for (Address address : message.getRecipients(RecipientType.CC)) {
//                    if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                        addCcAddresses(address);
//                    }
//
//                }
//            }

//            if (message.getRecipients(RecipientType.BCC).length > 0) {
//                          for (Address address : message.getRecipients(RecipientType.BCC)) {
//                              if (!account.isAnIdentity(address) && !Utility.arrayContains(replyToAddresses, address)) {
//                                  addcAddresses(address);
//                              }
//
//                         }
//                   }
            boolean shouldSendAsPgpInline = composePgpInlineDecider.shouldReplyInline(message);
            if (shouldSendAsPgpInline) {
                cryptoEnablePgpInline = true;
            }

        } catch (Exception e) {
            // can't happen, we know the recipient types exist
            throw new AssertionError(e);
        }
    }

    public void initFromMailto(MailTo mailTo) {
        addToAddresses(mailTo.getTo());
        addCcAddresses(mailTo.getCc());
        addCcAddresses(mailTo.getBcc());
    }

    public void initFromSendOrViewIntent(Intent intent) {
        String[] extraEmail = intent.getStringArrayExtra(Intent.EXTRA_EMAIL);
        String[] extraCc = intent.getStringArrayExtra(Intent.EXTRA_CC);
        String[] extraBcc = intent.getStringArrayExtra(Intent.EXTRA_BCC);
        if (extraEmail != null) {
            addToAddresses(addressFromStringArray(extraEmail));
        }
        if (extraCc != null) {
            addCcAddresses(addressFromStringArray(extraCc));
        }

        if (extraBcc != null) {
            addBccAddresses(addressFromStringArray(extraBcc));
        }
    }

    public void initFromDraftMessage(LocalMessage message) {
        initRecipientsFromDraftMessage(message);
        initPgpInlineFromDraftMessage(message);
    }

    private void initRecipientsFromDraftMessage(LocalMessage message) {
        try {
            addToAddresses(message.getRecipients(RecipientType.TO));

            Address[] ccRecipients = message.getRecipients(RecipientType.CC);
            addCcAddresses(ccRecipients);
            Address[] bccRecipients = message.getRecipients(RecipientType.BCC);
            addBccAddresses(bccRecipients);
        } catch (MessagingException e) {
            // can't happen, we know the recipient types exist
            throw new AssertionError(e);
        }
    }

    private void initPgpInlineFromDraftMessage(LocalMessage message) {
        cryptoEnablePgpInline = message.isSet(Flag.X_DRAFT_OPENPGP_INLINE);
    }

    void addToAddresses(Address... toAddresses) {
        addRecipientsFromAddresses(RecipientType.TO, toAddresses);
    }

    void addCcAddresses(Address... ccAddresses) {
        if (ccAddresses.length > 0) {
            addRecipientsFromAddresses(RecipientType.CC, ccAddresses);
        }
    }

    void addBccAddresses(Address... bccAddresses) {
        if (bccAddresses.length > 0) {
            addRecipientsFromAddresses(RecipientType.BCC, bccAddresses);
        }
    }

    public void onSwitchAccount(Account account) {
        this.account = account;
        String cryptoProvider = account.getOpenPgpProvider();
        setCryptoProvider(cryptoProvider);
    }

    @SuppressWarnings("UnusedParameters")
    public void onSwitchIdentity(Identity identity) {

        // TODO decide what actually to do on identity switch?
        /*
        if (mIdentityChanged) {
            mBccWrapper.setVisibility(View.VISIBLE);
        }
        mBccView.setText("");
        mBccView.addAddress(new Address(mAccount.getAlwaysBcc(), ""));
        */

    }

    private static Address[] addressFromStringArray(String[] addresses) {
        return addressFromStringArray(Arrays.asList(addresses));
    }

    private static Address[] addressFromStringArray(List<String> addresses) {
        ArrayList<Address> result = new ArrayList<>(addresses.size());

        for (String addressStr : addresses) {
            Collections.addAll(result, Address.parseUnencoded(addressStr));
        }

        return result.toArray(new Address[result.size()]);
    }


    public void updateCryptoStatus() {
        cachedCryptoStatus = null;

        boolean isOkStateButLostConnection = cryptoProviderState == CryptoProviderState.OK &&
                (openPgpServiceConnection == null || !openPgpServiceConnection.isBound());
        if (isOkStateButLostConnection) {
            cryptoProviderState = CryptoProviderState.LOST_CONNECTION;
            pendingUserInteractionIntent = null;
        }
    }

    public ComposeCryptoStatus getCurrentCryptoStatus() {
        if (cachedCryptoStatus == null) {
            ComposeCryptoStatusBuilder builder = new ComposeCryptoStatusBuilder()
                    .setCryptoProviderState(cryptoProviderState)
                    .setCryptoMode(currentCryptoMode)
                    .setEnablePgpInline(cryptoEnablePgpInline)
                    .setRecipients(getAllRecipients());

            long accountCryptoKey = account.getCryptoKey();
            if (accountCryptoKey != Account.NO_OPENPGP_KEY) {
                // TODO split these into individual settings? maybe after key is bound to identity
                builder.setSigningKeyId(accountCryptoKey);
                builder.setSelfEncryptId(accountCryptoKey);
            }

            cachedCryptoStatus = builder.build();
        }

        return cachedCryptoStatus;
    }

    public boolean isForceTextMessageFormat() {
        ComposeCryptoStatus cryptoStatus = getCurrentCryptoStatus();
        return cryptoStatus.isEncryptionEnabled() || cryptoStatus.isSigningEnabled();
    }

    public boolean isAllowSavingDraftRemotely() {
        ComposeCryptoStatus cryptoStatus = getCurrentCryptoStatus();
        return cryptoStatus.isEncryptionEnabled() || cryptoStatus.isSigningEnabled();
    }

    @SuppressWarnings("UnusedParameters")
    public void onToTokenAdded(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onToTokenRemoved(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onToTokenChanged(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onCcTokenAdded(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onCcTokenRemoved(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onCcTokenChanged(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onBccTokenAdded(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onBccTokenRemoved(Recipient recipient) {
        updateCryptoStatus();
    }

    @SuppressWarnings("UnusedParameters")
    public void onBccTokenChanged(Recipient recipient) {
        updateCryptoStatus();
    }

    public void onCryptoModeChanged(CryptoMode cryptoMode) {
        currentCryptoMode = cryptoMode;
        updateCryptoStatus();
    }

    public void onCryptoPgpInlineChanged(boolean enablePgpInline) {
        cryptoEnablePgpInline = enablePgpInline;
        updateCryptoStatus();
    }

    private void addRecipientsFromAddresses(final RecipientType recipientType, final Address... addresses) {
        new RecipientLoader(context, cryptoProvider, addresses) {
            @Override
            public void deliverResult(List<Recipient> result) {
                Recipient[] recipientArray = result.toArray(new Recipient[result.size()]);
                recipientMvpView.addRecipients(recipientType, recipientArray);
                stopLoading();
                abandon();
            }
        }.startLoading();
    }

    private void addRecipientFromContactUri(final RecipientType recipientType, final Uri uri) {
        new RecipientLoader(context, cryptoProvider, uri, false) {
            @Override
            public void deliverResult(List<Recipient> result) {
                // TODO handle multiple available mail addresses for a contact?
                if (result.isEmpty()) {
                    recipientMvpView.showErrorContactNoAddress();
                    return;
                }

                Recipient recipient = result.get(0);
                recipientMvpView.addRecipients(recipientType, recipient);

                stopLoading();
                abandon();
            }
        }.startLoading();
    }

    private void setCryptoProvider(String cryptoProvider) {

        boolean providerIsBound = openPgpServiceConnection != null && openPgpServiceConnection.isBound();
        boolean isSameProvider = cryptoProvider != null && cryptoProvider.equals(this.cryptoProvider);
        if (isSameProvider && providerIsBound) {
            cryptoProviderBindOrCheckPermission();
            return;
        }

        if (providerIsBound) {
            openPgpServiceConnection.unbindFromService();
            openPgpServiceConnection = null;
        }

        this.cryptoProvider = cryptoProvider;

        if (cryptoProvider == null) {
            cryptoProviderState = CryptoProviderState.UNCONFIGURED;
            return;
        }

        cryptoProviderState = CryptoProviderState.UNINITIALIZED;
        openPgpServiceConnection = new OpenPgpServiceConnection(context, cryptoProvider, new OnBound() {
            @Override
            public void onBound(IOpenPgpService2 service) {
                cryptoProviderBindOrCheckPermission();
            }

            @Override
            public void onError(Exception e) {
                onCryptoProviderError(e);
            }
        });
        cryptoProviderBindOrCheckPermission();
    }

    private void cryptoProviderBindOrCheckPermission() {
        if (openPgpServiceConnection == null) {
            cryptoProviderState = CryptoProviderState.UNCONFIGURED;
            return;
        }

        if (!openPgpServiceConnection.isBound()) {
            pendingUserInteractionIntent = null;
            openPgpServiceConnection.bindToService();
            return;
        }

        if (pendingUserInteractionIntent != null) {
            recipientMvpView
                    .launchUserInteractionPendingIntent(pendingUserInteractionIntent, OPENPGP_USER_INTERACTION);
            pendingUserInteractionIntent = null;
            return;
        }

        getOpenPgpApi().checkPermissionPing(this);
    }

    private void onCryptoProviderError(Exception e) {
        // TODO handle error case better
        cryptoProviderState = CryptoProviderState.ERROR;
        updateCryptoStatus();
    }

    @Override
    public void onPgpPermissionCheckResult(Intent result) {
        int resultCode = result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR);
        switch (resultCode) {
            case OpenPgpApi.RESULT_CODE_SUCCESS:
                cryptoProviderState = CryptoProviderState.OK;
                break;
            case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
                pendingUserInteractionIntent = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
                cryptoProviderState = CryptoProviderState.ERROR;
                break;

            case OpenPgpApi.RESULT_CODE_ERROR:
            default:
                cryptoProviderState = CryptoProviderState.ERROR;
                break;
        }
        updateCryptoStatus();
    }

    public void onActivityDestroy() {
        if (openPgpServiceConnection != null && openPgpServiceConnection.isBound()) {
            openPgpServiceConnection.unbindFromService();
        }
        openPgpServiceConnection = null;
    }

    public OpenPgpApi getOpenPgpApi() {
        if (openPgpServiceConnection == null || !openPgpServiceConnection.isBound()) {

        }
        return new OpenPgpApi(context, openPgpServiceConnection.getService());
    }

    public enum CryptoProviderState {
        UNCONFIGURED,
        UNINITIALIZED,
        LOST_CONNECTION,
        ERROR,
        OK
    }

    public enum CryptoMode {
        DISABLE,
        SIGN_ONLY,
        OPPORTUNISTIC,
        PRIVATE,
    }
}
