package com.linute.linute.SquareCamera.overlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/28/16.
 */
public class ManipulableImageView extends ImageView {


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

//    private Paint mPaint;


    private void init(){
//        mPaint = new Paint();
//        mPaint.setColor(0xFFFF0000);
//        mPaint.setStrokeWidth(5);
    }

    private ViewManipulationListener mCollisionListener;

    public void setManipulationListener(ViewManipulationListener collisionListener){
        mCollisionListener = collisionListener;
    }


    private float mTotalScale = 1;
    private short mScaleXFlip = 1;
//    private float focusX = 0;
//    private float focusY = 0;
    ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mTotalScale *= scaleGestureDetector.getScaleFactor();

            // Don't let the object get too small or too large.
//            mTotalScale = Math.max(0.1f, Math.min(mTotalScale, 5.0f));

//            focusX = scaleGestureDetector.getFocusX();
//            focusY = scaleGestureDetector.getFocusY();

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

    boolean isInCollision = false;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);



        mRotationGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        int id;

        Rect myBounds = new Rect(), theirBounds = new Rect();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                long time = event.getDownTime();

                if(time - timeDown < 500){
                    mScaleXFlip *= -1;
                    setScaleX(mTotalScale * mScaleXFlip);
                }
                timeDown = time;
                if(mCollisionListener != null) mCollisionListener.onViewPickedUp(this);
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

                if(mCollisionListener != null){
                    getHitRect(myBounds);
                    mCollisionListener.getCollisionSensor().getHitRect(theirBounds);

                    if(myBounds.intersect(theirBounds) && !isInCollision){
                        isInCollision = true;
                        mCollisionListener.onViewCollision(this);
                    }else if(!myBounds.intersect(theirBounds) && isInCollision){
                        isInCollision = false;
                    }

                }


                return true;
            case MotionEvent.ACTION_UP:
                if(mCollisionListener != null){
                    mCollisionListener.onViewDropped(this);
                    if(isInCollision){
                        isInCollision = false;
                        mCollisionListener.onViewDropCollision(this);
                    }
                }
                return true;
            default:
                return false;
        }


    }

    public interface ViewManipulationListener {
        void onViewPickedUp(View me);
        void onViewDropped(View me);
        void onViewCollision(View me);
        void onViewDropCollision(View me);
        View getCollisionSensor();
    }

}
