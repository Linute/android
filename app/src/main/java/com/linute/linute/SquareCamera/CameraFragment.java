package com.linute.linute.SquareCamera;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linute.linute.R;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.schedulers.Schedulers.io;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class CameraFragment extends Fragment implements Camera.PictureCallback {

    public static final String TAG = CameraFragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_mode";
    public static final String IMAGE_INFO = "image_info";

    private static final int PICTURE_SIZE_MAX_WIDTH = 2560;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 1280;

    private int mCameraID;
    private String mFlashMode;
    private Camera mCamera;
    private SquareCameraPreview mPreviewView;
    private SurfaceTexture mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private ImageParameters mImageParameters;
    private CameraOrientationListener mOrientationListener;

    private ImageView mTakePhotoBtn;
    private CheckBox mAnonCheckbox;
    private EditSaveVideoFragment.VideoDimen mVideoDimen;
    private View mCameraOps;
    private View mGalleryButton;
    private Uri mVideoUri;
    private ProgressBar mRecordProgress;
    private CompositeSubscription mSubscriptions;
    private Toolbar mToolbar;
    private ObjectAnimator mProgressAnimator;

    private boolean mIsRecording = false;
    private boolean mSurfaceAlreadyCreated = false;
    private boolean mIsSafeToTakePhoto = false;
    private boolean mVideoProcessing = false;

    private int mCameraType;

    public static Fragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mOrientationListener = new CameraOrientationListener(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore your state here because a double rotation with this fragment
        // in the backstack will cause improper state restoration
        // onCreate() -> onSavedInstanceState() instead of going through onCreateView()
        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mFlashMode = CameraSettingPreferences.getCameraFlashMode(getActivity());
            mImageParameters = new ImageParameters();
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
            mImageParameters = savedInstanceState.getParcelable(IMAGE_INFO);
        }

        mCameraType = ((CameraActivity) getActivity()).getCameraType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.square_camera_take_photo, container, false);

        mToolbar = (Toolbar) root.findViewById(R.id.square_cam_tool_bar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto && !mIsRecording && !mVideoProcessing)
                    getActivity().finish();
            }
        });

        mToolbar.setTitle("Camera");
        mTakePhotoBtn = (ImageView) root.findViewById(R.id.capture_image_button);

        mAnonCheckbox = (CheckBox) root.findViewById(R.id.anon_checkbox);
        mGalleryButton = root.findViewById(R.id.cameraFragment_galleryButton);


        if (mCameraType == CameraActivity.JUST_CAMERA) {
            mAnonCheckbox.setVisibility(View.INVISIBLE);
            mGalleryButton.setVisibility(View.INVISIBLE);
        }else {
            mGalleryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsSafeToTakePhoto && !mIsRecording && !mVideoProcessing)
                        ((CameraActivity) getActivity()).launchGalleryFragment();
                }
            });
        }

        mCameraOps = root.findViewById(R.id.camera_ops);
        mRecordProgress = (ProgressBar) root.findViewById(R.id.record_progress);

        setOnClickListeners(root); //activate buttons

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOrientationListener.enable();

        mPreviewView = (SquareCameraPreview) view.findViewById(R.id.camera_preview_view);

        mPreviewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceHolder = surface;

                if (hasCameraAndWritePermission()) {
                    mSurfaceAlreadyCreated = true;
                    //start camera after slight delay. Without delay, there is huge lag time between active and inactive app state
                    mShowCameraHandler.postDelayed(mShowPreview, 250);
                }
            }


            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceAlreadyCreated = false;
                // stop the preview
                if (mCamera != null) {
                    mShowCameraHandler.removeCallbacks(mShowPreview);
                    releaseMediaRecorder();
                    stopCameraPreview();
                    mCamera.release();
                    mCamera = null;
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        mImageParameters.mIsPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        setupFlashMode();
    }

    //buttons will only be set after camera view has appeared
    private void setOnClickListeners(View view) {
        view.findViewById(R.id.change_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto) {
                    if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                        mCameraID = getBackCameraID();
                    } else {
                        mCameraID = getFrontCameraID();
                    }
                    restartPreview();
                }
            }
        });

        View changeCameraFlashModeBtn = view.findViewById(R.id.flash);
        changeCameraFlashModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto) {
                    if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                        mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                    } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                        mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                    }

                    setupFlashMode();
                    setupCamera();
                }
            }
        });

        if (mCameraType == CameraActivity.JUST_CAMERA) {
            mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture();
                }
            });

        }else {
            mTakePhotoBtn.setOnTouchListener(new View.OnTouchListener() {

                Handler mRecordHandler = new Handler();

                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mIsSafeToTakePhoto && !mIsRecording) {
                            new StartCameraTask().execute();
                        }
                    }
                };

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)) {
                        mRecordHandler.removeCallbacks(mRunnable);
                        mTakePhotoBtn.setImageResource(R.drawable.square_camera_unselected);
                        if (!mVideoProcessing) {
                            if (mIsRecording) {
                                new StopCamera().execute();
                            } else {
                                takePicture();
                            }
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (!mIsRecording && !mVideoProcessing) {
                            mTakePhotoBtn.setImageResource(R.drawable.square_camera_selected);
                            mRecordHandler.removeCallbacks(mRunnable);
                            mRecordHandler.postDelayed(mRunnable, 1000);
                        }
                    }
                    return true;
                }
            });
        }
    }


    //hides the gallery, flash, and reverse camera button
    private void hideCameraButtons(boolean hide) {
        if (hide && mCameraOps.getVisibility() == View.VISIBLE) {
            mCameraOps.setVisibility(View.INVISIBLE);

            if (mCameraType == CameraActivity.CAMERA_AND_VIDEO_AND_GALLERY) {
                mGalleryButton.setVisibility(View.INVISIBLE);
            }
        } else if (!hide && mCameraOps.getVisibility() == View.INVISIBLE) {
            mCameraOps.setVisibility(View.VISIBLE);
            if (mCameraType == CameraActivity.CAMERA_AND_VIDEO_AND_GALLERY) {
                mGalleryButton.setVisibility(View.VISIBLE);
            }
        }
    }


    private void showVideoTooShortDialog() {
        if (getActivity() == null) return;
        Log.i(TAG, "showVideoTooShortDialog: ");
        new AlertDialog.Builder(getActivity())
                .setTitle("Video is too short")
                .setMessage("Videos must be 3 seconds or longer.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void goToVideoEditFragment(Uri uri, EditSaveVideoFragment.VideoDimen videoDimen) {
        if (getActivity() != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            EditSaveVideoFragment.newInstance(uri, mAnonCheckbox.isChecked(), videoDimen),
                            EditSaveVideoFragment.TAG)
                    .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                    .commit();
        }
    }

    // we do not have an auto flash
    // some phones crash if both autofocus and flash are on. There is no way to tell from autoflash is
    // flash is going to go off.
    private void setupFlashMode() {
        View view = getView();
        if (view == null) return;

        final TextView autoFlashIcon = (TextView) view.findViewById(R.id.auto_flash_icon);
        if (Camera.Parameters.FLASH_MODE_AUTO.equalsIgnoreCase(mFlashMode)) {
            mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
            autoFlashIcon.setText("Off");
        } else if (Camera.Parameters.FLASH_MODE_ON.equalsIgnoreCase(mFlashMode)) {
            autoFlashIcon.setText("On");
        } else if (Camera.Parameters.FLASH_MODE_OFF.equalsIgnoreCase(mFlashMode)) {
            autoFlashIcon.setText("Off");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        outState.putParcelable(IMAGE_INFO, mImageParameters);
        super.onSaveInstanceState(outState);
    }


    //returns true if able to get camera and false if we werent able to
    private boolean getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Restart the camera preview
     */
    private void restartPreview() {

        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }

        if (getCamera(mCameraID)) { //were able to find a camera to use
            startCameraPreview();
            mPreviewView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            mCamera.setPreviewTexture(mSurfaceHolder);
            mCamera.startPreview();
            setSafeToTakePhoto(true);
            setCameraFocusReady(true);
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        setSafeToTakePhoto(false);
        setCameraFocusReady(false);

        // Nulls out callbacks, stops face detection
        mCamera.stopPreview();
        mPreviewView.setCamera(null);
    }

    private void setSafeToTakePhoto(final boolean isSafeToTakePhoto) {
        mIsSafeToTakePhoto = isSafeToTakePhoto;
    }

    private void setCameraFocusReady(final boolean isFocusReady) {
        if (this.mPreviewView != null) {
            mPreviewView.setIsFocusReady(isFocusReady);
        }
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */

    private int displayOrientation = 0;

    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        // Clockwise rotation needed to align the window display to the natural position
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        // CameraInfo.Orientation is the angle relative to the natural position of the device
        // in clockwise rotation (angle that is rotated clockwise from the natural position)
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mImageParameters.mDisplayOrientation = displayOrientation;
        mImageParameters.mLayoutOrientation = degrees;

        mCamera.setDisplayOrientation(mImageParameters.mDisplayOrientation);
    }


    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        int bPrevWid = bestPreviewSize.width;
        int bPrevHei = bestPreviewSize.height;

        int bPicWid = bestPictureSize.width;
        int bPicHei = bestPictureSize.height;

        parameters.setPreviewSize(bPrevWid, bPrevHei);
        parameters.setPictureSize(bPicWid, bPicHei);

        List<String> focusmodes = parameters.getSupportedFocusModes();

        // Set continuous picture focus, if it's supported
        if (focusmodes != null && focusmodes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
            mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
        }

        final View changeCameraFlashModeBtn = getView().findViewById(R.id.flash);
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
            changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    private Size determineBestSize(List<Size> sizes, int widthThreshold) {

        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();

        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);

            boolean isDesireRatio = (size.width / 4) == (size.height / 3) || (size.height / 4) == (size.width / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }


        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Take a picture
     */
    private void takePicture() {

        if (mIsSafeToTakePhoto && !mIsRecording) {
            setSafeToTakePhoto(false);

            mOrientationListener.rememberOrientation();
            // Shutter callback occurs after the image is captured. This can
            // be used to trigger a sound to let the user know that image is taken
            final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                }
            };

            // Raw callback occurs when the raw image data is available
            final Camera.PictureCallback raw = null;

            // postView callback occurs when a scaled, fully processed
            // postView image is available.
            final Camera.PictureCallback postView = null;

            //if on or auto, dont autofocus

            try {
                mCamera.cancelAutoFocus();
                mCamera.takePicture(shutterCallback, raw, postView, CameraFragment.this);
            } catch (RuntimeException e) {
                //if we call take picture before camera preview has started: will throw runtime exception
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //restartPreview() is called when camera preview surface is created
        //to prevent redundancy, only run this one if surfaceCreated() isn't called when fragment resumed
        if (mSurfaceAlreadyCreated && hasCameraAndWritePermission()) {
            if (mCamera == null) {
                restartPreview();
            }
        }
    }

    //delay drawing the camera preview
    //opening the camera in onresume causes a slight lag when resuming
    final Handler mShowCameraHandler = new Handler();

    final Runnable mShowPreview = new Runnable() {
        @Override
        public void run() {
            if (mCamera == null) {
                restartPreview();
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mProgressAnimator != null) {
            mProgressAnimator.removeAllListeners();
            mProgressAnimator.end();
            mProgressAnimator.cancel();
        }

        mOrientationListener.disable();

        mShowCameraHandler.removeCallbacks(mShowPreview);
        // stop the preview
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mSubscriptions != null) {
            mSubscriptions.unsubscribe();
        }

        CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashMode);
    }


