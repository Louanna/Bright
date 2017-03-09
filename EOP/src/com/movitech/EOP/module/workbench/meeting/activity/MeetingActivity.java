package com.movitech.EOP.module.workbench.meeting.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.workbench.meeting.adapter.MeetingAdapter;
import com.movitech.EOP.module.workbench.meeting.model.Meeting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class MeetingActivity extends EOPBaseActivity {
    private ListView metting;
    private List<Meeting> nowMettings = new ArrayList<Meeting>();
    private List<Meeting> sevenMettings = new ArrayList<Meeting>();
    private List<Object> allMettings = new ArrayList<Object>();
    private MeetingAdapter mettingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        mettingAdapter = new MeetingAdapter(MeetingActivity.this, null);
        TextView title = (TextView) findViewById(R.id.common_top_title);
        ImageView topLeft = (ImageView) findViewById(R.id.common_top_left);
        ImageView topRight = (ImageView) findViewById(R.id.common_top_right);
        metting = (ListView) findViewById(R.id.metting);

        metting.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Meeting meeting = null;
                for (int i = 0; i < allMettings.size(); i++) {
                    Object object = allMettings.get(i);
                    if (object instanceof Meeting) {
                        meeting = (Meeting) object;
                        if (i == position) {
                            meeting.setShowing(!meeting.isShowing());
                        } else {
                            meeting.setShowing(false);
                        }
                    }

                }
                mettingAdapter.setMettings(allMettings);
                mettingAdapter.notifyDataSetChanged();
            }

        });
        topRight.setVisibility(View.GONE);
        title.setText(getString(R.string.meeting));
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getMettings();
    }

    AsyncTask<Void, Void, String> meetingTask = null;

    private void getMettings() {
        if (meetingTask != null) {
            meetingTask.cancel(true);
            meetingTask = null;
        }
        meetingTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialogUtil.showLoadingDialog(MeetingActivity.this,
                        getString(R.string.waiting), false);
            }

            @Override
            protected String doInBackground(Void... params) {
                long begin = System.currentTimeMillis();

                String nowMeetingUrl = CommConstants.URL + "meet/getnowmeeting?userid=" + CommConstants.loginConfig.getmUserInfo().getEmpAdname();
                HttpManager.postJson(nowMeetingUrl, "", new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        praseMetting(response, nowMettings);
                    }
                });

                String sevenMeetingUrl = CommConstants.URL + "meet/getsevenmeeting?userid=" + CommConstants.loginConfig.getmUserInfo().getEmpAdname();
                HttpManager.postJson(sevenMeetingUrl, "", new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        praseMetting(response, sevenMettings);
                    }
                });

//				String response1 = HttpClientUtils.post(CommConstants.URL+"meet/getnowmeeting?userid="+ CommConstants.loginConfig.getmUserInfo().getEmpAdname(), "", Charset.forName("UTF-8"));
//				String response2 = HttpClientUtils.post(CommConstants.URL+"meet/getsevenmeeting?userid="+ CommConstants.loginConfig.getmUserInfo().getEmpAdname(), "", Charset.forName("UTF-8"));
                long end = System.currentTimeMillis();
                System.out.println("获取会议时间差(毫秒):" + (end - begin) + "");
//				praseMetting(response1, nowMettings);
//				praseMetting(response2, sevenMettings);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                progressDialogUtil.dismiss();
                allMettings.add("今日会议");
                allMettings.addAll(nowMettings);
                allMettings.add("待开会议");
                allMettings.addAll(sevenMettings);
                mettingAdapter.setMettings(allMettings);
                metting.setAdapter(mettingAdapter);
                mettingAdapter.notifyDataSetChanged();
            }

        };
        meetingTask.execute(null, null, null);
    }

    private void praseMetting(String response, List<Meeting> mettings) {
        try {
            JSONObject jObject = new JSONObject(response);
            if (mettings != null) {
                mettings.clear();
            }
            if (jObject.has("ok")) {
                boolean ok = jObject.getBoolean("ok");
                if (ok) {
                    if (jObject.has("objValue")) {
                        JSONArray jaArray = jObject.getJSONArray("objValue");
                        for (int i = 0; i < jaArray.length(); i++) {
                            JSONObject jObject2 = jaArray.getJSONObject(i);
                            Meeting metting = new Meeting();
                            if (jObject2.has("id")) {
                                metting.setId(jObject2.getInt("id"));
                            }
                            if (jObject2.has("meetingTitle")) {
                                metting.setMettingTitle(jObject2.getString("meetingTitle"));
                            }
                            if (jObject2.has("meetingDate")) {
                                metting.setMettingDate(jObject2.getString("meetingDate"));
                            }
                            if (jObject2.has("meetingbeginDate")) {
                                metting.setMeetingbeginDate(jObject2.getString("meetingbeginDate"));
                            }
                            if (jObject2.has("meetingendDate")) {
                                metting.setMeetingendDate(jObject2.getString("meetingendDate"));
                            }
                            if (jObject2.has("meetingUser")) {
                                metting.setMeetingUser(jObject2.getString("meetingUser"));
                            }
                            if (jObject2.has("meetingCompany")) {
                                metting.setMeetingCompany(jObject2.getString("meetingCompany"));
                            }
                            if (jObject2.has("meetingroom")) {
                                metting.setMeetingroom(jObject2.getString("meetingroom"));
                            }
                            mettings.add(metting);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (meetingTask != null) {
            meetingTask.cancel(true);
            meetingTask = null;
        }
    }
}
