package com.linute.linute.MainContent.EditScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mikhail on 8/23/16.
 */
public class OverlaysTool extends EditContentTool {

    ArrayList<Overlay> mOverlays;
    private OverlaysAdapter mOverlaysAdapter;
    private RecyclerView mOverlaysRV;
    private final ImageView overlayView;
    private Bitmap mBackingBitmap;
    private RequestManager mRequestManager;

    private Overlay mSelectedOverlay;

    public OverlaysTool(final Uri uri, EditFragment.ContentType type, ViewGroup overlaysView, RequestManager manager) {
        super(uri, type, overlaysView);

        //image
        if (type == EditFragment.ContentType.Photo || type == EditFragment.ContentType.UploadedPhoto)
            setImageBackground(uri, overlaysView.getResources().getDisplayMetrics().widthPixels);
        else  //video
            setVideoBackground(uri, overlaysView.getResources().getDisplayMetrics().widthPixels);

        mRequestManager = manager;

        mOverlays = new ArrayList<>();
        mOverlays.add(new Overlay(null, null, null));
        overlayView = new ImageView(overlaysView.getContext());
        overlayView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mOverlaysView.addView(overlayView);
        initFiltersAsync(overlaysView.getContext());

    }

    private void setImageBackground(final Uri uri, int width) {

        final BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri.getPath(), opts);
        opts.inSampleSize = opts.outWidth / (width / 5);
        Log.i("AAA","BACK SCALE="+opts.inSampleSize);
        opts.inJustDecodeBounds = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mDestroyed) {
                    mBackingBitmap = BitmapFactory.decodeFile(uri.getPath(), opts);
                }

                //do after decoding
                if (mDestroyed) {
                    mBackingBitmap.recycle();
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOverlaysAdapter != null) {
                                mOverlaysAdapter.setBackBitmap(mBackingBitmap);
                                mOverlaysAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void setVideoBackground(Uri uri, final int screenwidth) {

        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.getPath());

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap frame = retriever.getFrameAtTime(0);

                if (!mDestroyed) {
                    float scale = (float) screenwidth / 5 / frame.getWidth();
                    mBackingBitmap = Bitmap.createScaledBitmap(frame, (int) (scale * frame.getWidth()), (int) (scale * frame.getHeight()), false);
                    if (mBackingBitmap != frame) frame.recycle();

                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (mOverlaysAdapter != null) {
                                        mOverlaysAdapter.setBackBitmap(mBackingBitmap);
                                        mOverlaysAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                }

                //check one more time after finishing
                if (mDestroyed) {
                    mBackingBitmap.recycle();
                }

            }
        }).start();

    }

//    public OverlaysTool(Uri uri, EditFragment.ContentType type, ViewGroup overlaysView, RequestManager manager) {
//        super(uri, type, overlaysView);
//        mRequestManager = manager;
//
//
//        mOverlays = new ArrayList<>();
//        mOverlays.add(null);
//        overlayView = new ImageView(overlaysView.getContext());
//        overlayView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        mOverlaysView.addView(overlayView);
//        initFiltersAsync(overlaysView.getContext());
//    }

