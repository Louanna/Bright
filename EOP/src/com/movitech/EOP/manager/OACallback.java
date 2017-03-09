package com.movitech.EOP.manager;

import com.movitech.EOP.module.workbench.model.News;

import java.util.List;

/**
 * Created by Administrator on 2016/11/16.
 */

public interface OACallback {

    public void getOAData();

    public void showDataList(List<News> newsList);

    public void showUnreadCount(int unreadNum);

    public void onError();
}
