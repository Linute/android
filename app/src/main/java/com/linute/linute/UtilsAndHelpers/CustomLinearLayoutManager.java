package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by QiFeng on 2/15/16.
 */
public class CustomLinearLayoutManager extends LinearLayoutManager {

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        final LinearSmoothScroller linearSmoothScroller =
                new LinearSmoothScroller(recyclerView.getContext()) {
                    private static final float MILLISECONDS_PER_INCH =  200f;

                    @Override
                    public PointF computeScrollVectorForPosition(int targetPosition) {
                        return CustomLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
                    }

                    @Override
                    protected float calculateSpeedPerPixel
                            (DisplayMetrics displayMetrics) {
                        return MILLISECONDS_PER_INCH / getItemCount() / displayMetrics.densityDpi;
                    }
                };

        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}
