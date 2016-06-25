package com.linute.linute.SquareCamera.overlay;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by mikhail on 6/24/16.
 */
public class WipeViewPager extends FrameLayout {

    private int mPosition = 0;
    private RelativeLayout[] mContainerViews = new RelativeLayout[3];


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
        int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        int[] gravity = {Gravity.LEFT, Gravity.CENTER, Gravity.RIGHT};
        for (int i = 0; i < mContainerViews.length; i++) {
            mContainerViews[i] = new RelativeLayout(context);
            mContainerViews[i].setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            mContainerViews[i].setGravity(gravity[i]);
//            mContainerViews[i].setBackgroundColor(colors[i]);
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
    private static final float KINETIC_THRESHOLD = 20;
    private static final float STATIC_THRESHOLD = 200;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initX = event.getX(0);
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX(0);
                float dX = initX - x;
                if (dX > 0) { //user is swiping to the left
                    mContainerViews[RIGHT].getLayoutParams().width = (int) (SCREEN_WIDTH - x);
                    mContainerViews[RIGHT].setX(x);
                    mContainerViews[RIGHT].requestLayout();
                    mContainerViews[CENTER].getLayoutParams().width = (int) x;
                    mContainerViews[CENTER].setX(0);
                    mContainerViews[CENTER].requestLayout();

                } else { //user is swiping to the right
                    mContainerViews[LEFT].getLayoutParams().width = (int) x;
                    mContainerViews[LEFT].setX(0);
                    mContainerViews[LEFT].requestLayout();
                    mContainerViews[CENTER].getLayoutParams().width = (int) (SCREEN_WIDTH - x);
                    mContainerViews[CENTER].setX(x);
                    mContainerViews[CENTER].requestLayout();
                }

                lastX = x;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX(0);
                int swapIndex = CENTER;
                //TODO check bounds of mPosition
                if (lastX - x > KINETIC_THRESHOLD || x < STATIC_THRESHOLD) { // swiping to the left
                    swapIndex = RIGHT;
                    mPosition++;
                } else if (lastX - x < -KINETIC_THRESHOLD || SCREEN_WIDTH - x < STATIC_THRESHOLD) { // swiping to the right
                    swapIndex = LEFT;
                    mPosition--;
                }
                if (swapIndex != CENTER) {
                    //swap containers for next swipe action
                    RelativeLayout tmp = mContainerViews[CENTER];
                    mContainerViews[CENTER] = mContainerViews[swapIndex];
                    mContainerViews[swapIndex] = tmp;
                    //prepare container contents for next wipe action;
                }
                prepareContainerViewPositions();
                if(swapIndex != CENTER){
                    prepareContainerViewContents();
                }

                return true;
        }
        return super.onTouchEvent(event);
    }

    private void prepareContainerViewContents() {
        if (mWipeAdapter != null) {
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            if (mPosition - 1 > 0) {
                View leftView = mContainerViews[LEFT].getChildAt(0);
                boolean isNull = leftView == null;
                leftView = mWipeAdapter.getView(leftView, mContainerViews[LEFT], mPosition - 1,  false);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(metrics.widthPixels,metrics.heightPixels);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                leftView.setLayoutParams(params);
                if (isNull)
                    mContainerViews[LEFT].addView(leftView);
            }
            if (mPosition + 1 < mWipeAdapter.getCount()) {
                View rightView = mContainerViews[RIGHT].getChildAt(0);
                boolean isNull = rightView == null;
                rightView = mWipeAdapter.getView(rightView, mContainerViews[RIGHT], mPosition + 1,  true);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(metrics.widthPixels,metrics.heightPixels);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                rightView.setLayoutParams(params);
                if (isNull)
                    mContainerViews[RIGHT].addView(rightView);

            }
        }
    }

    private void prepareContainerViewPositions() {
        mContainerViews[LEFT].setX(0);
        mContainerViews[LEFT].getLayoutParams().width = 0;
        mContainerViews[LEFT].requestLayout();
        mContainerViews[RIGHT].setX(SCREEN_WIDTH);
        mContainerViews[RIGHT].getLayoutParams().width = 0;
        mContainerViews[RIGHT].requestLayout();
        mContainerViews[CENTER].setX(0);
        mContainerViews[CENTER].getLayoutParams().width = SCREEN_WIDTH;
        mContainerViews[CENTER].requestLayout();
    }

    public interface WipeAdapter {
        int getCount();

        View getView(View recycle, ViewGroup container, int position, boolean isRight);
    }

}
