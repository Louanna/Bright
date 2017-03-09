package com.movitech.EOP.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.workbench.constants.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/5/25.
 */
public class ServerListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);

        TextView titleTv = (TextView) findViewById(R.id.common_top_title);
        titleTv.setText(getResources().getString(R.string.config_servers));
        ImageView titleLeft = (ImageView) findViewById(R.id.common_top_left);
        titleLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final LinearLayout serverlistLayout = (LinearLayout) findViewById(R.id.ll_server_list);

        HttpManager.getJson(CommConstants.URL_DOWNLOAD + "/server_list1.txt", new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) throws JSONException {

                if (StringUtils.notEmpty(response)) {
                    JSONArray serverList = new JSONArray(response);
                    for (int i = 0; i < serverList.length(); i++) {

                        final JSONObject serverObj = serverList.getJSONObject(i);

                        String serverName = serverObj.getString("server_name");
                        String serverDes = serverObj.getString("server_description");
                        final String apiUrl = serverObj.getString("protocol") + "://" + serverObj.getString("server_url");

                        View serverItem = LayoutInflater.from(ServerListActivity.this).inflate(R.layout.list_item_server, null);
                        ((TextView) serverItem.findViewById(R.id.tv_server_name)).setText(serverName);
                        ((TextView) serverItem.findViewById(R.id.tv_server_describe)).setText(serverDes);

                        final CheckBox serverCheckBox = (CheckBox) serverItem.findViewById(R.id.cb_server);
                        serverCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {

                                    for (int i = 0; i < serverlistLayout.getChildCount(); i++) {
                                        ((CheckBox) serverlistLayout.getChildAt(i).findViewById(R.id.cb_server)).setChecked(false);
                                    }

                                    serverCheckBox.setChecked(true);

                                    try {
                                        CommConstants.BASE_URL = apiUrl;
                                        CommConstants.HOST_PORT = ":" + serverObj.getString("api_port");
                                        CommConstants.HOST_CMS_PORT = ":" + serverObj.getString("cms_port");

                                        CommConstants.URL_XMPP = serverObj.getString("im_url").split(":")[0];
                                        CommConstants.PORT = Integer.valueOf(serverObj.getString("im_url").split(":")[1]);

                                        CommConstants.URL_INTERNALEA = serverObj.getString("attendance_url");

                                        CommConstants.initApiUrl();
                                        Constants.initWorkTableApi();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //手动删除db文件
                                    String PACKAGE_NAME = ServerListActivity.this.getPackageName();
                                    String DATABASE_PATH = "/data"
                                            + Environment.getDataDirectory().getAbsolutePath() + "/"
                                            + PACKAGE_NAME + "/databases";
                                    String databaseFilename = DATABASE_PATH + "/" + "eoop.db";
                                    File dbFile = new File(databaseFilename);
                                    if (dbFile.exists()) {
                                        dbFile.delete();
                                    }

                                    //强制下次登陆重新下载db文件
                                    MFSPHelper.setLong("lastSyncTime", -1);
                                } else {
                                    //如果一个都不选择，则置为默认的
                                    CommConstants.initHost(ServerListActivity.this);
                                    Constants.initWorkTableApi();
                                }
                            }
                        });

                        if (CommConstants.BASE_URL.equalsIgnoreCase(apiUrl)) {
                            serverCheckBox.setChecked(true);
                        } else {
                            serverCheckBox.setChecked(false);
                        }

                        serverlistLayout.addView(serverItem);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) serverItem
                                .getLayoutParams();
                        layoutParams.setMargins(0, 20, 0, 0);
                        serverItem.setLayoutParams(layoutParams);
                    }
                }
            }
        });
    }
}
