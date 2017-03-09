package com.movit.platform.im.utils;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;

public class BitmapUtil {

    public static Bitmap getCombineBitmaps(List<MyBitmapEntity> mEntityList,
                                           Bitmap... bitmaps) {
        Bitmap newBitmap = Bitmap.createBitmap(200, 200, Config.ARGB_8888);
        for (int i = 0; i < mEntityList.size(); i++) {
            newBitmap = mixtureBitmap(newBitmap, bitmaps[i], new PointF(
                    mEntityList.get(i).x, mEntityList.get(i).y));
        }
        return newBitmap;
    }

    public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
                                       PointF fromPoint) {
        if (first == null || second == null || fromPoint == null) {
            return null;
        }
        Bitmap newBitmap = Bitmap.createBitmap(first.getWidth(),
                first.getHeight(), Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        cv.drawBitmap(first, 0, 0, null);
        cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        return newBitmap;
    }

    public static List<MyBitmapEntity> getBitmapEntitys(Context mContext, int count) {
        List<MyBitmapEntity> mList = new LinkedList<MyBitmapEntity>();
        String value = PropertiesUtil.readData(mContext, String.valueOf(count), R.raw.data);
        if (StringUtils.notEmpty(value)){
            String[] arr1 = value.split(";");
            int length = arr1.length;
            for (int i = 0; i < length; i++) {
                String content = arr1[i];
                String[] arr2 = content.split(",");
                MyBitmapEntity entity = null;
                for (int j = 0; j < arr2.length; j++) {
                    entity = new MyBitmapEntity();
                    entity.x = Float.valueOf(arr2[0]);
                    entity.y = Float.valueOf(arr2[1]);
                    entity.width = Float.valueOf(arr2[2]);
                    entity.height = Float.valueOf(arr2[3]);
                }
                mList.add(entity);
            }
        }
        return mList;
    }
}
