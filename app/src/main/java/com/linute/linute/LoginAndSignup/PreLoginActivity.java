package com.linute.linute.LoginAndSignup;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private LoginButton mFacebookLoginButton;
    private TextView mSignupText;

    private CallbackManager mCallbackManager;

    private ImageSwitcher mImageSwitcher;

    //list of images we will use
    private int[] mBackgroundImageResID = {
            R.drawable.college_walk1,
            R.drawable.college_walk2,
            R.drawable.college_walk3
    };

    private int mImageCount = mBackgroundImageResID.length;
    private int mCurrentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_pre_login);
        mCallbackManager = CallbackManager.Factory.create();

        //switches background images
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.background_image_switcher);

        mLinuteLoginButton = (Button) findViewById(R.id.prelogin_linute_login);
        mLinuteLoginButton.setOnClickListener(mLinuteOnClickListener);

        mSignupText = (TextView) findViewById(R.id.linute_signup);
        mSignupText.setOnClickListener(mLinuteSignUpClicked);


        mFacebookLoginButton = (LoginButton) findViewById(R.id.preLogin_facebook_login);

        setUpFacebookLoginButton();

        setUpImageSwitcher();
    }

    private void setUpFacebookLoginButton() {
        List<String> readPermissions = new ArrayList<>(3);
        readPermissions.add("user_friends");
        readPermissions.add("public_profile");
        readPermissions.add("email");
        readPermissions.add("user_relationships");

        mFacebookLoginButton.setReadPermissions(readPermissions);
        mFacebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    Log.i(TAG, "Missing permissions");
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

    private void loginOrSignUpWithFacebook(final String fbToken){
        new LSDKUser(this).authorizationFacebook(fbToken, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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

                        //never signed up before or non edu email
                        if (isUnique || !isEduEmail(user.getEmail())) {
                            persistTempData(user);
                            goToFBSignUpActivity();
                        }

                        //has signed up already and using edu email
                        else {
                            persistData(user); //save data

                            //if no college id or name, go to colleg picker activity
                            //else go to main
                            goToNextActivity((user.getCollegeName() == null || user.getCollegeId() == null)
                                    ? CollegePickerActivity.class : MainActivity.class);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast();
                    }

                } else {
                    Log.i(TAG, "onResponse: " + response.body().string());
                    showServerErrorToast();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setUpImageSwitcher() {

        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mImageSwitcher.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });

        mImageSwitcher.setImageResource(mBackgroundImageResID[0]); //set first image

        //set in and out animations
        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));

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

    @Override
    protected void onResume() {
        super.onResume();
        mImageSwitcher.postDelayed(rSwitchPicture, 6000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mImageSwitcher.removeCallbacks(rSwitchPicture);
    }

    private Runnable rSwitchPicture = new Runnable() {
        @Override
        public void run() {
            //cycle through the images
            mImageSwitcher.setImageResource(mBackgroundImageResID[getNextBackgroundImageIndex()]);
            mImageSwitcher.postDelayed(this, 6000);
        }
    };

    private int getNextBackgroundImageIndex() {
        //cycles to beginning image when at the end
        return ++mCurrentImageIndex == mImageCount ? mCurrentImageIndex = 0 : mCurrentImageIndex;
    }


    //go to SignIn Actvity
    private OnClickListener mLinuteOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent goToLinuteLogin = new Intent(getApplicationContext(), LinuteLoginActivity.class);
            startActivity(goToLinuteLogin);
        }
    };

    //takes you to signup activity
    private OnClickListener mLinuteSignUpClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent goToLinuteLogin = new Intent(getApplicationContext(), LinuteSignUpActivity.class);
            goToLinuteLogin.putExtra("CURR_BACKGROUND_INDEX", mCurrentImageIndex);
            startActivity(goToLinuteLogin);
        }
    };

    private void showFacebookErrorToast() {
        Toast.makeText(this, R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }

    private void showServerErrorToast(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(PreLoginActivity.this);
            }
        });
    }

    private boolean isEduEmail(String email){
        if (email != null && email.endsWith(".edu")){
            return true;
        }

        return false;
    }


    private void persistData(LinuteUser user){
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

    private void persistTempData(LinuteUser user){
        SharedPreferences.Editor sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

        sharedPreferences.putString("password", mFBToken);
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

    private void goToFBSignUpActivity(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(PreLoginActivity.this, FacebookSignUpActivity.class);
                startActivity(i);
            }
        });

    }
}

