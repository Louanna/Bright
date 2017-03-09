package com.movit.platform.framework.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.movit.platform.common.R;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class WaterMarkView extends View {
    private String mWaterString = ""; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private List<Path> paths;

    public WaterMarkView(Context context) {
        super(context);
        init(null, 0);
    }

    public WaterMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WaterMarkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.WaterMarkView, defStyle, 0);

        mWaterString = a.getString(
                R.styleable.WaterMarkView_waterString);
        mExampleColor = a.getColor(
                R.styleable.WaterMarkView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.WaterMarkView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.WaterMarkView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.WaterMarkView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setAntiAlias(true);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        paths = new ArrayList<>();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mWaterString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        double unit = Math.ceil(mTextWidth / 5) + 20;
        int xCount = (int) Math.ceil(getWidth() / (unit * 3));
        int yCount = (int) Math.ceil(getHeight() / (unit * 4));

        paths.clear();
        for (int i = 0; i < xCount; i++) {
            for (int j = 0; j < yCount; j++) {
                Path path = new Path();
                path.moveTo(Float.valueOf(Double.valueOf(i * unit * 3).toString()), Float.valueOf(Double.valueOf(j *
                        unit * 4).toString()));
                path.lineTo(Float.valueOf(Double.valueOf((i + 1) * unit * 3).toString()), Float.valueOf(Double
                        .valueOf((j + 1) * unit * 4).toString()));
                paths.add(path);
            }
        }

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        for (Path path : paths) {
            canvas.drawPath(path, mTextPaint);
            canvas.drawTextOnPath(mWaterString, path,
                    40, 40,
                    mTextPaint);
        }
        // Draw the text.
        //        canvas.drawText(mExampleString,
        //                paddingLeft + (contentWidth - mTextWidth) / 2,
        //                paddingTop + (contentHeight + mTextHeight) / 2,
        //                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getWaterString() {
        return mWaterString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param waterString The example string attribute value to use.
     */
    public void setWaterString(String waterString) {
        mWaterString = waterString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
