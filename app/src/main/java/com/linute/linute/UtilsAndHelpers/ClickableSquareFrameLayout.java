package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by QiFeng on 3/9/16.
 */
public class ClickableSquareFrameLayout extends  SquareFrameLayout {


    private OnClickListener mOnClickListener;

    public ClickableSquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mOnClickListener = l;
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mOnClickListener != null;
    }
}
