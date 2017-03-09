package com.movit.platform.cloud.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.movit.platform.cloud.R;
import com.movit.platform.cloud.net.AsyncNetHandler;
import com.movit.platform.cloud.net.INetHandler;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.ToastUtils;

import org.json.JSONException;

import okhttp3.Call;

/**
 * Created by air on 16/5/23.
 * New Folder
 */
public class NewFolderActivity extends BaseActivity{

    private TextView tvLeft;
    private TextView tvRight;
    private TextView tvTitle;
    private EditText etNewFolder;

    private String path;

    INetHandler netHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_folder);
        findViews();
        initDate();
    }

    private void findViews(){
        tvLeft = (TextView)findViewById(R.id.common_top_left);
        tvLeft.setBackgroundResource(android.R.color.transparent);
        tvRight = (TextView)findViewById(R.id.tv_common_top_right);
        tvRight.setBackgroundResource(android.R.color.transparent);
        tvTitle = (TextView)findViewById(R.id.common_top_title);
        tvTitle.setText(getResources().getText(R.string.sky_drive_new_folder));
        etNewFolder = (EditText) findViewById(R.id.et_new_folder);

        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = etNewFolder.getText().toString().trim();
                if(!TextUtils.isEmpty(folderName)){
                    newFolder(folderName);
                }else {
                    ToastUtils.showToast(context,getString(R.string.sky_drive_hint_folder_name));
                }
            }
        });
    }

    private void initDate(){
        netHandler = AsyncNetHandler.getInstance(this);
        path = getIntent().getStringExtra("path");
    }


    private void newFolder(String folderName){
        progressDialogUtil.showLoadingDialog(context,"",false);
        String newPath = path+"/"+folderName;
        netHandler.createFolder(newPath, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                progressDialogUtil.dismiss();
                ToastUtils.showToast(context,getString(R.string.sky_drive_new_folder_failed));
            }

            @Override
            public void onResponse(String response) throws JSONException {
                Log.d("NewFolderActivity", "onResponse: "+response);
                progressDialogUtil.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
