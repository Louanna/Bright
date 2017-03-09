package com.movit.platform.cloud.net;


import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.Callback;
import com.movit.platform.framework.core.okhttp.callback.CloudListCallback;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;

import org.json.JSONException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.MediaType;

/**
 * author: Qian Ping
 * En_Name: Zoro Qian
 * E-mail: qianping1220@gmail.com
 * version: 4.0.2
 * Created Time: 2016年3月17日 下午4:56:01
 * Description: 网络请求实现.
 * */

public class AsyncNetHandler implements INetHandler{

    private String ContentType = "application/json";
    private Context context;

    private  AsyncNetHandler(Context context) {
        this.context = context;
    }

    /** Generate the Singleton */
    private static AsyncNetHandler mNetHandler = null;

    public static INetHandler getInstance(Context context) {
        if(null == mNetHandler){
            mNetHandler = new AsyncNetHandler(context);
        }
        return mNetHandler;
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return CommConstants.URL_EOP_DMS+relativeUrl;
    }

    @Override
    public void okmLogin(StringCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/auth/login";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .build()
                .execute(responseCallback);
    }

    @Override
    public void getRootFolder(StringCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/repository/getRootFolder";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .build()
                .execute(responseCallback);
    }

    @Override
    public void getDeptFolder(StringCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/folder/listAllByDept";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .build()
                .execute(responseCallback);
    }

    @Override
    public void getPersonalFolder(StringCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/repository/getPersonalFolder";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .build()
                .execute(responseCallback);
    }

    @Override
    public void getShareFolder(CloudListCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/share/list";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
//        String credential = Credentials.basic("okmadmin","admin");
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .build()
                .execute(responseCallback);
    }

    @Override
    public void postShareDocument(String uuid, String shareUser, StringCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/share/create";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        JSONObject object = new JSONObject();
        object.put("shareUuid",uuid);
        object.put("shareUser",shareUser);
        object.put("type","1");
        String content = JSON.toJSONString(object);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpUtils
                .postStringWithToken()
                .mediaType(JSON)
                .url(getAbsoluteUrl(url))
                .content(content)
                .addHeader("Authorization", credential)
                .addHeader("Content-type",ContentType)
                .build()
                .execute(responseCallback);

    }

    @Override
    public void getDocumentChildren(String fldId, Callback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/document/getChildren";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .url(getAbsoluteUrl(url))
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .addParams("fldId", fldId)
                .tag(fldId)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void getFolderChildren(String fldId, Callback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/folder/getChildren";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .url(getAbsoluteUrl(url))
                .addParams("fldId", fldId)
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .tag(fldId)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void downDocument(String docId, FileCallBack fileCallBack) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/document/getContent";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .url(getAbsoluteUrl(url))
                .addHeader("Authorization", credential)
                .addHeader("Content-type","application/octet-stream")
                .addParams("docId", docId)
                .build()
                .execute(fileCallBack);
    }

    @Override
    public long downDocument(String docId,String filePath,String fileName){
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        String url = "services/rest/document/getContent?docId="+docId;
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
//        String apkUrl = getAbsoluteUrl(url);

        String apkUrl = CommConstants.CLOUD_URL + url;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl.trim()));
        request.setDestinationInExternalPublicDir(filePath, fileName);
        request.addRequestHeader("Authorization", credential);
        // request.setTitle("MeiLiShuo");
        // request.setDescription("MeiLiShuo desc");
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        // request.setMimeType("application/cn.trinea.download.file");
        return downloadManager.enqueue(request);
    }

    @Override
    public long downKnowDocument(String fileName,String filePath,String downFilePath){
//        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
//        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
//                ,userInfo.getEmpAdname());
        String url = CommConstants.URL_DOWN+downFilePath;

        Log.d("test","url="+url);

        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(filePath, fileName);
//        request.addRequestHeader("Authorization", credential);
        // request.setTitle("MeiLiShuo");
        // request.setDescription("MeiLiShuo desc");
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        // request.setMimeType("application/cn.trinea.download.file");
        return downloadManager.enqueue(request);
    }

    @Override
    public void getShareByName(String name,CloudListCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/search/getShareByName";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .addParams("name", name)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void getFindAll(String name, String path, StringCallback responseCallback) {
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String url = "services/rest/search/findAll";
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        try {
            name = URLEncoder.encode(name,"UTF-8");
            path = URLEncoder.encode(path,"UTF-8");
        }catch (UnsupportedEncodingException  e){
            e.printStackTrace();
        }
        OkHttpUtils.getWithToken()
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .url(getAbsoluteUrl(url))
                .addParams("name", name)
                .addParams("path", path)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void createFolder(String path, StringCallback responseCallback) {
        String url = "services/rest/folder/create";
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        JSONObject object = new JSONObject();
        object.put("path",path);
        String content = JSON.toJSONString(object);

        MediaType JSON = MediaType.parse("application/json;charset=utf-8");

        OkHttpUtils
                .postStringWithToken()
                .mediaType(JSON)
                .url(getAbsoluteUrl(url))
                .content(content)
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void uploadFile(String path,String fileName,File file, FileCallBack responseCallback) {
        String url = "services/rest/document/createSimple";
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        String credential = Credentials.basic(MFSPHelper.getString(CommConstants.USERID)
                ,userInfo.getEmpAdname());
        String newPath = path + "/" +fileName;
        OkHttpUtils.postWithToken()
                .addFile("content",fileName,file)
                .addParams("docPath",newPath)
                .url(getAbsoluteUrl(url))
                .addHeader("Authorization", credential)
                .addHeader("Accept",ContentType)
                .build()
                .execute(responseCallback);
    }

    @Override
    public void saveDownloadFile(String fileId, String fileName, String userId) {
        String url = CommConstants.URL_EOP_API+"r/sys/rest/saveDownloadFile";
        OkHttpUtils.postWithToken()
                .addParams("fileId",fileId)
                .addParams("fileName",fileName)
                .addParams("userId",userId)
                .url(url)
                .addHeader("Accept",ContentType)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        Log.d("AsyncNetHandler","saveDownloadFile : "+response);
                    }
                });
    }
}
