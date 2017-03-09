package com.movit.platform.framework.view.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CustomViewPager extends ViewPager {
	private boolean willIntercept = true;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (willIntercept) {
			// 这个地方直接返回true会很卡
			try {
				return super.onInterceptTouchEvent(arg0);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (willIntercept) {
			return super.onTouchEvent(event);
		}
		return false;
	}

	/**
	 * 设置ViewPager是否拦截点击事件
	 * 
	 * @param value
	 *            if true, ViewPager拦截点击事件 if false,
	 *            ViewPager将不能滑动，ViewPager的子View可以获得点击事件 主要受影响的点击事件为横向滑动
	 * 
	 */
	public void setTouchIntercept(boolean value) {
		willIntercept = value;
	}

	@Override
	protected boolean canScroll(View arg0, boolean arg1, int arg2, int arg3,
			int arg4) {
		return super.canScroll(arg0, arg1, arg2, arg3, arg4);
	}
}
