package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.linute.linute.API.LSDKUser;
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
import java.util.HashMap;
import java.util.Map;

public class ChangeEmailActivity extends AppCompatActivity {

    public static final String TAG = "ChangeEmailActivity";
    private SharedPreferences mSharedPreferences;

    private EditText mEmailText;
    private String mSavedEmail;

    private String mPincode;
    private EditText mPinCodeText;

    private View mSaveButton;
    private View mVerifyButton;

    private ViewFlipper mViewFlipper;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);


        setupToolbar();
        bindViews();
        setDefaultValues();
        setUpOnClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.changeemail_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Email");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mProgressBar.getVisibility() != View.GONE) return true;
                if (mViewFlipper.getDisplayedChild() != 0) {
                    setToGoBackAnimation(true);
                    mViewFlipper.showPrevious();
                    setToGoBackAnimation(false);
                } else {
                    onBackPressed();
                }
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }


    private void bindViews() {
        mEmailText = (EditText) findViewById(R.id.changeemail_text);
        mProgressBar = (ProgressBar) findViewById(R.id.changeemail_progressbar);
        mViewFlipper = (ViewFlipper) findViewById(R.id.changeemail_view_flipper);

        mPinCodeText = (EditText) findViewById(R.id.changeemail_pin_code);

        setToGoBackAnimation(false);

        mSaveButton = findViewById(R.id.changeemail_save_button);
        mVerifyButton = findViewById(R.id.changeemail_check_verify);

    }

    private void setDefaultValues() {
        mSavedEmail = mSharedPreferences.getString("email", "");
        mEmailText.append(mSavedEmail);
    }


    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailUniquenessAndSave();
            }
        });

        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVerifyCode();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void checkEmailUniquenessAndSave() {
        final String email = mEmailText.getText().toString().toLowerCase();

        //not valid email or user hasn't eddited email
        if (!edittedEmail(email) || !isEmailValid(email)) return;

        showProgress(true);

        LSDKUser user = new LSDKUser(this);

        user.isUniqueEmail(email, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) { //no connection
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(ChangeEmailActivity.this); //show error
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {//unique email
                    response.body().close();
                    getPinCode(email);
                } else {//not unique
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() { //show error
                        @Override
                        public void run() {
                            showProgress(false);
                            emailAlreadyTaken();
                        }
                    });
                }
            }
        });
    }


    private void getPinCode(final String email) {

        String fName = mSharedPreferences.getString("firstName", "");
        String lName = mSharedPreferences.getString("lastName", "");

        new LSDKUser(this).getConfirmationCodeForEmail(email, fName, lName, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(ChangeEmailActivity.this);
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {

                        JSONObject json = new JSONObject(response.body().string());

                        mPincode = json.getString("pinCode");
                        mSavedEmail = email;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                mViewFlipper.showNext();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(ChangeEmailActivity.this);
                                showProgress(false);
                            }
                        });
                    }
                } else {
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(ChangeEmailActivity.this);
                            showProgress(false);
                        }
                    });
                }
            }
        });

    }


    private void checkVerifyCode() {
        if (mPincode.equals(mPinCodeText.getText().toString())) {
            saveEmail();
        } else {
            mPinCodeText.setError("Invalid pin");
            mPinCodeText.requestFocus();
        }
    }

    private void saveEmail() {

        LSDKUser user = new LSDKUser(this);

        showProgress(true);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", mSavedEmail);

        user.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(ChangeEmailActivity.this);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(ChangeEmailActivity.this);
                                showProgress(false);
                                finish();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(ChangeEmailActivity.this);
                                showProgress(false);
                            }
                        });
                    }
                } else {
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(ChangeEmailActivity.this);
                            showProgress(false);
                        }
                    });
                }

            }
        });
    }

    private void emailAlreadyTaken() {
        mEmailText.setError(getString(R.string.signup_error_email_taken));
        mEmailText.requestFocus();
    }

    private boolean edittedEmail(String email) {
        return !email.equals(mSavedEmail);
    }


    //TODO: ADD ERROR FOR NON EDU EMAIL
    private boolean isEmailValid(String email) {
        if (TextUtils.isEmpty(email)) {
            mEmailText.setError(getString(R.string.error_field_required));
            mEmailText.requestFocus();
            return false;
        }
        // no @                     //not edu email             //@cuny.edu
        if (!email.contains("@") || !email.endsWith(".edu") || email.startsWith("@") ||
                email.contains("@.") || email.contains(" ")) {
            //me@.edu                   //whitespace
            mEmailText.setError(getString(R.string.error_invalid_email));
            mEmailText.requestFocus();
            return false;
        }
        return true;
    }

    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("email", user.getEmail()).apply();
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final View button = mViewFlipper.getDisplayedChild() == 0 ? mSaveButton : mVerifyButton;

        button.setVisibility(show ? View.GONE : View.VISIBLE);
        button.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setVisibility(show ? View.GONE : View.VISIBLE);
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
        if (focusable) {  //turn on
            mEmailText.setFocusableInTouchMode(true);
            mPinCodeText.setFocusableInTouchMode(true);
        } else {
            mEmailText.setFocusable(false);
            mPinCodeText.setFocusable(false);
        }
    }

    private void setToGoBackAnimation(boolean goBack) {
        mViewFlipper.setInAnimation(this, goBack ? R.anim.slide_in_left : R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(this, goBack ? R.anim.slide_out_right : R.anim.slide_out_left);

    }
}
