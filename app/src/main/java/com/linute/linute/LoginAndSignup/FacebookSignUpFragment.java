package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by QiFeng on 1/14/16.
 */
public class FacebookSignUpFragment extends Fragment {
    public static final String TAG = FacebookSignUpFragment.class.getSimpleName();

    private String mPinCode;
    private String mProfilePath;
    private boolean mInVerificationView = false;

    private SharedPreferences mSharedPreferences;

    private View mLayer1Buttons;
    private View mLayer2Buttons; //layer 2

    private TextView mEmailConfirmText;

    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mVerificationCodeEditText;
    private EditText mEmailEditText;

    private CircularImageView mProfileImage;

    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;

    private ViewSwitcher mViewSwitcher;

    private boolean mCheckInProgress = false;

    private boolean mImageIsFBLink;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_facebook_signup, container, false);


        setUpViewSwitcher(rootView);
        bindViews(rootView);

        if(getActivity() != null) {
            mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, Context.MODE_PRIVATE);

            mFirstNameEditText.append(mSharedPreferences.getString("firstName", ""));
            mLastNameEditText.append(mSharedPreferences.getString("lastName", ""));
            String image = mSharedPreferences.getString("profileImage", "");

            if (image.contains("facebook.com")) {
                mProfilePath = image;
                mImageIsFBLink = true;
            } else {
                mProfilePath = Utils.getImageUrlOfUser(image);
                mImageIsFBLink = false;
            }
        }

        rootView.findViewById(R.id.fbSignUp_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewSwitcher.getDisplayedChild() != 0){
                    goBackAnimation(true);
                    mViewSwitcher.showPrevious();
                    goBackAnimation(false);
                }else {
                    getFragmentManager().popBackStack();
                }
            }
        });

        loadProfileImage();
        setOnClickListener();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("firstName", mFirstNameEditText.getText().toString());
        outState.putString("lastName", mLastNameEditText.getText().toString());
        outState.putString("newEmail", mEmailEditText.getText().toString());
        outState.putString("profileImage", mProfilePath);

        outState.putString("pinCode", mPinCode);
        outState.putBoolean("inVerify", mInVerificationView);
        outState.putBoolean("isFBImage", mImageIsFBLink);

        super.onSaveInstanceState(outState);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mFirstNameEditText.setText(savedInstanceState.getString("firstName"));
            mLastNameEditText.setText(savedInstanceState.getString("lastName"));
            mEmailEditText.setText(savedInstanceState.getString("newEmail"));
            mEmailConfirmText.setText(mEmailEditText.getText().toString());
            mProfilePath = savedInstanceState.getString("profileImage");
            mPinCode = savedInstanceState.getString("pinCode");
            mInVerificationView = savedInstanceState.getBoolean("inVerify");
            mImageIsFBLink = savedInstanceState.getBoolean("isFBImage");

            loadProfileImage();
        }
    }


    private void setUpViewSwitcher(View root) {
        mViewSwitcher = (ViewSwitcher) root.findViewById(R.id.fbSignUp_view_switcher);
        goBackAnimation(false); //slide in right and out left
    }

    private void bindViews(View root) {

        mLayer1Buttons = root.findViewById(R.id.fb_signup_button_layer);
        mLayer2Buttons = root.findViewById(R.id.fbSignUp_code_verify_buttons);

        mEmailConfirmText = (TextView) root.findViewById(R.id.fbSignUp_email_confirm_text_view);

        mFirstNameEditText = (EditText) root.findViewById(R.id.fbSignUp_fname_text);
        mFirstNameEditText.setNextFocusDownId(R.id.fbSignUp_lname_text);

        mLastNameEditText = (EditText) root.findViewById(R.id.fbSignUp_lname_text);
        mVerificationCodeEditText = (EditText) root.findViewById(R.id.signUp_verify_code);
        mEmailEditText = (EditText) root.findViewById(R.id.fbSignUp_email);

        mProfileImage = (CircularImageView) root.findViewById(R.id.fbSignUp_profile_pic_view);
        mProgressBar1 = (ProgressBar) root.findViewById(R.id.fbSignUp_progress_bar1);
        mProgressBar2 = (ProgressBar) root.findViewById(R.id.fbSignUp_progress_bar2);

        mLayer2Buttons = root.findViewById(R.id.fbSignUp_code_verify_buttons);
    }

    private void setOnClickListener() {
        mLayer2Buttons.findViewById(R.id.fbSignUp_update_info_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInformation();
            }
        });

        mLayer1Buttons.findViewById(R.id.fbSignUp_next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailAndGetPinCode();
            }
        });
    }


    private void updateInformation() {

        if (mCheckInProgress) return;

        if (!mPinCode.equals(mVerificationCodeEditText.getText().toString())) {
            mVerificationCodeEditText.setError("Invalid code");
            mVerificationCodeEditText.requestFocus();
            return;
        }


        final Map<String, Object> newInfo = new HashMap<>();
        newInfo.put("firstName", mFirstNameEditText.getText().toString());
        newInfo.put("lastName", mLastNameEditText.getText().toString());
        newInfo.put("email", mEmailEditText.getText().toString().trim());
        newInfo.put("socialFacebook", mSharedPreferences.getString("socialFacebook", ""));
        newInfo.put("sex", mSharedPreferences.getInt("sex", 0));

        String dob = mSharedPreferences.getString("dob", "");
        if (!dob.equals("") && !dob.equals("null")){
            newInfo.put("dob", dob);
        }
        newInfo.put("registrationType", mSharedPreferences.getString("registrationType", ""));
        newInfo.put("passwordFacebook", mSharedPreferences.getString("passwordFacebook", ""));
        newInfo.put("password", mSharedPreferences.getString("password", ""));


        showProgress(true, 1);

        if (mImageIsFBLink) {
            Glide.with(this)
                    .load(mProfilePath)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(1080, 1080) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            newInfo.put("profileImage", Utils.encodeImageBase64(resource));
                            update(newInfo);
                        }
                    });
        } else {
            update(newInfo);
        }

