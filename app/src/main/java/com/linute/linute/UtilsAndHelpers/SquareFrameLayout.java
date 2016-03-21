package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by QiFeng on 2/27/16.
 *
 * NOTE: this layout always uses width
 */
public class SquareFrameLayout extends FrameLayout {


    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
