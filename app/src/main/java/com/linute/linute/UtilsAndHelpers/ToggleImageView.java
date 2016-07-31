package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by QiFeng on 7/14/16.
 */
public class ToggleImageView extends ImageView {

    private int[] mResIds = new int[2];
    private boolean mActive = false;

    public ToggleImageView(Context context) {
        super(context);
    }

    public ToggleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setImageViews(@DrawableRes int inactive, @DrawableRes int active) {
        mResIds[0] = inactive;
        mResIds[1] = active;
    }

    public boolean isActive() {
        return mActive;
    }


    public void setActive(boolean active) {
        mActive = active;
        setImageResource(mResIds[mActive ? 1 : 0]);
    }
}
