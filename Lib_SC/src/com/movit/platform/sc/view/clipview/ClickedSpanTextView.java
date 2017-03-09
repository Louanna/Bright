package com.movit.platform.sc.view.clipview;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class ClickedSpanTextView extends TextView {

	private static final String TAG = ClickedSpanTextView.class.getSimpleName();
	Context mContext;
	private Long lastClickTime = 0l;

	public ClickedSpanTextView(Context context) {
		super(context);
		mContext = context;
	}

	public ClickedSpanTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	private int mStart = -1;
	private int mEnd = -1;

	ClickedSpanListener clickableSpan;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();

		int x = (int) event.getX();
		int y = (int) event.getY();

		x -= getTotalPaddingLeft();
		y -= getTotalPaddingTop();

		x += getScrollX();
		y += getScrollY();

		Layout layout = getLayout();
		int line = layout.getLineForVertical(y);
		int off = layout.getOffsetForHorizontal(line, x);

		CharSequence text = getText();

		Spannable buffer = (Spannable) text;
		ClickedSpanListener[] link = buffer.getSpans(off, off,
				ClickedSpanListener.class);

		if (System.currentTimeMillis() - lastClickTime > 500) {
			if (clickableSpan != null) {
				Log.v("action", "onLongClick");
				clickableSpan.onLongClick(this);
				Selection.removeSelection(buffer);
				if (mStart >= 0 && mEnd >= mStart) {
					buffer.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
							mStart, mEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					mStart = -1;
					mEnd = -1;
				}
				clickableSpan = null;
				return true;
			}
		}

		if (link.length != 0) {
			ClickedSpanListener linkSpan = link[0];
			if (action == MotionEvent.ACTION_DOWN) {
				clickableSpan = linkSpan;
				lastClickTime = System.currentTimeMillis();
				mStart = buffer.getSpanStart(link[0]);
				mEnd = buffer.getSpanEnd(link[0]);

				if (mStart >= 0 && mEnd >= mStart) {
					buffer.setSpan(new BackgroundColorSpan(Color.GRAY), mStart,
							mEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					// 取消掉系统本身的点击背景颜色
					Selection.setSelection(buffer, 0, 0);
				}
			} else if (action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_CANCEL) {

				if (mStart >= 0 && mEnd >= mStart) {
					buffer.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
							mStart, mEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					if (clickableSpan == linkSpan) {
						clickableSpan.onClick(this);
					}
					mStart = -1;
					mEnd = -1;
				}
				clickableSpan = null;
				Selection.removeSelection(buffer);
			} else if (action == MotionEvent.ACTION_MOVE) {

				if (clickableSpan != null && clickableSpan != linkSpan) {
					if (mStart >= 0 && mEnd >= mStart) {
						buffer.setSpan(new BackgroundColorSpan(
								Color.TRANSPARENT), mStart, mEnd,
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						Selection.removeSelection(buffer);
						clickableSpan = null;
					}
				}
			}
			return true;
		} else {
			Selection.removeSelection(buffer);
			if (mStart >= 0 && mEnd >= mStart) {
				buffer.setSpan(new BackgroundColorSpan(Color.TRANSPARENT),
						mStart, mEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				mStart = -1;
				mEnd = -1;
			}
			clickableSpan = null;
			return false;
		}
	}

	@Override
	public boolean hasFocusable() {
		return false;
	}

}
