package com.movitech.EOP.module.workbench.attendance.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.CircleImageView;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.workbench.attendance.model.Attendance;

public class AttendanceListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater mInflater;
	private List<Attendance> mData;
	private Handler handler;
	AQuery aq;
	float width;

	public AttendanceListAdapter(Context context, List<Attendance> mData,
			ListView listView, Handler handler) {
		super();
		this.context = context;
		this.mData = mData;
		this.mInflater = LayoutInflater.from(context);
		this.handler = handler;

		Display display = ((Activity) context).getWindowManager()
				.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		width = displayMetrics.widthPixels;// 得到宽度
		aq = new AQuery(context);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int postion, View converView, ViewGroup arg2) {
		ViewHolder holder = null;
		holder = new ViewHolder();
		if (postion == 0) {
			converView = initTopView(converView, arg2, holder);
		} else {
			converView = initListView(converView, arg2, holder);
		}
		converView.setTag(holder);

		AQuery aQuery = aq.recycle(converView);
		if (postion == 0) {
			initTopData(holder, aQuery);
		} else {
			initListData(holder, converView, arg2, postion, aQuery);
		}

		return converView;
	}

	public View initTopView(View converView, ViewGroup arg2, ViewHolder holder) {
		converView = mInflater.inflate(R.layout.attendance_list_item_0, arg2,
				false);
		holder.avatar = (CircleImageView) converView
				.findViewById(R.id.ea_head_avatar);
		holder.name = (TextView) converView.findViewById(R.id.ea_head_name);
		holder.day = (TextView) converView.findViewById(R.id.ea_head_time);
		holder.pencil = (ImageView) converView
				.findViewById(R.id.ea_head_pencil);
		holder.line = (View) converView.findViewById(R.id.ea_head_line);
		return converView;
	}

	public View initListView(View converView, ViewGroup arg2, ViewHolder holder) {
		converView = mInflater.inflate(R.layout.attendance_list_item, arg2,
				false);
		holder.time = (TextView) converView.findViewById(R.id.ea_item_time);
		holder.reason = (TextView) converView.findViewById(R.id.ea_item_reason);
		holder.location = (TextView) converView
				.findViewById(R.id.ea_item_location);
		holder.photo1 = (ImageView) converView
				.findViewById(R.id.ea_item_photo_1);
		holder.photo2 = (ImageView) converView
				.findViewById(R.id.ea_item_photo_2);
		holder.photo3 = (ImageView) converView
				.findViewById(R.id.ea_item_photo_3);

		return converView;
	}

	public void initTopData(ViewHolder holder, AQuery aQuery) {
		holder.flag = "0";
		String myUserId = MFSPHelper.getString(CommConstants.USERID);
		UserDao dao = UserDao.getInstance(context);
		UserInfo userInfo = dao.getUserInfoById(myUserId);
		if (userInfo == null) {
			return;
		}
		holder.name.setText(userInfo.getEmpCname());
		holder.day.setText(mData.get(0).getTime());
		int picId = R.drawable.avatar_male;
		if ("男".equals(userInfo.getGender())) {
			picId = R.drawable.avatar_male;
		} else if ("女".equals(userInfo.getGender())) {
			picId = R.drawable.avatar_female;
		}

		String uname = MFSPHelper.getString(CommConstants.AVATAR);
		String avatarName = userInfo.getAvatar();
		String avatarUrl = "";

		if (StringUtils.notEmpty(avatarName)) {
			avatarUrl = avatarName;
		}
		if (StringUtils.notEmpty(uname)) {
			avatarUrl = uname;
		}
		// 这边的图片不做缓存处理 这边的是圆的
		if (StringUtils.notEmpty(avatarUrl)) {
			aQuery.id(holder.avatar).image(CommConstants.URL_DOWN + avatarUrl,
					false, true, 128, picId);
		} else {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), picId);
			holder.avatar.setImageBitmap(bitmap);
		}

		holder.pencil.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handler.sendEmptyMessage(1);
			}
		});

		if (mData.size() == 1) {
			holder.line.setVisibility(View.INVISIBLE);
		}
	}

	public void initListData(ViewHolder holder, View converView,
			ViewGroup arg2, final int postion, AQuery aQuery) {
		Attendance ea = mData.get(postion);
		holder.flag = ea.getId();
		holder.time.setText(ea.getTime().substring(11,
				ea.getTime().length() - 3));
		holder.reason.setText(ea.getReason());
		holder.location.setText(ea.getLocation());
		String pictures = ea.getPhotos();
		System.out.println(pictures);
		final String[] photos = pictures.split(",");
		BitmapAjaxCallback callback = new BitmapAjaxCallback();
		callback.animation(AQuery.FADE_IN_NETWORK);
		callback.rotate(true);
		callback.preset(aQuery.getCachedImage(R.drawable.zone_pic_default));
		aQuery.id(holder.photo1).image(photos[0], true, true, 256, 0, callback);
		if (photos.length >= 2) {
			BitmapAjaxCallback callback2 = new BitmapAjaxCallback();
			callback2.animation(AQuery.FADE_IN_NETWORK);
			callback2.rotate(true);
			callback2
					.preset(aQuery.getCachedImage(R.drawable.zone_pic_default));
			aQuery.id(holder.photo2).image(photos[1], true, true, 256, 0,
					callback2);
			if (photos.length == 3) {
				BitmapAjaxCallback callback3 = new BitmapAjaxCallback();
				callback3.animation(AQuery.FADE_IN_NETWORK);
				callback3.rotate(true);
				callback3.preset(aQuery
						.getCachedImage(R.drawable.zone_pic_default));
				aQuery.id(holder.photo3).image(photos[2], true, true, 256, 0,
						callback3);
			} else {
				holder.photo3.setVisibility(View.GONE);
			}
		} else {
			holder.photo2.setVisibility(View.GONE);
			holder.photo3.setVisibility(View.GONE);
		}

		holder.photo1.setOnClickListener(new MyOnClickListener(0, photos));
		holder.photo2.setOnClickListener(new MyOnClickListener(1, photos));
		holder.photo3.setOnClickListener(new MyOnClickListener(2, photos));
	}

	public class MyOnClickListener implements OnClickListener {
		int postion;
		String[] photos;

		public MyOnClickListener(int postion, String[] photos) {
			super();
			this.postion = postion;
			this.photos = photos;
		}

		@Override
		public void onClick(View v) {
			ArrayList<String> imageNames = new ArrayList<String>();
			ArrayList<String> preset = new ArrayList<String>();
			for (int i = 0; i < photos.length; i++) {
				imageNames.add(photos[i]);
				String smallName = photos[i].replace(".", "_s.");
				preset.add(smallName);
			}
			Intent intent = new Intent(context, ImageViewPagerActivity.class);
			int[] location = new int[2];
			v.getLocationOnScreen(location);
			intent.putExtra("locationX", location[0]);
			intent.putExtra("locationY", location[1]);
			intent.putExtra("width", v.getWidth());
			intent.putExtra("height", v.getHeight());
			intent.putStringArrayListExtra("selectedImgs", imageNames);
			intent.putStringArrayListExtra("presetImgs", preset);
			intent.putExtra("isLongURL", true);
			intent.putExtra("postion", postion);
			context.startActivity(intent);
			((Activity) context).overridePendingTransition(0, 0);
		}

	}

	public final class ViewHolder {
		public String flag;
		public CircleImageView avatar;
		public TextView name;
		public TextView day;
		public View line;
		public TextView reason;
		public TextView location;
		public TextView time;
		public ImageView pencil;
		public ImageView photo1;
		public ImageView photo2;
		public ImageView photo3;

	}
}
