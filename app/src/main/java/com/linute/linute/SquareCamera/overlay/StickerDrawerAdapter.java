package com.linute.linute.SquareCamera.overlay;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.linute.linute.R;

import java.util.ArrayList;

/**
 * Created by mikhail on 6/28/16.
 */
public class StickerDrawerAdapter extends RecyclerView.Adapter<StickerDrawerAdapter.StickerVH> {

    private ArrayList<Bitmap> stickers;
    private StickerListener mStickerListener;

    public StickerDrawerAdapter() {
        this.stickers = new ArrayList<>();
    }

    public StickerDrawerAdapter(ArrayList<Bitmap> stickers) {
        this.stickers = stickers;
    }

    @Override
    public StickerVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.layout_sticker, parent, false);
        return new StickerVH(itemView);
    }

    @Override
    public void onBindViewHolder(StickerVH holder, int position) {
        final Bitmap sticker = stickers.get(position);
        holder.bind(sticker);
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStickerListener.onStickerSelected(stickers.get((int)view.getTag()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    public void setStickerListener(StickerListener listener){
        mStickerListener = listener;
    }

    public static class StickerVH extends RecyclerView.ViewHolder {

        ImageView stickerIV;

        public StickerVH(View itemView) {
            super(itemView);
            stickerIV = (ImageView)itemView.findViewById(R.id.image_sticker);

        }

        public void bind(Bitmap sticker){
            this.stickerIV.setImageBitmap(sticker);
        }
    }

    public interface StickerListener {
        public void onStickerSelected(Bitmap sticker);
    }

}
