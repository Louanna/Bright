package com.movit.platform.common.manager;

import android.content.Context;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Administrator on 2015/12/16.
 */
public class CommManager {

    public static void postDeviceType(final String deviceId, final Context context) {

        String deviceType = "2";
        JSONObject object = new JSONObject();
        try {
            String userId = MFSPHelper.getString(CommConstants.USERID);
            object.put("userId", userId);
            object.put("deviceType", deviceType);
            object.put("device", userId + "," + deviceId);
            object.put("mobilemodel", CommConstants.PHONEBRAND);
            object.put("mobileversion", CommConstants.PHONEVERSION);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        if (StringUtils.notEmpty(BaseApplication.Token)) {
            OkHttpUtils.postStringWithToken()
                    .url(CommConstants.URL_UPDATE_DEVICE)
                    .content(object.toString())
                    .mediaType(JSON)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {

                        }

                        @Override
                        public void onResponse(String response) throws JSONException {

                        }
                    });
        }
    }

    public interface AttentionCallback {
        public void onAfter();
    }

    //获取关注的人和被关注的人的信息
    public static void getAttentionData(final Context mContext, final AttentionCallback callback) {
        OkHttpUtils.getWithToken()
                .url(CommConstants.URL_ATTENTIONS + MFSPHelper.getString(CommConstants.USERID))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                        CommConstants.GET_ATTENTION_FINISH = true;
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        json2obj(response);
                    }

                    @Override
                    public void onAfter() {
                        callback.onAfter();
                    }
                });
    }

    private static void json2obj(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("objValue")
                    && !jsonObject.isNull("objValue")) {
                JSONObject objValue = jsonObject.getJSONObject("objValue");

                ArrayList<String> toBeAttentionPO = new ArrayList<String>();
                if (null != objValue && objValue.has("toBeAttentionPO")
                        && !objValue.isNull("toBeAttentionPO")) {
                    JSONArray array = objValue.getJSONArray("toBeAttentionPO");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String userid = "";
                        if (object.has("userid")) {
                            userid = object.getString("userid");
                            if (!toBeAttentionPO.contains(userid)) {
                                toBeAttentionPO.add(userid);
                            }
                        }
                    }
                }

                ArrayList<String> attentionPO = new ArrayList<String>();
                if (objValue.has("attentionPO") && !objValue.isNull("attentionPO")) {
                    JSONArray array = objValue.getJSONArray("attentionPO");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        String attentionid = "";
                        if (object.has("attentionid")) {
                            attentionid = object.getString("attentionid");
                            if (!attentionPO.contains(attentionid)) {
                                attentionPO.add(attentionid);
                            }
                        }
                    }
                }
                CommConstants.loginConfig.getmUserInfo().setAttentionPO(attentionPO);
                CommConstants.loginConfig.getmUserInfo().setToBeAttentionPO(toBeAttentionPO);
            }
            //标记获取完毕，在tab页中需要先判断是否获取完毕，获取完毕再显示页面内容，否则loading
            CommConstants.GET_ATTENTION_FINISH = true;
        } catch (JSONException e) {
            e.printStackTrace();
            CommConstants.GET_ATTENTION_FINISH = true;
        }
    }
}
