package com.movitech.EOP.module.workbench.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movit.platform.sc.module.zone.model.BaseModel;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.workbench.adapter.ProcessAdapter;
import com.movitech.EOP.module.workbench.model.Process;
import com.movitech.EOP.module.workbench.model.ProcessModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;


/**
 * Created by air on 16/10/20.
 * 搜索流程
 */

public class SearchProcessActivity extends EOPBaseActivity implements
        XListView.IXListViewListener, AbsListView.OnScrollListener {

    private EditText searchText;
    private ImageView searchClear;

    private InputMethodManager inputmanger;

    private XListView listView;
    private ProcessAdapter adapter;
    private String systemCode;
    private int currentPage = 1;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_process);
        findViews();
        initData();
    }

    private void findViews(){
        systemCode = getIntent().getStringExtra("systemCode");

        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(getString(R.string.process_search));
        ImageView back = (ImageView) findViewById(R.id.common_top_left);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchClear = (ImageView) findViewById(R.id.search_clear);
        searchText = (EditText) findViewById(R.id.search_key);
        searchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputmanger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
            }
        });
        inputmanger = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        searchText.setOnKeyListener(new View.OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    content = searchText.getText().toString();
                    if (!TextUtils.isEmpty(content.trim())) {
                        searchClear.setVisibility(View.VISIBLE);
                        currentPage = 1;
                        adapter.getDataList().clear();
                        adapter.notifyDataSetChanged();
                        getBpiItemInfo(currentPage,content);
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                    }
                    inputmanger.hideSoftInputFromWindow(
                            searchText.getWindowToken(), 0);
                }
                return false;
            }
        });

        listView = (XListView) findViewById(R.id.process_listview);
    }

    private void initData(){

        adapter = new ProcessAdapter(SearchProcessActivity.this,new ArrayList<ProcessModel>());
        adapter.setIsDo(ProcessAdapter.DONE);
        listView.setAdapter(adapter);
        adapter.getDataList().clear();

        listView.setPullRefreshEnable(true);
        listView.setPullLoadEnable(false);
        listView.setXListViewListener(this);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(SearchProcessActivity.this, WebViewActivity.class);
                intent.putExtra("URL", adapter.getDataList().get(position).getAppUrl());
                SearchProcessActivity.this.startActivity(intent);
            }
        });

    }

    //获取待办数据
    private void getBpiItemInfo(int page,String content){

        OAManager.getOAORERPList(ProcessAdapter.DONE, systemCode, page,content, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                e.printStackTrace();
                listView.stopRefresh();
                listView.setRefreshTime(DateUtils.date2Str(new Date()));
                listView.stopLoadMore();
            }

            @Override
            public void onResponse(String response) throws JSONException {
                if(!TextUtils.isEmpty(response)){
                    BaseModel baseModel = JSON.parseObject(response,BaseModel.class);
                    if(baseModel.getOk()){
                        String object = JSON.toJSONString(baseModel.getObjValue());
                        Process process = JSON.parseObject(object,Process.class);
                        if(null != process){
                            ArrayList<ProcessModel> dataList = process.getItem();
                            if(null != dataList && !dataList.isEmpty()){
                                listView.setPullLoadEnable(true);
                                adapter.getDataList().addAll(dataList);
                                adapter.notifyDataSetChanged();
                                currentPage++;
                            }else {
                                listView.setPullLoadEnable(false);
                            }
                        }
                    }else {
                        ToastUtils.showToast(getContext(),baseModel.getValue());
                    }
                }else {
                    ToastUtils.showToast(getContext(),getString(R.string.message_server_unavailable));
                }
                listView.stopRefresh();
                listView.setRefreshTime(DateUtils.date2Str(new Date()));
                listView.stopLoadMore();
            }
        });
    }

    @Override
    public void onRefresh() {
        adapter.getDataList().clear();
        currentPage = 1;
        getBpiItemInfo(currentPage,content);
    }

    @Override
    public void onLoadMore() {
        getBpiItemInfo(currentPage,content);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex + 2; // 加上底部的loadMoreView项 和顶部刷新的
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex) {
            // 如果是自动加载,可以在这里放置异步加载数据的代码
            listView.startLoadMore();
        }
    }

    int visibleLastIndex;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }
}
