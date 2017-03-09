package com.movit.platform.mail.ui.compose;


import java.util.Arrays;
import java.util.List;

import android.app.PendingIntent;
import android.text.TextWatcher;
import android.widget.Toast;


import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.SendMailActivity;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Message.RecipientType;
import com.movit.platform.mail.ui.view.RecipientSelectView;
import com.movit.platform.mail.bean.Recipient;

public class RecipientMvpView {
    private static final int VIEW_INDEX_HIDDEN = -1;
    private static final int VIEW_INDEX_CRYPTO_STATUS_DISABLED = 0;
    private static final int VIEW_INDEX_CRYPTO_STATUS_ERROR = 1;
    private static final int VIEW_INDEX_CRYPTO_STATUS_NO_RECIPIENTS = 2;
    private static final int VIEW_INDEX_CRYPTO_STATUS_ERROR_NO_KEY = 3;
    private static final int VIEW_INDEX_CRYPTO_STATUS_DISABLED_NO_KEY = 4;
    private static final int VIEW_INDEX_CRYPTO_STATUS_UNTRUSTED = 5;
    private static final int VIEW_INDEX_CRYPTO_STATUS_TRUSTED = 6;
    private static final int VIEW_INDEX_CRYPTO_STATUS_SIGN_ONLY = 7;

    private static final int VIEW_INDEX_BCC_EXPANDER_VISIBLE = 0;
    private static final int VIEW_INDEX_BCC_EXPANDER_HIDDEN = 1;

    private final SendMailActivity activity;
    private final RecipientSelectView toView;
    private final RecipientSelectView ccView;
    private final RecipientSelectView bccView;
    private RecipientPresenter presenter;


    public RecipientMvpView(SendMailActivity activity) {
        this.activity = activity;
        toView = (RecipientSelectView) activity.findViewById(R.id.edit_recipients);
        ccView = (RecipientSelectView) activity.findViewById(R.id.edit_cc);
        bccView = (RecipientSelectView) activity.findViewById(R.id.edit_bcc);
    }

    public void setPresenter(final RecipientPresenter presenter) {
        this.presenter = presenter;
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        toView.addTextChangedListener(textWatcher);
        ccView.addTextChangedListener(textWatcher);
        bccView.addTextChangedListener(textWatcher);
    }

    public void requestFocusOnToField() {
        toView.requestFocus();
    }

    public void requestFocusOnCcField() {
        ccView.requestFocus();
    }

    public void addRecipients(RecipientType recipientType, Recipient... recipients) {
        switch (recipientType) {
            case TO: {
                toView.addRecipients(recipients);
                break;
            }
            case CC: {
                ccView.addRecipients(recipients);
                break;
            }
            case BCC: {
                bccView.addRecipients(recipients);
                break;
            }
        }
    }

    public void showNoRecipientsError() {
        Toast.makeText(activity, R.string.message_compose_error_no_recipients, Toast.LENGTH_SHORT).show();
        //toView.setError(toView.getContext().getString(R.string.message_compose_error_no_recipients));
    }

    public List<Address> getToAddresses() {
        return Arrays.asList(toView.getAddresses());
    }

    public List<Address> getCcAddresses() {
        return Arrays.asList(ccView.getAddresses());
    }

    public List<Address> getBccAddresses() {
        return Arrays.asList(bccView.getAddresses());
    }

    public List<Recipient> getToRecipients() {
        return toView.getObjects();
    }

    public List<Recipient> getCcRecipients() {
        return ccView.getObjects();
    }

    public List<Recipient> getBccRecipients() {
        return bccView.getObjects();
    }

    public boolean recipientToHasUncompletedText() {
        return toView.hasUncompletedText();
    }

    public boolean recipientCcHasUncompletedText() {
        return ccView.hasUncompletedText();
    }


    public void showToUncompletedError() {
        Toast.makeText(activity, R.string.compose_error_incomplete_recipient, Toast.LENGTH_SHORT).show();
        //toView.setError(toView.getContext().getString(R.string.compose_error_incomplete_recipient));
    }

    public void showCcUncompletedError() {
        Toast.makeText(activity, R.string.cc_compose_error_incomplete_recipient, Toast.LENGTH_SHORT).show();
        //ccView.setError(ccView.getContext().getString(R.string.compose_error_incomplete_recipient));
    }

    public void showErrorContactNoAddress() {
        Toast.makeText(activity, R.string.error_contact_address_not_found, Toast.LENGTH_SHORT).show();
    }

    public void launchUserInteractionPendingIntent(PendingIntent pendingIntent, int requestCode) {
        activity.launchUserInteractionPendingIntent(pendingIntent, requestCode);
    }

    public enum CryptoStatusDisplayType {
        UNCONFIGURED(VIEW_INDEX_HIDDEN),
        UNINITIALIZED(VIEW_INDEX_HIDDEN),
        DISABLED(VIEW_INDEX_CRYPTO_STATUS_DISABLED),
        SIGN_ONLY(VIEW_INDEX_CRYPTO_STATUS_SIGN_ONLY),
        OPPORTUNISTIC_EMPTY(VIEW_INDEX_CRYPTO_STATUS_NO_RECIPIENTS),
        OPPORTUNISTIC_NOKEY(VIEW_INDEX_CRYPTO_STATUS_DISABLED_NO_KEY),
        OPPORTUNISTIC_UNTRUSTED(VIEW_INDEX_CRYPTO_STATUS_UNTRUSTED),
        OPPORTUNISTIC_TRUSTED(VIEW_INDEX_CRYPTO_STATUS_TRUSTED),
        PRIVATE_EMPTY(VIEW_INDEX_CRYPTO_STATUS_NO_RECIPIENTS),
        PRIVATE_NOKEY(VIEW_INDEX_CRYPTO_STATUS_ERROR_NO_KEY),
        PRIVATE_UNTRUSTED(VIEW_INDEX_CRYPTO_STATUS_UNTRUSTED),
        PRIVATE_TRUSTED(VIEW_INDEX_CRYPTO_STATUS_TRUSTED),
        ERROR(VIEW_INDEX_CRYPTO_STATUS_ERROR);


        final int childToDisplay;

        CryptoStatusDisplayType(int childToDisplay) {
            this.childToDisplay = childToDisplay;
        }
    }
}
