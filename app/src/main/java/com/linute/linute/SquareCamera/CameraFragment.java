package com.linute.linute.SquareCamera;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.linute.linute.MainContent.EditScreen.Dimens;
import com.linute.linute.MainContent.EditScreen.EditFragment;
import com.linute.linute.MainContent.EditScreen.PostOptions;
import com.linute.linute.R;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

import static rx.schedulers.Schedulers.io;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment {

    public static final String TAG = CameraFragment.class.getSimpleName();

    public static final int MAX_RESOLUTION = 3200;

    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_on";
    public static final String KEY_LAST_CAMERA = "last_camera";

    private CameraType mCameraType;
    private int mCameraID;
    private boolean mFlashOn;

    private Camera mCamera;
    private CustomCameraPreview mPreviewView;
    private SurfaceTexture mSurfaceHolder;
    private MediaRecorder mMediaRecorder;

    private ImageView mTakePhotoBtn;
    private Dimens mVideoDimen;
    private View mFlashContainer;
    private View mReverseContainer;
    private CustomView mFlashTop;

    private View mFocusCircle;

    private Uri mVideoUri;

    private ProgressBar mRecordProgress;
    private ObjectAnimator mProgressAnimator;

    private Handler mRecordHandler = new Handler();

    private Subscription mProcessImageSubscriptions;
    private Subscription mStartCameraSubscription;

    private boolean mIsRecording = false;
    private boolean mSurfaceAlreadyCreated = false;
    private boolean mIsSafeToTakePhoto = false;
    private boolean mVideoProcessing = false;

    private PostOptions mPostOptions;

    private static final String ARG_POST_OPTIONS = "content_type";
    private Toolbar mToolbar;


    public static Fragment newInstance(PostOptions options) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_POST_OPTIONS, options);
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    public CameraFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore your state here because a double rotation with this fragment
        // in the backstack will cause improper state restoration
        // onCreate() -> onSavedInstanceState() instead of going through onCreateView()
        if (savedInstanceState == null) {
            mCameraID = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(KEY_LAST_CAMERA, getBackCameraID());
            mFlashOn = CameraSettingPreferences.getCameraFlashMode(getActivity());
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashOn = savedInstanceState.getBoolean(CAMERA_FLASH_KEY);
        }

        Bundle args = getArguments();
        if(args != null){
            mPostOptions = args.getParcelable(ARG_POST_OPTIONS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.square_camera_take_photo, container, false);

        mFlashTop = (CustomView) root.findViewById(R.id.flash_top);

        //make square if doesn't meet our requirements
        if (!ScreenSizeSingleton.getSingleton().mHasRatioRequirement) {
            mFlashTop.setMakeSquare(true);
            root.requestLayout();
        }

        mCameraType = ((CameraActivity) getActivity()).getCameraType();

        mTakePhotoBtn = (ImageView) root.findViewById(R.id.capture_image_button);

        mToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto && !mIsRecording && !mVideoProcessing)
                    getActivity().finish();
            }
        });

        mRecordProgress = (ProgressBar) root.findViewById(R.id.record_progress);
        mFocusCircle = root.findViewById(R.id.focus);

        setOnClickListeners(root);

        return root;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPreviewView = (CustomCameraPreview) view.findViewById(R.id.camera_preview_view);
        mPreviewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //Log.i(TAG, "onSurfaceTextureAvailable: ");
                mSurfaceHolder = surface;
                if (hasCameraAndWritePermission()) {
                    mSurfaceAlreadyCreated = true;
                    //start camera after slight delay. Without delay,
                    //there is huge lag time between active and inactive app state
                    mShowCameraHandler.postDelayed(mShowPreview, 250);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceAlreadyCreated = false;
                mShowCameraHandler.removeCallbacks(mShowPreview);
                // stop the preview
                if (mCamera != null) {
                    // we will get a runtime exception if camera had already been released, either by onpause
                    // or when we switched cameras
                    try {
                        releaseMediaRecorder();
                        stopCameraPreview();
                        mCamera.release();
                        mCamera = null;
                    } catch (RuntimeException e) {
                        Log.i(TAG, "onSurfaceTextureDestroyed: release error : ignore");
                        mCamera.release();
                        mCamera = null;
                    }
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

        mPreviewView.setOnTouched(new CustomCameraPreview.OnFocus() {
            @Override
            public void onFocusStart(float x, float y) {
                float center = (float) mFocusCircle.getWidth() / 2;
                mFocusCircle.setX(x - center);
                mFocusCircle.setY(y - center);
                mFocusCircle.setAlpha(0);
                mFocusCircle.setScaleX(0.75f);
                mFocusCircle.setScaleY(0.75f);
                mFocusCircle.setVisibility(View.VISIBLE);
                mFocusCircle.animate()
                        .alpha(1.f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start();
            }

            @Override
            public void onFocusFinished() {
                mFocusCircle.clearAnimation();
                mFocusCircle.setVisibility(View.GONE);
            }
        });

        setupFlashMode();
    }

    //buttons will only be set after camera view has appeared
    private void setOnClickListeners(final View view) {
        mFlashContainer = view.findViewById(R.id.flash_container);
        mReverseContainer = view.findViewById(R.id.reverse_container);

        mReverseContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto) {
                    if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                        mCameraID = getBackCameraID();
                    } else {
                        mCameraID = getFrontCameraID();
                    }
                    restartPreview();
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt(KEY_LAST_CAMERA, mCameraID).apply();
                }
            }
        });

        mFlashContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto) {
                    mFlashOn = !mFlashOn;
                    setupFlashMode();
                }
            }
        });

        if (!mCameraType.contains(CameraType.CAMERA_VIDEO)) {
            mTakePhotoBtn.setImageResource(R.drawable.camera_button);
            mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture();
                }
            });
        } else {
            mTakePhotoBtn.setOnTouchListener(new View.OnTouchListener() {
                Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mIsSafeToTakePhoto && !mIsRecording) {
                            new StartCameraTask().execute();
                        }
                    }
                };

                float mMovement;
                float mYPosition;
                int mMovementThreshold = 30;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_UP:
                            mRecordHandler.removeCallbacks(mRunnable);
                            mTakePhotoBtn.setImageResource(R.drawable.square_camera_unselected);
                            if (!mVideoProcessing) {
                                if (mIsRecording) {
                                    new StopCamera().execute();
                                } else {
                                    takePicture();
                                }
                            }
                            break;

                        case MotionEvent.ACTION_DOWN:
                            mYPosition = event.getY();
                            mMovement = 0;
                            mMovementThreshold = 30;
                            if (!mIsRecording && !mVideoProcessing) {
                                mTakePhotoBtn.setImageResource(R.drawable.square_camera_selected);
                                mRecordHandler.removeCallbacks(mRunnable);
                                mRecordHandler.postDelayed(mRunnable, 600);
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (mIsRecording) {
                                mMovement += mYPosition - event.getY();
                                mYPosition = event.getY();
                                if (Math.abs(mMovement) > mMovementThreshold) {
                                    mPreviewView.zoom(mMovement > 0 ? 2 : -2);
                                    mMovement = 0;
                                    mMovementThreshold = 5;
                                }
                            }
                            break;
                    }
                    return true;
                }
            });
        }
    }


    //hides the gallery, flash, and reverse camera button
    private void hideCameraButtons(boolean hide) {
        if (hide) {
            mFlashContainer.setVisibility(View.INVISIBLE);
            mReverseContainer.setVisibility(View.INVISIBLE);
        } else {
            mFlashContainer.setVisibility(View.VISIBLE);
            mReverseContainer.setVisibility(View.VISIBLE);
        }
    }


    private void showVideoTooShortDialog() {
        if (getActivity() == null) return;
        ///Log.i(TAG, "showVideoTooShortDialog: ");
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

    private void goToVideoEditFragment(Uri uri, Dimens videoDimen) {
        if (getActivity() != null) {
            try {
                final int returnType = ((CameraActivity) getActivity()).getReturnType();

                mPostOptions.type = PostOptions.ContentType.Video;
                getFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                EditFragment.newInstance(uri, returnType, videoDimen, mPostOptions ),
                                EditFragment.TAG)
                        .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                        .commit();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    // we do not have an auto flash
    // some phones crash if both autofocus and flash are on. There is no way to tell from autoflash is
    // flash is going to go off.
    private void setupFlashMode() {
        if (mFlashContainer == null || getContext() == null) return;
        ImageView view = ((ImageView) mFlashContainer.findViewById(R.id.flash_icon));
        if (mFlashOn) view.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        else view.clearColorFilter();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putBoolean(CAMERA_FLASH_KEY, mFlashOn);
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

            mStartCameraSubscription = Observable.create(new Observable.OnSubscribe<Void>() {
                @Override
                public void call(Subscriber<? super Void> subscriber) {
                    setSafeToTakePhoto(false);
                    setCameraFocusReady(false);
                    stopCameraPreview();
                    mCamera.release();
                    mCamera = null;
                    subscriber.onCompleted();
                }
            }).subscribeOn(io())
                    .observeOn(mainThread())
                    .subscribe(
                            new Subscriber<Void>() {
                                @Override
                                public void onCompleted() {
                                    if (getCamera(mCameraID)) { //were able to find a camera to use
                                        startCameraPreview();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onNext(Void aVoid) {
                                }
                            });
        } else if (getCamera(mCameraID)) { //were able to find a camera to use
            startCameraPreview();
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
        /*
      Determine the current display orientation and rotate the camera preview
      accordingly
     */
        int displayOrientation;
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(displayOrientation);
    }


    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestSize(mCamera.getParameters().getSupportedPreviewSizes());
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        List<String> focusmodes = parameters.getSupportedFocusModes();

        // Set continuous picture focus, if it's supported
        if (focusmodes != null && focusmodes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        parameters.setZoom(0);

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Camera.Size determineBestSize(List<Camera.Size> sizes) {

        Camera.Size bestSize = null;

        for (Camera.Size size : sizes) {
            boolean isDesireRatio = (size.width / 4) == (size.height / 3) || (size.height / 4) == (size.width / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize && size.height < MAX_RESOLUTION && size.width < MAX_RESOLUTION) {
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

            if (mFlashOn) {
                if (!turnOnFlashLight()) {
                    fadeInFlashForgreound(true);
                }

                // need to wait for flashlight to turn on fully
                mRecordHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                takeImageOfView();
                            }
                        }, 300);

            } else { //flash was off or flash failed to turn on
                takeImageOfView();
            }
        }
    }

    //returns true if flashlight turned on
    private boolean turnOnFlashLight() {
        List<String> flashModes = mCamera.getParameters().getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            Camera.Parameters p = mCamera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p);
            return true;
        }
        return false;
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

        mRecordHandler.removeCallbacksAndMessages(null);

        mShowCameraHandler.removeCallbacks(mShowPreview);

        if (mStartCameraSubscription != null) mStartCameraSubscription.unsubscribe();
        if (mProcessImageSubscriptions != null) mProcessImageSubscriptions.unsubscribe();

        if (mIsRecording && !mVideoProcessing) {
            mTakePhotoBtn.setImageResource(R.drawable.square_camera_unselected);
            if (mProgressAnimator != null) {
                mProgressAnimator.removeAllListeners();
                mProgressAnimator.cancel();
            }

            mRecordProgress.setVisibility(View.INVISIBLE);
            mRecordProgress.setProgress(0);
            mRecordProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.camera_progress_red));

            mTakePhotoBtn.setImageResource(R.drawable.square_camera_capture);

            hideCameraButtons(false);
            try {
                mMediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            releaseMediaRecorder();
            mIsRecording = false;
        }

        // stop the preview
        if (mCamera != null) {
            // we will get a runtime exception if camera had already been released, either by onpause
            // or when we switched cameras
            try {
                mCamera.cancelAutoFocus();
                stopCameraPreview();
                mCamera.release();
                mCamera = null;
            } catch (RuntimeException e) {
                Log.i(TAG, "onPause: release error : ignore");
                mCamera.release();
                mCamera = null;
            }
        }
        CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashOn);
    }


    private void takeImageOfView() {
//        subscriber.onNext(ImageUtility.savePicture(getActivity(), mPreviewView.getBitmap()));
        mProcessImageSubscriptions = Observable
                .just(stopCamera())
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(
                        new Action1<Void>() {
                            @Override
                            public void call(Void aVoid) {
                                if (getActivity() == null) return;

                                final int returnType = ((CameraActivity) getActivity()).getReturnType();

                                mStartCameraSubscription = Observable
                                        .just(saveBitmap())
                                        .subscribeOn(io())
                                        .observeOn(mainThread())
                                        .subscribe(
                                                new Action1<Uri>() {
                                                    @Override
                                                    public void call(Uri uri) {

                                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                                        options.inJustDecodeBounds = true;
                                                        BitmapFactory.decodeFile(uri.getPath(), options);

                                                        Dimens photoDimens = new Dimens(options.outWidth, options.outHeight);

                                                        mPostOptions.type = PostOptions.ContentType.Photo;

                                                        getFragmentManager()
                                                                .beginTransaction()
                                                                .replace(
                                                                        R.id.fragment_container,
                                                                        EditFragment.newInstance(uri, returnType, photoDimens, mPostOptions),
                                                                        EditFragment.TAG)
                                                                .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                                                                .commit();

                                                        setSafeToTakePhoto(true);
                                                    }
                                                }
                                        );
                            }
                        }
                );
    }


    private Uri saveBitmap() {
        //Log.d(TAG, "saveBitmap: "+mPreviewView.getBitmap().getWidth());
        //Log.i(TAG, "saveBitmap: "+(int)(mPreviewView.getBitmap().getWidth() * 6f / 5f));
        int width = mPreviewView.getWidth();
//        Bitmap previewBitmap = mPreviewView.getBitmap(width,
//                ScreenSizeSingleton.getSingleton().mHasRatioRequirement ?
//                        (int) (width * 6f / 5f) :
//                        width);

        Bitmap original = mPreviewView.getBitmap();
        Bitmap previewBitmap =
                Bitmap.createBitmap(original, 0, 0, width,
                        ScreenSizeSingleton.getSingleton().mHasRatioRequirement ?
                                (int) (width * 6f / 5f) :
                                width
                );

        if (previewBitmap != original)
            original.recycle();

        Uri uri = ImageUtility.savePicture(getContext(), previewBitmap);
        previewBitmap.recycle();
        return uri;
    }

    private Void stopCamera() {
        mCamera.stopPreview();
        return null;
    }


    private void fadeInFlashForgreound(final boolean show) {
        mFlashTop.clearAnimation();

        AlphaAnimation alphaAnimation = show ?
                new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(200);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFlashTop.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mFlashTop.startAnimation(alphaAnimation);
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraAndWritePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasPermission(Manifest.permission.CAMERA)
                && (!mCameraType.contains(CameraType.CAMERA_VIDEO) || hasPermission(Manifest.permission.RECORD_AUDIO));
    }


    private Uri prepareMediaRecorder() {
        if (getActivity() == null) return null;

        //profile might not exist
        // if 480 doesnt exist, use lowest possible
        CamcorderProfile camcorderProfile = CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P) ?
                CamcorderProfile.get(CamcorderProfile.QUALITY_480P) :
                CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);

        Camera.Parameters p = mCamera.getParameters();
        Size vidSize = p.getSupportedVideoSizes() != null ? determineBestSize(p.getSupportedVideoSizes()) : p.getPreviewSize();

        //Log.i(TAG, "prepareMediaRecorder: "+vidSize.width);
        //Log.i(TAG, "prepareMediaRecorder: "+vidSize.height);

        mVideoDimen = new Dimens(
                vidSize.width,
                vidSize.height,
                mCameraID == CameraInfo.CAMERA_FACING_FRONT
        );

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        mMediaRecorder.setProfile(camcorderProfile);
        mMediaRecorder.setVideoSize(vidSize.width, vidSize.height);

        mMediaRecorder.setOrientationHint(mCameraID == CameraInfo.CAMERA_FACING_FRONT ? 270 : 90);

        final Uri imageUri;

        try {
            imageUri = Uri.parse(ImageUtility.getVideoUri()); //Uri.parse(ImageUtility.getTempFilePath(getActivity(), "uncropped_video", ".mp4"));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //delete the cached video if  we can. Takes up a lot of space
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
        protected void onPreExecute() {
            super.onPreExecute();
            if (mFlashOn && !turnOnFlashLight()) {
                fadeInFlashForgreound(true);
            }
            mIsRecording = true;
            if(mToolbar != null){
                mToolbar.setTitle("Recording...");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            mVideoUri = prepareMediaRecorder();

            if (mVideoUri != null) {
                try {
                    mMediaRecorder.start();
                    return true;
                } catch (RuntimeException e) {
                    Log.i(TAG, "doInBackground: failed to start video");
                }
            }

            return false;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            mIsRecording = aBoolean;
            if (aBoolean) {
                hideCameraButtons(true);

                mRecordProgress.setMax(1000);
                mRecordProgress.setVisibility(View.VISIBLE);
                mProgressAnimator =
                        ObjectAnimator.ofInt(mRecordProgress, "progress", 0, 1000);

                mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (animation.getCurrentPlayTime() >= 2000) {
                            mProgressAnimator.removeAllUpdateListeners();
                            mRecordProgress.setProgressDrawable(
                                    ContextCompat.getDrawable(getContext(), R.drawable.camera_progress));
                        }
                    }
                });

                mProgressAnimator.setDuration(15000);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.start();

                mTakePhotoBtn.setImageResource(R.drawable.square_camera_record);
            } else {
                mFlashTop.setVisibility(View.INVISIBLE);
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
            if(mToolbar != null){
                mToolbar.setTitle("Camera");
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
            mRecordProgress.setVisibility(View.INVISIBLE);
            mRecordProgress.setProgress(0);
            mRecordProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.camera_progress_red));

            if (mFlashTop.getVisibility() == View.VISIBLE)
                fadeInFlashForgreound(false);

            if (mCamera != null) {
                Camera.Parameters p = mCamera.getParameters();
                if (p.getSupportedFlashModes() != null && p.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(p);
                }
            }

            mTakePhotoBtn.setImageResource(R.drawable.square_camera_capture);
            hideCameraButtons(false);

            mIsRecording = false;
            mVideoProcessing = false;

            if (duration < 2000) {
                //showVideoTooShortDialog();
                takePicture();
            } else if (mVideoUri != null) {  //go to edit video screen
                goToVideoEditFragment(mVideoUri, mVideoDimen);
            }
        }
    }
}

