package com.linute.linute.SquareCamera.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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


    private Paint mPaint;


    private void init(Context context) {
        mImageView = new ImageView(context);

        addView(mImageView);
        mImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mImageView.setX(100);
        mImageView.setY(100);
        mPaint = new Paint();
        mPaint.setColor(0xFFFF0000);
        mPaint.setStrokeWidth(5);

    }

    private ViewManipulationListener mCollisionListener;

    public void setManipulationListener(ViewManipulationListener collisionListener) {
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
        public void OnRotation(RotationGestureDetector rotationDetector) {
//            mImageView.setRotation(-mScaleXFlip * rotationDetector.getAngle() + getRotation());
            Matrix m = mImageView.getImageMatrix();
            mSubRotation = rotationDetector.getAngle();
            m.setRotate(-mScaleXFlip * (mTotalRotation + mSubRotation), mImageView.getWidth() / 2, mImageView.getHeight() / 2);
            mImageView.setImageMatrix(m);
            mImageView.invalidate();
        }
    });

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, mPaint);
        super.draw(canvas);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        canvas.drawRect(rect, mPaint);

        canvas.restore();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, mPaint);
        canvas.drawRect(rect, mPaint);
        super.draw(canvas);
    }


    float[] initialX = new float[10];
    float[] initialY = new float[10];
    float[] positionX = new float[10];
    float[] positionY = new float[10];
    int mainX, mainY;

    long timeDown;

    boolean isInCollision = false;

    Rect rect = new Rect();


    boolean isTouched = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);


        getImageBounds(rect);
        invalidate();

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {

            Log.i("AAA", rect.toString());
            Log.i("AAA", (int) event.getX() + "," + (int) event.getY());
            if (rect.contains((int) event.getX(), (int) event.getY())) {
                isTouched = true;
            } else {
                return false;
            }


            long time = event.getDownTime();

            if (time - timeDown < 500) {
                mScaleXFlip *= -1;
                mImageView.setScaleX(mTotalScale * mScaleXFlip);
            }
            timeDown = time;
            if (mCollisionListener != null) mCollisionListener.onViewPickedUp(this);
        }

        if (!isTouched) {
            return false;
        }


        if (!mRotationGestureDetector.onTouchEvent(event)) {
            mTotalRotation += mSubRotation;
            mSubRotation = 0;
        }

        mScaleGestureDetector.onTouchEvent(event);

        int id;


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_POINTER_DOWN:
                id = event.getPointerId(event.getActionIndex());
                initialX[id] = event.getRawX() - mImageView.getX();
                initialY[id] = event.getRawY() - mImageView.getY();
                positionX[id] = event.getRawX();
                positionY[id] = event.getRawX();
                return true;

            case MotionEvent.ACTION_MOVE:


//                positionX[0] = event.getRawX() - initialX[0];// + positionX[id];
//                positionY[0] = event.getRawY() - initialY[0];// + positionY[id];

                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pid = event.getPointerId(i);
                    positionX[pid] = event.getX(i) - initialX[0];
                    positionY[pid] = event.getY(i) - initialY[0];
                }


                float posX = 0;
                float posY = 0;

                for (int i = 0; i < event.getPointerCount(); i++) {
                    int pid = event.getPointerId(i);
                    posX += positionX[pid];
                    posY += positionY[pid];
//                    posX =(positionX[pid] - posX)/2 + posX;
//                    posY =(positionY[pid] - posY)/2 + posY;
                }

                posX /= event.getPointerCount();
                posY /= event.getPointerCount();


                mImageView.setX(posX);
                mImageView.setY(posY);

                if (mCollisionListener != null) {
                    mCollisionListener.getCollisionSensor().getHitRect(rect);
                    rect.left -= 10;
                    rect.right += 10;
                    rect.top -= 10;
                    rect.bottom += 10;

                    boolean intersects = rect.contains(mainX, mainY);

                    if (intersects && !isInCollision) {
                        isInCollision = true;
                        mCollisionListener.onViewCollisionBegin(this);
                    } else if (!intersects && isInCollision) {
                        isInCollision = false;
                        mCollisionListener.onViewCollisionEnd(this);
                    }

                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
               /* for (int i = 0; i < event.getPointerCount(); i++) {
                    int pid = event.getPointerId(i);
                    initialX[pid] = event.getX(i);
                    initialY[pid] = event.getY(i);
                }
*/
                if (event.getActionIndex() == 1) {
                    mTotalRotation += mSubRotation;
                    mSubRotation = 0;
                }

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


    private static boolean isViewContains(View view, int rx, int ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = (int) (view.getWidth() * view.getScaleX());
        int h = (int) (view.getHeight() * view.getScaleY());

        Log.i("AAA", x + " " + y + " " + w + " " + h);

        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }


    private Rect getImageBounds(Rect rect) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mImageView.getClipBounds(rect);

        } else {*/
        int[] l = new int[2];
        mImageView.getLocationOnScreen(l);
        rect.left = l[0];
        rect.right = (int) (rect.left + (mImageView.getWidth() * mImageView.getScaleX() * mScaleXFlip));
        rect.top = l[1];
        rect.bottom = (int) (rect.top + (mImageView.getHeight() * mImageView.getScaleY()));
//        }
        return rect;
    }


    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        int bigD = bitmap.getWidth();
        if (bigD < bitmap.getHeight()) {
            bigD = bitmap.getHeight();
        }
        mImageView.setLayoutParams(new FrameLayout.LayoutParams(bigD, bigD));
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
