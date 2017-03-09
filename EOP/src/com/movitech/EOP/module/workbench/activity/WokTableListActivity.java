package com.movitech.EOP.module.workbench.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.module.workbench.adapter.ExpandableForWorkTableAdapter;
import com.movitech.EOP.module.workbench.constants.Constants;
import com.movitech.EOP.module.workbench.manager.WorkTableManage;
import com.movitech.EOP.module.workbench.model.WorkTable;

public class WokTableListActivity extends EOPBaseActivity {

    TextView mTopTitle;
    ImageView mTopLeftImage;
    TextView topRight;

    private ExpandableListView mListView;
    ExpandableForWorkTableAdapter expandAdapter;
    // 存放父列表数据
    List<Map<String, Object>> groupData = new ArrayList<Map<String, Object>>();
    // 放子列表列表数据
    List<List<Map<String, Object>>> childData = new ArrayList<List<Map<String, Object>>>();

    ArrayList<WorkTable> datas;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case Constants.UPDATEMODULES_RESULT:
                    try {
                        progressDialogUtil.dismiss();
                        String result = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(result);
                        boolean ok = jsonObject.getBoolean("ok");
                        if (ok) {
                            EOPApplication.showToast(context, getString(R.string.save_succeed));
                            Intent data = new Intent();
                            data.putExtra("allWorkTables", datas);

                            String spjson = com.alibaba.fastjson.JSONObject
                                    .toJSONString(datas);
                            MFSPHelper.setString("allWorkTables", spjson);
                            setResult(1, data);
                            finish();
                        } else {
                            EOPApplication.showToast(context, getString(R.string.save_failed));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constants.MODULE_ERROR:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(context, getString(R.string.get_messages_error));
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_tables);

        initView();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupData.clear();
        childData.clear();
    }

    private void initView() {
        mTopTitle = (TextView) findViewById(R.id.common_top_title);
        mTopLeftImage = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        mListView = (ExpandableListView) findViewById(R.id.worktable_list);

        topRight.setText(getString(R.string.save));
        topRight.setTextColor(getResources().getColor(R.color.white));
        mTopTitle.setText(getString(R.string.config_worktable));
        mTopLeftImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDialogUtil.showLoadingDialog(WokTableListActivity.this,
                        getString(R.string.saving), false);
                String moduleIds = "";
                for (int i = 0; i < datas.size(); i++) {
                    WorkTable workTable = datas.get(i);
                    if ("show".equals(workTable.getDisplay())) {
                        System.out.println(workTable.getName());
                        moduleIds += workTable.getId() + ",";
                    }
                }
                WorkTableManage manage = new WorkTableManage(context);
                manage.updateModules(handler, moduleIds);
            }
        });

    }

    private void initData() {
        setAdapter();
    }

    private void setAdapter() {
        initListData();
        progressDialogUtil.dismiss();

        expandAdapter = new ExpandableForWorkTableAdapter(groupData, childData,
                context);
        mListView.setAdapter(expandAdapter);

        for (int i = 0; i < groupData.size(); i++) {
            mListView.expandGroup(i);
        }
        mListView.setOnGroupClickListener(new OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                return true;
            }
        });
    }

    private void initListData() {
        groupData.clear();
        childData.clear();
        if (datas.isEmpty()) {
            return;
        }
        // 已排序，分组
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", getString(R.string.useful));
        groupData.add(map);
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("status", getString(R.string.unUseful));
        groupData.add(map2);

        List<Map<String, Object>> childList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < datas.size(); i++) {
            WorkTable workTable = datas.get(i);
            if (workTable.getStatus().equals("available")) {
                Map<String, Object> childMap = new HashMap<String, Object>();
                childMap.put("worktable", workTable);
                childList.add(childMap);
            }
        }
        childData.add(childList);
        List<Map<String, Object>> childList2 = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < datas.size(); i++) {
            WorkTable workTable = datas.get(i);
            if (workTable.getStatus().equals("unavailable")) {
                Map<String, Object> childMap = new HashMap<String, Object>();
                childMap.put("worktable", workTable);
                childList2.add(childMap);
            }
        }
        childData.add(childList2);
    }

}
