package com.movitech.EOP.base;

import android.content.Context;
import android.os.Handler;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

public class CompanyInfoable implements Runnable {

	private Handler handler;

	public CompanyInfoable(Context context, Handler myHandler) {
		super();
		this.handler = myHandler;

		HttpManager.getJson(CommConstants.URL_EOP_COMPANY, new StringCallback() {
			@Override
			public void onError(Call call, Exception e) {
				handler.sendEmptyMessage(5);
			}

			@Override
			public void onResponse(String response) throws JSONException {
				if (StringUtils.notEmpty(response)) {
					JSONObject jsonObject = new JSONObject(response);
					boolean ok = jsonObject.getBoolean("ok");
					if (ok) {
						JSONObject object = jsonObject.getJSONObject("objValue");
						CommConstants.productName = object.getString("productName");
						CommConstants.companyName = object.getString("name");
						CommConstants.companyLogo = object.getString("companyLogo");
//						aQuery.cache(CommConstants.URL_DOWN + CommConstants.companyLogo, 0);
						MFAQueryHelper.cacheImage(CommConstants.URL_DOWN + CommConstants.companyLogo);
					}
					handler.sendEmptyMessageDelayed(5, 1000);
				}

			}
		});
	}

	@Override
	public void run() {
//		try {
//			String result = HttpClientUtils.get(CommConstants.URL_EOP_COMPANY);
//			JSONObject jsonObject = new JSONObject(result);
//			boolean ok = jsonObject.getBoolean("ok");
//			if (ok) {
//				JSONObject object = jsonObject.getJSONObject("objValue");
//				CommConstants.productName = object.getString("productName");
//				CommConstants.companyName = object.getString("name");
//				CommConstants.companyLogo = object.getString("companyLogo");
//				aQuery.cache(CommConstants.URL_DOWN + CommConstants.companyLogo, 0);
//			}
//			handler.sendEmptyMessageDelayed(5, 1000);
//		} catch (Exception e) {
//			e.printStackTrace();
//			handler.sendEmptyMessage(5);
//		}
	}
}
