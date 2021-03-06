package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangePhoneActivity extends BaseSocketActivity {

    public static final String TAG = "ChangePhone";

    private SharedPreferences mTempSharedPref;
    private SharedPreferences mSharedPreferences;

    private ViewSwitcher mViewSwitcher;

    private EditText mPhoneNumber;
    private EditText mConfirmation;

    private Button mGetConfirmation;
    private Button mEnterNewNumber;
    private Button mVerifyConfirmation;

    private View mSecondViewButtons;

    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;

    private LSDKUser mUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone);

        mTempSharedPref = getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE);
        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        mUser = new LSDKUser(this);

        setupToolbar();
        bindView();
        setUpViewSwitcher();
        setUpDefaultValues();
        setUpOnClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.changephone_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("Phone");
        setSupportActionBar(toolbar);
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
        if (mProgressBar1.getVisibility() == View.GONE && mProgressBar2.getVisibility() == View.GONE) {
            super.onBackPressed();
        }
    }

    private void bindView() {
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.changePhone_viewSwitcher);
        mPhoneNumber = (EditText) findViewById(R.id.changePhone_phone);
        mConfirmation = (EditText) findViewById(R.id.changePhone_code);

        mGetConfirmation = (Button) findViewById(R.id.changePhone_get_confirmation);
        mEnterNewNumber = (Button) findViewById(R.id.changePhone_new_number);
        mVerifyConfirmation = (Button) findViewById(R.id.changePhone_save_button);

        mSecondViewButtons = findViewById(R.id.changePhone_second_view_buttons);

        mProgressBar1 = (ProgressBar) findViewById(R.id.changePhone_progressbar1);
        mProgressBar2 = (ProgressBar) findViewById(R.id.changePhone_progressbar2);
    }

    private void setUpDefaultValues() {
        //check if there is already a phone number associated with account
        String phone = mSharedPreferences.getString("phone", "");

        //Log.v(TAG, phone);

        //if no number associated, check if theres a temp number stored
        if (phone.equals("null") || phone.equals(""))
            phone = mTempSharedPref.getString("tempPhone", "");

        //Log.v(TAG, phone);

        mPhoneNumber.append(phone.equals("") ? "+1" : phone);
    }

    private void setUpViewSwitcher() {
        mViewSwitcher.setInAnimation(this, R.anim.slide_in_right);
        mViewSwitcher.setOutAnimation(this, R.anim.slide_out_left);

        //if there is already a code present, go to confirmation
        if (!mTempSharedPref.getString("tempPhone", "").equals("")) {
            mViewSwitcher.showNext();
        }
    }

    private void setUpOnClickListeners() {
        mGetConfirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPhoneUniqueness();
            }
        });

        mEnterNewNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewSwitcher.showPrevious();
            }
        });

        mVerifyConfirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConfirmation();
            }
        });
    }

    //checks to see if phone number unique
    private void checkPhoneUniqueness() {

        //get rid of white spaces and '-' characters
        final String phone = mPhoneNumber.getText().toString().trim().replaceAll("-", "");

        //not valid phone number
        if (!isValidPhoneNumber(phone) || !phoneEditted(phone))
            return;


        showProgress(true, mGetConfirmation, mProgressBar1, mPhoneNumber);

        mUser.isUniquePhone(phone, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { //no connection
                showBadConnectionToast(mGetConfirmation, mProgressBar1, mPhoneNumber);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();
                    getConfirmationCode(phone); //is unqiue so try to get confirmation code
                } else if (response.code() == 404){//not unique so tell user
                    Log.v(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false, mGetConfirmation, mProgressBar1, mPhoneNumber);
                            mPhoneNumber.setError(getString(R.string.changephone_number_in_use));
                            mPhoneNumber.requestFocus();
                        }
                    });
                }else {
                    response.body().close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(ChangePhoneActivity.this);
                        }
                    });
                }
            }
        });
    }


    //gets confirmation code and saves it
    private void getConfirmationCode(final String phone) {
        mUser.getConfirmationCodeForPhone(phone, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showBadConnectionToast(mGetConfirmation, mProgressBar1, mPhoneNumber);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String code = new JSONObject(response.body().string()).getString("pinCode");
                        persistTempData(phone);//save information
                        //Log.d(TAG, "onResponse: "+code);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() { //show next view
                                showProgress(false, mGetConfirmation, mProgressBar1, mPhoneNumber);
                                mViewSwitcher.showNext();
                            }
                        });
                    } catch (JSONException e) {//weird response, show error
                        e.printStackTrace();
                        showServerErrorToast(mGetConfirmation, mProgressBar1, mPhoneNumber);
                    }
                } else {
                    Log.e(TAG, response.body().string());
                    showServerErrorToast(mGetConfirmation, mProgressBar1, mPhoneNumber);
                }
            }
        });
    }

    private void checkConfirmation() {
        mConfirmation.setError(null);
        String enteredCode = mConfirmation.getText().toString();
        showProgress(true, mSecondViewButtons, mProgressBar2, mConfirmation);

        final String phone = mTempSharedPref.getString("tempPhone", "");
        mUser.checkPhonePincode(phone, enteredCode, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(ChangePhoneActivity.this);
                        showProgress(false, mSecondViewButtons, mProgressBar2, mConfirmation);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    updateInformation(phone);
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false, mSecondViewButtons, mProgressBar2, mConfirmation);
                            mConfirmation.setError("Invalid pin");
                            mConfirmation.requestFocus();
                        }
                    });
                }
                response.body().close();
            }
        });


    }

    private void updateInformation(String phone){
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("phone", phone);

        mUser.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showBadConnectionToast(mSecondViewButtons, mProgressBar2, mConfirmation);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        persistData(new LinuteUser(new JSONObject(response.body().string()))); //save phone number
                        Utils.deleteTempSharedPreference(mTempSharedPref); //delete temp info

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false, mSecondViewButtons, mProgressBar2, mConfirmation);
                                Utils.showSavedToast(ChangePhoneActivity.this);
                                finish();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast(mSecondViewButtons, mProgressBar2, mConfirmation);
                    }
                } else {
                    Log.e(TAG, response.body().string());
                    showServerErrorToast(mSecondViewButtons, mProgressBar2, mConfirmation);
                }
            }
        });
    }

    private boolean isValidPhoneNumber(String number) {
        //matches string "+1" + 10digits
        if (!number.matches("\\+1\\d{10}")) {
            mPhoneNumber.setError("Invalid Phone Number");
            mPhoneNumber.requestFocus();
            return false;
        }
        return true;
    }

    //determines if user editted phone
    private boolean phoneEditted(String phone) {
        return !phone.equals(mSharedPreferences.getString("phone", ""));
    }

    private void persistTempData(String tempPhone) {
        mTempSharedPref.edit().putString("tempPhone", tempPhone).apply();
    }

    private void persistData(LinuteUser user) {
        //Log.v(TAG, user.getPhone());
        mSharedPreferences.edit().putString("phone", user.getPhone()).apply();
    }


    //shows or hide prpgress bar
    //@param show - show or hide progress bar
    //@param buttonLayer - buttons that are hidden during loading
    //@param bar - progressbar that is hidden or shown
    //@param texView - the editText that needs to be made unfocuable; don't want user editing text during load
    private void showProgress(final boolean show, final View buttonLayer, final ProgressBar bar, View textView) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        buttonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
        buttonLayer.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                buttonLayer.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        bar.setVisibility(show ? View.VISIBLE : View.GONE);
        bar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });


        setFocusable(!show, textView);
    }

    private void setFocusable(boolean focusable, View textView) {
        if (focusable)  //turn on
            textView.setFocusableInTouchMode(true);

        else textView.setFocusable(false);
    }

    private void showServerErrorToast(final View buttonLayer, final ProgressBar bar, final View textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, buttonLayer, bar, textView);
                Utils.showServerErrorToast(ChangePhoneActivity.this);
            }
        });
    }

    private void showBadConnectionToast(final View buttonLayer, final ProgressBar bar, final View textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, buttonLayer, bar, textView);
                Utils.showBadConnectionToast(ChangePhoneActivity.this);
            }
        });
    }
}
