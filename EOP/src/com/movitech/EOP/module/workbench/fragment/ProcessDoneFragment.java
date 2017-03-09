package com.movitech.EOP.module.workbench.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movit.platform.sc.module.zone.model.BaseModel;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.BaseFragment;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.workbench.activity.WebViewActivity;
import com.movitech.EOP.module.workbench.adapter.ProcessAdapter;
import com.movitech.EOP.module.workbench.model.Process;
import com.movitech.EOP.module.workbench.model.ProcessModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;

/**
 * Created by air on 16/10/19.
 * 已办
 */
public class ProcessDoneFragment extends BaseFragment implements
        XListView.IXListViewListener, AbsListView.OnScrollListener {

    private XListView listView;
    private ProcessAdapter adapter;
    private String systemCode;
    private int currentPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_process, null, false);
        Bundle bundle=getArguments();
        systemCode = bundle.getString("systemCode");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initViews() {
        listView = (XListView) mRootView.findViewById(R.id.process_listview);
    }

    @Override
    protected void initDatas() {

        adapter = new ProcessAdapter(getActivity(),new ArrayList<ProcessModel>());
        adapter.setIsDo(ProcessAdapter.DONE);
        listView.setAdapter(adapter);
        adapter.getDataList().clear();

        listView.setPullRefreshEnable(true);
        listView.setXListViewListener(this);
        listView.setOnScrollListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(ProcessDoneFragment.this.getActivity(), WebViewActivity.class);
                intent.putExtra("isOA",true);
                intent.putExtra("title",adapter.getDataList().get(position-1).getTitle());
                intent.putExtra("URL", adapter.getDataList().get(position-1).getAppUrl());
                ProcessDoneFragment.this.getActivity().startActivity(intent);
            }
        });

        getBpiItemInfo(currentPage);
    }

    @Override
    protected void resumeDatas() {

    }

    //获取已办数据
    private void getBpiItemInfo(int page){

        OAManager.getOAORERPList(ProcessAdapter.DONE, systemCode, page,"", new StringCallback() {
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
                            if(null != processNumListener && null!=object && null!=(new JSONObject(object)).getString("total")){
                                CommConstants.OADONETOTAL = (new JSONObject(object)).getString("total");
                                processNumListener.getTodoNum(CommConstants.OATODOTOTAL,CommConstants.OADONETOTAL);
                            }
                        }
                    }else {
                        ToastUtils.showToast(getActivity(),baseModel.getValue());
                    }
                }else {
                    ToastUtils.showToast(getActivity(),getString(R.string.message_server_unavailable));
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
        getBpiItemInfo(currentPage);
    }

    @Override
    public void onLoadMore() {
        getBpiItemInfo(currentPage);
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

    private ProcessNumListener processNumListener;

    public interface ProcessNumListener{
        void getTodoNum(String todo,String done);
    }

    public void setProcessNumListener(ProcessNumListener processNumListener){
        this.processNumListener = processNumListener;
    }
}
