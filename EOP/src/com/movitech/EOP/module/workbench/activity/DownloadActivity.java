package com.movitech.EOP.module.workbench.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.cloud.utils.FileType;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.manager.HttpManager;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.view.NumberProgressBar;

import org.json.JSONException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

/**
 * Created by Administrator on 2017/2/20.
 */

public class DownloadActivity extends EOPBaseActivity {

    private ImageView iv_filetype;
    private TextView tv_filename;
    private NumberProgressBar bnp;

    private Timer timer;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        initViews();
        downloadFile();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(bnp.getProgress()<99){
                            bnp.incrementProgressBy(1);
                        }
                    }
                });
            }
        }, 1000, 100);
    }

    private void downloadFile() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        String cookies = intent.getStringExtra("cookies");

        String[] params = url.substring(url.lastIndexOf("?")).split("&");
        String filename = "";
        for (int i = 0; i < params.length; i++) {
            if (params[i].contains("filename")) {
                filename = params[i].substring(params[i].lastIndexOf("=") + 1);
                try {
                    filename = java.net.URLDecoder.decode(filename, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        FileType.getFileTYpe(iv_filetype,filename);
        tv_filename.setText(filename);

        HttpManager.downloadFile(url, cookies, new FileCallBack(CommConstants.SD_CARD_IM, filename) {
            @Override
            public void onError(Call call, Exception e) {
            }

            @Override
            public void onResponse(File response) throws JSONException {
                file = response;
            }

            @Override
            public void inProgress(float progress, long total) {

            }

            @Override
            public void onAfter(){
                bnp.setProgress(100);
            }
        });
    }

    private void initViews() {
        ((TextView) findViewById(R.id.common_top_title)).setText("附件");
        findViewById(R.id.common_top_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadActivity.this.finish();
            }
        });

        iv_filetype = (ImageView) findViewById(R.id.iv_filetype);
        tv_filename = (TextView) findViewById(R.id.tv_filename);
        bnp = (NumberProgressBar) findViewById(R.id.numberbar1);

        Button bt_download = (Button) findViewById(R.id.bt_download);
        bt_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bnp.getProgress() == 100) {
                    //使用其他应用打开
                    FileNameMap fileNameMap = URLConnection.getFileNameMap();
                    String type = fileNameMap.getContentTypeFor(file.getName());
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), type);
                    startActivity(intent);

                } else {
                    Toast.makeText(DownloadActivity.this, "附件下载中,请稍后...", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }
}
