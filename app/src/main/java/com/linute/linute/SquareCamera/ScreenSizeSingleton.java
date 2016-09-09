package com.linute.linute.SquareCamera;

import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by QiFeng on 6/11/16.
 */
public class ScreenSizeSingleton {
    public final Point mSize;
    public final boolean mHasRatioRequirement;

    public static final float MIN_RATIO = 1.4f;

    private static ScreenSizeSingleton mScreenSizeSingleton;

    private ScreenSizeSingleton(WindowManager manager){
        Display d = manager.getDefaultDisplay();

        mSize = new Point();
        d.getSize(mSize);
        mHasRatioRequirement = (float)mSize.x / mSize.y >= MIN_RATIO || (float)mSize.y / mSize.x >= MIN_RATIO;
    }


    public static ScreenSizeSingleton getSingleton(){
        return mScreenSizeSingleton;
    }

    public static void init(WindowManager m){
        if (mScreenSizeSingleton == null)
            mScreenSizeSingleton = new ScreenSizeSingleton(m);
    }

    public Point getSize(){
        return mSize;
    }

}