//        new LSDKUser(this).updateUserInfo(newInfo, mSharedPreferences.getString("email", ""), new Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//                failedInternetConnection(1);
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    try {
//                        String responseString = response.body().string();
//                        Log.i(TAG, "onResponse: " + responseString);
//                        persistData(new LinuteUser(new JSONObject(responseString))); //save data
//
//                        goToCollegePicker();
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        serverError(1);
//                    }
//
//
//                } else {
//                    serverError(1);
//                    Log.e(TAG, "onResponse: " + response.body().string());
//                }
//            }
//        });

    }

    private void update(Map<String, Object> params) {

        if (getActivity() == null) return;
        new LSDKUser(getActivity()).createUser(params, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                failedInternetConnection(1);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body().string();
                        Log.i(TAG, "onResponse: " + responseString);
                        persistData(new LinuteUser(new JSONObject(responseString))); //save data
                        PreLoginActivity activity = (PreLoginActivity) getActivity();
                        if (activity != null){
                            activity.goToNextActivity();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        serverError(1);
                    }


                } else {
                    serverError(1);
                    Log.e(TAG, "onResponse: " + response.body().string());
                }
            }
        });
    }


    private void checkEmailAndGetPinCode() {

        if (mCheckInProgress) return;

        if (!checkNames()) return;

        final String email = mEmailEditText.getText().toString().trim().toLowerCase();

        if (checkEmail(email)) {
            showProgress(true, 0);
            mEmailConfirmText.setText(email);

            if (getActivity() == null) return;

            new LSDKUser(getActivity()).isUniqueEmail(email, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    failedInternetConnection(0);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String reponseString = response.body().string();
                    if (response.code() == 200) { //email was good
                        getPinCode(email);
                    } else if (response.code() == 404) { //another error
                        Log.e(TAG, reponseString);
                        nonUniqueEmail();
                    } else {
                        Log.e(TAG, "onResponse: " + reponseString);
                        serverError(0);
                    }
                }
            });
        }
    }


    private boolean checkNames() {

        boolean goodCredentials = true;
        if (TextUtils.isEmpty(mLastNameEditText.getText().toString())) {
            mLastNameEditText.setError(getString(R.string.error_field_required));
            mLastNameEditText.requestFocus();
            goodCredentials = false;
        }
        if (TextUtils.isEmpty(mFirstNameEditText.getText().toString())) {
            mFirstNameEditText.setError(getString(R.string.error_field_required));
            mFirstNameEditText.requestFocus();
            goodCredentials = false;
        }

        return goodCredentials;
    }


    private void getPinCode(String email) {
        if(getActivity() == null) return;
        new LSDKUser(getActivity()).getConfirmationCodeForEmail(email, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                failedInternetConnection(0);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        mPinCode = (new JSONObject(response.body().string())).getString("pinCode");
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false, 0);
                                mViewSwitcher.showNext();
                                mInVerificationView = true;
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        serverError(0);
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    serverError(0);
                }
            }
        });
    }


    private boolean checkEmail(String emailString) {
        //if empty return false
        if (TextUtils.isEmpty(emailString)) {
            mEmailEditText.setError(getString(R.string.error_field_required));
            mEmailEditText.requestFocus();
            return false;
        }

        //invalid email
        else if (!emailString.contains("@") || emailString.startsWith("@") || emailString.contains("@.")) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
            mEmailEditText.requestFocus();
            return false;
        }

        //not edu email
        /* TODO: uncmoment this
        else if (!emailString.endsWith(".edu")){
            mEmailEditText.setError("This must be an edu email");
            mEmailEditText.requestFocus();
            return false;
        }*/

        //good email
        else {
            return true;
        }
    }


    private void loadProfileImage() {
        Glide.with(this)
                .load(mProfilePath)
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(mProfileImage);
    }

    private void goBackAnimation(boolean goBack) {
        if (getActivity() == null) return;
        mViewSwitcher.setInAnimation(getActivity(), goBack ? R.anim.slide_in_left : R.anim.slide_in_right);
        mViewSwitcher.setOutAnimation(getActivity(), goBack ? R.anim.slide_out_right : R.anim.slide_out_left);
    }

    private void showProgress(final boolean show, final int viewIndex) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final View progressBar;
        final View buttons;

        if (viewIndex == 0) {
            progressBar = mProgressBar1;
            buttons = mLayer1Buttons;
        } else {
            progressBar = mProgressBar2;
            buttons = mLayer2Buttons;
        }

        buttons.setVisibility(show ? View.GONE : View.VISIBLE);
        buttons.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                buttons.setVisibility(show ? View.GONE : View.VISIBLE);
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

        mCheckInProgress = show;
    }


    private void failedInternetConnection(final int index) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showBadConnectionToast(getActivity());
            }
        });
    }

    private void serverError(final int index) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showServerErrorToast(getActivity());
            }
        });
    }

    private void nonUniqueEmail() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, 0);
                mEmailEditText.setError("Email already in use");
                mEmailEditText.requestFocus();
            }
        });
    }

    private void persistData(LinuteUser user) {
        if (getActivity() == null) return;
        SharedPreferences.Editor sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();
        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
        sharedPreferences.putString("email", user.getEmail());
        sharedPreferences.putString("status", user.getStatus());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("phone", user.getPhone());
        sharedPreferences.putString("collegeName", user.getCollegeName());
        sharedPreferences.putString("collegeId", user.getCollegeId());
        sharedPreferences.putString("campus", user.getCampus());
        sharedPreferences.putString("socialFacebook", user.getSocialFacebook());
        sharedPreferences.putString("password", mSharedPreferences.getString("password", ""));

        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();

        Utils.deleteTempSharedPreference(mSharedPreferences.edit());
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mFirstNameEditText.isFocused()){
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mFirstNameEditText.getWindowToken(), 0);
        }else if (mLastNameEditText.hasFocus()){
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLastNameEditText.getWindowToken(), 0);
        }else if (mEmailEditText.hasFocus()){
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmailEditText.getWindowToken(), 0);
        }

    }
}
