package com.linute.linute.SquareCamera;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_on";

    private int mCameraType;
    private int mCameraID;
    private boolean mFlashOn;

    private Camera mCamera;
    private CustomCameraPreview mPreviewView;
    private SurfaceTexture mSurfaceHolder;
    private MediaRecorder mMediaRecorder;

    private ImageView mTakePhotoBtn;
    private EditSaveVideoFragment.VideoDimen mVideoDimen;
    private View mCameraOps;
    private View mGalleryButton;

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

    private Point mDisplayMetrics;

    public static Fragment newInstance() {
        return new CameraFragment();
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
            mCameraID = getBackCameraID();
            mFlashOn = CameraSettingPreferences.getCameraFlashMode(getActivity());
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashOn = savedInstanceState.getBoolean(CAMERA_FLASH_KEY);
        }

        mDisplayMetrics = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getRealSize(mDisplayMetrics);
        mCameraType = ((CameraActivity) getActivity()).getCameraType();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.square_camera_take_photo, container, false);

        mTakePhotoBtn = (ImageView) root.findViewById(R.id.capture_image_button);

        //mAnonCheckbox = (CheckBox) root.findViewById(R.id.anon_checkbox);
        mGalleryButton = root.findViewById(R.id.cameraFragment_galleryButton);

        if (mCameraType == CameraActivity.JUST_CAMERA) {
            mGalleryButton.setVisibility(View.INVISIBLE);
        } else {
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

        setupFlashMode();
    }

    //buttons will only be set after camera view has appeared
    private void setOnClickListeners(final View view) {
        final View changeCameraFlashModeBtn = view.findViewById(R.id.flash);

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

        changeCameraFlashModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto) {
                    mFlashOn = !mFlashOn;
                    setupFlashMode();
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

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_UP)) {
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
                            mRecordHandler.postDelayed(mRunnable, 600);
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

    private void goToVideoEditFragment(Uri uri, EditSaveVideoFragment.VideoDimen videoDimen) {
        if (getActivity() != null) {
            try {
                //// TODO: 6/4/16 fix saveVid params
                getFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                EditSaveVideoFragment.newInstance(uri, false, videoDimen),
                                EditSaveVideoFragment.TAG)
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
        View view = getView();
        if (view == null) return;

        ((TextView) view.findViewById(R.id.auto_flash_icon)).setText(mFlashOn ? "On" : "Off");
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
        Size bestPictureSize = determineBestSize(mCamera.getParameters().getSupportedPictureSizes());

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

        final View changeCameraFlashModeBtn = getView().findViewById(R.id.flash);
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
            mFlashOn = false;
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestSize(List<Size> sizes) {

        Size bestSize = null;

        for (Size size : sizes) {
            //same size as screen, return the size
            if ((mDisplayMetrics.y == size.height && mDisplayMetrics.x == size.width) ||
                    (mDisplayMetrics.x == size.height && mDisplayMetrics.y == size.width))
                return size;

            //better size
            if ((bestSize == null) || size.width < mDisplayMetrics.x || size.height < mDisplayMetrics.x) {
                //ratio we need
                if ((mDisplayMetrics.x * size.width == mDisplayMetrics.y * size.height) ||
                        (mDisplayMetrics.y * size.width == mDisplayMetrics.x * size.height))
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

            if (mFlashOn && turnOnFlashLight()) {
                // need to wait for flashlight to turn on fully
                mRecordHandler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                takeImageOfView();
                            }
                        }, 300);
            } else {
                takeImageOfView();
            }
        }
    }

    //returns true if flashlight turned on
    private boolean turnOnFlashLight() {
        if (mCamera.getParameters().getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
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
                Log.i(TAG, "onPause: ");
            } catch (RuntimeException e) {
                Log.i(TAG, "onPause: release error : ignore");
                mCamera.release();
                mCamera = null;
            }
        }
        CameraSettingPreferences.saveCameraFlashMode(getActivity(), mFlashOn);
    }

    private void takeImageOfView() {
        mProcessImageSubscriptions = Observable.create(
                new Observable.OnSubscribe<Uri>() {
                    @Override
                    public void call(Subscriber<? super Uri> subscriber) {
                        mCamera.stopPreview();
                        subscriber.onNext(ImageUtility.savePicture(getActivity(), mPreviewView.getBitmap()));
                    }
                })
                .subscribeOn(io())
                .observeOn(mainThread()).subscribe(new Action1<Uri>() {
                    @Override
                    public void call(Uri uri) {
                        getFragmentManager()
                                .beginTransaction()
                                .replace(
                                        R.id.fragment_container,
                                        EditSavePhotoFragment.newInstance(uri),
                                        EditSavePhotoFragment.TAG)
                                .addToBackStack(CameraActivity.EDIT_AND_GALLERY_STACK_NAME)
                                .commit();

                        setSafeToTakePhoto(true);
                    }
                });
    }

    private boolean usingFrontFaceCamera() {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        return (info.facing == CameraInfo.CAMERA_FACING_FRONT);
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
        // if 480 doesnt exist, use lowest possible
        CamcorderProfile camcorderProfile = CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P) ?
                CamcorderProfile.get(CamcorderProfile.QUALITY_480P) :
                CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);

        //the profile might not be 4:3
        //is that is the case, use the height to find 4:3 ratio
        //remark: width is the longer side of the phone
        //        height is the shorter side

        //Log.i(TAG, "prepareMediaRecorder: h:" + camcorderProfile.videoFrameHeight + "W: " + camcorderProfile.videoFrameWidth);
//        int bestWidth = camcorderProfile.videoFrameWidth;
//
//        Size betterSize = getVideoSize(camcorderProfile.videoFrameHeight);

        // couldn't find a 4:3 ratio so set it to the profiles values
        //if (betterSize != null) bestWidth = betterSize.width;

        //sent to EditSaveVideoFragment dimensions so we know how much to crop

        //Log.i(TAG, "prepareMediaRecorder: "+bestWidth +" "+camcorderProfile.videoFrameHeight);


        // stop and restart
        // must stop and reset camera when params are changed
        // must set preview size to same size as video size or it will record green on some phones
        //mCamera.cancelAutoFocus();
//        Camera.Parameters param = mCamera.getParameters();
//
//        // video size and preview size might be different
//        // if they are, just fine a 4:3 preview size
//        if (param.getSupportedVideoSizes() != null) {
//            //Log.i(TAG, "prepareMediaRecorder: not null");
//            Camera.Size size = getVideoPreviewSize(camcorderProfile.videoFrameHeight);
//            param.setPreviewSize(size.width, size.height);
//        } else {
//            // Log.i(TAG, "prepareMediaRecorder: null");
//            param.setPreviewSize(bestWidth, camcorderProfile.videoFrameHeight);
//        }
//
//        mCamera.stopPreview();
//        mCamera.setParameters(param);
//        mCamera.startPreview();
        Camera.Parameters p = mCamera.getParameters();
        Size vidSize = p.getSupportedVideoSizes() != null ? determineBestSize(p.getSupportedVideoSizes()) : p.getPreviewSize();

        mVideoDimen = new EditSaveVideoFragment.VideoDimen(
                vidSize.height,
                vidSize.width, mCameraID == CameraInfo.CAMERA_FACING_FRONT
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
            mRecordProgress.setVisibility(View.INVISIBLE);
            mRecordProgress.setProgress(0);
            mRecordProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.camera_progress_red));

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
