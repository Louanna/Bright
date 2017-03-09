package com.movitech.EOP.module.mine.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.Obj2JsonUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.EOPBaseActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.MediaType;

public class RePasswordActivity extends EOPBaseActivity {

	private TextView title;
	private TextView commenLeft;
	private TextView commenRight;
	private EditText oldPassword;
	private EditText newPassword;
	private EditText confirmPassword;

	Pattern pattern = Pattern.compile("^[A-Za-z0-9_]{6,20}$");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repassword);
		initView();
	}

	private void initView() {
		title = (TextView) findViewById(R.id.common_top_title);
		commenLeft = (TextView) findViewById(R.id.common_top_left);
		commenRight = (TextView) findViewById(R.id.tv_common_top_right);
		oldPassword = (EditText) findViewById(R.id.old_password);
		newPassword = (EditText) findViewById(R.id.new_password);
		confirmPassword = (EditText) findViewById(R.id.confirm_password);
		title.setText(getString(R.string.user_setting_info_password));
		commenLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		commenRight.setText(getString(R.string.save));
		commenRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String old = oldPassword.getText().toString();
				String newStr = newPassword.getText().toString();
				String confirm = confirmPassword.getText().toString();
				String password = MFSPHelper.getString(CommConstants.PASSWORD);
				if (old.equals(password)) {
					if (newStr.equals(confirm)) {
						Matcher matcher = pattern.matcher(newStr);
						if (matcher.matches()) {
							updateUserPassword(newStr);
						} else {
							EOPApplication.showToast(context,
									getString(R.string.password_rule));
						}
					} else {
						EOPApplication.showToast(context, getString(R.string.password_different));
					}
				} else {
					EOPApplication.showToast(context, getString(R.string.password_error));
				}
			}
		});
	}

	public void updateUserPassword(final String newPassWord) {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("id", MFSPHelper.getString(CommConstants.USERID));
		paramsMap.put("empAdpassword", newPassWord);
		String json = Obj2JsonUtils.map2json(paramsMap);
		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		OkHttpUtils.postStringWithToken()
				.url(CommConstants.URL_UPDATE_PASSWORD)
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
				progressDialogUtil.dismiss();
				EOPApplication.showToast(context, getString(R.string.password_modify_success));
				MFSPHelper.setString(CommConstants.PASSWORD, newPassword.getText()
						.toString());
				onBackPressed();
				break;
			case 2:
				progressDialogUtil.dismiss();
				EOPApplication.showToast(context, getString(R.string.password_modify_error));
				break;
			default:
				break;
			}
		}
	};
}
