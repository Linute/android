package com.linute.linute.ProfileCamera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SquareCameraPreview extends TextureView{

    public static final String TAG = SquareCameraPreview.class.getSimpleName();


    private static final int FOCUS_SQR_SIZE = 100;
    private static final int FOCUS_MAX_BOUND = 1000;
    private static final int FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND;

    private static final double ASPECT_RATIO = 3.0 / 4.0;
    protected Camera mCamera;

    private float mLastTouchX;
    private float mLastTouchY;

    // For focus
    private boolean mIsFocus;
    private boolean mIsFocusReady;
    protected Camera.Area mFocusArea;
    protected ArrayList<Camera.Area> mFocusAreas;

    public SquareCameraPreview(Context context) {
        super(context);
        init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SquareCameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {
        mFocusArea = new Camera.Area(new Rect(), 1000);
        mFocusAreas = new ArrayList<>();
        mFocusAreas.add(mFocusArea);
    }

    /**
     * Measure the view and its content to determine the measured width and the
     * measured height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        final boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            if (width > height * ASPECT_RATIO) {
                width = (int) (height * ASPECT_RATIO + 0.5);
            } else {
                height = (int) (width / ASPECT_RATIO + 0.5);
            }
        } else {
            if (height > width * ASPECT_RATIO) {
                height = (int) (width * ASPECT_RATIO + 0.5);
            } else {
                width = (int) (height / ASPECT_RATIO + 0.5);
            }
        }

        setMeasuredDimension(width, height);
    }

    public int getViewWidth() {
        return getWidth();
    }

    public int getViewHeight() {
        return getHeight();
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mIsFocus = true;

                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mIsFocus && mIsFocusReady) {
                    handleFocus(mCamera.getParameters());
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                mCamera.cancelAutoFocus();
                mIsFocus = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }

        return true;
    }


    private void handleFocus(Camera.Parameters params) {
        float x = mLastTouchX;
        float y = mLastTouchY;

        if (!setFocusBound(x, y)) return;

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null
                && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            //Log.d(TAG, mFocusAreas.size() + "");
            params.setFocusAreas(mFocusAreas);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // Callback when the auto focus completes
                }
            });
        }
    }

    public void setIsFocusReady(final boolean isFocusReady) {
        mIsFocusReady = isFocusReady;
    }

    private boolean setFocusBound(float x, float y) {
        int left = (int) (x - FOCUS_SQR_SIZE / 2);
        int right = (int) (x + FOCUS_SQR_SIZE / 2);
        int top = (int) (y - FOCUS_SQR_SIZE / 2);
        int bottom = (int) (y + FOCUS_SQR_SIZE / 2);

        if (FOCUS_MIN_BOUND > left || left > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > right || right > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > top || top > FOCUS_MAX_BOUND) return false;
        if (FOCUS_MIN_BOUND > bottom || bottom > FOCUS_MAX_BOUND) return false;

        mFocusArea.rect.set(left, top, right, bottom);

        return true;
    }

}