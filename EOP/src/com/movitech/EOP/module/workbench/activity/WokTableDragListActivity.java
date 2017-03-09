package com.movitech.EOP.module.workbench.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.module.workbench.adapter.DragAdapter;
import com.movitech.EOP.module.workbench.manager.WorkTableClickDelagate;
import com.movitech.EOP.module.workbench.model.WorkTable;
import com.movitech.EOP.view.DragGridViewPage;

import java.util.ArrayList;
import java.util.List;

public class WokTableDragListActivity extends EOPBaseActivity {

    TextView mTopTitle;
    ImageView mTopLeftImage;
    ImageView topRight;

    List<WorkTable> datas = new ArrayList<>();
    DragGridViewPage dragGridViewPage;
    DragAdapter dragAdapter;
    boolean isAdd = false;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 33:// model add
                    WorkTable table = (WorkTable) msg.obj;
                    // 保存本地
                    String otherjson = JSONArray.toJSONString(datas);
                    MFSPHelper.setString("otherWorkTables", otherjson);

                    Intent intent = new Intent();
//                    setResult(1,intent);
                    intent.setAction("table");
                    intent.putExtra("table",table);
                    BaseApplication.getInstance().sendBroadcast(intent);


//                    WorkTable more = new WorkTable();
//                    more.setId("more");
//                    HomeFragemnt.myWorkTables.remove(more);
//                    HomeFragemnt.myWorkTables.add(table);
//                    String myjson = JSONArray
//                            .toJSONString(HomeFragemnt.myWorkTables);
//                    MFSPHelper.setString("myWorkTables", myjson);
//                    HomeFragemnt.myWorkTables.add(more);
                    isAdd = true;
                    break;
                case 34:
                    final String myjsons = JSONArray.toJSONString(datas);
                    MFSPHelper.setString("otherWorkTables", myjsons);
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
        setContentView(R.layout.activity_worktable_list);

        initView();
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mTopTitle = (TextView) findViewById(R.id.common_top_title);
        mTopLeftImage = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        dragGridViewPage = (DragGridViewPage) findViewById(R.id.dragGridView);

        topRight.setVisibility(View.GONE);
        mTopTitle.setText(getString(R.string.more));
        mTopLeftImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isAdd) {
            //setResult(1);
        }
        super.onBackPressed();
    }

    private void initData() {
        datas = (List<WorkTable>)getIntent().getSerializableExtra("otherTable");
        setAdapter();
    }

    private void setAdapter() {
        progressDialogUtil.dismiss();

        dragGridViewPage.setCustomerEnable(false);
        dragAdapter = new DragAdapter(context, datas, dragGridViewPage,
                handler, "add");
        dragGridViewPage.setAdapter(dragAdapter, false);
        dragGridViewPage.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == -1) {
                    return;
                }
                if (dragAdapter.isShowDel()) {
                    dragAdapter.clearDrag();
                } else {
                    WorkTableClickDelagate clickDelagate = new WorkTableClickDelagate(context);
                    clickDelagate.onClickWorkTable(datas, position);
                }
            }
        });
    }
}
