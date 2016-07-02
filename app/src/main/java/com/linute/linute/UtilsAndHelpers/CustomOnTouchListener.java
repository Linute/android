package com.linute.linute.UtilsAndHelpers;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by QiFeng on 7/1/16.
 */
public abstract class CustomOnTouchListener implements View.OnTouchListener {

    private static final int DOUBLE_TAP_THRESHOLD = 300; // milliseconds
    private static final int LONG_PRESS_THRESHOLD = 500;

    private int longPressReleaseThreshold;
    private boolean clicked = false;
    private boolean longPressActive = false;
    private Handler mDelayHandler = new Handler();

    private float touchDownX = 0;
    private float touchDownY = 0;

    private long longPressStart = 0;

    public CustomOnTouchListener() {
        longPressReleaseThreshold = 3000;
    }

    public CustomOnTouchListener(int longPressReleaseThreshold) {
        this.longPressReleaseThreshold = longPressReleaseThreshold;
    }

    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.i("test", "onTouch: down");

                //keep track of where we pressed down
                touchDownX = event.getX();
                touchDownY = event.getY();

                //if not looking for the second tap
                if (!clicked) {
                    //remove the single click callback and add new long press callback
                    mDelayHandler.removeCallbacksAndMessages(null);
                    mDelayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            longPressActive = true;
                            longPressStart = event.getEventTime();
                            onLongPress();
                        }
                    }, LONG_PRESS_THRESHOLD);
                }
                break;
            case MotionEvent.ACTION_UP:
                //Log.i("test", "onTouch: up");

                //remove every callback we had
                mDelayHandler.removeCallbacksAndMessages(null);
                if (!clicked) {
                    //onlongpress has been triggered
                    if (longPressActive) {
                        longPressActive = false;
                        onLongPressCancelled(event.getEventTime() - longPressStart >= longPressReleaseThreshold);
                    } else {
                        // add singleclickcallback and watch for second click
                        // if second click occurs, remove single click callback
                        // if second click doesnt occur in time frame, single click will run
                        clicked = true;
                        mDelayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onSingleTap();
                                clicked = false;
                            }
                        }, DOUBLE_TAP_THRESHOLD);
                    }
                } else { //second click occurs
                    clicked = false;
                    onDoubleTap(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                //Log.i("test", "onTouch: cancelled");
                mDelayHandler.removeCallbacksAndMessages(null);
                clicked = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.i("test", "onTouch: move");

                // will sometimes trigger move even if no movement
                // just do some math
                if (Math.abs(event.getX() - touchDownX) + Math.abs(event.getY() - touchDownY) > 2) {

                    //if we move, remove all callbacks
                    if (!longPressActive)
                        mDelayHandler.removeCallbacksAndMessages(null);

                    return longPressActive;
                }
                return false;
        }

        return true;
    }

    protected abstract void onSingleTap();

    protected abstract void onDoubleTap(float x, float y);

    protected abstract void onLongPress();

    protected abstract void onLongPressCancelled(boolean thresholdMet);
}
