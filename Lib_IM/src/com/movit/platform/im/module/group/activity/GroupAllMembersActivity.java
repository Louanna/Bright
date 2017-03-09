package com.movit.platform.im.module.group.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.module.detail.activity.ChatDetailActivity;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.group.fragment.GroupAllMembersFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupAllMembersActivity extends IMBaseActivity {

	private FrameLayout frameLayout;
	protected TextView mTopTitle;
	private ImageView mTopLeftImage;

	public static List<UserInfo> memberUserInfos;
	View membersView;
	private int groupType = -1;//群组类型  4：匿名群组

	public static Handler chatDetailActivityHandler = null;
	public static GroupAllMembersActivity groupAllMembersActivity = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.im_activity_group_all_members);
		initData();
		initView();
		groupAllMembersActivity = this;
	}

	private void initData() {
		if (memberUserInfos != null) {
			memberUserInfos.clear();
		}
		Group group = (Group) getIntent().getSerializableExtra("groupInfo");
		memberUserInfos = (ArrayList<UserInfo>) getIntent().getSerializableExtra("userInfos");
		groupType = getIntent().getIntExtra("groupType", -1);

		String userId = MFSPHelper.getString(CommConstants.USERID);
		if (userId.equals(group.getCreaterId())) {
			memberUserInfos.remove(memberUserInfos.size() - 1);
			memberUserInfos.remove(memberUserInfos.size() - 1);
		} else {
			if (groupType != CommConstants.CHAT_TYPE_GROUP_ANS) {
				memberUserInfos.remove(memberUserInfos.size() - 1);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		GroupAllMembersActivity.chatDetailActivityHandler = null;
		ChatDetailActivity.groupAllMembersAdapter = null;
		ChatDetailActivity.groupSearchResultAdapter = null;
		if (memberUserInfos != null) {
			memberUserInfos.clear();
		}
	}

	private void initView() {
		membersView = LayoutInflater.from(this).inflate(R.layout.comm_activity_all_members, null);
		frameLayout = (FrameLayout) findViewById(R.id.main_frame);
		mTopTitle = (TextView) membersView.findViewById(R.id.common_top_title);
		mTopTitle.setText(getString(R.string.group_all_members_title_str) + "(" + memberUserInfos.size() + ")");
		mTopLeftImage = (ImageView) membersView.findViewById(R.id.common_top_img_left);
		mTopLeftImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

        membersView.findViewById(R.id.common_top_img_right).setVisibility(View.GONE);

		FragmentManager fragmentManager = this.getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.common_fragment, new GroupAllMembersFragment(), "GroupAllMembersFragment");
		transaction.commitAllowingStateLoss();
		frameLayout.addView(membersView);

	}

	/**
	 * 更新标题文字
	 */
	public void refreshTitleText(List<UserInfo> userInfos){
		if (mTopTitle != null) {
			mTopTitle.setText(getString(R.string.group_all_members_title_str) + "(" + userInfos.size() + ")");
		}
	}
	
}
