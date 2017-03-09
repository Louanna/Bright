package com.movit.platform.mail.util;

import android.content.Context;
import android.util.TypedValue;

import com.movit.platform.mail.R;
import com.movit.platform.mail.activity.compose.ContactPictureLoader;

public class ContactPicture {

    public static ContactPictureLoader getContactPictureLoader(Context context) {
        final int defaultBgColor;
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.contactPictureFallbackDefaultBackgroundColor,
                outValue, true);
        defaultBgColor = outValue.data;
        return new ContactPictureLoader(context, defaultBgColor);
    }
}
