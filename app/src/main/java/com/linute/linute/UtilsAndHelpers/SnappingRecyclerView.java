package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by QiFeng on 4/23/16.
 */
public class SnappingRecyclerView extends RecyclerView {

    private boolean mCanBeTouched = true;
    private OnPositionChanged mOnPositionChanged;

    public SnappingRecyclerView(Context context) {
        super(context);
    }

    public SnappingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SnappingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (!mCanBeTouched) {
                mCanBeTouched = true;
            } else {
                LinearLayoutManager llm = (LinearLayoutManager) getLayoutManager();
                int first = llm.findFirstVisibleItemPosition();
                int last = llm.findLastVisibleItemPosition();
                if (first != last) {
                    mCanBeTouched = false;
                    smoothScrollToPosition(first);
                    if (mOnPositionChanged != null)
                        mOnPositionChanged.positionChanged(first);
                } else {
                    mCanBeTouched = true;
                }
            }
        }
        super.onScrollStateChanged(state);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !mCanBeTouched || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (!mCanBeTouched) return true;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
        int first = linearLayoutManager.findLastVisibleItemPosition();
        int last = linearLayoutManager.findFirstVisibleItemPosition();
        if (first != last) {
            mCanBeTouched = false;
            if (velocityX > 0) {
                smoothScrollToPosition(first);
                if (mOnPositionChanged != null)
                    mOnPositionChanged.positionChanged(first);
            } else {
                smoothScrollToPosition(last);
                if (mOnPositionChanged != null)
                    mOnPositionChanged.positionChanged(last);
            }
            return true;
        }

        return false;
    }


    public void setIndicatorControllers(OnPositionChanged onPositionChanged) {
        mOnPositionChanged = onPositionChanged;
    }

    public void previousImage() {
        if (mCanBeTouched) {
            int pos = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition() - 1;
            if (pos >= 0) {
                smoothScrollToPosition(pos);
                if (mOnPositionChanged != null)
                    mOnPositionChanged.positionChanged(pos);
            }
        }
    }

    public void nextImage() {
        if (mCanBeTouched) {
            LinearLayoutManager llm = (LinearLayoutManager) getLayoutManager();
            int pos = llm.findLastVisibleItemPosition() + 1;
            if (pos < llm.getItemCount()) {
                smoothScrollToPosition(pos);
                if (mOnPositionChanged != null)
                    mOnPositionChanged.positionChanged(pos);
            }
        }
    }

    public interface OnPositionChanged {
        void positionChanged(int newPosition);
    }
}
