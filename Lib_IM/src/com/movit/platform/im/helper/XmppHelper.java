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

package com.movit.platform.im.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.manager.CommManager;
import com.movit.platform.framework.helper.MFHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.manager.IMManager;
import com.movit.platform.im.manager.MessageManager;
import com.movit.platform.im.manager.XmppManager;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import com.tencent.android.tpush.XGPushManager;

/**
 * Created by Louanna.Lu on 2015/10/16.
 */
public class XmppHelper {

    private Context context;
    private CommonHelper loginHelper;
    private ServiceHelper serviceHelper;

    public XmppHelper(Context context) {
        super();
        this.context = context;
        loginHelper = new CommonHelper(context);
        serviceHelper = new ServiceHelper();
    }

    // 登录xmpp
    public void loginXMPP() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                LoginInfo loginConfig = loginHelper.getLoginConfig();
                String username = loginConfig.getmUserInfo().getEmpAdname();
                String password = loginConfig.getmUserInfo().getOpenFireToken();

                if (StringUtils.empty(password)) {
                    password = "1";
                }
                try {
                    XmppManager manager = XmppManager.getInstance();
                    // 初始化xmpp配置
                    manager.initialize(CommConstants.URL_XMPP, CommConstants.PORT);
                    XMPPConnection connection = manager.getConnection();

                    connection.login(username.toLowerCase().trim(), password + "," + MFHelper.getDeviceId(context), MFHelper.getDeviceId(context)); // 登录
                    connection.sendPacket(new Presence(Presence.Type.available));

                    String xmpp = connection.getServiceName();
                    MessageManager.SUFFIX = "@" + xmpp;
                    MessageManager.GROUP_AT = "@conference." + xmpp;
                    handler.sendEmptyMessage(3);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(2);
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:// 失败
                    Intent intent = new Intent(
                            CommConstants.SIMPLE_LOGIN_ACTION);
                    intent.putExtra("body", context.getString(R.string.im_connect_failed));
                    intent.putExtra("type", CommConstants.TYPE_JUST_TIPS);
                    intent.setPackage(context.getPackageName());
                    context.sendBroadcast(intent);
                    break;
                case 3:// 初始化各项服务
                    //每次成功登录XMPP后，均需重新updatDevice，以保证设备信息及时更新，push消息推送正确
                    registerXGPush();
                    //每次成功登录XMPP后，均需主动调用getGroupList
                    GroupManager.getInstance(context).getGroupList();
                    //每次成功登录XMPP后，均需重新获取聊天记录
                    IMManager.getContactList(context,null);
                    // 每次成功登录XMPP后，均需启动XMPP服务
                    serviceHelper.startService();
                    break;
                default:
                    break;
            }
        }
    };

    private void registerXGPush() {
        try {
            String deviceId = MFHelper.getDeviceId(context);
            String userId = MFSPHelper.getString(CommConstants.USERID);
            //信鸽服务器注册
            XGPushManager.registerPush(context, userId + "," + deviceId);
            Intent service = new Intent(context, com.tencent.android.tpush.service.XGPushService.class);
            context.startService(service);
            //EOP服务器注册
            CommManager.postDeviceType(deviceId, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
