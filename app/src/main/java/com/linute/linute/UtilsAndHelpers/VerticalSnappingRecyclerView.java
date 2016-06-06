package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by QiFeng on 5/15/16.
 */
public class VerticalSnappingRecyclerView extends RecyclerView {

    private boolean mCanBeTouched = true;
    private int mFocusedPosition = 0;

    private RecyclerView.ViewHolder mHolder;

    private OnScrollStoppedListener mOnScrollStoppedListener;


    public VerticalSnappingRecyclerView(Context context) {
        super(context);
    }

    public VerticalSnappingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSnappingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void customScrollToPosition(int position) {
        if (!mCanBeTouched) return;
        mFocusedPosition = position;
        mCanBeTouched = false;

        if (mOnScrollStoppedListener != null){
            mOnScrollStoppedListener.scrollStarted();
        }

        smoothScrollToPosition(position);
    }

    public void setOnScrollStoppedListener(OnScrollStoppedListener l) {
        mOnScrollStoppedListener = l;
    }


    boolean mDragged = false;
    @Override
    public void onScrollStateChanged(int state) {

        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            if (!mCanBeTouched) {
                mDragged = false;
                mCanBeTouched = true;
                if (mOnScrollStoppedListener != null) {
                    mOnScrollStoppedListener.scrollStopped(mFocusedPosition);
                }
            } else {
                LinearLayoutManager llm = (LinearLayoutManager) getLayoutManager();
                if(llm == null) return;

                int first = llm.findFirstVisibleItemPosition();

                View firstItem = llm.findViewByPosition(first);

                if (firstItem == null) return;

                if ((firstItem.getBottom() > (firstItem.getHeight() / 2)) || first == llm.getItemCount() - 1) {
                    mCanBeTouched = false;
                    mFocusedPosition = first;
                    smoothScrollToPosition(first);
                } else {
                    mCanBeTouched = false;
                    mFocusedPosition = first + 1;
                    smoothScrollToPosition(first + 1);
                }
            }
        }else if (state == SCROLL_STATE_DRAGGING){
            mDragged = true;
            if (mOnScrollStoppedListener != null){
                mOnScrollStoppedListener.scrollStarted();
            }
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !mCanBeTouched || super.dispatchTouchEvent(ev);
    }


    public void enableTouchListener(){
        addOnItemTouchListener(new OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (!mCanBeTouched ) return true;
                if (e.getAction() == MotionEvent.ACTION_UP && !mDragged) {
                    LinearLayoutManager llm = (LinearLayoutManager) getLayoutManager();

                    if (llm == null) return false;
                    int f = llm.findFirstCompletelyVisibleItemPosition();

                    if (f == NO_POSITION){
                        f = llm.findFirstVisibleItemPosition();

                        if (f == NO_POSITION) return false;
                    }

                    View v = llm.findViewByPosition(f);

                    if (v.getBottom() < e.getY()){
                        customScrollToPosition(f + 1);
                        return true;
                    }else if (v.getTop() > e.getY()){
                        customScrollToPosition(f - 1);
                        return true;
                    }

                    return false;

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (!mCanBeTouched) return true;

        LinearLayoutManager llm = (LinearLayoutManager) getLayoutManager();
        if (llm == null) return true;

        int first = llm.findFirstVisibleItemPosition();

        if (first == NO_POSITION) return true;

        if (velocityY < 0) {
            if (mFocusedPosition == first){
                if (mFocusedPosition == 0){
                    if (llm.findFirstCompletelyVisibleItemPosition() != first){
                        mCanBeTouched = false;
                        mFocusedPosition = first;
                        smoothScrollToPosition(first);
                    }
                    else {
                        mCanBeTouched = false;
                        stopScroll();
                    }
                }

                else {
                    mCanBeTouched = false;
                    mFocusedPosition = first-1;
                    smoothScrollToPosition(first-1);
                }
            }else {
                if (llm.findFirstCompletelyVisibleItemPosition() == 0){
                    mFocusedPosition = first;
                    mDragged = false;
                }else {
                    mCanBeTouched = false;
                    mFocusedPosition = first;
                    smoothScrollToPosition(first);
                }
            }

        } else {
            mCanBeTouched = false;
            mFocusedPosition = first+1;
            smoothScrollToPosition(first+1);
        }

        return true;
    }


    public ViewHolder getHolder() {
        return mHolder;
    }

    public void setHolder(ViewHolder holder) {
        mHolder = holder;
    }

    public interface OnScrollStoppedListener {
        void scrollStopped(int position);
        void scrollStarted();
    }

}
