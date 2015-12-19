package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by QiFeng on 12/4/15.
 */
public class RecyclerViewWithEmptyListSupport extends RecyclerView {


    private View mEmptyView; //what's shown when view is empty

    private AdapterDataObserver mEmptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter =  getAdapter();
            if(adapter != null && mEmptyView != null) {
                if(adapter.getItemCount() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    RecyclerViewWithEmptyListSupport.this.setVisibility(View.GONE);
                }
                else {
                    mEmptyView.setVisibility(View.GONE);
                    RecyclerViewWithEmptyListSupport.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };


    public RecyclerViewWithEmptyListSupport(Context context) {
        super(context);
    }


    public RecyclerViewWithEmptyListSupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewWithEmptyListSupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter != null) {
            adapter.registerAdapterDataObserver(mEmptyObserver);
        }

        mEmptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
    }
}