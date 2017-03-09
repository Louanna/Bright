/*
 * Copyright (C) 2015 Bright Yu Haiyang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: y.haiyang@qq.com
 */

package com.movitech.EOP.module.workbench.constants;

import com.movit.platform.common.constants.CommConstants;

/**
 * Created by Louanna.Lu on 2015/11/3.
 */
public class Constants {

    public final static int ALLMODULES_RESULT = 11;
    public final static int PERSONALMODULES_RESULT = 12;
    public final static int MODULE_ERROR = 13;
    public final static int UPDATEMODULES_RESULT = 14;

    private static String URL_WORK_TABLE = CommConstants.URL_EOP_API + "r/sys/appmgtrest/";

    public static String GET_ALL_MOUDLES = URL_WORK_TABLE + "getallmodules";
    public static String GET_PERSONAL_MOUDLES = URL_WORK_TABLE + "getpersonalmodules";
    public static String UPDATE_PERSONAL_MOUDLES = URL_WORK_TABLE + "updatepersonmodules";

    public static String URL_OFFICE_HR = CommConstants.URL_EOP_ADMIN + "a/oa/hr/myInformation?";

    //积分商城
    //用云拓真实环境演示
    public static String URL_OFFICE_SHOP = "http://218.4.117.12:1040" + "/eoop-studio/" + "a/shop/mobile/center?";

    // 其他情况应该设置如下：
    // public final static String URL_OFFICE_SHOP = CommConstants.URL_EOP_ADMIN+ "a/shop/mobile/center?";


    public static String URL_OFFICE_ATTENDANCE = CommConstants.URL_EOP_ADMIN + "a/oa/hr/myAttendance?";

    public static String URL_OFFICE_NEWS = CommConstants.URL_EOP_NEWS + "eoop/news?";

    public static String URL_OFFICE_TASK = CommConstants.BASE_URL + CommConstants.HOST_PORT + "/eoop-task?";


    //应用状态:1.available,2.unavailable,3.offline
    public final static String STATUS_AVAILABLE = "1";
    public final static String STATUS_UNAVAILABLE = "2";
    public final static String STATUS_OFFLINE = "3";

    //应用类型:1.internal html5,2.html5,3.3,4.thirdparty_app
    public final static String TYPE_INTERNAL_HTML5 = "1";
    public final static String TYPE_WEB_HTML5 = "2";
    public final static String TYPE_NATIVE_APP = "3";
    public final static String TYPE_THIRDPARTY_APP = "4";
    public final static String TYPE_OA = "5";//OA下级菜单
    public final static String TYPE_TPH = "6";//TPH下级菜单

    //应用是否展示 1.show 2.hide
    public final static String DISPLAY_SHOW = "1";
    public final static String DISPLAY_HIDE = "2";

    public static void initWorkTableApi() {

        URL_WORK_TABLE = CommConstants.URL_EOP_API + "r/sys/appmgtrest/";

        GET_ALL_MOUDLES = URL_WORK_TABLE + "getallmodules";
        GET_PERSONAL_MOUDLES = URL_WORK_TABLE + "getpersonalmodules";
        UPDATE_PERSONAL_MOUDLES = URL_WORK_TABLE + "updatepersonmodules";
        URL_OFFICE_HR = CommConstants.URL_EOP_ADMIN + "a/oa/hr/myInformation?";

        //积分商城
        //用云拓真实环境演示
        URL_OFFICE_SHOP = "http://218.4.117.12:1040" + "/eoop-studio/" + "a/shop/mobile/center?";
        // 其他情况应该设置如下：
        // public final static String URL_OFFICE_SHOP = CommConstants.URL_EOP_ADMIN+ "a/shop/mobile/center?";

        URL_OFFICE_ATTENDANCE = CommConstants.URL_EOP_ADMIN + "a/oa/hr/myAttendance?";
        URL_OFFICE_NEWS = CommConstants.URL_EOP_NEWS + "eoop/news?";
        URL_OFFICE_TASK = CommConstants.BASE_URL + CommConstants.HOST_PORT + "/eoop-task?";
    }
}
