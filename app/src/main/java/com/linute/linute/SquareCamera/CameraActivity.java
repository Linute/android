package com.linute.linute.SquareCamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.linute.linute.R;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();

    private boolean mHasWriteAndCameraPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.squarecamera__CameraFullScreenTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.squarecamera__activity_camera);

        requestPermissions();

        if (savedInstanceState == null && mHasWriteAndCameraPermission) {
            Log.i(TAG, "onCreate: MAIN");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, CameraFragment.newInstance(), CameraFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRecievedRequestPermissionResults){
            Log.i(TAG, "onResume: PERMISSIONS");
            clearBackStack();
            if (mHasWriteAndCameraPermission) launchCameraFragment();
            else launchPermissionNeededFragment();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!hasCameraAndWritePermission()) return;

        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) { //got image from gallery
            beginCrop(data.getData()); //crop image
        } else if (requestCode == Crop.REQUEST_CROP) { //photo came back from crop
            if (resultCode == RESULT_OK) {
                Uri imageUri = Crop.getOutput(data);

            } else if (resultCode == Crop.RESULT_ERROR) { //error cropping, show error
                Toast.makeText(this, Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void returnPhotoUri(Uri uri) {
        Intent data = new Intent();
        data.setData(uri);

        if (getParent() == null) {
            setResult(RESULT_OK, data);
        } else {
            getParent().setResult(RESULT_OK, data);
        }

        finish();
    }

    private void beginCrop(Uri source) { //begin crop activity
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    public void onCancel(View view) {
        getSupportFragmentManager().popBackStack();
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraAndWritePermission() {
        return hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && hasPermission(Manifest.permission.CAMERA);
    }


    private static final int REQUEST_PERMISSIONS = 21;

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


    private boolean mRecievedRequestPermissionResults = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:

                mRecievedRequestPermissionResults = true;

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


    private void launchCameraFragment() {
        Log.i(TAG, "launchedCamera");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, CameraFragment.newInstance(), CameraFragment.TAG)
                .commit();
    }

    private void launchPermissionNeededFragment() {
        Log.i(TAG, "launchPermissionNeededFragment: ");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NeedPermissionsFragment(), NeedPermissionsFragment.TAG)
                .commit();
    }

    private void launchEditAndSaveFragment(Uri uri){

    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();

        //if we are currently in edit photo
        if (manager.getBackStackEntryCount() == 1)
            manager.popBackStack();
    }
}