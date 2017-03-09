package com.movit.platform.mail.crypto;

import android.text.TextUtils;

import com.movit.platform.mail.bean.Identity;

public class OpenPgpApiHelper {

    public static String buildUserId(Identity identity) {
        StringBuilder sb = new StringBuilder();

        String name = identity.getName();
        if (!TextUtils.isEmpty(name)) {
            sb.append(name).append(" ");
        }
        sb.append("<").append(identity.getEmail()).append(">");

        return sb.toString();
    }
}
