package com.movit.platform.sc.module.zone.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.sc.R;

import java.util.ArrayList;
import java.util.List;

public class ZoneItemGridAdapter extends BaseAdapter {

	private ArrayList<String> imageNames = new ArrayList<>();
	float width;
	Context context;
	AQuery aq;
	LayoutInflater inflater;

	public ZoneItemGridAdapter(List<String> imageNames, Context context, AQuery aQuery) {
		super();
		inflater = LayoutInflater.from(context);
		this.imageNames.clear();
		this.imageNames.addAll(imageNames);
		this.context = context;
		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		float density = displayMetrics.density; // 得到密度
		width = displayMetrics.widthPixels;// 得到宽度
		aq = aQuery;
	}

	@Override
	public int getCount() {
		return imageNames.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return imageNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.sc_item_zone_gridview,null);
		ImageView imageView = (ImageView) convertView
				.findViewById(R.id.list_gridview_item_img);

		AQuery aQuery = aq.recycle(convertView);
		String uname = (String) getItem(position);
		LayoutParams para = imageView.getLayoutParams();
		para.width = (int) (width / 4);
		para.height = (int) (width / 4);
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.zone_pic_default);
		Log.v("pic", para.width + "--" + para.height);
		final Bitmap bitmap2 = PicUtils.zoomImage(bitmap, para.width,
				para.height);
		imageView.setLayoutParams(para);

		BitmapAjaxCallback callback = new BitmapAjaxCallback() {

			@Override
			protected void callback(String url, ImageView iv, Bitmap bm,
					AjaxStatus status) {
				super.callback(url, iv, bm, status);
				if (status.getCode() != 200) {
					iv.setImageBitmap(bitmap2);
				}
			}
		};
		callback.animation(AQuery.FADE_IN_NETWORK);
		callback.rotate(true);
		callback.ratio(1.0f);
		callback.preset(bitmap2);
		String smallName = uname.replace(".", "_s.");
		Log.v("pic", CommConstants.URL_DOWN + smallName);
//		 String smallName = uname;
		if (aQuery.shouldDelay(position, convertView, parent, CommConstants.URL_DOWN
				+ smallName)) {
			aQuery.id(imageView).image(bitmap2);
		} else {
			aQuery.id(imageView).image(CommConstants.URL_DOWN + smallName, true,
					true, 256, 0, callback);
		}

		imageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				ArrayList<String> preset = new ArrayList<>();
				for (int i = 0; i < imageNames.size(); i++) {
					String smallName = imageNames.get(i).replace(".", "_s.");
					preset.add(smallName);
				}
				Intent intent = new Intent(context,
						ImageViewPagerActivity.class);
				int[] location = new int[2];
				v.getLocationOnScreen(location);
				intent.putExtra("locationX", location[0]);
				intent.putExtra("locationY", location[1]);
				intent.putExtra("width", v.getWidth());
				intent.putExtra("height", v.getHeight());
				intent.putStringArrayListExtra("selectedImgs", imageNames);
				intent.putStringArrayListExtra("presetImgs", preset);
				intent.putExtra("postion", position);
				context.startActivity(intent);
				((Activity) context).overridePendingTransition(0, 0);
			}
		});

		return convertView;
	}
}
