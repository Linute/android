package com.linute.linute.SquareCamera.overlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/25/16.
 * <p/>
 * View that allows users to cycle through images. Uses a "wipe" transition
 * <p/>
 * Requires a WipeAdapter to provide it with images, set using @link setAdapter(WipeAdapter)
 * <p/>
 * Will request images with no bounds (infinite scrolling)
 * <p/>
 * Images MUST be the size of the view to display correctly
 */
public class WipeViewPager extends FrameLayout {

    private int mPosition = 0;
    private AlignedImageView[] mContainerViews = new AlignedImageView[3];


    private WipeAdapter mWipeAdapter;

    private static final int LEFT = 0, CENTER = 1, RIGHT = 2;

    private int SCREEN_WIDTH;


    public WipeViewPager(Context context) {
        super(context);
        init(context);
    }

    public WipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WipeViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        SCREEN_WIDTH = context.getResources().getDisplayMetrics().widthPixels;
        int[] colors = {0x22FF0000, 0x2200FF00, 0x220000FF};
        int[] gravity = {Gravity.LEFT, Gravity.CENTER, Gravity.RIGHT};
        for (int i = 0; i < mContainerViews.length; i++) {
            mContainerViews[i] = new AlignedImageView(context);
            mContainerViews[i].setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            mContainerViews[i].setGravity(gravity[i]);
//            mContainerViews[i].setBackgroundColor(colors[i]);
            mContainerViews[i].setScaleType(ImageView.ScaleType.MATRIX);
            addView(mContainerViews[i]);
        }
        prepareContainerViewPositions(-1);
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public void setWipeAdapter(WipeAdapter wipeAdapter) {
        this.mWipeAdapter = wipeAdapter;
        prepareContainerViewContents();
    }

    private float initX, lastX, lastVelX, lastTime,  x;

    private enum DragDirection {
        Left2Right, None, Right2Left
    }

    private DragDirection mDragDirection = DragDirection.None;
    private static final float KINETIC_THRESHOLD = 3;
    private static final float STATIC_THRESHOLD = 200;
    private static final float INIT_DRAG_THRESHOLD = 50;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initX = event.getX(0);
                lastTime = event.getEventTime();

                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX(0);
                switch (mDragDirection) {
                    case None:
                        float dX = initX - x;
                        if (dX < -INIT_DRAG_THRESHOLD) {
                            mDragDirection = DragDirection.Left2Right;
                            LinearInterpolator lInter = new LinearInterpolator();
                            mContainerViews[LEFT].animate().x(x-SCREEN_WIDTH).setDuration(100).setInterpolator(lInter).start();
                            mContainerViews[CENTER].animate().x(x).setDuration(100).setInterpolator(lInter).start();
//                            mContainerViews[CENTER].setAlignLeft(false);
                            break;
                        }
                        if (dX > INIT_DRAG_THRESHOLD) {
                            mDragDirection = DragDirection.Right2Left;
                            LinearInterpolator lInter = new LinearInterpolator();
                            mContainerViews[RIGHT].animate().x(x).setDuration(100).setInterpolator(lInter).start();
                            mContainerViews[CENTER].animate().x(x-SCREEN_WIDTH).setDuration(100).setInterpolator(lInter).start();
//
//                            mContainerViews[CENTER].setAlignLeft(true);
                            break;
                        }
                        break;
                    case Left2Right:
                        mContainerViews[LEFT].setX(x - SCREEN_WIDTH);
//                        mContainerViews[LEFT].getLayoutParams().width = (int) x;
//                        mContainerViews[LEFT].requestLayout();
                        mContainerViews[CENTER].setX(x);
//                        mContainerViews[CENTER].getLayoutParams().width = (int) (SCREEN_WIDTH - x);
//                        mContainerViews[CENTER].requestLayout();
                        break;
                    case Right2Left:
                        mContainerViews[RIGHT].setX(x);
//                        mContainerViews[RIGHT].getLayoutParams().width = (int) (SCREEN_WIDTH - x);
//                        mContainerViews[RIGHT].requestLayout();
                        mContainerViews[CENTER].setX(x - SCREEN_WIDTH);
//                        mContainerViews[CENTER].getLayoutParams().width = (int) x;
//                        mContainerViews[CENTER].requestLayout();
                        break;
                }
                lastVelX = (lastX - x)/(event.getEventTime() - lastTime);
                lastTime = event.getEventTime();
                lastX = x;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX(0);
                int swapIndex = CENTER;
                //TODO check bounds of mPosition
                switch (mDragDirection) {
                    case Left2Right:
                        if (lastVelX < KINETIC_THRESHOLD || x > SCREEN_WIDTH - STATIC_THRESHOLD) {
                            swapIndex = LEFT;
                            mPosition--;
                        }
                        break;
                    case Right2Left:
                        if (lastVelX > KINETIC_THRESHOLD || x < STATIC_THRESHOLD) {
                            swapIndex = RIGHT;
                            mPosition++;
                        }
                        break;
                }
                if (swapIndex != CENTER) {
                    //swap containers for next swipe action
                    AlignedImageView tmp = mContainerViews[CENTER];
                    mContainerViews[CENTER] = mContainerViews[swapIndex];
                    mContainerViews[swapIndex] = mContainerViews[(swapIndex == LEFT ? RIGHT : LEFT)];
                    mContainerViews[(swapIndex == LEFT ? RIGHT : LEFT)] = tmp;
                    //prepare container contents for next wipe action;
                }
                prepareContainerViewPositions(swapIndex);
                if (swapIndex != CENTER) {
                    prepareContainerViewContents();
                }
                mDragDirection = DragDirection.None;

                return true;
        }
        return super.onTouchEvent(event);
    }

    private void prepareContainerViewContents() {
        if (mWipeAdapter != null) {
//            if (mPosition - 1 > 0) {
            mContainerViews[LEFT].setImageBitmap(mWipeAdapter.getOverlay(mPosition - 1));
//            }
//            if (mPosition + 1 < mWipeAdapter.getCount()) {
            mContainerViews[RIGHT].setImageBitmap(mWipeAdapter.getOverlay(mPosition + 1));

//            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        prepareContainerViewPositions(-1);
        prepareContainerViewContents();
    }

    private void prepareContainerViewPositions(int swapIndex) {

//        int duration = Math.abs(lastVelX) > KINETIC_THRESHOLD ? (int)((swapIndex == LEFT ? SCREEN_WIDTH-x : x)/Math.abs(lastVelX)) : 200;

        int duration = 200;

        if (swapIndex == RIGHT || swapIndex == CENTER) {
            mContainerViews[LEFT].animate().x(-SCREEN_WIDTH).setDuration(duration).start();
        } else {
            mContainerViews[LEFT].setX(-SCREEN_WIDTH);
        }

        if (swapIndex == LEFT || swapIndex == CENTER) {
            mContainerViews[RIGHT].animate().x(SCREEN_WIDTH).setDuration(duration).start();
        } else {
            mContainerViews[RIGHT].setX(SCREEN_WIDTH);
        }
        mContainerViews[CENTER].animate().x(0).setDuration(duration).start();
    }

    public interface WipeAdapter {
        int getCount();

        Bitmap getOverlay(int position);
    }

}
