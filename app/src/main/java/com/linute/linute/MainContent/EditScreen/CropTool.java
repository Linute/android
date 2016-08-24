package com.linute.linute.MainContent.EditScreen;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by mikhail on 8/23/16.
 */
public class CropTool extends EditContentTool {


    private final EditFragment.Activatable mActivatable;
    /*measurements taken from bottom (0 in bottom and top = full image)*/
    private int mTopY = 0;
    private int mBotY = -30;

    private View topBar;
    private View botBar;
    private View topFade;
    private View botFade;
    private final View mCropperLayout;

    public int MIN_SIZE = 300;
    public int MAX_SIZE = 600;
    private final View baseContentView;


    public CropTool(Uri uri, EditFragment.ContentType type, ViewGroup overlays, EditFragment.Activatable activatable) {
        super(uri, type, overlays);
        mCropperLayout = LayoutInflater.from(overlays.getContext()).inflate(R.layout.tools_cropper_overlay, mOverlaysView, false);
        mCropperLayout.setAlpha(.3f);
        mOverlaysView.addView(mCropperLayout);

        topBar = mCropperLayout.findViewById(R.id.top_bar);
        botBar = mCropperLayout.findViewById(R.id.bot_bar);

        topBar.setVisibility(View.GONE);
        botBar.setVisibility(View.GONE);
        topFade = mCropperLayout.findViewById(R.id.top_fade);
        botFade = mCropperLayout.findViewById(R.id.bot_fade);
        updateCropperView();

        mActivatable = activatable;

        baseContentView = ((View) mOverlaysView.getParent()).findViewById(R.id.base_content);
       /* baseContentView.setOnTouchListener(new View.OnTouchListener() {

            float startX = 0;
            float startY = 0;
            float initX = 0;
            float initY = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mGestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }


                Log.i("AAA",MotionEvent.actionToString(motionEvent.getActionMasked()));
//                mGestureDetector.onTouchEvent(motionEvent);

                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = view.getX();
                        initY = view.getY();
                        startX = motionEvent.getRawX();
                        startY = motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.setX(initX + (motionEvent.getRawX() - startX));
                        view.setY(initY + (motionEvent.getRawY() - startY));
                        view.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:


                        break;
                }

                return false;
            }
        });*/
    }

    ScaleGestureDetector mGestureDetector = new ScaleGestureDetector(mOverlaysView.getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
        float totalScale = 1;


        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            totalScale *= scaleGestureDetector.getScaleFactor();
            baseContentView.setPivotX(scaleGestureDetector.getFocusX());
            baseContentView.setPivotY(scaleGestureDetector.getFocusY());
            baseContentView.setScaleX(totalScale);
            baseContentView.setScaleY(totalScale);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

        }
    });


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
                    if (view == botBar) {
                        mBotY = (int) (initBarY + (startY - motionEvent.getRawY()));

                        if (mBotY + botBar.getHeight() + mTopY + topBar.getHeight() + MIN_SIZE > mCropperLayout.getHeight()) {
                            mBotY = mCropperLayout.getHeight() - (botBar.getHeight() + mTopY + topBar.getHeight() + MIN_SIZE);
                        }
                        if (mBotY < 0) {
                            mBotY = 0;
                        }

                    } else {
                        mTopY = (int) (initBarY + (motionEvent.getRawY() - startY));

                        if (mTopY + botBar.getHeight() + mBotY + topBar.getHeight() + MIN_SIZE > mCropperLayout.getHeight()) {
                            mTopY = mCropperLayout.getHeight() - (botBar.getHeight() + mBotY + topBar.getHeight() + MIN_SIZE);
                        }
                        if (mTopY < 0) {
                            mTopY = 0;
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
    }

    @Override
    public View createToolOptionsView(LayoutInflater inflater, ViewGroup parent) {

        View rootView = new View(parent.getContext());//inflater.inflate(R.layout.tools_options_crop, parent, false;)


        return rootView;
    }

    @Override
    public void processContent(Uri uri, EditFragment.ContentType contentType, ProcessingOptions options) {

        options.topInset = mTopY;
        options.bottomInset = mBotY;

    }

    @Override
    public void onOpen() {
        super.onOpen();
        mCropperLayout.setAlpha(1);
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
        mCropperLayout.setAlpha(.3f);
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
        return R.drawable.crop_icon;
    }
}
