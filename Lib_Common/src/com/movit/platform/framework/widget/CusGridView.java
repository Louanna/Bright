package com.movit.platform.framework.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class CusGridView extends GridView {

	public CusGridView(Context context) {
		super(context);
	}

	public CusGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CusGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 int expandSpec = MeasureSpec.makeMeasureSpec(
	               Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);  
	    super.onMeasure(widthMeasureSpec, expandSpec); 
	}

}
