package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.linute.linute.MainContent.MainActivity;
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
public class LinuteLoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    public static final String TAG = "LoginActivity";

    private boolean mCheckingCredentials = false;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private Button mSigninButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linute_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.signin_email_text);

        //set last logged in email
        mEmailView.append(getSharedPreferences(
                LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).getString("lastLoginEmail", ""));

        mPasswordView = (EditText) findViewById(R.id.signin_email_password_text);
        mProgressBar = (ProgressBar) findViewById(R.id.signin_progress_bar);

        mSigninButton = (Button) findViewById(R.id.signin_signin_button);
        mSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mCheckingCredentials) { //already checking credentials
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showBadConnectionToast(this);
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            checkRegisteredDevice(email, password);


        }
    }

    private void checkRegisteredDevice(String email, String password){
        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        if (sharedPreferences.getBoolean("deviceRegistered", false)){
            checkCredentialsWithDB(email, password);
        }else {
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

        mSigninButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mSigninButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSigninButton.setVisibility(show ? View.GONE : View.VISIBLE);
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

        LSDKUser checker = new LSDKUser(this);
        checker.loginUserWithEmail(email, password, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(rFailedConnectionAction);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        saveCredentials(response.body().string(), password);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                goToNextActivity();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Credentials weren't saved");
                    }

                } else if (response.code() == 404) { //bad credentials
                    response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            invalidCredentials();
                        }
                    });
                } else {
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(LinuteLoginActivity.this);
                            showProgress(false);
                        }
                    });

                }
            }
        });
    }

    private void goToNextActivity() {
        Class nextActivity;
        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        //college set, go to college
        if (sharedPreferences.getString("collegeName", null) != null && sharedPreferences.getString("collegeId", null) != null)
            nextActivity = MainActivity.class;

        //college picker is not set. go to college picker
        else
            nextActivity = CollegePickerActivity.class;

        Intent i = new Intent(this, nextActivity);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear stack
        startActivity(i); //start new activity
        finish();
    }


    private Runnable rFailedConnectionAction = new Runnable() {
        @Override
        public void run() {
            showProgress(false);
            Utils.showBadConnectionToast(LinuteLoginActivity.this);
        }
    };

    private void invalidCredentials() {
        showProgress(false);
        mEmailView.setError("Invalid email or password");
        mPasswordView.setError("Invalid email or password");
        mEmailView.requestFocus();
    }

    private void saveCredentials(String responseString, String password) throws JSONException {
        JSONObject response = new JSONObject(responseString);
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

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
                runOnUiThread(rFailedConnectionAction);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(LinuteLoginActivity.this);
                            showProgress(false);
                        }
                    });
                } else {
                    Log.v(TAG, response.body().string());
                    getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE)
                            .edit()
                            .putBoolean("deviceRegistered", true)
                            .apply();
                    checkCredentialsWithDB(email, password);
                }
            }
        });

    }

}

