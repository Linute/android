package com.linute.linute.MainContent.DiscoverFragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 2/9/16.
 */
public class EmptyPostDetailHolder extends RecyclerView.ViewHolder {

    public EmptyPostDetailHolder(View itemView) {
        super(itemView);
        ((ImageView) itemView.findViewById(R.id.empty_cell_bg_image)).setImageResource(R.drawable.campus);
    }
}
