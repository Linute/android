package com.linute.linute.SquareCamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.linute.linute.R;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();

    private boolean mHasWriteAndCameraPermission = false;

    private ImageParameters mImageParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.squarecamera__CameraFullScreenTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.squarecamera__activity_camera);

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

        if (mRecievedRequestPermissionResults) { //only runs if we have updated permissions information
            clearBackStack(); //clears gallery or camera fragment
            if (mHasWriteAndCameraPermission) launchCameraFragment();
            else launchPermissionNeededFragment();
        }
    }

    public void setImageParameters(ImageParameters param) {
        mImageParameters = param;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!hasCameraAndWritePermission()) return;

        if (resultCode == RESULT_CANCELED) { //cancelled gallery pick or crop
            clearBackStack(); //remove gallery fragment
        }

        else if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) { //got image from gallery
            beginCrop(data.getData()); //crop image
        }

        else if (requestCode == Crop.REQUEST_CROP) { //photo came back from crop
            if (resultCode == RESULT_OK) {

                Uri imageUri = Crop.getOutput(data);
                launchEditAndSaveFragment(imageUri);

            } else if (resultCode == Crop.RESULT_ERROR) { //error cropping, show error
                Toast.makeText(this, Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void beginCrop(Uri source) { //begin crop activity
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, CameraFragment.newInstance(), CameraFragment.TAG)
                .commit();
    }

    private void launchPermissionNeededFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NeedPermissionsFragment(), NeedPermissionsFragment.TAG)
                .commit();
    }

    private void launchEditAndSaveFragment(Uri uri) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        EditSavePhotoFragment.newInstance(uri, mImageParameters),
                        EditSavePhotoFragment.TAG)
                .addToBackStack(EDIT_AND_GALLERY_STACK_NAME)
                .commit();
    }

    public void clearBackStack() { //pops all frag with name
        getSupportFragmentManager().popBackStack(EDIT_AND_GALLERY_STACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static final String EDIT_AND_GALLERY_STACK_NAME = "edit_and_gallery_stack_name";

    public void launchGalleryFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        GalleryFragment.newInstance(),
                        GalleryFragment.TAG)
                .addToBackStack(EDIT_AND_GALLERY_STACK_NAME)
                .commit();

        goToGalleryAndCrop();
    }

    private void goToGalleryAndCrop() {
        Crop.pickImage(this);
    }
}