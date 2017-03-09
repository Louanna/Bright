package com.movitech.EOP.module.qrcode;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.module.qrcode.zxing.view.ViewfinderView;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.application.EOPApplication;
import com.movitech.EOP.module.workbench.activity.WebViewActivity;

import okhttp3.Call;

public class ScanActivity extends MipcaActivity {
	private ImageView back;
	private ImageView topRight;
	private TextView title;
	String type;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);

		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		back = (ImageView) findViewById(R.id.common_top_left);
		title = (TextView) findViewById(R.id.common_top_title);
		topRight = (ImageView) findViewById(R.id.common_top_right);
		title.setText(getResources().getString(R.string.scan));
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ScanActivity.this.finish();
			}
		});
		topRight.setVisibility(View.GONE);

		type = getIntent().getStringExtra("type");
	}

	@Override
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		final String resultString = result.getText();
		if (resultString.equals("")) {
			Toast.makeText(ScanActivity.this, getResources().getString(R.string.scan_error),
					Toast.LENGTH_SHORT).show();
		} else {
			if ("sign".equals(type)) {
				String url = resultString + MFSPHelper.getString(CommConstants.EMPADNAME);
				HttpManager.getJsonWithToken(url, new StringCallback() {
					@Override
					public void onError(Call call, Exception e) {
						mHandler.sendEmptyMessage(2);
					}

					@Override
					public void onResponse(String response) throws JSONException {
						mHandler.obtainMessage(1, response).sendToTarget();
					}
				});
			} else {
				Intent resultIntent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("result", resultString);
				bundle.putParcelable("bitmap", barcode);
				resultIntent.putExtras(bundle);
				startActivity(new Intent(this, WebViewActivity.class).putExtra(
						"URL", resultString).putExtra("sao-sao", true));
			}
		}
		ScanActivity.this.finish();
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				try {
					String json = (String) msg.obj;
					JSONObject object = new JSONObject(json);
					int code = object.getInt("code");
					String message = object.getString("msg");
					if (code == 0) {
						ScanActivity.this.finish();
					}
					EOPApplication.showToast(context, message);
				} catch (JSONException e) {
					e.printStackTrace();
					EOPApplication.showToast(context, getResources().getString(R.string.sign_error));
				}
				break;
			case 2:
				EOPApplication.showToast(context, getResources().getString(R.string.sign_error));
				break;
			default:
				break;
			}
		}

	};
}
