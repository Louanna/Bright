package com.movitech.EOP.module.mine.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.mine.adapter.AttentionAdapter;

public class AttentionListActivity extends EOPBaseActivity {
	TextView title;
	ImageView topLeft;
	TextView topRight;

	ListView listView;
	AttentionAdapter adapter;
	ArrayList<String> datas;
	String type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attentions);
		initView();
		initDatas();
	}

	private void initView() {
		title = (TextView) findViewById(R.id.common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_img_left);
		topRight = (TextView) findViewById(R.id.common_top_img_right);
		topRight.setVisibility(View.GONE);

		listView = (ListView) findViewById(R.id.attention_list);
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void initDatas() {
		progressDialogUtil.showLoadingDialog(AttentionListActivity.this,
				getString(R.string.loading), false);
		type = getIntent().getStringExtra("type");
		UserInfo userInfo = CommConstants.loginConfig
				.getmUserInfo();
		if ("attention".equals(type)) {
			title.setText(getString(R.string.my_attention));
			datas = userInfo.getAttentionPO();
		} else if ("beAttention".equals(type)) {
			title.setText(getString(R.string.attention_me));
			datas = userInfo.getToBeAttentionPO();
		}
		if (datas == null) {
			progressDialogUtil.dismiss();
			return;
		}
		adapter = new AttentionAdapter(datas, context);
		listView.setAdapter(adapter);
		progressDialogUtil.dismiss();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (adapter != null) {
			UserInfo userInfo = CommConstants.loginConfig
					.getmUserInfo();
			if ("attention".equals(type)) {
				datas = userInfo.getAttentionPO();
			} else if ("beAttention".equals(type)) {
				datas = userInfo.getToBeAttentionPO();
			}
			adapter.notifyDataSetChanged();
		}
	}

}
