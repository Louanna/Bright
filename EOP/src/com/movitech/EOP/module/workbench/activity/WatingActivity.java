package com.movitech.EOP.module.workbench.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;

public class WatingActivity extends EOPBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wating);
        TextView title = (TextView) findViewById(R.id.common_top_title);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        TextView topRight = (TextView) findViewById(R.id.common_top_img_right);
        topRight.setVisibility(View.GONE);
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        title.setText("企业工作流");
    }

}
