package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

public class EditNameActivity extends AppCompatActivity {

    public static final String TAG = "EditNameAcivity";

    private EditText mFirstName;
    private EditText mLastName;
    private SharedPreferences mSharedPreferences;
    private Button mSaveButton;
    private ProgressBar mProgressBar;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        bindView();
        setUpToolbar();
        setDefaultValues();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        mToolBar = (Toolbar) findViewById(R.id.editname_toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    public void onBackPressed() {
        if (mProgressBar.getVisibility() == View.GONE) {
            super.onBackPressed();
        }
    }

    private void setDefaultValues() {
        mFirstName.append(mSharedPreferences.getString("firstName", ""));
        mLastName.append(mSharedPreferences.getString("lastName", ""));
    }

    private void bindView() {
        mFirstName = (EditText) findViewById(R.id.prof_edit_fname_text);
        mLastName = (EditText) findViewById(R.id.prof_edit_lname_text);
        mSaveButton = (Button) findViewById(R.id.editname_save_button);
        mProgressBar = (ProgressBar) findViewById(R.id.prof_edit_name_progressbar);
    }

    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });
    }


    private boolean areValidFields(String firstName, String lastName) {
        boolean areValid = true;
        //no changes made
        if (lastName.equals(mSharedPreferences.getString("lastName", "")) &&
                firstName.equals(mSharedPreferences.getString("firstName", "")))
            return false;

        if (lastName.isEmpty()) {
            mLastName.setError(getString(R.string.error_field_required));
            mLastName.requestFocus();
            areValid = false;
        }
        if (firstName.isEmpty()) {
            mFirstName.setError(getString(R.string.error_field_required));
            mFirstName.requestFocus();
            areValid = false;
        }
        return areValid;
    }

    private void saveName() {
        String lastName = mLastName.getText().toString();
        String firstName = mFirstName.getText().toString();

        if (areValidFields(firstName, lastName)) {
            LSDKUser user = new LSDKUser(this);
            showProgress(true);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("firstName", firstName);
            userInfo.put("lastName", lastName);
            user.updateUserInfo(userInfo, null, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(EditNameActivity.this);
                            showProgress(false);
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            LinuteUser user = new LinuteUser(new JSONObject(response.body().string()));
                            saveInfo(user);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showSavedToast(EditNameActivity.this);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(EditNameActivity.this);
                                }
                            });
                        }


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(EditNameActivity.this);
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
        if (focusable) { //turn on
            mFirstName.setFocusableInTouchMode(true);
            mLastName.setFocusableInTouchMode(true);
        } else {
            mFirstName.setFocusable(false);
            mLastName.setFocusable(false);
        }
    }

    private void saveInfo(LinuteUser user) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.apply();
    }

}
