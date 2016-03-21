package com.linute.linute.UtilsAndHelpers.VideoClasses;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by QiFeng on 3/8/16.
 */
public class SquareVideoView extends TextureVideoView {


    public SquareVideoView(Context context) {
        super(context);
    }

    public SquareVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //make it square
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
