package com.movitech.EOP.module.qrcode;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.movit.platform.common.api.IZoneManager;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;
import com.movitech.EOP.base.ViewHelper;
import com.movitech.EOP.module.qrcode.zxing.encoding.EncodingHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import cn.sharesdk.ShareDialog;

public class EopCodeActivity extends EOPBaseActivity {

	ImageView more;
	ImageView two;
	TextView name;
	Bitmap qrCodeBitmap;
	public static final int ZONE_SAY_RESULT = 4;
	JSONArray uploadImagesJsonArray = new JSONArray();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two_dimensional_code);
		ImageView topLeft = (ImageView) findViewById(R.id.common_top_img_left);
		two = (ImageView) findViewById(R.id.two_dimensional);
		more = (ImageView) findViewById(R.id.common_top_img_right);
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		name = (TextView) findViewById(R.id.app_name);
		if (StringUtils.notEmpty(CommConstants.companyName)) {
			name.setText(CommConstants.companyName);
		} else {
			name.setText(getString(R.string.app_name));
		}

		try {
			qrCodeBitmap = EncodingHandler.createQRCode(
					CommConstants.APP_DOWNLOAD_URL, new ViewHelper().dip2px(context, 200));
			two.setImageBitmap(qrCodeBitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}

		more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final ShareDialog dialog = new ShareDialog(EopCodeActivity.this);
				dialog.show2Item();
				dialog.setCancelButtonOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						switch (position){
//							case 0:
//								shareToSC();
//								break;
							case 0:
								dialog.shareToWeChat();
								break;
							case 1:
								dialog.shareToMoments();
								break;
							default:
								break;
						}
						dialog.dismiss();
					}
				});
			}
		});
	}

	private void shareToSC(){
		String filePath = CommConstants.SD_DATA_PIC + Calendar.getInstance().getTimeInMillis()+".png";
		File file = new File(filePath);
		FileOutputStream out;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			Bitmap bitmap = takeScreenShot(EopCodeActivity.this);
			if (bitmap.compress(
					Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		toUploadFile(filePath);
	}

	private void toUploadFile(final String filePath) {
//		File file = new File(filePath);
//		HttpManager.uploadFile(CommConstants.URL_UPLOAD, null, file, "file", new ListCallback() {
//			@Override
//			public void onError(Call call, Exception e) {
//			}
//
//			@Override
//			public void onResponse(JSONArray response) throws JSONException {
//				if (StringUtils.notEmpty(response)) {
//					Message msg = Message.obtain();
//					msg.what = UPLOAD_SUCCESS_CODE;
//					Bundle data = new Bundle();
//					data.putString("message", response.getJSONObject(0).getString("uName"));
//					data.putString("uploadPath",filePath);
//					msg.setData(data);
//					handler.sendMessage(msg);
//				}
//			}
//		});
	}

	private static final int UPLOAD_SUCCESS_CODE = 1;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPLOAD_SUCCESS_CODE:
					Bundle bundle = msg.getData();
					String message = bundle.getString("message");
					String path = bundle.getString("uploadPath");
					// 上传成功，发送消息
					try {
						JSONObject remoteJson = new JSONObject();
						remoteJson.put("name", message);
						remoteJson.put("size", PicUtils.getPicSizeJson(path));
						uploadImagesJsonArray.put(0,remoteJson);

						// 删除拍照旋转的图片；
						String newPathString = PicUtils.getTempPicPath(path);
						File f = new File(newPathString);
						if (f.exists()) {
							f.delete();
						}
						toSay();
					} catch (Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(3);
					}
					break;
				case ZONE_SAY_RESULT:
					String result = (String) msg.obj;
					try {
						JSONObject jsonObject = new JSONObject(result);
						int code = jsonObject.getInt("code");
						if (0 == code) {
							ToastUtils.showToast(EopCodeActivity.this,
									getString(R.string.share_succeed));
						} else {
							ToastUtils.showToast(EopCodeActivity.this,
									getString(R.string.share_failed));
						}
					} catch (Exception e) {
						e.printStackTrace();
						ToastUtils.showToast(EopCodeActivity.this, getString(R.string.share_failed));
					}
					break;
				case 2 | 3:
						new Thread(new Runnable() {

							@Override
							public void run() {
								handler.sendEmptyMessage(999);

							}
						}).start();
					break;
				case 999:
					ToastUtils.showToast(EopCodeActivity.this,getString(R.string.picture_upload_failed));
					break;
				default:
					break;
			}
		}

	};

	public void toSay() {
		try {
			JSONObject imageJsonObject = new JSONObject();
			imageJsonObject.put("image", uploadImagesJsonArray);
			String content = "";
			IZoneManager zoneManager = ((BaseApplication) this.getApplication()).getManagerFactory().getZoneManager();
			zoneManager.say(content, "0", "0",
						imageJsonObject.toString(), "", "", "", handler);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private Bitmap takeScreenShot(Activity activity) {
		// View是你需要截图的View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();

		// 获取状态栏高度
//		Rect frame = new Rect();
//		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//		int statusBarHeight = frame.top;

		int statusBarHeight = getResources().getDimensionPixelOffset(R.dimen.dp_32);

		// 获取屏幕长和高
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		// 去掉标题栏
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
				- statusBarHeight);
		view.destroyDrawingCache();
		return b;
	}
}
