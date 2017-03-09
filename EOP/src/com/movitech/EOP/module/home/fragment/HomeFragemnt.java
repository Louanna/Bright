package com.movitech.EOP.module.home.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.activity.DownloadActivity;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.base.BaseFragment;
import com.movitech.EOP.module.qrcode.ScanActivity;
import com.movitech.EOP.module.workbench.adapter.DragAdapter;
import com.movitech.EOP.module.workbench.constants.Constants;
import com.movitech.EOP.module.workbench.manager.WorkTableClickDelagate;
import com.movitech.EOP.module.workbench.manager.WorkTableManage;
import com.movitech.EOP.module.workbench.model.WorkTable;
import com.movitech.EOP.utils.Json2ObjUtils;
import com.movitech.EOP.view.DragGridViewPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class HomeFragemnt extends BaseFragment {

    private List<WorkTable> oaWorkTables;
    private List<WorkTable> tphWorkTables;
    private List<WorkTable> myWorkTables;
    private List<WorkTable> otherWorkTables;
    private ArrayList<WorkTable> allWorkTables;

    private ImageView ivShare;

    private DragGridViewPage dragGridViewPage;
    private DragAdapter dragAdapter;
    private Context context;
    private PopupWindow popupWindow;
    private Timer timer;
    private Map<String, Integer> unReadNums = new HashMap<String, Integer>();

    public final static int MODULE_ERROR = 13;

    private boolean first = true;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
                case Constants.PERSONALMODULES_RESULT:
                    saveAndShowWorkTable((String) msg.obj);
                    break;
                case MODULE_ERROR:
                    progressDialogUtil.dismiss();
                    EOPApplication.showToast(getActivity(), getResources().getString(R.string.get_messages_error));
                    setAdapter();
                    break;
                case 33:// model del
                    WorkTable table = (WorkTable) msg.obj;
                    // 保存本地
                    WorkTable more = new WorkTable();
                    more.setId("more");
                    myWorkTables.remove(more);
                    String myjson = JSONArray.toJSONString(myWorkTables);
                    MFSPHelper.setString("myWorkTables", myjson);
                    myWorkTables.add(more);

                    otherWorkTables.add(table);
                    String otherjson = JSONArray.toJSONString(otherWorkTables);
                    MFSPHelper.setString("otherWorkTables", otherjson);
                    break;
                case 34:
                    final WorkTable moreTable = new WorkTable();
                    moreTable.setId("more");
                    myWorkTables.remove(moreTable);
                    final String myjsons = JSONArray.toJSONString(myWorkTables);
                    MFSPHelper.setString("myWorkTables", myjsons);
                    myWorkTables.add(moreTable);
                    break;
                default:
                    break;
            }
        }

    };

    private void saveAndShowWorkTable(String json) {
        progressDialogUtil.dismiss();
        try {
            allWorkTables = Json2ObjUtils.getAllmodules(json);

//            if (myWorkTables == null || otherWorkTables == null) {
                if (myWorkTables == null) {
                    myWorkTables = new ArrayList<>();
                } else {
                    myWorkTables.clear();
                }
                if (otherWorkTables == null) {
                    otherWorkTables = new ArrayList<>();
                } else {
                    otherWorkTables.clear();
                }
                if (oaWorkTables == null) {
                    oaWorkTables = new ArrayList<>();
                } else {
                    oaWorkTables.clear();
                }
                if (tphWorkTables == null) {
                    tphWorkTables = new ArrayList<>();
                } else {
                    tphWorkTables.clear();
                }

                for (int i = 0; i < allWorkTables.size(); i++) {

                    if (Constants.STATUS_OFFLINE.equals(allWorkTables.get(i)
                            .getStatus())) {
                        continue;
                    }
                    if (Constants.STATUS_AVAILABLE
                            .equals(allWorkTables.get(i).getStatus())) {
                        if(Constants.TYPE_OA.equals(allWorkTables.get(i).getType())){
                            oaWorkTables.add(allWorkTables.get(i));
                        }else if(Constants.TYPE_TPH.equals(allWorkTables.get(i).getType())){
                            tphWorkTables.add(allWorkTables.get(i));
                        }else {
                            myWorkTables.add(allWorkTables.get(i));
                        }
                    } else {
                        otherWorkTables.add(allWorkTables.get(i));
                    }
                }
                // 保存本地
                String myjson = JSONArray.toJSONString(myWorkTables);
                MFSPHelper.setString("myWorkTables", myjson);
                String otherjson = JSONArray
                        .toJSONString(otherWorkTables);
                MFSPHelper.setString("otherWorkTables", otherjson);
//            } else {
//                // 如果服务器有更新，则加入other
//                List<WorkTable> tempList = new ArrayList<WorkTable>();
//                List<WorkTable> myTempList = new ArrayList<WorkTable>();
//                List<WorkTable> otherTempList = new ArrayList<WorkTable>();
//
//                for (WorkTable workTable : allWorkTables) {
//                    if (!Constants.STATUS_OFFLINE.equals(workTable.getStatus())) {
//                        tempList.add(workTable);
//                    }
//                }
//
//                for (int i = 0; i < myWorkTables.size(); i++) {
//                    WorkTable table = myWorkTables.get(i);
//                    for (int j = 0; j < allWorkTables.size(); j++) {
//
//                        if (Constants.STATUS_OFFLINE.equals(allWorkTables.get(j)
//                                .getStatus())) {
//                            continue;
//                        }
//
//                        if (table.equals(allWorkTables.get(j))) {
//                            myTempList.add(allWorkTables.get(j));
//                        }
//                    }
//                }
//                myWorkTables.clear();
//                myWorkTables.addAll(myTempList);
//                for (int i = 0; i < otherWorkTables.size(); i++) {
//                    WorkTable table = otherWorkTables.get(i);
//                    for (int j = 0; j < allWorkTables.size(); j++) {
//                        if (Constants.STATUS_OFFLINE.equals(allWorkTables.get(j)
//                                .getStatus())) {
//                            continue;
//                        }
//                        if (table.equals(allWorkTables.get(j))) {
//                            otherTempList.add(allWorkTables.get(j));
//                        }
//                    }
//                }
//                otherWorkTables.clear();
//                otherWorkTables.addAll(otherTempList);
//                tempList.removeAll(myWorkTables);
//                tempList.removeAll(otherWorkTables);
//
//                for (int k = 0; k < tempList.size(); k++) {
//                    WorkTable table = tempList.get(k);
//
//                    if (Constants.STATUS_AVAILABLE.equalsIgnoreCase(table.getStatus())) {
//
//                        myWorkTables.add(table);
//                    } else if (Constants.STATUS_UNAVAILABLE.equalsIgnoreCase(table.getStatus())) {
//                        otherWorkTables.add(table);
//                    }
//                }
//                // 保存本地
//                String myjson = JSONArray.toJSONString(myWorkTables);
//                MFSPHelper.setString("myWorkTables", myjson);
//                String otherjson = JSONArray
//                        .toJSONString(otherWorkTables);
//                MFSPHelper.setString("otherWorkTables", otherjson);
//            }
            setAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        unReadNums.put(WorkTableClickDelagate.EMAIL, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.eop_fragment_home, null, false);

        //注册广播
        BaseApplication.getInstance().registerReceiver(tableBroadcastReceiver, new IntentFilter("table"));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void initDialog() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.pop_home_guid, null);
        final Dialog customDialog = new Dialog(context, com.movit.platform.common.R.style.ImageloadingDialogStyle);
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(view);
        customDialog.setCancelable(false);
        customDialog.setCanceledOnTouchOutside(false);
        Window win = customDialog.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
        customDialog.show();

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MFSPHelper.setBoolean("isHomeGuid", false);
                customDialog.dismiss();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void initViews() {

        dragGridViewPage = (DragGridViewPage) findViewById(R.id.dragGridView);
        dragGridViewPage.setCustomerEnable(false);
        ivShare = (ImageView) findViewById(R.id.iv_share);
        ivShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initPopuptWindow();
                popupWindow.showAsDropDown(v);
//                Intent intent = new Intent();
//                intent.setClass(HomeFragemnt.this.getActivity(), ScanActivity.class);
//                startActivity(intent);
            }
        });
    }

    @Override
    protected void initDatas() {

        String json = MFSPHelper.getString("myWorkTables");
        String otherjson = MFSPHelper.getString("otherWorkTables");
        try {
            myWorkTables = com.alibaba.fastjson.JSONArray.parseArray(json,
                    WorkTable.class);
            otherWorkTables = com.alibaba.fastjson.JSONArray.parseArray(
                    otherjson, WorkTable.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        WorkTableManage manage = new WorkTableManage(getActivity());
        progressDialogUtil.showLoadingDialog(getActivity(), getResources().getString(R.string.loading), false);
        manage.getPersonalModules(handler);
    }

    @Override
    protected void resumeDatas() {
        if (first) {
            first = false;
        } else {
//            MailboxEntry.getUnread();
        }

    }

    public void setAdapter() {
        if (myWorkTables == null) {
            myWorkTables = new ArrayList<WorkTable>();
        }
//        WorkTable table = new WorkTable();
//        table.setId("more");
//        myWorkTables.add(table);
        dragAdapter = new DragAdapter(context, myWorkTables, dragGridViewPage,
                handler, "del");

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
//                    if (position == dragGridViewPage.getAvalableChildCount() - 1) {// more
//                        Intent intent = new Intent(getActivity(),WokTableDragListActivity.class);
//                        intent.putExtra("otherTable", (Serializable) otherWorkTables);
//                        startActivityForResult(intent, 1);
//                    } else {
//                        if (position > dragGridViewPage.getCustomerPostion()) {
//                            position = position - 1;
//                        }
//                        WorkTableClickDelagate clickDelagate = new WorkTableClickDelagate(context);
//                        clickDelagate.onClickWorkTable(myWorkTables, position);
//                    }

//                    if (position > dragGridViewPage.getCustomerPostion()) {
//                        position = position - 1;
//                    }
                    WorkTableClickDelagate clickDelagate = new WorkTableClickDelagate(context);
                    if(myWorkTables.get(position).getAndroid_access_url().equals(WorkTableClickDelagate.OA)){
                        clickDelagate.setOaWorkTables(oaWorkTables);
                    }
                    if(myWorkTables.get(position).getAndroid_access_url().equals(WorkTableClickDelagate.TPH)){
                        clickDelagate.setTphWorkTables(tphWorkTables);
                    }
                    clickDelagate.onClickWorkTable(myWorkTables, position);

                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1) {

                dragAdapter.reSetData();
                dragAdapter.notifyDataSetChanged();
            }
        }
    }

    private final BroadcastReceiver tableBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals("table")) {
                try {
                    WorkTable table = (WorkTable) intent.getSerializableExtra("table");
                    WorkTable more = new WorkTable();
                    more.setId("more");
                    myWorkTables.remove(more);
                    myWorkTables.add(table);
                    String myjson = JSONArray.toJSONString(myWorkTables);
                    MFSPHelper.setString("myWorkTables", myjson);
                    myWorkTables.add(more);

                    String otherjson = MFSPHelper.getString("otherWorkTables");
                    try {
                        otherWorkTables = com.alibaba.fastjson.JSONArray.parseArray(
                                otherjson, WorkTable.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    dragAdapter.reSetData();
                    dragAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    private void initPopuptWindow() {
        View contactView = LayoutInflater.from(getActivity()).inflate(
                R.layout.contact_pop_window, null);
        LinearLayout group_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_1);
        ImageView imageView1 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_1);
        TextView textView1 = (TextView) contactView
                .findViewById(R.id.pop_textview_1);
        LinearLayout email_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_2);
        ImageView imageView2 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_2);
        TextView textView2 = (TextView) contactView
                .findViewById(R.id.pop_textview_2);
        imageView1.setImageResource(R.drawable.scan_ico_scanning);
        textView1.setText(getResources().getString(R.string.scan));
        imageView2.setImageResource(R.drawable.scan_ico_download);
        textView2.setText(getResources().getString(R.string.download_app));
        group_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                Intent intent = new Intent();
                intent.setClass(HomeFragemnt.this.getActivity(), ScanActivity.class);
                startActivity(intent);
            }
        });
        email_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
