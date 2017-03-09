package com.movit.platform.im.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.content.Context;

public class PropertiesUtil {

    private static final String PROPERTY_FILE = "c://nine_rect.properties";

    /**
     * @param key
     * @return
     */
    public static String readData(Context mContext, String key, int resId) {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(mContext.getResources().openRawResource(resId));
            props.load(in);
            in.close();
            String value = props.getProperty(key);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param key
     * @param value
     */
    public static void writeData(String key, String value) {
        Properties prop = new Properties();
        try {
            File file = new File(PROPERTY_FILE);
            if (!file.exists())
                file.createNewFile();
            InputStream fis = new FileInputStream(file);
            prop.load(fis);
            fis.close();// һ��Ҫ���޸�ֵ֮ǰ�ر�fis
            OutputStream fos = new FileOutputStream(PROPERTY_FILE);
            prop.setProperty(key, value);
            prop.store(fos, "Update '" + key + "' value");
            fos.close();
        } catch (IOException e) {
            System.err.println("Visit " + PROPERTY_FILE + " for updating "
                    + value + " value error");
        }
    }
}
