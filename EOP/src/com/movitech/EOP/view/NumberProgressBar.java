package com.movitech.EOP.view;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.movitech.EOP.Test.R;


/**
 * Created by daimajia on 14-4-30.
 */
public class NumberProgressBar extends View {
    /**
     * 进度值最大值
     */
    private int mMaxProgress = 100;

    /**
     * Current progress, can not exceed the max progress.
     * 当前进度值，不能超过进度值最大值
     */
    private int mCurrentProgress = 0;

    /**
     * The progress area bar color.
     * 当前进度值文本之前的进度条颜色
     */
    private int mReachedBarColor;

    /**
     * The bar unreached area color.
     * 当前进度值文本之后的进度条颜色
     */
    private int mUnreachedBarColor;

    /**
     * The progress text color.
     * 当前进度值文本的颜色
     */
    private int mTextColor;

    /**
     * The progress text size.
     * 当前进度值文本的字体大小
     */
    private float mTextSize;

    /**
     * The height of the reached area.
     * 当前进度值文本之前的进度条的高度
     */
    private float mReachedBarHeight;

    /**
     * The height of the unreached area.
     * 当前进度值文本之后的进度条的高度
     */
    private float mUnreachedBarHeight;

    /**
     * The suffix of the number.
     * 当前进度值的百分比后缀
     */
    private String mSuffix = "%";

    /**
     * The prefix.
     * 当前进度值的百分比前缀
     */
    private String mPrefix = "";

    //当前进度值文本的默认颜色
    private final int default_text_color = Color.rgb(66, 145, 241);
    //当前进度值文本的字体大小
    private final float default_text_size;

    //当前进度值之前的默认进度条颜色
    private final int default_reached_color = Color.rgb(66, 145, 241);
    //当前进度值之后的默认进度条颜色
    private final int default_unreached_color = Color.rgb(204, 204, 204);

    //当前进度值之前文本的默认间距
    private final float default_progress_text_offset;

    //当前进度值文本之前的进度条的默认高度
    private final float default_reached_bar_height;
    //当前进度值文本之后的进度条的默认高度
    private final float default_unreached_bar_height;

    /**
     * For save and restore instance of progressbar.
     */
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_REACHED_BAR_HEIGHT = "reached_bar_height";
    private static final String INSTANCE_REACHED_BAR_COLOR = "reached_bar_color";
    private static final String INSTANCE_UNREACHED_BAR_HEIGHT = "unreached_bar_height";
    private static final String INSTANCE_UNREACHED_BAR_COLOR = "unreached_bar_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";
    private static final String INSTANCE_TEXT_VISIBILITY = "text_visibility";
    //默认显示当前进度值文本   0为显示，1为不显示
    private static final int PROGRESS_TEXT_VISIBLE = 0;


    /**
     * The width of the text that to be drawn.
     * 要绘制的当前进度值的文本的宽度
     */
    private float mDrawTextWidth;

    /**
     * The drawn text start.
     * 要绘制的当前进度值的文本的起始位置
     */
    private float mDrawTextStart;

    /**
     * The drawn text end.
     * 要绘制的当前进度值的文本的结束位置
     */
    private float mDrawTextEnd;

    /**
     * The text that to be drawn in onDraw().
     * 要绘制的当前进度值的文本
     */
    private String mCurrentDrawText;

    /**
     * The Paint of the reached area.
     * 绘制当前进度值文本之前的进度条的画笔
     */
    private Paint mReachedBarPaint;
    /**
     * The Paint of the unreached area.
     * 绘制当前进度值文本之后的进度条的画笔
     */
    private Paint mUnreachedBarPaint;
    /**
     * The Paint of the progress text.
     * 绘制当前进度值文本的的画笔
     */
    private Paint mTextPaint;

    /**
     * Unreached bar area to draw rect.
     * 当前进度值文本之后的进度条（长方形）
     */
    private RectF mUnreachedRectF = new RectF(0, 0, 0, 0);
    /**
     * Reached bar area rect.
     * 当前进度值之前文本的进度条（长方形）
     */
    private RectF mReachedRectF = new RectF(0, 0, 0, 0);

    /**
     * The progress text offset.
     * 当前进度值之前文本的间距
     */
    private float mOffset;

    /**
     * Determine if need to draw unreached area.
     * 是否绘制当前进度值之后的进度条
     */
    private boolean mDrawUnreachedBar = true;
    /**
     * 是否绘制当前进度值之前的进度条
     */
    private boolean mDrawReachedBar = true;
    /**
     * 是否绘制当前进度值文本
     */
    private boolean mIfDrawText = true;

    /**
     * Listener
     */
    private OnProgressBarListener mListener;