//    @Override
//    public void onStop() {
//        super.onStop();
//        mPreviewView.setBackgroundColor(Color.parseColor("#000000"));
//    }


    /**
     * A picture has been taken
     *
     * @param data   -- data from camera
     * @param camera -- camera used
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        mSubscriptions = new CompositeSubscription();
        Observable<Uri> onSave = Observable.just(
                ImageUtility.savePicture(getActivity(), rotatePicture(displayOrientation, data)))
                .subscribeOn(io())
                .observeOn(mainThread());

        mSubscriptions.add(onSave.subscribe(new Action1<Uri>() {
            @Override
            public void call(Uri uri) {
                getFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                EditSavePhotoFragment.newInstance(uri, mAnonCheckbox.isChecked()),
                                EditSavePhotoFragment.TAG)
                        .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                        .commit();

                setSafeToTakePhoto(true);
            }
        }));
    }


    private Bitmap rotatePicture(int rotation, byte[] data) {
        Bitmap bitmap = ImageUtility.decodeSampledBitmapFromByte(getActivity(), data);
//        Log.d(TAG, "original bitmap width " + bitmap.getWidth() + " height " + bitmap.getHeight());

        Bitmap oldBitmap;
        oldBitmap = bitmap;

        int x = 0;
        int y = 0;

        int measure;

        if (oldBitmap.getHeight() < oldBitmap.getWidth()) {
            if (usingFrontFaceCamera()) {
                x += oldBitmap.getWidth() - oldBitmap.getHeight();
            }
            measure = oldBitmap.getHeight();
        } else {
            measure = oldBitmap.getWidth();
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(usingFrontFaceCamera() ? 360 - rotation : rotation, oldBitmap.getWidth() / 2, oldBitmap.getHeight() / 2);

        bitmap = Bitmap.createBitmap(
                oldBitmap, x, y, measure, measure, matrix, false
        );

        oldBitmap.recycle();


        if (usingFrontFaceCamera()) {
            oldBitmap = bitmap;

            matrix = new Matrix();
            matrix.setScale(-1, 1);
            matrix.postTranslate(oldBitmap.getWidth(), 0);

            bitmap = Bitmap.createBitmap(
                    oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, false
            );

            oldBitmap.recycle();
        }

        return bitmap;
    }

    private boolean usingFrontFaceCamera() {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        return (info.facing == CameraInfo.CAMERA_FACING_FRONT);
    }


    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        /**
         * @param degrees Amount of clockwise rotation from the device's natural position
         * @return Normalized degrees to just 0, 90, 180, 270
         */
        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            rememberOrientation();
            return mRememberedNormalOrientation;
        }

    }


    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraAndWritePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasPermission(Manifest.permission.CAMERA) && hasPermission(Manifest.permission.RECORD_AUDIO);
    }


    private Uri prepareMediaRecorder() {
        if (getActivity() == null) return null;

        //profile might not exist
        CamcorderProfile camcorderProfile = CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P) ?
                CamcorderProfile.get(CamcorderProfile.QUALITY_480P) :
                CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);

        //height has to be 480. try to find width that'll keep aspect ratio 4:3
        //Log.i(TAG, "prepareMediaRecorder: h:" + camcorderProfile.videoFrameHeight + "W: " + camcorderProfile.videoFrameWidth);
        int bestWidth = camcorderProfile.videoFrameWidth;
        Size betterSize = getVideoSize(camcorderProfile.videoFrameHeight);

        if (betterSize != null) {
            bestWidth = betterSize.width;
        }

        //sent to EditSaveVideoFragment dimensions so we know how much to crop
        mVideoDimen = new EditSaveVideoFragment.VideoDimen(camcorderProfile.videoFrameHeight, bestWidth, mCameraID == CameraInfo.CAMERA_FACING_FRONT);

        //Log.i(TAG, "prepareMediaRecorder: "+bestWidth +" "+camcorderProfile.videoFrameHeight);

        //stop and restart

        mCamera.cancelAutoFocus();
        Camera.Parameters param = mCamera.getParameters();
        param.setPreviewSize(bestWidth, camcorderProfile.videoFrameHeight);
        mCamera.stopPreview(); // must stop and reset camera when params are changed
        mCamera.setParameters(param); //must set preview size to same size and video size or it will record green on some phones
        mCamera.startPreview();

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT); //default ?
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT); //default ?

        mMediaRecorder.setProfile(camcorderProfile);
        mMediaRecorder.setVideoSize(bestWidth, camcorderProfile.videoFrameHeight);

        mMediaRecorder.setOrientationHint(mCameraID == CameraInfo.CAMERA_FACING_FRONT ? 270 : 90);


        final Uri imageUri;

        try {
            imageUri = Uri.parse(ImageUtility.getTempFilePath(getActivity(), "uncropped_video", ".mp4"));
            mMediaRecorder.setOutputFile(imageUri.getPath());

            //called when reached file size limit or max duration of video
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                        try {
                            if (mProgressAnimator != null) {
                                mProgressAnimator.removeAllListeners();
                                mProgressAnimator.end();
                                mProgressAnimator.cancel();
                            }
                            mMediaRecorder.stop();
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                        releaseMediaRecorder();
                        goToVideoEditFragment(imageUri, mVideoDimen);
                        mIsRecording = false;
                    }
                }
            });

            mMediaRecorder.setMaxDuration(15000); //15 sec
            mMediaRecorder.setMaxFileSize(50000000); //50mb
            mMediaRecorder.prepare();

        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return null;
        } catch (IOException e) {
            releaseMediaRecorder();
            e.printStackTrace();
            return null;
        }

        return imageUri;
    }


    private Camera.Size getVideoSize(int sizePref) {

        List<Camera.Size> supportedSizes = mCamera.getParameters().getSupportedPreviewSizes();

        for (Camera.Size size : supportedSizes) { //need size with 4 : 3 ratio
            if (size.height == sizePref && size.width / 4 == size.height / 3) {
                return size;
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImageUtility.deleteCachedVideo(mVideoUri);
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mCamera != null) {
                mCamera.lock();
            }
        }
    }


    class StartCameraTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            mVideoUri = prepareMediaRecorder();

            if (mVideoUri != null) {
                mMediaRecorder.start();
                return true;
            }

            return false;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mIsRecording = aBoolean;
            if (aBoolean) {
                hideCameraButtons(true);
                mRecordProgress.setVisibility(View.VISIBLE);
                mToolbar.setTitle("Recording");
                mProgressAnimator =
                        ObjectAnimator.ofInt(mRecordProgress, "progress", 0, 15000);

                mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    boolean updated = false;


                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (!updated && animation.getCurrentPlayTime() >= 3000) {
                            updated = true;
                            mRecordProgress.setProgressDrawable(
                                    ContextCompat.getDrawable(getContext(), R.drawable.camera_progress)
                            );
                        }
                    }
                });

                mProgressAnimator.setDuration(15000);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.start();

                mTakePhotoBtn.setImageResource(R.drawable.square_camera_record);
            }
        }
    }


    class StopCamera extends AsyncTask<Void, Void, Void> {
        long duration = 0;

        @Override
        protected void onPreExecute() {
            mVideoProcessing = true;
            if (mProgressAnimator != null) {
                duration = mProgressAnimator.getCurrentPlayTime();
                mProgressAnimator.removeAllListeners();
                mProgressAnimator.cancel();
            }

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mMediaRecorder.stop();
                releaseMediaRecorder();
            } catch (RuntimeException e) {
                duration = 0;
                releaseMediaRecorder();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mRecordProgress.setVisibility(View.GONE);
            mRecordProgress.setProgress(0);
            mRecordProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.camera_progress_red));

            mToolbar.setTitle("Camera");
            mTakePhotoBtn.setImageResource(R.drawable.square_camera_capture);
            hideCameraButtons(false);
            if (duration < 3000) { //recorded video was less than 2.5 secs. slight lag time between button press and start recording
                showVideoTooShortDialog();
            } else if (mVideoUri != null) {  //go to edit video screen
                goToVideoEditFragment(mVideoUri, mVideoDimen);
            }

            mIsRecording = false;
            mVideoProcessing = false;
        }
    }
}
