package com.movitech.EOP.module.workbench.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.manager.HttpManager;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.widget.MFWebView;
import com.movit.platform.framework.widget.SelectPicPopup;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.qrcode.ScanActivity;

import org.json.JSONException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cn.sharesdk.ShareDialog;
import okhttp3.Call;
import okhttp3.Request;

public class WebViewActivity extends EOPBaseActivity {

    private MFWebView mWebView;
    private CusDialog dialogUtil;
    private TextView topTitle, topClose;
    private ImageView topLeft, topRight;

    private ArrayList<UserInfo> atAllUserInfos = new ArrayList<UserInfo>();
    private ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<OrganizationTree>();

    private ValueCallback<Uri> mUploadMessage;
    public final static int FILECHOOSER_RESULTCODE = 22;
    public final static int CAMERA_RESULTCODE = 23;
    public final static int PHOTO_RESULTCODE = 24;

    private String mCameraFilePath, moduleId, currentTime, takePicturePath = "";
    private RelativeLayout toplayout;

    // 自定义的弹出框类
    private SelectPicPopup popWindow;
    private Uri imageUri;// The Uri to store the big

    private boolean isMovithomepage;

    private String cookies = "";

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        initViews();

        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        moduleId = intent.getStringExtra("moduleId");
        boolean sao_sao = intent.getBooleanExtra("sao-sao", false);
        boolean goChat = intent.getBooleanExtra("goChat", false);
        final boolean bpm = intent.getBooleanExtra("BPM", false);
        boolean needTitle = intent.getBooleanExtra("needTitle", false);
        boolean isOA = intent.getBooleanExtra("isOA", false);
        if (needTitle) {
            toplayout.setVisibility(View.GONE);
        }
        try {
            boolean gohr = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_GOHR", false);
            if (gohr) {
                if (MFSPHelper.getString(CommConstants.EMPADNAME).equalsIgnoreCase(
                        "karolina.tian")) {
                    topRight.setVisibility(View.GONE);
                }
            } else {
                topRight.setVisibility(View.GONE);
            }
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (sao_sao) {
            topTitle.setText(getString(R.string.scan_result));
        }

        if (StringUtils.notEmpty(getIntent().getStringExtra("title"))) {
            topTitle.setText(getIntent().getStringExtra("title"));
        }

        if (bpm) {
            topTitle.setText(getString(R.string.movit_bpm));
        }

        isMovithomepage = getIntent().getBooleanExtra("movithomepage", false);
        if (isMovithomepage) {
            topClose.setVisibility(View.GONE);
            topRight.setImageResource(R.drawable.icon_share_more);
        }

        if (goChat || isMovithomepage) {
            topRight.setVisibility(View.VISIBLE);
        } else {
            topRight.setVisibility(View.GONE);
        }

        if ("Eclaim".equalsIgnoreCase(moduleId)) {
            topLeft.setVisibility(View.GONE);
        } else {
            topLeft.setVisibility(View.VISIBLE);
        }

        mWebView.stopLoading();
        mWebView.clearHistory();

        mWebView.addJavascriptInterface(new WebViewFuncion(), "eoopWebView");

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
                            getString(R.string.waiting), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                CookieManager cookieManager = CookieManager.getInstance();
                cookies = cookieManager.getCookie(url);
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
                    if (bpm) {
                        topTitle.setText("盟拓BPM");
                    } else {
                        topTitle.setText(title);
                    }
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
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);

