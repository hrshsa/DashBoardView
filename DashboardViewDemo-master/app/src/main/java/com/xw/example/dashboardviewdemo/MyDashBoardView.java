package com.xw.example.dashboardviewdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HuangRuiShu on 2016/10/10.
 */
public class MyDashBoardView extends View {

    private int mRadius;         //仪表盘半径
    private int mStartAngle;     //开始的角度，默认180
    private int mSweepAngle;     //扫过的角度，默认180
    private int mLongFingerLength;    //长指针的长度
    private int mTextLength;
    private int mShortFingerLength;    //短指针的长度
    private int mBgColor;    //背景颜色

    private int mCircleRadius;     //中心圆半径
    private int mLongFingerCount;     //长指针的数量
    private int mShortFingerCount;     //短指针的数量
    private int mSliceCountInOneBigSlice; // 划分一大份长的小份数

    private float mRealAngle;          //指针真实值
    private float mRealValue;          //真实值
    private Paint mPaint;
    private Paint mLongPaint;        //长指针颜色
    private Paint mShortPaint;       //短指针颜色
    private Paint mBgPaint;       //背景颜色
    private Paint mArcPaint;         //表盘颜色
    private RectF mArcRectf;      //包含最外层弧形的矩形
    private Rect mTextRectf;
    private float mCenterY = 0.0f;
    private float mCenterX = 0.0f;
    private float mBigSliceAngle; // 大刻度等分角度
    private float mSmallSliceAngle; // 小刻度等分角度


    private int mViewWidth; // 控件宽度
    private int mViewHeight; // 控件高度
    private Path mPath;


    private List<String> mScaleList = new ArrayList<>();
    private int mMinValue;
    private int mMaxValue;

    public MyDashBoardView(Context context) {
        this(context, null);
    }

