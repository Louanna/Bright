package com.movit.platform.mail.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ConfirmationDialogFragment extends DialogFragment implements OnClickListener,
        OnCancelListener {
    private ConfirmationDialogFragmentListener mListener;

    private static final String ARG_DIALOG_ID = "dialog_id";
    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CONFIRM_TEXT = "confirm";
    private static final String ARG_CANCEL_TEXT = "cancel";
    private static final String ARG_NEUTRAL_TEXT = "neutral";


    public static ConfirmationDialogFragment newInstance(int dialogId, String title, String message,
                                                         String confirmText, String cancelText, String neuralText) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DIALOG_ID, dialogId);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_CONFIRM_TEXT, confirmText);
        args.putString(ARG_CANCEL_TEXT, cancelText);
        args.putString(ARG_NEUTRAL_TEXT, neuralText);
        fragment.setArguments(args);
        return fragment;
    }

    public interface ConfirmationDialogFragmentListener {
        void doPositiveClick(int dialogId);

        void doNegativeClick(int dialogId);

        void doNeutralClick(int dialogId);

        void dialogCancelled(int dialogId);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE);
        String message = args.getString(ARG_MESSAGE);
        String confirmText = args.getString(ARG_CONFIRM_TEXT, null);
        String cancelText = args.getString(ARG_CANCEL_TEXT, null);
        String neutralText = args.getString(ARG_NEUTRAL_TEXT, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        if (confirmText != null) {
            builder.setPositiveButton(confirmText, this);
        }
        if (cancelText != null) {
            builder.setNegativeButton(cancelText, this);
        }
        if (neutralText != null) {
            builder.setNeutralButton(neutralText, this);
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                getListener().doPositiveClick(getDialogId());
                break;
            }
            case DialogInterface.BUTTON_NEGATIVE: {
                getListener().doNegativeClick(getDialogId());
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                getListener().doNeutralClick(getDialogId());
                break;
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getListener().dialogCancelled(getDialogId());
    }

    private int getDialogId() {
        return getArguments().getInt(ARG_DIALOG_ID);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ConfirmationDialogFragmentListener) activity;
        } catch (ClassCastException e) {
            System.out.println(" don't implement ConfirmationDialogFragmentListener");
        }
    }

    private ConfirmationDialogFragmentListener getListener() {
        if (mListener != null) {
            return mListener;
        }

        // fallback to getTargetFragment...
        try {
            return (ConfirmationDialogFragmentListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().getClass() +
                    " must implement ConfirmationDialogFragmentListener");
        }
    }
}
