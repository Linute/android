package com.linute.linute.SquareCamera;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;

import com.linute.linute.Camera.commonsware.cwac.camera.CameraView;
import com.linute.linute.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TakePictureFragment extends Fragment {


    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_SETUP_PHOTO = 1;

    public static final String TAG = TakePictureFragment.class.getSimpleName();
    private View mShutter;
    private CameraView mCameraView;
    private Button mTakePhotoButton;
    private Button mQuitTakePhotoButton;

    View mUpperLayer;
    View mLowerLayer;

    View mRationailityLayer;

    private File mPhotoFile;


    public TakePictureFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TakePictureFragment newInstance() {
        TakePictureFragment fragment = new TakePictureFragment();
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_take_picture, container, false);

        bindViews(rootView);
        setUpButtons();

        mCameraView.setHost(((CameraActivity) getActivity()).getCameraHost());
        return rootView;
    }

    private void bindViews(View v) {
        mShutter = v.findViewById(R.id.vShutter);
        mCameraView = (CameraView) v.findViewById(R.id.cameraView);
        mTakePhotoButton = (Button) v.findViewById(R.id.btnTakePhoto);
        mQuitTakePhotoButton = (Button) v.findViewById(R.id.btn_cancel);
        mUpperLayer = v.findViewById(R.id.takePic_upper_layer);
        mLowerLayer = v.findViewById(R.id.takePic_lower_layer);
        mRationailityLayer = v.findViewById(R.id.takePic_rationality_text_layer);
    }

    private void setUpButtons() {
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTakePhotoClick();
            }
        });
    }

    private boolean mHasCheckedPermissions;

    @Override
    public void onResume() {
        super.onResume();
        if (!mHasCheckedPermissions) {
            Log.i(TAG, "onResume: llllll");
            requestPermissions();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: test");
        mCameraView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("Stop", true);
        super.onSaveInstanceState(outState);
    }

    private static final int REQUEST_PERMISSIONS = 21;

    public void requestPermissions() {

        /*
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            showPermissionNeeded();
            return;
        }*/

        List<String> permissions = new ArrayList<>();
        //check for camera
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        //check for write
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //we need permissions
        if (!permissions.isEmpty()) {
            requestPermissions(
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSIONS);
        } else {
            //we have permissions : show camera
            Log.i(TAG, "heelllooo");
            launchCamera();
        }
    }

    private void launchCamera() {
        if (mRationailityLayer.getVisibility() == View.VISIBLE) {
            mRationailityLayer.setVisibility(View.GONE);
        }
        if (mCameraView.getVisibility() == View.INVISIBLE)
            mCameraView.setVisibility(View.VISIBLE);
        Log.i(TAG, "launchCamera: ");
        mCameraView.onResume();
    }

    private void showPermissionNeeded() {
        if (mRationailityLayer.getVisibility() == View.GONE) {
            mRationailityLayer.setVisibility(View.VISIBLE);
            mCameraView.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                mHasCheckedPermissions = true;
                for (int result : grantResults) // if we didn't get approved for a permission, show permission needed frag
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        showPermissionNeeded();
                        Log.i(TAG, "onRequestPermissionsResult: ");
                        return;
                    }
                launchCamera();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onTakePhotoClick() {
        //TODO: flash
        mTakePhotoButton.setEnabled(false);
        mCameraView.takePicture(true, true);
        animateShutter();
    }

    private void animateShutter() {
        mShutter.setVisibility(View.VISIBLE);
        mShutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(mShutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(mShutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mShutter.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }
}
