package com.movitech.EOP.view;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Adapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.Scroller;

import com.movitech.EOP.Test.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class DragGridViewPage extends ViewGroup {

	/** DragGridView的item长按响应的时间， 默认是1000毫秒，也可以自行设置 */
	private long dragResponseMS = 1000;
	/** 是否可以拖拽，默认不可以 */
	private boolean isDrag = false;
	/** 震动器 */
	private Vibrator mVibrator;

	private WindowManager mWindowManager;
	/** item镜像的布局参数 */
	private WindowManager.LayoutParams mWindowLayoutParams;
	/** 状态栏的高度 */
	private int mStatusHeight;
	/** 正在拖拽的position */
	private int mDragPosition;
	private boolean mAnimationEnd = true;

	/** 刚开始拖拽的item对应的View */
	private View mStartDragItemView = null;
	/** 用于拖拽的镜像，这里直接用一个ImageView */
	private ImageView mDragImageView;

	/** 我们拖拽的item对应的Bitmap */
	private Bitmap mDragBitmap;
	/** 按下的点到所在item的上边缘的距离 */
	private int mPoint2ItemTop;
	/** 按下的点到所在item的左边缘的距离 */
	private int mPoint2ItemLeft;
	/** DragGridView距离屏幕顶部的偏移量 */
	private int mOffset2Top;
	/** DragGridView距离屏幕左边的偏移量 */
	private int mOffset2Left;
	/** DragGridView自动向下滚动的边界值 */
	private int mDownScrollBorder;
	/** DragGridView自动向上滚动的边界值 */
	private int mUpScrollBorder;
	/** DragGridView自动滚动的速度 */
	private static final int speed = 20;

	private boolean canOnClick = false;

	onItemDelectAndSwapCallback onItemDelectAndSwapCallback;
	private OnItemClickListener mOnItemClickListener;
	DragGridBaseAdapter mDragAdapter;
	// scorll
	private Direction orientation = Direction.vertical;
	private VelocityTracker velocityTracker;
	private Scroller mScroller;
	private int mTouchSlop;
	private int maxFlingVelocity;
	private int minFlingVelocity;
	private static final int MIN_FLING_VELOCITY = 400;
	private static final int INVALID_POINTER = -1;
	private int mActivePointerId = INVALID_POINTER;
	int desireWidth;
	int desireHeight;

	enum Direction {
		horizontal, vertical;
	};

	// touch
	private int mStartDownX;
	private int mStartDownY;
	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;

	// internal paddings
	private int mPaddingLeft;
	private int mPaddingTop;
	private int mPaddingRight;
	private int mPaddingButtom;

	private int mItemWidth;
	private int mItemHeight;
	private int mCustomerWidth;
	private int mCustomerHeight;
	// item 间距
	private int mItemGap;
	private int mCustomerGap;
	private static final int DEFAULT_GRID_GAP = 0; // gap between grids (dips)
	private static final int DEFAULT_CUSTOMER_GAP = 10; // gap between grids
														// (dips)
	private int lineColor = 0xffe6e6e6;
	private final int mNumColumns = 3;
	private int mNumCustomer = 0;
	private int mNumRows;
	boolean mCustomerEnable = true;

	private int avalableChildCount;
	private int lastAvalableChildCount = 1;

	private Adapter mAdapter;
	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			dataSetChanged();
		}

		@Override
		public void onInvalidated() {
			dataSetChanged();
		}
	};

	private Handler mHandler = new Handler();

	// 用来处理是否为长按的Runnable
	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			isDrag = true; // 设置可以拖拽
			// mVibrator.vibrate(50); // 震动一下
			canOnClick = false;
			mDragAdapter.startDrag(mStartDragItemView, mDragPosition);

			// 开启mDragItemView绘图缓存
			mStartDragItemView.setDrawingCacheEnabled(true);
			// 获取mDragItemView在缓存中的Bitmap对象
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
					.getDrawingCache());
			// 这一步很关键，释放绘图缓存，避免出现重复的镜像
			mStartDragItemView.destroyDrawingCache();

			mStartDragItemView.setVisibility(View.INVISIBLE);// 隐藏该item
			// 根据我们按下的点显示item镜像
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};

	public DragGridViewPage(Context context) {
		super(context);
		initDraggableGridViewPager(context);
	}

	public DragGridViewPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDraggableGridViewPager(context);
	}

	public DragGridViewPage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initDraggableGridViewPager(context);

	}

	@Override
	protected void onDetachedFromWindow() {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
		super.onDetachedFromWindow();
	}

	private void initDraggableGridViewPager(Context context) {
		mVibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); // 获取状态栏的高度
        lineColor = getResources().getColor(R.color.worktable_line_color);
		setWillNotDraw(false);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setFocusable(true);
		setChildrenDrawingOrderEnabled(true);

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		final float density = context.getResources().getDisplayMetrics().density;

		mItemGap = (int) (DEFAULT_GRID_GAP * density);
		mCustomerGap = (int) (DEFAULT_CUSTOMER_GAP * density);
		// internal paddings
		mPaddingLeft = getPaddingLeft();
		mPaddingTop = getPaddingTop();
		mPaddingRight = getPaddingRight();
		mPaddingButtom = getPaddingBottom();
		super.setPadding(0, 0, 0, 0);
		minFlingVelocity = (int) (MIN_FLING_VELOCITY * density);
		maxFlingVelocity = configuration.getScaledMaximumFlingVelocity();

		mScroller = new Scroller(context);
		mTouchSlop = ViewConfigurationCompat
				.getScaledPagingTouchSlop(configuration);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		mItemWidth = (getWidth() - mPaddingLeft - mPaddingRight - (mNumColumns - 1)
				* mItemGap)
				/ mNumColumns;
		mItemHeight = mItemWidth;

		mCustomerWidth = getWidth() - mPaddingLeft - mPaddingRight;

		mCustomerHeight = mItemWidth + 2 * mCustomerGap;

		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			Rect rect = null;
			if (mCustomerEnable) {
				if (i == mNumColumns * mNumCustomer) {
					rect = getRectByCustomerView(i);
				} else {
					rect = getRectByPosition(i);
				}
			} else {
				rect = getRectByPosition(i);
			}

			child.measure(MeasureSpec.makeMeasureSpec(rect.width(),
					MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
					rect.height(), MeasureSpec.EXACTLY));
			child.layout(rect.left, rect.top, rect.right, rect.bottom);
		}
		mNumRows = ((getChildCount() + (mNumColumns - 1)) / mNumColumns);// 判断共几行
		if (mCustomerEnable) {
			if (mNumRows < mNumCustomer) {
				mNumRows = mNumCustomer;
			}
			if ((getChildCount() + (mNumColumns - 1)) % mNumColumns != 0) {
				mNumRows += 1;
			}
		}
		desireHeight = mItemWidth * mNumRows + mItemGap * (mNumRows - 1)
				+ mPaddingTop + mPaddingButtom + 2 * mCustomerGap;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		View localView1 = getChildAt(0);
		if (localView1 == null) {
			return;
		}
		int childCount = getChildCount();
		Paint localPaint;
		localPaint = new Paint();
		localPaint.setStyle(Paint.Style.STROKE);
		localPaint.setColor(lineColor);
		final float density = getResources().getDisplayMetrics().density;

		localPaint.setStrokeWidth(1 * density);

		for (int i = 0; i < childCount; i++) {
			View cellView = getChildAt(i);
			int k = 0;
			if (i < mNumColumns * mNumCustomer) {
				k = i;
			} else if (i > mNumColumns * mNumCustomer) {
				k = i + mNumColumns - 1;
			} else {
				k = mNumColumns * mNumCustomer;
			}

			int l = cellView.getLeft();
			int t = cellView.getTop();
			int r = l + mItemWidth;
			int b = t + mItemHeight;
			int cr = l + mCustomerWidth;
			int cb = t + mCustomerHeight;
			if ((k + 1) % mNumColumns == 0) {
				canvas.drawLine(l, b, r, b, localPaint);
			} else {
				if (k == mNumColumns * mNumCustomer) {
					canvas.drawLine(l, cb, cr, cb, localPaint);
				} else {
					if (i == getChildCount() - 1) {// 最后一个，画完整条线
						canvas.drawLine(r, t, r, b, localPaint);
						canvas.drawLine(l, b, r, b, localPaint);
						int emptyCount = mNumColumns - (k + 1) % mNumColumns;
						for (int j = 1; j <= emptyCount; j++) {
							canvas.drawLine(r + j * mItemWidth, t, r + j
									* mItemWidth, b, localPaint);
							canvas.drawLine(l + j * mItemWidth, b, r + j
									* mItemWidth, b, localPaint);
						}

					} else {
						canvas.drawLine(r, t, r, b, localPaint);
						canvas.drawLine(l, b, r, b, localPaint);
					}
				}

			}
		}

	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		return super.drawChild(canvas, child, drawingTime);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getActionMasked();

		if (action == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			// 该事件可能不是我们的
			return false;
		}

		boolean isIntercept = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 如果动画还未结束，则将此事件交给onTouchEvet()处理，
			// 否则，先分发给子View
			isIntercept = !mScroller.isFinished();
			// 如果此时不拦截ACTION_DOWN时间，应该记录下触摸地址及手指id，当我们决定拦截ACTION_MOVE的event时，
			// 将会需要这些初始信息（因为我们的onTouchEvent将可能接收不到ACTION_DOWN事件）
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int pointerIndex = MotionEventCompat.findPointerIndex(ev,
					mActivePointerId);
			int mx = (int) ev.getX(pointerIndex);
			int my = (int) ev.getY(pointerIndex);

			// 根据方向进行拦截，（其实这样，如果我们的方向是水平的，里面有一个ScrollView，那么我们是支持嵌套的）
			if (orientation == Direction.horizontal) {
				if (Math.abs(mDownX - mx) >= mTouchSlop) {
					// we get a move event for ourself
					isIntercept = true;
				}
			} else {
				if (Math.abs(mDownY - my) >= mTouchSlop) {
					isIntercept = true;
				}
			}

			// 如果不拦截的话，我们不会更新位置，这样可以通过累积小的移动距离来判断是否达到可以认为是Move的阈值。
			// 这里当产生拦截的话，会更新位置（这样相当于损失了mTouchSlop的移动距离，如果不更新，可能会有一点点跳的感觉）
			if (isIntercept) {
				mDownX = mx;
				mDownY = my;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// 这是触摸的最后一个事件，无论如何都不会拦截
			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}
			break;
		case MotionEventCompat.ACTION_POINTER_DOWN:
			final int index = MotionEventCompat.getActionIndex(ev);
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			// 获取离开屏幕的手指的索引
			int pointerIndexLeave = ev.getActionIndex();
			int pointerIdLeave = ev.getPointerId(pointerIndexLeave);
			if (mActivePointerId == pointerIdLeave) {
				// 离开屏幕的正是目前的有效手指，此处需要重新调整，并且需要重置VelocityTracker
				int reIndex = pointerIndexLeave == 0 ? 1 : 0;
				mActivePointerId = ev.getPointerId(reIndex);
				// 调整触摸位置，防止出现跳动
				mDownX = (int) ev.getX(reIndex);
				mDownY = (int) ev.getY(reIndex);
				if (velocityTracker != null)
					velocityTracker.clear();
			}
			break;
		}
		return isIntercept;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();

        if(getChildCount() == 0){
            return  false;
        }

		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 获取索引为0的手指id
			mActivePointerId = MotionEventCompat.getPointerId(event, 0);
			mDownX = (int) event.getX();
			mDownY = (int) event.getY();
			mStartDownX = (int) event.getX();
			mStartDownY = (int) event.getY();

			if (!mScroller.isFinished())
				mScroller.abortAnimation();
			canOnClick = false;
			// 根据按下的X,Y坐标获取所点击item的position
			mDragPosition = getPositionByXY(mDownX, mDownY);
			// Log.v("mDragPosition-down", mDragPosition +
			// " avalableChildCount:"
			// + avalableChildCount);
			if (mDragPosition != -1 && mDragPosition < avalableChildCount) {
				// 根据position获取该item所对应的View
				mStartDragItemView = getChildAt(mDragPosition);
				mStartDragItemView.setPressed(true);
				canOnClick = true;
//				// 如果是最后一个item 无法drag
//				if (mDragPosition != avalableChildCount
//						- lastAvalableChildCount) {
//					// 使用Handler延迟dragResponseMS执行mLongClickRunnable
//					mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
//
//					int[] mLocationInScreen = new int[2];
//					getLocationOnScreen(mLocationInScreen);// 获取本布局的坐标－
//					int[] location = new int[2];
//					getChildAt(0).getLocationOnScreen(location);
//					int top = mLocationInScreen[1] - location[1] + mPaddingTop;
//
//					// 下面这几个距离大家可以参考我的博客上面的图来理解下
//					mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();
//					mPoint2ItemTop = mDownY + top - mStartDragItemView.getTop();
//
//					mOffset2Left = (int) (event.getRawX() - mDownX);
//					mOffset2Top = (int) (event.getRawY() - mDownY);
//
//					// 获取DragGridView自动向上滚动的偏移量，小于这个值，DragGridView向下滚动
//					mDownScrollBorder = mItemHeight;
//					// 获取DragGridView自动向下滚动的偏移量，大于这个值，DragGridView向上滚动
//					mUpScrollBorder = getHeight() - mItemHeight;
//				} else {
//					mHandler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							mDragAdapter.clearDrag();
//						}
//					}, dragResponseMS / 2);
//
//				}
			}

			break;
		case MotionEvent.ACTION_MOVE:
			// 获取当前手指id所对应的索引，虽然在ACTION_DOWN的时候，我们默认选取索引为0
			// 的手指，但当有第二个手指触摸，并且先前有效的手指up之后，我们会调整有效手指

			// 屏幕上可能有多个手指，我们需要保证使用的是同一个手指的移动轨迹，
			// 因此此处不能使用event.getActionIndex()来获得索引
			final int pointerIndex = MotionEventCompat.findPointerIndex(event,
					mActivePointerId);
			int mx = (int) event.getX(pointerIndex);
			int my = (int) event.getY(pointerIndex);

			if (isDrag && mDragImageView != null) {
				moveX = mx;
				moveY = my;
				// 拖动item
				onDragItem(moveX, moveY);
				if (mStartDragItemView != null)
					mStartDragItemView.setPressed(false);
			} else {
				// Log.d("moveBy", "deltaX: " + Math.abs(mStartDownX - mx)
				// + "    deltaY: " + Math.abs(mStartDownY - my));
				if (Math.abs(mStartDownY - my) > 15
						|| Math.abs(mStartDownX - mx) > 15) {
					mHandler.removeCallbacks(mLongClickRunnable);
					moveBy((int) (mDownX - mx), (int) (mDownY - my));
					// 有移动
					canOnClick = false;
					if (mStartDragItemView != null)
						mStartDragItemView.setPressed(false);
				}
				// // 如果我们在按下的item上面移动，只要不超过item的边界我们就不移除mRunnable
				// if (!isTouchInItem(mStartDragItemView, mx, my)) {
				// mHandler.removeCallbacks(mLongClickRunnable);
				// moveBy((int) (mDownX - mx), (int) (mDownY - my));
				// }
			}

			mDownX = mx;
			mDownY = my;
			break;
		case MotionEvent.ACTION_UP:
			// 先判断是否是点击事件
			final int pi = MotionEventCompat.findPointerIndex(event,
					mActivePointerId);
			final float x = MotionEventCompat.getX(event, pi);
			final float y = MotionEventCompat.getY(event, pi);
			if (mStartDragItemView != null)
				mStartDragItemView.setPressed(false);
			if (isDrag && mDragImageView != null) {
				onStopDrag();
				isDrag = false;
			} else {
				final int currentPosition = getPositionByXY((int) x, (int) y);
				if (currentPosition == mDragPosition) {
					if (canOnClick) {
						onItemClick(currentPosition);
					}
				}
				velocityTracker.computeCurrentVelocity(1000, maxFlingVelocity);
				float velocityX = velocityTracker.getXVelocity(pi);
				float velocityY = velocityTracker.getYVelocity(pi);

				completeMove(-velocityX, -velocityY);
				if (velocityTracker != null) {
					velocityTracker.recycle();
					velocityTracker = null;
				}
			}
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);

			mActivePointerId = INVALID_POINTER;
			break;
		case MotionEventCompat.ACTION_POINTER_DOWN:
			final int index = MotionEventCompat.getActionIndex(event);
			mActivePointerId = MotionEventCompat.getPointerId(event, index);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			// 获取离开屏幕的手指的索引
			int pointerIndexLeave = event.getActionIndex();
			int pointerIdLeave = event.getPointerId(pointerIndexLeave);
			if (mActivePointerId == pointerIdLeave) {
				// 离开屏幕的正是目前的有效手指，此处需要重新调整，并且需要重置VelocityTracker
				int reIndex = pointerIndexLeave == 0 ? 1 : 0;
				mActivePointerId = event.getPointerId(reIndex);
				// 调整触摸位置，防止出现跳动
				mDownX = (int) event.getX(reIndex);
				mDownY = (int) event.getY(reIndex);
				if (velocityTracker != null)
					velocityTracker.clear();
			}
			break;
		}
		return true;
	}

	// 此处的moveBy是根据水平或是垂直排放的方向，
	// 来选择是水平移动还是垂直移动
	public void moveBy(int deltaX, int deltaY) {
		// Log.d("moveBy", "deltaX: " + deltaX + "    deltaY: " + deltaY);
		if (orientation == Direction.horizontal) {
			if (Math.abs(deltaX) >= Math.abs(deltaY))
				scrollBy(deltaX, 0);
		} else {
			if (Math.abs(deltaY) >= Math.abs(deltaX))
				scrollBy(0, deltaY);
		}
	}

	private void completeMove(float velocityX, float velocityY) {
		// Log.d("completeMove", "velocityX:" + velocityX + " velocityY:"
		// + velocityY);
		if (orientation == Direction.horizontal) {
			int mScrollX = getScrollX();
			int maxX = desireWidth - getWidth();
			if (mScrollX > maxX) {
				// 超出了右边界，弹回
				mScroller.startScroll(mScrollX, 0, maxX - mScrollX, 0);
				invalidate();
			} else if (mScrollX < 0) {
				// 超出了左边界，弹回
				mScroller.startScroll(mScrollX, 0, -mScrollX, 0);
				invalidate();
			} else if (Math.abs(velocityX) >= minFlingVelocity && maxX > 0) {
				mScroller.fling(mScrollX, 0, (int) velocityX, 0, 0, maxX, 0, 0);
				invalidate();
			}
		} else {
			int mScrollY = getScrollY();
			int maxY;
			if (getHeight() >= desireHeight) {// 当前没有超出屏幕
				maxY = 0;
			} else {
				maxY = desireHeight - getHeight();
			}
			// Log.d("completeMove", "mScrollY:" + mScrollY + " desireHeight:"
			// + desireHeight + " getHeight():" + getHeight());
			if (mScrollY > maxY) {
				// 超出了下边界，弹回
				mScroller.startScroll(0, mScrollY, 0, maxY - mScrollY);
				invalidate();
			} else if (mScrollY < 0) {
				// 超出了上边界，弹回
				mScroller.startScroll(0, mScrollY, 0, -mScrollY);
				invalidate();
			} else if (Math.abs(velocityY) >= minFlingVelocity && maxY > 0) {
				mScroller.fling(0, mScrollY, 0, (int) velocityY, 0, 0, 0, maxY);
				invalidate();
			}
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (orientation == Direction.horizontal) {
				scrollTo(mScroller.getCurrX(), 0);
				postInvalidate();
			} else {
				scrollTo(0, mScroller.getCurrY());
				postInvalidate();
			}
		}
	}

	public void setAdapter(Adapter adapter, boolean mCustomerEnable) {
		if (adapter instanceof DragGridBaseAdapter) {
			mDragAdapter = (DragGridBaseAdapter) adapter;
		} else {
			return;
		}
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
			removeAllViews();
			scrollTo(0, 0);
		}
		mAdapter = adapter;
		if (mAdapter != null) {

			setCustomerEnable(mCustomerEnable);
			if (!mCustomerEnable) {
				mNumCustomer = mAdapter.getCount() / mNumColumns + 10;// 确保不在有效范围内
				lastAvalableChildCount = 0;// 最后一个不是更多
			}
			mAdapter.registerDataSetObserver(mDataSetObserver);
			for (int i = 0; i < mAdapter.getCount(); i++) {
				final View child = mAdapter.getView(i, null, this);
				addView(child);
			}
		}
	}

	private void dataSetChanged() {
		for (int i = 0; i < getChildCount() && i < mAdapter.getCount(); i++) {
			final View child = getChildAt(i);
			final View newChild = mAdapter.getView(i, child, this);
			if (newChild != child) {
				removeViewAt(i);
				addView(newChild, i);
			}
		}
		for (int i = getChildCount(); i < mAdapter.getCount(); i++) {
			final View child = mAdapter.getView(i, null, this);
			addView(child);
		}
		while (getChildCount() > mAdapter.getCount()) {
			removeViewAt(getChildCount() - 1);
		}

	}

	private Rect getRectByPosition(int position) {

		int row = 0, col = 0, left = 0, top = 0;
		if (position > mNumColumns * mNumCustomer) {
			// position 17... 第6行第一个开始
			position = position + (mNumColumns - 1);
			row = position / mNumColumns;// 判断是第几行
			col = position % mNumColumns;// 判断是第几列
			left = mPaddingLeft + col * (mItemWidth + mItemGap);
			top = mPaddingTop + row * (mItemHeight + mItemGap) + 2
					* mCustomerGap;

		} else {
			row = position / mNumColumns;// 判断是第几行
			col = position % mNumColumns;// 判断是第几列
			left = mPaddingLeft + col * (mItemWidth + mItemGap);
			top = mPaddingTop + row * (mItemHeight + mItemGap);
		}

		return new Rect(left, top, left + mItemWidth, top + mItemHeight);
	}

	private Rect getRectByCustomerView(int position) {
		int row = position / mNumColumns;// 判断是第几行 16/4
		int col = 0;// 判断是第几列
		int left = mPaddingLeft + col;
		int top = mPaddingTop + row * (mItemHeight + mItemGap);
		return new Rect(left, top, left + mCustomerWidth, top + mCustomerHeight);
	}

	private int getPositionByXY(int x, int y) {
		int[] mLocationInScreen = new int[2];
		getLocationOnScreen(mLocationInScreen);// 获取本布局的坐标－
		int[] location = new int[2];
		getChildAt(0).getLocationOnScreen(location);
		int top = mLocationInScreen[1] - location[1];

		int downY = (y + top - mPaddingTop);
		if (mCustomerEnable) {
			if (downY > getChildAt(mNumColumns * mNumCustomer).getTop()) {
				downY = downY - mCustomerGap;
			}
			if (downY > getChildAt(mNumColumns * mNumCustomer).getTop()
					+ mCustomerHeight) {
				downY = downY - mCustomerGap;
			}
		}
		int col = (x - mPaddingLeft) / (mItemWidth + mItemGap);
		int row = downY / (mItemHeight + mItemGap);
		if (x < mPaddingLeft
				|| x >= (mPaddingLeft + col * (mItemWidth + mItemGap) + mItemWidth)
				|| y < mPaddingTop
				|| y >= (mPaddingTop + row * (mItemHeight + mItemGap) + mItemHeight)
				|| col < 0 || col >= mNumColumns || row < 0 || row >= mNumRows) {
			// touch in padding
			return -1;
		}
		int position = -1;
		if (row < mNumCustomer) {
			position = row * mNumColumns + col;
		} else if (row == mNumCustomer) {
			return -1;
		} else {
			position = row * mNumColumns - (mNumColumns - 1) + col;
		}
		if (position < 0 || position >= getChildCount()) {
			// empty item
			return -1;
		}
		return position;
	}

	/**
	 * 是否点击在GridView的item上面
	 * 
	 * @param itemView
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchInItem(View dragView, int x, int y) {
		if (dragView == null) {
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}

		if (y < topOffset || y > topOffset + dragView.getHeight()) {
			return false;
		}
		return true;
	}

	/**
	 * 创建拖动的镜像
	 * 
	 * @param bitmap
	 * @param downX
	 *            按下的点相对父控件的X坐标
	 * @param downY
	 *            按下的点相对父控件的X坐标
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {

		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // 图片之外的其他地方透明
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;

		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left - 5;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight - 5;
		// mWindowLayoutParams.alpha = 0.55f; // 透明度
		mWindowLayoutParams.width = mItemWidth + 10;
		mWindowLayoutParams.height = mItemHeight + 10;

		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(bitmap);

		// mDragImageView.setBackgroundColor(getResources().getColor(
		// android.R.color.darker_gray));
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}

	/**
	 * 拖动item，在里面实现了item镜像的位置更新，item的相互交换以及GridView的自行滚动
	 * 
	 * @param x
	 * @param y
	 */
	private void onDragItem(int moveX, int moveY) {
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left - 5;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight - 5;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // 更新镜像的位置
		onSwapItem(moveX, moveY);

		// GridView自动滚动
		mHandler.post(mScrollRunnable);
	}

	/**
	 * 当moveY的值大于向上滚动的边界值，触发GridView自动向上滚动 当moveY的值小于向下滚动的边界值，触发GridView自动向下滚动
	 * 否则不进行滚动
	 */
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			int scrollY;
			int[] mLocationInScreen = new int[2];
			getLocationOnScreen(mLocationInScreen);// 获取本布局的坐标－
			int[] location = new int[2];
			getChildAt(0).getLocationOnScreen(location);
			int[] lastLocation = new int[2];
			getChildAt(getChildCount() - 1).getLocationOnScreen(lastLocation);

			if (moveY > mUpScrollBorder) {
				if (lastLocation[1] + mItemHeight + mPaddingButtom <= mLocationInScreen[1]
						+ getHeight()) {
					mHandler.removeCallbacks(mScrollRunnable);
					velocityTracker.computeCurrentVelocity(1000,
							maxFlingVelocity);
					float velocityX = velocityTracker
							.getXVelocity(mActivePointerId);
					float velocityY = velocityTracker
							.getYVelocity(mActivePointerId);

					completeMove(-velocityX, -velocityY);
					if (velocityTracker != null) {
						velocityTracker.recycle();
						velocityTracker = null;
					}
					return;
				}
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 800);
			} else if (moveY < mDownScrollBorder) {
				if (location[1] >= mLocationInScreen[1] + mPaddingTop) {
					mHandler.removeCallbacks(mScrollRunnable);
					velocityTracker.computeCurrentVelocity(1000,
							maxFlingVelocity);
					float velocityX = velocityTracker
							.getXVelocity(mActivePointerId);
					float velocityY = velocityTracker
							.getYVelocity(mActivePointerId);

					completeMove(-velocityX, -velocityY);
					if (velocityTracker != null) {
						velocityTracker.recycle();
						velocityTracker = null;
					}
					return;
				}

				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 800);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}
			scrollBy(0, scrollY);
			invalidate();
		}
	};

	/**
	 * 交换item,并且控制item之间的显示与隐藏效果
	 * 
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		// 获取我们手指移动到的那个item的position
		final int tempPosition = getPositionByXY(moveX, moveY);
		if (tempPosition >= avalableChildCount - lastAvalableChildCount) {
			// 最后一个不移动
			return;
		}
		swapItem(tempPosition);
	}

	private void swapItem(final int tempPosition) {
		// 假如tempPosition 改变了并且tempPosition不等于-1,则进行交换
		if (tempPosition != mDragPosition && tempPosition != -1
				&& mAnimationEnd) {
			mDragAdapter.reorderItems(mDragPosition, tempPosition);
			mDragAdapter.setHideItem(tempPosition);

			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);
					animateReorder(mDragPosition, tempPosition);
					mDragPosition = tempPosition;
					return true;
				}
			});
		}
	}

	/**
	 * 停止拖拽我们将之前隐藏的item显示出来，并将镜像移除
	 */
	private void onStopDrag() {
		final View view = getChildAt(mDragPosition);
		if (view != null) {
			// int[] location = new int[2];
			// view.getLocationOnScreen(location);
			// float o = mItemWidth + 10;
			// float n = mItemWidth;
			// List<Animator> resultList = new LinkedList<Animator>();
			// AnimatorSet resultSet = new AnimatorSet();
			//
			// resultList.add(createTranslationAnimations(mDragImageView,
			// mWindowLayoutParams.x, location[0], mWindowLayoutParams.y,
			// location[1] - mStatusHeight));
			// resultSet.playTogether(resultList);
			// resultSet.setDuration(300);
			// resultSet.setInterpolator(new
			// AccelerateDecelerateInterpolator());
			// resultSet.addListener(new AnimatorListenerAdapter() {
			// @Override
			// public void onAnimationStart(Animator animation) {
			// mAnimationEnd = false;
			// }
			//
			// @Override
			// public void onAnimationEnd(Animator animation) {
			// mAnimationEnd = true;
			// }
			// });
			// resultSet.start();

			mAnimationEnd = true;
			view.setVisibility(View.VISIBLE);
			mDragAdapter.setHideItem(-1);
			mDragAdapter.stopDrag();
			removeDragImage();
		}

	}

	/**
	 * 从界面上面移动拖动镜像
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	/**
	 * 创建移动动画
	 * 
	 * @param view
	 * @param startX
	 * @param endX
	 * @param startY
	 * @param endY
	 * @return
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
				startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
				startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	/**
	 * item的交换动画效果
	 * 
	 * @param oldPosition
	 * @param newPosition
	 */
	private void animateReorder(final int oldPosition, final int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = oldPosition; pos < newPosition; pos++) {
				View view = getChildAt(pos);
				if (pos == mNumColumns * mNumCustomer) {
					continue;
				}

				int k = pos;
				if (pos > mNumColumns * mNumCustomer) {
					k = pos + mNumColumns - 1;
				}
				if ((k + 1) % mNumColumns == 0) {
					if (newPosition > mNumColumns * mNumCustomer
							&& mNumColumns * mNumCustomer > oldPosition
							&& pos == mNumColumns * mNumCustomer - 1) {
						resultList.add(createTranslationAnimations(view,
								-view.getWidth() * (mNumColumns - 1), 0, 2
										* view.getHeight() + 2 * mCustomerGap,
								0));
					} else {
						resultList.add(createTranslationAnimations(view,
								-view.getWidth() * (mNumColumns - 1), 0,
								view.getHeight(), 0));
					}
				} else {
					resultList.add(createTranslationAnimations(view,
							view.getWidth(), 0, 0, 0));
				}
			}
		} else {
			for (int pos = oldPosition; pos > newPosition; pos--) {
				View view = getChildAt(pos);
				if (pos == mNumColumns * mNumCustomer) {
					continue;
				}

				int k = pos;
				if (pos > mNumColumns * mNumCustomer) {
					k = pos + mNumColumns - 1;
				}
				if (k % mNumColumns == 0) {
					if (oldPosition > mNumColumns * mNumCustomer
							&& mNumColumns * mNumCustomer > newPosition
							&& pos == mNumColumns * mNumCustomer + 1) {
						resultList.add(createTranslationAnimations(view,
								view.getWidth() * (mNumColumns - 1), 0, -2
										* view.getHeight() - 2 * mCustomerGap,
								0));
					} else {
						resultList.add(createTranslationAnimations(view,
								view.getWidth() * (mNumColumns - 1), 0,
								-view.getHeight(), 0));
					}

				} else {
					resultList.add(createTranslationAnimations(view,
							-view.getWidth(), 0, 0, 0));
				}
			}
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(300);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimationEnd = true;
				if (onItemDelectAndSwapCallback != null) {
					onStopDrag();
					onItemDelectAndSwapCallback.swapDone();

					mNumRows = ((getChildCount() + (mNumColumns - 1)) / mNumColumns);// 判断共几行
					if (mCustomerEnable) {
						if (mNumRows < mNumCustomer) {
							mNumRows = mNumCustomer;
						}
						if ((getChildCount() + (mNumColumns - 1)) % mNumColumns != 0) {
							mNumRows += 1;
						}
					}
					desireHeight = mItemWidth * mNumRows + mItemGap
							* (mNumRows - 1) + mPaddingTop + mPaddingButtom + 2
							* mCustomerGap;
					completeMove(0, 0);

				}

			}
		});
		resultSet.start();
	}

	/**
	 * 获取状态栏的高度
	 * 
	 * @param context
	 * @return
	 */
	private static int getStatusHeight(Context context) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		((Activity) context).getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass
						.getField("status_bar_height").get(localObject)
						.toString());
				statusHeight = context.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	private void onItemClick(int position) {
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(null, getChildAt(position),
					position, position / mNumColumns);
		}
	}

	public int getCustomerPostion() {
		// if (mCustomerEnable) {
		// return mNumColumns * mNumCustomer;
		// } else {
		// return -1;
		// }
		return mNumColumns * mNumCustomer;
	}

	public boolean ismCustomerEnable() {
		return mCustomerEnable;
	}

	public void setCustomerEnable(boolean enable) {
		mCustomerEnable = enable;
	}

	public void onItemDelectAndSwap(int position, int tempPostion,
			onItemDelectAndSwapCallback callback) {
		mDragPosition = position;
		getChildAt(position).setVisibility(View.INVISIBLE);// 隐藏该item
		canOnClick = false;
		onItemDelectAndSwapCallback = callback;

		if (tempPostion == getCustomerPostion()) {
			tempPostion += 1;
		}

		if (position == tempPostion
				&& tempPostion == getAvalableChildCount() - 1) {
			if (onItemDelectAndSwapCallback != null) {
				onStopDrag();
				onItemDelectAndSwapCallback.swapDone();

				mNumRows = ((getChildCount() + (mNumColumns - 1)) / mNumColumns);// 判断共几行
				if (mCustomerEnable) {
					if (mNumRows < mNumCustomer) {
						mNumRows = mNumCustomer;
					}
					if ((getChildCount() + (mNumColumns - 1)) % mNumColumns != 0) {
						mNumRows += 1;
					}
				}
				desireHeight = mItemWidth * mNumRows + mItemGap
						* (mNumRows - 1) + mPaddingTop + mPaddingButtom + 2
						* mCustomerGap;
				completeMove(0, 0);

			}
		} else {
			swapItem(tempPostion);
		}

	}

	public void setOnItemDelectAndSwapCallback(
			onItemDelectAndSwapCallback onItemDelectAndSwapCallback) {
		this.onItemDelectAndSwapCallback = onItemDelectAndSwapCallback;
	}

	public interface onItemDelectAndSwapCallback {
		public void swapDone();
	}

	public int getAvalableChildCount() {
		return avalableChildCount;
	}

	public void setAvalableChildCount(int avalableChildCount) {
		this.avalableChildCount = avalableChildCount;
	}

	public interface DragGridBaseAdapter {
		/**
		 * 重新排列数据
		 *
		 * @param oldPosition
		 * @param newPosition
		 */
		public void reorderItems(int oldPosition, int newPosition);

		/**
		 * 设置某个item隐藏
		 *
		 * @param hidePosition
		 */
		public void setHideItem(int hidePosition);

		/**
		 * 显示某个item的删除
		 *
		 */
		public void startDrag(View view, int postion);

		public void stopDrag();

		public void clearDrag();
	}

}
