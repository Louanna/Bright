package com.movit.platform.framework.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.movit.platform.framework.utils.ScreenUtils;

/**
 * Created by Administrator on 2016/8/8.
 */
public class MFWebView extends WebView {
    public MFWebView(Context context) {
        super(context);
        initWebview(context);
    }

    public MFWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWebview(context);
    }

    public MFWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWebview(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MFWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWebview(context);
    }

    public MFWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        initWebview(context);
    }

    private void initWebview(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            getSettings().setAllowUniversalAccessFromFileURLs(true);

        getSettings().setJavaScriptEnabled(true);
        /*** 打开本地缓存提供JS调用 **/
        getSettings().setDomStorageEnabled(true);
        getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = context.getApplicationContext().getCacheDir().getAbsolutePath();
        getSettings().setAppCachePath(appCachePath);
        getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
        getSettings().setAppCacheEnabled(true);
        getSettings().setDatabaseEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        // User settings
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setUseWideViewPort(true);//关键点
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        getSettings().setDisplayZoomControls(false);
        getSettings().setBuiltInZoomControls(true); // 设置显示缩放按钮
        getSettings().setSupportZoom(true); // 支持缩放
        getSettings().setLoadWithOverviewMode(true);

        //webview在安卓5.0之前默认允许其加载混合网络协议内容
        //在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        int mDensity = ScreenUtils.getDensityDpi(context);
        if (mDensity == 240) {
            getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == 160) {
            getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if(mDensity == 120) {
            getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        }else if(mDensity == DisplayMetrics.DENSITY_XHIGH){
            getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else if (mDensity == DisplayMetrics.DENSITY_TV){
            getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else{
            getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }
    }

}
