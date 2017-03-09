package com.movit.platform.im.module.group.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.swipeLayout.SwipeLayout;
import com.movit.platform.framework.widget.swipeLayout.adapter.BaseSwipeAdapter;
import com.movit.platform.im.R;
import com.movit.platform.im.module.group.activity.GroupAllMembersActivity;

import java.util.List;

public class GroupAllMembersAdapter extends BaseSwipeAdapter {

    private static final String TAG = GroupAllMembersAdapter.class.getCanonicalName();
    private List<UserInfo> userInfos;
    private Activity activity;
    private LayoutInflater inflater;
    private int groupType;
    private String createrID;
    AQuery aq;

    public GroupAllMembersAdapter(List<UserInfo> userInfos, Activity activity, int groupType, String createrID) {
        super();
        this.userInfos = userInfos;
        this.activity = activity;
        this.groupType = groupType;
        this.createrID = createrID;
        inflater = LayoutInflater.from(activity);
        aq = new AQuery(activity);
    }

    public List<UserInfo> getUserInfos() {
        return userInfos;
    }

    public void setUserInfos(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }

    @Override
    public int getCount() {
        return (userInfos == null || userInfos.size() == 0) ? 0 : userInfos
                .size();
    }

    @Override
    public Object getItem(int position) {
        return userInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.group_member_layout;
    }

	@Override
	public View generateView(int position, ViewGroup parent) {
		View v = inflater.inflate(R.layout.comm_item_group_member_search, parent, false);
		SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
		swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
		swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
		return v;
	}

    @Override
    public void fillValues(final int position, View convertView) {
        Button delBtn = (Button) convertView.findViewById(R.id.group_all_members_recent_del_btn);
		final UserInfo userInfo = userInfos.get(position);
		if (userInfo == null) {
            return;
        }
        if (createrID.equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID))) {
			if (userInfo.getId().equalsIgnoreCase(createrID)) {
                delBtn.setVisibility(View.GONE);
            } else {
                delBtn.setVisibility(View.VISIBLE);
                delBtn.setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                closeItem(position);
                                GroupAllMembersActivity.chatDetailActivityHandler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        GroupAllMembersActivity.chatDetailActivityHandler.obtainMessage(1, position).sendToTarget();
                                    }
                                }, 200);
                            }
                        });
            }
        } else {
            delBtn.setVisibility(View.GONE);
        }

        convertView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                String adnameString = MFSPHelper.getString(CommConstants.EMPADNAME);
				if (groupType == CommConstants.CHAT_TYPE_GROUP_ANS || userInfo.getEmpAdname().equalsIgnoreCase(adnameString)) {
                    return true;
                }
                Bundle bundle = new Bundle();
				bundle.putSerializable("userInfo", userInfo);
                ((BaseApplication) activity.getApplication()).getUIController().startPrivateChat(activity, bundle);
                return true;
            }
        });

        ViewHolderChild holderChild = new ViewHolderChild();

        holderChild.icon = (ImageView) convertView.findViewById(R.id.group_all_members_item_icon);
        holderChild.name = (TextView) convertView.findViewById(R.id.group_all_members_item_name);
        holderChild.content = (TextView) convertView.findViewById(R.id.group_all_members_item_content);
        holderChild.subName = (TextView) convertView.findViewById(R.id.group_all_members_item_sub_name);

		AQuery aQuery = aq.recycle(convertView);
		try {
			holderChild.content.setVisibility(View.VISIBLE);
			holderChild.subName.setVisibility(View.GONE);

			if (groupType == CommConstants.CHAT_TYPE_GROUP_ANS) {
				holderChild.name.setText(userInfo.getNickName());
				if (createrID.equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID))
						|| userInfo.getId().equalsIgnoreCase(createrID)
						|| userInfo.getId().equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID))) {
					holderChild.subName.setVisibility(View.VISIBLE);
					holderChild.subName.setText("(" + userInfo.getEmpCname() + ")");
				} else {
					holderChild.subName.setVisibility(View.GONE);
				}
				holderChild.content.setVisibility(View.GONE);//匿名成员不显示部门

				if (createrID.equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID))) {//当前用户是群组管理员
					setAvator(aQuery, userInfo, holderChild);
				} else {
					if (userInfo.getId().equalsIgnoreCase(createrID)
							|| userInfo.getId().equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID))){
						setAvator(aQuery, userInfo, holderChild);
					} else {
						Bitmap bitmap = PicUtils.getRoundedCornerBitmap(activity, R.drawable.avatar_ans, 10);
						holderChild.icon.setImageBitmap(bitmap);
					}
				}
			} else {
				setAvator(aQuery, userInfo, holderChild);
				UserDao dao = UserDao.getInstance(activity);
				OrganizationTree org = dao.getOrganizationByOrgId(userInfo.getOrgId());
				if (org != null) {
					holderChild.content.setText(org.getObjname());
				}
				holderChild.name.setText(userInfo.getEmpCname().split("\\.")[0]);
				holderChild.subName.setText(userInfo.getEmpAdname());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setAvator(AQuery aQuery, UserInfo userInfo, final ViewHolderChild viewHolderChild) {
		int picId = R.drawable.avatar_male;
		if ("男".equals(userInfo.getGender())) {
            picId = R.drawable.avatar_male;
        } else if ("女".equals(userInfo.getGender())) {
            picId = R.drawable.avatar_female;
        }
		String uname = MFSPHelper.getString(CommConstants.AVATAR);
		String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
		String avatarName = userInfo.getAvatar();
		String avatarUrl = "";
		if (StringUtils.notEmpty(avatarName)) {
            avatarUrl = avatarName;
        }
		if (adname.equalsIgnoreCase(userInfo.getEmpAdname()) && StringUtils.notEmpty(uname)) {
            avatarUrl = uname;
        }
        final Bitmap bitmap = PicUtils.getRoundedCornerBitmap(activity, picId, 10);
		if (StringUtils.notEmpty(avatarUrl)) {
            BitmapAjaxCallback callback = new BitmapAjaxCallback();
            callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                    .round(10).fallback(picId)
                    .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                    .fileCache(true).targetWidth(128);
//            aQuery.id(viewHolderChild.icon).image(callback);
            aQuery.id(viewHolderChild.icon).image(CommConstants.URL_DOWN + avatarUrl,true,true,0,0,
                    new BitmapAjaxCallback(){
                        @Override
                        protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                            super.callback(url, iv, bm, status);
                            if( null == bm || bm.getByteCount() <= 0){
                                viewHolderChild.icon.setImageBitmap(bitmap);
                            }
                        }
                    });
        } else {
//            Bitmap bitmap = PicUtils.getRoundedCornerBitmap(activity, picId, 10);
			viewHolderChild.icon.setImageBitmap(bitmap);
        }
	}

	final class ViewHolderChild {
		ImageView icon;
		TextView name;
		TextView content;
		TextView subName;
	}

}