    public enum ProgressTextVisibility {
        Visible, Invisible
    }

    public NumberProgressBar(Context context) {
        this(context, null);
    }

    public NumberProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.numberProgressBarStyle);
    }

    public NumberProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        default_reached_bar_height = dp2px(1.5f);
        default_unreached_bar_height = dp2px(1.0f);
        default_text_size = sp2px(10);
        default_progress_text_offset = dp2px(3.0f);

        //获取自定义属性
        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberProgressBar,
                defStyleAttr, 0);
        mReachedBarColor = attributes.getColor(R.styleable.NumberProgressBar_progress_reached_color, default_reached_color);
        mUnreachedBarColor = attributes.getColor(R.styleable.NumberProgressBar_progress_unreached_color, default_unreached_color);
        mTextColor = attributes.getColor(R.styleable.NumberProgressBar_progress_text_color, default_text_color);
        mTextSize = attributes.getDimension(R.styleable.NumberProgressBar_progress_text_size, default_text_size);

        mReachedBarHeight = attributes.getDimension(R.styleable.NumberProgressBar_progress_reached_bar_height, default_reached_bar_height);
        mUnreachedBarHeight = attributes.getDimension(R.styleable.NumberProgressBar_progress_unreached_bar_height, default_unreached_bar_height);
        mOffset = attributes.getDimension(R.styleable.NumberProgressBar_progress_text_offset, default_progress_text_offset);

        int textVisible = attributes.getInt(R.styleable.NumberProgressBar_progress_text_visibility, PROGRESS_TEXT_VISIBLE);

        if (textVisible != PROGRESS_TEXT_VISIBLE) {
            mIfDrawText = false;
        }

        setProgress(attributes.getInt(R.styleable.NumberProgressBar_progress_current, 0));
        setMax(attributes.getInt(R.styleable.NumberProgressBar_progress_max, 100));
        //回收 TypedArray,用于后续调用时可复用之。回收到TypedArrayPool池中，以备后用
        attributes.recycle();
        initializePainters();
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) mTextSize;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max((int) mTextSize, Math.max((int) mReachedBarHeight, (int) mUnreachedBarHeight));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * MeasureSpec参数的值为int型，分为高32位和低16为，
         * 高32位保存的是specMode，低16位表示specSize，
         *
         * specMode分三种：
         1、MeasureSpec.UNSPECIFIED,父视图不对子视图施加任何限制，子视图可以得到任意想要的大小；
         2、MeasureSpec.EXACTLY，父视图希望子视图的大小是specSize中指定的大小；
         3、MeasureSpec.AT_MOST，子视图的大小最多是specSize中的大小。
         */
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    private int measure(int measureSpec, boolean isWidth) {

        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        /**
         父决定子的确切大小，子被限定在给定的边界里，忽略本身想要的大小。
         (当设置width或height为match_parent时，模式为EXACTLY，因为子view会占据剩余容器的空间，所以它大小是确定的)
         */
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            /**
             *子最大可以达到的指定大小
             * (当设置为wrap_content时，模式为AT_MOST, 表示子view的大小最多是多少，这样子view会根据这个上限来设置自己的尺寸)
             */
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //如果要绘制当前进度值文本
        if (mIfDrawText) {
            calculateDrawRectF();
        }else {
            calculateDrawRectFWithoutProgressText();
        }

        //如果要绘制当前进度值之前的进度条
        if (mDrawReachedBar) {
            canvas.drawRect(mReachedRectF, mReachedBarPaint);
        }

        //如果要绘制当前进度值之后的进度条
        if (mDrawUnreachedBar) {
            canvas.drawRect(mUnreachedRectF, mUnreachedBarPaint);
        }

        //绘制当前进度值文本
        if (mIfDrawText)
            canvas.drawText(mCurrentDrawText, mDrawTextStart, mDrawTextEnd, mTextPaint);
    }

    /**
     * 初始化画笔
     */
    private void initializePainters() {
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setColor(mReachedBarColor);

        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setColor(mUnreachedBarColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
    }

    /**
     * 计算不要绘制当前进度值文本时 图形的各个属性
     */
    private void calculateDrawRectFWithoutProgressText() {
        //当前进度值不画

        //当前进度值之前的进度条（长方形）的属性
        mReachedRectF.left = getPaddingLeft();
        mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
        mReachedRectF.right =
                (getWidth() - getPaddingLeft() - getPaddingRight()) / (getMax() * 1.0f) * getProgress()
                        + getPaddingLeft();
        mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;

        //当前进度值之后的进度条（长方形）的属性
        mUnreachedRectF.left = mReachedRectF.right;
        mUnreachedRectF.right = getWidth() - getPaddingRight();
        mUnreachedRectF.top = getHeight() / 2.0f + -mUnreachedBarHeight / 2.0f;
        mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight / 2.0f;
    }

    /**
     * 计算要绘制当前进度值文本时 图形的各个属性
     */
    private void calculateDrawRectF() {
        //要绘制的当前进度值的文本
        mCurrentDrawText = String.format("%d", getProgress() * 100 / getMax());
        mCurrentDrawText = mPrefix + mCurrentDrawText + mSuffix;
        //要绘制的当前进度值的文本的宽度
        mDrawTextWidth = mTextPaint.measureText(mCurrentDrawText);
        //如果当前进度值为0，则不绘制当前进度值之前的进度条
        if (getProgress() == 0) {
            mDrawReachedBar = false;
            mDrawTextStart = getPaddingLeft();
        }
        //否则绘制当前进度值文本之前的进度条
        else {
            mDrawReachedBar = true;
            //当前进度值文本之前的进度条（长方形）的属性
            mReachedRectF.left = getPaddingLeft();
            mReachedRectF.top = getHeight() / 2.0f - mReachedBarHeight / 2.0f;
            mReachedRectF.right= (getWidth() - getPaddingLeft() - getPaddingRight()) / (getMax() * 1.0f) * getProgress()
                    - mOffset + getPaddingLeft();
            mReachedRectF.bottom = getHeight() / 2.0f + mReachedBarHeight / 2.0f;
            //当前进度值的文本的起始位置
            mDrawTextStart = (mReachedRectF.right + mOffset);
        }
        //当前进度值的文本的结束位置
        mDrawTextEnd = (int) ((getHeight() / 2.0f) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2.0f));
        //如果画不下当前进度值的文本了，就重新计算下当前进度值的文本的起始位置和当前进度值之前的进度条（长方形）的右边
        if ((mDrawTextStart + mDrawTextWidth) >= getWidth() - getPaddingRight()) {
            mDrawTextStart = getWidth() - getPaddingRight() - mDrawTextWidth;
            mReachedRectF.right = mDrawTextStart - mOffset;
        }

        //当前进度值文本之后的进度条的起始位置
        float unreachedBarStart = mDrawTextStart + mDrawTextWidth + mOffset;
        //如果画不下进度值文本之后的进度条了，就不画进度值之后的进度条
        if (unreachedBarStart >= getWidth() - getPaddingRight()) {
            mDrawUnreachedBar = false;
        } else {
            mDrawUnreachedBar = true;
            //当前进度值文本之后的进度条（长方形）的属性
            mUnreachedRectF.left = unreachedBarStart;
            mUnreachedRectF.right = getWidth() - getPaddingRight();
            mUnreachedRectF.top = getHeight() / 2.0f + -mUnreachedBarHeight / 2.0f;
            mUnreachedRectF.bottom = getHeight() / 2.0f + mUnreachedBarHeight / 2.0f;
        }
    }

    /**
     * Get progress text color.
     * 获取当前进度值文本的颜色
     * @return progress text color.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Get progress text size.
     * 获取当前进度值文本的字体大小
     * @return progress text size.
     */
    public float getProgressTextSize() {
        return mTextSize;
    }

    /**
     * 获取当前进度值文本之后的进度条颜色
     */
    public int getUnreachedBarColor() {
        return mUnreachedBarColor;
    }

    /**
     * 获取当前进度值文本之前的进度条颜色
     */
    public int getReachedBarColor() {
        return mReachedBarColor;
    }
    /**
     * 获取进度条的当前进度值
     */
    public int getProgress() {
        return mCurrentProgress;
    }
    /**
     * 获取进度条的最大值
     */
    public int getMax() {
        return mMaxProgress;
    }
    /**
     * 获取当前进度值文本之前的进度条的高度
     */
    public float getReachedBarHeight() {
        return mReachedBarHeight;
    }
    /**
     * 获取当前进度值文本之后的进度条的高度
     */
    public float getUnreachedBarHeight() {
        return mUnreachedBarHeight;
    }
    /**
     * 设置当前进度值文本的字体大小
     * @param textSize 当前进度值文本的字体大小
     */
    public void setProgressTextSize(float textSize) {
        this.mTextSize = textSize;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }
    /**
     * 设置当前进度值文本的颜色
     * @param textColor 当前进度值文本的颜色
     */
    public void setProgressTextColor(int textColor) {
        this.mTextColor = textColor;
        mTextPaint.setColor(mTextColor);
        invalidate();
    }
    /**
     * 设置当前进度值文本之后的进度条颜色
     * @param barColor 当前进度值文本之后的进度条颜色
     */
    public void setUnreachedBarColor(int barColor) {
        this.mUnreachedBarColor = barColor;
        mUnreachedBarPaint.setColor(mUnreachedBarColor);
        invalidate();
    }
    /**
     * 设置当前进度值文本之前的进度条颜色
     * @param progressColor 当前进度值文本之前的进度条颜色
     */
    public void setReachedBarColor(int progressColor) {
        this.mReachedBarColor = progressColor;
        mReachedBarPaint.setColor(mReachedBarColor);
        invalidate();
    }
    /**
     * 设置当前进度值文本之前的进度条的高度
     * @param height 当前进度值文本之前的进度条的高度
     */
    public void setReachedBarHeight(float height) {
        mReachedBarHeight = height;
    }
    /**
     * 设置当前进度值文本之后的进度条的高度
     * @param height 当前进度值文本之后的进度条的高度
     */
    public void setUnreachedBarHeight(float height) {
        mUnreachedBarHeight = height;
    }

    /**
     * 设置进度值的最大值
     * @param maxProgress 进度值的最大值
     */
    public void setMax(int maxProgress) {
        if (maxProgress > 0) {
            this.mMaxProgress = maxProgress;
            invalidate();
        }
    }
    /**
     * 设置当前进度值文本的后缀
     * @param suffix 当前进度值文本的后缀
     */
    public void setSuffix(String suffix) {
        if (suffix == null) {
            mSuffix = "";
        } else {
            mSuffix = suffix;
        }
    }
    /**
     * 获取当前进度值文本的后缀
     */
    public String getSuffix() {
        return mSuffix;
    }
    /**
     * 设置当前进度值文本的前缀
     * @param prefix 当前进度值文本的前缀
     */
    public void setPrefix(String prefix) {
        if (prefix == null)
            mPrefix = "";
        else {
            mPrefix = prefix;
        }
    }
    /**
     * 获取当前进度值文本的前缀
     */
    public String getPrefix() {
        return mPrefix;
    }

    /**
     * 设置进度条的当前进度值增加
     * @param by 增加多少
     */
    public void incrementProgressBy(int by) {
        if (by > 0) {
            setProgress(getProgress() + by);
        }
        if (mListener != null) {
            //回调onProgressChange()方法来处理进度值变化后的事件
            mListener.onProgressChange(getProgress(), getMax());
        }
    }

    /**
     * 设置当前进度值
     *
     * @param progress 当前进度值
     */
    public void setProgress(int progress) {
        if (progress <= getMax() && progress >= 0) {
            this.mCurrentProgress = progress;
            invalidate();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getProgressTextSize());
        bundle.putFloat(INSTANCE_REACHED_BAR_HEIGHT, getReachedBarHeight());
        bundle.putFloat(INSTANCE_UNREACHED_BAR_HEIGHT, getUnreachedBarHeight());
        bundle.putInt(INSTANCE_REACHED_BAR_COLOR, getReachedBarColor());
        bundle.putInt(INSTANCE_UNREACHED_BAR_COLOR, getUnreachedBarColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getSuffix());
        bundle.putString(INSTANCE_PREFIX, getPrefix());
        bundle.putBoolean(INSTANCE_TEXT_VISIBILITY, getProgressTextVisibility());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            mTextColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            mTextSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            mReachedBarHeight = bundle.getFloat(INSTANCE_REACHED_BAR_HEIGHT);
            mUnreachedBarHeight = bundle.getFloat(INSTANCE_UNREACHED_BAR_HEIGHT);
            mReachedBarColor = bundle.getInt(INSTANCE_REACHED_BAR_COLOR);
            mUnreachedBarColor = bundle.getInt(INSTANCE_UNREACHED_BAR_COLOR);
            initializePainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            setPrefix(bundle.getString(INSTANCE_PREFIX));
            setSuffix(bundle.getString(INSTANCE_SUFFIX));
            setProgressTextVisibility(bundle.getBoolean(INSTANCE_TEXT_VISIBILITY) ? ProgressTextVisibility.Visible : ProgressTextVisibility.Invisible);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * dp转px
     */
    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
    /**
     * sp转px
     */
    public float sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
    /**
     * 设置是否绘制当前进度值文本
     */
    public void setProgressTextVisibility(ProgressTextVisibility visibility) {
        mIfDrawText = visibility == ProgressTextVisibility.Visible;
        invalidate();
    }
    /**
     * 获取是否绘制当前进度值文本
     */
    public boolean getProgressTextVisibility() {
        return mIfDrawText;
    }
    /**
     * 设置进度值变化时的监听器
     */
    public void setOnProgressBarListener(OnProgressBarListener listener) {
        mListener = listener;
    }
}
