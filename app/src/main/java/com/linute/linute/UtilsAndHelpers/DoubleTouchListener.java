package com.linute.linute.UtilsAndHelpers;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mikhail on 11/5/16.
 */

public abstract class DoubleTouchListener implements View.OnTouchListener {


    private final int maxDuration;
    long tDown = 0;

    public DoubleTouchListener(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
            long timeNow = event.getDownTime();
            if(timeNow - tDown < maxDuration){
                onDoubleTouch((int)event.getX(), (int)event.getY());
                tDown = 0;
            }else{
                tDown = timeNow;
            }
        }
        return false;
    }

    protected abstract void onDoubleTouch(int x, int y);
}
