package com.linute.linute.UtilsAndHelpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by QiFeng on 3/8/16.
 */
public class SquareVideoView extends VideoView {


    public SquareVideoView(Context context) {
        super(context);
    }

    public SquareVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }



    //make it square
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
