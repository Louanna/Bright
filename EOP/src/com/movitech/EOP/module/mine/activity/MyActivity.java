package com.movitech.EOP.module.mine.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.mine.fragment.MyFragemnt;

/**
 * Created by air on 16/9/19.
 *
 */
public class MyActivity extends EOPBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eop_activity_my);

        MyFragemnt myFragemnt = new MyFragemnt();
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.add(R.id.my_frame, myFragemnt);
        ft.commit();

        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(getResources().getString(R.string.tab_setting));

        ImageView back = (ImageView) findViewById(R.id.common_top_left);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