//                startActivity(new Intent(getActivity(),
//                        EopCodeActivity.class));
                startActivity(new Intent(getActivity(),
                        DownloadActivity.class));


            }
        });
        popupWindow = new PopupWindow(contactView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

    }

    public void getUnread(int i) {
        if (i >= 0) {
            unReadNums.put(WorkTableClickDelagate.EMAIL, i);
            if (dragAdapter != null) {
                dragAdapter.setUnreadNums(unReadNums);
                dragAdapter.notifyDataSetChanged();
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (dragAdapter != null) {
                            dragAdapter.setUnreadNums(unReadNums);
                            //dragAdapter.reSetData();
                            dragAdapter.notifyDataSetChanged();
                        }
                    }
                }, 1000);
            }
        }
    }

    public void setProcessUnreadNum(int oaUnreadNum,int erpUnreadNum){
        unReadNums.put(WorkTableClickDelagate.OA, oaUnreadNum);
        unReadNums.put(WorkTableClickDelagate.ERP, erpUnreadNum);
        if (dragAdapter != null) {
            dragAdapter.setUnreadNums(unReadNums);
            dragAdapter.notifyDataSetChanged();
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dragAdapter != null) {
                        dragAdapter.setUnreadNums(unReadNums);
                        //dragAdapter.reSetData();
                        dragAdapter.notifyDataSetChanged();
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        BaseApplication.getInstance().unregisterReceiver(tableBroadcastReceiver);

//        if (myWorkTables != null) {
//            myWorkTables.clear();
//            myWorkTables = null;
//        }
    }

}
