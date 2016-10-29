package com.linute.linute.MainContent.EditScreen.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.linute.linute.MainContent.EditScreen.PostOptions.ContentType;
import com.linute.linute.MainContent.EditScreen.ProcessingOptions;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.overlay.ManipulableImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mikhail on 8/23/16.
 */
public class StickersTool extends EditContentTool {


    ArrayList<Sticker> mStickers;
    private StickersAdapter mStickersAdapter;
    private RecyclerView mStickersRV;
    private FrameLayout mStickersContainer;
    private ManipulableImageView.ViewManipulationListener mManipulationListener;
    private final ImageView mTrashCanIV;
    private ArrayList<PlacedSticker> mPlacedStickers = new ArrayList<>();


    public StickersTool(Uri uri, ContentType type, ViewGroup overlaysView, ImageView trashCan) {
        super(uri, type, overlaysView);

        View container = LayoutInflater.from(overlaysView.getContext()).inflate(R.layout.tool_overlay_stickers, overlaysView, false);


        mStickers = new ArrayList<>();

        mStickersContainer = (FrameLayout) container.findViewById(R.id.layout_stickers);

        mTrashCanIV = trashCan;
        mTrashCanIV.setVisibility(View.GONE);
        mTrashCanIV.setLayoutParams(new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mTrashCanIV.getResources().getDisplayMetrics());
        mTrashCanIV.setPadding(pad, pad, pad, pad);


        mManipulationListener = new ManipulableImageView.ViewManipulationListener() {
            @Override
            public void onViewPickedUp(View me) {
                mTrashCanIV.setVisibility(View.VISIBLE);
                me.bringToFront();
            }

            @Override
            public void onViewDropped(View me) {
                mTrashCanIV.setVisibility(View.GONE);
                mTrashCanIV.setColorFilter(null);

            }

            @Override
            public void onViewCollisionBegin(View me) {
                mTrashCanIV.setColorFilter(new PorterDuffColorFilter(me.getResources().getColor(R.color.secondaryColor), PorterDuff.Mode.MULTIPLY));
                mTrashCanIV.setImageResource(R.drawable.trash_icon);
            }

            @Override
            public void onViewCollisionEnd(View me) {
                mTrashCanIV.setImageResource(R.drawable.trash_icon_closed);
                mTrashCanIV.setColorFilter(null);
            }

            @Override
            public void onViewDropCollision(View me) {
                mStickersContainer.removeView(me);
                mPlacedStickers.remove(me);
                mTrashCanIV.setImageResource(R.drawable.trash_icon_closed);
            }

            @Override
            public View getCollisionSensor() {
                return mTrashCanIV;
            }
        };

        mOverlaysView.addView(container);
    }

    @Override
    public void onOpen() {
        super.onOpen();
        for (PlacedSticker sticker : mPlacedStickers) {
            sticker.imageView.setDrawBorder(false);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        for (PlacedSticker sticker : mPlacedStickers) {
            sticker.imageView.setDrawBorder(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Sticker sticker : mStickers) {
            sticker.bitmap.recycle();
        }
    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {
        mStickersRV = new RecyclerView(parent.getContext());

        StickerVH.containerHeight = parent.getHeight();

        mStickersAdapter = new StickersAdapter(mStickers);
        mStickersAdapter.setOnStickerTouchListener(onStickerTouchListener);

        mStickersRV.setAdapter(mStickersAdapter);
        mStickersRV.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false));


        initStickersAsync(parent.getContext());


        return mStickersRV;
    }


    @Override
    public void processContent(Uri uri, ContentType contentType, ProcessingOptions options) {
        mTrashCanIV.setVisibility(View.GONE);
        options.stickers.clear();
        for(PlacedSticker sticker:mPlacedStickers){
            options.stickers.add(sticker.fileName);
        }
    }

    @Override
    public String getName() {
        return "Stickers";
    }

    @Override
    public int getDrawable() {
        return R.drawable.sticker_icon_selected;
    }


    private StickersAdapter.OnStickerTouchListener onStickerTouchListener = new StickersAdapter.OnStickerTouchListener() {
        @Override
        public boolean onStickerTouch(View view, int index, Sticker sticker) {


            ManipulableImageView stickerIV = new ManipulableImageView(mOverlaysView.getContext());
            stickerIV.setImageBitmap(sticker.bitmap);

            stickerIV.setManipulationListener(mManipulationListener);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            stickerIV.setLayoutParams(params);


            mStickersContainer.addView(stickerIV);
            mPlacedStickers.add(new PlacedSticker(sticker, stickerIV));

            /*stickerIV.setX(view.getX());
            stickerIV.setY(view.getY());*/

            return false;
        }
    };

    protected static class StickersAdapter extends RecyclerView.Adapter<StickerVH> {

        ArrayList<Sticker> stickers;

        int mSelectedItem;


        OnStickerTouchListener mOnTouchListener;


        public StickersAdapter(ArrayList<Sticker> stickers) {
            this.stickers = stickers;
        }

        @Override
        public StickerVH onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView view = new ImageView(parent.getContext());
//            int height = parent.getHeight();
//            int width = parent.getContext().getResources().getDisplayMetrics().widthPixels/5;
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setPadding(16, 16, 16, 16);
            return new StickerVH(view);
        }

        @Override
        public void onBindViewHolder(final StickerVH holder, int position) {
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
            boolean onStickerTouch(View view, int index, Sticker sticker);
        }

    }


    protected static class StickerVH extends RecyclerView.ViewHolder {

        ImageView vPreview;
        public static int containerHeight = 0;

        public StickerVH(View itemView) {
            super(itemView);
            vPreview = (ImageView) itemView;
        }

        public void bind(Sticker sticker, boolean isSelected) {
            Bitmap overlay = sticker.bitmap;
            vPreview.setImageBitmap(overlay);
            vPreview.setLayoutParams(new RecyclerView.LayoutParams(overlay.getWidth() * containerHeight / overlay.getHeight(), containerHeight));
            vPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private void initStickersAsync(final Context context) {
        final File memeDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "memes/");
        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                File[] memes = memeDir.listFiles();
                if (memes != null && !mDestroyed) {
                    for (File f : memes) {
                        if (mDestroyed)
                            break;

                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(f.getAbsolutePath(), options);

                        try {
                            if (options.outWidth != 0) {
//                            float scale = (float) metrics.widthPixels/ 5 / b.getWidth();

                                options.inJustDecodeBounds = false;
                                options.inSampleSize = options.outWidth/(metrics.widthPixels / 5);
                                Log.i("AAA", "STICKER SCALE = " + options.inSampleSize);


                                mStickers.add(
                                        new Sticker(
                                                f.getName(),
                                                BitmapFactory.decodeFile(f.getAbsolutePath(), options)
                                        )
                                );

                            }
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

                if (mDestroyed) {
                    for (Sticker bitmap : mStickers) {
                        if (bitmap != null && bitmap.bitmap != null) {
                            bitmap.bitmap.recycle();
                        }
                    }
                }
            }

        }).start();
    }


    protected static class Sticker {
        public String fileName;
        public Bitmap bitmap;

        public Sticker(String fileName, Bitmap bitmap) {
            this.fileName = fileName;
            this.bitmap = bitmap;
        }

        public Sticker(Sticker copy) {
            this.fileName = copy.fileName;
            this.bitmap = copy.bitmap;
        }
    }

    protected static class PlacedSticker extends Sticker {

        public final ManipulableImageView imageView;

        public PlacedSticker(Sticker copy, ManipulableImageView imageView) {
            super(copy);
            this.imageView = imageView;
        }
    }


}
