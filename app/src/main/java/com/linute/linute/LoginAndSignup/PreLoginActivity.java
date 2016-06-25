package com.linute.linute.LoginAndSignup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PreLoginActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    public static String TAG = PreLoginActivity.class.getSimpleName();

    private String mFBToken;

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

        setUpFacebookCallback();


        View v = findViewById(R.id.chat_button);

        if (v != null)
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@tapt.io"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Help Me Plz");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT,
                            "If you are having issues with your signin (ie. not receiving pincode, " +
                                    "email not working), swap this text out with the problem you are having.");
                    startActivity(intent);
                }
            });

        if (savedInstanceState == null)
            replaceFragment(new PreLoginFragment());

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

        Intent intent = getIntent();
        if (intent != null) {
            String text = intent.getStringExtra("BANNED");
            if (text != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Banned")
                        .setMessage(text)
                        .setPositiveButton("okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
            intent.removeExtra("BANNED");
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.login_activity_fragment_frame, fragment);
        transaction.commit();
    }

    public void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.frag_fade_in, R.anim.frag_fade_out);
        transaction.replace(R.id.login_activity_fragment_frame, fragment).addToBackStack(null);
        transaction.commit();
    }


    public void selectedFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(PreLoginActivity.this, Arrays.asList("user_friends", "public_profile", "email"));
    }

    public void selectedTaptLogin() {
        addFragment(new LinuteLoginFragment());
    }

    public void selectedSignup() {
        addFragment(new LinuteSignUpFragment());
    }

    public void selectForgotPassword() {
        addFragment(new ForgotPasswordFragment());
    }

    private void setUpFacebookCallback() {

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    showFacebookPermissionsRationale();
                    return;
                }

                mFBToken = loginResult.getAccessToken().getToken();

                final ProgressDialog progress = ProgressDialog.show(PreLoginActivity.this, null, "Retrieving information from Facebook", true);
                loginOrSignUpWithFacebook(mFBToken, progress);
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


    private void loginOrSignUpWithFacebook(final String fbToken, final ProgressDialog progress) {

        new LSDKUser(this).authorizationFacebook(fbToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Utils.showBadConnectionToast(PreLoginActivity.this);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        String responseString = response.body().string();
                        //Log.i(TAG, "onResponse: " + responseString);
                        JSONObject object = new JSONObject(responseString);
                        boolean isUnique = object.getBoolean("isUnique");

                        final LinuteUser user = new LinuteUser(object);

                        if (isUnique) {
                            persistTempData(user);
                            goToFBSignUpFragment(progress);
                        }

                        //has signed up already and using edu email
                        else {
                            Log.i(TAG, "onResponse: going to college picker or logging in");
                            persistData(user); //save data
                            saveNotificationPreferences(object);
                            //if no college id or name, go to colleg picker activity
                            //else go to main
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    goToNextActivity((user.getCollegeName() == null || user.getCollegeId() == null)
                                            ? CollegePickerActivity.class : MainActivity.class);
                                }
                            });
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


    private void persistData(LinuteUser user) {
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

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
        sharedPreferences.putString("userToken", user.getUserToken());

        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();
    }


    private void saveNotificationPreferences(JSONObject object) {
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

        try {
            JSONObject settings = object.getJSONObject("notificationSettings");
            sharedPreferences.putBoolean("notif_follow", getBooleanFromJSONObj("follow", settings));
            sharedPreferences.putBoolean("notif_message", getBooleanFromJSONObj("message", settings));
            sharedPreferences.putBoolean("notif_mention", getBooleanFromJSONObj("mention", settings));
            sharedPreferences.putBoolean("notif_alsoComment", getBooleanFromJSONObj("alsoComment", settings));
            sharedPreferences.putBoolean("notif_comment", getBooleanFromJSONObj("comment", settings));
            sharedPreferences.putBoolean("notif_like", getBooleanFromJSONObj("like", settings));
        } catch (JSONException e) {
            e.printStackTrace();
            sharedPreferences.putBoolean("notif_follow", true);
            sharedPreferences.putBoolean("notif_message", true);
            sharedPreferences.putBoolean("notif_mention", true);
            sharedPreferences.putBoolean("notif_alsoComment", true);
            sharedPreferences.putBoolean("notif_comment", true);
            sharedPreferences.putBoolean("notif_like", true);
        }

        sharedPreferences.apply();
    }

    private void persistTempData(LinuteUser user) {
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE).edit();

        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("socialFacebook", user.getSocialFacebook());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putString("registrationType", user.getRegistrationType());
        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
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

    private void goToFBSignUpFragment(final ProgressDialog progressDialog) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                addFragment(new FacebookSignUpFragment());
            }
        });
    }

    public boolean getBooleanFromJSONObj(String key, JSONObject obj) {
        try {
            return obj.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void goToNextActivity() {
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


}

