package com.linute.linute.SquareCamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.linute.linute.MainContent.EditScreen.EditFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;

import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends BaseSocketActivity {

    public final static int SEND_POST = 14;  //send image/video to server
    public final static int RETURN_URI = 15; //save image and return image/video uri
    public final static int RETURN_URI_AND_PRIVACY = 16;

    public final static int IMAGE = 1;
    public final static int VIDEO = 2;
    public final static int ALL = 3;

    //if we get send url, we will send result to url,
    //    else, we'll send back a uri
    public final static String RETURN_TYPE = "send_to_url";
    public final static String CAMERA_TYPE = "camera_type";
    //public final static String GALLERY_TYPE = "gallery_filters";
    public final static String ANON_KEY = "anon_key";
    public final static String CONTENT_SUB_TYPE = "content_sub_type";

    public EditFragment.ContentSubType contentType = EditFragment.ContentSubType.None;

    private CameraType mCameraType;
    private int mReturnType;
    //private int mGalleryType;

    public static final String TAG = CameraActivity.class.getSimpleName();

    protected boolean mHasWriteAndCameraPermission = false;

    /**
     * need the following intent:
     * CameraActivity.CAMERA_TYPE - CAMERA_AND_VIDEO_AND_GALLERY or JUST_CAMERA
     * CameraActivity.RETURN_TYPE - SEND_POST or RETURN_URI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.squarecamera__activity_camera);

        ScreenSizeSingleton.init(getWindowManager());

        Intent i = getIntent();
        if (i != null) {
            mCameraType = i.getParcelableExtra(CAMERA_TYPE);
            if (mCameraType == null)
                mCameraType = new CameraType(CameraType.CAMERA_PICTURE);

            mReturnType = i.getIntExtra(RETURN_TYPE, RETURN_URI);
            contentType = (EditFragment.ContentSubType)i.getSerializableExtra(CONTENT_SUB_TYPE);
            if(contentType == null){
                contentType = EditFragment.ContentSubType.None;
            }
            //mGalleryType = i.getIntExtra(GALLERY_TYPE, ALL);
        } else {
            mCameraType = new CameraType(CameraType.CAMERA_PICTURE);
            mReturnType = RETURN_URI;
            //mGalleryType = ALL;
        }

        clearBackStack();

        requestPermissions();

        if (savedInstanceState == null && mHasWriteAndCameraPermission) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, CameraFragment.newInstance(contentType), CameraFragment.TAG)
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
        outState.putParcelable("cameraType", mCameraType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCameraType = savedInstanceState.getParcelable("cameraType");
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

        if (mCameraType.contains(CameraType.CAMERA_VIDEO) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
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
                .replace(R.id.fragment_container, CameraFragment.newInstance(contentType), CameraFragment.TAG)
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
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//            if (getSupportFragmentManager().findFragmentByTag(SendToFragment.TAG) != null) {
//                getSupportFragmentManager().popBackStack();
//                return;
//            }
//
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag(EditFragment.TAG);

            if (fragment != null) {
                getSupportFragmentManager().popBackStack();
            } else {
                clearBackStack();
            }
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    public CameraType getCameraType() {
        return mCameraType;
    }

    public int getReturnType() {
        return mReturnType;
    }

//    public int getGalleryType() {
//        return mGalleryType;
//    }
}