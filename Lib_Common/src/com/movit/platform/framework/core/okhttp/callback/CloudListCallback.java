package com.movit.platform.framework.core.okhttp.callback;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Response;

public abstract class CloudListCallback extends Callback<JSONArray> {
    @Override
    public JSONArray parseNetworkResponse(Response response) throws IOException {
        String result = response.body().string();

        Log.d("ListCallback","response="+result);

        JSONArray array = null;
        try {
            JSONObject object = JSON.parseObject(result);
            array = object.getJSONArray("result");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }
}

