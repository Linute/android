package com.linute.linute.SquareCamera;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by QiFeng on 8/22/16.
 */
public class CustomView extends View {


    private boolean makeSquare = false;

    public CustomView(Context context) {
        super(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setMakeSquare(boolean makeSquare) {
        this.makeSquare = makeSquare;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //if doesn't have required size, we force to square
        setMeasuredDimension(getMeasuredWidth(),
                makeSquare ? getMeasuredWidth() : (int) (getMeasuredWidth() * (1.2))
        );
    }
}
