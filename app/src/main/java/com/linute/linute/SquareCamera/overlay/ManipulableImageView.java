package com.linute.linute.SquareCamera.overlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
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
    int mainX, mainY;

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
                initialX[id] = event.getRawX() - getX();
                initialY[id] = event.getRawY() - getY();
                positionX[id] = event.getRawX();
                positionY[id] = event.getRawX();
                return true;

            case MotionEvent.ACTION_MOVE:

                    mainX = (int)event.getRawX();
                    mainY = (int)event.getRawY();
                

                positionX[0] = event.getRawX() - initialX[0];// + positionX[id];
                positionY[0] = event.getRawY() - initialY[0];// + positionY[id];


                float posX = positionX[0];
                float posY = positionY[0];

/*
                for(int i=0;i<event.getPointerCount();i++){
                    int pid = event.getPointerId(i);
                    posX =(positionX[pid] - posX)/2 + posX;
                    posY =(positionY[pid] - posY)/2 + posY;
                }
*/


                setX(posX);
                setY(posY);

                if(mCollisionListener != null){
                    Point p = new Point(mainX, mainY);
                    mCollisionListener.getCollisionSensor().getHitRect(theirBounds);
                    theirBounds.left -= 10;
                    theirBounds.right += 10;
                    theirBounds.top -= 10;
                    theirBounds.bottom += 10;

                    boolean intersects = theirBounds.contains(p.x, p.y);

                    if(intersects && !isInCollision){
                        isInCollision = true;
                        mCollisionListener.onViewCollisionBegin(this);
                    }else if(!intersects && isInCollision){
                        isInCollision = false;
                        mCollisionListener.onViewCollisionEnd(this);
                    }

                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                for(int i=0;i<event.getPointerCount();i++){
                    int pid = event.getPointerId(i);
                    initialX[pid] = event.getX(i);
                    initialY[pid] = event.getY(i);

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
        void onViewCollisionBegin(View me);
        void onViewCollisionEnd(View me);
        void onViewDropCollision(View me);
        View getCollisionSensor();
    }

}
