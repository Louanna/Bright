package com.movit.platform.im.module.group.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.module.detail.activity.ChatDetailActivity;
import com.movit.platform.im.module.group.activity.GroupAllMembersActivity;
import com.movit.platform.im.module.group.adapter.GroupAllMembersAdapter;
import com.movit.platform.im.module.group.entities.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAllMembersFragment extends Fragment {

	public static ListView listView;
	private GroupAllMembersAdapter adapter;
	private GroupAllMembersAdapter searchMemberAdapter;
	private LinearLayout searchView;
	private ListView searchMemberList;
	private EditText searchText;
	private ImageView searchClear;
	private LinearLayout searchBar;

	private List<UserInfo> memberList = new ArrayList<UserInfo>();
	private List<UserInfo> searchResultList = new ArrayList<UserInfo>();
	protected Handler handler;
	private InputMethodManager inputmanger;

	private static final String TAG = GroupAllMembersFragment.class.getCanonicalName();
	private int groupType = -1;
	private Group group = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inputmanger = (InputMethodManager) this.getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		groupType = getActivity().getIntent().getIntExtra("groupType", -1);
		group = (Group) getActivity().getIntent().getSerializableExtra("groupInfo");
		memberList = GroupAllMembersActivity.memberUserInfos;
		adapter = new GroupAllMembersAdapter(memberList, this.getActivity(), groupType, group.getCreaterId());
		ChatDetailActivity.groupAllMembersAdapter = adapter;

		searchMemberAdapter = new GroupAllMembersAdapter(searchResultList, this.getActivity(), groupType, group.getCreaterId());
		ChatDetailActivity.groupSearchResultAdapter = searchMemberAdapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.comm_fragment_group_all_member_list, null);

		initView(rootView);// 初始化view
		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		memberList.clear();
		memberList = null;
		searchResultList.clear();
		searchResultList = null;
		listView = null;
	}

	private void initView(View rootView) {
		listView = (ListView) rootView.findViewById(R.id.group_all_member_list);
		searchBar = (LinearLayout) rootView.findViewById(R.id.search_bar);
		searchClear = (ImageView) rootView.findViewById(R.id.search_clear);
		searchText = (EditText) rootView.findViewById(R.id.search_key);
		searchView = (LinearLayout) rootView.findViewById(R.id.search_view);
		searchMemberList = (ListView) rootView.findViewById(R.id.member_search_list);
		searchBar.setVisibility(View.VISIBLE);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				UserInfo userInfo = memberList.get(position);
				toWhoDetailInfoPage(getActivity(), userInfo.getId(), TAG);
			}

		});
		searchMemberList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UserInfo userInfo = searchResultList.get(position);
				toWhoDetailInfoPage(getActivity(), userInfo.getId(), TAG);
			}

		});
		searchClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchText.setText("");
				searchClear.setVisibility(View.INVISIBLE);
				inputmanger.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
				searchView.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
			}
		});
		final InputMethodManager inputMethodManager = (InputMethodManager) this
				.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		searchText.setOnKeyListener(new OnKeyListener() {// 输入完后按键盘上的搜索键

					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_ENTER
								&& event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
							String content = searchText.getText().toString();
							if (content != null && !"".equals(content)) {
								searchClear.setVisibility(View.VISIBLE);
								searchView.setVisibility(View.VISIBLE);
								listView.setVisibility(View.GONE);
								sortFreshData(content + "");
							} else {
								searchClear.setVisibility(View.INVISIBLE);
								searchView.setVisibility(View.GONE);
								listView.setVisibility(View.VISIBLE);
								inputmanger.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
							}
							inputMethodManager.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
						}
						return false;
					}
				});
		listView.setAdapter(adapter);
	}

	/**
	 * 点击某人头像跳转到某人详细资料页面
	 * @param context
	 * @param id
	 * @param tag
	 */
	public void toWhoDetailInfoPage(Activity context, String id, String tag) {
		if (groupType == CommConstants.CHAT_TYPE_GROUP_ANS) {
			ToastUtils.showToast(context,getString(R.string.can_not_see_user_detail));
			return;
		}
		UserDao dao = UserDao.getInstance(context);
		UserInfo userInfo = dao.getUserInfoById(id);
		if (userInfo == null) {
			ToastUtils.showToast(context, context.getResources().getString(R.string.toast_msg_not_findUser_str));
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putSerializable("userInfo", userInfo);
		Intent intent = new Intent(context, UserDetailActivity.class);
		intent.putExtras(bundle);
		context.startActivity(intent);

	}

	protected void sortFreshData(final String content) {
		searchResultList.clear();
		if (content != null && !"".equals(content.trim())) {
			//" where empAdname like ? or empCname like ? or fullNameSpell like
			//? or firstNameSpell like ? or phone like ? or mphone like ?
			for (int i = 0; i < memberList.size(); i++) {
				UserInfo userInfo = memberList.get(i);
				if (groupType == CommConstants.CHAT_TYPE_GROUP_ANS) {
					if (userInfo.getNickName() != null && userInfo.getNickName().contains(content)){
						searchResultList.add(userInfo);
					}
				} else {
					if ((userInfo.getEmpAdname() != null && userInfo.getEmpAdname().contains(content))
							|| (userInfo.getEmpCname() != null && userInfo.getEmpCname().contains(content))
							|| (userInfo.getFullNameSpell() != null && userInfo.getFullNameSpell().contains(content))
							|| (userInfo.getFirstNameSpell() != null && userInfo.getFirstNameSpell().contains(content))
							|| (userInfo.getPhone() != null && userInfo.getPhone().contains(content))
							|| (userInfo.getMphone() != null && userInfo.getMphone().contains(content))) {
						searchResultList.add(userInfo);
					}
				}
				userInfo = null;
			}
		} 
		searchMemberAdapter = new GroupAllMembersAdapter(searchResultList, this.getActivity(), groupType, group.getCreaterId());
		ChatDetailActivity.groupSearchResultAdapter = searchMemberAdapter;

		searchMemberList.setAdapter(searchMemberAdapter);
		searchMemberAdapter.notifyDataSetChanged();
	}
}
