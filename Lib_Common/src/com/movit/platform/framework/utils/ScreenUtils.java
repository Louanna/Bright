package com.movit.platform.framework.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;

import com.movit.platform.common.R;

/** * @类名:ScreenUtils * @类描述:屏幕工具类 * @修改人: * @修改时间: * @修改备注: * @版本: */
public class ScreenUtils {
	/**
	 * * @方法说明:获取DisplayMetrics对象 * @方法名称:getDisPlayMetrics * @param context * @return
	 * * @返回值:DisplayMetrics
	 */
	public static DisplayMetrics getDisPlayMetrics(Context context) {
		DisplayMetrics metric = new DisplayMetrics();
		if (null != context) {
			((Activity) context).getWindowManager().getDefaultDisplay()
					.getMetrics(metric);
		}
		return metric;
	}

	/**
	 * * @方法说明:获取屏幕的宽度（像素） * @方法名称:getScreenWidth * @param context * @return *
	 * 
	 * @返回值:int
	 */
	public static int getScreenWidth(Context context) {
		int width = getDisPlayMetrics(context).widthPixels;
		return width;
	}

	/**
	 * * @方法说明:获取屏幕的高（像素） * @方法名称:getScreenHeight * @param context * @return *
	 * 
	 * @返回值:int
	 */
	public static int getScreenHeight(Context context) {
		int height = getDisPlayMetrics(context).heightPixels;
		return height;
	}

	/**
	 * * @方法说明:屏幕密度(0.75 / 1.0 / 1.5) * @方法名称:getDensity * @param context * @return
	 * * @返回 float
	 */
	public static float getDensity(Context context) {
		return getDisPlayMetrics(context).density;
	}

	/**
	 * * @方法说明:屏幕密度DPI(120 / 160 / 240) * @方法名称:getDensityDpi * @param context * @return
	 * * @返回 int
	 */
	public static int getDensityDpi(Context context) {
		int densityDpi = getDisPlayMetrics(context).densityDpi;
		return densityDpi;
	}

	//截屏
	private Bitmap screenShot(Activity activity) {
		// View是你需要截图的View
		View view = activity.getWindow().getDecorView();

		//这里的R.id.dialog_view随便写的,应该是webview的id才对
		View webView = view.findViewById(R.id.dialog_view);
		webView.setDrawingCacheEnabled(true);
		webView.buildDrawingCache();
		Bitmap b1 = webView.getDrawingCache();

		// 获取状态栏高度
//		Rect frame = new Rect();
//		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
//		int statusBarHeight = frame.top;

//      int statusBarHeight = activity.getResources().getDimensionPixelOffset(R.dimen.dp_32);
		int statusBarHeight = 0;

		// 获取屏幕长和高
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		// 去掉标题栏
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, b1.getWidth(), b1.getHeight());
		view.destroyDrawingCache();
		return b;
	}
}