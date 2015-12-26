package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.soundcloud.android.crop.Crop;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LinuteSignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    // SDK
    private LSDKUser mLSDKUser;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private EditText mFirstNameTextView;
    private EditText mLastNameTextView;
    private Button mEmailSignUpButton;
    private Bitmap mProfilePictureBitmap;

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

        bindViews();
        setUpOnClickListeners();

    }

    private void bindViews() {
        mEmailView = (EditText) findViewById(R.id.signup_email_text);
        mPasswordView = (EditText) findViewById(R.id.signup_password);
        mFirstNameTextView = (EditText) findViewById(R.id.signup_fname_text);
        mLastNameTextView = (EditText) findViewById(R.id.signup_lname_text);
        mProfilePictureView = (CircularImageView) findViewById(R.id.signup_profile_pic_view);
        mProgressBar = (ProgressBar) findViewById(R.id.signup_progress_bar);
        mEmailSignUpButton = (Button) findViewById(R.id.signup_submit_button);

        mFirstNameTextView.setNextFocusDownId(R.id.signup_lname_text);
    }

    private void setUpOnClickListeners() {
        //attempt to sign up when button pressed
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        //when imaged pressed, user can select where to find profile image
        mProfilePictureView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LinuteSignUpActivity.this);
                builder.setTitle("Image Source");
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
                    dispatchTakePictureIntent();
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


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {
        //if alreadying querying, return
        if (mCredentialCheckInProgress) { //users can't press Sign Up button during the check
            return;
        }

        if (!Utils.isNetworkAvailable(this)) { //if no network connection
            Utils.showBadConnectionToast(this);
            return;
        }

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String fName = mFirstNameTextView.getText().toString();
        final String lName = mLastNameTextView.getText().toString();

        boolean areGoodCredentials = areGoodCredentials(email, password, fName, lName);

        if (areGoodCredentials) {
            //check email and sign up
            showProgress(true);
            //check if email unique
            mLSDKUser.isUniqueEmail(email, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(rFailedConnectionAction);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.code() == 200) {
                        signUp(email, password, fName, lName);
                    } else if(response.code() == 404) { //another error
                        Log.e(TAG, response.body().string());
                        runOnUiThread(rNotUniqueEmailAction);
                    }else {
                        runOnUiThread(rServerErrorAction);
                    }
                }
            });
        }
    }

    //checks if provided credentials are good
    //marks them if they are invalid credentials
    private boolean areGoodCredentials(String email, String password, String fName, String lName) {
        boolean isGood = true;

        View mFocusView = null;

        // Reset errors.
        mEmailView.setError(null);
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


        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mFocusView = mEmailView;
            isGood = false;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mFocusView = mEmailView;
            isGood = false;
        }

        if (!isGood)
            mFocusView.requestFocus();

        return isGood;
    }

    //TODO: ADD ERROR FOR NON EDU EMAIL
    private boolean isEmailValid(String email) {
        // no @                     //not edu email             //@cuny.edu
        if (!email.contains("@") || !email.endsWith(".edu") || email.startsWith("@") ||
                email.contains("@.") || email.contains(" "))
            //me@.edu                   //whitespace
            return false;
        return true;
    }

    private boolean isPasswordValid(String password) {
        //longer than 5 and doesn't contain whitespace
        return password.length() >= 6 && !password.contains(" ");
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mEmailSignUpButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mEmailSignUpButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mEmailSignUpButton.setVisibility(show ? View.GONE : View.VISIBLE);
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

        setTextEditsFocus(!show); //when progess shown, don't focus this
        mCredentialCheckInProgress = show;
    }

    //make edit text editable and uneditable
    //don't want people changing values after hitting submit
    private void setTextEditsFocus(boolean focus) {
        //making content focusable uses different functions for some reason
        if (focus) { //turn on
            mEmailView.setFocusableInTouchMode(true);
            mPasswordView.setFocusableInTouchMode(true);
            mFirstNameTextView.setFocusableInTouchMode(true);
            mLastNameTextView.setFocusableInTouchMode(true);
        } else { //turn off
            mEmailView.setFocusable(false);
            mPasswordView.setFocusable(false);
            mFirstNameTextView.setFocusable(false);
            mLastNameTextView.setFocusable(false);
        }
        mProfilePictureView.setClickable(focus);
    }

    private void signUp(String email, final String password, String fName, String lName) {

        Map<String, String> userInfo = new HashMap<>();
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
                runOnUiThread(rFailedConnectionAction);
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
                        runOnUiThread(rServerErrorAction);
                    }

                } else { //couldn't get response
                    runOnUiThread(rServerErrorAction);
                }
            }
        });

        Log.v(TAG, "success");
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

        if (user.getSocialFacebook() != null)
            sharedPreferences.putString("socialFacebook", user.getSocialFacebook());

        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();

        Utils.testLog(this, TAG);

    }


    //confirm email code activity?
    private void goToNextActivity() {

        //FIXME: maybe confirm email first?

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear stack
        startActivity(i); //start new activty
        finish();
    }

    private Runnable rServerErrorAction = new Runnable() {
        @Override
        public void run() {
            showProgress(false);
            Utils.showServerErrorToast(LinuteSignUpActivity.this);
        }
    };

    //hides progress bar and shows a bad connection Toast
    private Runnable rFailedConnectionAction = new Runnable() {
        @Override
        public void run() {
            showProgress(false);
            Utils.showBadConnectionToast(LinuteSignUpActivity.this);
        }
    };

    //entered email was not unque. run action
    private Runnable rNotUniqueEmailAction = new Runnable() {
        @Override
        public void run() {
            mEmailView.setError(getString(R.string.signup_error_email_taken));
            showProgress(false);
            mEmailView.requestFocus();
        }
    };


    static final int REQUEST_TAKE_PHOTO = 1;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO) { //got response from camera
            if (resultCode == RESULT_OK) {  //was able to get picture
                File f = new File(mCurrentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                galleryAddPic(contentUri); // add to gallery
                beginCrop(contentUri); //crop image
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

                    mProfilePictureView.setImageBitmap(mProfilePictureBitmap);

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

}

