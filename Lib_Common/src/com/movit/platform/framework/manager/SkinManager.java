package com.movit.platform.framework.manager;

import android.content.Context;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ZipUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/8/11.
 */
public class SkinManager {

    public interface Callback {

        //skin加载失败
        public void onError();

        //skinZip下载失败
        public void onDownloadError();

        //skinZip解压失败
        public void onUpzipError();

        //加载皮肤
        public void toLoadSkin();
    }

    public static void loadSkin(final Context context, final Callback callback) {
        String skinUrl = "http://eoopapppub.movitechcn.com:8080/eoopapp/skin/android/" + CommConstants.HOST_TYPE + "/SkinVersion.json";
        HttpManager.getJson(skinUrl, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                callback.onError();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                JSONObject object = new JSONObject(response);
                String skinType = object.getString("skinType");
                String downloadUrl = object.getString("url");
                String fileName = object.getString("skinFileName");
                String topColor = object.getString("topColor");
                BaseApplication.TOP_COLOR = topColor;

                File skinDir = new File(context.getDir("theme", Context.MODE_PRIVATE), skinType);

                if ("default".equals(skinType) || skinDir.exists()) {
                    callback.toLoadSkin();
                } else {
                    downloadSkinZip(context, callback, downloadUrl, fileName, skinDir, skinType);
                }
            }
        });
    }

    private static void downloadSkinZip(final Context context, final Callback callback, final String downloadUrl, final String fileName, final File skinDir, final String skinType) {

        HttpManager.downloadFile(downloadUrl, new FileCallBack(CommConstants.SD_DOWNLOAD, fileName) {

            @Override
            public void onError(Call call, Exception e) {
                callback.onDownloadError();
            }

            @Override
            public void onResponse(File response) throws JSONException {
                try {
                    ZipUtils.upZipFilePro(response, skinDir.getAbsolutePath());
                    MFSPHelper.setString("topColor", BaseApplication.TOP_COLOR);
                    MFSPHelper.setString(BaseApplication.SKINTYPE, skinType);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onUpzipError();
                } finally {
                    callback.toLoadSkin();
                }
            }

            @Override
            public void inProgress(float progress, long total) {

            }
        });
    }
}
