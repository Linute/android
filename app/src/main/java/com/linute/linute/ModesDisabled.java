package com.linute.linute;

/**
 * Created by QiFeng on 11/4/16.
 */

public class ModesDisabled {

    private static ModesDisabled mModes;

    private boolean mRealPosts;
    private boolean mRealComments;
    private boolean mAnonPosts;
    private boolean mAnonComments;


    public static ModesDisabled getInstance(){
        if (mModes == null)
            mModes = new ModesDisabled();
        return mModes;
    }

    private ModesDisabled(){
        mRealPosts = false;
        mRealComments = false;
        mAnonComments = false;
        mAnonPosts = false;
    }

    public boolean isRealPosts() {
        return mRealPosts;
    }

    public void setRealPosts(boolean realPosts) {
        mRealPosts = realPosts;
    }

    public boolean isRealComments() {
        return mRealComments;
    }

    public void setRealComments(boolean realComments) {
        mRealComments = realComments;
    }

    public boolean isAnonPosts() {
        return mAnonPosts;
    }

    public void setAnonPosts(boolean anonPosts) {
        mAnonPosts = anonPosts;
    }

    public boolean isAnonComments() {
        return mAnonComments;
    }

    public void setAnonComments(boolean anonComments) {
        mAnonComments = anonComments;
    }
}
