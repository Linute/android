package com.linute.linute.MainContent.Settings;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.ImageUtils;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.soundcloud.android.crop.Crop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangeProfileImageFragment extends Fragment {

    public static final String TAG = "ChangeProfileImage";

    private boolean mHasChangedImage = false; //won't allow send unless user actually makes a change

    private TextView mEditButton;
    private TextView mSaveButton;
    private View mButtonLayer;

    private CircularImageView mImageView;

    private SharedPreferences mSharedPreferences;

    private ProgressBar mProgressBar;

    private Bitmap mProfilePictureBitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change_profile_image, container, false);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        bindViews(rootView);
        setDefaultValues();
        setUpOnClickListeners();

        ((EditProfileInfoActivity)getActivity()).setTitle("Photo");

        return rootView;
    }

    private void bindViews(View rootView) {
        mSaveButton = (TextView) rootView.findViewById(R.id.changeprofileimage_save_button);
        mEditButton = (TextView) rootView.findViewById(R.id.changeprofileimage_change_button);
        mButtonLayer = rootView.findViewById(R.id.changeprofileimage_buttons);

        mImageView = (CircularImageView) rootView.findViewById(R.id.changeprofileimage_image);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.changeprofileimage_progressbar);
    }

    private void setUpOnClickListeners() {
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Image Source");
                String[] options = {"Camera", "Photo Gallery", "Cancel"};
                builder.setItems(options, actionListener);
                builder.create().show();
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
    }


    //select between camera and photogallery
    DialogInterface.OnClickListener actionListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    //go to camera
                    requestPermissions();
                    break;
                case 1:
                    //go to gallery
                    Crop.pickImage(getActivity(), ChangeProfileImageFragment.this);
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }
    };


    private void setDefaultValues() {
        Glide.with(this)
                .load(Utils.getImageUrlOfUser(mSharedPreferences.getString("profileImage", "")))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(mImageView);
    }


    private void saveImage() {
        if (!mHasChangedImage) return; //no edits to image
        LSDKUser user = new LSDKUser(getActivity());
        showProgress(true);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("profileImage", Utils.encodeImageBase64(mProfilePictureBitmap));

        user.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        LinuteUser user = new LinuteUser(new JSONObject(response.body().string()));
                        persistData(user);

                        mHasChangedImage = false;

                        final EditProfileInfoActivity activity = (EditProfileInfoActivity) getActivity();
                        if (activity == null) return;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(activity);
                                showProgress(false);

                                activity.setMainActivityNeedsToUpdate(true);
                                getFragmentManager().popBackStack();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                showProgress(false);
                            }
                        });
                    }
                } else {
                    Log.v(TAG, response.body().string());
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getActivity());
                            showProgress(false);
                        }
                    });
                }
            }
        });
    }


    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCurrentPhotoPath == null) Log.i(TAG, "onActivityResult: 123"); //return;

        if (requestCode == REQUEST_TAKE_PHOTO) { //got response from camera
            if (mCurrentPhotoPath == null) return; //NOTE: added this

            if (!hasWritePermission() && !hasCameraPermissions()){
                if (mCurrentPhotoPath != null){
                    new File(mCurrentPhotoPath).delete();
                    mCurrentPhotoPath = null;
                }
                return;
            }

            if (resultCode == Activity.RESULT_OK) {  //was able to get picture
                File f = new File(mCurrentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                galleryAddPic(contentUri); // add to gallery
                beginCrop(contentUri); //crop image
            }
             else { //no picture captured. delete the temp file created to hold image
                if (!new File(mCurrentPhotoPath).delete())
                    Log.v(TAG, "could not delete temp file");
                mCurrentPhotoPath = null;
            }
        } else if (requestCode == Crop.REQUEST_PICK) { //got image from gallery
            if (resultCode == Activity.RESULT_OK)
                beginCrop(data.getData()); //crop image
        } else if (requestCode == Crop.REQUEST_CROP) { //photo came back from crop
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = Crop.getOutput(data);
                ImageUtils.normalizeImageForUri(getActivity(), imageUri);
                try {
                    //release old pictures resources
                    if (mProfilePictureBitmap != null) mProfilePictureBitmap.recycle();


                    //scale cropped image to 1080 x 1080 (will be sent to database
                    mProfilePictureBitmap = Bitmap.createScaledBitmap(
                            MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri),
                            1080, 1080, false);

                    mImageView.setImageBitmap(mProfilePictureBitmap);
                    mHasChangedImage = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == Crop.RESULT_ERROR) { //error cropping, show error
                Toast.makeText(getActivity(), Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void beginCrop(Uri source) { //begin crop activity
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(getActivity(), ChangeProfileImageFragment.this);
    }


    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("profileImage", user.getProfileImage()).apply();
        mSharedPreferences.edit().putString("imageSigniture", new Random().nextInt() + "").apply();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
        mButtonLayer.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        setFocusable(!show);
    }

    private void setFocusable(boolean focusable) {
        mImageView.setClickable(focusable);
    }


    //Request Permissiosns
    private static final int REQUEST_PERMISSIONS = 10;

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(){
        List<String> permissions = new ArrayList<>();
        //check for camera
        if (!hasCameraPermissions()){
            permissions.add(Manifest.permission.CAMERA);
        }
        //check for write
        if (!hasWritePermission()){
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //we need permissions
        if (!permissions.isEmpty()){
           requestPermissions(permissions.toArray(new String[permissions.size()]),
                   REQUEST_PERMISSIONS);
        }else {
            //we have permissions : show camera
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: ");
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                for (int result : grantResults) // if we didn't get approved for a permission, show permission needed frag
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        showRationalizationDialog();
                        return;
                    }
                dispatchTakePictureIntent();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showRationalizationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Allow Tapt to Use your phone's storage?")
                .setMessage("Tapt needs access to your phone's camera and storage to take and save images.")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions();
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Couldn't create image path.");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //create folder for our pictures
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Tapt");

        if (!storageDir.exists()) storageDir.mkdir();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    //makes picture available to other gallery and other apps
    private void galleryAddPic(Uri contentUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }


    private boolean hasWritePermission(){
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermissions(){
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

    }
}

