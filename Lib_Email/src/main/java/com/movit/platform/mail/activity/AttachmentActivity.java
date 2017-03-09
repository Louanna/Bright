package com.movit.platform.mail.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.movit.platform.mail.R;
import com.movit.platform.mail.adapter.AttachmentListAdapter;
import com.movit.platform.mail.controller.AttachmentController;
import com.movit.platform.mail.controller.MessageController;
import com.movit.platform.mail.fragment.ConfirmationDialogFragment;
import com.movit.platform.mail.fragment.ProgressDialogFragment;
import com.movit.platform.mail.mailstore.AttachmentViewInfo;

import java.util.List;
import java.util.Locale;

public class AttachmentActivity extends Activity implements AdapterView.OnItemClickListener,
        ConfirmationDialogFragment.ConfirmationDialogFragmentListener {
    private List<AttachmentViewInfo> attachmentViewInfos;
    private MessageController mController;
    private DownloadManager downloadManager;
    private Handler handler = new Handler();
    private AttachmentViewInfo currentAttachmentViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachmentViewInfos = EmailContentActivity.getAttachmentViewInfo();
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        mController = MessageController.getInstance(this);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_attachment);
        TextView common_top_title = (TextView) findViewById(R.id.common_top_title);
        common_top_title.setText(R.string.attachment_list);
        ImageView common_top_left = (ImageView) findViewById(R.id.common_top_left);
        common_top_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView attachment_list = (ListView) findViewById(R.id.attachment_list);
        AttachmentListAdapter attachmentListAdapter = new AttachmentListAdapter(this, attachmentViewInfos);
        attachment_list.setAdapter(attachmentListAdapter);
        attachment_list.setOnItemClickListener(this);
    }

    public void showAttachmentLoadingDialog() {
        String message = getString(R.string.dialog_attachment_progress_title);
        DialogFragment fragment = ProgressDialogFragment.newInstance(null, message);
        fragment.show(getFragmentManager(), getDialogTag(R.id.dialog_attachment_progress));
    }

    private void showAttachmentDialog(String name) {
        String title = getString(R.string.dialog_confirm_save);
        String message = getString(R.string.dialog_confirm_save_message) + name;
        String confirmText = getString(R.string.dialog_confirm_save_confirm_button);
        String cancelText = getString(R.string.dialog_confirm_save_cancel_button);
        DialogFragment fragment = ConfirmationDialogFragment.newInstance(R.id.dialog_attachment, title, message,
                confirmText, cancelText, null);
        fragment.show(getFragmentManager(), getDialogTag(R.id.dialog_attachment));
    }

    public void hideAttachmentLoadingDialogOnMainThread() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                removeAttachmentDialog(R.id.dialog_attachment_progress);
            }
        });
    }

    private void removeAttachmentDialog(int dialogId) {
        FragmentManager fm = getFragmentManager();
        // Make sure the "show dialog" transaction has been processed when we call
        // findFragmentByTag() below. Otherwise the fragment won't be found and the dialog will
        // never be dismissed.
        fm.executePendingTransactions();
        DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(getDialogTag(dialogId));
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    private String getDialogTag(int dialogId) {
        return String.format(Locale.US, "dialog-%d", dialogId);
    }

    public void runOnMainThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentAttachmentViewInfo = attachmentViewInfos.get(position);
        showAttachmentDialog(currentAttachmentViewInfo.displayName);
    }

    @Override
    public void doPositiveClick(int dialogId) {
        if(dialogId == R.id.dialog_attachment){
            getAttachmentController(currentAttachmentViewInfo).saveAttachment();
        }
    }

    @Override
    public void doNegativeClick(int dialogId) {
        if(dialogId == R.id.dialog_attachment){
            getAttachmentController(currentAttachmentViewInfo).viewAttachment();
        }
    }

    @Override
    public void doNeutralClick(int dialogId){
        /* do nothing */
    }

    @Override
    public void dialogCancelled(int dialogId) {
        /* do nothing */
    }

    private AttachmentController getAttachmentController(AttachmentViewInfo attachment) {
        return new AttachmentController(mController, downloadManager, this, attachment);
    }

}
