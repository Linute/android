package com.linute.linute.MainContent.EditScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.linute.linute.R;
import com.linute.linute.SquareCamera.overlay.ManipulableImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mikhail on 8/23/16.
 */
public class StickersTool extends EditContentTool {


    ArrayList<Bitmap> mStickers;
    private OverlaysAdapter mStickersAdapter;
    private RecyclerView mStickersRV;
    private FrameLayout mStickersContainer;

    public StickersTool(Uri uri, EditFragment.ContentType type, ViewGroup overlaysView) {
        super(uri, type, overlaysView);
        mStickers = new ArrayList<>();


        mStickersContainer = new FrameLayout(overlaysView.getContext());
        mStickersContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mOverlaysView.addView(mStickersContainer);

    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        mStickersRV = new RecyclerView(parent.getContext());


        mStickersAdapter = new OverlaysAdapter(mStickers);
        mStickersAdapter.setOnStickerTouchListener(onStickerTouchListener);

        mStickersRV.setAdapter(mStickersAdapter);
        mStickersRV.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));


        initStickersAsync(parent.getContext());


        return mStickersRV;
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

    }

    @Override
    public String getName() {
        return "Stickers";
    }

    @Override
    public int getDrawable() {
        return R.drawable.sticker_icon;
    }


    private OverlaysAdapter.OnStickerTouchListener onStickerTouchListener = new OverlaysAdapter.OnStickerTouchListener() {
        @Override
        public boolean onStickerTouch(View view, int index, Bitmap bitmap) {


            ManipulableImageView stickerIV = new ManipulableImageView(mOverlaysView.getContext());
            stickerIV.setImageBitmap(bitmap);


            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            stickerIV.setLayoutParams(params);



            mStickersContainer.addView(stickerIV);

            /*stickerIV.setX(view.getX());
            stickerIV.setY(view.getY());*/

            return false;
        }
    };

    protected static class OverlaysAdapter extends RecyclerView.Adapter<OverlayItemVH> {

        ArrayList<Bitmap> stickers;

        int mSelectedItem;



        OnStickerTouchListener mOnTouchListener;


        public OverlaysAdapter(ArrayList<Bitmap> stickers) {
            this.stickers = stickers;
        }

        @Override
        public OverlayItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView view = new ImageView(parent.getContext());
            int height = parent.getHeight();
            int width = height / 6 * 5;
            view.setLayoutParams(new RecyclerView.LayoutParams(width, height));
            return new OverlayItemVH(view);
        }

        @Override
        public void onBindViewHolder(final OverlayItemVH holder, int position) {
            holder.bind(stickers.get(position), mSelectedItem == position);
           /* holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int index = holder.getAdapterPosition();
                    return mOnTouchListener.onStickerTouch(view, index, stickers.get(index));
                }
            });*/
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = holder.getAdapterPosition();
                    mOnTouchListener.onStickerTouch(view, index, stickers.get(index));

                }
            });
        }


        @Override
        public int getItemCount() {
            return stickers.size();
        }

        public void setOnStickerTouchListener(OnStickerTouchListener onTouchListener) {
            this.mOnTouchListener = onTouchListener;
        }

        interface OnStickerTouchListener {
            public boolean onStickerTouch(View view, int index, Bitmap bitmap);
        }

    }




    protected static class OverlayItemVH extends RecyclerView.ViewHolder {

        ImageView vPreview;

        public OverlayItemVH(View itemView) {
            super(itemView);
            vPreview = (ImageView) itemView;
        }

        public void bind(Bitmap overlay, boolean isSelected) {
            vPreview.setImageBitmap(overlay);
        }
    }

    private void initStickersAsync(final Context context) {
        final File memeDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "memes/");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                File[] memes = memeDir.listFiles();
                if (memes != null) {
                    for (File f : memes) {
                        try {
                            mStickers.add(BitmapFactory.decodeFile(f.getAbsolutePath(), options));

                           /* mStickersRV.post(new Runnable() {
                                @Override
                                public void run() {
                                    mStickersAdapter.notifyItemInserted(mStickers.size() - 1);
                                }
                            });*/

                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                        } catch (NullPointerException np) {
                            np.printStackTrace();
                        }
                    }
                }
                mStickersRV.post(new Runnable() {
                    @Override
                    public void run() {
                        mStickersAdapter.notifyDataSetChanged();
                    }
                });
            }

        }).start();


    }
}
