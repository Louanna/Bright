package com.movitech.EOP.module.workbench.meeting.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.StringUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.workbench.meeting.model.Meeting;

public class MeetingAdapter extends BaseAdapter {

	private Context context;
	private List<Object> mettings;
	Meeting metting = null;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	public List<Object> getMettings() {
		return mettings;
	}

	public void setMettings(List<Object> mettings) {
		this.mettings = mettings;
	}

	public MeetingAdapter(Context context, List<Object> mettings) {
		super();
		this.context = context;
		this.mettings = mettings;
	}

	@Override
	public int getCount() {
		return (mettings == null) ? 0 : mettings.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null
				|| convertView.getTag(R.drawable.icon + position) == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_meeting, parent, false);
			viewHolder.title = (TextView) convertView
					.findViewById(R.id.metting_title);
			viewHolder.content = (TextView) convertView
					.findViewById(R.id.metting_content);
			viewHolder.metting = (TextView) convertView
					.findViewById(R.id.metting);
			viewHolder.metting_man = (TextView) convertView
					.findViewById(R.id.metting_man);
			viewHolder.hline = (View) convertView.findViewById(R.id.hline);
			convertView.setTag(R.id.icon + position, viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag(R.drawable.icon
					+ position);
		}
		Object o = mettings.get(position);

		if (o instanceof String) {
			viewHolder.title.setVisibility(View.GONE);
			viewHolder.content.setVisibility(View.GONE);
			viewHolder.metting_man.setVisibility(View.GONE);
			viewHolder.metting.setVisibility(View.VISIBLE);
			viewHolder.metting.setText(o.toString());
			if ((position + 1 + 1) < getCount()) {
				Object o1 = mettings.get(position + 1);
				if (o1 instanceof String) {
					viewHolder.hline.setVisibility(View.GONE);
					viewHolder.title.setVisibility(View.VISIBLE);
					viewHolder.title.setGravity(Gravity.CENTER);
					viewHolder.title.setText("暂无数据");
				}
			} else if ((position + 1) == getCount()) {
				Object o1 = mettings.get(position);
				if (o1 instanceof String) {
					viewHolder.hline.setVisibility(View.GONE);
					viewHolder.title.setVisibility(View.VISIBLE);
					viewHolder.title.setGravity(Gravity.CENTER);
					viewHolder.title.setText("暂无数据");
				}
			}
		} else {
			metting = (Meeting) o;
			viewHolder.title.setVisibility(View.VISIBLE);
			viewHolder.metting.setVisibility(View.GONE);
			if (metting.isShowing()) {
				viewHolder.content.setVisibility(View.VISIBLE);
				viewHolder.metting_man.setVisibility(View.VISIBLE);
			} else {
				viewHolder.content.setVisibility(View.GONE);
				viewHolder.metting_man.setVisibility(View.GONE);
			}

			String startTime = metting.getMeetingbeginDate();
			String endTime = metting.getMeetingendDate();
			String room = metting.getMeetingroom();
			String dateString = metting.getMettingDate();
			String todayString = format.format(new Date());
			if (StringUtils.empty(startTime)) {
				startTime = "待定";
			}
			if (StringUtils.empty(endTime)) {
				endTime = "待定";
			}
			if (StringUtils.empty(room)) {
				room = "";
			}
			if (todayString.equalsIgnoreCase(dateString)) {
				viewHolder.title.setText(startTime + "-" + endTime + " "
						+ metting.getMettingTitle());
			} else {
				viewHolder.title.setText(dateString + " " + startTime + "-"
						+ endTime + " " + metting.getMettingTitle());
			}
			viewHolder.content.setText("主题：" + metting.getMettingTitle() + "\n"
					+ "地点：" + room);
			final String meettingUserString = metting.getMeetingUser();
			viewHolder.metting_man.setText("召集人：" + meettingUserString);
			viewHolder.metting_man.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,
							UserDetailActivity.class);
					UserInfo userInfo = getUser(meettingUserString);
					if (userInfo != null) {
						intent.putExtra("userInfo", userInfo);
						context.startActivity(intent);
					}
				}
			});
		}
		if (getCount() == 2) {
			viewHolder.hline.setVisibility(View.GONE);
			viewHolder.title.setVisibility(View.VISIBLE);
			viewHolder.title.setText("暂无数据");
			viewHolder.title.setGravity(Gravity.CENTER);
		}
		return convertView;
	}

	private UserInfo getUser(String name) {
		try {
			String[] names = name.split("\\.");
			name = names[1] + "." + names[0];
			for (UserInfo userInfo : CommConstants.allUserInfos) {
				if (userInfo.getEmpCname().equalsIgnoreCase(name)) {
					return userInfo;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isEnabled(int position) {
		if (mettings != null && mettings.size() >= (position + 1)) {
			if (mettings.get(position) instanceof String) {
				return false;
			} else if (mettings.get(position) instanceof Meeting) {
				return true;
			}
		}
		return super.isEnabled(position);
	}

	class ViewHolder {
		TextView title;
		TextView content;
		TextView metting;
		TextView metting_man;
		View hline;
	}
}
