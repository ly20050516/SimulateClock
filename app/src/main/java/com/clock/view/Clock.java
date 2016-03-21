package com.clock.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.clock.ly.clock.R;

import java.util.Calendar;
import java.util.Timer;

/**
 * Created by LY on 3/18/2016.
 */
public class Clock extends View {

    private static final float DEFAULT_RADIUS = 300.0f;
    private static final String[] DEGREE_VALUE = {"12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
    Context mContext = null;
    private Paint mPaintCircle;
    private Paint mPaintIntegralPoint;
    private Paint mPaintScale;
    private Paint mPaintHour;
    private Paint mPaintMinute;
    private Paint mPaintSecond;

    //attrs
    float mRadius;
    boolean mMoveAble;

    //clock handler
    HandlerThread mClockThread = new HandlerThread("Clock Tick");

    ClockHandler mClockHandler = null;

    float mCircleX;
    float mCircleY;
    int mLastX;
    int mLastY;

    private int mClockHour;
    private int mClockMinute;
    private int mClockSecond;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public Clock(Context context) {
        super(context);
        initAttrs(context, null, null);
        initView(context);
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p/>
     * <p/>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public Clock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs, R.styleable.Clock);
        initView(context);

    }


    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    public Clock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, R.styleable.Clock);
        initView(context);
    }

    private void initAttrs(Context context, AttributeSet attrs, int[] clock) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, clock);
            mRadius = ta.getFloat(R.styleable.Clock_radius, DEFAULT_RADIUS);
            mMoveAble = ta.getBoolean(R.styleable.Clock_moveable, true);
            ta.recycle();
        } else {
            mRadius = DEFAULT_RADIUS;
            mMoveAble = true;
        }

    }

    private void initView(Context context) {
        mContext = context;

        mClockThread.start();
        mClockHandler = new ClockHandler(mClockThread.getLooper());
        mClockHandler.sendEmptyMessage(0);

        mPaintCircle = new Paint();
        mPaintCircle.setColor(Color.RED);
        mPaintCircle.setStrokeWidth(8);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setTextSize(30);

        mPaintIntegralPoint = new Paint();
        mPaintIntegralPoint.setColor(Color.RED);
        mPaintIntegralPoint.setStrokeWidth(5);
        mPaintIntegralPoint.setStyle(Paint.Style.FILL);
        mPaintIntegralPoint.setTextSize(30);

        mPaintScale = new Paint();
        mPaintScale.setColor(Color.RED);
        mPaintScale.setStrokeWidth(3);
        mPaintScale.setStyle(Paint.Style.FILL);
        mPaintScale.setTextSize(16);

        mPaintHour = new Paint();
        mPaintHour.setColor(Color.RED);
        mPaintHour.setStrokeWidth(20);
        mPaintHour.setStyle(Paint.Style.FILL);


        mPaintMinute = new Paint();
        mPaintMinute.setColor(Color.RED);
        mPaintMinute.setStrokeWidth(10);
        mPaintMinute.setStyle(Paint.Style.FILL);

        mPaintSecond = new Paint();
        mPaintSecond.setColor(Color.RED);
        mPaintSecond.setStrokeWidth(5);
        mPaintSecond.setStyle(Paint.Style.FILL);

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                Log.d("Liu", "onTouchEvent mLastX " + mLastX + ",mLastY" + mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int offx = (int) (event.getX() - mLastX);
                int offy = (int) (event.getY() - mLastY);
                offsetLeftAndRight(offx);
                offsetTopAndBottom(offy);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return mMoveAble;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSize < heightSize) {
            // vertical
            heightSize = widthSize;
            if (widthMode != MeasureSpec.UNSPECIFIED) {
                widthSize = heightSize = (int) mRadius * 2;
            }
        } else {
            // horizontal
            widthSize = heightSize;
            if (heightMode != MeasureSpec.UNSPECIFIED) {
                widthSize = heightSize = (int) mRadius * 2;
            }
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        mCircleX = getWidth() / 2;
        mCircleY = getHeight() / 2;

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mCircleX, mCircleY, mRadius, mPaintCircle);
        canvas.drawCircle(mCircleX, mCircleY, mRadius + 20, mPaintCircle);

        for (int i = 0; i < 12; i++) {

            if (i % 3 == 0) {
                canvas.drawLine(mCircleX, mCircleY - mRadius, mCircleX, mCircleY - mRadius + 60, mPaintIntegralPoint);
                canvas.drawText(DEGREE_VALUE[i], (float) (mCircleX - mPaintIntegralPoint.measureText(DEGREE_VALUE[i]) / 2.), mCircleY - mRadius + 90, mPaintIntegralPoint);
            } else {
                canvas.drawLine(mCircleX, mCircleY - mRadius, mCircleX, mCircleY - mRadius + 30, mPaintScale);
                canvas.drawText(DEGREE_VALUE[i], (float) (mCircleX - mPaintScale.measureText(DEGREE_VALUE[i]) / 2.), mCircleY - mRadius + 60, mPaintScale);

            }
            canvas.rotate(30, mCircleX, mCircleY);
        }

        canvas.save();
        float degree = (float) -(90 - ((mClockHour % 12) * 30 +  (float)mClockMinute * 0.5));
        canvas.rotate(degree, mCircleX, mCircleY);
        canvas.translate(mCircleX, mCircleY);
        canvas.drawLine(0, 0, 50, 0, mPaintHour);

        canvas.translate(-mCircleX, -mCircleX);
        canvas.rotate(-degree, mCircleX, mCircleY);
        canvas.rotate(-(90 - mClockMinute * 6), mCircleX, mCircleY);
        canvas.translate(mCircleX, mCircleY);
        canvas.drawLine(0, 0, 80, 0, mPaintMinute);

        canvas.translate(-mCircleX, -mCircleX);
        canvas.rotate((90 - mClockMinute * 6), mCircleX, mCircleY);
        canvas.rotate(-(90 - mClockSecond * 6), mCircleX, mCircleY);
        canvas.translate(mCircleX, mCircleY);
        canvas.drawLine(0, 0, 100, 0, mPaintSecond);

        canvas.restore();
    }

    class ClockHandler extends Handler{

        public  ClockHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Calendar calendar = Calendar.getInstance();

            mClockHour = calendar.get(Calendar.HOUR);
            mClockMinute = calendar.get(Calendar.MINUTE);
            mClockSecond = calendar.get(Calendar.SECOND);
            Clock.this.postInvalidate();
            mClockHandler.sendEmptyMessageDelayed(0,1000);
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}
