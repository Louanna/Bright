package com.movitech.EOP.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/1/10.
 */

public class DownloadActivity extends Activity{

    @BindView(R.id.webview)
    WebView myWebView;

    @BindView(R.id.common_top_title)
    TextView tv_topbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        ButterKnife.bind(this);

        initView();
    }

    private void initView(){
        tv_topbar.setText("下载我");
        myWebView.loadUrl("http://p.bre600708.com:81/bg1.jpg");
    }

    @OnClick(R.id.common_top_img_left)
    public void gotoHomePage(){
        this.finish();
    }

    @OnClick(R.id.common_top_close)
    public void closePage(){
        this.finish();
    }

}
