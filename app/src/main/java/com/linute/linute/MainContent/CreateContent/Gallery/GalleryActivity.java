package com.linute.linute.MainContent.CreateContent.Gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import com.linute.linute.MainContent.EditScreen.PostOptions;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;

/**
 * Created by mikhail on 8/20/16.
 */

//// TODO: 8/30/16 Permission
public class GalleryActivity extends BaseSocketActivity {
    public static final String TAG = GalleryActivity.class.getSimpleName();
    public static final int REQ_READ_EXT_STORAGE = 52;

    private int mGalleryType;
    private int mReturnType;
    public static final String ARG_GALLERY_TYPE = "gallery_type";
    public static final String ARG_RETURN_TYPE = "return_type";
    public static final String ARG_POST_OPTIONS = "post_options";


    public static final int PICK_IMAGE = 0;
    public static final int PICK_VIDEO = 1;
    public static final int PICK_ALL = 2;

    private PostOptions mPostOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent intent = getIntent();
        mGalleryType = intent.getIntExtra(ARG_GALLERY_TYPE, PICK_ALL);
        mReturnType = intent.getIntExtra(ARG_RETURN_TYPE, CameraActivity.RETURN_URI_AND_PRIVACY);
        mPostOptions = intent.getParcelableExtra(ARG_POST_OPTIONS);


        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (hasReadPermission()) {
            addPickerFragment();
        } else {
            addPermissionsFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionUpdated){
            mPermissionUpdated = false;
            if (hasReadPermission()) {
                addPickerFragment();
            } else {
                addPermissionsFragment();
            }
        }
    }

    private void addPickerFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PickerFragment.newInstance(mGalleryType, mReturnType, mPostOptions)).commit();
    }


    private void addPermissionsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PermissionFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            setResult(RESULT_CANCELED);

        super.onBackPressed();

    }

    private boolean mPermissionUpdated = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_READ_EXT_STORAGE:
                mPermissionUpdated = true;
                break;
        }
    }

    //Permissions
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasReadPermission() {
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public void getReadPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_READ_EXT_STORAGE);
    }
}
