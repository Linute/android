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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

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

public class EditGenderActivity extends AppCompatActivity {

    private static final String TAG = "EditGenderActivity";
    private Spinner mSpinner;

    private Button mSaveButton;
    private Button mCancelButton;
    private View mButtonLayer;

    private ProgressBar mProgressBar;

    private SharedPreferences mSharedPreferences;

    private int mSavedGender = 0; //gender saved to sharedPref

    //List of choices
    private String [] mGenders = {"Not Specified", "Male", "Female"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gender);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        bindViews();
        setupToolbar();
        setUpSpinner();
        setDefaultValues();
        setUpOnClickLisenters();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.editgender_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sex");
    }


    private void bindViews() {
        mSpinner = (Spinner) findViewById(R.id.editgender_spinner);
        mSaveButton = (Button) findViewById(R.id.editgender_save_button);
        mCancelButton = (Button) findViewById(R.id.editgender_cancel_button);

        mButtonLayer = findViewById(R.id.editgender_buttons);

        mProgressBar = (ProgressBar) findViewById(R.id.editgender_progressbar);
    }

    private void setUpSpinner(){

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        mGenders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
    }

    private void setDefaultValues(){
        mSavedGender = mSharedPreferences.getInt("sex", 0);
        mSpinner.setSelection(mSavedGender);
    }

    private void setUpOnClickLisenters(){
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(0, 0);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGender();
            }
        });
    }

    private void saveGender() {
        final int gender = mSpinner.getSelectedItemPosition();

        //gender hasn't been editted
        if(!genderEditted(gender))
            return;

        LSDKUser user = new LSDKUser(this);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("sex", gender+"");

        showProgress(true);

        user.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(EditGenderActivity.this);
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
                        mSavedGender = gender;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(EditGenderActivity.this);
                            }
                        });
                    } catch (JSONException e) { //caught error
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(EditGenderActivity.this);
                            }
                        });
                    }

                } else { //log error and show server error
                    Log.e(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(EditGenderActivity.this);
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

    private boolean genderEditted(int gender){
        return gender != mSavedGender;
    }

    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putInt("sex", user.getSex()).apply();
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

        mSpinner.setClickable(!show); //don't allow edit when querying
    }

    public static String getGenderFromIndex(int position){
        switch (position) {
            case 0:
                return "Not Specified";
            case 1:
                return "Male";
            case 2:
                return "Female";
            default:
                return "Not Specified";
        }
    }

}
