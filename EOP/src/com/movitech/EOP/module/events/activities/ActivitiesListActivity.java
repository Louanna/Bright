package com.movitech.EOP.module.events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ToastUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.events.awards.AwardsListActivity;
import com.movitech.EOP.module.events.base.EventBaseActivity;
import com.movitech.EOP.module.events.contants.EventsContants;
import com.movitech.EOP.module.events.manager.EventsManager;
import com.movitech.EOP.module.events.model.Activities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/29.
 * <p/>
 * 活动列表界面
 */
public class ActivitiesListActivity extends EventBaseActivity {

    private List<Activities> activitiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化页面
        initContentView();

        progressDialogUtil.showLoadingDialog(this, getResources().getString(R.string.loading), false);

        //发起请求:获取活动列表
        EventsManager.getActivitiesListByUserId(MFSPHelper.getString(CommConstants.USERID), new StringCallback() {

            @Override
            public void onError(Call call, Exception e) {
                progressDialogUtil.dismiss();
                ToastUtils.showToastBottom(ActivitiesListActivity.this, getString(R.string.activities_list_error));
            }

            @Override
            public void onResponse(String response) {

                progressDialogUtil.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("ok")) {
                        activitiesList = new ArrayList<Activities>();
                        JSONArray jsonArray = jsonObject.getJSONArray("objValue");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Activities activities = new Activities();
                            activities.setId(jsonArray.getJSONObject(i).getString("id"));
                            activities.setName(jsonArray.getJSONObject(i).getString("name"));
                            activitiesList.add(activities);
                        }
                        //填充页面内容
                        setContentValue();
                    } else {
                        Toast.makeText(ActivitiesListActivity.this, getString(R.string.activities_list_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_activities_awards;
    }

    @Override
    protected void setTopBarValue() {
        tv_topbar_center.setText(getResources().getString(R.string.participant_activities));
    }

    @Override
    protected void setContentValue() {
        lv_awards.setAdapter(new ActivitiesListAdapter(this, activitiesList));
    }

    @Override
    protected void onItemClickListener(int position) {
        Intent intent = new Intent(this, AwardsListActivity.class);
        intent.putExtra(EventsContants.INTENT_ACTIVITY, activitiesList.get(position));
        startActivity(intent);
    }

}
