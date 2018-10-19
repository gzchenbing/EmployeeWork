package com.kmnfsw.work.sign.view;

import com.kmnfsw.work.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;
/**自定义进度条*/
public class SimpleProgressbar extends ProgressBar {
    private static final String TAG = ".sign.view.SimpleProgressbar";

    private static final int DEFAULT_UNREACHED_COLOR = Color.argb(255, 180, 180, 180);//DEFAULT_UNREACHED_COLOR
	private static final int DEFAULT_REACHED_COLOR = Color.argb(250, 3, 145, 255);
	
    // 进度条默认高，单位为 dp
    public static final int DEFAULT_LINE_HEIGHT = 8;
    // 进度条默认宽，单位为 dp
    public static final int DEFAULT_LINE_WIDTH = 0;

    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 未到达进度条颜色
     */
    private int unreachedColor;
    /**
     * 已到达进度条颜色
     */
    private int reachedColor;
    /**
     * 默认进度条最小的高（不含内边距）
     */
    private int minLineHeight;
    /**
     * 默认进度条最小的宽（不含内边距）
     */
    private int minLineWidth;
    /**
     * 实际使用的进度条的高（不含内边距）
     */
    private int lineHeight;
    /**
     * 实际使用的进度条的宽（不含内边距）
     */
    private int lineWidth;

    public SimpleProgressbar(Context context) {
        //        super(context);
        this(context, null);
    }

    public SimpleProgressbar(Context context, AttributeSet attrs) {
        //        super(context, attrs);
        this(context, attrs, 0);
    }

    public SimpleProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        unreachedColor = DEFAULT_UNREACHED_COLOR;
        reachedColor = DEFAULT_REACHED_COLOR;
        minLineHeight = dp2px(DEFAULT_LINE_HEIGHT);
        minLineWidth = dp2px(DEFAULT_LINE_WIDTH);

        obtainStyledAttributes(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int desiredWidth = minLineWidth + getPaddingLeft() + getPaddingRight();
        int desiredHeight = minLineHeight + getPaddingTop() + getPaddingBottom();

        int width;
        int height;

        if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(widthSize, desiredWidth);
        } else {
            width = Math.max(widthSize, desiredWidth);
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(heightSize, desiredHeight);
        } else {
            height = Math.max(heightSize, desiredHeight);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //        super.onDraw(canvas);
        // 获取画布的宽高
        int width = getWidth();
        int height = getHeight();
        // 获取进度条的实际宽高
        int lineWidth = width - getPaddingLeft() - getPaddingRight();
        int lineHeight = height - getPaddingTop() - getPaddingBottom();
        // 获取当前进度
        float ratio = getProgress() * 1.0f / getMax();
        // 获取未完成进度大小
        int unreachedWidth = (int) (lineWidth * (1 - ratio));
        // 获取已完成进度大小
        int reachedWidth = lineWidth - unreachedWidth;
        // 绘制已完成进度条，设置画笔颜色和大小
        paint.setColor(reachedColor);
        paint.setStrokeWidth(lineHeight);
        // 计算已完成进度条起点和终点的坐标
        int startX = getPaddingLeft();
        int startY = getHeight() / 2;
        int stopX = startX + reachedWidth;
        int stopY = startY;
        // 画线
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        // 设置画笔颜色
        paint.setColor(unreachedColor);

        startX = getPaddingLeft() + reachedWidth;
        stopX = width - getPaddingRight();
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    private void obtainStyledAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SimpleProgressbar, defStyleAttr, 0);

        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SimpleProgressbar_reachedColor:
                    reachedColor = a.getColor(attr, DEFAULT_REACHED_COLOR);
                    break;
                case R.styleable.SimpleProgressbar_unreachedColor:
                    unreachedColor = a.getColor(attr, DEFAULT_UNREACHED_COLOR);
                    break;
            }
        }

        a.recycle();
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }
}