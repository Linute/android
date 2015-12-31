package com.linute.linute.MainContent.Settings;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.SquareImageView;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.soundcloud.android.crop.Crop;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

public class ChangeProfileImageActivity extends AppCompatActivity {

    public static final String TAG = "ChangeProfileImage";

    private boolean mHasSavedImage = false; //if image was successfully saved to DB, update profile frag

    private boolean mHasChangedImage = false; //won't allow send unless user actually makes a change

    private Button mSaveButton;
    private Button mCancelButton;
    private View mButtonLayer;

    private SquareImageView mImageView;

    private SharedPreferences mSharedPreferences;

    private ProgressBar mProgressBar;

    private Bitmap mProfilePictureBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_image);
        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        bindViews();

        if (!Utils.isNetworkAvailable(this)) Utils.showBadConnectionToast(this);
        setDefaultValues();

        setUpOnClickListeners();
    }


    private void bindViews() {
        mSaveButton = (Button) findViewById(R.id.changeprofileimage_save_button);
        mCancelButton = (Button) findViewById(R.id.changeprofileimage_cancel_button);
        mButtonLayer = findViewById(R.id.changeprofileimage_buttons);

        mImageView = (SquareImageView) findViewById(R.id.changeprofileimage_image);

        mProgressBar = (ProgressBar) findViewById(R.id.changeprofileimage_progressbar);
    }

    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(0, 0);
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ChangeProfileImageActivity.this);
                builder.setTitle("Image Source");
                String[] options = {"Camera", "Photo Gallery", "Cancel"};
                builder.setItems(options, actionListener);
                builder.create().show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    Crop.pickImage(ChangeProfileImageActivity.this);
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (mProgressBar.getVisibility() == View.GONE) {
            if (mHasSavedImage) setResult(RESULT_OK); //tell parent to update
            else setResult(RESULT_CANCELED); //tell parent not to update
            super.onBackPressed();
        }
    }



    private void setDefaultValues() {

        Glide.with(this)
                .load(Utils.getImageUrlOfUser(mSharedPreferences.getString("profileImage", "")))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(mImageView);
    }


    private void saveImage() {
        if (!mHasChangedImage) return; //no edits to image
        LSDKUser user = new LSDKUser(this);
        showProgress(true);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("profileImage", Utils.encodeImageBase64(mProfilePictureBitmap));

        user.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(ChangeProfileImageActivity.this);
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        LinuteUser user = new LinuteUser(new JSONObject(response.body().string()));
                        persistData(user);
                        mHasSavedImage = true;
                        mHasChangedImage = false;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(ChangeProfileImageActivity.this);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(ChangeProfileImageActivity.this);
                            }
                        });
                    }
                } else {
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(ChangeProfileImageActivity.this);
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
            }
        });
    }


    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO) { //got response from camera
            if (resultCode == RESULT_OK) {  //was able to get picture
                if (hasWritePermission()) {
                    File f = new File(mCurrentPhotoPath);
                    Uri contentUri = Uri.fromFile(f);
                    galleryAddPic(contentUri); // add to gallery
                    beginCrop(contentUri); //crop image
                }else
                    showRationalizationDialog();
            } else { //no picture captured. delete the temp file created to hold image
                if (!new File(mCurrentPhotoPath).delete())
                    Log.v(TAG, "could not delete temp file");
                mCurrentPhotoPath = null;
            }
        } else if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) { //got image from gallery
            beginCrop(data.getData()); //crop image
        } else if (requestCode == Crop.REQUEST_CROP) { //photo came back from crop
            if (resultCode == RESULT_OK) {
                Uri imageUri = Crop.getOutput(data);
                try {
                    //release old pictures resources
                    if (mProfilePictureBitmap != null) mProfilePictureBitmap.recycle();

                    //scale cropped image to 1080 x 1080 (will be sent to database
                    mProfilePictureBitmap = Bitmap.createScaledBitmap(
                            MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri),
                            1080, 1080, false);

                    mImageView.setImageBitmap(mProfilePictureBitmap);
                    mHasChangedImage = true;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == Crop.RESULT_ERROR) { //error cropping, show error
                Toast.makeText(this, Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void beginCrop(Uri source) { //begin crop activity
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
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
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSIONS);
        }else {
            //we have permissions : show camera
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        new AlertDialog.Builder(this)
                .setTitle("Allow Woohoo to Use your phone's storage?")
                .setMessage("Woohoo needs access to your phone's camera and storage to take and save images.")
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
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
                Environment.DIRECTORY_PICTURES), "Linute");

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
        this.sendBroadcast(mediaScanIntent);
    }


    private boolean hasWritePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermissions(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

    }
}

