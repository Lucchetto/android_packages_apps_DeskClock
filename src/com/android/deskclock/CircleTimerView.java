package com.android.deskclock;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: Insert description here. (generated by isaackatz)
 */
public class CircleTimerView extends View {


    private int mRedColor;
    private int mWhiteColor;
    private long mIntervalTime = 0;
    private long mIntervalStartTime = -1;
    private long mMarkerTime = -1;
    private long mCurrentIntervalTime = 0;
    private long mAccumulatedTime = 0;
    private boolean mPaused = false;
    private static float mTextSize = 96;
    private static float mStrokeSize = 4;
    private final Paint mPaint = new Paint();
    private final Paint mTextPaint = new Paint();
    private final RectF mArcRect = new RectF();
    private Resources mResources;

    // Class has 2 modes:
    // Timer mode - counting down. in this mode the animation is counter-clockwise and stops at 0
    // Stop watch mode - counting up - in this mode the animation is clockwise and will keep the
    //                   animation until stopped.
    private boolean mTimerMode = false; // default is stop watch view

    Runnable mAnimationThread = new Runnable() {

        @Override
        public void run() {
            mCurrentIntervalTime =
                    System.currentTimeMillis() - mIntervalStartTime + mAccumulatedTime;
            invalidate();
            postDelayed(mAnimationThread, 20);
        }

    };

    public CircleTimerView(Context context) {
        this(context, null);
    }

    public CircleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setIntervalTime(long t) {
        mIntervalTime = t;
    }

    public void setMarkerTime(long t) {
        mMarkerTime = mCurrentIntervalTime;
    }

    public void reset() {
        mIntervalStartTime = -1;
        mMarkerTime = -1;
        invalidate();
    }
    public void startIntervalAnimation() {
        mIntervalStartTime = System.currentTimeMillis();
        this.post(mAnimationThread);
        mPaused = false;
    }
    public void stopIntervalAnimation() {
        this.removeCallbacks(mAnimationThread);
        mIntervalStartTime = -1;
        mAccumulatedTime = 0;
    }

    public boolean isAnimating() {
        return (mIntervalStartTime != -1);
    }

    public void pauseIntervalAnimation() {
        this.removeCallbacks(mAnimationThread);
        mAccumulatedTime += System.currentTimeMillis() - mIntervalStartTime;
        mPaused = true;
    }



    private void init(Context c) {

        mResources = c.getResources();

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeSize);

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mWhiteColor = mResources.getColor(R.color.clock_white);
        mRedColor = mResources.getColor(R.color.clock_red);
    }

    public void setTimerMode(boolean mode) {
        mTimerMode = mode;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int xCenter = getWidth() / 2 + 1;
        int yCenter = getHeight() / 2;

        mPaint.setStrokeWidth(mStrokeSize);
        float radius = Math.min(xCenter, yCenter) - mStrokeSize;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            xCenter = (int) (radius + mStrokeSize);
        }

        mPaint.setColor(mWhiteColor);
        canvas.drawCircle (xCenter, yCenter, radius, mPaint);

        mPaint.setColor(mRedColor);
        if (mIntervalStartTime != -1) {
            mArcRect.top = yCenter - radius;
            mArcRect.bottom = yCenter + radius;
            mArcRect.left =  xCenter - radius;
            mArcRect.right = xCenter + radius;
            float percent = (float)mCurrentIntervalTime / (float)mIntervalTime;
            if (mTimerMode){
                canvas.drawArc (mArcRect, 270, - percent * 360 , false, mPaint);
            } else {
                canvas.drawArc (mArcRect, 270, + percent * 360 , false, mPaint);
            }
            mPaint.setStrokeWidth(mStrokeSize + 10);
            if (mTimerMode){
                canvas.drawArc (mArcRect, 265 - percent * 360, 10 , false, mPaint);
            } else {
                canvas.drawArc (mArcRect, 265 + percent * 360, 10 , false, mPaint);
            }
         }
        if (mMarkerTime != -1) {
            mPaint.setStrokeWidth(mStrokeSize + 30);
            mPaint.setColor(mWhiteColor);
            float angle = (float)(mMarkerTime % mIntervalTime) / (float)mIntervalTime * 360;
            canvas.drawArc (mArcRect, 270 + angle, 1 , false, mPaint);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
      Parcelable superState = super.onSaveInstanceState();
      SavedState ss = new SavedState(superState);

      ss.mIntervalTime = this.mIntervalTime;
      ss.mIntervalStartTime = this.mIntervalStartTime;
      ss.mCurrentIntervalTime = this.mCurrentIntervalTime;
      ss.mAccumulatedTime = this.mAccumulatedTime;
      ss.mPaused = this.mPaused;
      ss.mTimerMode = this.mTimerMode;

      removeCallbacks(mAnimationThread);
      return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
      if(!(state instanceof SavedState)) {
        super.onRestoreInstanceState(state);
        return;
      }

      SavedState ss = (SavedState)state;
      super.onRestoreInstanceState(ss.getSuperState());

      this.mIntervalTime = ss.mIntervalTime;
      this.mIntervalStartTime = ss.mIntervalStartTime;
      this.mCurrentIntervalTime = ss.mCurrentIntervalTime;
      this.mAccumulatedTime = ss.mAccumulatedTime;
      this.mPaused = ss.mPaused;
      this.mTimerMode = ss.mTimerMode;
      if (mIntervalStartTime != -1 && !mPaused) {
          this.post(mAnimationThread);
      }
    }

    static class SavedState extends BaseSavedState {
        public boolean mPaused = false;
        public long mIntervalTime = 0;
        public long mIntervalStartTime = -1;
        public long mCurrentIntervalTime = 0;
        public long mAccumulatedTime = 0;
        public boolean mTimerMode;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.mIntervalTime = in.readLong();
            this.mIntervalStartTime = in.readLong();
            this.mCurrentIntervalTime = in.readLong();
            this.mAccumulatedTime = in.readLong();
            this.mPaused = (in.readInt() == 1);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(this.mIntervalTime);
            out.writeLong(this.mIntervalStartTime);
            out.writeLong(this.mCurrentIntervalTime);
            out.writeLong(this.mAccumulatedTime);
            out.writeInt(this.mPaused?1:0);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
        };
    }

}