package com.movit.platform.mail.provider;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.movit.platform.mail.bean.Account;
import com.movit.platform.mail.BuildConfig;
import com.movit.platform.mail.preferences.Preferences;
import com.movit.platform.mail.mailstore.LocalStore;
import com.movit.platform.mail.mailstore.LocalStore.AttachmentInfo;
import org.openintents.openpgp.util.ParcelFileDescriptorUtil;

import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.MimeUtility;


/**
 * A simple ContentProvider that allows file access to attachments.
 */
public class AttachmentProvider extends ContentProvider {
    private static String AUTHORITY;
    public static Uri CONTENT_URI;

    private static final String[] DEFAULT_PROJECTION = new String[] {
            AttachmentProviderColumns._ID,
            AttachmentProviderColumns.DATA,
    };

    public static class AttachmentProviderColumns {
        public static final String _ID = "_id";
        public static final String DATA = "_data";
        public static final String DISPLAY_NAME = "_display_name";
        public static final String SIZE = "_size";
    }


    public static Uri getAttachmentUri(String accountUuid, long id) {
        return CONTENT_URI.buildUpon()
                .appendPath(accountUuid)
                .appendPath(Long.toString(id))
                .build();
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".attachmentprovider";
        CONTENT_URI = Uri.parse("content://" + AUTHORITY);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        List<String> segments = uri.getPathSegments();
        String accountUuid = segments.get(0);
        String id = segments.get(1);
        String mimeType = (segments.size() < 3) ? null : segments.get(2);

        return getType(accountUuid, id, mimeType);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        List<String> segments = uri.getPathSegments();
        String accountUuid = segments.get(0);
        String attachmentId = segments.get(1);

        return openAttachment(accountUuid, attachmentId);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String[] columnNames = (projection == null) ? DEFAULT_PROJECTION : projection;

        List<String> segments = uri.getPathSegments();
        String accountUuid = segments.get(0);
        String id = segments.get(1);

        final AttachmentInfo attachmentInfo;
        try {
            final Account account = Preferences.getPreferences(getContext()).getAccount(accountUuid);
            attachmentInfo = LocalStore.getInstance(account, getContext()).getAttachmentInfo(id);
        } catch (MessagingException e) {
            System.out.println("Unable to retrieve attachment info from local store for ID: " + id);
            return null;
        }
        if (attachmentInfo == null) {
            return null;
        }

        MatrixCursor ret = new MatrixCursor(columnNames);
        Object[] values = new Object[columnNames.length];
        for (int i = 0, count = columnNames.length; i < count; i++) {
            String column = columnNames[i];
            if (AttachmentProviderColumns._ID.equals(column)) {
                values[i] = id;
            } else if (AttachmentProviderColumns.DATA.equals(column)) {
                values[i] = uri.toString();
            } else if (AttachmentProviderColumns.DISPLAY_NAME.equals(column)) {
                values[i] = attachmentInfo.name;
            } else if (AttachmentProviderColumns.SIZE.equals(column)) {
                values[i] = attachmentInfo.size;
            }
        }
        ret.addRow(values);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    private String getType(String accountUuid, String id, String mimeType) {
        String type;
        final Account account = Preferences.getPreferences(getContext()).getAccount(accountUuid);

        try {
            final LocalStore localStore = LocalStore.getInstance(account, getContext());

            AttachmentInfo attachmentInfo = localStore.getAttachmentInfo(id);
            if (mimeType != null) {
                type = mimeType;
            } else {
                type = attachmentInfo.type;
            }
        } catch (MessagingException e) {
            System.out.println("Unable to retrieve LocalStore for " + account);
            type = MimeUtility.DEFAULT_ATTACHMENT_MIME_TYPE;
        }

        return type;
    }

    private ParcelFileDescriptor openAttachment(String accountUuid, String attachmentId) {
        try {
            InputStream inputStream = getAttachmentInputStream(accountUuid, attachmentId);
            return ParcelFileDescriptorUtil.pipeFrom(inputStream);
        } catch (MessagingException e) {
            System.out.println("Error getting InputStream for attachment");
            return null;
        } catch (IOException e) {
            System.out.println("Error creating ParcelFileDescriptor");
            return null;
        }
    }

    private InputStream getAttachmentInputStream(String accountUuid, String attachmentId) throws MessagingException {
        final Account account = Preferences.getPreferences(getContext()).getAccount(accountUuid);
        LocalStore localStore = LocalStore.getInstance(account, getContext());
        return localStore.getAttachmentInputStream(attachmentId);
    }
}
