package com.movit.platform.im.utils;

/**
 * Created by Administrator on 2016/8/1.
 */
public class MyBitmapEntity {

    float x;
    float y;
    float width;
    float height;
    static int devide = 1;
    int index = -1;

    @Override
    public String toString() {
        return "MyBitmap [x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + ", devide=" + devide + ", index="
                + index + "]";
    }
}
