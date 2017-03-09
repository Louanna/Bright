package com.movit.platform.framework.view.faceview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.R;

public class FaceAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private int currentPage = 0;
	private Map<String, Integer> mFaceMap;
	private List<Integer> faceList = new ArrayList<Integer>();
	Context context;
	float width;

	public FaceAdapter(Context context, int currentPage) {
		this.inflater = LayoutInflater.from(context);
		this.currentPage = currentPage;
		mFaceMap = CommConstants.mFaceMap;
		this.context = context;
		initData();
		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		width = displayMetrics.widthPixels;// 得到宽度
	}

	private void initData() {
		for (Map.Entry<String, Integer> entry : mFaceMap.entrySet()) {
			faceList.add(entry.getValue());
		}
	}

	@Override
	public int getCount() {
		return CommConstants.NUM + 1;
	}

	@Override
	public Object getItem(int position) {
		return faceList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.comm_item_faceview, null, false);
			viewHolder.faceIV = (ImageView) convertView
					.findViewById(R.id.face_iv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (position == CommConstants.NUM) {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.emotion_del_normal);
			bitmap = Bitmap.createScaledBitmap(bitmap, (int) width / 11,
					(int) width / 11, true);
			viewHolder.faceIV.setImageBitmap(bitmap);
		} else {
			int count = CommConstants.NUM * currentPage + position;
			int resourceId = faceList.get(count);

			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), resourceId);
			bitmap = Bitmap.createScaledBitmap(bitmap, (int) width / 11,
					(int) width / 11, true);
			viewHolder.faceIV.setImageBitmap(bitmap);
		}
		return convertView;
	}

	public static class ViewHolder {
		ImageView faceIV;
	}
}
