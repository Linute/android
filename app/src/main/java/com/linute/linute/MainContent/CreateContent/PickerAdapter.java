package com.linute.linute.MainContent.CreateContent;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by QiFeng on 8/30/16.
 */
public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.GalleryViewHolder> {


    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder{

        public GalleryViewHolder(View itemView) {
            super(itemView);
        }
    }
}
