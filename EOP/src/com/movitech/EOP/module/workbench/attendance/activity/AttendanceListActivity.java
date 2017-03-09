package com.movitech.EOP.module.workbench.attendance.activity;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.workbench.activity.WebViewActivity;
import com.movitech.EOP.module.workbench.attendance.adapter.AttendanceListAdapter;
import com.movitech.EOP.module.workbench.attendance.model.Attendance;
import com.movitech.EOP.utils.Json2ObjUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;

public class AttendanceListActivity extends EOPBaseActivity {

	ListView listView;
	TextView title;
	ImageView topLeft;
	ImageView topRight;
	ImageView noAttendance;
	AttendanceListAdapter adapter;
	List<Attendance> mData = new ArrayList<Attendance>();
	Button go1;
	Button go2;
	LinearLayout goLayout;

	private int mYear = -1;
	private int mMonth = -1; // 0 ~ 11
	private int mDay = -1;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				startActivityForResult(new Intent(context,
						AttendanceCreateActivity.class), 1);
				break;
			case 2:
				String day = (String) msg.obj;
				setAdapter(day);
				break;
			case 3:
				String[] arr = (String[]) msg.obj;
				String urlString = "";
				if (arr[1].contains("?")) {
					urlString = arr[1] + "&ticket=" + arr[0];
				} else {
					urlString = arr[1] + "?ticket=" + arr[0];
				}
				Intent intent = new Intent(context, WebViewActivity.class);
				intent.putExtra("URL", urlString);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attendance);
		iniView();
		getListByDay(DateUtils.getCurDateStr("yyyy-MM-dd"));
	}

	private void iniView() {
		listView = (ListView) findViewById(R.id.ea_listview);
		title = (TextView) findViewById(R.id.common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_left);
		topRight = (ImageView) findViewById(R.id.common_top_right);
		noAttendance = (ImageView) findViewById(R.id.noAttendance);
		go1 = (Button) findViewById(R.id.ea_btn_go1);
		go2 = (Button) findViewById(R.id.ea_btn_go2);
		goLayout = (LinearLayout) findViewById(R.id.ea_btn_go_layout);
		title.setText("我的考勤");
		topRight.setImageResource(R.drawable.ico_calendar);
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		// 设置为当前日期
		final Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		topRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MyDatePickerDialog picker = new MyDatePickerDialog(
						AttendanceListActivity.this, new OnDateSetListener() {

							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								Calendar calendar = Calendar.getInstance();
								calendar.set(year, monthOfYear, dayOfMonth);
								getListByDay(DateUtils.date2Str(calendar,
										"yyyy-MM-dd"));
							}
						}, mYear, mMonth, mDay);

				picker.show();
			}
		});

		boolean isCas = false;
		try {
			// cas认证
			isCas = getPackageManager().getApplicationInfo(getPackageName(),
					PackageManager.GET_META_DATA).metaData
					.getBoolean("CHANNEL_CAS");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (!isCas) {
			goLayout.setVisibility(View.GONE);
		}

		go1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url1 = "";
				String ticket = MFSPHelper.getString("ticket");
				boolean isCas = false;
				try {
					// cas认证
					isCas = getPackageManager().getApplicationInfo(
							getPackageName(), PackageManager.GET_META_DATA).metaData
							.getBoolean("CHANNEL_CAS");
					url1 = getPackageManager().getApplicationInfo(
							getPackageName(), PackageManager.GET_META_DATA).metaData
							.getString("CHANNEL_EA_URL1");
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				if (isCas) {
					casTickets(ticket, url1);
				}
			}
		});
		go2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String url2 = "";
				String ticket = MFSPHelper.getString("ticket");
				boolean isCas = false;
				try {
					// cas认证
					isCas = getPackageManager().getApplicationInfo(
							getPackageName(), PackageManager.GET_META_DATA).metaData
							.getBoolean("CHANNEL_CAS");
					url2 = getPackageManager().getApplicationInfo(
							getPackageName(), PackageManager.GET_META_DATA).metaData
							.getString("CHANNEL_EA_URL2");
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				if (isCas) {
					casTickets(ticket, url2);
				}
			}
		});
	}

	public void casTickets(final String value, final String url) {

		JSONObject paramsObj = new JSONObject();
		paramsObj.put("service", url);
		HttpManager.postJson(value, paramsObj.toString(), new StringCallback() {
			@Override
			public void onError(Call call, Exception e) {

			}

			@Override
			public void onResponse(String response) throws JSONException {
				handler.obtainMessage(3, new String[] { response, url })
						.sendToTarget();
			}
		});
	}

	class MyDatePickerDialog extends DatePickerDialog {

		public MyDatePickerDialog(Context context, OnDateSetListener callBack,
				int year, int monthOfYear, int dayOfMonth) {
			super(context, callBack, year, monthOfYear, dayOfMonth);
		}

		@Override
		protected void onStop() {
			// super.onStop();
		}
	}

	private void getListByDay(final String day) {
		mData.clear();

        String url = CommConstants.URL_EOP_ATTENDANCE + "getbyday?userId=" + MFSPHelper.getString(CommConstants.USERID)
                + "&currentTime=" + day;;
        OkHttpUtils
                .postWithToken()
                .url(url)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e) {
                        handler.obtainMessage(2, day).sendToTarget();
                    }

                    @Override
                    public void onResponse(final String response) {
                        try {
                            mData = Json2ObjUtils.getAttendanceListData(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        handler.obtainMessage(2, day).sendToTarget();
                    }
                });
	}

	private void setAdapter(String time) {
		noAttendance.setVisibility(View.GONE);
		Attendance attendance = new Attendance();
		attendance.setTime(time);
		mData.add(0, attendance);
		adapter = new AttendanceListAdapter(context, mData, listView, handler);
		listView.setAdapter(adapter);
		if (mData.size() == 1) {
			noAttendance.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 1) {
			if (arg1 == 1) {
				getListByDay(DateUtils.getCurDateStr("yyyy-MM-dd"));
			}
		}
	}

}
