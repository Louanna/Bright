package com.movitech.EOP.base;

import android.content.Context;

public class ViewHelper {

	public ViewHelper() {
		super();
	}

	public int dip2px(Context context, float dpValue) {
		if (context == null) {
			return 0;
		}
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
