package com.movit.platform.common.task;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.manager.CommManager;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.Json2ObjUtils;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movit.platform.framework.utils.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by Administrator on 2016/7/22.
 */
public class LoginTask {

    private Context context;
    private CommonHelper tools;
    private LoginInfo loginConfig;
    private DialogUtils progressDialogUtil, progressDownLoading;

    private String failReason, userName, password;

    public final static int SUCCESSS = 1;
    public final static int FAIL = 2;

    public LoginTask(Context context, String userName, String password) {
        CommonHelper commonHelper = new CommonHelper(context);
        LoginInfo loginConfig = commonHelper.getLoginConfig();
        loginConfig.setPassword(password);
        this.loginConfig = loginConfig;

        this.userName = userName;
        this.password = password;
        this.context = context;
        this.tools = new CommonHelper(context);
        this.progressDialogUtil = DialogUtils.getInstants();
        this.progressDownLoading = DialogUtils.getInstants();
    }

    public void loginEOPServer(String url, String name, String password, TelephonyManager mTelephonyManager) {

        Map<String, String> params = new HashMap<>();
        params.put("username", name);
        params.put("password", password);
        params.put("deviceType", "2");
        params.put("device", mTelephonyManager.getDeviceId());

        HttpManager.postJson(url, Obj2JsonUtils.map2json(params), new MyStringCallback(context));
    }

    private class MyStringCallback extends StringCallback {

        private Context context;

        MyStringCallback(Context context) {
            this.context = context;
        }

        @Override
        public void onBefore(Request request) {
            //默认未登录
            CommConstants.IS_LOGIN_EOP_SERVER = false;
            progressDialogUtil.showLoadingDialog(context, context.getString(R.string.is_login), false);
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
            // 处理返回的信息
            doNext(doResult(new JSONObject(response)));
        }
    }

    private Integer doResult(JSONObject result) {
        if (result != null) {
            try {
                boolean okValue = result.getBoolean("ok");
                if (!okValue) {
                    String value = result.getString("value");
                    failReason = value;
                    return CommConstants.LOGIN_FAILE_REASON;
                } else {
                    if (result.has("value")) {
                        String ticket = result.getString("value");
                        MFSPHelper.setString("ticket", ticket);
                    }

                    UserInfo userInfo = Json2ObjUtils.getUserInfoFromJson(result.getString("objValue"));

                    loginConfig.setmUserInfo(userInfo);
                    CommConstants.loginConfig = loginConfig;
                    tools.saveLoginConfig(loginConfig);// 保存用户配置信息
                    BaseApplication.Token = userInfo.getOpenFireToken();
                    return CommConstants.LOGIN_SECCESS;
                }
            } catch (Exception e) {
                CommConstants.IS_LOGIN_EOP_SERVER = false;
                e.printStackTrace();
                return CommConstants.SERVER_UNAVAILABLE;
            }
        } else {
            return CommConstants.SERVER_UNAVAILABLE;
        }
    }

