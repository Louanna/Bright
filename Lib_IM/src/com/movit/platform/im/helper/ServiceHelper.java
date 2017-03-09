package com.movit.platform.im.helper;

import android.content.Intent;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.im.service.XMPPService;
import com.movit.platform.im.service.ReConnectService;

public class ServiceHelper {

	public ServiceHelper() {
		super();
	}

	public void startService() {
		// 聊天服务
		Intent chatServer = new Intent(BaseApplication.getInstance(), XMPPService.class);
		BaseApplication.getInstance().startService(chatServer);
		// 自动恢复连接服务
		Intent reConnectService = new Intent(BaseApplication.getInstance(), ReConnectService.class);
		BaseApplication.getInstance().startService(reConnectService);
	}

	/**
	 * 
	 * 销毁服务.
	 * 
	 * @author shimiso
	 * @update 2012-5-16 下午12:16:08
	 */
	public void stopService() {
		// 聊天服务
		Intent chatServer = new Intent(BaseApplication.getInstance(), XMPPService.class);
		BaseApplication.getInstance().stopService(chatServer);
		// // 自动恢复连接服务
		Intent reConnectService = new Intent(BaseApplication.getInstance(), ReConnectService.class);
		BaseApplication.getInstance().stopService(reConnectService);
	}

}
