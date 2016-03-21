package com.linute.linute.MainContent.ProfileFragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by QiFeng on 2/9/16.
 */
public class EmptyProfileHolder extends RecyclerView.ViewHolder {

    public EmptyProfileHolder(View itemView) {
        super(itemView);
        ((ImageView) itemView.findViewById(R.id.empty_cell_bg_image)).setImageResource(R.drawable.no_profile);
        ((TextView) itemView.findViewById(R.id.empty_cell_bg_text)).setText(R.string.profile_no_posts);
    }

}
