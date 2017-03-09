package com.movitech.EOP.module.qrcode;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.Test.R;

public class ManualActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		TextView topRight = (TextView) findViewById(R.id.common_top_img_right);
		ImageView back = (ImageView) findViewById(R.id.common_top_img_left);
		TextView title = (TextView) findViewById(R.id.common_top_title);
		title.setText("首次使用帮助");
		topRight.setVisibility(View.GONE);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
			RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
			layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
		}
	}
}
