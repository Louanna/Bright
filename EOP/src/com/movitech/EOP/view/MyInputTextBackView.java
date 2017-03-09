package com.movitech.EOP.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.movitech.EOP.Test.R;

public class MyInputTextBackView extends View {

	private static float density;
	Paint paint;
	/**
	 * 四个绿色边角对应的长度
	 */
	private int ScreenRate;

	/**
	 * 四个绿色边角对应的宽度
	 */
	private static final int CORNER_WIDTH = 4;
	private static final int OFFSET_WIDTH = 2;

	public MyInputTextBackView(Context context, AttributeSet attrs) {
		super(context, attrs);
		density = context.getResources().getDisplayMetrics().density;
		// 将像素转换成dp
		ScreenRate = (int) (15 * density);
		paint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		paint.setColor(getResources().getColor(R.color.white));
		Rect frame = new Rect(0, 0, width, height);
		canvas.drawRect(CORNER_WIDTH + OFFSET_WIDTH, CORNER_WIDTH
				+ OFFSET_WIDTH, width - CORNER_WIDTH - OFFSET_WIDTH, height
				- CORNER_WIDTH - OFFSET_WIDTH, paint);
		// 画扫描框边上的角，总共8个部分
		canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
				frame.top + CORNER_WIDTH, paint);
		canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
				frame.top + ScreenRate, paint);
		canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
				frame.top + CORNER_WIDTH, paint);
		canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
				frame.top + ScreenRate, paint);
		canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
				+ ScreenRate, frame.bottom, paint);
		canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left
				+ CORNER_WIDTH, frame.bottom, paint);
		canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
				frame.right, frame.bottom, paint);
		canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
				frame.right, frame.bottom, paint);
	}

}
