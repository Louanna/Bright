package com.movit.platform.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.movit.platform.common.R;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.task.LoginTask;
import com.movit.platform.framework.utils.DialogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 * Created by Louanna.Lu on 2015/10/10.
 */
public class DownloadOrgReceiver extends BroadcastReceiver {

    private CommonHelper commonHelper;
    private DownloadOrgCallBack downloadOrgCallBack;

    public DownloadOrgReceiver(Context context, DownloadOrgCallBack downloadOrgCallBack) {

        this.commonHelper = new CommonHelper(context);
        this.downloadOrgCallBack = downloadOrgCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (CommConstants.ACTION_ORGUNITION_DONE.equals(action)) {
            int result = intent.getIntExtra("getOrgunitList", -1);
            switch (result) {
                case LoginTask.SUCCESSS:
                    DialogUtils.getInstants().dismiss();

                    CommConstants.IS_LOGIN_EOP_SERVER = true;
                    String mtype = android.os.Build.MODEL; // 手机型号
                    String mtyb = android.os.Build.BRAND;// 手机品牌
                    CommConstants.PHONEBRAND = mtyb + " " + mtype;
                    CommConstants.PHONEVERSION = android.os.Build.VERSION.RELEASE;

                    // 保存用户配置信息
                    LoginInfo loginInfo = commonHelper.getLoginConfig();
                    loginInfo.setPassword(intent.getStringExtra("password"));
                    commonHelper.saveLoginConfig(loginInfo);

                    //初始化表情符
                    initFaceMap(context);
                    initFaceGifMap(context);

                    //后续操作
                    downloadOrgCallBack.afterInitCommon();
                    break;
                case LoginTask.FAIL:
                    DialogUtils.getInstants().dismiss();
                    Toast.makeText(context,context.getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void initFaceGifMap(Context context) {
        String content = ""; // 文件内容字符
        try {
            InputStream instream = context.getResources().getAssets()
                    .open("emoji_gif.json");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                // 分行读取
                while ((line = buffreader.readLine()) != null) {
                    content += line;
                }
                instream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            JSONArray array = jsonObject.getJSONArray("emoji");
            JSONArray keys = array.getJSONArray(0);
            JSONArray values = array.getJSONArray(1);
            Class draw = R.drawable.class;
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                String value = values.getString(i);
                // f_0.gif
                if (value.endsWith(".gif")) {
                    Field field = draw.getDeclaredField(value.substring(0,
                            value.length() - 4));
                    int pic = field.getInt(value);
                    CommConstants.mFaceGifMap.put(key, pic);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFaceMap(Context context) {
        String content = ""; // 文件内容字符
        try {
            InputStream instream = context.getResources().getAssets()
                    .open("emoji.json");
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                // 分行读取
                while ((line = buffreader.readLine()) != null) {
                    content += line;
                }
                instream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(content);
            JSONArray array = jsonObject.getJSONArray("emoji");
            JSONArray keys = array.getJSONArray(0);
            JSONArray values = array.getJSONArray(1);
            Class draw = R.drawable.class;
            for (int i = 0; i < keys.length(); i++) {
                String key = keys.getString(i);
                String value = values.getString(i);
                // f_s_0.png
                Field field = draw.getDeclaredField(value.substring(0,
                        value.length() - 4));
                int pic = field.getInt(value);
                CommConstants.mFaceMap.put(key, pic);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface DownloadOrgCallBack {
        // 初始化结束后的操作
        public void afterInitCommon();
    }
}
