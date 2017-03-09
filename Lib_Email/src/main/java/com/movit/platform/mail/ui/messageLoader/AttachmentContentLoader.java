package com.movit.platform.mail.ui.messageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.movit.platform.mail.bean.Attachment;

import org.apache.commons.io.IOUtils;

/**
 * Loader to fetch the content of an attachment.
 *
 * This will copy the data to a temporary file in our app's cache directory.
 */
public class AttachmentContentLoader extends AsyncTaskLoader<Attachment> {
    private static final String FILENAME_PREFIX = "attachment";

    private final Attachment mAttachment;

    public AttachmentContentLoader(Context context, Attachment attachment) {
        super(context);
        mAttachment = attachment;
    }

    @Override
    protected void onStartLoading() {
        if (mAttachment.state == Attachment.LoadingState.COMPLETE) {
            deliverResult(mAttachment);
        }

        if (takeContentChanged() || mAttachment.state == Attachment.LoadingState.METADATA) {
            forceLoad();
        }
    }

    @Override
    public Attachment loadInBackground() {
        Context context = getContext();
        try {
            File file = File.createTempFile(FILENAME_PREFIX, null, context.getCacheDir());
            file.deleteOnExit();
            ContentResolver contentResolver = context.getContentResolver();
            InputStream in = getAttachmentInputStream(mAttachment.uri, contentResolver);
            try {
                FileOutputStream out = new FileOutputStream(file);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

            mAttachment.filename = file.getAbsolutePath();
            mAttachment.state = Attachment.LoadingState.COMPLETE;

            return mAttachment;
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAttachment.filename = null;
        mAttachment.state = Attachment.LoadingState.CANCELLED;

        return mAttachment;
    }

    private InputStream getAttachmentInputStream(@NonNull Uri uri, ContentResolver contentResolver) throws FileNotFoundException {
        if (uri == null) {
            throw new NullPointerException("Argument 'uri' must not be null");
        }

        String scheme = uri.getScheme();
        if (!ContentResolver.SCHEME_FILE.equals(scheme)) {
            return contentResolver.openInputStream(uri);
        }

        File file = new File(uri.getPath());
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

//        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//        int fileUid = getFileUidOrThrow(fileDescriptor);
//        if (fileUid == android.os.Process.myUid()) {
//            throw new FileNotFoundException("File is owned by the application itself");
//        }

        AssetFileDescriptor fd = new AssetFileDescriptor(parcelFileDescriptor, 0, -1);
        try {
            return fd.createInputStream();
        } catch (IOException e) {
            throw new FileNotFoundException("Unable to create stream");
        }
    }

}
