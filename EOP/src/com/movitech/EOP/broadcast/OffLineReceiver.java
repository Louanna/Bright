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

package com.movitech.EOP.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.movit.platform.common.constants.CommConstants;
import com.movitech.EOP.activity.DialogActivity;

/**
 * Created by Louanna.Lu on 2015/10/29.
 * 离线广播
 */
public class OffLineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (CommConstants.SIMPLE_LOGIN_ACTION.equals(action)) {

            if (CommConstants.TYPE_LOGINOUT.equals(intent.getStringExtra("type"))) {
                //强制退出APP
                Intent intent1 = new Intent(context, DialogActivity.class);
                intent1.putExtra("body", intent.getStringExtra("body"));
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            } else if (CommConstants.TYPE_JUST_TIPS.equals(intent.getStringExtra("type"))) {
                //Tips
                Toast.makeText(context, intent.getStringExtra("body"), Toast.LENGTH_SHORT);
            }
        }
    }
}
