package com.movit.platform.framework.core.okhttp.callback;

import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.TokenBean;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by zhy on 15/12/14.
 *
 */
public abstract class StringCallback extends Callback<String> {
    @Override
    public String parseNetworkResponse(Response response) throws IOException {
        String result = response.body().string();
        Log.d("StringCallback", "responseStr:"+result);
        TokenBean token = JSON.parseObject(result, TokenBean.class);
        if(null != token && null != token.getMessage()&& null != token.getType()&& "0".equalsIgnoreCase(token.getType())){

            Intent intent = new Intent(CommConstants.SIMPLE_LOGIN_ACTION);
            intent.putExtra("body", token.getMessage());
            intent.putExtra("type", CommConstants.TYPE_LOGINOUT);//强制退出，重新登录
            BaseApplication.getInstance().sendBroadcast(intent);
            return "";
        }else {
            return result;
        }
    }
}

