package com.movitech.EOP.module.workbench.manager;

import android.content.Context;
import android.os.Handler;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.module.workbench.constants.Constants;

import org.json.JSONException;

import okhttp3.Call;

public class WorkTableManage {
	Context context;

	public WorkTableManage(Context context) {
		super();
		this.context = context;
	}

	public void getAllModules(final Handler handler) {
		OkHttpUtils.postWithToken()
				.url(Constants.GET_ALL_MOUDLES)
				.build()
				.execute(new StringCallback() {
					@Override
					public void onError(Call call, Exception e) {
						handler.sendEmptyMessage(Constants.MODULE_ERROR);
					}

					@Override
					public void onResponse(String response) throws JSONException {
						handler.obtainMessage(Constants.ALLMODULES_RESULT,
								response).sendToTarget();
					}
				});
	}

	public void getPersonalModules(final Handler handler) {
		OkHttpUtils.getWithToken()
				.url(Constants.GET_PERSONAL_MOUDLES
						+ "?userId="
						+ MFSPHelper.getString(CommConstants.USERID))
				.build()
				.execute(new StringCallback() {
					@Override
					public void onError(Call call, Exception e) {
						handler.sendEmptyMessage(Constants.MODULE_ERROR);
					}

					@Override
					public void onResponse(String response) throws JSONException {
						handler.obtainMessage(Constants.PERSONALMODULES_RESULT,
								response).sendToTarget();
					}
				});
	}

	public void updateModules(final Handler handler, final String modules) {
		String url = Constants.UPDATE_PERSONAL_MOUDLES
				+ "?userId="
				+ MFSPHelper.getString(CommConstants.USERID)
				+ "&moduleIds=" + modules;
		OkHttpUtils.postWithToken()
				.url(url)
				.build()
				.execute(new StringCallback() {
					@Override
					public void onError(Call call, Exception e) {
						handler.sendEmptyMessage(Constants.MODULE_ERROR);
					}

					@Override
					public void onResponse(String response) throws JSONException {
						handler.obtainMessage(Constants.UPDATEMODULES_RESULT,
								response).sendToTarget();
					}
				});
	}
}
