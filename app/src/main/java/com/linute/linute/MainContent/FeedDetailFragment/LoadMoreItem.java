package com.linute.linute.MainContent.FeedDetailFragment;

/**
 * Created by QiFeng on 4/4/16.
 */
public class LoadMoreItem {

    private boolean mLoading;

    public LoadMoreItem(){
        mLoading = false;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
    }
}
