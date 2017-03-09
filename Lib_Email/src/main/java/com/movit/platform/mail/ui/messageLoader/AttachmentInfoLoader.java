package com.movit.platform.mail.ui.messageLoader;

import java.io.File;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.movit.platform.mail.bean.Attachment;
import com.fsck.k9.mail.internet.MimeUtility;

/**
 * Loader to fetch metadata of an attachment.
 */
public class AttachmentInfoLoader  extends AsyncTaskLoader<Attachment> {
    private final Attachment mAttachment;

    public AttachmentInfoLoader(Context context, Attachment attachment) {
        super(context);
        mAttachment = attachment;
    }

    @Override
    protected void onStartLoading() {
        if (mAttachment.state == Attachment.LoadingState.METADATA) {
            deliverResult(mAttachment);
        }

        if (takeContentChanged() || mAttachment.state == Attachment.LoadingState.URI_ONLY) {
            forceLoad();
        }
    }

    @Override
    public Attachment loadInBackground() {
        Uri uri = mAttachment.uri;
        String contentType = mAttachment.contentType;

        long size = -1;
        String name = null;

        ContentResolver contentResolver = getContext().getContentResolver();

        Cursor metadataCursor = contentResolver.query(
                uri,
                new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE },
                null,
                null,
                null);

        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    name = metadataCursor.getString(0);
                    size = metadataCursor.getInt(1);
                }
            } finally {
                metadataCursor.close();
            }
        }

        if (name == null) {
            name = uri.getLastPathSegment();
        }

        String usableContentType = contentResolver.getType(uri);
        if (usableContentType == null && contentType != null && contentType.indexOf('*') != -1) {
            usableContentType = contentType;
        }

        if (usableContentType == null) {
            usableContentType = MimeUtility.getMimeTypeByExtension(name);
        }

        if (size <= 0) {
            String uriString = uri.toString();
            if (uriString.startsWith("file://")) {
                File f = new File(uriString.substring("file://".length()));
                size = f.length();
            } else {
                System.out.println("Not a file: " + uriString);
            }
        } else {
            System.out.println("old attachment.size: " + size);
        }
        mAttachment.contentType = usableContentType;
        mAttachment.name = name;
        mAttachment.size = size;
        mAttachment.state = Attachment.LoadingState.METADATA;

        return mAttachment;
    }
}
