package com.movit.platform.framework.core.okhttp.callback;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Response;

public abstract class ListCallback extends Callback<JSONArray> {
    @Override
    public JSONArray parseNetworkResponse(Response response) throws IOException {
        String result = response.body().string();

        Log.d("ListCallback","response="+result);

        JSONArray array = null;
        try {
            array = new JSONArray(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }
}

