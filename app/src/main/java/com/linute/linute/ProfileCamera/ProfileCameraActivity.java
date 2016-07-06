package com.linute.linute.ProfileCamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.linute.linute.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by QiFeng on 7/5/16.
 */
public class ProfileCameraActivity extends AppCompatActivity {

    public static final String TAG = ProfileCameraActivity.class.getSimpleName();
    public static final int TYPE_GALLERY = 99;
    public static final int TYPE_CAMERA = 100;
    public static final String TYPE_KEY = "profile_cam_type";

    private int mType;


    /**
     * Needs intent:
     * TYPE - Gallery for picking and cropping
     * Camera for taking pictures
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.squarecamera__activity_camera);

        Intent i = getIntent();
        if (i != null) mType = i.getIntExtra(TYPE_KEY, TYPE_CAMERA);
        else mType = TYPE_CAMERA;

        if (savedInstanceState == null) {
            if (mType == TYPE_GALLERY){
                replaceWithoutAddingToBackstack(new ProfilePictureGalleryFragment());
            }
            else {
                boolean hasPerm = requestPermissions();
                if (hasPerm) {
                    replaceWithoutAddingToBackstack(new ProfilePictureCamera());
                }
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE_KEY, mType);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mType = savedInstanceState.getInt(TYPE_KEY);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        if (mType == TYPE_GALLERY){
            finish();
        }else {
            super.onBackPressed();
        }
    }

    protected static final int REQUEST_PERMISSIONS = 23;

    public boolean requestPermissions() {
        List<String> permissions = new ArrayList<>();
        //check for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        //check for write
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        //we need permissions
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSIONS);
            return false;
        }

        //we have permissions : show camera
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:

                //mReceivedRequestPermissionResults = true;

                for (int result : grantResults) // if we didn't get approved for a permission, show permission needed frag
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new ProfilePictureCamera(), ProfilePictureCamera.TAG)
                                .commit();
                        return;
                    }

                Log.i(TAG, "onRequestPermissionsResult: rational");
                //// TODO: 7/5/16 rational

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void replaceFragment(Fragment fragment, String tag){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }


    public void replaceWithoutAddingToBackstack(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
