package com.linute.linute.MainContent.EditScreen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mikhail on 8/23/16.
 */
public class OverlaysTool extends EditContentTool {

    ArrayList<Bitmap> mOverlays;
    private OverlaysAdapter mOverlaysAdapter;
    private RecyclerView mOverlaysRV;
    private final ImageView overlayView;

    public OverlaysTool(Uri uri, EditFragment.ContentType type, ViewGroup overlaysView) {
        super(uri, type, overlaysView);
        mOverlays = new ArrayList<>();
        overlayView = new ImageView(overlaysView.getContext());
        overlayView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mOverlaysView.addView(overlayView);
        initFiltersAsync(overlaysView.getContext());

    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        mOverlaysRV = new RecyclerView(parent.getContext());

        mOverlaysAdapter = new OverlaysAdapter(mOverlays, mUri);
        mOverlaysAdapter.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                overlayView.setImageBitmap(mOverlays.get(i));
            }
        });

        mOverlays.add(null);
        mOverlaysRV.setAdapter(mOverlaysAdapter);
        mOverlaysRV.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));
        mOverlaysRV.addItemDecoration(new SpaceItemDecoration(24));


        return mOverlaysRV;
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

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

        private final Uri mUri;
        ArrayList<Bitmap> overlays;

        int mSelectedItem;


        OnItemSelectedListener mOnItemSelectedListener;


        public OverlaysAdapter(ArrayList<Bitmap> overlays, Uri uri) {
            this.overlays = overlays;
            mUri = uri;
        }

        @Override
        public OverlayItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_overlay, parent, false);
            int height = parent.getHeight();
            int width = height / 6 * 5;
            ((ImageView)v.findViewById(R.id.image_back)).setImageURI(mUri);
            v.setLayoutParams(new RecyclerView.LayoutParams(width, height));
            return new OverlayItemVH(v);
        }

        @Override
        public void onBindViewHolder(final OverlayItemVH holder, int position) {
            holder.bind(overlays.get(position), mSelectedItem == position);
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


    protected static class OverlayItemVH extends RecyclerView.ViewHolder {

        ImageView vBack;
        ImageView vOverlay;

        public OverlayItemVH(View itemView) {
            super(itemView);
            vOverlay = (ImageView) itemView.findViewById(R.id.image_overlay);
        }

        public void bind(Bitmap overlay, boolean isSelected) {
            vOverlay.setImageBitmap(overlay);
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
                if (filters != null) {
                    for (File f : filters) {
                        Bitmap b = null;
                        Bitmap scaled = null;
                        b = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                        if (b != null) {
                            float scale = (float) metrics.widthPixels / b.getWidth();

                            scaled = Bitmap.createScaledBitmap(b, (int) (b.getWidth() * scale), (int) (b.getHeight() * scale), false);
                            try {
                                mOverlays.add(scaled);
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            } catch (NullPointerException np) {
                                np.printStackTrace();
                            } finally {
                                //It turns out the original image may be passed back as an optimisation,
                                // if the width/height of the resize match the original image
                                if (b != scaled) {
                                    b.recycle();
                                }
                            }

                        }

                        if(mOverlaysAdapter != null) {
                            mOverlaysRV.post(new Runnable() {
                                @Override
                                public void run() {
                                    mOverlaysAdapter.notifyItemInserted(mOverlays.size() - 1);
                                }
                            });
                        }


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

    interface OnItemSelectedListener{
        void onItemSelected(int i);
    }
}
