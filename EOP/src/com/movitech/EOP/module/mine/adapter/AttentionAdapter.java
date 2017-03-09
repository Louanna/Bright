package com.movitech.EOP.module.mine.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movitech.EOP.Test.R;

import java.util.ArrayList;

public class AttentionAdapter extends BaseAdapter {
	private ArrayList<String> datas;
	private Context mContext;
	private LayoutInflater inflater;

	public AttentionAdapter(ArrayList<String> datas, Context mContext) {
		super();
		this.mContext = mContext;
		inflater = LayoutInflater.from(mContext);
		this.datas = datas;
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.contact_expandlist_child_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView
					.findViewById(R.id.contact_item_icon);
			holder.name = (TextView) convertView
					.findViewById(R.id.contact_item_name);
			holder.content = (TextView) convertView
					.findViewById(R.id.contact_item_content);
			holder.subName = (TextView) convertView
					.findViewById(R.id.contact_item_sub_name);
			holder.checkbox = (CheckBox) convertView
					.findViewById(R.id.contact_item_checkbox);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		String userId = (String) getItem(position);
		UserDao dao = UserDao.getInstance(mContext);
		final UserInfo userInfo = dao.getUserInfoById(userId);
		if (userInfo != null) {
			int picId = R.drawable.avatar_male;
			if (mContext.getString(R.string.boy).equals(userInfo.getGender())) {
				picId = R.drawable.avatar_male;
			} else if (mContext.getString(R.string.girl).equals(userInfo.getGender())) {
				picId = R.drawable.avatar_female;
			}
			String uname = MFSPHelper.getString(CommConstants.AVATAR);
			String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
			String avatarName = userInfo.getAvatar();

			String avatarUrl = "";
			if (StringUtils.notEmpty(avatarName)) {
				avatarUrl = avatarName;
			}
			if (adname.equalsIgnoreCase(userInfo.getEmpAdname())
					&& StringUtils.notEmpty(uname)) {
				avatarUrl = uname;
			}
			if (StringUtils.notEmpty(avatarUrl)) {
				MFAQueryHelper.setImageView(holder.icon,CommConstants.URL_DOWN + avatarUrl,picId);
			} else {
				Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId,
						10);
				holder.icon.setImageBitmap(bitmap);
			}

			// holder.content.setVisibility(View.VISIBLE);
			holder.subName.setVisibility(View.GONE);
			holder.name.setText(userInfo.getEmpCname());
			holder.subName.setText(userInfo.getEmpAdname());
			OrganizationTree org = dao
					.getOrganizationByOrgId(userInfo.getOrgId());
			if (org!=null) {
				holder.content.setText(org.getObjname());
			}

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext,
							UserDetailActivity.class);
					intent.putExtra("userInfo", userInfo);
					mContext.startActivity(intent);
				}
			});
			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					String adnameString = MFSPHelper.getString(CommConstants.EMPADNAME);
					if (userInfo.getEmpAdname().equalsIgnoreCase(adnameString)) {
						return true;
					}
					return true;
				}
			});
		}
		return convertView;
	}

	class ViewHolder {
		ImageView icon;
		TextView name;
		TextView content;
		TextView subName;
		CheckBox checkbox;
	}
}
