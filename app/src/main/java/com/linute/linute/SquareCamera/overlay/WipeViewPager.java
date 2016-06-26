package com.linute.linute.SquareCamera.overlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/25/16.
 *
 * View that allows users to cycle through images. Uses a "wipe" transition
 *
 * Requires a WipeAdapter to provide it with images, set using @link setAdapter(WipeAdapter)
 *
 * Will request images with no bounds (infinite scrolling)
 *
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
        prepareContainerViewPositions();
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public void setWipeAdapter(WipeAdapter wipeAdapter) {
        this.mWipeAdapter = wipeAdapter;
        prepareContainerViewContents();
    }

    private float initX, initY, lastX, lastY, x;

    private enum DragDirection {
        Left2Right, None, Right2Left
    }

    ;
    private DragDirection mDragDirection = DragDirection.None;
    private static final float KINETIC_THRESHOLD = 20;
    private static final float STATIC_THRESHOLD = 200;
    private static final float INIT_DRAG_THRESHOLD = 50;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initX = event.getX(0);
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX(0);
                Log.i("AAA", mDragDirection.toString());
                switch (mDragDirection) {
                    case None:
                        float dX = initX - x;
                        if (dX < -INIT_DRAG_THRESHOLD) {
                            mDragDirection = DragDirection.Left2Right;
                            mContainerViews[CENTER].setAlignLeft(false);
                            break;
                        }
                        if (dX > INIT_DRAG_THRESHOLD) {
                            mDragDirection = DragDirection.Right2Left;
                            mContainerViews[CENTER].setAlignLeft(true);
                            break;
                        }
                        break;
                    case Left2Right:
                        mContainerViews[LEFT].setX(0);
                        mContainerViews[LEFT].getLayoutParams().width = (int) x;
                        mContainerViews[LEFT].requestLayout();
                        mContainerViews[CENTER].setX(x);
                        mContainerViews[CENTER].getLayoutParams().width = (int) (SCREEN_WIDTH - x);
                        mContainerViews[CENTER].requestLayout();
                        break;
                    case Right2Left:
                        mContainerViews[RIGHT].setX(x);
                        mContainerViews[RIGHT].getLayoutParams().width = (int) (SCREEN_WIDTH - x);
                        mContainerViews[RIGHT].requestLayout();
                        mContainerViews[CENTER].setX(0);
                        mContainerViews[CENTER].getLayoutParams().width = (int) x;
                        mContainerViews[CENTER].requestLayout();
                        break;
                }
                lastX = x;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX(0);
                int swapIndex = CENTER;
                //TODO check bounds of mPosition

                switch (mDragDirection) {
                    case Left2Right:
                        if (x - lastX > KINETIC_THRESHOLD || x > SCREEN_WIDTH - STATIC_THRESHOLD) {
                            swapIndex = LEFT;
                            mPosition --;
                        }
                        break;
                    case Right2Left:
                        if (lastX - x > KINETIC_THRESHOLD || x < STATIC_THRESHOLD) {
                            swapIndex = RIGHT;
                            mPosition ++;
                        }
                        break;
                }
                if (swapIndex != CENTER) {
                    Log.i("AAA", mPosition+"");
                    //swap containers for next swipe action
                    AlignedImageView tmp = mContainerViews[CENTER];
                    mContainerViews[CENTER] = mContainerViews[swapIndex];
                    mContainerViews[swapIndex] = tmp;
                    //prepare container contents for next wipe action;
                }
                prepareContainerViewPositions();
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

    private void prepareContainerViewPositions() {
        mContainerViews[LEFT].setX(0);
        mContainerViews[LEFT].getLayoutParams().width = 0;
        mContainerViews[LEFT].requestLayout();
        mContainerViews[LEFT].setAlignLeft(true);
        mContainerViews[RIGHT].setX(SCREEN_WIDTH);
        mContainerViews[RIGHT].getLayoutParams().width = 0;
        mContainerViews[RIGHT].requestLayout();
        mContainerViews[RIGHT].setAlignLeft(false);
        mContainerViews[CENTER].setX(0);
        mContainerViews[CENTER].getLayoutParams().width = SCREEN_WIDTH;
        mContainerViews[CENTER].requestLayout();
    }

    public interface WipeAdapter {
        int getCount();

        Bitmap getOverlay(int position);
    }

}
