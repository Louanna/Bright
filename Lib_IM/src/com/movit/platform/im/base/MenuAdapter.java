package com.movit.platform.im.base;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.im.R;

public class MenuAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	Context context;
	List<Integer> mIntegers = new ArrayList<Integer>();
	List<String> titles = new ArrayList<String>();

	public MenuAdapter(Context context, List<Integer> mIntegers,
			List<String> titles) {
		this.inflater = LayoutInflater.from(context);
		this.context = context;
		this.mIntegers = mIntegers;
		this.titles = titles;
	}

	@Override
	public int getCount() {
		return mIntegers.size();
	}

	@Override
	public Object getItem(int position) {
		return mIntegers.get(position);
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
			convertView = inflater.inflate(R.layout.im_menu_chat, null, false);
			viewHolder.menuImg = (ImageView) convertView
					.findViewById(R.id.menu_img);
			viewHolder.title = (TextView) convertView
					.findViewById(R.id.menu_title);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.menuImg.setImageResource(mIntegers.get(position));
		viewHolder.title.setText(titles.get(position));
		return convertView;
	}

	public static class ViewHolder {
		ImageView menuImg;
		TextView title;
	}
}
