package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


/**
 * Created by QiFeng on 6/7/16.
 */


/**
 * If an image with width/height ratio is more than 16/9, make imageview a square
 */
public class MinimumWidthImageView extends ImageView {

    public MinimumWidthImageView(Context context) {
        super(context);
    }

    public MinimumWidthImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MinimumWidthImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //if width is longer AND width/height has higher ratio than 16/9, make it a square
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if (d != null && d.getIntrinsicWidth() > d.getIntrinsicHeight()
                && (d.getIntrinsicWidth() * 9 > d.getIntrinsicHeight() * 16)) {
            setAdjustViewBounds(false);
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
            return;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
