package com.linute.linute.UtilsAndHelpers;

import android.os.Handler;
import android.util.Log;
import android.view.View;

/**
 * Created by QiFeng on 2/6/16.
 */
public abstract class DoubleAndSingleClickListener implements View.OnClickListener {

    private static final long DOUBLE_CLICK_TIME_DELTA = 200;//milliseconds

    boolean clicked = false;

    private Handler mHandler = new Handler();

    @Override
    public void onClick(final View v) {
        if (!clicked){
            clicked = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onSingleClick(v);
                    clicked = false;
                }
            }, DOUBLE_CLICK_TIME_DELTA);
        }

        else{
            mHandler.removeCallbacksAndMessages(null);
            onDoubleClick(v);
            clicked = false;
        }
    }

    public abstract void onSingleClick(View v);
    public abstract void onDoubleClick(View v);
}