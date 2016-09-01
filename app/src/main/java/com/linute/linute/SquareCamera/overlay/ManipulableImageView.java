package com.linute.linute.SquareCamera.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/28/16.
 */
public class ManipulableImageView extends FrameLayout {


    private ImageView mImageView;
    private Bitmap image;
    private Bitmap flipped;

    public ManipulableImageView(Context context) {
        super(context);
        init(context);
    }

    public ManipulableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ManipulableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }



    private void init(Context context) {
        mImageView = new ImageView(context);

        setClipChildren(false);

        addView(mImageView);
        mImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mImageView.setX(100);
        mImageView.setY(100);

    /*    bPaint = new Paint();
        bPaint.setColor(0xFF0000FF);
        bPaint.setStrokeWidth(10);
        rPaint = new Paint();
        rPaint.setColor(0xFFFF0000);
    }

    Paint bPaint;
    Paint rPaint;

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawRect(collisionRect, rPaint);
        canvas.drawPoint(mainX, mainY, bPaint);

        */
    }

    private ViewManipulationListener mCollisionListener;

    public void setManipulationListener(ViewManipulationListener collisionListener) {
        mCollisionListener = collisionListener;
        mCollisionListener.getCollisionSensor().getHitRect(collisionRect);
        collisionRect.left -= 10;
        collisionRect.right += 10;
        collisionRect.top -= 10;
        collisionRect.bottom += 10;
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

            mImageView.setScaleX(mTotalScale * mScaleXFlip);
            mImageView.setScaleY(mTotalScale);
            mImageView.invalidate();
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

    float mTotalRotation = 0;
    float mSubRotation = 0;

    RotationGestureDetector mRotationGestureDetector = new RotationGestureDetector(new RotationGestureDetector.OnRotationGestureListener() {
        @Override
        public void onRotation(RotationGestureDetector rotationDetector) {
//            mImageView.setRotation(-mScaleXFlip * rotationDetector.getAngle() + getRotation());
            Matrix m = mImageView.getImageMatrix();

            mSubRotation = rotationDetector.getAngle();
            m.setRotate(-mScaleXFlip * (mTotalRotation + mSubRotation), mImageView.getWidth() / 2, mImageView.getHeight() / 2);
            mImageView.setImageMatrix(m);
            mImageView.invalidate();
        }

        @Override
        public void onRotationStarted(RotationGestureDetector rotationDetector) {
            mTotalRotation += mSubRotation;
            mSubRotation = 0;
        }
    });


    float[] initialX = new float[10];
    float[] initialY = new float[10];
    float[] positionX = new float[10];
    float[] positionY = new float[10];
    int mainX, mainY;

    long timeDown;

    boolean isInCollision = false;

    Rect rect = new Rect();
    Rect collisionRect = new Rect();


    boolean isTouched = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);


        getImageBounds(rect);
        invalidate();

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            if (rect.contains((int) event.getX(), (int) event.getY())) {
                isTouched = true;
            } else {
                return false;
            }


            //double tap
            long time = event.getDownTime();

            if (time - timeDown < 500) {
                mScaleXFlip *= -1;
                mImageView.setImageBitmap(mScaleXFlip == -1 ? flipped : image);
                mImageView.invalidate();
                timeDown = 0;
            } else {
                timeDown = time;
            }
            if (mCollisionListener != null) mCollisionListener.onViewPickedUp(this);
        }

        if (!isTouched) {
            return false;
        }


        mRotationGestureDetector.onTouchEvent(event);

        mScaleGestureDetector.onTouchEvent(event);

        int id;


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_POINTER_DOWN:
                int index = event.getActionIndex();
                id = event.getPointerId(index);
                initialX[id] = event.getX(index) - mImageView.getX();
                initialY[id] = event.getY(index) - mImageView.getY();
                positionX[id] = event.getX(index) - initialX[id];
                positionY[id] = event.getY(index) - initialY[id];


                refreshPosition(event);
                return true;

            case MotionEvent.ACTION_MOVE:


                refreshPosition(event);

                if (mCollisionListener != null) {


                    try {
                        mainX = (int) event.getX(0);
                        mainY = (int) event.getY(0);

                        mCollisionListener.getCollisionSensor().getHitRect(collisionRect);
                        collisionRect.left -= 10;
                        collisionRect.right += 10;
                        collisionRect.top -= 10;
                        collisionRect.bottom += 10;

                        boolean intersects = collisionRect.contains(mainX, mainY);

                        if (intersects && !isInCollision) {
                            isInCollision = true;
                            mCollisionListener.onViewCollisionBegin(this);
                        } else if (!intersects && isInCollision) {
                            isInCollision = false;
                            mCollisionListener.onViewCollisionEnd(this);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }


                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                id = event.getPointerId(event.getActionIndex());
                initialX[id] = 0;
                initialY[id] = 0;
                positionX[id] = 0;
                positionY[id] = 0;

                return true;
            case MotionEvent.ACTION_UP:
                if (mCollisionListener != null) {
                    mCollisionListener.onViewDropped(this);
                    if (isInCollision) {
                        isInCollision = false;
                        mCollisionListener.onViewDropCollision(this);
                    }
                }
                isTouched = false;
                return true;
            default:
                return false;
        }


    }

    private void refreshPosition(MotionEvent event) {
        float posX = mImageView.getX();
        float posY = mImageView.getY();
        float nPosX, nPosY;
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pid = event.getPointerId(i);
            nPosX = event.getX(i) - initialX[pid];
            nPosY = event.getY(i) - initialY[pid];


            posX += (nPosX - positionX[pid]) / event.getPointerCount();
            posY += (nPosY - positionY[pid]) / event.getPointerCount();

            positionX[pid] = nPosX;
            positionY[pid] = nPosY;
        }


        mImageView.setX(posX);
        mImageView.setY(posY);
    }


    private Rect getImageBounds(Rect rect) {
        mImageView.getHitRect(rect);
        return rect;
    }


    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        int bigD = bitmap.getWidth();
        if (bigD < bitmap.getHeight()) {
            bigD = bitmap.getHeight();
        }
        mImageView.setLayoutParams(new FrameLayout.LayoutParams(bigD, bigD));
        this.image = bitmap;
        Matrix m = new Matrix();
        m.setScale(-1, 1);
        this.flipped = Bitmap.createBitmap(image, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
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
