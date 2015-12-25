package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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

public class EditStatusActivity extends AppCompatActivity {

    private static final String TAG = "EditStatusActivity";
    private SharedPreferences mSharedPreferences;
    private Toolbar mToolBar;

    private ProgressBar mProgressBar;
    private EditText mStatusText;
    private Button mSaveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_status);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        bindView();
        setUpToolbar();
        setDefaultValues();
        setUpOnClickListeners();
        setUpEditTextMaxLines();
    }


    private void bindView() {
        mProgressBar = (ProgressBar) findViewById(R.id.editstatus_progressbar);
        mSaveButton = (Button) findViewById(R.id.editstatus_save);
        mStatusText = (EditText) findViewById(R.id.editstatus_status_text);
    }

    private void setUpToolbar() {
        mToolBar = (Toolbar) findViewById(R.id.editstatus_toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setDefaultValues() {
        String status = mSharedPreferences.getString("status", ""); //if there is a status, set it as default
        if (!status.equals(""))
            mStatusText.append(status);
    }


    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
            }
        });
    }

    //sets max line number to 3
    private void setUpEditTextMaxLines() {
        mStatusText.addTextChangedListener(new TextWatcher() {
            private String text;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                text = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mStatusText.getLineCount() > 3) {
                    mStatusText.setText(text);
                }

            }
        });
    }


    private void saveStatus() {
        String status = mStatusText.getText().toString();

        //if no changes made, do nothing
        if (!changesMadeToStatus(status)) return;

        LSDKUser user = new LSDKUser(this);
        showProgress(true); //show  progress bar

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("status", status);

        //query server
        user.updateUserInfo(userInfo, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(EditStatusActivity.this);
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) { //good response
                    try {
                        LinuteUser user = new LinuteUser(new JSONObject(response.body().string())); //create container
                        persistData(user); //save data
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(EditStatusActivity.this);
                            }
                        });

                    } catch (JSONException e) { //error parsing data
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(EditStatusActivity.this);
                            }
                        });
                    }

                } else {
                    Log.e(TAG, response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(EditStatusActivity.this);
                        }
                    });
                }

                //hide progressbar
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
            }
        });
    }

    //checks if any changes were made to status
    //if no changes were made, we won't query server
    private boolean changesMadeToStatus(String status) {
        if (mSharedPreferences.getString("status", "").equals(status))
            return false;

        return true;
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
            mStatusText.setFocusableInTouchMode(focusable);
        } else {
            mStatusText.setFocusable(focusable);
        }
    }

    //save status to Shared Prefs
    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("status", user.getStatus()).commit();
    }
}
