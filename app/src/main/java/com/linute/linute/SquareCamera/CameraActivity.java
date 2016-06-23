package com.linute.linute.SquareCamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.linute.linute.R;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    public final static int CAMERA_AND_VIDEO_AND_GALLERY = 11; // everything
    public final static int JUST_CAMERA = 12;  //just camera; no gallery or video option

    public final static int SEND_POST = 14;  //send image/video to server
    public final static int RETURN_URI = 15; //save image and return image/video uri

    public final static int IMAGE = 1;
    public final static int VIDEO = 2;

    //if we get send url, we will send result to url,
    //    else, we'll send back a uri
    public final static String RETURN_TYPE = "send_to_url";
    public final static String CAMERA_TYPE = "camera_type";

    private int mCameraType;
    private int mReturnType;

    public static final String TAG = CameraActivity.class.getSimpleName();

    protected boolean mHasWriteAndCameraPermission = false;


    /** need the following intent:
     *  CameraActivity.CAMERA_TYPE - CAMERA_AND_VIDEO_AND_GALLERY or JUST_CAMERA
     *  CameraActivity.RETURN_TYPE - SEND_POST or RETURN_URI
     *
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.squarecamera__activity_camera);
        HasSoftKeySingleton.getmSoftKeySingleton(getWindowManager());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Intent i = getIntent();
        if (i != null) {
            mCameraType = i.getIntExtra(CAMERA_TYPE, JUST_CAMERA);
            mReturnType = i.getIntExtra(RETURN_TYPE, RETURN_URI);
        } else {
            mCameraType = JUST_CAMERA;
            mReturnType = RETURN_URI;
        }

        requestPermissions();

        if (savedInstanceState == null && mHasWriteAndCameraPermission) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, CameraFragment.newInstance(), CameraFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReceivedRequestPermissionResults) { //only runs if we have updated permissions information
            clearBackStack(); //clears gallery or camera fragment
            if (mHasWriteAndCameraPermission) launchCameraFragment();
            else launchPermissionNeededFragment();
            mReceivedRequestPermissionResults = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("returnType", mReturnType);
        outState.putInt("cameraType", mCameraType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCameraType = savedInstanceState.getInt("cameraType");
        mReturnType = savedInstanceState.getInt("returnType");
    }

    protected static final int REQUEST_PERMISSIONS = 21;

    public void requestPermissions() {
        List<String> permissions = new ArrayList<>();
        //check for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        //check for write
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (mCameraType == CAMERA_AND_VIDEO_AND_GALLERY) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
        }

        //we need permissions
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSIONS);
        } else {
            //we have permissions : show camera
            mHasWriteAndCameraPermission = true;
        }
    }


    protected boolean mReceivedRequestPermissionResults = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:

                mReceivedRequestPermissionResults = true;

                for (int result : grantResults) // if we didn't get approved for a permission, show permission needed frag
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        mHasWriteAndCameraPermission = false;
                        return;
                    }

                mHasWriteAndCameraPermission = true;
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    protected void launchCameraFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, CameraFragment.newInstance(), CameraFragment.TAG)
                .commit();
    }

    protected void launchPermissionNeededFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NeedPermissionsFragment(), NeedPermissionsFragment.TAG)
                .commit();
    }


    public void clearBackStack() { //pops all frag with name
        getSupportFragmentManager().popBackStack(EDIT_AND_GALLERY_STACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static final String EDIT_AND_GALLERY_STACK_NAME = "edit_and_gallery_stack_name";

    public void launchFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(EDIT_AND_GALLERY_STACK_NAME)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) clearBackStack();
        else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    public int getCameraType() {
        return mCameraType;
    }

    public int getReturnType() {
        return mReturnType;
    }
}