//    public void setBackingBitmap(Bitmap back) {
//        mBackingBitmap = back;
//        mOverlaysAdapter.setBackBitmap(mBackingBitmap);
//        mOverlaysAdapter.notifyDataSetChanged();
//    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        mOverlaysRV = new RecyclerView(parent.getContext());

        mOverlaysAdapter = new OverlaysAdapter(mOverlays);
        mOverlaysAdapter.setBackBitmap(mBackingBitmap);

        mOverlaysAdapter.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                /*if(mOverlaysFullSize.size() <= i){
                    mOverlaysFullSize.ensureCapacity(i+1);
                }
                if(mOverlaysFullSize.get(i) == null){
                    mOverlaysFullSize.set(i, Bitmap.createBitmap());
                }*/
                Overlay overlay = mOverlays.get(i);
                if (overlay != null)
                    mRequestManager
                            .load(overlay.file)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(overlayView);

                mSelectedOverlay = overlay;
            }
        });

        mOverlaysRV.setAdapter(mOverlaysAdapter);
        mOverlaysRV.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));
        mOverlaysRV.addItemDecoration(new SpaceItemDecoration(24));


        return mOverlaysRV;
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {
        options.filters.clear();
        if (mSelectedOverlay != null) {
            options.filters.add(mSelectedOverlay.filename);
        }
    }

    @Override
    public String getName() {
        return "Filters";
    }

    @Override
    public int getDrawable() {
        return R.drawable.filters_icon_selected;
    }


    protected static class OverlaysAdapter extends RecyclerView.Adapter<OverlayItemVH> {

        Bitmap mBackBitmap;
        ArrayList<Overlay> overlays;
        int mSelectedItem;

        OnItemSelectedListener mOnItemSelectedListener;


        public OverlaysAdapter(ArrayList<Overlay> overlays) {
            this.overlays = overlays;
        }

        protected void setBackBitmap(Bitmap bitmap) {
            mBackBitmap = bitmap;
        }

        @Override
        public OverlayItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_overlay, parent, false);
            int height = parent.getHeight();
            int width = height / 6 * 5;
            v.setLayoutParams(new RecyclerView.LayoutParams(width, height));
            return new OverlayItemVH(v);
        }

        @Override
        public void onBindViewHolder(final OverlayItemVH holder, int position) {
            holder.bind(overlays.get(position), mSelectedItem == position, mBackBitmap);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectItem(holder.getAdapterPosition());
                }
            });
        }

        public void selectItem(int index) {
            int oldSelection = mSelectedItem;
            mSelectedItem = index;

            notifyItemChanged(oldSelection);
            notifyItemChanged(mSelectedItem);

            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelected(index);
            }

        }

        @Override
        public int getItemCount() {
            return overlays.size();
        }

        public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
            this.mOnItemSelectedListener = onItemSelectedListener;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        overlayView.setImageBitmap(null);

        for (Overlay overlay : mOverlays) {
            if (overlay != null)
                overlay.recycle();
        }

        mRequestManager.onDestroy();
    }

    protected static class OverlayItemVH extends RecyclerView.ViewHolder {

        ImageView vOverlay;
        ImageView vBackground;

        public OverlayItemVH(View itemView) {
            super(itemView);
            vOverlay = (ImageView) itemView.findViewById(R.id.image_overlay);

            vBackground = ((ImageView) itemView.findViewById(R.id.image_back));
        }

        public void bind(Overlay overlay, boolean isSelected, Bitmap backBitmap) {
            vOverlay.setImageBitmap(overlay == null ? null : overlay.thumbnail);
            vBackground.setImageBitmap(backBitmap);
        }
    }

    private void initFiltersAsync(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                final File filterDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filters/");
                final DisplayMetrics metrics = context.getResources().getDisplayMetrics();


                final BitmapFactory.Options measureOptions = new BitmapFactory.Options();
                measureOptions.inJustDecodeBounds = true;
                final BitmapFactory.Options options = new BitmapFactory.Options();
                File[] filters = filterDir.listFiles();
                if (filters != null && !mDestroyed) {
                    for (File f : filters) {
                        if (mDestroyed)
                            break;

                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                        if (options.outWidth != 0) {
//                            float scale = (float) metrics.widthPixels/ 5 / b.getWidth();

                            options.inJustDecodeBounds = false;
                            options.inSampleSize =  options.outWidth/(metrics.widthPixels / 5) + 1;
                            Log.i("AAA", "OVERLAY SCALE = "+options.inSampleSize);
                            try {
                                Bitmap scaled = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                                mOverlays.add(new Overlay(f.getName(), f, scaled));
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            } catch (NullPointerException np) {
                                np.printStackTrace();
                            }
                        }

                        if (mOverlaysAdapter != null) {
                            mOverlaysRV.post(new Runnable() {
                                @Override
                                public void run() {
                                    mOverlaysAdapter.notifyItemInserted(mOverlays.size() - 1);
                                }
                            });
                        }
                    }
                }

                if (mDestroyed) {
                    for (Overlay bitmap : mOverlays) {
                        if (bitmap != null)
                            bitmap.recycle();
                    }
                }
            }
        }).start();


    }

    @Override
    public void onDisable() {
        super.onDisable();
        overlayView.setVisibility(View.GONE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        overlayView.setVisibility(View.VISIBLE);
    }

    interface OnItemSelectedListener {
        void onItemSelected(int i);
    }

    protected static class Overlay {
        String filename;
        File file;
        Bitmap thumbnail;

        public Overlay(String filename, File file, Bitmap thumbnail) {
            this.filename = filename;
            this.file = file;
            this.thumbnail = thumbnail;
        }


        public void recycle() {
            if (thumbnail != null) thumbnail.recycle();
        }

    }
}
