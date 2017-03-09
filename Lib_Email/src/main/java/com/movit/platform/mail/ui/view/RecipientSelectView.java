package com.movit.platform.mail.ui.view;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


import com.fsck.k9.mail.Address;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.mail.R;
import com.movit.platform.mail.adapter.RecipientAdapter;
import com.movit.platform.mail.bean.Recipient;
import com.movit.platform.mail.util.SimpleTextWatcher;
import com.tokenautocomplete.TokenCompleteTextView;


public class RecipientSelectView extends TokenCompleteTextView<Recipient> {

    private static final int MINIMUM_LENGTH_FOR_FILTERING = 2;

    private static final String ARG_QUERY = "query";

    private static final int LOADER_ID_FILTERING = 0;
    private static final int LOADER_ID_ALTERNATES = 1;

    private RecipientAdapter adapter;
    private Recipient operationRecipient;
    private boolean attachedToWindow = true;
    private TokenListener<Recipient> listener;
    private Activity activity;

    public RecipientSelectView(Context context) {
        super(context);
        initView(context);
    }

    public RecipientSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RecipientSelectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        // don't allow duplicates, based on equality of recipient objects, which is e-mail addresses
        allowDuplicates(false);
        // if a token is completed, pick an entry based on best guess.
        // Note that we override performCompletion, so this doesn't actually do anything
        performBestGuess(true);
        setThreshold(1);
    }

    public void setInfos(Activity activity, List<Recipient> recipients) {
        this.activity = activity;

        adapter = new RecipientAdapter(activity, recipients);
        setAdapter(adapter);
//        addTextChangedListener(new SimpleTextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (adapter != null) {
//                    String key = getText().toString().replace(",", "").trim();
//                    if (!TextUtils.isEmpty(key)) {
//                        adapter.getFilter().filter(key);
//                    }
//                }
//            }
//        });

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                msgCount++;
                Message msg = new Message();
                msg.what = MSGCODE;
                msg.obj = text;
                mHandler.sendMessageDelayed(msg, 1 * 1000);
            }
        });

        setOnTextChangerListener(new onTextChangerListener() {
            @Override
            public void onTextChanger(String text) {
                if (adapter != null) {
                    String key = getText().toString().replace(",", "").trim();
                    if (!TextUtils.isEmpty(key)) {
                        adapter.getFilter().filter(key);
                    }
                }
            }
        });
    }

    private onTextChangerListener textChangerListener = null;

    public void setOnTextChangerListener(onTextChangerListener listener) {
        this.textChangerListener = listener;
    }

    public interface onTextChangerListener {
        public void onTextChanger(String text);
    }

    private static final int MSGCODE = 80000;
    private int msgCount=0;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSGCODE) {
                if (msgCount == 1) {
                    if (textChangerListener != null) {
                        textChangerListener.onTextChanger((String)msg.obj);
                    }
                    msgCount = 0;
                } else {
                    msgCount--;
                }

            }
        }
    };


    @Override
    protected View getViewForObject(Recipient recipient) {
        View view = inflateLayout();
        RecipientTokenViewHolder holder = new RecipientTokenViewHolder(view);
        view.setTag(holder);
        bindObjectView(recipient, view);
        return view;
    }

    @SuppressLint("InflateParams")
    private View inflateLayout() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        return layoutInflater.inflate(R.layout.recipient_item, null, false);
    }

    private void bindObjectView(Recipient recipient, View view) {
        RecipientTokenViewHolder holder = (RecipientTokenViewHolder) view.getTag();
        holder.vName.setText(recipient.getDisplayNameOrAddress());
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int action = event.getActionMasked();
        Editable text = getText();

        if (text != null && action == MotionEvent.ACTION_UP) {
            int offset = getOffsetForPosition(event.getX(), event.getY());

            if (offset != -1) {
                TokenImageSpan[] links = text.getSpans(offset, offset, RecipientTokenSpan.class);
                if (links.length > 0) {
                    showDialog(links[0].getToken());
                    return true;
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected Recipient defaultObject(String completionText) {
        Address[] parsedAddresses = Address.parse(completionText);
        if (parsedAddresses.length == 0 || parsedAddresses[0].getAddress() == null) {
            return null;
        }

        return new Recipient(parsedAddresses[0]);
    }

    public boolean isEmpty() {
        return getObjects().isEmpty();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    @Override
    public void onFocusChanged(boolean hasFocus, int direction, Rect previous) {
        super.onFocusChanged(hasFocus, direction, previous);
        if (hasFocus) {
            displayKeyboard();
        }
    }

    private void displayKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void performCompletion() {
        Object bestGuess = defaultObject(currentCompletionText());
        if (bestGuess != null) {
            replaceText(convertSelectionToString(bestGuess));
        }
    }

    @Override
    protected void performFiltering(@NonNull CharSequence text, int start, int end, int keyCode) {
        String query = text.subSequence(start, end).toString();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
    }

    public void addRecipients(Recipient... recipients) {
        for (Recipient recipient : recipients) {
            addObject(recipient);
        }
    }

    public Address[] getAddresses() {
        List<Recipient> recipients = getObjects();
        Address[] address = new Address[recipients.size()];
        for (int i = 0; i < address.length; i++) {
            address[i] = recipients.get(i).address;
        }
        return address;
    }

    private void showDialog(Recipient recipient) {
        if (!attachedToWindow) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
        operationRecipient = recipient;
        if (activity != null) {
            String s = activity.getResources().getString(R.string.dialog_delete_recipient) + operationRecipient.address.getPersonal();
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_delete_recipient_title)
                    .setMessage(s)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            removeObject(operationRecipient);
                            operationRecipient = null;
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();

                        }
                    }).create().show();
        }
    }


    public boolean hasUncompletedText() {
        String currentCompletionText = currentCompletionText();
        return !TextUtils.isEmpty(currentCompletionText) && !isPlaceholderText(currentCompletionText);
    }

    static private boolean isPlaceholderText(String currentCompletionText) {
        // TODO string matching here is sort of a hack, but it's somewhat reliable and the info isn't easily available
        return currentCompletionText.startsWith("+") && currentCompletionText.substring(1).matches("[0-9]+");
    }

    /**
     * This method builds the span given a recipient object. We override it with identical
     * functionality, but using the custom RecipientTokenSpan class which allows us to
     * retrieve the view for redrawing at a later point.
     */
    @Override
    protected TokenImageSpan buildSpanForObject(Recipient obj) {
        if (obj == null) {
            return null;
        }

        View tokenView = getViewForObject(obj);
        return new RecipientTokenSpan(tokenView, obj, (int) maxTextWidth());
    }

    /**
     * Find the token view tied to a given recipient. This method relies on spans to
     * be of the RecipientTokenSpan class, as created by the buildSpanForObject method.
     */
    private View getTokenViewForRecipient(Recipient currentRecipient) {
        Editable text = getText();
        if (text == null) {
            return null;
        }
        RecipientTokenSpan[] recipientSpans = text.getSpans(0, text.length(), RecipientTokenSpan.class);
        for (RecipientTokenSpan recipientSpan : recipientSpans) {
            if (recipientSpan.getToken() == currentRecipient) {
                return recipientSpan.view;
            }
        }
        return null;
    }

    /**
     * We use a specialized version of TokenCompleteTextView.TokenListener as well,
     * adding a callback for onTokenChanged.
     */
    public void setTokenListener(TokenListener<Recipient> listener) {
        super.setTokenListener(listener);
        this.listener = listener;
    }

    private class RecipientTokenSpan extends TokenImageSpan {
        private final View view;

        public RecipientTokenSpan(View view, Recipient recipient, int token) {
            super(view, recipient, token);
            this.view = view;
        }
    }

    private static class RecipientTokenViewHolder {
        public final TextView vName;

        RecipientTokenViewHolder(View view) {
            vName = (TextView) view.findViewById(R.id.name);
        }
    }


}
