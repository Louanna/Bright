package com.movit.platform.framework.manager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.view.CusDialog;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/8/11.
 */
public class UpgradeManager {

    public static void checkAPKVersion(final Context context, final Callback callback) {

        HttpManager.getJson(CommConstants.DOWNLOAD_URL, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                callback.onError();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int serverVersion = jsonObject.getInt("version");
                    String isForced = jsonObject.getString("forceUpdate");
                    String updateMes = jsonObject.getString("newChanges");
                    String downloadUrl = jsonObject.getString("url");

                    int appVersion = context.getPackageManager().getPackageInfo(
                            context.getPackageName(),
                            PackageManager.GET_META_DATA).versionCode;

                    int ignoreVersion = MFSPHelper.getInteger(CommConstants.IGNORE_CHECK_VERSION_CODE);

                    if (ignoreVersion > 0) {
                        if ((ignoreVersion < serverVersion)) {
                            showUpgradeDialog(context, downloadUrl, serverVersion, isForced, updateMes, callback);
                        }
                    } else {
                        if (appVersion < serverVersion) {
                            showUpgradeDialog(context, downloadUrl, serverVersion, isForced, updateMes, callback);
                        } else {
                            callback.onAfter();
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    callback.onError();
                }
            }
        });
    }

    public interface Callback {

        public void onError();

        //检查完毕
        public void onAfter();

        //忽略此次版本升级
        public void onCancel();

        //升级版本
        public void doUpgrade(final Context context, final String downloadUrl);
    }

    private static void showUpgradeDialog(final Context context, final String downloadUrl, final int serverVersion, final String isForced, final String updateMes, final Callback callback) {
        final CusDialog dialogUtil = CusDialog.getInstance();
        dialogUtil.showVersionDialog(context);
        dialogUtil.setUpdateDialog(updateMes, isForced);
        dialogUtil.setConfirmClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MFSPHelper.setInteger(CommConstants.IGNORE_CHECK_VERSION_CODE, 0);
                callback.doUpgrade(context, downloadUrl);
            }
        });
        dialogUtil.setCancleClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MFSPHelper.setInteger(CommConstants.IGNORE_CHECK_VERSION_CODE, serverVersion);
                dialogUtil.dismiss();
                callback.onCancel();
            }
        });
    }
}
