package com.movit.platform.sc.module.zone.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.sc.R;

public class ZonePublishPicGridViewAdapter extends BaseAdapter {
	private LayoutInflater listContainer;
	private int selectedPosition = -1;
	private boolean shape;
	private List<String> selectImagesList;
	Context context;
	AQuery aQuery;

	public boolean isShape() {
		return shape;
	}

	public void setShape(boolean shape) {
		this.shape = shape;
	}

	public class ViewHolder {
		public ImageView image;
		public ImageView delImage;
	}

	public ZonePublishPicGridViewAdapter(Context context,
			List<String> selectImagesList) {
		this.context = context;
		listContainer = LayoutInflater.from(context);
		this.selectImagesList = selectImagesList;
		aQuery = new AQuery(context);
	}

	public int getCount() {
		if (selectImagesList.size() < 9) {
			return selectImagesList.size() + 1;
		} else {
			return selectImagesList.size();
		}
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public void setSelectedPosition(int position) {
		selectedPosition = position;
	}

	public int getSelectedPosition() {
		return selectedPosition;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// 自定义视图
		ViewHolder holder = null;
		holder = new ViewHolder();
		// 获取list_item布局文件的视图
		convertView = listContainer.inflate(R.layout.sc_item_zone_publish,
				null);

		// 获取控件对象
		holder.image = (ImageView) convertView
				.findViewById(R.id.gridview_item_img);
		holder.delImage = (ImageView) convertView
				.findViewById(R.id.gridview_item_delImg);
		// 设置控件集到convertView
		convertView.setTag(holder);
		AQuery aq = aQuery.recycle(convertView);

		holder.image.setScaleType(ScaleType.CENTER_CROP);
		if (position == selectImagesList.size()) {
			aq.id(holder.image).image(R.drawable.group_invite_add);
			holder.delImage.setVisibility(View.GONE);
			if (position == 9) {
				holder.image.setVisibility(View.GONE);
			}
		} else {
			BitmapAjaxCallback callback = new BitmapAjaxCallback();
			callback.animation(AQuery.FADE_IN_NETWORK);
			callback.rotate(true);
			aq.id(holder.image).image(new File(selectImagesList.get(position)),
					true, 256, callback);

			holder.delImage.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

				}
			});
		}

		return convertView;
	}
}