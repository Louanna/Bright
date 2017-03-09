package com.movit.platform.mail.provider;


import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.movit.platform.mail.BuildConfig;


public class K9FileProvider extends FileProvider {
    private static String AUTHORITY;

    public static Uri getUriForFile(Context context, File file, String mimeType) {
        Uri uri = FileProvider.getUriForFile(context, AUTHORITY, file);
        return uri.buildUpon().appendQueryParameter("mime_type", mimeType).build();
    }

    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName() + ".fileprovider";
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return uri.getQueryParameter("mime_type");
    }
}
