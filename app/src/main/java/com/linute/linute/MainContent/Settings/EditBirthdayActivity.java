package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditBirthdayActivity extends AppCompatActivity {

    private static final String TAG = "EditBirthdayActivity";
    private ProgressBar mProgressBar;
    private DatePicker mDatePicker;
    private Button mSaveButton;

    private String mDob;

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

    private void bindViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.editbirthday_progressbar);
        mDatePicker = (DatePicker) findViewById(R.id.editbirthday_datepicker);
        mSaveButton = (Button) findViewById(R.id.editbirthday_save_button);
    }

    private void setUpToolbar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.editbirthday_toolbar);
        setSupportActionBar(toolBar);

        getSupportActionBar().setTitle("Birthday");
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
        String dob = mSharedPreferences.getString("dob", "");

        Calendar c = Calendar.getInstance();

        //try to set date picker to person's birthday
        try {
            if (!dob.equals("")) {
                SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                c.setTime(fm.parse(dob));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        mDob = formatDateFromInts(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
    }


    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBirthday();
            }
        });
    }


    public String formatDateFromInts(int year, int month, int day) {
        String date = year + "-";
        month++; //month is in range 0-11 so we need to add one
        date += (month < 10 ? "0" + month : month) + "-";
        date += day < 10 ? "0" + day : day;
        return date;
    }

    private void saveBirthday() {

        final String dob = formatDateFromInts(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());

        if (!birthdayHasBeenEditted(dob))
            return;

        LSDKUser user = new LSDKUser(this);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("dob", dob);
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
                        mDob = dob;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(EditBirthdayActivity.this);
                            }
                        });
                    } catch (JSONException e) { //caught error
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
        mSharedPreferences.edit().putString("dob", user.getDob()).apply();
    }

    private boolean birthdayHasBeenEditted(String birthday) {
        return !mDob.equals(birthday);
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


        mDatePicker.setClickable(!show); //don't allow edit when querying
    }

}
