package com.movit.platform.sc.module.zone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.sc.R;
import com.movit.platform.sc.activity.SCBaseActivity;
import com.movit.platform.sc.view.clipview.ClipImageView;

import java.io.File;

public class ClipImageActivity extends SCBaseActivity {
	private ClipImageView imageView;
	private TextView topRight;
	private TextView topTitle;
	private ImageView topLeft;

	AQuery aQuery;
	String path;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sc_activity_clipimage);
		aQuery = new AQuery(this);

		path = getIntent().getStringExtra("takePicturePath");
		imageView = (ClipImageView) findViewById(R.id.src_pic);
		topRight = (TextView) findViewById(R.id.common_top_img_right);
		topTitle = (TextView) findViewById(R.id.common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_img_left);
		// 设置需要裁剪的图片
		BitmapAjaxCallback callback = new BitmapAjaxCallback();
		callback.rotate(true);
		aQuery.id(imageView).image(new File(path), true, 1000, callback);
		
		topTitle.setText("");
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		topRight.setText(getString(R.string.complete));
		topRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 此处获取剪裁后的bitmap
				Bitmap bitmap = imageView.clip();
				String tempPath  = PicUtils.compressImageAndSave(path, bitmap, 500);

				Intent intent = new Intent();
				intent.putExtra("takePicturePath", tempPath);
				setResult(3, intent);
				finish();
			}
		});
	}

}
