package com.movitech.EOP.module.events.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.activity.MainActivity;
import com.movitech.EOP.base.EOPBaseActivity;

/**
 * Created by Administrator on 2015/12/30.
 */
public abstract class EventBaseActivity extends EOPBaseActivity {

    protected TextView tv_topbar_center;
    protected ImageView iv_topbar_left;
    protected ListView lv_awards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        //初始化标题栏
        initTopBar();
        //填充标题栏
        setTopBarValue();
    }

    //初始化标题栏
    private void initTopBar() {

        tv_topbar_center = (TextView) findViewById(R.id.tv_topbar_center);
        iv_topbar_left = (ImageView) findViewById(R.id.iv_topbar_left);
        TextView tv_topbar_close = (TextView) findViewById(R.id.tv_topbar_close);
        tv_topbar_close.setVisibility(View.VISIBLE);

        tv_topbar_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到首页
                Intent intent = new Intent(EventBaseActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                EventBaseActivity.this.startActivity(intent);
            }
        });

        iv_topbar_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBaseActivity.this.finish();
            }
        });
    }

    //初始化ContentView
    protected void initContentView() {
        lv_awards = (ListView) findViewById(R.id.lv_content);
        lv_awards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //ListView点击事件
                onItemClickListener(i);
            }
        });
    }

    //获取View
    protected abstract int getContentView();

    //填充标题栏
    protected abstract void setTopBarValue();

    //填充页面内容
    protected abstract void setContentValue();

    //ListView点击事件
    protected abstract void onItemClickListener(int position);
}
