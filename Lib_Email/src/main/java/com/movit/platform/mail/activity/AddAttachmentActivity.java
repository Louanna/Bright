package com.movit.platform.mail.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.mail.R;
import com.movit.platform.mail.adapter.AddAttachmentAdapter;
import com.movit.platform.mail.bean.Attachment;
import com.movit.platform.mail.bean.LoadingAttachment;
import com.movit.platform.mail.fragment.ConfirmationDialogFragment;
import com.movit.platform.mail.ui.messageLoader.AttachmentContentLoader;
import com.movit.platform.mail.ui.messageLoader.AttachmentInfoLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddAttachmentActivity extends Activity implements AdapterView.OnItemClickListener,
        ConfirmationDialogFragment.ConfirmationDialogFragmentListener {

    private LoaderManager.LoaderCallbacks<Attachment> mAttachmentInfoLoaderCallback =
            new LoaderManager.LoaderCallbacks<Attachment>() {
                @Override
                public Loader<Attachment> onCreateLoader(int id, Bundle args) {
                    SendMailActivity.mNumAttachmentsLoading++;
                    Attachment attachment = args.getParcelable(LOADER_ARG_ATTACHMENT);
                    return new AttachmentInfoLoader(AddAttachmentActivity.this, attachment);
                }

                @Override
                public void onLoadFinished(Loader<Attachment> loader, Attachment attachment) {
                    attachment.loaderId = ++SendMailActivity.mMaxLoaderId;
                    LoadingAttachment loadingAttachment = new LoadingAttachment();
                    loadingAttachment.name = attachment.name;
                    loadingAttachment.loadId = attachment.loaderId;
                    if(currentSize + attachment.size < MaxSize){
                        currentSize = currentSize + attachment.size;
                        loadingAttachmentList.add(loadingAttachment);
                        addAttachmentAdapter.notifyDataSetChanged();
                        initAttachmentContentLoader(attachment);
                    }else{
                        Toast.makeText(AddAttachmentActivity.this,R.string.attachment_too_much,Toast.LENGTH_SHORT).show();
                    }
                    int loaderId = loader.getId();
                    getLoaderManager().destroyLoader(loaderId);
                }

                @Override
                public void onLoaderReset(Loader<Attachment> loader) {
                    onFetchAttachmentFinished();
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
                    return new AttachmentContentLoader(AddAttachmentActivity.this, attachment);
                }

                @Override
                public void onLoadFinished(Loader<Attachment> loader, Attachment attachment) {
                    int loaderId = loader.getId();
                    for (int i = 0; i < loadingAttachmentList.size(); i++) {
                        if (loadingAttachmentList.get(i).loadId == loaderId) {
                            loadingAttachmentList.get(i).isLoading = false;
                        }
                        break;
                    }
                    addAttachmentAdapter.notifyDataSetChanged();
                    attachment.loaderId = loaderId;
                    onFetchAttachmentSuccess(attachment);
                    getLoaderManager().destroyLoader(loaderId);
                    onFetchAttachmentFinished();
                }

                @Override
                public void onLoaderReset(Loader<Attachment> loader) {
                    onFetchAttachmentFinished();
                }
            };

    private ListView attachment_list;
    private List<LoadingAttachment> loadingAttachmentList = new ArrayList<>();
    private AddAttachmentAdapter addAttachmentAdapter;
    private int deletePosition;
    private static final int ACTIVITY_REQUEST_PICK_ATTACHMENT = 1;
    private static final String LOADER_ARG_ATTACHMENT = "attachment";
    public static final String LOADING_ATTACHMENT = "loading_attachment";
    public static final String SAVE_ATTACHMENT = "save_attachment";
    public static final String DELETE_ATTACHMENT = "delete_attachment";
    private final long MaxSize = 10485760;
    private long currentSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Attachment> attachments = getIntent().getParcelableArrayListExtra("list");
        if (attachments != null) {
            for (int i = 0; i < attachments.size(); i++) {
                LoadingAttachment loadingAttachment = new LoadingAttachment();
                loadingAttachment.name = attachments.get(i).name;
                loadingAttachment.loadId = attachments.get(i).loaderId;
                loadingAttachment.isLoading = false;
                loadingAttachmentList.add(loadingAttachment);
                currentSize = currentSize + attachments.get(i).size;
            }
        }
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_add_attachment);
        ImageView common_top_left = (ImageView) findViewById(R.id.common_top_left);
        common_top_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView common_top_txt_right = (TextView) findViewById(R.id.common_top_txt_right);
        common_top_txt_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAttachment();
            }
        });
        attachment_list = (ListView) findViewById(R.id.attachment_list);
        addAttachmentAdapter = new AddAttachmentAdapter(this, loadingAttachmentList);
        attachment_list.setAdapter(addAttachmentAdapter);
        attachment_list.setOnItemClickListener(this);
    }

    private void addAttachment() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, null), ACTIVITY_REQUEST_PICK_ATTACHMENT);
    }

    private void showAttachmentDialog() {
        String title = getString(R.string.dialog_confirm_delete_title);
        String message = getString(R.string.dialog_confirm_delete_attachment);
        String confirmText = getString(R.string.dialog_confirm_delete_confirm_button);
        String cancelText = getString(R.string.dialog_confirm_delete_cancel_button);
        DialogFragment fragment = ConfirmationDialogFragment.newInstance(R.id.dialog_delete_attachment, title, message,
                confirmText, cancelText, null);
        fragment.show(getFragmentManager(), getDialogTag(R.id.dialog_delete_attachment));
    }

    private String getDialogTag(int dialogId) {
        return String.format(Locale.US, "dialog-%d", dialogId);
    }

    @Override
    public void doPositiveClick(int dialogId) {
        if (dialogId == R.id.dialog_delete_attachment) {
            deleteAttachment();
        }
    }

    @Override
    public void doNegativeClick(int dialogId) {
        /* do nothing */
    }

    @Override
    public void doNeutralClick(int dialogId) {
        /* do nothing */
    }

    @Override
    public void dialogCancelled(int dialogId) {
        /* do nothing */
    }

    private void deleteAttachment() {
        LoadingAttachment loadingAttachment = (LoadingAttachment) addAttachmentAdapter.getItem(deletePosition);
        int deleteId = loadingAttachment.loadId;
        Intent intent = new Intent();
        intent.setAction(DELETE_ATTACHMENT);
        intent.putExtra("id", deleteId);
        sendBroadcast(intent);
        loadingAttachmentList.remove(deletePosition);
        addAttachmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LoadingAttachment loadingAttachment = (LoadingAttachment) addAttachmentAdapter.getItem(deletePosition);
        if (!loadingAttachment.isLoading) {
            deletePosition = position;
            showAttachmentDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case ACTIVITY_REQUEST_PICK_ATTACHMENT:
                addAttachmentsFromResultIntent(data);
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void addAttachmentsFromResultIntent(Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int i = 0, end = clipData.getItemCount(); i < end; i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (uri != null) {
                        addAttachment(uri);
                    }
                }
                return;
            }
        }
        Uri uri = data.getData();
        if (uri != null) {
            addAttachment(uri);
        }
    }

    private void addAttachment(Uri uri) {
        Attachment attachment = new Attachment();
        attachment.state = Attachment.LoadingState.URI_ONLY;
        attachment.uri = uri;
        attachment.contentType = null;
        //the loaderId is id of Loader,every attachment need two Loaders(information and content)
        attachment.loaderId = ++SendMailActivity.mMaxLoaderId;
        initAttachmentInfoLoader(attachment);
    }

    private void initAttachmentInfoLoader(Attachment attachment) {
        LoaderManager loaderManager = getLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putParcelable(LOADER_ARG_ATTACHMENT, attachment);
        loaderManager.initLoader(attachment.loaderId, bundle, mAttachmentInfoLoaderCallback);
    }

    private void onFetchAttachmentFinished() {
        Intent intent = new Intent();
        intent.setAction(LOADING_ATTACHMENT);
        sendBroadcast(intent);
    }

    private void onFetchAttachmentSuccess(Attachment attachment) {
        Intent intent = new Intent();
        intent.setAction(SAVE_ATTACHMENT);
        intent.putExtra("attachment", attachment);
        sendBroadcast(intent);
    }

}
