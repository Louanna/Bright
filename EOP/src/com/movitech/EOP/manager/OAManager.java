package com.movitech.EOP.manager;

import android.content.Intent;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.StringUtils;
import com.movitech.EOP.activity.MainActivity;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.module.workbench.model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Administrator on 2016/11/16.
 */

public class OAManager {

    public static final String OA = "OA";
    public static final String ERP = "ERP";

    /**
     * 生成access_token
     */
    public static void createToken(final OACallback oaCallback) {

        StringBuilder sb = new StringBuilder();
        String url = CommConstants.SSO_URL + "/authserver/open/genrateToken?";

        sb.append(url).append("username=").append(CommConstants.loginConfig.getmUserInfo().getEmpAdname());
        sb.append("&").append("password=").append(CommConstants.loginConfig.getPassword());

        StringCallback stringCallback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("result")) {
                        CommConstants.ACCESSTOKEN = responseObj.getJSONObject("objValue").getString("access_token");

                        if (null != oaCallback) {
                            oaCallback.getOAData();
                        }

                    }
                }
            }
        };

        HttpManager.postJson(sb.toString(), "", stringCallback);
    }

    /**
     * 验证access_token
     */
    public static void validateToken(final OACallback oaCallback) {

        StringBuilder sb = new StringBuilder();
        String url = CommConstants.SSO_URL + "/authserver/open/validateAccessToken?";

        sb.append(url).append("access_token=").append(CommConstants.ACCESSTOKEN);
        sb.append("&").append("appFrom=").append("Movitech");

        StringCallback stringCallback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {


            }

            @Override
            public void onResponse(String response) throws JSONException {
                if (StringUtils.notEmpty(response)) {
                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("result")) {
                        oaCallback.getOAData();
                    }
                } else {
                    createToken(oaCallback);
                }
            }
        };

        HttpManager.postJson(sb.toString(), "", stringCallback);
    }

    /**
     * 获取OA详情
     */
    public static void getOAData(String type, int currentpage, int pagesize, final OACallback callback) {

        StringBuilder sb = new StringBuilder();

        String url = CommConstants.SSO_URL + "/authserver/open/oaInfo?";

        sb.append(url).append("token=").append(CommConstants.ACCESSTOKEN);
        sb.append("&").append("type=").append(type);
        sb.append("&").append("currentpage=").append(currentpage);
        sb.append("&").append("pagesize=").append(pagesize);

        StringCallback stringCallback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                callback.onError();
            }

            @Override
            public void onResponse(String response) throws JSONException {

                if (StringUtils.notEmpty(response)) {
                    JSONArray objList = new JSONObject(response).getJSONArray("record");
                    List<News> data = new ArrayList<>();
                    for (int i = 0; i < objList.length(); i++) {

                        News news = new News();
                        try {
                            news.setTime(objList.getJSONObject(i).getString("create_time"));
                            news.setTitle(objList.getJSONObject(i).getString("title"));
                            news.setUrl(objList.getJSONObject(i).getString("app_url"));
                            data.add(news);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    callback.showDataList(data);

                    JSONObject numObj = new JSONObject(response).getJSONObject("page");

                    if (StringUtils.notEmpty(numObj) && numObj.has("recordcount")) {
                        callback.showUnreadCount(numObj.getInt("recordcount"));
                    } else {
                        callback.showUnreadCount(0);
                    }

                } else {
                    callback.onError();
                }
            }
        };

        HttpManager.postJson(sb.toString(), "", stringCallback);
    }

    /**
     * 获取OA/ERP的待办已办
     * type 类型
     * proposer申请人
     * systemCode来源 OA或者ERP
     * pageValue页数默认值是1
     * content搜索内容
     */
    public static void getOAORERPList(String type, String systemCode
            , int pageValue, String content, StringCallback callback) {

        if (null != CommConstants.loginConfig.getmUserInfo()) {
            StringBuilder sb = new StringBuilder();

            String url;

            if (OAManager.OA.equals(systemCode)) {
                url = CommConstants.SSO_URL + "/authserver/open/oaList?";
            } else {
                url = CommConstants.SSO_URL + "/authserver/open/erpList?";
            }

            sb.append(url).append("userId=").append(CommConstants.loginConfig.getmUserInfo().getId());
            sb.append("&").append("type=").append(type);
            sb.append("&").append("pageNo=").append(String.valueOf(pageValue));
            sb.append("&").append("pagesize=").append("");
            sb.append("&").append("title=").append(content);

            HttpManager.postJson(sb.toString(), "", callback);
        }

    }

    /**
     * 获取ERP待办数
     */
    public static void getERPTodoNum() {

        if (null != CommConstants.loginConfig && null != CommConstants.loginConfig.getmUserInfo()) {
            String url = CommConstants.SSO_URL + "/authserver/open/erpTodoCount?userId=" + CommConstants.loginConfig.getmUserInfo().getId();

            StringCallback stringCallback = new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {

                }

                @Override
                public void onResponse(String response) throws JSONException {

                    if (StringUtils.notEmpty(response)) {
                        JSONObject responseObj = new JSONObject(response);

                        if (responseObj.getBoolean("result")) {
                            Intent intent = new Intent();
                            intent.setAction(MainActivity.UNREAD_PROCESS);

                            CommConstants.ERPUNREADNUM = responseObj.getInt("objValue");
                            intent.putExtra("oaUnreadNum", CommConstants.OAUNREADNUM);
                            intent.putExtra("erpUnreadNum", CommConstants.ERPUNREADNUM);
                            EOPApplication.getInstance().sendBroadcast(intent);
                        }
                    }
                }
            };

            HttpManager.postJson(url, "", stringCallback);
        }
    }

    /**
     * 获取OA待办数
     */
    public static void getOATodoNum() {

        if (null != CommConstants.loginConfig && null != CommConstants.loginConfig.getmUserInfo()) {
            String url = CommConstants.SSO_URL + "/authserver/open/oaTodoCount?userId=" + CommConstants.loginConfig.getmUserInfo().getId();

            StringCallback stringCallback = new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {

                }

                @Override
                public void onResponse(String response) throws JSONException {

                    if (StringUtils.notEmpty(response)) {
                        JSONObject responseObj = new JSONObject(response);

                        if (responseObj.getBoolean("result")) {
                            Intent intent = new Intent();
                            intent.setAction(MainActivity.UNREAD_PROCESS);

                            CommConstants.OAUNREADNUM = responseObj.getInt("objValue");
                            intent.putExtra("erpUnreadNum", CommConstants.ERPUNREADNUM);
                            intent.putExtra("oaUnreadNum", CommConstants.OAUNREADNUM);
                            EOPApplication.getInstance().sendBroadcast(intent);
                        }
                    }
                }
            };

            HttpManager.postJson(url, "", stringCallback);
        }
    }

    /**
     * 之前有用的,后来接口设计更改了,这个暂时不用了
     */
    public static void getBpiURL() {

        StringCallback stringCallback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {

                if (StringUtils.notEmpty(response)) {
                    JSONObject responseObj = new JSONObject(response);
                    if (responseObj.getBoolean("ok") && StringUtils.notEmpty(responseObj.get("objValue"))) {
                        JSONArray array = responseObj.getJSONArray("objValue");
                        for (int i = 0; i < array.length(); i++) {
                            CommConstants.BPIURL.put(array.getJSONObject(i).getString("systemCode"), array.getJSONObject(i).getString("appUrl"));
                        }
                    }
                }

            }
        };

        HttpManager.getJsonWithToken(CommConstants.URL_BPI, stringCallback);
    }

    /**
     * 用户操作日志
     */
    public static void addUserActionLog(final String moduleId, final int action) {
        String url = CommConstants.URL_EOP_API + "r/sys/appmgtrest/savemodulelog";
        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();

        if (null != userInfo) {
            url = url + "?userId=" + userInfo.getId() + "&moduleId=" + moduleId + "&action=" + action;

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpUtils.postStringWithToken()
                    .content("")
                    .mediaType(JSON)
                    .url(url)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response) throws JSONException {
                        }
                    });
        }
    }

}
