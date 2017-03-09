package com.movitech.EOP.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Administrator on 2016/6/17.
 */
public class DESUtils {
    private static final String KEY = "1qaz2wsx";
    private static final String ARG1 = "Useraccount=";
    private static final String ARG2 = "&Timestamp=";

    public static String encryptDES(String userName) throws Exception {
        String s = ARG1 + userName + ARG2 + getTimeString();
        System.out.println("s=" + s);
        IvParameterSpec zeroIv = new IvParameterSpec(KEY.getBytes());
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        byte[] encryptedData = cipher.doFinal(s.getBytes());
        return printHexString(encryptedData);
    }

    //将指定byte数组以16进制的形式打印到控制台
    public static String printHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte byt : b) {
            String hex = Integer.toHexString(byt & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
            sb.append("-");
        }
        return sb.substring(0, sb.lastIndexOf("-"));
    }

    private static String getTimeString() {
        Date date = new Date();
        TimeZone pst = TimeZone.getTimeZone("GMT+8:00");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        formatter.setTimeZone(pst);
        return formatter.format(date);
    }
}
