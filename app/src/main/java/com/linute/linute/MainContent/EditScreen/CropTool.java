package com.linute.linute.MainContent.EditScreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/23/16.
 */
public class CropTool extends EditContentTool {


    public static final int FADE_OPEN_OPACITY = 0x33000000;
    public static final int FADE_CLOSE_OPACITY = 0xFF000000;
    public static final int INDEX_CUSTOM_CROP = 2;
    public static final int INDEX_SQUARE_CROP = 1;
    public static final int INDEX_OG_CROP = 0;
    private final MoveZoomImageView mActivatable;
    private final EditFragment.RequestDisableToolListener reqDisableToolListener;
    private final View mContentView;
    /*measurements taken from bottom (0 in bottom and top = full image)*/
    private int mTopY = 0;
    private int mBotY = 0;

    private View topBar;
    private View botBar;
    private View topFade;
    private View botFade;
    private View topHandle;
    private View botHandle;


    private final View mCropperLayout;

    public int MIN_SIZE = 300;
    public int MAX_SIZE = 600;
    public int BOT_BOUND = 0;
    public int TOP_BOUND = 0;


    public boolean MOVE_OTHER_BAR = false;

    CropMode[] mCropModes;
    ViewGroup[] mCropModeViews;
    private int mSelected;

    Dimens mDimens;

    private int mFadeBaseColor;
    private Rect imageBounds;


