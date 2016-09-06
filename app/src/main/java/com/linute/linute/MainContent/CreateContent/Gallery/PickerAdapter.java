package com.linute.linute.MainContent.CreateContent.Gallery;

import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.linute.linute.R;

import java.util.ArrayList;

/**
 * Created by QiFeng on 8/30/16.
 */
public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.GalleryViewHolder> {

    private ArrayList<GalleryItem> mGalleryItems;
    private GalleryItemSelected mGalleryItemSelected;
    private RequestManager mRequestManager;


    public PickerAdapter(ArrayList<GalleryItem> items) {
        mGalleryItems = items;
    }

    public void setGalleryItemSelected(GalleryItemSelected galleryItemSelected) {
        mGalleryItemSelected = galleryItemSelected;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GalleryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false));
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        holder.bindView(mGalleryItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mGalleryItems.size();
    }

    public class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView vImageView;
        View vVideoIcon;
        GalleryItem mGalleryItem;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            vImageView = (ImageView) itemView.findViewById(R.id.image);
            vVideoIcon = itemView.findViewById(R.id.play);
        }


        public void bindView(GalleryItem item) {
            mGalleryItem = item;

            vVideoIcon.setVisibility(
                    item.type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ?
                            View.VISIBLE :
                            View.GONE
            );

            mRequestManager.load(item.path)
                    .into(vImageView);

        }

        @Override
        public void onClick(View v) {
            if (mGalleryItemSelected != null) {
                mGalleryItemSelected.itemClicked(mGalleryItem);
            }
        }
    }


    public interface GalleryItemSelected {
        void itemClicked(GalleryItem item);
    }


}
