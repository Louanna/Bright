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

package com.movit.platform.common.application;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Louanna.Lu on 2015/10/30.
 */
public interface UIController {

    //跳转到首页
    public void startMainActivity(Activity activity, Intent intent, int flag);

    /***
     * 同事圈页面
     ***/
    //同事圈个人头像点击事件
    public void onOwnHeadClickListener(Activity activity, Intent intent, int flag);

    //个人详细资料发消息点击事件
    public void onSendMessageClickListener(Activity activity, Intent intent);

    //个人详细资料同事圈点击事件
    public void onZoneOwnClickListener(Activity activity, Intent intent, int flag);

    public void onZoneOwnClickListener(Fragment fragment, Intent intent, int flag);

    // 发起单聊
    public void startPrivateChat(Activity activity, Bundle bundle);

    // 发起多人聊天
    public void startMultChat(Activity activity, Bundle bundle);

    // 进入群聊列表页面
    public void onGroupListClickListener(Activity activity);

    // 进入动态消息列表页面
    public void onZoneMsgClickListener(Fragment fragment, Intent intent, int flag);

    // 进入动态消息详情页面
    public void onZoneMsgDetailClickListener(Activity activity, Intent intent, int flag);

    // 进入发布说说页面
    public void onZonePublishClickListener(Fragment fragment, Intent intent, int flag);

    // 进入部门组织页面
    public void onIMOrgClickListener(Activity activity, Intent intent, int flag);

    // 进入发邮件界面
    public void onSendEmailClickListener(Activity activity, Intent intent);

    public ComponentName getMainComponentName(Context context);

}
