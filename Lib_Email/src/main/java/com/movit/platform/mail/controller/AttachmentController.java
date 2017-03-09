package com.movit.platform.mail.controller;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.movit.platform.mail.activity.AttachmentActivity;
import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.R;
import com.movit.platform.mail.cache.TemporaryAttachmentStore;
import com.movit.platform.mail.util.FileHelper;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.Part;
import com.fsck.k9.mail.internet.MimeUtility;
import com.movit.platform.mail.mailstore.AttachmentViewInfo;
import com.movit.platform.mail.mailstore.LocalMessage;
import com.movit.platform.mail.mailstore.LocalPart;

import org.apache.commons.io.IOUtils;


public class AttachmentController {
    private final Context context;
    private final MessageController controller;
    private final AttachmentActivity attachmentActivity;
    private final AttachmentViewInfo attachment;
    private final DownloadManager downloadManager;
    private boolean needView = false;


    public AttachmentController(MessageController controller, DownloadManager downloadManager,
                                AttachmentActivity attachmentActivity, AttachmentViewInfo attachment) {
        this.context = attachmentActivity.getApplicationContext();
        this.controller = controller;
        this.downloadManager = downloadManager;
        this.attachmentActivity = attachmentActivity;
        this.attachment = attachment;
    }

    private boolean needsDownloading() {
        return isPartMissing() && isLocalPart();
    }

    private boolean isPartMissing() {
        return attachment.part.getBody() == null;
    }

    private boolean isLocalPart() {
        return attachment.part instanceof LocalPart;
    }

    public void viewAttachment() {
        if (!attachment.mimeType.split("/")[0].equals("image")) {
            if (needsDownloading()) {
                downloadAndViewAttachment((LocalPart) attachment.part);
            } else {
                viewLocalAttachment();
            }
        } else {
            //小米无法以K9提供的方式直接预览，所以只能先做保存再做展示，如发现更好的办法请替换这部分代码
            needView = true;
            saveAttachment();
        }
    }

    private void downloadAndViewAttachment(LocalPart localPart) {
        downloadAttachment(localPart, new Runnable() {
            @Override
            public void run() {
                viewLocalAttachment();
            }
        });
    }

