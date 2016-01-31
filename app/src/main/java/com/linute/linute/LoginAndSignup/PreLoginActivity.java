package com.linute.linute.LoginAndSignup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreLoginActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    public static String TAG = "PreLogin";

    private String mFBToken;

    private Button mLinuteLoginButton;
    private Button mFacebookloginButton;
    private TextView mSignupText;

    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_pre_login);
        mCallbackManager = CallbackManager.Factory.create();

        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }

        bindViews();
        setUpOnClickListeners();

        setUpFacebookCallback();

    }


    private void bindViews() {
        //switches background images
        mLinuteLoginButton = (Button) findViewById(R.id.prelogin_linute_login);
        mFacebookloginButton = (Button) findViewById(R.id.preLogin_facebook_login);
        mSignupText = (TextView) findViewById(R.id.linute_signup);
    }

    private void setUpOnClickListeners() {

        mFacebookloginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(PreLoginActivity.this, Arrays.asList("user_friends", "public_profile", "email"));
            }
        });

        mLinuteLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLinuteLogin = new Intent(getApplicationContext(), LinuteLoginActivity.class);
                startActivity(goToLinuteLogin);
            }
        });

        mSignupText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLinuteLogin = new Intent(getApplicationContext(), LinuteSignUpActivity.class);
                startActivity(goToLinuteLogin);
            }
        });
    }

    private void setUpFacebookCallback() {

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    showFacebookPermissionsRationale();
                    return;
                }

                mFBToken = loginResult.getAccessToken().getToken(); //NOTE : NEED IT>
                loginOrSignUpWithFacebook(mFBToken);

            }

            @Override
            public void onCancel() {
                return;
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                showFacebookErrorToast();
            }
        });

    }

    private void loginOrSignUpWithFacebook(final String fbToken) {

        final ProgressDialog progress = ProgressDialog.show(this, null, "Retrieving information from Facebook",true);

        new LSDKUser(this).authorizationFacebook(fbToken, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Utils.showBadConnectionToast(PreLoginActivity.this);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        String responseString = response.body().string();
                        Log.i(TAG, "onResponse: " + responseString);
                        JSONObject object = new JSONObject(responseString);
                        boolean isUnique = object.getBoolean("isUnique");

                        LinuteUser user = new LinuteUser(object);

                        if (isUnique) {
                            persistTempData(user);
                            goToFBSignUpActivity(progress);
                        }

                        //has signed up already and using edu email
                        else {
                            Log.i(TAG, "onResponse: going to college picker or logging in");
                            persistData(user); //save data
                            //if no college id or name, go to colleg picker activity
                            //else go to main
                            goToNextActivity((user.getCollegeName() == null || user.getCollegeId() == null)
                                    ? CollegePickerActivity.class : MainActivity.class);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast(progress);
                    }

                } else {
                    Log.i(TAG, "onResponse: " + response.body().string());
                    showServerErrorToast(progress);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void showFacebookPermissionsRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Facebook Permissions")
                .setMessage("Tapt needs access to your Facebook account's email and friends list to log in properly.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginManager.getInstance().logInWithReadPermissions(PreLoginActivity.this, Arrays.asList("user_friends", "public_profile", "email"));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("FBToken", mFBToken);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mFBToken = savedInstanceState.getString("FBToken");
        }
    }


    private void showFacebookErrorToast() {
        Toast.makeText(this, R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }

    private void showServerErrorToast(final ProgressDialog dialog) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(PreLoginActivity.this);
                dialog.dismiss();
            }
        });
    }

    private boolean isEduEmail(String email) {
        if (email != null && email.endsWith(".edu")) {
            return true;
        }

        return false;
    }


    private void persistData(LinuteUser user) {
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

        sharedPreferences.putString("password", mFBToken);
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

        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();

    }

    private void persistTempData(LinuteUser user) {
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE).edit();

        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("password", mFBToken);
        sharedPreferences.putString("socialFacebook", user.getSocialFacebook());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putString("registrationType", user.getRegistrationType());
        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
        sharedPreferences.putString("passwordFacebook", user.getPasswordFacebook());
        sharedPreferences.putString("email", user.getEmail());

        sharedPreferences.apply();
    }


    private void goToNextActivity(final Class nextActivity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(PreLoginActivity.this, nextActivity);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //clear stack
                startActivity(i); //start new activity
                finish();
            }
        });
    }

    private void goToFBSignUpActivity(final ProgressDialog progressDialog) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                Intent i = new Intent(PreLoginActivity.this, FacebookSignUpActivity.class);
                startActivity(i);
            }
        });

    }
}

