package com.movit.platform.framework.utils;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {

	public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
	public static final int LENGTH_LONG = Toast.LENGTH_LONG;

	private static Toast toast;
	private static Handler handler = new Handler();

	private static Runnable run = new Runnable() {
		public void run() {
			toast.cancel();
		}
	};

	private static void toast(Context ctx, CharSequence msg, int duration,
			boolean center) {
		handler.removeCallbacks(run);
		// handler的duration不能直接对应Toast的常量时长，在此针对Toast的常量相应定义时长
		switch (duration) {
		case LENGTH_SHORT:// Toast.LENGTH_SHORT值为0，对应的持续时间大概为1s
			duration = 1000;
			break;
		case LENGTH_LONG:// Toast.LENGTH_LONG值为1，对应的持续时间大概为3s
			duration = 2000;
			break;
		default:
			break;
		}
		if (null != toast) {
			toast.setText(msg);
		} else {
			toast = Toast.makeText(ctx, msg, duration);
			if (center) {
				toast.setGravity(Gravity.CENTER, 0, 0);
			} else {
				toast.setGravity(Gravity.BOTTOM, 0, 80);
			}
		}
		handler.postDelayed(run, duration);
		toast.show();
	}

	/**
	 * 封装Toast提示框信息，显示在中间
	 * 
	 * @param context
	 * @param words
	 */
	public static void showToast(Context ctx, String msg) {
		// Toast toast = Toast.makeText(context, words, 2000);
		// toast.setGravity(Gravity.CENTER, 0, 0);
		// toast.show();

		if (null == ctx) {
			throw new NullPointerException("The ctx is null!");
		}
		int duration = LENGTH_LONG;
		toast(ctx, msg, duration, true);

	}

	/**
	 * 封装Toast提示框信息，显示在底部
	 * 
	 * @param context
	 * @param words
	 */
	public static void showToastBottom(Context ctx, String msg) {
		if (null == ctx) {
			throw new NullPointerException("The ctx is null!");
		}
		int duration = LENGTH_LONG;
		toast(ctx, msg, duration, false);
	}
}
