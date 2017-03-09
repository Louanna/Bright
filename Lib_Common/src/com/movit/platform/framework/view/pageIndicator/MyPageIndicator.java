/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.movit.platform.framework.view.pageIndicator;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.movit.platform.common.R;

/**
 * Draws circles (one for each view). The current view position is filled and
 * others are only stroked.
 */
public class MyPageIndicator extends View implements PageIndicator {
	private static final int INVALID_POINTER = -1;

	int mItemWidth;
	int mItemHeight;
	int mOffset = 10;

	int mPaddingLeft;
	int mPaddingRight;
	int mPaddingTop;
	private float mPageOffset;
	Bitmap plane;
	Bitmap circle;
	Bitmap plane_a;
	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mListener;
	private int mCurrentPage;
	private int mSnapPage;
	private int mScrollState;
	private int mOrientation;
	private boolean mCentered;
	private boolean mSnap;

	private int mTouchSlop;
	private float mLastMotionX = -1;
	private int mActivePointerId = INVALID_POINTER;
	private boolean mIsDragging;

	private boolean goright = true;

	public MyPageIndicator(Context context) {
		this(context, null);
	}

	public MyPageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.vpiCirclePageIndicatorStyle);
	}

	@SuppressWarnings("deprecation")
	public MyPageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode())
			return;

		mCentered = true;
		mOrientation = HORIZONTAL;
		mPaddingLeft = getPaddingLeft();
		mPaddingRight = getPaddingRight();
		mPaddingTop = getPaddingTop();

		plane = BitmapFactory.decodeResource(getResources(),
				R.drawable.frame_plane);
		circle = BitmapFactory.decodeResource(getResources(),
				R.drawable.frame_circle);
//
//		Matrix matrix = new Matrix();
//		matrix.postRotate(180); /* 翻转180度 */
//		int width = plane.getWidth();
//		int height = plane.getHeight();
//		plane_a = Bitmap.createBitmap(plane, 0, 0, width, height, matrix, true);

		plane_a = plane;

		int planeWidth = plane.getWidth();
		int circleWidth = circle.getWidth();
		mItemWidth = (planeWidth + mOffset);
		mItemHeight = mItemWidth;
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat
				.getScaledPagingTouchSlop(configuration);
	}

	public void setCentered(boolean centered) {
		mCentered = centered;
		invalidate();
	}

	public boolean isCentered() {
		return mCentered;
	}

	public void setOrientation(int orientation) {
		switch (orientation) {
		case HORIZONTAL:
		case VERTICAL:
			mOrientation = orientation;
			requestLayout();
			break;

		default:
			throw new IllegalArgumentException(
					"Orientation must be either HORIZONTAL or VERTICAL.");
		}
	}

	public int getOrientation() {
		return mOrientation;
	}

	public void setSnap(boolean snap) {
		mSnap = snap;
		invalidate();
	}

	public boolean isSnap() {
		return mSnap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mViewPager == null) {
			return;
		}
		final int count = mViewPager.getAdapter().getCount();
		if (count == 0) {
			return;
		}

		if (mCurrentPage >= count) {
			setCurrentItem(count - 1);
			return;
		}

		int planeWidth = plane.getWidth();
