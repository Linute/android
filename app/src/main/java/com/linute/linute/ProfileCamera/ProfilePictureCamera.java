package com.linute.linute.ProfileCamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraSettingPreferences;
import com.linute.linute.SquareCamera.ImageUtility;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 7/5/16.
 */
@SuppressWarnings("deprecation")
public class ProfilePictureCamera extends Fragment implements Camera.PictureCallback, Camera.ShutterCallback {

    public static final String TAG = ProfilePictureCamera.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_on";

    private Camera mCamera;

    private int mCameraID;
    private boolean mFlashOn;

    private SurfaceTexture mSurfaceHolder;


    private Handler mRecordHandler = new Handler();
    private Handler mShowPreviewHandler = new Handler();

    private Subscription mProcessImageSubscriptions;
    private Subscription mStartCameraSubscription;

    //private View vFocusCircle;
    private TextView vFlashText;
    private View vFlash;
    private SquareCameraPreview vPreviewView;

    private boolean mSurfaceAlreadyCreated = false;
    private boolean mIsSafeToTakePhoto = false;


    public ProfilePictureCamera() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mCameraID = getFrontCameraID();
            mFlashOn = CameraSettingPreferences.getCameraFlashMode(getActivity());
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashOn = savedInstanceState.getBoolean(CAMERA_FLASH_KEY);
        }
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_take_profile_picture, container, false);

        vFlash = root.findViewById(R.id.flash);
        vFlashText = (TextView) vFlash.findViewById(R.id.flash_text);
        vPreviewView = (SquareCameraPreview) root.findViewById(R.id.camera_preview_view);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().setResult(Activity.RESULT_CANCELED);
                getActivity().finish();
            }
        });

        View reverseCam = root.findViewById(R.id.switch_camera);
        if (Camera.getNumberOfCameras() < 2) {
            reverseCam.setVisibility(View.GONE);
        } else {
            reverseCam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCameraID = getBackCameraID();
                    } else {
                        mCameraID = getFrontCameraID();
                    }
                    restartPreview();
                }
            });
        }

        root.findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        vFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSafeToTakePhoto && toggleFlash()){
                    setupFlashMode();
                }
            }
        });

        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vPreviewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //Log.i(TAG, "onSurfaceTextureAvailable: ");
                mSurfaceHolder = surface;
                if (hasCameraAndWritePermission()) {
                    mSurfaceAlreadyCreated = true;
                    //start camera after slight delay. Without delay,
                    //there is huge lag time between active and inactive app state
                    mShowPreviewHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mCamera == null) restartPreview();
                        }
                    }, 250);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                mSurfaceAlreadyCreated = false;
                mShowPreviewHandler.removeCallbacksAndMessages(null);
                // stop the preview
                if (mCamera != null) {
                    // we will get a runtime exception if camera had already been released, either by onpause
                    // or when we switched cameras

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


    @Override
    public void onPause() {
        super.onPause();

        mRecordHandler.removeCallbacksAndMessages(null);
        mShowPreviewHandler.removeCallbacksAndMessages(null);

        if (mStartCameraSubscription != null) mStartCameraSubscription.unsubscribe();
        if (mProcessImageSubscriptions != null) mProcessImageSubscriptions.unsubscribe();

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


    // we do not have an auto flash
    // some phones crash if both autofocus and flash are on. There is no way to tell from autoflash is
    // flash is going to go off.
    private void setupFlashMode() {
        vFlashText.setText(mFlashOn ? "On" : "Off");
    }

    /**
     * Take a picture
     */
    private void takePicture() {
        if (mIsSafeToTakePhoto) {
            mIsSafeToTakePhoto = false;

            // can cause runtime exception if click take picture too quickly when activity starts
            try {
                if (mFlashOn) {
                    mCamera.takePicture(this, null, null, this);
                } else { //flash was off or flash failed to turn on
                    mCamera.cancelAutoFocus();
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            mCamera.takePicture(ProfilePictureCamera.this, null, null, ProfilePictureCamera.this);
                        }
                    });
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                mIsSafeToTakePhoto = true;
            }
        }
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(getActivity(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraAndWritePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasPermission(Manifest.permission.CAMERA);
    }

    /**
     * Restart the camera preview
     */
    private void restartPreview() {
        if (mCamera != null) {

            mStartCameraSubscription = Observable.create(new Observable.OnSubscribe<Void>() {
                @Override
                public void call(Subscriber<? super Void> subscriber) {
                    mIsSafeToTakePhoto = false;
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

    //returns true if able to get camera and false if we werent able to
    private boolean getCamera(int cameraID) {
        try {
            mCamera = Camera.open(cameraID);
            vPreviewView.setCamera(mCamera);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
            return false;
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
            mIsSafeToTakePhoto = true;
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
        mIsSafeToTakePhoto = false;
        setCameraFocusReady(false);

        // Nulls out callbacks, stops face detection
        mCamera.stopPreview();
        vPreviewView.setCamera(null);
    }

    private void setCameraFocusReady(final boolean isFocusReady) {
        if (this.vPreviewView != null) {
            vPreviewView.setIsFocusReady(isFocusReady);
        }
    }

    private void determineDisplayOrientation() {
        mCamera.setDisplayOrientation(90);
    }

    private boolean isFrontFacingCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }


    /**
     * Setup the camera parameters
     */
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Camera.Size bestPreviewSize = determineBestSize(mCamera.getParameters().getSupportedPreviewSizes());
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        Camera.Size bestPictureSize = determineBestSize(mCamera.getParameters().getSupportedPictureSizes());
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        List<String> focusmodes = parameters.getSupportedFocusModes();
        // Set continuous picture focus, if it's supported
        if (focusmodes != null && focusmodes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_ON)){
            vFlash.setVisibility(View.VISIBLE);
            setupFlashMode();

            if (mFlashOn) parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        }else {
            vFlash.setVisibility(View.GONE);
            mFlashOn = false;
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Camera.Size determineBestSize(List<Camera.Size> sizes) {

        Camera.Size bestSize = null;

        for (Camera.Size size : sizes) {
            boolean isDesireRatio = (size.width / 4) == (size.height / 3) || (size.height / 4) == (size.width / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize && size.height < 3000 && size.width < 3000) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (getActivity() == null) return;

        mProcessImageSubscriptions =
                Observable.just(ImageUtility.savePicture(getActivity(), rotateImage(data)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Uri>() {
                            @Override
                            public void call(Uri uri) {
                                ProfileCameraActivity activity = (ProfileCameraActivity) getActivity();
                                if (activity != null){
                                    activity.replaceFragment(
                                            ConfirmProfilePicture.newInstance(uri),
                                            ConfirmProfilePicture.TAG
                                    );
                                }
                            }
                        });
    }

    private Bitmap rotateImage(byte[] data) {
        Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix mat = new Matrix();
        boolean front = isFrontFacingCamera();

        if (front) {
            mat.setScale(1, -1);
            mat.postRotate(270);
        } else {
            mat.setRotate(90);
        }

        if (front) {
            image = Bitmap.createBitmap(
                    image,
                    image.getWidth() - image.getHeight(),
                    0,
                    image.getHeight(),
                    image.getHeight(),
                    mat,
                    true
            );
        } else {
            image = Bitmap.createBitmap(image, 0, 0, image.getHeight(), image.getHeight(), mat, true);
        }

        return image;
    }

    @Override
    public void onShutter() {
        if (getActivity() == null) return;
        AudioManager mgr = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
    }


    private boolean toggleFlash(){

        if (mCamera == null) return false;

        Camera.Parameters parameters = mCamera.getParameters();
        String flashmode = mFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_ON;

        if (parameters.getSupportedFlashModes().contains(flashmode)){
            mFlashOn = !mFlashOn;
            parameters.setFlashMode(flashmode);
            mCamera.setParameters(parameters);
            return true;
        }

        return false;
    }
}