    private void doNext(Integer result) {
        switch (result) {
            case CommConstants.LOGIN_FAILE_REASON:
                progressDialogUtil.dismiss();
                MFSPHelper.setBoolean(CommConstants.IS_AUTOLOGIN, false);
                MFSPHelper.setBoolean(CommConstants.IS_REMEMBER, false);
                Toast.makeText(context, failReason, Toast.LENGTH_SHORT).show();
                break;
            case CommConstants.LOGIN_SECCESS:
                // 账号登录成功
                // 直接去获取组织架构，群组信息登陆后获取
                handler.sendEmptyMessage(1);
                break;
            case CommConstants.LOGIN_ERROR_ACCOUNT_PASS:// 账户或者密码错误
                progressDialogUtil.dismiss();
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.message_invalid_username_password),
                        Toast.LENGTH_SHORT).show();
                break;
            case CommConstants.SERVER_UNAVAILABLE:// 服务器连接失败
                progressDialogUtil.dismiss();
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.message_server_unavailable),
                        Toast.LENGTH_SHORT).show();
                break;
            case CommConstants.LOGIN_ERROR:// 未知异常
                progressDialogUtil.dismiss();
                Toast.makeText(
                        context,
                        context.getResources().getString(
                                R.string.unrecoverable_error), Toast.LENGTH_SHORT)
                        .show();
                break;
        }
        //通知autoLogin
        Intent intent = new Intent(CommConstants.ACTION_LOGIN_DONE);
        intent.putExtra("result", result);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        boolean orgFull = context.getPackageManager().getApplicationInfo(
                                context.getPackageName(),
                                PackageManager.GET_META_DATA).metaData.getBoolean(
                                "CHANNEL_ORG_FULL", false);
                        if (orgFull) {
                            downloadEopDbForFull();
                        } else {
                            downloadEopDb();
                        }
                    } catch (Exception e) {
                        CommConstants.IS_LOGIN_EOP_SERVER = false;
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    doNext(CommConstants.SERVER_UNAVAILABLE);
                    break;
                case 3:
                    progressDownLoading.dismiss();
                    progressDialogUtil.dismiss();
                    doNext(CommConstants.LOGIN_FAILE_REASON);
                    break;
                case 4:
                    updateEoopDB();
                    break;
                case 5:
                    //获取groupList,保证chatservice中join in group时已经获取到group
                    //((BaseApplication) ((Activity) context).getApplication()).getManagerFactory().getGroupManager().getGroupList();
                    //获取关注与被关注的人的信息
                    CommManager.getAttentionData(context,new MyAttentionCallback());
                    break;
                case 6:
                    downloadEopDb();
                    break;
                default:
                    break;
            }
        }
    };

    public class MyAttentionCallback implements CommManager.AttentionCallback{
        @Override
        public void onAfter(){

        }
    }

    private void downloadEopDb() {
        long lastSyncTime = MFSPHelper.getLong("lastSyncTime");
        //判断是否需要下载db
        if (lastSyncTime == -1) {
            getEopDbDownloadURL(new DownloadCallback());
        } else {
            handler.sendEmptyMessage(4);
            handler.sendEmptyMessage(5);
        }
    }

    private void getEopDbDownloadURL(final Callback callback) {
        progressDownLoading.dismiss();
        progressDialogUtil.dismiss();
        progressDownLoading.showDownLoadingDialog(context, context.getString(R.string.downloading_file),
                false);
        progressDownLoading.getLoadingDialog().setCancelable(true);

        OkHttpUtils.getWithToken()
                .url(CommConstants.URL_FULL_ORG_LIST)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        progressDownLoading.dismiss();
                        CommConstants.IS_LOGIN_EOP_SERVER = false;
                        failReason = context.getString(R.string.downloading_file_fail);
                        handler.sendEmptyMessage(3);
                    }

                    @Override
                    public void onResponse(final String response) throws JSONException {
                        JSONObject jsonResult = new JSONObject(response);
                        long orgTime = jsonResult.getLong("lastSyncTime");
                        String downloadUrl = jsonResult.getString("fullOrgFilePath");
                        toDownloadEopDB(downloadUrl, orgTime, callback);
                    }
                });
    }

    private class DownloadCallback implements Callback {

        @Override
        public void onAfterUpzip() {
            handler.sendEmptyMessage(4);
            handler.sendEmptyMessage(5);
        }

        @Override
        public void onDownloadError() {
            CommConstants.IS_LOGIN_EOP_SERVER = false;
            failReason = context.getString(R.string.unzip_file_fail);
            handler.sendEmptyMessage(3);
        }
    }

    private class DownloadCallbackForFull implements Callback {
        @Override
        public void onAfterUpzip() {
            handler.sendEmptyMessage(5);
            sendLoginBroadcast();
        }

        @Override
        public void onDownloadError() {
            CommConstants.IS_LOGIN_EOP_SERVER = false;
            failReason = context.getString(R.string.unzip_file_fail);;
            handler.sendEmptyMessage(3);
        }
    }

    private void sendLoginBroadcast() {
        UserDao dao = UserDao.getInstance(context);
        CommConstants.allUserInfos = dao.getAllUserInfos();
        CommConstants.allOrgunits = dao.getAllOrgunitions();

        UserInfo userInfo = dao.getUserInfoById(loginConfig.getmUserInfo().getId());
        if (userInfo == null) {
            failReason = context.getString(R.string.no_such_person);
            handler.sendEmptyMessage(3);
            return;
        }
        Intent intent = new Intent(CommConstants.ACTION_ORGUNITION_DONE);
        intent.putExtra("getOrgunitList", SUCCESSS);
        intent.putExtra("userName", userName);
        intent.putExtra("password", password);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    private interface Callback {
        //解压之后
        public void onAfterUpzip();

        //下载失败
        public void onDownloadError();
    }

    private void toDownloadEopDB(String downloadUrl, final long orgTime, final Callback callback) {
        HttpManager.downloadFile(downloadUrl, new FileCallBack(CommConstants.SD_DOWNLOAD, "eoop.db.zip") {

            @Override
            public void onError(Call call, Exception e) {
                progressDownLoading.dismiss();
                CommConstants.IS_LOGIN_EOP_SERVER = false;
                failReason = context.getString(R.string.downloading_file_fail);
                handler.sendEmptyMessage(3);
            }

            @Override
            public void onResponse(File response) throws JSONException {
                // 解压缩
                try {
                    ZipUtils.upZipFile(response, CommConstants.SD_DOWNLOAD);
                    MFSPHelper.setLong("lastSyncTime", orgTime);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onDownloadError();
                }
            }

            @Override
            public void inProgress(float progress, long total) {
                progressDownLoading.setDownLoadProcess(progress*100);
            }

            @Override
            public void onAfter() {
                progressDownLoading.dismiss();
                callback.onAfterUpzip();
            }
        });
    }

    // 增量更新
    private void updateEoopDB() {
        progressDialogUtil.dismiss();
        progressDownLoading.dismiss();
        progressDialogUtil.showLoadingDialog(context, context.getString(R.string.waiting), false);
        progressDialogUtil.getLoadingDialog().setCancelable(true);

        final UserDao dao = UserDao.getInstance(context);
        CommConstants.allUserInfos = dao.getAllUserInfos();
        CommConstants.allOrgunits = dao.getAllOrgunitions();

        long lastSyncTime = MFSPHelper.getLong("lastSyncTime");

        OkHttpUtils.getWithToken()
                .url(CommConstants.URL_DELTA_ORG_LIST + DateUtils.date2Str(new Date(lastSyncTime),
                        "yyyy-MM-dd_HH:mm:ss"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        CommConstants.IS_LOGIN_EOP_SERVER = false;
                        handler.sendEmptyMessage(2);
                    }

                    @Override
                    public void onResponse(final String response) throws JSONException {
                        try {
                            JSONObject jsonResult = new JSONObject(response);
                            if (jsonResult.has("code")) {
                                String code = jsonResult.getString("code");
                                if ("org.outOfDeltaRange".equals(code)) {
                                    MFSPHelper.setLong("lastSyncTime", -1);
                                    String databaseFilename = dao.getUserDBFile();
                                    (new File(databaseFilename)).delete();
                                    CommConstants.allUserInfos = null;
                                    CommConstants.allOrgunits = null;
                                    handler.sendEmptyMessage(6);
                                    return;
                                }
                            }
                            long time = jsonResult.getLong("lastSyncTime");
                            List<OrganizationTree> orgList = new ArrayList<>();
                            List<UserInfo> userList = new ArrayList<>();

                            if (jsonResult.has("orgList")) {
                                JSONArray array = jsonResult.getJSONArray("orgList");
                                for (int i = 0; i < array.length(); i++) {
                                    orgList.add(Json2ObjUtils.getOrgunFromJson(array
                                            .getJSONObject(i).toString()));
                                }
                            }
                            if (jsonResult.has("userList")) {
                                JSONArray userarray = jsonResult
                                        .getJSONArray("userList");
                                for (int j = 0; j < userarray.length(); j++) {
                                    userList.add(Json2ObjUtils
                                            .getUserInfoFromJson(userarray
                                                    .getJSONObject(j).toString()));
                                }
                            }

                            for (int i = 0; i < orgList.size(); i++) {
                                dao.updateOrgByFlags(orgList.get(i));
                            }
                            for (int j = 0; j < userList.size(); j++) {
                                dao.updateUserByFlags(userList.get(j));
                            }
                            MFSPHelper.setLong("lastSyncTime", time);
                            sendLoginBroadcast();
                        } catch (Exception e) {
                            e.printStackTrace();
                            CommConstants.IS_LOGIN_EOP_SERVER = false;
                            handler.sendEmptyMessage(2);
                        }
                    }
                });
    }

    /**
     * 全量更新
     */
    private void downloadEopDbForFull() {
        long lastSyncTime = MFSPHelper.getLong("lastSyncTime");
        //判断是否需要下载db
        if (lastSyncTime == -1) {
            //需要下载db，则先去服务端获取下载db的URL
            getEopDbDownloadURL(new DownloadCallbackForFull());
        } else {
            // 判断是否当天，否则去服务端获取下载db的URL
            String day = DateUtils.getDay(lastSyncTime);
            String today = DateUtils.getDay(System.currentTimeMillis());
            if (!day.equals(today)) {
                MFSPHelper.setLong("lastSyncTime", -1);
                UserDao.getInstance(context).deleteDb();
                getEopDbDownloadURL(new DownloadCallbackForFull());
            } else {
                sendLoginBroadcast();
            }
        }
    }
}