    public CropTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays, MoveZoomImageView activatable, Dimens dimens, EditFragment.RequestDisableToolListener listener, View contentView) {
        super(uri, type, overlays);
        mActivatable = activatable;
        mDimens = dimens;

        mContentView = contentView;

        mCropperLayout = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tools_overlay_crop, mOverlaysView, false);
        ((ViewGroup) (mOverlaysView.getParent())).addView(mCropperLayout);

        topBar = mCropperLayout.findViewById(R.id.top_bar);
        botBar = mCropperLayout.findViewById(R.id.bot_bar);
        topBar.setVisibility(View.GONE);
        botBar.setVisibility(View.GONE);

        topFade = mCropperLayout.findViewById(R.id.top_fade);
        botFade = mCropperLayout.findViewById(R.id.bot_fade);

        topHandle = mCropperLayout.findViewById(R.id.top_handle);
        botHandle = mCropperLayout.findViewById(R.id.bot_handle);
        topHandle.setVisibility(View.GONE);
        botHandle.setVisibility(View.GONE);
        updateCropperView();

        reqDisableToolListener = listener;
        mFadeBaseColor = 0;//mOverlaysView.getContext().getResources().getColor(R.color.colorPrimary);


        DisplayMetrics metrics = mOverlaysView.getContext().getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int height = mDimens.height * displayWidth / mDimens.width;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 5;
        Bitmap backingBitmap = BitmapFactory.decodeFile(uri.getPath(), opts);

        float dimenRatio = (float) mDimens.height / mDimens.width;
        mCropModes = new CropMode[]{
                (dimenRatio == 6.0 / 5) ?
                        new CropMode("Default", backingBitmap, true, displayWidth * 6 / 5, displayWidth * 6 / 5) {  //6*5
                            @Override
                            public void onSelected() {
                                super.onSelected();
                                topHandle.setVisibility(View.GONE);
                                botHandle.setVisibility(View.GONE);
//                        topFade.setVisibility(View.GONE);
//                        botFade.setVisibility(View.GONE);
                                topBar.setVisibility(View.GONE);
                                botBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onUnSelected() {
                                super.onUnSelected();
                                topHandle.setVisibility(View.VISIBLE);
                                botHandle.setVisibility(View.VISIBLE);
//                        topFade.setVisibility(View.VISIBLE);
//                        botFade.setVisibility(View.VISIBLE);
                                topBar.setVisibility(View.VISIBLE);
                                botBar.setVisibility(View.VISIBLE);
                            }
                        }
                        :
                        new CropMode("Default",backingBitmap, true, (int) (displayWidth * dimenRatio), (int) (displayWidth * dimenRatio))
                ,        //no crop
                new CropMode("Square", backingBitmap, true, displayWidth, displayWidth),   //square
                new CropMode("Custom", backingBitmap, false, displayWidth / 16 * 9, height){
                    @Override
                    public void onSelected() {
                        int height = ((View) mOverlaysView.getParent()).getHeight();
                        mTopY = mBotY = (height - minHeight) / 2;
                        super.onSelected();
                    }
                } //freeform
        };

        mActivatable.setManipulationListener(new MoveZoomImageView.ViewManipulationListener() {
            @Override
            public void onViewMoved(View me) {
                imageBounds = new Rect();
                ((MoveZoomImageView) me).getImageBounds(imageBounds);
//                TOP_BOUND = BOT_BOUND = (mCropperLayout.getHeight()-rect.height())/2;

                updateCropperBounds();
                boundCropper();
                updateCropperView();
                updateCropperModes(imageBounds);

            }

            @Override
            public void onViewPickedUp(View me) {

            }

            @Override
            public void onViewDropped(View me) {
                ((MoveZoomImageView) me).getImageBounds(imageBounds);

                updateCropperBounds();
                boundCropper();
                updateCropperView();
                updateCropperModes(imageBounds);

//                if (reqDisableToolListener != null)
//                    reqDisableToolListener.requestDisable(OverlaysTool.class, mTopY != 0 || mBotY != 0 || mCropperLayout.getHeight() != mCropperLayout.getWidth()*6/5);
            }

            @Override
            public void onViewCollisionBegin(View me) {

            }

            @Override
            public void onViewCollisionEnd(View me) {

            }

            @Override
            public void onViewDropCollision(View me) {

            }

            @Override
            public View getCollisionSensor() {
                return null;
            }
        });

        mCropModes[0].apply();

    }

    public void updateCropperBounds() {
        int cropperLayoutHeight = mCropperLayout.getHeight();
        if(cropperLayoutHeight == 0) return;

        TOP_BOUND = Math.max(imageBounds.top, 0);
        BOT_BOUND = Math.max(cropperLayoutHeight - imageBounds.bottom, 0);


        int imgHeight = Math.min(imageBounds.bottom, mContentView.getHeight()) - Math.max(imageBounds.top, 0);
        mCropModes[INDEX_OG_CROP].minHeight = imgHeight;
        mCropModes[INDEX_OG_CROP].maxHeight = imgHeight;

        mCropModes[INDEX_CUSTOM_CROP].maxHeight = imgHeight;
        mCropModes[mSelected].apply();

    }

    public void boundCropper() {
        int cropperLayoutHeight = mCropperLayout.getHeight();

        if(cropperLayoutHeight == 0) return;

        int imageHeight = cropperLayoutHeight - mBotY - mTopY;

        if (imageHeight > MAX_SIZE) {
            mTopY = mBotY = (cropperLayoutHeight - MAX_SIZE) / 2;

        }else if(imageHeight<MIN_SIZE){
            mTopY = mBotY = (cropperLayoutHeight - MIN_SIZE) / 2;
        }



        if (mBotY < BOT_BOUND) {
            mBotY = BOT_BOUND;


            if (imageHeight > MAX_SIZE) {
                mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                if (mTopY < TOP_BOUND) {
                    mTopY = TOP_BOUND;
                    mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                }
            }

            if (imageHeight < MIN_SIZE) {
                mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                if (mTopY < TOP_BOUND) {
                    mTopY = TOP_BOUND;
                    mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                }
            }


        } else if (mTopY < TOP_BOUND) {
            mTopY = TOP_BOUND;

            if (imageHeight > MAX_SIZE) {
                mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                if (mBotY < BOT_BOUND) {
                    mBotY = BOT_BOUND;
                    mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                }
            }

            if (imageHeight < MIN_SIZE) {
                mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                if (mBotY < BOT_BOUND) {
                    mBotY = BOT_BOUND;
                    mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                }
            }
        }
    }


    View.OnTouchListener touchListener = new View.OnTouchListener() {
        float startY;
        int initBarY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startY = motionEvent.getRawY();
                    if (view == botHandle) {
                        initBarY = mBotY;
                    } else {
                        initBarY = mTopY;
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int cropperLayoutHeight = mCropperLayout.getHeight();
                    if (view == botHandle) {
                        mBotY = (int) (initBarY + (startY - motionEvent.getRawY()));
                        if (mBotY < BOT_BOUND) mBotY = BOT_BOUND;

                        int imageHeight = cropperLayoutHeight - mBotY - mTopY;

                        if (imageHeight > MAX_SIZE) {
                            if (MOVE_OTHER_BAR) {
                                mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                                if (mTopY < TOP_BOUND) {
                                    mTopY = TOP_BOUND;
                                    mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                                }
                            } else {
                                mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                            }
                        }

                        if (imageHeight < MIN_SIZE) {
                            if (MOVE_OTHER_BAR) {
                                mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                                if (mTopY < TOP_BOUND) {
                                    mTopY = TOP_BOUND;
                                    mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                                }
                            } else {
                                mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                            }
                        }


                    } else {
                        mTopY = (int) (initBarY + (motionEvent.getRawY() - startY));
                        if (mTopY < TOP_BOUND) mTopY = TOP_BOUND;
                        int imageHeight = cropperLayoutHeight - mBotY - mTopY;

                        if (imageHeight > MAX_SIZE) {
                            if (MOVE_OTHER_BAR) {
                                mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                                if (mBotY < BOT_BOUND) {
                                    mBotY = BOT_BOUND;
                                    mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                                }
                            } else {
                                mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                            }
                        }

                        if (imageHeight < MIN_SIZE) {
                            if (MOVE_OTHER_BAR) {
                                mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                                if (mBotY < BOT_BOUND) {
                                    mBotY = BOT_BOUND;
                                    mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                                }
                            } else {
                                mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                            }
                        }

                    }
                    updateCropperView();
                    return true;
                case MotionEvent.ACTION_UP:
                    startY = -1;
                    initBarY = -1;
                    return true;
            }

            return false;


        }
    };


    private void updateCropperView() {

        ViewGroup.LayoutParams topParam = topFade.getLayoutParams();
        topParam.height = mTopY;
        topFade.setLayoutParams(topParam);


        ViewGroup.LayoutParams botParam = botFade.getLayoutParams();
        botParam.height = mBotY;
        botFade.setLayoutParams(botParam);


        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = mTopY;
        params.bottomMargin = mBotY;
        mOverlaysView.setLayoutParams(params);
        mOverlaysView.setTop(mTopY);
        mOverlaysView.setBottom(mBotY);
        mOverlaysView.invalidate();

        if (reqDisableToolListener != null)
            reqDisableToolListener.requestDisable(OverlaysTool.class, mTopY != 0 || mBotY != 0 || mCropperLayout.getHeight() != mCropperLayout.getWidth()*6/5);

    }

    private void updateCropperModes(Rect rect) {
        if (mCropModeViews != null) {
            int imgHeight = Math.min(rect.bottom, mContentView.getHeight()) - Math.max(rect.top, 0);
            if(imgHeight >= mContentView.getWidth()){
                mCropModeViews[INDEX_SQUARE_CROP].setVisibility(View.VISIBLE);
                selectCropMode(INDEX_CUSTOM_CROP);
            }else{
                mCropModeViews[INDEX_SQUARE_CROP].setVisibility(View.GONE);
            }
        }
    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {

        LinearLayout rootView = new LinearLayout(parent.getContext());
        rootView.setGravity(Gravity.CENTER);
        rootView.setOrientation(LinearLayout.HORIZONTAL);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCropMode((int) view.getTag());
            }
        };


        mCropModeViews = new ViewGroup[mCropModes.length];


