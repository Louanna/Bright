package com.movitech.EOP.module.workbench.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.widget.xlistview.XListView;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.manager.OACallback;
import com.movitech.EOP.manager.OAManager;
import com.movitech.EOP.module.workbench.adapter.CompanyNewsAdapter;
import com.movitech.EOP.module.workbench.model.News;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by air on 16/9/26.
 * Company News page
 */
public class CompanyNewsActivity extends EOPBaseActivity implements XListView.IXListViewListener,AbsListView.OnScrollListener {

    private String type;
    private int currentpage, pagesize;

    private TextView unreadCount;
    private XListView lvCompanyNews;
    private CompanyNewsAdapter adapter;

    private ArrayList<News> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_news);

        findViews();

        type = getIntent().getStringExtra("code");
        currentpage = 1;
        pagesize = 10;

        initData();
    }

    private void findViews(){
        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(getIntent().getStringExtra("topTitle"));

        ImageView topImgRight = (ImageView) findViewById(R.id.common_top_img_right);
        topImgRight.setVisibility(View.GONE);
        topImgRight.setImageResource(R.drawable.nf_ico_search);

        unreadCount = (TextView) findViewById(R.id.unread_count);

        ImageView back = (ImageView) findViewById(R.id.common_top_img_left);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        lvCompanyNews = (XListView) findViewById(R.id.lv_company_news);
        lvCompanyNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context,WebViewActivity.class);
                intent.putExtra("title",((News)adapter.getItem(position-1)).getTitle());
                intent.putExtra("URL",((News)adapter.getItem(position-1)).getUrl());
                intent.putExtra("isOA",true);
                startActivity(intent);
            }
        });

        lvCompanyNews.setXListViewListener(this);
        lvCompanyNews.setOnScrollListener(this);
    }

    private void initData() {

        if (StringUtils.notEmpty(CommConstants.ACCESSTOKEN)) {
            OAManager.validateToken(oaCallback);
        } else {
            OAManager.createToken(oaCallback);
        }
    }

    private OACallback oaCallback = new OACallback() {
        @Override
        public void getOAData() {

            OAManager.getOAData(type, currentpage, pagesize, this);
        }

        @Override
        public void showDataList(List<News> newsList) {

            if(newsList.size() > 0){
                lvCompanyNews.setPullLoadEnable(true);
                data.addAll(newsList);
                currentpage++;
            }else {
                lvCompanyNews.setPullLoadEnable(false);
            }
            lvCompanyNews.stopLoadMore();
            if(null!=adapter){
                adapter.notifyDataSetChanged();
            }else{
                setAdapter();
            }

            lvCompanyNews.stopRefresh();
            lvCompanyNews.setRefreshTime(DateUtils.date2Str(new Date()));
        }

        @Override
        public void showUnreadCount(int unreadNum) {
            unreadCount.setText(String.format(getString(R.string.news_unread_num), unreadNum));
        }

        @Override
        public void onError() {
            ToastUtils.showToast(CompanyNewsActivity.this,getString(R.string.get_message_fail));
            lvCompanyNews.stopRefresh();
            lvCompanyNews.setRefreshTime(DateUtils.date2Str(new Date()));
        }
    };

    @Override
    public void onRefresh() {
        currentpage = 1;
        data.clear();
        initData();
    }

    @Override
    public void onLoadMore() {
        initData();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int itemsLastIndex = adapter.getCount() - 1; // 数据集最后一项的索引
        int lastIndex = itemsLastIndex + 2; // 加上底部的loadMoreView项 和顶部刷新的
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex) {
            // 如果是自动加载,可以在这里放置异步加载数据的代码
            lvCompanyNews.startLoadMore();
        }
    }

    int visibleLastIndex;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private void setAdapter() {
        adapter = new CompanyNewsAdapter(data,this);
        lvCompanyNews.setAdapter(adapter);
        if (data.isEmpty()) {
            lvCompanyNews.setPullLoadEnable(false);
        } else {
            lvCompanyNews.setPullLoadEnable(true);
        }
        lvCompanyNews.setPullRefreshEnable(true);
    }

}
