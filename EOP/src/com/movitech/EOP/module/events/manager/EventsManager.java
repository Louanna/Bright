package com.movitech.EOP.module.events.manager;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;

/**
 * Created by Administrator on 2016/1/4.
 */
public class EventsManager {

    //获取活动列表
    public static void getActivitiesListByUserId(String userId, StringCallback stringCallback) {

        String url = CommConstants.URL_EVENTS + "/listPersonalActivity?userId=" + userId;

        OkHttpUtils
                .postStringWithToken()
                .url(url)
                .content("")
                .build()
                .execute(stringCallback);
    }

    //获取奖项列表
    public static void getAwardsList(String userId, String activityId, StringCallback stringCallback) {

        String url = CommConstants.URL_EVENTS + "/listActivityPrize?userId=" + userId + "&activityId=" + activityId;

        OkHttpUtils
                .postStringWithToken()
                .url(url)
                .content("")
                .build()
                .execute(stringCallback);
    }

    //获取获奖人员列表
    public static void getWinnersList(String awardId, StringCallback stringCallback) {

        String url = CommConstants.URL_EVENTS + "/listPrizeNameList?prizeId=" + awardId;

        OkHttpUtils
                .postStringWithToken()
                .url(url)
                .content("")
                .build()
                .execute(stringCallback);
    }

    //发起摇奖请求
    public static void postLuckyDraw(String userId, StringCallback stringCallback) {

        String url = CommConstants.URL_EVENTS + "/shakePrize?userId=" + userId;

        OkHttpUtils
                .postStringWithToken()
                .url(url)
                .content("")
                .build()
                .execute(stringCallback);
    }
}
