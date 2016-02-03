package com.linute.linute.LoginAndSignup;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.linute.linute.API.Device;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.API.QuickstartPreferences;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.ImageUtils;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
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

/**
 * A login screen that offers login via email/password.
 */
public class LinuteSignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    // SDK
    private LSDKUser mLSDKUser;
    private String mEmailString;
    private String mPinCode;

    //flipper
    private ViewFlipper mViewFlipper;
    private int mCurrentViewFlipperIndex = 0;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPinCodeView;

    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;
    private ProgressBar mProgressBar3;

    private EditText mFirstNameTextView;
    private EditText mLastNameTextView;

    //layer 1
    private Button mNextButton;

    //layer 2
    private View mLayerTwoButtons;

    //layer 3
    private Button mEmailSignUpButton;

    private Bitmap mProfilePictureBitmap;

    private TextView mEmailConfirmTextView;

    private CircularImageView mProfilePictureView;

    private boolean mCredentialCheckInProgress = false; //determine if currently querying database

    private String mCurrentPhotoPath; //the path of photo we take

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linute_sign_up);
        // Set up the login form.

        //create LSDKUser
        mLSDKUser = new LSDKUser(this);

        setUpViewFlipper();

        bindViews();
        setUpOnClickListeners();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("mSavedEmail", mEmailString);
        outState.putString("mSavedPin", mPinCode);
        outState.putInt("mCurrentFlipperIndex", mCurrentViewFlipperIndex);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mPinCode = savedInstanceState.getString("mSavedPin");
            mCurrentViewFlipperIndex = savedInstanceState.getInt("mCurrentFlipperIndex");
            mEmailString = savedInstanceState.getString("mSavedEmail");
            if (mEmailString != null) {
                mEmailView.setText(mEmailString);
                mEmailConfirmTextView.setText(mEmailString);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mViewFlipper.getDisplayedChild() != mCurrentViewFlipperIndex)
            mViewFlipper.setDisplayedChild(mCurrentViewFlipperIndex);
    }

    private void setUpViewFlipper() {
        mViewFlipper = (ViewFlipper) findViewById(R.id.signUp_view_flipper);
        mViewFlipper.setInAnimation(this, R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(this, R.anim.slide_out_left);
    }

    private void bindViews() {
        mEmailView = (EditText) findViewById(R.id.signup_email_text);
        mPasswordView = (EditText) findViewById(R.id.signup_password);
        mFirstNameTextView = (EditText) findViewById(R.id.signup_fname_text);
        mLastNameTextView = (EditText) findViewById(R.id.signup_lname_text);
        mProfilePictureView = (CircularImageView) findViewById(R.id.signup_profile_pic_view);

        mProgressBar1 = (ProgressBar) findViewById(R.id.signUp_progress_bar1);
        mProgressBar2 = (ProgressBar) findViewById(R.id.signUp_progress_bar2);
        mProgressBar3 = (ProgressBar) findViewById(R.id.signUp_progress_bar3);

        mEmailSignUpButton = (Button) findViewById(R.id.signup_submit_button);
        mNextButton = (Button) findViewById(R.id.signUp_verify_email_button);
        mLayerTwoButtons = findViewById(R.id.signUp_code_verify_buttons);

        mPinCodeView = (EditText) findViewById(R.id.signUp_verify_code);
        mFirstNameTextView.setNextFocusDownId(R.id.signup_lname_text);

        mEmailConfirmTextView = (TextView) findViewById(R.id.signUp_email_confirm_text_view);
    }

    private void setUpOnClickListeners() {

        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDeviceRegistered();
            }
        });

        //attempt to sign up when button pressed
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        //when imaged pressed, user can select where to find profile image
        mProfilePictureView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LinuteSignUpActivity.this);
                String[] options = {"Camera", "Photo Gallery", "Cancel"};
                builder.setItems(options, actionListener);
                builder.create().show();
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
                    Crop.pickImage(LinuteSignUpActivity.this);
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        }
    };

    //this is just to double check if the device was registered properly
    private void checkDeviceRegistered() {
        if (mCredentialCheckInProgress) return;

        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        showProgress(true, 0);

        if (sharedPreferences.getBoolean("deviceRegistered", false)) {
            checkEmailAndGetPinCode();
        } else {
            sendRegistrationDevice(sharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, ""));
        }
    }


    private void checkEmailAndGetPinCode() {

        final String email = mEmailView.getText().toString().trim();

        if (checkEmail(email)) {

            mEmailString = email;
            mEmailConfirmTextView.setText(mEmailString);

            mLSDKUser.isUniqueEmail(email, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    failedConnectionWithCurrentView(0);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.code() == 200) { //email was good
                        response.body().string();
                        getPinCode();
                    } else if (response.code() == 404) { //another error
                        Log.e(TAG, response.body().string());
                        notUniqueEmail();
                    } else {
                        Log.e(TAG, "onResponse: " + response.body().string());
                        serverErrorCurrentView(0);
                    }
                }
            });
        } else {
            showProgress(false, 0);
        }
    }

    private void getPinCode() {
        mLSDKUser.getConfirmationCodeForEmail(mEmailString, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                failedConnectionWithCurrentView(0);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String stringResp = response.body().string();
                        mPinCode = (new JSONObject(stringResp).getString("pinCode"));
//                        Log.i(TAG, "onResponse: " + stringResp);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false, 0);
                                mViewFlipper.showNext();
                                mCurrentViewFlipperIndex++;
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        serverErrorCurrentView(0);
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    serverErrorCurrentView(0);
                }
            }
        });
    }


    private boolean checkEmail(String emailString) {
        //if empty return false
        if (TextUtils.isEmpty(emailString)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();
            return false;
        }

        //invalid email
        else if (!emailString.contains("@") || emailString.startsWith("@") || emailString.contains("@.")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            return false;
        }

        /*TODO: uncomment this
        //not edu email
        else if (!emailString.endsWith(".edu")){
            mEmailView.setError("Must be a valid edu email");
            mEmailView.requestFocus();
            return false;
        }
        */

        //good email
        else {
            return true;
        }
    }

    //checks if provided credentials are good
    //marks them if they are invalid credentials
    private boolean areGoodCredentials(String password, String fName, String lName) {
        boolean isGood = true;

        View mFocusView = null;

        // Reset errors.
        mPasswordView.setError(null);
        mFirstNameTextView.setError(null);
        mLastNameTextView.setError(null);

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mFocusView = mPasswordView;
            isGood = false;
        }

        if (TextUtils.isEmpty(lName)) {
            mLastNameTextView.setError(getString(R.string.error_field_required));
            mFocusView = mLastNameTextView;
            isGood = false;
        }

        if (TextUtils.isEmpty(fName)) {
            mFirstNameTextView.setError(getString(R.string.error_field_required));
            mFocusView = mFirstNameTextView;
            isGood = false;
        }

        if (!isGood)
            mFocusView.requestFocus();

        return isGood;
    }

    private boolean isPasswordValid(String password) {
        //longer than 5 and doesn't contain whitespace
        return password.length() >= 6 && !password.contains(" ");
    }

    private void showProgress(final boolean show, final int currentViewIndex) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final View progressBar;
        final View button;

        switch (currentViewIndex) {
            case 0:
                progressBar = mProgressBar1;
                button = mNextButton;
                break;
            case 1:
                progressBar = mProgressBar2;
                button = mLayerTwoButtons;
                break;
            case 2:
                progressBar = mProgressBar3;
                button = mEmailSignUpButton;
                break;
            default:
                return;
        }

        button.setVisibility(show ? View.GONE : View.VISIBLE);
        button.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mCredentialCheckInProgress = show;
    }

    private void signUp() {

        //if alreadying querying, return
        if (mCredentialCheckInProgress) return; //users can't press Sign Up button during the check

        final String email = mEmailString;
        final String password = mPasswordView.getText().toString();
        final String fName = mFirstNameTextView.getText().toString();
        final String lName = mLastNameTextView.getText().toString();

        boolean areGoodCredentials = areGoodCredentials(password, fName, lName);

        if (areGoodCredentials) {
            showProgress(true, 2);
            Map<String, Object> userInfo = new HashMap<>();
            String encodedProfilePicture;

            encodedProfilePicture = Utils.encodeImageBase64(
                    mProfilePictureBitmap != null ? mProfilePictureBitmap :
                            BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture_placeholder));

            //add information
            userInfo.put("email", email);
            userInfo.put("password", password);
            userInfo.put("firstName", fName);
            userInfo.put("lastName", lName);
            userInfo.put("profileImage", encodedProfilePicture);
            userInfo.put("timeZone", Utils.getTimeZone());

            //try to create user
            mLSDKUser.createUser(userInfo, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) { // no response
                    failedConnectionWithCurrentView(2);
                }

                @Override
                public void onResponse(Response response) throws IOException { //get response
                    if (response.isSuccessful()) { //got response

                        try {
                            saveSuccessInformation(response.body().string(), password);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    goToNextActivity();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Counldn't save info");
                            serverErrorCurrentView(2);
                        }

                    } else { //couldn't get response
                        Log.e(TAG, "onResponse: " + response.body().string());
                        serverErrorCurrentView(2);
                    }
                }
            });
        }
    }

    private void saveSuccessInformation(String responseString, String password) throws JSONException {
        JSONObject response = new JSONObject(responseString);
        LinuteUser user = new LinuteUser(response);
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

        sharedPreferences.putString("password", password);
        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
        sharedPreferences.putString("email", user.getEmail());
        sharedPreferences.putString("status", user.getStatus());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("collegeName", user.getCollegeName());
        sharedPreferences.putString("collegeId", user.getCollegeId());

        sharedPreferences.putString("lastLoginEmail", user.getEmail());

        if (user.getSocialFacebook() != null)
            sharedPreferences.putString("socialFacebook", user.getSocialFacebook());

        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();

        Utils.testLog(this, TAG);

    }


    private void goToNextActivity() {
        Intent i = new Intent(this, CollegePickerActivity.class); //need to pick college
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear stack
        startActivity(i); //start new activity
        finish();
    }

    private void serverErrorCurrentView(final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showServerErrorToast(LinuteSignUpActivity.this);
            }
        });


    }

    private void failedConnectionWithCurrentView(final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showBadConnectionToast(LinuteSignUpActivity.this);
            }
        });
    }


    private void notUniqueEmail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmailView.setError(getString(R.string.signup_error_email_taken));
                showProgress(false, 0);
                mEmailView.requestFocus();
            }
        });
    }


    static final int REQUEST_TAKE_PHOTO = 1;

    //Request Permissiosns
    private static final int REQUEST_PERMISSIONS = 17;

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
                } else {
                    showRationalizationDialog();
                }
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
                ImageUtils.normalizeImageForUri(this, imageUri);
                try {
                    //release old pictures resources
                    if (mProfilePictureBitmap != null) mProfilePictureBitmap.recycle();

                    //scale cropped image to 1080 x 1080 (will be sent to database
                    mProfilePictureBitmap = Bitmap.createScaledBitmap(
                            MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri),
                            1080, 1080, false);

                    mProfilePictureView.setImageBitmap(mProfilePictureBitmap);

                    //save mCurrentFilePath
                    mCurrentPhotoPath = imageUri.getPath();
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


    public void requestPermissions() {
        List<String> permissions = new ArrayList<>();
        //check for camera
        if (!hasCameraPermissions()) {
            permissions.add(Manifest.permission.CAMERA);
        }
        //check for write
        if (!hasWritePermission()) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //we need permissions
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    REQUEST_PERMISSIONS);
        } else {
            //we have permissions : show camera
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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


    private boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

    }


    public void enterNewEmail(View view) {
        if (mCredentialCheckInProgress) return;
        setToGoBackAnimation(true); //change animations
        mPinCodeView.setText("");
        mViewFlipper.showPrevious();
        mCurrentViewFlipperIndex--;
        mEmailString = null;
        mPinCode = null;
        setToGoBackAnimation(false);  //change animations
    }

    public void verifyCode(View view) {
        if (mCredentialCheckInProgress) return;

        showProgress(true, 1);

        if (mPinCode != null && mPinCodeView.getText().toString().equals(mPinCode)) {
            mViewFlipper.showNext();
            mCurrentViewFlipperIndex++;
        } else {
            mPinCodeView.setError("Invalid code");
            mPinCodeView.requestFocus();
        }
        showProgress(false, 1);
    }

    private void setToGoBackAnimation(boolean goBack) {

        mViewFlipper.setInAnimation(this, goBack ? R.anim.slide_in_left : R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(this, goBack ? R.anim.slide_out_right : R.anim.slide_out_left);

    }


    //used to registere device if somehow device wasn't registered
    private void sendRegistrationDevice(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String versionName = "";
        String versionCode = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, Object> device = new HashMap<>();
        device.put("token", token);
        device.put("version", versionName);
        device.put("build", versionCode);
        device.put("os", Build.VERSION.SDK_INT + "");
        device.put("type", "android");

        Device.createDevice(headers, device, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "failed registration");
                failedConnectionWithCurrentView(0);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, response.body().string());
                    serverErrorCurrentView(0);
                } else {
                    Log.v(TAG, response.body().string());
                    getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean("deviceRegistered", true)
                            .apply();
                    checkEmailAndGetPinCode();
                }
            }
        });

    }
}

