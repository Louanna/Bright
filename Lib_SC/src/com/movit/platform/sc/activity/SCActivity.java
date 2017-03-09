package com.movit.platform.sc.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.sc.R;
import com.movit.platform.sc.module.zone.fragment.ZoneFragment;

public class SCActivity extends FragmentActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_activity);
		FragmentManager fragmentManager = this.getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.common_fragment,new ZoneFragment(),"EmptyFragment");
		transaction.commitAllowingStateLoss();

		TextView topTitle = (TextView) findViewById(R.id.common_top_title);
		topTitle.setText(getIntent().getStringExtra("topTitle"));
		ImageView ivLeft = (ImageView) findViewById(R.id.common_top_left);
		ivLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

}
