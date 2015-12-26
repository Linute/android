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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

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

    private Button mSaveButton;

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
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void bindViews() {
        mEmailText = (EditText) findViewById(R.id.changeemail_text);
        mSaveButton = (Button) findViewById(R.id.changeemail_save_button);
        mProgressBar = (ProgressBar) findViewById(R.id.changeemail_progressbar);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void checkEmailUniquenessAndSave() {
        final String email = mEmailText.getText().toString();

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
                    saveEmail(email);
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

    private void saveEmail(final String email) {

        LSDKUser user = new LSDKUser(this);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", email);

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
                        mSavedEmail = email;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(ChangeEmailActivity.this);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(ChangeEmailActivity.this);
                            }
                        });
                    }
                } else {
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(ChangeEmailActivity.this);
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
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

        mSaveButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mSaveButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSaveButton.setVisibility(show ? View.GONE : View.VISIBLE);
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
        if (focusable)  //turn on
            mEmailText.setFocusableInTouchMode(true);

        else mEmailText.setFocusable(false);
    }
}
