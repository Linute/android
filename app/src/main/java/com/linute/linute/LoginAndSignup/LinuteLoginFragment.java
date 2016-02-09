package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;


import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.linute.linute.API.Device;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.API.QuickstartPreferences;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A login screen that offers login via email/password.
 */
public class LinuteLoginFragment extends Fragment {


    public static final String TAG = "LoginFragment";

    private boolean mCheckingCredentials = false;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private View mSigninButton;
    private View mCreateAccount;

    private View mButtonsLayer;

    private View mForgotPassword; //TODO


    public LinuteLoginFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mEmailView = (EditText) rootView.findViewById(R.id.signin_email_text);

        mButtonsLayer = rootView.findViewById(R.id.login_buttons_layer);

        //set last logged in email
        mEmailView.append(getActivity().getSharedPreferences(
                LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("lastLoginEmail", ""));

        mPasswordView = (EditText) rootView.findViewById(R.id.signin_email_password_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.signin_progress_bar);

        mSigninButton =  rootView.findViewById(R.id.signin_signin_button);
        mSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mCreateAccount =  rootView.findViewById(R.id.signin_create_button);
        mCreateAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null) {
                    activity.selectedSignup();
                }
            }
        });


        mForgotPassword = rootView.findViewById(R.id.login_forgot_pass);

        mForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null) {
                    activity.selectForgotPassword();
                }
            }
        });

        rootView.findViewById(android.R.id.home).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null){
                    activity.onBackPressed();
                }
            }
        });

        return rootView;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mCheckingCredentials || getActivity() == null) { //already checking credentials
            return;
        }
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().toLowerCase();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            cancel = true;
        }

        if (!cancel) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            checkRegisteredDevice(email, password);
        }
    }

    private void checkRegisteredDevice(String email, String password) {
        if (getActivity() == null) return;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean("deviceRegistered", false)) {
            checkCredentialsWithDB(email, password);
        } else {
            sendRegistrationDevice(sharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, ""), email, password);
        }
    }

    private boolean isEmailValid(String email) {
        /*NOTE: some old users still have non-edu emails
         *      we have to take them to a update email activity in that situation
         *
         */
        // @.edu                        //hey@.edu          //hey.edu
        if (email.startsWith("@") || email.contains("@.") || !email.contains("@") ||
                !email.contains(".") || email.contains(" "))
            //hello@edededu             //whitespace
            return false;
        return true;
    }

    private boolean isPasswordValid(String password) {
        //longer than 5 and can't contain whitespace
        return password.length() >= 6 && !password.contains(" ");
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mButtonsLayer.setVisibility(show ? View.GONE : View.VISIBLE);
        mButtonsLayer.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mButtonsLayer.setVisibility(show ? View.GONE : View.VISIBLE);
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


        mCheckingCredentials = show;
        setFocusable(!show);
    }

    private void setFocusable(boolean focusable) {
        if (focusable) { //turn on
            mEmailView.setFocusableInTouchMode(true);
            mPasswordView.setFocusableInTouchMode(true);
        } else {
            mEmailView.setFocusable(false);
            mPasswordView.setFocusable(false);
        }
    }

    private void checkCredentialsWithDB(String email, final String password) {

        if (getActivity() == null) return;
        LSDKUser checker = new LSDKUser(getActivity());
        checker.loginUserWithEmail(email, password, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(rFailedConnectionAction);
            }

            @Override
            public void onResponse(Response response) throws IOException {

                String res = response.body().string();

                Log.i(TAG, "onResponse: "+response.code()+" "+res);

                if (response.isSuccessful()) {
                    try {
                        saveCredentials(res, password);
                        final PreLoginActivity activity = (PreLoginActivity) getActivity();
                        if (activity == null) return;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.goToNextActivity();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Credentials weren't saved");
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                showProgress(false);
                            }
                        });
                    }
                } else if (response.code() == 404) { //bad credentials

                    try{
                        JSONObject obj = new JSONObject(res);
                        final boolean emailError = obj.getString("error").equals("email");

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (emailError){
                                    mEmailView.setError("No account with this email");
                                }else {
                                    mPasswordView.setError("Invalid password");
                                }
                                showProgress(false);
                            }
                        });

                    }catch (JSONException e){
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
                    //Log.e(TAG, "onResponse: "+res);
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


    private Runnable rFailedConnectionAction = new Runnable() {
        @Override
        public void run() {
            showProgress(false);
            Utils.showBadConnectionToast(getActivity());
        }
    };

    private void invalidCredentials() {
        showProgress(false);
        mEmailView.setError("Invalid email or password");
        mPasswordView.setError("Invalid email or password");
    }

    private void saveCredentials(String responseString, String password) throws JSONException {

        if (getActivity() == null) return;

        JSONObject response = new JSONObject(responseString);
        SharedPreferences.Editor sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

        LinuteUser user = new LinuteUser(response);

        sharedPreferences.putString("password", password);
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


        sharedPreferences.putString("lastLoginEmail", user.getEmail());

        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();
    }


    //used to registere device if somehow device wasn't registered
    private void sendRegistrationDevice(String token, final String email, final String password) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String versionName = "";
        String versionCode = "";
        if (getActivity() == null) return;
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(rFailedConnectionAction);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, response.body().string());
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getActivity());
                            showProgress(false);
                        }
                    });
                } else {
                    Log.v(TAG, response.body().string());
                    if (getActivity() == null) return;
                    getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("deviceRegistered", true)
                            .apply();
                    checkCredentialsWithDB(email, password);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mEmailView.hasFocus()){
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
        }else if (mPasswordView.hasFocus()){
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
        }

    }
}

