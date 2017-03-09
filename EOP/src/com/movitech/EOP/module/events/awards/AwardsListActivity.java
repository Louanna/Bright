package com.movitech.EOP.module.events.awards;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ToastUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.events.base.EventBaseActivity;
import com.movitech.EOP.module.events.contants.EventsContants;
import com.movitech.EOP.module.events.manager.EventsManager;
import com.movitech.EOP.module.events.model.Activities;
import com.movitech.EOP.module.events.model.Awards;
import com.movitech.EOP.module.events.winners.WinnersListAcivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/29.
 *
 * 活动详情界面（显示奖项列表）
 */
public class AwardsListActivity extends EventBaseActivity {

    //奖项
    private List<Awards> awardsList;
    private Activities activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化页面
        initContentView();

        //设置标题
        activities = (Activities)getIntent().getSerializableExtra(EventsContants.INTENT_ACTIVITY);
        tv_topbar_center.setText(activities.getName());

        progressDialogUtil.showLoadingDialog(this, getResources().getString(R.string.loading), false);

        //发起请求:获取活动奖项列表
        EventsManager.getAwardsList(MFSPHelper.getString(CommConstants.USERID), activities.getId(), new StringCallback() {

            @Override
            public void onError(Call call, Exception e) {
                progressDialogUtil.dismiss();
                ToastUtils.showToastBottom(AwardsListActivity.this, getString(R.string.awards_list_error));
            }

            @Override
            public void onResponse(String response) {
                progressDialogUtil.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("ok")) {
                        awardsList = new ArrayList<Awards>();
                        JSONArray jsonArray = jsonObject.getJSONArray("objValue");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Awards awards = new Awards();
                            awards.setId(jsonArray.getJSONObject(i).getString("id"));
                            awards.setAwardsName(jsonArray.getJSONObject(i).getString("name"));
                            awards.setPrizeName(jsonArray.getJSONObject(i).getString("prizeName"));
                            awards.setNum(jsonArray.getJSONObject(i).getString("prizeCount"));
                            awards.setStatus(jsonArray.getJSONObject(i).getString("isLottery"));
                            awardsList.add(awards);
                        }
                        //填充页面内容
                        setContentValue();
                    } else {
                        Toast.makeText(AwardsListActivity.this, getString(R.string.activities_list_error), Toast.LENGTH_SHORT).show();
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
        tv_topbar_center.setText("");
    }

    @Override
    protected void setContentValue() {
        lv_awards.setAdapter(new AwardsListAdapter(this,awardsList));
    }

    @Override
    protected void onItemClickListener(int position) {

        Intent intent = new Intent(this, WinnersListAcivity.class);
        intent.putExtra(EventsContants.INTENT_AWARD, awardsList.get(position));
        startActivity(intent);
    }

}
