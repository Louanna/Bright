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

package com.movit.platform.im.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.movit.platform.im.service.XMPPService;
import com.movit.platform.im.utils.ChatTools;

/**
 * Created by Louanna.Lu on 2015/10/16.
 */
public class HomeKeyEventReceiver extends BroadcastReceiver {

    static final String SYSTEM_REASON = "reason";
    static final String SYSTEM_HOME_KEY = "homekey";// home key
    static final String SYSTEM_RECENT_APPS = "recentapps";// long home key

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_REASON);
            if (reason != null) {
                if (reason.equals(SYSTEM_HOME_KEY)) {
                    // home key处理点
                    ChatTools.leaveChat();
                    (new Handler()).post(new Runnable() {
                        @Override
                        public void run() {
                            // 聊天服务
                            Intent chatServer = new Intent(context, XMPPService.class);
                            context.stopService(chatServer);
                        }
                    });

                } else if (reason.equals(SYSTEM_RECENT_APPS)) {
                    // long home key处理点
                }
            }
        }
    }

}
