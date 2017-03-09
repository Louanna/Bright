package com.movitech.EOP.module.workbench.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.activity.MainActivity;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.workbench.adapter.WorkTableAdapter;
import com.movitech.EOP.module.workbench.model.WorkTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by air on 16/9/22.
 *
 */
public class WorkTableActivity extends EOPBaseActivity {

    private static final String NEWS = "News";//公司新闻
    private static final String WORK = "Work";//党建工作
    private static final String NOTICE = "Notice";//通知公告
    private static final String ZHILI = "Company";//公司治理
    private static final String FAWEN = "Files";//公司发文
    private static final String FLOW = "Flow";//行政流程
    private static final String TASK = "Task";//日程安排

    private ImageView ivBack;
    private TextView topTitle;
    private GridView gvWorkTable;

    private ArrayList<WorkTable> workTables;
    private WorkTableAdapter workTableAdapter;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_table);
        findViews();
        initData();
        setListener();

        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(MainActivity.UNREAD_PROCESS);
        registerReceiver(UnreadNumReceiver, intentFilter2);
    }

    private void findViews(){
        ivBack = (ImageView) findViewById(R.id.common_top_left);
        topTitle = (TextView) findViewById(R.id.common_top_title);
        gvWorkTable = (GridView) findViewById(R.id.gv_work_table);
    }

    @SuppressWarnings("unchecked")
    private void initData(){
        userInfo = CommConstants.loginConfig.getmUserInfo();
        String title = getIntent().getStringExtra("topTitle");
        topTitle.setText(title);
        workTables = (ArrayList<WorkTable>) getIntent().getSerializableExtra("workTable");
        workTableAdapter = new WorkTableAdapter(workTables,WorkTableActivity.this);

        Map<String, Integer> unreadNum = new HashMap<>();
        unreadNum.put("Flow",CommConstants.OAUNREADNUM);
        workTableAdapter.setUnreadNum(unreadNum);

        gvWorkTable.setAdapter(workTableAdapter);
    }

    private final BroadcastReceiver UnreadNumReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.UNREAD_PROCESS)) {
                if (workTableAdapter != null) {

                    Map<String, Integer> unreadNum = new HashMap<>();
                    unreadNum.put("Flow",intent.getIntExtra("oaUnreadNum",0));

                    workTableAdapter.setUnreadNum(unreadNum);
                    workTableAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void setListener(){
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        gvWorkTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WorkTable workTable = workTables.get(position);
                switch (workTable.getAndroid_access_url()){
                    case NEWS:
                        Intent newsIntent = new Intent(context,CompanyNewsActivity.class);
                        newsIntent.putExtra("topTitle",workTable.getName());
                        newsIntent.putExtra("code","xw");
                        startActivity(newsIntent);
                        break;
                    case NOTICE:
                        Intent noticeIntent = new Intent(context,CompanyNewsActivity.class);
                        noticeIntent.putExtra("topTitle",workTable.getName());
                        noticeIntent.putExtra("code","tzgg");
                        startActivity(noticeIntent);
                        break;
                    case WORK:
                        Intent workIntent = new Intent(context,CompanyNewsActivity.class);
                        workIntent.putExtra("topTitle",workTable.getName());
                        workIntent.putExtra("code","djgz");
                        startActivity(workIntent);
                        break;
                    case ZHILI:
                        Intent intent = new Intent(context,CompanyNewsActivity.class);
                        intent.putExtra("topTitle",workTable.getName());
                        intent.putExtra("code","gsgg");
                        startActivity(intent);
                        break;
                    case FAWEN:
                        Intent fawenIntent = new Intent(context,CompanyNewsActivity.class);
                        fawenIntent.putExtra("topTitle",workTable.getName());
                        fawenIntent.putExtra("code","gsfw");
                        startActivity(fawenIntent);
                        break;
                    case FLOW:
                        Intent intent2 = new Intent(context,ProcessActivity.class);
                        intent2.putExtra("topTitle",workTable.getName());
                        intent2.putExtra("systemCode",OAManager.OA);
                        startActivity(intent2);
                       break;
                    case TASK:
                        Intent intent3 = new Intent(context,WebViewActivity.class);
                        intent3.putExtra("topTitle",workTable.getName());
                        intent3.putExtra("URL", CommConstants.HTTP_API_URL+"/schedule.html?userName="+userInfo.getEmpAdname());
                        startActivity(intent3);
                        break;
                    default:
                        Intent intent1 = new Intent(context,WebViewActivity.class);
                        intent1.putExtra("topTitle",workTable.getName());
                        intent1.putExtra("URL",workTable.getAndroid_access_url());
                        startActivity(intent1);
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OAManager.addUserActionLog(getIntent().getStringExtra("moduleId"),2);
        unregisterReceiver(UnreadNumReceiver);
    }

}
