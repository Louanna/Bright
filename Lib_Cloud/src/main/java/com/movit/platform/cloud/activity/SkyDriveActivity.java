package com.movit.platform.cloud.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.movit.platform.cloud.R;
import com.movit.platform.cloud.net.AsyncNetHandler;
import com.movit.platform.cloud.net.INetHandler;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;

import org.json.JSONException;

import okhttp3.Call;

/**
 * Created by air on 16/3/15.
 *
 * 我的网盘
 */
public class SkyDriveActivity extends BaseActivity {

    /**
     *  获取文档管理文件
     */
    protected final int REQUEST_CODE_SEND_FILE_2 = 511;

    INetHandler netHandler = null;
    String personRootFldId;
    String publicRootFldId;

    private String isChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sky_drive);

        findViews();

        initDate();

    }

    private void findViews(){
        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(R.string.sky_drive_my);
        TextView topRight = (TextView) findViewById(R.id.common_top_img_right);
        topRight.setVisibility(View.GONE);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        PercentRelativeLayout rlPublicArea = (PercentRelativeLayout)findViewById(R.id.rl_public_area);
        PercentRelativeLayout rlPersonalArea = (PercentRelativeLayout)findViewById(R.id.rl_personal_area);
        PercentRelativeLayout rlShareArea = (PercentRelativeLayout)findViewById(R.id.rl_share_area);

        isChat = getIntent().getStringExtra("chat");
        if(!TextUtils.isEmpty(isChat)){
            rlPublicArea.setVisibility(View.GONE);
            rlShareArea.setVisibility(View.GONE);
            rlPersonalArea.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelOffset(R.dimen.dp_200),0
            ));
        }
    }

    private void initDate(){
        netHandler = AsyncNetHandler.getInstance(this);
        netHandler.okmLogin(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                Log.d(SkyDriveActivity.class.getSimpleName(), "onResponse: "+response);
                JSONObject object = JSON.parseObject(response);
                if(object.containsKey("personRootFldId")){
                    personRootFldId = object.getString("personRootFldId");
                }
                if(object.containsKey("publicRootFldId")){
                    publicRootFldId = object.getString("publicRootFldId");
                }
            }
        });
    }


    public void onClick(View v) {
        Intent intent = new Intent(SkyDriveActivity.this,PublicAreaActivity.class);
        if(v.getId() == R.id.rl_public_area){//公共区域
            intent.putExtra("title",getString(R.string.sky_drive_text1));
            intent.putExtra("rootFldId",publicRootFldId);
            startActivityForResult(intent,REQUEST_CODE_SEND_FILE_2);
        }else if(v.getId() == R.id.rl_personal_area){//个人区域
            intent.putExtra("title",getString(R.string.sky_drive_text2));
            intent.putExtra("rootFldId",personRootFldId);
            intent.putExtra("chat",isChat);
            startActivityForResult(intent,REQUEST_CODE_SEND_FILE_2);
        }else if(v.getId() == R.id.rl_share_area){//我的共享区域
            startActivity(new Intent(SkyDriveActivity.this,ShareAreaActivity.class));
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SEND_FILE_2 && null != data){
            setResult(RESULT_OK,data);
            finish();
        }
    }
}
