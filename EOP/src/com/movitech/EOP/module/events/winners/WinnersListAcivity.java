package com.movitech.EOP.module.events.winners;

import android.os.Bundle;
import android.widget.TextView;

import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.events.base.EventBaseActivity;
import com.movitech.EOP.module.events.contants.EventsContants;
import com.movitech.EOP.module.events.manager.EventsManager;
import com.movitech.EOP.module.events.model.Awards;
import com.movitech.EOP.module.events.model.Winners;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/29.
 * <p/>
 * 中奖名单列表界面
 */
public class WinnersListAcivity extends EventBaseActivity {

    private TextView tv_noData;
    //奖项
    private List<Winners> winnersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化页面
        initContentView();

        tv_noData = (TextView)findViewById(R.id.tv_noData);

        Awards awards = (Awards)getIntent().getSerializableExtra(EventsContants.INTENT_AWARD);
        tv_topbar_center.setText(awards.getAwardsName());

        TextView tv_name = (TextView)findViewById(R.id.tv_name);
        tv_name.setText(getResources().getString(R.string.lucky_name));
        tv_name.setTextColor(getResources().getColor(R.color.red));

        TextView tv_phone = (TextView)findViewById(R.id.tv_phone);
        tv_phone.setText(getResources().getString(R.string.phone_num));
        tv_phone.setTextColor(getResources().getColor(R.color.red));

        TextView tv_dep = (TextView)findViewById(R.id.tv_department);
        tv_dep.setText(getResources().getString(R.string.from));
        tv_dep.setTextColor(getResources().getColor(R.color.red));

        progressDialogUtil.showLoadingDialog(this, getResources().getString(R.string.loading), false);

        //发起请求:获取获奖人员列表
        EventsManager.getWinnersList(awards.getId(), new StringCallback() {

            @Override
            public void onError(Call call, Exception e) {
                progressDialogUtil.dismiss();
                ToastUtils.showToastBottom(WinnersListAcivity.this, getString(R.string.winner_list_error));
            }

            @Override
            public void onResponse(String response) {
                progressDialogUtil.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("ok")) {

                        winnersList = new ArrayList<Winners>();
                        JSONArray jsonArray = jsonObject.getJSONArray("objValue");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Winners winners = new Winners();
                            winners.setName(jsonArray.getJSONObject(i).getString("name"));

                            String telStr = jsonArray.getJSONObject(i).getString("tel");
                            if (StringUtils.notEmpty(telStr)) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(telStr.substring(0, 3)).append("****").append(telStr.substring(7));
                                winners.setPhone(sb.toString());
                            }

                            winners.setLoginName(jsonArray.getJSONObject(i).getString("loginName"));
                            winners.setDepartment(jsonArray.getJSONObject(i).getString("officeName"));

                            winnersList.add(winners);
                        }

                        //填充页面内容
                        setContentValue();
                    } else {
                        lv_awards.setEmptyView(tv_noData);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_winners;
    }

    @Override
    protected void setTopBarValue() {
        tv_topbar_center.setText("");
    }

    @Override
    protected void setContentValue() {
        lv_awards.setAdapter(new WinnersListAdapter(this, winnersList));
    }

    @Override
    protected void onItemClickListener(int position) {

    }

}
