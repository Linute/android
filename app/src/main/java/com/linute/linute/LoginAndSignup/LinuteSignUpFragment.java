package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.ProfileCamera.ProfileCameraActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.WebViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LinuteSignUpFragment extends Fragment {

    public static final String TAG = "SignUpActivity";

    // SDK
    private String mEmailString;
    private String mPinCode;

    //flipper
    private ViewFlipper mViewFlipper;
    private int mCurrentViewFlipperIndex = 0;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPinCodeView;

    private View mProgressBar1;
    private View mProgressBar2;
    private View mProgressBar3;

    private EditText mFirstNameTextView;
    private EditText mLastNameTextView;

    private View mSubmitButton;
    private View mVerifyLayer;
    private View mResendButton;

    private TextView mEmailConfirmTextView;

    private CircleImageView mProfilePictureView;

    private boolean mCredentialCheckInProgress = false; //determine if currently querying database

    private Uri mImageUri;


    public LinuteSignUpFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_linute_sign_up, container, false);


        setUpViewFlipper(rootView);
        bindViews(rootView);

        setUpOnClickListeners();

        rootView.findViewById(R.id.signUp_back_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewFlipper.getDisplayedChild() == 1) {
                    setToGoBackAnimation(true);
                    mViewFlipper.showPrevious();
                    mCurrentViewFlipperIndex--;
                    setToGoBackAnimation(false);  //change animations
                } else {
                    getFragmentManager().popBackStack();
                }
            }
        });

        rootView.findViewById(R.id.create_privacy_policy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.LOAD_URL, "https://www.tapt.io/privacy-policy");
                startActivity(intent);
            }
        });

        rootView.findViewById(R.id.create_terms_of_services).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.LOAD_URL, "https://www.tapt.io/terms-of-service");
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("mSavedEmail", mEmailString);
        outState.putString("mSavedPin", mPinCode);
        outState.putInt("mCurrentFlipperIndex", mCurrentViewFlipperIndex);
        outState.putParcelable("image", mImageUri);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mPinCode = savedInstanceState.getString("mSavedPin");
            mCurrentViewFlipperIndex = savedInstanceState.getInt("mCurrentFlipperIndex");
            mEmailString = savedInstanceState.getString("mSavedEmail");
            if (mEmailString != null) {
                mEmailView.setText(mEmailString);
                mEmailConfirmTextView.setText(mEmailString);
            }
            mImageUri = savedInstanceState.getParcelable("image");
        }

        if (mImageUri != null) {
            mProfilePictureView.setImageURI(mImageUri);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mViewFlipper.getDisplayedChild() != mCurrentViewFlipperIndex)
            mViewFlipper.setDisplayedChild(mCurrentViewFlipperIndex);
    }

    private void setUpViewFlipper(View root) {
        mViewFlipper = (ViewFlipper) root.findViewById(R.id.signUp_view_flipper);
        mViewFlipper.setInAnimation(getActivity(), R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(getActivity(), R.anim.slide_out_left);
    }

    private void bindViews(View root) {
        mEmailView = (EditText) root.findViewById(R.id.signup_email_text);
        mPasswordView = (EditText) root.findViewById(R.id.signup_password);
        mFirstNameTextView = (EditText) root.findViewById(R.id.signup_fname_text);
        mLastNameTextView = (EditText) root.findViewById(R.id.signup_lname_text);
        mProfilePictureView = (CircleImageView) root.findViewById(R.id.signup_profile_pic_view);

        mProgressBar1 = root.findViewById(R.id.signUp_progress_bar1);
        mProgressBar2 = root.findViewById(R.id.signUp_progress_bar2);

//        mEmailSignUpButton = root.findViewById(R.id.signup_get_verify_code_button);
//        mGetPinCodeButton = root.findViewById(R.id.signUp_submit_butt);

        mPinCodeView = (EditText) root.findViewById(R.id.signUp_verify_code);
        mFirstNameTextView.setNextFocusDownId(R.id.signup_lname_text);

        mResendButton = root.findViewById(R.id.resend);
        mProgressBar3 = root.findViewById(R.id.resend_progress);

        mSubmitButton = root.findViewById(R.id.signUp_submit_butt);
        mVerifyLayer = root.findViewById(R.id.signup_verify_layer);

        mEmailConfirmTextView = (TextView) root.findViewById(R.id.signUp_email_confirm_text_view);
    }

    private void setUpOnClickListeners() {

        mVerifyLayer.findViewById(R.id.signup_get_verify_code_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailAndGetPinCode();
            }
        });

        //attempt to sign up when button pressed
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode();
            }
        });

        //when imaged pressed, user can select where to find profile image
        mProfilePictureView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String[] options = {"Camera", "Photo Gallery", "Cancel"};
                builder.setItems(options, actionListener);
                builder.create().show();
            }
        });

        mResendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resendPincode();
            }
        });
    }

    DialogInterface.OnClickListener actionListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            int type;
            int request;
            switch (which) {
                case 0:
                    type = ProfileCameraActivity.TYPE_CAMERA;
                    request = REQUEST_TAKE_PHOTO;
                    break;
                case 1:
                    type = ProfileCameraActivity.TYPE_GALLERY;
                    request = REQUEST_GALLERY;
                    break;
                default:
                    return;
            }

            Intent i = new Intent(getActivity(), ProfileCameraActivity.class);
            i.putExtra(ProfileCameraActivity.TYPE_KEY, type);
            startActivityForResult(i, request);
        }
    };


    private void checkEmailAndGetPinCode() {

        final String email = mEmailView.getText().toString().trim().toLowerCase();

        final String password = mPasswordView.getText().toString();
        final String fName = mFirstNameTextView.getText().toString().trim();
        final String lName = mLastNameTextView.getText().toString().trim();

        showProgress(true, 0);

        if (checkEmail(email) && areGoodCredentials(password, fName, lName)) {

            mEmailString = email;
            mEmailConfirmTextView.setText(mEmailString);

            if (getActivity() == null) return;

            new LSDKUser(getActivity()).isUniqueEmail(email, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    failedConnectionWithCurrentView(0);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
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
        if (getActivity() == null) return;

        final String fName = mFirstNameTextView.getText().toString().trim();
        final String lName = mLastNameTextView.getText().toString().trim();
        new LSDKUser(getActivity()).getConfirmationCodeForEmail(mEmailString, fName, lName, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                failedConnectionWithCurrentView(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String stringResp = response.body().string();
                        mPinCode = (new JSONObject(stringResp).getString("pinCode"));
                        Log.i(TAG, "onResponse: " + stringResp);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
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

    private void resendPincode() {
        if (getActivity() == null) return;

        final String fName = mFirstNameTextView.getText().toString().trim();
        final String lName = mLastNameTextView.getText().toString().trim();
        mResendButton.setVisibility(View.GONE);
        mProgressBar3.setVisibility(View.VISIBLE);
        new LSDKUser(getActivity()).getConfirmationCodeForEmail(mEmailString, fName, lName, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResendButton.setVisibility(View.VISIBLE);
                            mProgressBar3.setVisibility(View.GONE);
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String stringResp = response.body().string();
                        mPinCode = (new JSONObject(stringResp).getString("pinCode"));
                        //Log.i(TAG, "onResponse: " + stringResp);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResendButton.setVisibility(View.VISIBLE);
                                mProgressBar3.setVisibility(View.GONE);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResendButton.setVisibility(View.VISIBLE);
                                    mProgressBar3.setVisibility(View.GONE);
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mResendButton.setVisibility(View.VISIBLE);
                                mProgressBar3.setVisibility(View.GONE);
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
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
        } else if (!emailString.endsWith(".edu")) {
            mEmailView.setError("Must be a valid edu email");
            mEmailView.requestFocus();
            return false;
        }

        //good email
        else {
            return true;
        }
    }

    //checks if provided credentials are good
    //marks them if they are invalid credentials
    private boolean areGoodCredentials(String password, String fName, String lName) {
        boolean isGood = true;

        // Reset errors.
        mPasswordView.setError(null);
        mFirstNameTextView.setError(null);
        mLastNameTextView.setError(null);
        View need = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            need = mPasswordView;
            isGood = false;
        }

        if (TextUtils.isEmpty(lName)) {
            mLastNameTextView.setError(getString(R.string.error_field_required));
            need = mLastNameTextView;
            isGood = false;
        }

        if (TextUtils.isEmpty(fName)) {
            mFirstNameTextView.setError(getString(R.string.error_field_required));
            need = mFirstNameTextView;
            isGood = false;
        }

        if (need != null) need.requestFocus();
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
                button = mVerifyLayer;
                break;
            case 1:
                progressBar = mProgressBar2;
                button = mSubmitButton;
                break;
            default:
                return;
        }

        button.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        button.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
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


    public void verifyCode() {
        if (mCredentialCheckInProgress) return;
        showProgress(true, 1);

        if (mPinCode != null && mPinCodeView.getText().toString().equals(mPinCode)) {
            signUp();
        } else {
            mPinCodeView.setError("Invalid code");
            showProgress(false, 1);
        }
    }


    private void signUp() {

        //if alreadying querying, return

        final String email = mEmailString;
        final String password = mPasswordView.getText().toString();
        final String fName = mFirstNameTextView.getText().toString().trim();
        final String lName = mLastNameTextView.getText().toString().trim();

        boolean areGoodCredentials = areGoodCredentials(password, fName, lName);

        if (areGoodCredentials) {
            Map<String, Object> userInfo = new HashMap<>();
            //add information
            userInfo.put("email", email);
            userInfo.put("password", password);
            userInfo.put("firstName", fName);
            userInfo.put("lastName", lName);

            if (mImageUri != null) {
                try {
                    userInfo.put("profileImage", Utils.encodeImageBase64(Bitmap.createScaledBitmap(
                            MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageUri),
                            1080, 1080, false)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            userInfo.put("timeZone", Utils.getTimeZone());

            //try to create user
            if (getActivity() == null) return;
            new LSDKUser(getActivity()).createUser(userInfo, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { // no response
                    failedConnectionWithCurrentView(1);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException { //get response
                    if (response.isSuccessful()) { //got response

                        try {
                            String res = response.body().string();
                            Log.i(TAG, "onResponse: " + res);
                            saveSuccessInformation(res);
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    PreLoginActivity activity = (PreLoginActivity) getActivity();
                                    if (activity != null) {
                                        activity.goToNextActivity();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Counldn't save info");
                            serverErrorCurrentView(1);
                        }

                    } else { //couldn't get response
                        Log.e(TAG, "onResponse: " + response.body().string());
                        serverErrorCurrentView(1);
                    }
                }
            });
        } else {
            showProgress(false, 1);
            mViewFlipper.showPrevious();
            mCurrentViewFlipperIndex--;
        }
    }

    private void saveSuccessInformation(String responseString) throws JSONException {
        JSONObject response = new JSONObject(responseString);
        LinuteUser user = new LinuteUser(response);
        if (getActivity() == null) return;
        SharedPreferences.Editor sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
        sharedPreferences.putString("status", user.getStatus());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("collegeName", user.getCollegeName());
        sharedPreferences.putString("collegeId", user.getCollegeId());

        sharedPreferences.putString("lastLoginEmail", user.getEmail());
        sharedPreferences.putString("email", user.getEmail());

        if (user.getSocialFacebook() != null)
            sharedPreferences.putString("socialFacebook", user.getSocialFacebook());

        sharedPreferences.putString("userToken", user.getUserToken());
        sharedPreferences.putString("userName", user.getUserName());
        sharedPreferences.putString("points", user.getPoints());

        sharedPreferences.putBoolean("isLoggedIn", true);

        sharedPreferences.putBoolean("notif_follow", true);
        sharedPreferences.putBoolean("notif_message", true);
        sharedPreferences.putBoolean("notif_mention", true);
        sharedPreferences.putBoolean("notif_alsoComment", true);
        sharedPreferences.putBoolean("notif_comment", true);
        sharedPreferences.putBoolean("notif_like", true);

        sharedPreferences.apply();

        Utils.testLog(getActivity(), TAG);
    }

    private void serverErrorCurrentView(final int index) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showServerErrorToast(getActivity());
            }
        });


    }

    private void failedConnectionWithCurrentView(final int index) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showBadConnectionToast(getActivity());
            }
        });
    }


    private void notUniqueEmail() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEmailView.setError(getString(R.string.signup_error_email_taken));
                mEmailView.requestFocus();
                showProgress(false, 0);
            }
        });
    }


    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GALLERY = 3;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_GALLERY) && resultCode == Activity.RESULT_OK) { //photo came back from crop
            mImageUri = data.getData();
            if (getActivity() == null || mImageUri == null) return;
            mProfilePictureView.setImageURI(mImageUri);
        }
    }

    private void setToGoBackAnimation(boolean goBack) {
        if (getActivity() == null) return;
        mViewFlipper.setInAnimation(getActivity(), goBack ? R.anim.slide_in_left : R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(getActivity(), goBack ? R.anim.slide_out_right : R.anim.slide_out_left);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            View focusedView = getActivity().getCurrentFocus();
            if (focusedView instanceof EditText) {
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }
}

