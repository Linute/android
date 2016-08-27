package com.linute.linute.MainContent.EditScreen;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/23/16.
 */
public class CropTool extends EditContentTool {


    private final EditFragment.Activatable mActivatable;
    private final EditFragment.RequestDisableToolListener reqDisableToolListener;
    /*measurements taken from bottom (0 in bottom and top = full image)*/
    private int mTopY = 0;
    private int mBotY = 0;

    private View topBar;
    private View botBar;
    private View topFade;
    private View botFade;
    private final View mCropperLayout;

    public int MIN_SIZE = 300;
    public int MAX_SIZE = 600;
    public boolean MOVE_OTHER_BAR = false;

    CropMode[] mCropModes;
    FrameLayout[] mCropModeViews;
    private int mSelected;

    Dimens mDimens;


    public CropTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays, EditFragment.Activatable activatable, Dimens dimens, EditFragment.RequestDisableToolListener listener) {
        super(uri, type, overlays);
        mActivatable = activatable;
        mDimens = dimens;


        mCropperLayout = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tools_overlay_crop, mOverlaysView, false);
        ((ViewGroup)(mOverlaysView.getParent())).addView(mCropperLayout);

        topBar = mCropperLayout.findViewById(R.id.top_bar);
        botBar = mCropperLayout.findViewById(R.id.bot_bar);

        topBar.setVisibility(View.GONE);
        botBar.setVisibility(View.GONE);
        topFade = mCropperLayout.findViewById(R.id.top_fade);
        botFade = mCropperLayout.findViewById(R.id.bot_fade);
        updateCropperView();

        reqDisableToolListener = listener;

    }


    View.OnTouchListener touchListener = new View.OnTouchListener() {
        float startY;
        int initBarY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {


            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startY = motionEvent.getRawY();
                    if (view == botBar) {
                        initBarY = mBotY;
                    } else {
                        initBarY = mTopY;
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int cropperLayoutHeight = mCropperLayout.getHeight();
                    if (view == botBar) {
                        mBotY = (int) (initBarY + (startY - motionEvent.getRawY()));
                        if(mBotY < 0) mBotY = 0;

                        int imageHeight = cropperLayoutHeight - mBotY - mTopY;

                        if(imageHeight > MAX_SIZE){
                            if(MOVE_OTHER_BAR){
                                mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                                if(mTopY < 0){
                                    mTopY = 0;
                                    mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                                }
                            }else{
                                mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                            }
                        }

                        if (imageHeight < MIN_SIZE) {
                           if(MOVE_OTHER_BAR){
                               mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                               if(mTopY < 0){
                                   mTopY = 0;
                                   mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                               }
                           }else{
                               mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                           }
                        }



                    } else {
                        mTopY = (int) (initBarY + (motionEvent.getRawY() - startY));
                        if(mTopY < 0) mTopY = 0;
                        int imageHeight = cropperLayoutHeight - mBotY - mTopY;

                        if(imageHeight > MAX_SIZE){
                            if(MOVE_OTHER_BAR){
                                mBotY = cropperLayoutHeight - mTopY - MAX_SIZE;
                                if(mBotY < 0){
                                    mBotY = 0;
                                    mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                                }
                            }else{
                                mTopY = cropperLayoutHeight - mBotY - MAX_SIZE;
                            }
                        }

                        if (imageHeight < MIN_SIZE) {
                            if(MOVE_OTHER_BAR){
                                mBotY = cropperLayoutHeight - mTopY - MIN_SIZE;
                                if(mBotY < 0){
                                    mBotY = 0;
                                    mTopY = cropperLayoutHeight - mBotY - MIN_SIZE;
                                }
                            }else{
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

        if(reqDisableToolListener != null)
        reqDisableToolListener.requestDisable(OverlaysTool.class, mTopY != 0 || mBotY != 0);

    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {

        LinearLayout rootView = new LinearLayout(parent.getContext());
        rootView.setOrientation(LinearLayout.HORIZONTAL);


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCropMode((int) view.getTag());
            }
        };

        DisplayMetrics metrics = parent.getContext().getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int height = mDimens.height * displayWidth/mDimens.width;

        mCropModes = new CropMode[]{
                new CropMode(R.drawable.sticker_icon, false, displayWidth/16 * 9, height),
                new CropMode(R.drawable.sticker_icon, true, displayWidth, displayWidth),
                new CropMode(R.drawable.sticker_icon, true, displayWidth/16 * 9, displayWidth/16 * 9),
        };

        mCropModeViews = new FrameLayout[mCropModes.length];


        int maxHeight = parent.getHeight();
        int maxWidth = mCropperLayout.getWidth() * maxHeight /mCropperLayout.getHeight();


        for (int i = 0; i < mCropModes.length; i++) {
            CropMode mode = mCropModes[i];
            FrameLayout cropSettingLayout = (FrameLayout)inflater.inflate(R.layout.list_item_crop_mode, parent, false);

            cropSettingLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            FrameLayout ivLayout = (FrameLayout)cropSettingLayout.findViewById(R.id.layout_image_wrapper);
            ivLayout.setBackgroundResource(R.drawable.bg_crop_item);


            ImageView iv = (ImageView)ivLayout.findViewById(R.id.image_crop_mode);

            float r = (float)mode.maxHeight/mCropperLayout.getWidth();
            int h = (int)(maxWidth*r);
            int w = maxWidth;

            iv.setLayoutParams(new FrameLayout.LayoutParams(w,h));
            View borderView = cropSettingLayout.findViewById(R.id.layout_image_border);
            borderView.setLayoutParams(new FrameLayout.LayoutParams(w,h));

            iv.setImageURI(mUri);
            iv.setTag(i);
            iv.setOnClickListener(listener);

            rootView.addView(cropSettingLayout);

            mCropModeViews[i] = cropSettingLayout;
        }

        selectCropMode(0);
        return rootView;
    }

    public void selectCropMode(int index){
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

        options.topInset = mTopY;
        options.bottomInset = mBotY;

    }

    @Override
    public void onOpen() {
        super.onOpen();
        int color = 0xCC000000 + mOverlaysView.getContext().getResources().getColor(R.color.colorPrimary);
        topFade.setBackgroundColor(color);
        botFade.setBackgroundColor(color);
        topBar.setOnTouchListener(touchListener);
        botBar.setOnTouchListener(touchListener);
        topBar.setVisibility(View.VISIBLE);
        botBar.setVisibility(View.VISIBLE);
        if (mActivatable != null)
            mActivatable.setActive(true);
    }

    @Override
    public void onClose() {
        super.onClose();
        int color = 0xFF000000 + mOverlaysView.getContext().getResources().getColor(R.color.colorPrimary);
        topFade.setBackgroundColor(color);
        botFade.setBackgroundColor(color);
        topBar.setOnTouchListener(null);
        botBar.setOnTouchListener(null);
        topBar.setVisibility(View.GONE);
        botBar.setVisibility(View.GONE);
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

    protected class CropMode{
        public CropMode(int icon, boolean moveOtherBar, int minHeight, int maxHeight) {
            this.icon = icon;
            this.moveOtherBar = moveOtherBar;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }

        /* tells cropper to move other bar if
                 the bar being moved makes the content smaller than the minimum
                  or larger than the maximum
                  */
        int icon;
        boolean moveOtherBar;
        int minHeight;
        int maxHeight;

        public void onSelected(){
            MIN_SIZE = minHeight;
            MAX_SIZE = maxHeight;
            MOVE_OTHER_BAR = moveOtherBar;
            int height = ((View)mOverlaysView.getParent()).getHeight();
            mTopY = mBotY = (height-MAX_SIZE)/2;
            updateCropperView();
        }
        public void onUnSelected(){

        }
    }
}
