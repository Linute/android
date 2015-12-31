package com.linute.linute.SquareCamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.linute.linute.Camera.commonsware.cwac.camera.CameraHost;
import com.linute.linute.Camera.commonsware.cwac.camera.CameraHostProvider;
import com.linute.linute.Camera.commonsware.cwac.camera.PictureTransaction;
import com.linute.linute.Camera.commonsware.cwac.camera.SimpleCameraHost;
import com.linute.linute.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//TODO: might have to keep track of fragments
public class CameraActivity extends AppCompatActivity implements CameraHostProvider {

    public static final String TAG = CameraActivity.class.getSimpleName();

    private File mPhotoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        requestPermissions();
    }

    public File getPhotoFile() {
        return mPhotoFile;
    }

    @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(this);
    }


/*
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

    public void onCancel(View view) {
        getSupportFragmentManager().popBackStack();
    }
*/

    @Override
    protected void onPause() {
        super.onPause();
        //getSupportFragmentManager().popBackStack();
    }

    private static final int REQUEST_PERMISSIONS = 21;

    public void requestPermissions(){
        List<String> permissions = new ArrayList<>();
        //check for camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.CAMERA);
        }
        //check for write
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //we need permissions
        if (!permissions.isEmpty()){
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSIONS);
        }else {
            //we have permissions : show camera
            launchCameraFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                for (int result : grantResults) // if we didn't get approved for a permission, show permission needed frag
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        launchPermissionNeededFragment();
                        return;
                    }
                launchCameraFragment();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void launchCameraFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.camera_fragment_container, TakePictureFragment.newInstance(), TakePictureFragment.TAG)
                .commitAllowingStateLoss();
    }

    private void launchPermissionNeededFragment(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.camera_fragment_container, new NeedPermissionsFragment(), NeedPermissionsFragment.TAG)
                .commitAllowingStateLoss();
    }

    class MyCameraHost extends SimpleCameraHost {

        private Camera.Size previewSize;

        public MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, final Bitmap bitmap) {
            //TODO: Go to edit fragment
            Log.i("TAKENPHOTO", "TAKEN");
        }

        @Override
        public void saveImage(PictureTransaction xact, byte[] image) {
            super.saveImage(xact, image);
            mPhotoFile = getPhotoPath();
        }
    }
}