//        int maxHeight = parent.getHeight();
//        int maxWidth = mCropperLayout.getWidth() * maxHeight / mCropperLayout.getHeight();


        for (int i = 0; i < mCropModes.length; i++) {
            CropMode mode = mCropModes[i];
            ViewGroup cropSettingLayout = (ViewGroup) inflater.inflate(R.layout.list_item_crop_mode, parent, false);

            cropSettingLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            FrameLayout ivLayout = (FrameLayout) cropSettingLayout.findViewById(R.id.layout_image_wrapper);
            ivLayout.setBackgroundResource(R.drawable.bg_crop_item);


            ImageView iv = (ImageView) ivLayout.findViewById(R.id.image_crop_mode);

            float r = (float) (i == 2 ? mode.minHeight : mode.maxHeight)/ mCropperLayout.getWidth();
            float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30, parent.getResources().getDisplayMetrics());
            int w = (int)((parent.getHeight()- padding)/1.2);
            int h = (int)(w*r);

            iv.setImageBitmap(mode.icon);
            iv.setLayoutParams(new FrameLayout.LayoutParams(w, h));

           /* View borderView = cropSettingLayout.findViewById(R.id.layout_image_border);
            borderView.setLayoutParams(new FrameLayout.LayoutParams(w,h));*/

