package com.linute.linute.SquareCamera;

import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by QiFeng on 6/11/16.
 */
public class HasSoftKeySingleton {

    private int mBottomPixels;
    private boolean mHasNavigation;
    private Point mRealSize;
    private Point mSize;

    private static HasSoftKeySingleton mSoftKeySingleton;

    private HasSoftKeySingleton(WindowManager manager){
        Display d = manager.getDefaultDisplay();

        mRealSize = new Point();
        d.getRealSize(mRealSize);


        mSize = new Point();
        d.getSize(mSize);

        int diffX = mRealSize.x - mSize.x;
        int diffY = mRealSize.y - mSize.y;

        mBottomPixels = diffX > diffY ? diffX : diffY;
        mHasNavigation = mBottomPixels > 0;
    }


    public static HasSoftKeySingleton getmSoftKeySingleton(WindowManager m){
        if (mSoftKeySingleton == null){
            mSoftKeySingleton = new HasSoftKeySingleton(m);
        }

        return mSoftKeySingleton;
    }

    public Point getRealSize(){
        return mRealSize;
    }

    public boolean getHasNavigation(){
        return mHasNavigation;
    }

    public int getBottomPixels(){
        return mBottomPixels;
    }

    public Point getSize(){
        return mSize;
    }

}