                Intent intent = new Intent(WebViewActivity.this,DownloadActivity.class);
                intent.putExtra("url",url);
                intent.putExtra("cookies",cookies);
                startActivity(intent);
            }
        });

        String cookie = intent.getStringExtra("cookies");
        sysCookies(cookie, url);

        if (isOA) {
            url = url + "&access_token=" + CommConstants.ACCESSTOKEN;
        }
        Log.v("url", url);

        String headers = intent.getStringExtra("headers");
        if (!TextUtils.isEmpty(headers)) {
            HashMap<String, String> map = new Gson().fromJson(headers, new TypeToken<HashMap<String,
                    String>>() {
            }.getType());
            mWebView.loadUrl(url, map);
        } else {
            mWebView.loadUrl(url);
        }
    }

    private void sysCookies(String cookie, String url) {
        if (!TextUtils.isEmpty(cookie)) {
            CookieSyncManager.createInstance(WebViewActivity.this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            try {
                URL aURL = new URL(url);
                String domain = aURL.getHost();
                String path = aURL.getPath();

                HashMap<String, String> map = new Gson().fromJson(cookie, new TypeToken<HashMap<String,
                        String>>() {
                }.getType());
                for (String key : map.keySet()) {
                    String cookieValue = "";
                    String value = map.get(key);
                    cookieValue += key + "=" + value + ";";
                    cookieValue += "domain=" + domain + ";" + "path=" + path;
                    cookieManager.setCookie(url, cookieValue);
                }
                CookieSyncManager.getInstance().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.removeSessionCookie();//移除
    }

    private void initViews() {
        mWebView = (MFWebView) findViewById(R.id.webview);
        toplayout = (RelativeLayout) findViewById(R.id.common_top_layout);
        topTitle = (TextView) findViewById(R.id.common_top_title);
        topTitle.setText("Loading");
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (ImageView) findViewById(R.id.common_top_img_right);
        topClose = (TextView) findViewById(R.id.common_top_close);
        topRight.setImageResource(R.drawable.ico_chat);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if ("TimeSheet".equals(topTitle.getText())
                            || "我的考勤".equals(topTitle.getText())
                            || "任务日程".equals(topTitle.getText())) {
                        onBackPressed();
                    } else if (mWebView.getUrl().contains("eoop/newsDetail")) {
                        onBackPressed();
                    } else {
                        if (mWebView.canGoBack()) {
                            mWebView.goBack();
                        } else {
                            onBackPressed();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onBackPressed();
                }
            }
        });

        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isMovithomepage) {
                    final ShareDialog dialog = new ShareDialog(WebViewActivity.this);
                    dialog.show2Item();
                    dialog.setCancelButtonOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            switch (position) {
                                case 0:
                                    dialog.shareToWeChat();
                                    break;
                                case 1:
                                    dialog.shareToMoments();
                                    break;
                                default:
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        topClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (StringUtils.notEmpty(moduleId)) {
                    OAManager.addUserActionLog(moduleId, 2);
                }
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int arg0, int resultCode, Intent data) {
        switch (arg0) {
            case 1:
                String ids = "";
                String members = "";
                String adNames = "";
                Map<String, String> map = new HashMap<String, String>();
                if (data != null) {
                    atOrgunitionLists = (ArrayList<OrganizationTree>) data
                            .getSerializableExtra("atOrgunitionLists");
                    atAllUserInfos = (ArrayList<UserInfo>) data
                            .getSerializableExtra("atUserInfos");
                    if (!atAllUserInfos.isEmpty()) {
                        for (int i = 0; i < atAllUserInfos.size(); i++) {
                            ids += atAllUserInfos.get(i).getId() + ",";
                            members += atAllUserInfos.get(i).getEmpCname() + ",";
                            adNames += atAllUserInfos.get(i).getEmpAdname() + ",";
                        }
                        map.put("ids", ids.substring(0, ids.length() - 1));
                        map.put("members",
                                members.substring(0, members.length() - 1));
                        map.put("adnames",
                                adNames.substring(0, adNames.length() - 1));
                        final String json = Obj2JsonUtils.map2json(map);
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                mWebView.loadUrl("javascript:eoopWeb.confirmMember(" + json
                                        + ")");
                            }
                        });
                    }
                }
                break;
            case 2:
                mWebView.reload();
                break;
            case FILECHOOSER_RESULTCODE:
                if (null == mUploadMessage)
                    return;
                Uri result = data == null || resultCode != RESULT_OK ? null : data
                        .getData();
                if (result == null && data == null
                        && resultCode == Activity.RESULT_OK) {
                    File cameraFile = new File(mCameraFilePath);
                    if (cameraFile.exists()) {
                        result = Uri.fromFile(cameraFile);
                        // Broadcast to the media scanner that we have a new photo
                        // so it will be added into the gallery for the user.
                        sendBroadcast(new Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));

                    }
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
                result = null;
                break;
            // 如果是直接从相册获取
            case PHOTO_RESULTCODE:
                // 从相册中直接获取文件的真是路径，然后上传
                final String picPath = PicUtils.getPicturePath(data,
                        WebViewActivity.this);
                try {
                    takePicturePath = PicUtils
                            .getSmallImageFromFileAndRotaing(picPath);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                toUploadFile(takePicturePath);
                break;
            // 如果是调用相机拍照时
            case CAMERA_RESULTCODE:
                if (imageUri != null) {
                    boolean copy = FileUtils.copyFile(CommConstants.SD_CARD
                            + "/temp.jpg", CommConstants.SD_CARD_IMPICTURES
                            + currentTime + ".jpg");
                    new File(CommConstants.SD_CARD + "/temp.jpg").delete();
                    if (copy) {
                        String pathString = CommConstants.SD_CARD_IMPICTURES
                                + currentTime + ".jpg";
                        PicUtils.scanImages(context, pathString);
                        try {
                            takePicturePath = PicUtils
                                    .getSmallImageFromFileAndRotaing(pathString);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        toUploadFile(takePicturePath);
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(arg0, resultCode, data);
    }

    private void toUploadFile(final String filePath) {

        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();

        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        Map<String, String> map = new HashMap<>();
        map.put("strJson", encodedImage);

        HttpManager.uploadFile(CommConstants.UPLOAD_BPM_PIC, map, new StringCallback() {

            @Override
            public void onBefore(Request request) {
                progressDialogUtil.showLoadingDialog(WebViewActivity.this,
                        getString(R.string.waiting), false);
            }

            @Override
            public void onAfter() {
                progressDialogUtil.dismiss();
            }

            @Override
            public void inProgress(float progress) {

            }

            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {
                try {
                    InputStream inputStream = new ByteArrayInputStream(
                            response.getBytes());
                    // 创建解析
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser saxParser = spf.newSAXParser();
                    XMLSimpleContentHandler handler = new XMLSimpleContentHandler();
                    saxParser.parse(inputStream, handler);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class XMLSimpleContentHandler extends DefaultHandler {
        // localName表示元素的本地名称（不带前缀）；qName表示元素的限定名（带前缀）；atts 表示元素的属性集�
        StringBuffer body;

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("string")) {
                body = new StringBuffer();
            }

        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            super.characters(ch, start, length);
            body.append(ch, start, length); // 将读取的字符数组追加到builder中
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals("string")) {
                Log.v("XMLSimpleContentHandler", "body=" + body.toString());
                Log.v("XMLSimpleContentHandler", "takePicturePath=" + takePicturePath);
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:Start.setImageUrl('" + takePicturePath + "','" + body.toString()
                                + "')");
                    }
                });
            }
        }
    }

    class WebViewFuncion {

        public WebViewFuncion() {
            super();
        }

        //积分商城---商品详情页面
        @JavascriptInterface
        public void openDetail(final String url) {
            /**
             * modify  by zoro.qian
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String newUrl = "";
                    if (url.startsWith("http")) {
                        newUrl = url;
                    } else {
                        try {
                            URL url2 = new URL(mWebView.getUrl());
                            if (url2.getPort() > 0) {
                                newUrl = url2.getProtocol() + "://" + url2.getHost() + ":" + url2.getPort()
                                        + url;
                            } else {
                                newUrl = url2.getProtocol() + "://" + url2.getHost() + url;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("URL", newUrl);
                    startActivity(intent);
                }
            });
        }

        //积分商城---商城页面等
        @JavascriptInterface
        public void openTaskCalendarURL(final String url) {
            /**
             * modify  by zoro.qian
             */
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String newUrl = "";
                    if (url.startsWith("http:")) {
                        newUrl = url;
                    } else {
                        try {
                            URL url2 = new URL(mWebView.getUrl());
                            if (url2.getPort() > 0) {
                                newUrl = "http://" + url2.getHost() + ":" + url2.getPort()
                                        + url;
                            } else {
                                newUrl = "http://" + url2.getHost() + url;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println(newUrl);
                    Intent intent = new Intent(context, WebViewActivity.class);
                    intent.putExtra("URL", newUrl);
                    startActivityForResult(intent, 2);
                }
            });
        }

        @JavascriptInterface
        public void closeBroswer() {
            onBackPressed();
        }

        @JavascriptInterface
        public void chooseMember(String ids) {
            if (StringUtils.notEmpty(ids)) {
                atAllUserInfos.clear();
                atOrgunitionLists.clear();

                String[] arrId = ids.split(",");
                UserDao dao = UserDao.getInstance(context);
                for (int i = 0; i < arrId.length; i++) {
                    UserInfo userInfo = dao.getUserInfoById(arrId[i]);
                    atAllUserInfos.add(userInfo);
                }

                UserInfo userInfo = tools.getLoginConfig().getmUserInfo();
                if (atAllUserInfos.contains(userInfo)) {
                    atAllUserInfos.remove(userInfo);
                }
            }
            Intent intent = new Intent();
            intent.putExtra("TITLE", "选择联系人");
            intent.putExtra("ACTION", "WebView");
            intent.putExtra("atUserInfos", atAllUserInfos);

            ((BaseApplication) WebViewActivity.this.getApplication()).getUIController().onIMOrgClickListener(WebViewActivity.this, intent, 1);
        }

        @JavascriptInterface
        public void finishSaveTask(String message) {
            if (StringUtils.notEmpty(message)) {
                EOPApplication.showToast(context, message);
            }
            onBackPressed();
        }

        @JavascriptInterface
        public void turnToGroup(String roomId, String displayName) {
            // 获取群组信息 ， 跳转聊天
            progressDialogUtil.showLoadingDialog(context, getString(R.string.get_group_message), false);
            handler.obtainMessage(3, new String[]{roomId, displayName})
                    .sendToTarget();
        }

        @JavascriptInterface
        public void showAlert(String msg) {
            handler.obtainMessage(4, msg).sendToTarget();
        }

        @JavascriptInterface
        public void showTaskCalendarConfirm(String title) {
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
            EOPApplication.showToast(context, message);
        }

        @JavascriptInterface
        public void startLoading() {
            handler.sendEmptyMessage(5);
        }

        @JavascriptInterface
        public void stopLoading() {
            handler.sendEmptyMessage(6);
        }

        @JavascriptInterface
        public void meetingScan() {
            startActivity(new Intent(context, ScanActivity.class)
                    .putExtra("type", "sign"));
        }

        @JavascriptInterface
        public void openURL(String url) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        }

        @JavascriptInterface
        public void showNativeActionSheet() {
            // 实例化SelectPicPopupWindow
            popWindow = new SelectPicPopup(WebViewActivity.this,
                    itemsOnClick);
            // 显示窗口
            popWindow.showAtLocation(toplayout,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
        }


        @JavascriptInterface
        public void openPOCChat(String userId, String msgTxt) {
            UserDao dao = UserDao.getInstance(WebViewActivity.this);
            UserInfo userInfo = dao.getUserInfoById(userId);

            if (null != userInfo) {

                if (userInfo.getEmpId().equals(CommConstants.loginConfig
                        .getmUserInfo().getEmpId())) {
                    ToastUtils.showToast(getApplicationContext(), getString(R.string.contacts_is_self));
                    return;
                }
            } else {
                ToastUtils.showToast(getApplicationContext(), getString(R.string.user_is_not_exist));
            }
        }
    }

    private OnClickListener itemsOnClick = new OnClickListener() {

        public void onClick(View v) {
            popWindow.dismiss();

            switch (v.getId()) {
                case R.id.btn_take_photo:
                    // 跳转相机拍照
                    currentTime = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
                    imageUri = Uri.parse(CommConstants.IMAGE_FILE_LOCATION);
                    if (imageUri == null) {
                        return;
                    }
                    String sdStatus = Environment.getExternalStorageState();
                    if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(WebViewActivity.this, getString(R.string.can_not_find_sd), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent2, CAMERA_RESULTCODE);
                    break;
                case R.id.btn_pick_photo:
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, PHOTO_RESULTCODE);

                    break;
                default:
                    break;
            }

        }

    };

    public void getAllUsersInOrg(UserDao dao, OrganizationTree org,
                                 List<UserInfo> allUsers) {
        String orgId = org.getId();
        List<UserInfo> userInfos = dao.getAllUserInfosByOrgId(orgId);
        allUsers.addAll(userInfos);
        List<OrganizationTree> orgs = dao.getAllOrganizationsByParentId(orgId);
        for (int i = 0; i < orgs.size(); i++) {
            getAllUsersInOrg(dao, orgs.get(i), allUsers);
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    progressDialogUtil.dismiss();

                    break;
                case 2:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, getString(R.string.get_group_message_error));
                    break;
                case 3:

                    break;
                case 4:
                    final String messageString = (String) msg.obj;
                    dialogUtil = CusDialog.getInstance();
                    dialogUtil.showCustomDialog(context);
                    dialogUtil.setSimpleDialog(messageString);
                    dialogUtil.setConfirmClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (messageString.contains(getString(R.string.register_success))) {
                                dialogUtil.dismiss();
                                onBackPressed();
                            } else {
                                dialogUtil.dismiss();
                            }
                        }
                    });
                    break;
                case 5:
                    progressDialogUtil.showLoadingDialog(context, getString(R.string.waiting), false);
                    break;
                case 6:
                    progressDialogUtil.dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    private Intent createDefaultOpenableIntent(String acceptType) {
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

    private Intent createCamcorderIntent() {
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }

    private Intent createSoundRecorderIntent() {
        return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }

}