//            iv.setImageURI(mUri);

            TextView textView = (TextView)cropSettingLayout.findViewById(R.id.text_crop_name);
            textView.setText(mode.name);


            cropSettingLayout.setTag(i);
            cropSettingLayout.setOnClickListener(listener);



            rootView.addView(cropSettingLayout);

            mCropModeViews[i] = cropSettingLayout;
        }

        selectCropMode(2);
        updateCropperModes(imageBounds);
        return rootView;
    }

    public void selectCropMode(int index) {
        int oldSelected = mSelected;
        mSelected = index;

        mCropModes[oldSelected].onUnSelected();
        mCropModes[mSelected].onSelected();

        mCropModeViews[oldSelected].findViewById(R.id.layout_image_wrapper).getBackground().setColorFilter(null);
        mCropModeViews[index].findViewById(R.id.layout_image_wrapper).getBackground().setColorFilter(new PorterDuffColorFilter(
                mCropModeViews[mSelected].getResources().getColor(R.color.secondaryColor),
                PorterDuff.Mode.MULTIPLY
        ));

       /* mCropModeViews[index].setColorFilter(new PorterDuffColorFilter(
                mCropModeViews[mSelected].getResources().getColor(R.color.colorAccent),
                PorterDuff.Mode.ADD
        ));*/

    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

        mActivatable.getImageBounds(imageBounds);
        updateCropperBounds();
        boundCropper();

        options.topInset = mTopY;
        options.bottomInset = mBotY;
        botHandle.setVisibility(View.GONE);
        topHandle.setVisibility(View.GONE);

    }

    @Override
    public void onOpen() {
        super.onOpen();
        int color = FADE_OPEN_OPACITY + mFadeBaseColor;
        topFade.setBackgroundColor(color);
        botFade.setBackgroundColor(color);
        topHandle.setOnTouchListener(touchListener);
        botHandle.setOnTouchListener(touchListener);
        topBar.setVisibility(View.VISIBLE);
        botBar.setVisibility(View.VISIBLE);
        topHandle.setVisibility(View.VISIBLE);
        botHandle.setVisibility(View.VISIBLE);
        if (mActivatable != null) {
            mActivatable.setActive(true);
            mActivatable.invalidate();
        }

    }

    @Override
    public void onClose() {
        super.onClose();
        int color = FADE_CLOSE_OPACITY + mFadeBaseColor;
        topFade.setBackgroundColor(color);
        botFade.setBackgroundColor(color);
        topBar.setOnTouchListener(null);
        botBar.setOnTouchListener(null);
        topBar.setVisibility(View.GONE);
        botBar.setVisibility(View.GONE);
        topHandle.setVisibility(View.GONE);
        botHandle.setVisibility(View.GONE);
        if (mActivatable != null)
            mActivatable.setActive(false);
    }

    @Override
    public String getName() {
        return "Crop";
    }

    @Override
    public int getDrawable() {
        return R.drawable.crop_icon_selected;
    }

    protected class CropMode {

        public CropMode(String name, Bitmap icon, boolean moveOtherBar, int minHeight, int maxHeight) {
            this.name = name;
            this.icon = icon;
            this.moveOtherBar = moveOtherBar;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        /* tells cropper to move other bar if
                 the bar being moved makes the content smaller than the minimum
                  or larger than the maximum
                  */
        private final String name;
        Bitmap icon;
        boolean moveOtherBar;
        int minHeight;
        int maxHeight;

        public void onSelected() {
            apply();
//            int height = ((View) mOverlaysView.getParent()).getHeight();
//            mTopY = mBotY = (height - MAX_SIZE) / 2;
            boundCropper();

            updateCropperView();
        }

        public void apply() {
            MIN_SIZE = minHeight;
            MAX_SIZE = maxHeight;
            MOVE_OTHER_BAR = moveOtherBar;
        }

        public void onUnSelected() {

        }
    }
}