    public MyDashBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDashBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mLongPaint = new Paint();
        mLongPaint.setAntiAlias(true);
        mBgPaint = new Paint();
        mLongPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setAntiAlias(true);
        mShortPaint = new Paint();
        mArcPaint = new Paint();
        mShortPaint.setAntiAlias(true);
        mShortPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyDashBoardView);
        mRadius = ta.getDimensionPixelSize(R.styleable.MyDashBoardView_MyRadius, dpToPx(90));
        mStartAngle = ta.getInteger(R.styleable.MyDashBoardView_MyStartAngle, 180);
        mSliceCountInOneBigSlice = ta.getInteger(R.styleable.DashboardView_sliceCountInOneBigSlice, 5);
        if (mStartAngle != 180)
            mStartAngle += 180;
        mSweepAngle = ta.getInteger(R.styleable.MyDashBoardView_MySweepAngle, 180);
        mLongFingerCount = ta.getInteger(R.styleable.MyDashBoardView_MyBigSliceCount, 5);
        mCircleRadius = ta.getInteger(R.styleable.MyDashBoardView_MyCircleRadius, mRadius / 17);
        mRealValue = ta.getFloat(R.styleable.MyDashBoardView_MyRealTimeValue, 40.0f);
        int longColor = ta.getColor(R.styleable.MyDashBoardView_LongerFingerColor, Color.WHITE);
        mLongPaint.setColor(longColor);
        int shortColor = ta.getColor(R.styleable.MyDashBoardView_ShortFingerColor, Color.WHITE);
        mShortPaint.setColor(shortColor);
        int arcColor = ta.getColor(R.styleable.MyDashBoardView_MyArcColor, Color.WHITE);
        mArcPaint.setColor(arcColor);
        mMinValue = ta.getInteger(R.styleable.MyDashBoardView_MyMinValue, 0);
        mMaxValue = ta.getInteger(R.styleable.MyDashBoardView_MyMaxValue, 100);
        mBgColor = ta.getInteger(R.styleable.MyDashBoardView_MyBgColor, Color.BLUE);
        ta.recycle();

        mBigSliceAngle = mSweepAngle / (float) mLongFingerCount;
        mSmallSliceAngle = mBigSliceAngle / mSliceCountInOneBigSlice;
        mShortFingerCount = mLongFingerCount * 5;
        mShortFingerLength = mRadius - dpToPx(10);
        mLongFingerLength = mShortFingerLength - dpToPx(8);
        mTextLength = mLongFingerLength - dpToPx(2);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mViewWidth = mRadius * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2;
        if ((mStartAngle <= 90 && mStartAngle + mSweepAngle >= 90) ||
                (mStartAngle <= 270 && mStartAngle + mSweepAngle >= 270)) {
            mViewHeight = mRadius * 2 + getPaddingTop() + getPaddingBottom() + dpToPx(2) * 2;
        } else {
            float[] point1 = getCoordinatePoint(mRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(mRadius, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[1]), Math.abs(point2[1]));
            mViewHeight = (int) (max * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2);
        }
        mCenterX = mViewWidth / 2;
        mCenterY = mViewHeight / 2;
        mArcRectf = new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);
        mTextRectf = new Rect();
        mPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setColor(mBgColor);
        mPath = new Path();
        getMeasureNumbers();
        mRealAngle = getAngleFromResult(mRealValue);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = resolveSize(mViewWidth, widthMeasureSpec);
        if (mStartAngle >= 0 && mStartAngle + mSweepAngle <= 180) {
            mViewHeight = mRadius + mCircleRadius + dpToPx(10) +
                    getPaddingTop() + getPaddingBottom();
        } else {
            float[] point1 = getCoordinatePoint(mRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(mRadius, mStartAngle + mSweepAngle);
            float maxY = Math.max(Math.abs(point1[1]) - mCenterY, Math.abs(point2[1]) - mCenterY);
            float f = mCircleRadius + dpToPx(2) + dpToPx(25);
            float max = Math.max(maxY, f);
            mViewHeight = (int) (max + mRadius + getPaddingTop() + getPaddingBottom() + dpToPx(2) * 2);
        }
        int height = resolveSize(mViewHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);

    }

    private void getMeasureNumbers() {
        String strings = "";
        for (int i = 0; i <= mLongFingerCount; i++) {
            if (i == 0) {
                strings = String.valueOf(mMinValue);
            } else if (i == mLongFingerCount) {
                strings = String.valueOf(mMaxValue);
            } else {
                strings = String.valueOf(((mMaxValue - mMinValue) / mLongFingerCount) * i);
            }
            mScaleList.add(strings);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBgColor);
        mPaint.setStyle(Paint.Style.STROKE);
        //画最外层的弧形
        canvas.drawArc(mArcRectf, mStartAngle, mSweepAngle, false, mArcPaint);
        //长指针
        mPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i <= mLongFingerCount; i++) {
            float angle = i * mBigSliceAngle + mStartAngle;
            float[] point1 = getCoordinatePoint(mRadius, angle);
            float[] point2 = getCoordinatePoint(mLongFingerLength, angle);
            canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mLongPaint);
            mPaint.setTextSize(spToPx(17));
            if (angle % 360 > 135 && angle % 360 < 215) {
                mPaint.setTextAlign(Paint.Align.LEFT);
            } else if ((angle % 360 >= 0 && angle % 360 < 45) || (angle % 360 > 325 && angle % 360 <= 360)) {
                mPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                mPaint.setTextAlign(Paint.Align.CENTER);
            }
            String text = mScaleList.get(i);
            mPaint.getTextBounds(text, 0, text.length(), mTextRectf);
            float[] point3 = getCoordinatePoint(mTextLength, angle);
            if (i == 0 || i == mLongFingerCount) {
                canvas.drawText(text, point3[0], point3[1] + (mTextRectf.height() / 2), mPaint);
            } else {
                canvas.drawText(text, point3[0], point3[1] + mTextRectf.height(), mPaint);
            }
        }
        //短指针
        for (int j = 0; j <= mShortFingerCount; j++) {
            if (j % 5 != 0) {
                float angle = j * mSmallSliceAngle + mStartAngle;
                float[] point1 = getCoordinatePoint(mRadius, angle);
                float[] point2 = getCoordinatePoint(mShortFingerLength, angle);
                canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mShortPaint);
            }
        }

        //三角形指针
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#e4e9e9"));
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius, mPaint);
        float[] point1 = getCoordinatePoint(mCircleRadius / 3 * 2, mRealAngle + 90);
        mPath.moveTo(point1[0], point1[1]);
        float[] point2 = getCoordinatePoint(mCircleRadius / 3 * 2, mRealAngle - 90);
        mPath.lineTo(point2[0], point2[1]);
        float[] point3 = getCoordinatePoint(mRadius / 3 * 2, mRealAngle);
        mPath.lineTo(point3[0], point3[1]);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
        //读数
        canvas.drawText(mRealValue + "", mCenterX + dpToPx(12), mCenterY + mCircleRadius + dpToPx(20), mPaint);
    }

    /**
     * 依圆心坐标，半径，扇形角度，计算出扇形射线与圆弧交叉点的xy坐标
     */
    public float[] getCoordinatePoint(int radius, float cirAngle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(cirAngle); //将角度转换为弧度
        if (cirAngle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (cirAngle > 90 && cirAngle < 180) {
            arcAngle = Math.PI * (180 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (cirAngle > 180 && cirAngle < 270) {
            arcAngle = Math.PI * (cirAngle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (cirAngle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 通过数值得到角度位置
     */
    private float getAngleFromResult(float result) {
        if (result > mMaxValue)
            return mMaxValue;
        return mSweepAngle * (result - mMinValue) / (mMaxValue - mMinValue) + mStartAngle;
    }

    public void setmRealAngle(float angle) {
        mRealAngle = angle;
        mPath = new Path();
        invalidate();
    }

    public float getmRealAngle(float angle) {
        return mRealAngle;
    }
}
