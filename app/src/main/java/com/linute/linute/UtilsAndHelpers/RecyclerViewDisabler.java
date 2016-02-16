package com.linute.linute.UtilsAndHelpers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;


/**
 * Created by QiFeng on 2/12/16.
 */
public class RecyclerViewDisabler extends RecyclerView.OnScrollListener {

    private ScrolledUpRunnable mScrolledUpRunnable;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0){
            mScrolledUpRunnable.onScrolledUp();
        }
    }

    public void setScrolledUpRunnable(ScrolledUpRunnable r){
        mScrolledUpRunnable = r;
    }

    public interface ScrolledUpRunnable{

        public void onScrolledUp();

    }
}
