package com.linute.linute.SquareCamera.overlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/28/16.
 */
public class ManipulableImageView extends ImageView {

    private Paint mPaint;

    public ManipulableImageView(Context context) {
        super(context); init();
    }

    public ManipulableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ManipulableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    RotationGestureDetector mRotationGestureDetector = new RotationGestureDetector(new RotationGestureDetector.OnRotationGestureListener() {
        @Override
        public void OnRotation(RotationGestureDetector rotationDetector) {
            setRotation(-mScaleXFlip*rotationDetector.getAngle() + getRotation());

        }
    });

    private void init(){
        mPaint = new Paint();
        mPaint.setColor(0xFFFF0000);
        mPaint.setStrokeWidth(5);
    }

    private float mTotalScale = 1;
    private float mScaleXFlip = 1;
    private float focusX = 0;
    private float focusY = 0;
    ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mTotalScale *= scaleGestureDetector.getScaleFactor();

            // Don't let the object get too small or too large.
//            mTotalScale = Math.max(0.1f, Math.min(mTotalScale, 5.0f));

            focusX = scaleGestureDetector.getFocusX();
            focusY = scaleGestureDetector.getFocusY();

            setScaleX(mTotalScale * mScaleXFlip);
            setScaleY(mTotalScale);
            invalidate();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {


        }
    });
/*
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawPoint(focusX, focusY, mPaint);
    }*/


    float[] initialX = new float[10];
    float[] initialY = new float[10];
    float[] positionX = new float[10];
    float[] positionY = new float[10];
    long timeDown;



    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);



        mRotationGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        int id;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                long time = event.getDownTime();

                if(time - timeDown < 500){
                    mScaleXFlip *= -1;
                    setScaleX(mTotalScale * mScaleXFlip);
                }
                timeDown = time;
            case MotionEvent.ACTION_POINTER_DOWN:


                id = event.getPointerId(event.getActionIndex());
                initialX[id] = event.getX();
                initialY[id] = event.getY();
                positionX[id] = event.getRawX() - initialX[id];
                positionY[id] = event.getRawY() - initialY[id];
                return true;

            case MotionEvent.ACTION_MOVE:
                id = event.getPointerId(event.getActionIndex());

                positionX[id] = event.getRawX() - initialX[id];// + positionX[id];
                positionY[id] = event.getRawY() - initialY[id];// + positionY[id];

                setX(positionX[0]);
                setY(positionY[0]);

                return true;
            default:
                return false;
        }


    }
}

/* private GestureDetector detector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
         @Override
         public boolean onDown(MotionEvent motionEvent) {
             return false;
         }

         @Override
         public void onShowPress(MotionEvent motionEvent) {

         }

         @Override
         public boolean onSingleTapUp(MotionEvent motionEvent) {
             return false;
         }

         @Override
         public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
             return false;
         }

         @Override
         public void onLongPress(MotionEvent motionEvent) {

         }

         @Override
         public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
             return false;
         }
     });

*/
