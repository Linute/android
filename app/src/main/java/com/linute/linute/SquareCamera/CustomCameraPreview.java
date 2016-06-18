package com.linute.linute.SquareCamera;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CustomCameraPreview extends TextureView{

    public static final String TAG = CustomCameraPreview.class.getSimpleName();


    private static final int FOCUS_SQR_SIZE = 100;
    private static final int FOCUS_MAX_BOUND = 950;
    private static final int FOCUS_MIN_BOUND = -FOCUS_MAX_BOUND;

    private Camera mCamera;

    private float mLastTouchX;
    private float mLastTouchY;

    // For focus
    private boolean mIsFocus;
    private boolean mIsFocusReady;
    private Camera.Area mFocusArea;
    private ArrayList<Camera.Area> mFocusAreas;

    public CustomCameraPreview(Context context) {
        super(context);
        init(context);
    }

    public CustomCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mFocusArea = new Camera.Area(new Rect(), 500);
        mFocusAreas = new ArrayList<>();
        mFocusAreas.add(mFocusArea);
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
                if (mCamera!=null) mCamera.cancelAutoFocus();
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

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null
                && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            //Log.d(TAG, mFocusAreas.size() + "");

            setFocusArea(x,y);
            if (mOnFocus != null) mOnFocus.onFocusStart(x,y);
            params.setFocusAreas(mFocusAreas);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (mOnFocus != null) mOnFocus.onFocusFinished();
                }
            });
        }
    }

    public void setIsFocusReady(final boolean isFocusReady) {
        mIsFocusReady = isFocusReady;
    }

    private void setFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / getWidth()) * 2000 - FOCUS_MAX_BOUND).intValue(), FOCUS_SQR_SIZE);
        int top = clamp(Float.valueOf((y / getHeight()) * 2000 - FOCUS_MAX_BOUND).intValue(), FOCUS_SQR_SIZE);
        mFocusArea.rect.set(left, top, left + FOCUS_SQR_SIZE, top + FOCUS_SQR_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>FOCUS_MAX_BOUND){
            if (touchCoordinateInCameraReper>0){
                result = FOCUS_MAX_BOUND - focusAreaSize/2;
            } else {
                result = FOCUS_MIN_BOUND + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    private OnFocus mOnFocus;

    public void setOnTouched(OnFocus foc){
        mOnFocus = foc;
    }

    public interface OnFocus{
        void onFocusStart(float x, float y);
        void onFocusFinished();
    }
}
