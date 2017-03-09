package com.movit.platform.framework.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.movit.platform.common.constants.CommConstants;

import java.util.HashSet;
import java.util.Set;

public class MFSPHelper {

    private static SharedPreferences sp;

    //在Application中初始化
    public static void initialize(Context context) {
        sp = context.getSharedPreferences(CommConstants.LOGIN_SET,
                Context.MODE_PRIVATE);
    }

    public static String getString(String key) {
        return sp.getString(key, "");
    }

    public static void setString(String key, String value) {
        Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static int getInteger(String key) {
        return sp.getInt(key, -1);
    }

    public static void setInteger(String key, int value) {
        Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean bool) {
        return sp.getBoolean(key, bool);
    }

    public static void setBoolean(String key, Boolean value) {
        Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static Set<String> getStringSet(String key) {
        return sp.getStringSet(key, new HashSet<String>());
    }

    public static void setStringSet(String key, Set<String> value) {
        Editor editor = sp.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public static long getLong(String key) {
        return sp.getLong(key, -1);
    }

    public static void setLong(String key, long value) {
        Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }
}