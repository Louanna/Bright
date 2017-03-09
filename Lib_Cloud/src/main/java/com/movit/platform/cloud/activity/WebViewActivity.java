package com.movit.platform.cloud.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.cloud.R;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.widget.SelectPicPopup;

import java.io.File;
import java.util.ArrayList;

public class WebViewActivity extends BaseActivity{
    private WebView mWebView;
    CusDialog dialogUtil;
    TextView topTitle, topClose;
    ImageView topLeft;
    ImageView topRight;

    private String curWebViewUrl;

    private ArrayList<UserInfo> atAllUserInfos = new ArrayList<UserInfo>();
    private ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<OrganizationTree>();

    ValueCallback<Uri> mUploadMessage;
    public final static int FILECHOOSER_RESULTCODE = 22;
    public final static int CAMERA_RESULTCODE = 23;
    public final static int PHOTO_RESULTCODE = 24;
    String mCameraFilePath;
    String moduleId;
    private RelativeLayout toplayout;

    // 自定义的弹出框类
    SelectPicPopup popWindow;
    // 用来标识请求照相功能的activity

    String currentTime;
    Uri imageUri;// The Uri to store the big
    String takePicturePath = "";

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        initViews();

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        String title = intent.getStringExtra("title");
        topTitle.setText(title);

        mWebView.stopLoading();
        mWebView.clearHistory();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        mWebView.getSettings().setJavaScriptEnabled(true);
        /*** 打开本地缓存提供JS调用 **/
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir()
                .getAbsolutePath();
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.addJavascriptInterface(new WebViewFuncion(), "eoopWebView");

        // User settings
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);//关键点
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setBuiltInZoomControls(true); // 设置显示缩放按钮
        mWebView.getSettings().setSupportZoom(true); // 支持缩放
        mWebView.getSettings().setLoadWithOverviewMode(true);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        Log.d("maomao", "densityDpi = " + mDensity);
        if (mDensity == 240) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == 160) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if(mDensity == 120) {
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        }else if(mDensity == DisplayMetrics.DENSITY_XHIGH){
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else if (mDensity == DisplayMetrics.DENSITY_TV){
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else{
            mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }

        mWebView.clearCache(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                try {
                    view.stopLoading();
                    view.clearView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();//接受证书
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                try {
                    progressDialogUtil.dismiss();
                    progressDialogUtil.showLoadingDialog(WebViewActivity.this,
                            "请稍候...", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                try {
                    progressDialogUtil.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              String url) {
                return super.shouldInterceptRequest(view, url);
            }

        });

        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                try {
                    //topTitle.setText(title);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android > 4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType) {
                if (mUploadMessage != null)
                    return;
                mUploadMessage = uploadMsg;
                startActivityForResult(createDefaultOpenableIntent(acceptType),
                        FILECHOOSER_RESULTCODE);
            }

        };
        // 设置setWebChromeClient对象
        mWebView.setWebChromeClient(wvcc);

        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        Log.v("url", url);

        mWebView.loadUrl(url);
        curWebViewUrl = mWebView.getUrl();
//        mWebView.loadUrl("file:///android_asset/testPage3.html");
    }

    private void initViews(){
        mWebView = (WebView) findViewById(R.id.webview);
        toplayout = (RelativeLayout) findViewById(R.id.common_top_layout);
        topTitle = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (ImageView) findViewById(R.id.common_top_img_right);
        topClose = (TextView) findViewById(R.id.common_top_close);
        topClose.setVisibility(View.GONE);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        topClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }



    class WebViewFuncion {

        public WebViewFuncion() {
            super();
        }

        @JavascriptInterface
        public void closeBroswer() {
            onBackPressed();
        }

        @JavascriptInterface
        public void finishSaveTask(String message) {
            Log.v("message", "==" + message);
            onBackPressed();
        }

        @JavascriptInterface
        public void showTaskCalendarConfirm(String title) {
            Log.v("alert", title);
            dialogUtil = CusDialog.getInstance();
            dialogUtil.showCustomDialog(context);
            dialogUtil.setWebDialog(title);
            dialogUtil.setCancleClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogUtil.dismiss();
                }
            });
            dialogUtil.setConfirmClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    //TODO modify by anna
//					mWebView.loadUrl("javascript:eoopWeb.confirmSure()");

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:eoopWeb.confirmSure()");
                        }
                    });

                    dialogUtil.dismiss();
                }
            });
        }

        @JavascriptInterface
        public void showTaskCalendarAlert(String title) {
            Log.v("alert", title);
            dialogUtil = CusDialog.getInstance();
            dialogUtil.showCustomDialog(context);
            dialogUtil.setSimpleDialog(title);
            dialogUtil.setConfirmClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:eoopWeb.alertOK()");
                        }
                    });

                    dialogUtil.dismiss();
                }
            });
        }

        @JavascriptInterface
        public void showTaskCalendarToast(String message) {
        }


        @JavascriptInterface
        public void meetingScan() {
        }

        @JavascriptInterface
        public void openURL(String url) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        }


    }




    private Intent createDefaultOpenableIntent(String acceptType) {
        // Create and return a chooser with the default OPENABLE
        // actions including the camera, camcorder and sound
        // recorder where available.
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        if (StringUtils.empty(acceptType)) {
            i.setType("*/*");
        } else {
            i.setType(acceptType);
        }

        Intent chooser = createChooserIntent(createCameraIntent());
        chooser.putExtra(Intent.EXTRA_INTENT, i);
        return chooser;
    }

    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
        return chooser;
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath()
                + File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator
                + System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

}
