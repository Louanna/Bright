package com.movit.platform.framework.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by Administrator on 2016/8/23.
 */
public class LanguageUtils {

    public static void changeLanguage(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