    private void viewLocalAttachment() {
        new ViewAttachmentAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void saveAttachment() {
        boolean isExternalStorageMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (!isExternalStorageMounted) {
            String message;
            if (needView) {
                message = context.getString(R.string.message_view_status_attachment_not_saved_or_view);
            } else {
                message = context.getString(R.string.message_view_status_attachment_not_saved);
            }
            displayMessageToUser(message);
            return;
        }
        if (needsDownloading()) {
            downloadAndSaveAttachmentTo((LocalPart) attachment.part);
        } else {
            saveLocalAttachment();
        }
    }

    private void downloadAndSaveAttachmentTo(LocalPart localPart) {
        downloadAttachment(localPart, new Runnable() {
            @Override
            public void run() {
                saveAttachment();
            }
        });
    }

    private void downloadAttachment(LocalPart localPart, final Runnable attachmentDownloadedCallback) {
        String accountUuid = localPart.getAccountUuid();
        Account account = Preferences.getPreferences(context).getAccount(accountUuid);
        LocalMessage message = localPart.getMessage();
        attachmentActivity.showAttachmentLoadingDialog();
        controller.loadAttachment(account, message, attachment.part, new MessagingListener() {
            @Override
            public void loadAttachmentFinished(Account account, Message message, Part part) {
                attachmentActivity.hideAttachmentLoadingDialogOnMainThread();
                attachmentActivity.runOnMainThread(attachmentDownloadedCallback);
            }

            @Override
            public void loadAttachmentFailed(Account account, Message message, Part part, String reason) {
                attachmentActivity.hideAttachmentLoadingDialogOnMainThread();
            }
        });
    }

    private void saveLocalAttachment() {
        new SaveAttachmentAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private File saveAttachmentWithUniqueFileName() throws IOException {
        String filename = FileHelper.sanitizeFilename(attachment.displayName);
        File file = new File(MailboxController.getAttachmentDefaultPath() + "/" + filename);
        if (!file.exists()) {
            writeAttachmentToStorage(file);
            addSavedAttachmentToDownloadsDatabase(file);
        }
        return file;
    }


    private void writeAttachmentToStorage(File file) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(attachment.uri);
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                IOUtils.copy(in, out);
                out.flush();
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private void addSavedAttachmentToDownloadsDatabase(File file) {
        String fileName = file.getName();
        String path = file.getAbsolutePath();
        long fileLength = file.length();
        String mimeType = attachment.mimeType;
        downloadManager.addCompletedDownload(fileName, fileName, true, mimeType, path, fileLength, true);
    }

    private Intent getBestViewIntentAndSaveFileIfNecessary() {
        String displayName = attachment.displayName;
        String inferredMimeType = MimeUtility.getMimeTypeByExtension(displayName);
        IntentAndResolvedActivitiesCount resolvedIntentInfo;
        String mimeType = attachment.mimeType;
        if (MimeUtility.isDefaultMimeType(mimeType)) {
            resolvedIntentInfo = getBestViewIntentForMimeType(inferredMimeType);
        } else {
            resolvedIntentInfo = getBestViewIntentForMimeType(mimeType);
            if (!resolvedIntentInfo.hasResolvedActivities() && !inferredMimeType.equals(mimeType)) {
                resolvedIntentInfo = getBestViewIntentForMimeType(inferredMimeType);
            }
        }

        if (!resolvedIntentInfo.hasResolvedActivities()) {
            resolvedIntentInfo = getBestViewIntentForMimeType(MimeUtility.DEFAULT_ATTACHMENT_MIME_TYPE);
        }

        Intent viewIntent;
        if (resolvedIntentInfo.hasResolvedActivities() && resolvedIntentInfo.containsFileUri()) {
            try {
                File tempFile = TemporaryAttachmentStore.getFileForWriting(context, displayName);
                writeAttachmentToStorage(tempFile);
                viewIntent = createViewIntentForFileUri(resolvedIntentInfo.getMimeType(), Uri.fromFile(tempFile));
            } catch (IOException e) {
                viewIntent = createViewIntentForAttachmentProviderUri(MimeUtility.DEFAULT_ATTACHMENT_MIME_TYPE);
            }
        } else {
            viewIntent = resolvedIntentInfo.getIntent();
        }

        return viewIntent;
    }

    private IntentAndResolvedActivitiesCount getBestViewIntentForMimeType(String mimeType) {
        Intent contentUriIntent = createViewIntentForAttachmentProviderUri(mimeType);
        int contentUriActivitiesCount = getResolvedIntentActivitiesCount(contentUriIntent);

        if (contentUriActivitiesCount > 0) {
            return new IntentAndResolvedActivitiesCount(contentUriIntent, contentUriActivitiesCount);
        }

        File tempFile = TemporaryAttachmentStore.getFile(context, attachment.displayName);
        Uri tempFileUri = Uri.fromFile(tempFile);
        Intent fileUriIntent = createViewIntentForFileUri(mimeType, tempFileUri);
        int fileUriActivitiesCount = getResolvedIntentActivitiesCount(fileUriIntent);

        if (fileUriActivitiesCount > 0) {
            return new IntentAndResolvedActivitiesCount(fileUriIntent, fileUriActivitiesCount);
        }

        return new IntentAndResolvedActivitiesCount(contentUriIntent, contentUriActivitiesCount);
    }

    private Intent createViewIntentForAttachmentProviderUri(String mimeType) {
        Uri uri = getAttachmentUriForMimeType(attachment, mimeType);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        addUiIntentFlags(intent);

        return intent;
    }

    private Uri getAttachmentUriForMimeType(AttachmentViewInfo attachment, String mimeType) {
        if (attachment.mimeType.equals(mimeType)) {
            return attachment.uri;
        }

        return attachment.uri.buildUpon()
                .appendPath(mimeType)
                .build();
    }

    private Intent createViewIntentForFileUri(String mimeType, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        addUiIntentFlags(intent);

        return intent;
    }

    private void addUiIntentFlags(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }

    private int getResolvedIntentActivitiesCount(Intent intent) {
        PackageManager packageManager = context.getPackageManager();

        List<ResolveInfo> resolveInfos =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        return resolveInfos.size();
    }

    private void displayAttachmentNotSavedMessage() {
        String message = context.getString(R.string.message_view_status_attachment_not_saved_or_view);
        displayMessageToUser(message);
    }

    private void displayMessageToUser(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private static class IntentAndResolvedActivitiesCount {
        private Intent intent;
        private int activitiesCount;

        IntentAndResolvedActivitiesCount(Intent intent, int activitiesCount) {
            this.intent = intent;
            this.activitiesCount = activitiesCount;
        }

        public Intent getIntent() {
            return intent;
        }

        public boolean hasResolvedActivities() {
            return activitiesCount > 0;
        }

        public String getMimeType() {
            return intent.getType();
        }

        public boolean containsFileUri() {
            return "file".equals(intent.getData().getScheme());
        }
    }

    private class ViewAttachmentAsyncTask extends AsyncTask<Void, Void, Intent> {

        @Override
        protected Intent doInBackground(Void... params) {
            return getBestViewIntentAndSaveFileIfNecessary();
        }

        @Override
        protected void onPostExecute(Intent intent) {
            viewAttachment(intent);
        }

        private void viewAttachment(Intent intent) {
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                String message = context.getString(R.string.message_view_no_viewer, attachment.mimeType);
                displayMessageToUser(message);
            }
        }
    }

    private class SaveAttachmentAsyncTask extends AsyncTask<File, Void, File> {

        @Override
        protected File doInBackground(File... params) {
            try {
                return saveAttachmentWithUniqueFileName();
            } catch (IOException e) {
                System.out.println("Error saving attachment");
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            if (file == null) {
                displayAttachmentNotSavedMessage();
            } else {
                if (needView) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    addUiIntentFlags(intent);
                    context.startActivity(intent);
                } else {
                    String success = context.getString(R.string.save_attachment_successful);
                    displayMessageToUser(success);
                }
            }
        }
    }
}
