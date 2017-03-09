package com.movit.platform.sc.view.clipview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.RelativeLayout;

import com.movit.platform.sc.R;

public class ClickedRelativeLayout extends RelativeLayout implements
		OnClickListener, OnLongClickListener {

	private static final String TAG = ClickedRelativeLayout.class
			.getSimpleName();

	public ClickedRelativeLayout(Context context) {
		super(context);
		init();
	}

	public ClickedRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setBackgroundResource(R.drawable.m_txt_click_item_selector);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean result = super.onInterceptTouchEvent(ev);
		return result;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = super.onTouchEvent(event);
		return result;
	}

	@Override
	public boolean onLongClick(View v) {
		return true;
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public boolean hasFocusable() {
		return false;
	}

}
