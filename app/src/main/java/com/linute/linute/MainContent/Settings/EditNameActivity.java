package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class EditNameActivity extends AppCompatActivity {

    private static final String TAG = "EditNameAcivity";

    private EditText mFirstName;
    private EditText mLastName;
    private SharedPreferences mSharedPreferences;
    private Button mSaveButton;
    private ProgressBar mProgressBar;
    private Toolbar mToolBar;
    private View mButtonLayer;
    private Button mCancelButton;

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

    private void setUpToolbar(){
        mToolBar = (Toolbar) findViewById(R.id.editname_toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Name");
    }

    private void setDefaultValues(){
        mFirstName.append(mSharedPreferences.getString("firstName", ""));
        mLastName.append(mSharedPreferences.getString("lastName", ""));
    }

    private void bindView() {
        mFirstName = (EditText) findViewById(R.id.prof_edit_fname_text);
        mLastName = (EditText) findViewById(R.id.prof_edit_lname_text);
        mSaveButton = (Button) findViewById(R.id.editname_save_button);
        mProgressBar = (ProgressBar) findViewById(R.id.prof_edit_name_progressbar);
        mCancelButton = (Button) findViewById(R.id.editname_cancel_button);
        mButtonLayer = (View) findViewById(R.id.editname_buttons);
    }

    private void setUpOnClickListeners(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(0,0);
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
        if (firstName.isEmpty()){
            mFirstName.setError(getString(R.string.error_field_required));
            mFirstName.requestFocus();
            areValid = false;
        }
        return areValid;
    }

    private void saveName(){
        String lastName = mLastName.getText().toString();
        String firstName = mFirstName.getText().toString();

        if(areValidFields(firstName, lastName)){
            LSDKUser user = new LSDKUser(this);
            showProgress(true);
            Map<String, String> userInfo = new HashMap<>();
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
                    if (response.isSuccessful()){
                        try {
                            LinuteUser user = new LinuteUser(new JSONObject(response.body().string()));
                            saveInfo(user);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(EditNameActivity.this);
                                }
                            });
                        }


                    }else{
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
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
            mButtonLayer.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
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
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mButtonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        setFocusable(!show);
    }

    private void setFocusable(boolean focusable){
        if (focusable) { //turn on
            mFirstName.setFocusableInTouchMode(focusable);
            mLastName.setFocusableInTouchMode(focusable);
        }
        else {
            mFirstName.setFocusable(focusable);
            mLastName.setFocusable(focusable);
        }
    }

    private void saveInfo(LinuteUser user){
        Log.v(TAG, user.getFirstName());
        Log.v(TAG, user.getLastName());
        Log.v(TAG, mSharedPreferences.getString("lastName", "nothing"));
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.commit();
        Log.v(TAG, mSharedPreferences.getString("firstName", "nothing"));
        Log.v(TAG, mSharedPreferences.getString("lastName", "nothing"));
    }

}
