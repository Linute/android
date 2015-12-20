package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditBirthdayActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "EditBirthdayActivity";
    private ProgressBar mProgressBar;
    private TextView mBirthdayText;
    private Button mSaveButton;
    private Button mCancelButton;
    private Button mEditBirthdayButton;
    private View mButtonLayer;
    private Toolbar mToolBar;

    private String mBirthdayString;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_birthday);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        bindViews();
        setUpToolbar();
        setDefaultValues();
        setUpOnClickListeners();
    }

    private void bindViews(){
        mProgressBar = (ProgressBar) findViewById(R.id.editbirthday_progressbar);
        mBirthdayText = (TextView) findViewById(R.id.editbirthday_birthday_text);
        mSaveButton = (Button) findViewById(R.id.editbirthday_save_button);
        mCancelButton = (Button) findViewById(R.id.editbirthday_cancel_button);
        mEditBirthdayButton = (Button) findViewById(R.id.editbirthday_edit_button);
        mButtonLayer = findViewById(R.id.editbirthday_buttons);
    }

    private void setUpToolbar(){
        mToolBar = (Toolbar) findViewById(R.id.editbirthday_toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Birthday");
    }

    private void setDefaultValues(){
        String dob = mSharedPreferences.getString("dob", "");
        Log.v(TAG, dob);
        mBirthdayText.setText(Utils.formatToReadableString(
                dob.equals("") ? "Jan 1, 2000" : dob));
    }


    private void setUpOnClickListeners(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveBirthday();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(0, 0);
            }
        });

        mEditBirthdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }



    private void showDatePicker(){
        final Calendar c = Calendar.getInstance();

        new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mBirthdayString = formatDateFromInts(year, monthOfYear, dayOfMonth);
        mBirthdayText.setText(Utils.formatToReadableString(mBirthdayString));
    }

    public String formatDateFromInts(int year, int month, int day){
        String date = year+"-";
        date += (month < 10 ? "0" + month : month) + "-";
        date += day < 10 ? "0" + day : day;
        return date;
    }

    private void saveBirthday(){

        if (mBirthdayString == null || !birthdayHasBeenEditted(mBirthdayString))
            return;

        LSDKUser user = new LSDKUser(this);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("dob", mBirthdayString);
        showProgress(true);

        user.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(EditBirthdayActivity.this);
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
                    }
                    catch (JSONException e) { //caught error
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(EditBirthdayActivity.this);
                            }
                        });
                    }

                } else { //log error and show server error
                    Log.e(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(EditBirthdayActivity.this);
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

    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("dob", user.getDob()).commit();
    }

    private boolean birthdayHasBeenEditted(String birthday){
        String bday = mSharedPreferences.getString("dob", "");
        if (bday.isEmpty() || bday.equals(birthday)){
            return false;
        }
        return true;
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

        mEditBirthdayButton.setClickable(!show); //don't allow edit when querying
    }

}
