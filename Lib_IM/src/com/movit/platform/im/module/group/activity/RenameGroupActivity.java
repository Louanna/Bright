package com.movit.platform.im.module.group.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.entities.Group;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.MediaType;

public class RenameGroupActivity extends IMBaseActivity {

	private TextView title;
	private TextView commenLeft;
	private TextView commenRight;
	private EditText renameRoom;
	private Group group;
	private String newName;
	private String groupName;
	String type;
	String text;
	Pattern pattern_m = Pattern
			.compile("^((13[0-9])|(15[^4,\\D])|(18[0,0-9])|(17[0-9]))\\d{8}$");
	Pattern pattern = Pattern
			.compile("^0(10|2[0-5789]|\\d{3})[-]{0,1}\\d{7,8}$");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.im_activity_group_rename);
		group = (Group) getIntent().getSerializableExtra("group_object");
		type = getIntent().getStringExtra("type");
		text = getIntent().getStringExtra("text");
		groupName = getIntent().getStringExtra("group_name");
		
		initView();
	}

	private void initView() {
		title = (TextView) findViewById(R.id.common_top_title);
		commenLeft = (TextView) findViewById(R.id.common_top_left);
		commenRight = (TextView) findViewById(R.id.tv_common_top_right);
		renameRoom = (EditText) findViewById(R.id.rename_room);

		if (type.equals("mphone")) {
			title.setText(getString(R.string.phone));
			renameRoom.setText(text);
			renameRoom.setInputType(InputType.TYPE_CLASS_PHONE);
		} else if (type.equals("phone")) {
			title.setText(getString(R.string.telephone));
			renameRoom.setText(text);
			renameRoom.setInputType(InputType.TYPE_CLASS_PHONE);
		} else if (type.equals("group")) {
			title.setText(getString(R.string.group_name));
			renameRoom.setText(group.getDisplayName());
		}
		commenLeft.setText(getString(R.string.cancel));
		commenRight.setText(getString(R.string.save));

		commenLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		commenRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				newName = renameRoom.getText().toString();
				if (StringUtils.notEmpty(newName)) {
					if (type.equals("mphone")) {
						Matcher matcher = pattern_m.matcher(newName);
						if (matcher.matches()) {
							DialogUtils.getInstants().showLoadingDialog(
									RenameGroupActivity.this, getString(R.string.saving), false);
							updateUserPhone(newName, type);
						} else {
							ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.please_input_right_phone_number));
							return;
						}

					} else if (type.equals("phone")) {
						Matcher matcher = pattern.matcher(newName);
						if (matcher.matches()) {
							DialogUtils.getInstants().showLoadingDialog(
									RenameGroupActivity.this, getString(R.string.saving), false);
							updateUserPhone(newName, type);
						} else {
							ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.please_input_right_telphone_number));
							return;
						}
					} else if (type.equals("group")) {
						rename(newName);
					}
				} else {
					if (type.equals("mphone")) {
						ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.please_input_right_phone_number));
					} else if (type.equals("phone")) {
						ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.please_input_right_telphone_number));
					} else if (type.equals("group")) {
						ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.group_name_empty));
					}
				}
			}
		});
	}

	AsyncTask<Void, Void, String> renameTask = null;

	private void rename(final String newName) {

		DialogUtils.getInstants().showLoadingDialog(RenameGroupActivity.this,
				getString(R.string.modify_group_name), false);

		OkHttpUtils.postWithToken()
				.url(CommConstants.URL_EDIT_NAME)
				.addParams("userId", MFSPHelper.getString(CommConstants.USERID))
				.addParams("groupId", group.getId())
				.addParams("newName", newName)
				.addHeader("Content-Type","application/x-www-form-urlencoded")
				.build()
				.execute(new StringCallback() {
					@Override
					public void onError(Call call, Exception e) {
						DialogUtils.getInstants().dismiss();
						ToastUtils.showToast(RenameGroupActivity.this, "修改群名称失败");
					}

					@Override
					public void onResponse(String result) throws JSONException {
						boolean status = false;
						if (result == null) {
							return;
						}
						try {
							JSONObject jo = new JSONObject(result);
							status = jo.getBoolean("ok");
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (status) {
							//TODO anna
//					onBackPressed();

							RenameGroupActivity.this.setResult(Activity.RESULT_OK,new Intent().putExtra(IMConstants.KEY_GROUP_NAME,newName));
							RenameGroupActivity.this.finish();
						} else {
							ToastUtils.showToast(RenameGroupActivity.this, "修改群名称失败");
						}
						DialogUtils.getInstants().dismiss();
					}
				});
	}

	public void updateUserPhone(final String newPhone, final String type) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("id", MFSPHelper.getString(CommConstants.USERID));
		if (type.equals("mphone")) {
			paramsMap.put("mphone", newPhone);
		} else if (type.equals("phone")) {
			paramsMap.put("phone", newPhone);
		}
		String json = Obj2JsonUtils.map2json(paramsMap);
		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		OkHttpUtils.postStringWithToken()
				.url(CommConstants.URL_UPDATE_USER_INFO)
				.content(json)
				.mediaType(JSON)
				.build()
				.execute(new StringCallback() {
					@Override
					public void onError(Call call, Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(2);
					}

					@Override
					public void onResponse(String response) throws JSONException {
						JSONObject object = new JSONObject(response);
						boolean ok = object.getBoolean("ok");
						if (ok) {
							handler.sendEmptyMessage(1);
						} else {
							handler.sendEmptyMessage(2);
						}
					}
				});
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				DialogUtils.getInstants().dismiss();
				ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.save_succeed));
				if (type.equals("mphone")) {
					setResult(1, new Intent().putExtra("mphone", renameRoom
							.getText().toString()));
					CommConstants.loginConfig.getmUserInfo()
							.setMphone(renameRoom.getText().toString());
				} else if (type.equals("phone")) {
					setResult(2, new Intent().putExtra("phone", renameRoom
							.getText().toString()));
					CommConstants.loginConfig.getmUserInfo()
							.setPhone(renameRoom.getText().toString());
				}
				onBackPressed();
				break;
			case 2:
				DialogUtils.getInstants().dismiss();
				ToastUtils.showToast(RenameGroupActivity.this, getString(R.string.save_failed));
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (renameTask != null) {
			renameTask.cancel(true);
			renameTask = null;
		}
	}

	@Override
	public void updateGroup() {
		Group group = IMConstants.groupsMap.get(groupName);
		renameRoom.setText(group.getDisplayName());
	}

	@Override
	public void afterGroupNameChanged(String roomName, String displayName) {
		Group group = IMConstants.groupsMap.get(groupName);
		renameRoom.setText(group.getDisplayName());
	}

}