//		int circleWidth = circle.getWidth();

		int layoutWidth = mItemWidth * count;
		int left, top;
		left = getWidth() / 2 - layoutWidth / 2;
		top = getPaddingTop();
		for (int i = 0; i < count; i++) {
			int l = left + i * mItemWidth + (mItemWidth - planeWidth) / 2;
			int t = top + (mItemHeight - planeWidth) / 2;
			if (i == mCurrentPage) {
				if (mPageOffset != 0.0) {
					canvas.drawBitmap(circle, l, t, null);
				} else {
					Log.v("drawBitmap", l + " : " + t);
					Paint paint = new Paint();
					paint.setAlpha(0); // 设置透明程度
					canvas.drawBitmap(circle, l, t, paint);
				}
			} else {
				canvas.drawBitmap(circle, l, t, null);
			}

		}

		int cx = 0;
		if (!mSnap) {
			cx = (int) (mPageOffset * mItemWidth);
		}
		int l = left + mCurrentPage * mItemWidth + (mItemWidth - planeWidth)
				/ 2 + cx;
		int t = top + (mItemHeight - planeWidth) / 2;

		if (!goright) {
			canvas.drawBitmap(plane_a, l, t, null);
		} else {
			canvas.drawBitmap(plane, l, t, null);
		}
	}

	public Handler getMyHandler() {
		return myHandler;
	}

	public void setMyHandler(Handler myHandler) {
		this.myHandler = myHandler;
	}

	private Handler myHandler;

	public boolean onTouchEvent(android.view.MotionEvent ev) {
		if (super.onTouchEvent(ev)) {
			return true;
		}
		if ((mViewPager == null) || (mViewPager.getAdapter().getCount() == 0)) {
			return false;
		}

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mLastMotionX = ev.getX();
			break;

		case MotionEvent.ACTION_MOVE: {
			final int activePointerIndex = MotionEventCompat.findPointerIndex(
					ev, mActivePointerId);
			final float x = MotionEventCompat.getX(ev, activePointerIndex);
			final float deltaX = x - mLastMotionX;

			if (!mIsDragging) {
				if (Math.abs(deltaX) > mTouchSlop) {
					mIsDragging = true;
				}
			}

			if (mIsDragging) {
				mLastMotionX = x;
				if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
					mViewPager.fakeDragBy(deltaX);
				}
			}

			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (!mIsDragging) {
				final int count = mViewPager.getAdapter().getCount();
				final int width = getWidth();
				final float halfWidth = width / 2f;
				final float sixthWidth = width / 6f;

				if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
					if (action != MotionEvent.ACTION_CANCEL) {
						mViewPager.setCurrentItem(mCurrentPage - 1);
					}
					return true;
				} else if ((mCurrentPage < count - 1)
						&& (ev.getX() > halfWidth + sixthWidth)) {
					if (action != MotionEvent.ACTION_CANCEL) {
						mViewPager.setCurrentItem(mCurrentPage + 1);
					}
					return true;
				}
			}
			mIsDragging = false;
			mActivePointerId = INVALID_POINTER;
			if (mViewPager.isFakeDragging())
				mViewPager.endFakeDrag();
			break;

		case MotionEventCompat.ACTION_POINTER_DOWN: {
			final int index = MotionEventCompat.getActionIndex(ev);
			mLastMotionX = MotionEventCompat.getX(ev, index);
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		}

		case MotionEventCompat.ACTION_POINTER_UP:
			final int pointerIndex = MotionEventCompat.getActionIndex(ev);
			final int pointerId = MotionEventCompat.getPointerId(ev,
					pointerIndex);
			if (pointerId == mActivePointerId) {
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mActivePointerId = MotionEventCompat.getPointerId(ev,
						newPointerIndex);
			}
			mLastMotionX = MotionEventCompat.getX(ev,
					MotionEventCompat.findPointerIndex(ev, mActivePointerId));
			break;
		}

		return true;
	}

	@Override
	public void setViewPager(ViewPager view) {
		if (mViewPager == view) {
			return;
		}
		if (mViewPager != null) {
			mViewPager.setOnPageChangeListener(null);
		}
		if (view.getAdapter() == null) {
			throw new IllegalStateException(
					"ViewPager does not have adapter instance.");
		}
		mViewPager = view;
		mViewPager.setOnPageChangeListener(this);
		invalidate();
	}

	@Override
	public void setViewPager(ViewPager view, int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}

	@Override
	public void setCurrentItem(int item) {
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		mViewPager.setCurrentItem(item);
		mCurrentPage = item;
		invalidate();
	}

	@Override
	public void notifyDataSetChanged() {
		invalidate();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		mScrollState = state;

		if (mListener != null) {
			mListener.onPageScrollStateChanged(state);
		}
	}

	private static boolean doLogin = false;

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		mCurrentPage = position;
		if (goright) {
		}
		if (mPageOffset > positionOffset) {
			// 递减，向右侧滑动
			if (goright && positionOffset == 0.0) {
				goright = true;
			} else {
				goright = false;
			}
		} else if (mPageOffset < positionOffset) {
			// 递减，向右侧滑动
			goright = true;
		} else if (mPageOffset == positionOffset) {

			goright = true;

			if(!doLogin && 4 == position){

				doLogin = true;
				myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
								myHandler.sendEmptyMessageDelayed(5, 100);
                            }
                        }, 50);
			}

		}
		mPageOffset = positionOffset;

		invalidate();

		if (mListener != null) {
			mListener.onPageScrolled(position, positionOffset,
					positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		mCurrentPage = position;
		mSnapPage = position;

		invalidate();

		if (mListener != null) {
			mListener.onPageSelected(position);
		}
	}

	@Override
	public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
		mListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureLong(widthMeasureSpec),
				measureShort(heightMeasureSpec));
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureLong(int measureSpec) {
		int result;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
			// We were told how big to be
			result = specSize;
		} else {
			// Calculate the width according the views count
			final int count = mViewPager.getAdapter().getCount();
			result = (int) (getPaddingLeft() + getPaddingRight() + (count * mItemWidth));
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureShort(int measureSpec) {
		int result;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Measure the height
			result = (int) (mItemHeight + getPaddingTop() + getPaddingBottom());
			// Respect AT_MOST value if that was what is called for by
			// measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mCurrentPage = savedState.currentPage;
		mSnapPage = savedState.currentPage;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPage = mCurrentPage;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPage;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPage = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPage);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
