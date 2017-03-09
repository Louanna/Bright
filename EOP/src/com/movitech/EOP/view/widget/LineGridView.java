package com.movitech.EOP.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.ViewHelper;

public class LineGridView extends GridView {

	int width = 2;

	public LineGridView(Context context) {
		super(context);
		width = new ViewHelper().dip2px(
				getContext(), 1);
	}

	public LineGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		width = new ViewHelper().dip2px(
				getContext(), 1);
	}

	public LineGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		width = new ViewHelper().dip2px(
				getContext(), 1);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		View localView1 = getChildAt(0);
		if (localView1 == null) {
			return;
		}
		int column = getWidth() / localView1.getWidth();
		int childCount = getChildCount();
		Paint localPaint;
		localPaint = new Paint();
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setColor(getContext().getResources().getColor(
				R.color.chat_time_bg_color));
		localPaint.setStrokeWidth((float) width);// 设置线宽 

		for (int i = 0; i < childCount; i++) {
			View cellView = getChildAt(i);
			if ((i + 1) % column == 0) {// 最右边的
				// 底部
				canvas.drawLine(cellView.getLeft(), cellView.getBottom(),
						cellView.getRight(), cellView.getBottom(), localPaint);
			} else {
				// 底部+右侧
				canvas.drawLine(cellView.getRight(), cellView.getTop(),
						cellView.getRight(), cellView.getBottom(), localPaint);
				canvas.drawLine(cellView.getLeft(), cellView.getBottom(),
						cellView.getRight(), cellView.getBottom(), localPaint);
			}
		}
	}
}
