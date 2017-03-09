package com.movit.platform.framework.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.BitmapCallback;

import org.json.JSONException;

import java.io.File;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/8/17.
 */
public class MFAQueryHelper {

    private static final int defaultTargetWidth = 128;

    private static AQuery aQuery;

    //在Application中初始化
    public static void initialize(Context context) {
        aQuery = new AQuery(context);
    }

    public static void cacheImage(String url) {
        aQuery.cache(url, 0);
    }

    public static void setImageViewFromCache(int viewId, String url) {
        aQuery.id(viewId).getCachedImage(url);
    }

    public static void setImageView(final View view, String url, final int fallbackId) {
        HttpManager.downloadBitmap(url, new BitmapCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ((ImageView)view).setImageResource(fallbackId);
            }

            @Override
            public void onResponse(Bitmap response) throws JSONException {
                ((ImageView)view).setImageBitmap(response);
            }
        });
    }

    public static void setImageView(int viewId, String url, int fallbackId) {
        aQuery.id(viewId).image(url, true, true, defaultTargetWidth, fallbackId);
    }

    public static void setImageView(int viewId, String url, int targetWidth, int fallbackId) {
        aQuery.id(viewId).image(url, true, true, targetWidth, fallbackId);
    }

    public static void setImageView(final View view, final String url, final int targetWidth, final int fallbackId) {
        HttpManager.downloadBitmap(url, new BitmapCallback() {
            @Override
            public void onError(Call call, Exception e) {
                ((ImageView)view).setImageResource(fallbackId);
            }

            @Override
            public void onResponse(Bitmap response) throws JSONException {
                aQuery.id(view).width(targetWidth).image(response);
            }
        });
    }

    public static void setBigImageView(final ImageView loading, final ImageView present, final View view, final String url, final int targetWidth, final int fallbackId) {

        HttpManager.downloadBitmap(url, new BitmapCallback() {
            @Override
            public void onError(Call call, Exception e) {
                present.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
//                ((ImageView)view).setImageResource(fallbackId);
                aQuery.id(view).width(targetWidth).image(fallbackId);

            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onResponse(Bitmap response) throws JSONException {
                loading.setVisibility(View.GONE);
                present.setVisibility(View.GONE);
//                ((ImageView)view).setImageBitmap(response);

                aQuery.id(view).width(targetWidth).image(response);
            }
        });
    }

    public static void setImageView(int viewId, File file, int targetWidth) {
        BitmapAjaxCallback callback = new BitmapAjaxCallback();
        callback.rotate(true);
        aQuery.id(viewId).image(file, true, targetWidth, callback);
    }

    public static void setImageViewWithFileCache(int viewId, String url, int fallbackId) {
        aQuery.id(viewId).image(url, false, true, defaultTargetWidth, fallbackId);
    }

    public static void setImageViewWithFileCache(int viewId, String url, int targetWidth, int fallbackId) {
        aQuery.id(viewId).image(url, false, true, targetWidth, fallbackId);
    }

    public static void setImageViewWithMemCache(int viewId, String url, int fallbackId) {
        aQuery.id(viewId).image(url, true, false, defaultTargetWidth, fallbackId);
    }

    public static void setImageViewWithMemCache(int viewId, String url, int targetWidth, int fallbackId) {
        aQuery.id(viewId).image(url, true, false, targetWidth, fallbackId);
    }

}
