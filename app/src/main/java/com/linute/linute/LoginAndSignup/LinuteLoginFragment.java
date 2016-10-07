package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A login screen that offers login via email/password.
 */
public class LinuteLoginFragment extends Fragment {


    public static final String TAG = LinuteLoginFragment.class.getSimpleName();
    private boolean mCheckingCredentials = false;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private Button mSigninButton;

    private boolean mSafeForButtonAction = true;

    public LinuteLoginFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        mEmailView = (EditText) rootView.findViewById(R.id.signin_email_text);

        //set last logged in email
        mEmailView.append(getActivity().getSharedPreferences(
                LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("lastLoginEmail", ""));

        mPasswordView = (EditText) rootView.findViewById(R.id.signin_email_password_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.signin_progress_bar);

        mSigninButton = (Button) rootView.findViewById(R.id.log_in);
        mSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEmailView.getText().toString().isEmpty() && mPasswordView.getText().toString().length() >= 6) {
                    mSigninButton.setBackgroundResource(R.drawable.active_button);
                    mSigninButton.setTextColor(ContextCompat.getColor(mEmailView.getContext(), R.color.pure_white));
                } else {
                    mSigninButton.setBackgroundResource(R.drawable.inactive_button);
                    mSigninButton.setTextColor(ContextCompat.getColor(mEmailView.getContext(), R.color.secondaryColor));
                }
            }
        };

        mEmailView.addTextChangedListener(textWatcher);
        mPasswordView.addTextChangedListener(textWatcher);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSigninButton.callOnClick();
                }
                return false;
            }
        });

        rootView.findViewById(R.id.facebook_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null && mSafeForButtonAction) {
                    activity.selectedFacebookLogin();
                }
            }
        });

        rootView.findViewById(R.id.login_forgot_pass).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null && mSafeForButtonAction) {
                    activity.selectForgotPassword();
                }
            }
        });

        rootView.findViewById(android.R.id.home).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null && mSafeForButtonAction) {
                    activity.onBackPressed();
                }
            }
        });


        SwitchCompat devSwitch = (SwitchCompat) rootView.findViewById(R.id.dev_switch);
        if (API_Methods.IS_DEV_BUILD) {
            devSwitch.setVisibility(View.VISIBLE);
            devSwitch.setChecked(API_Methods.HOST.equals(API_Methods.HOST_LIVE));
            devSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Toast.makeText(getContext(), b ? "Live" : "Dev", Toast.LENGTH_SHORT).show();
                    API_Methods.HOST = b ? API_Methods.HOST_LIVE : API_Methods.HOST_DEV;
                    API_Methods.VERSION = b ? API_Methods.VERSION_LIVE : API_Methods.VERSION_DEV;
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, getContext().MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean("is_live", b).apply();
                }
            });
        }

        return rootView;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mCheckingCredentials || getActivity() == null) { //already checking credentials
            return;
        }
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showBadConnectionToast(getActivity());
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().toLowerCase().trim();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            cancel = true;
            mPasswordView.requestFocus();
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            cancel = true;
        }

        if (!cancel) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            checkCredentialsWithDB(email, password);
        }
    }


    private boolean isEmailValid(String email) {
        //NOTE: some old users still have non-edu emails
        //// TODO: 6/4/16 use regex
        // @.edu                        //hey@.edu          //hey.edu
        return !(email.startsWith("@") || email.contains("@.") || !email.contains("@") ||
                !email.contains(".") || email.contains(" "));
    }

    private boolean isPasswordValid(String password) {
        //longer than 5 and can't contain whitespace
        return password.length() >= 6 && !password.contains(" ");
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mSigninButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mSigninButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSigninButton.setVisibility(show ? View.GONE : View.VISIBLE);
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


        mCheckingCredentials = show;
        setFocusable(!show);
        mSafeForButtonAction = !show;
    }

    private void setFocusable(boolean focusable) {
        if (focusable) { //turn on
            mEmailView.setFocusableInTouchMode(true);
            mPasswordView.setFocusableInTouchMode(true);
        } else {
            mEmailView.setFocusable(false);
            mPasswordView.setFocusable(false);
        }
    }

    private void checkCredentialsWithDB(String email, final String password) {

        if (getActivity() == null) return;

        new LSDKUser(getActivity()).loginUserWithEmail(email, password, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(rFailedConnectionAction);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();
                Log.i(TAG, "onResponse: "+res);

                if (response.isSuccessful()) {
                    try {
                        JSONObject user = new JSONObject(res);
                        saveCredentials(res);
                        final PreLoginActivity activity = (PreLoginActivity) getActivity();
                        if (activity == null) return;
                        //occurs on first login after deactivating account
//                        if(user.has("isDeleted"))
                        final boolean reactivated = (user.has("isDeactivated") && "true".equals(user.getString("isDeactivated")));
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.goToNextActivity(reactivated);
                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Credentials weren't saved");
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                showProgress(false);
                            }
                        });
                    }
                } else if (response.code() == 404) { //bad credentials

                    try {
                        JSONObject obj = new JSONObject(res);
                        final String error = obj.getString("error");
                        //final boolean emailError = obj.getString("error").equals("email");

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                if (error.equals("email")) {
                                    mEmailView.setError("No account with this email");
                                    mEmailView.requestFocus();
                                } else if (error.equals("facebook")){
                                    mEmailView.setError("Please log in with facebook");
                                    mEmailView.requestFocus();
                                }else {
                                    mPasswordView.setError("Invalid password");
                                    mPasswordView.requestFocus();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                showProgress(false);
                            }
                        });
                    }

                } else {
                    //Log.e(TAG, "onResponse: "+res);
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getActivity());
                            showProgress(false);
                        }
                    });

                }
            }
        });
    }


    private Runnable rFailedConnectionAction = new Runnable() {
        @Override
        public void run() {
            showProgress(false);
            Utils.showBadConnectionToast(getActivity());
        }
    };


    private void saveCredentials(String responseString) throws JSONException {

        if (getActivity() == null) return;

        JSONObject response = new JSONObject(responseString);

        //Log.i(TAG, "saveCredentials: "+response.toString());

        SharedPreferences.Editor sharedPreferences = getActivity().
                getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit();

        try {
            JSONObject settings = response.getJSONObject("notificationSettings");
            sharedPreferences.putBoolean("notif_follow", getBooleanFromJSONObj("follow", settings));
            sharedPreferences.putBoolean("notif_message", getBooleanFromJSONObj("message", settings));
            sharedPreferences.putBoolean("notif_mention", getBooleanFromJSONObj("mention", settings));
            sharedPreferences.putBoolean("notif_alsoComment", getBooleanFromJSONObj("alsoComment", settings));
            sharedPreferences.putBoolean("notif_comment", getBooleanFromJSONObj("comment", settings));
            sharedPreferences.putBoolean("notif_like", getBooleanFromJSONObj("like", settings));
        } catch (JSONException e) {
            e.printStackTrace();
            sharedPreferences.putBoolean("notif_follow", true);
            sharedPreferences.putBoolean("notif_message", true);
            sharedPreferences.putBoolean("notif_mention", true);
            sharedPreferences.putBoolean("notif_alsoComment", true);
            sharedPreferences.putBoolean("notif_comment", true);
            sharedPreferences.putBoolean("notif_like", true);
        }


        LinuteUser user = new LinuteUser(response);

        sharedPreferences.putString("profileImage", user.getProfileImage());
        sharedPreferences.putString("userID", user.getUserID());
        sharedPreferences.putString("firstName", user.getFirstName());
        sharedPreferences.putString("lastName", user.getLastName());
        sharedPreferences.putString("status", user.getStatus());
        sharedPreferences.putString("dob", user.getDob());
        sharedPreferences.putInt("sex", user.getSex());
        sharedPreferences.putString("phone", user.getPhone());
        sharedPreferences.putString("collegeName", user.getCollegeName());
        sharedPreferences.putString("collegeId", user.getCollegeId());
        sharedPreferences.putString("campus", user.getCampus());
        sharedPreferences.putString("email", user.getEmail());
        sharedPreferences.putString("lastLoginEmail", user.getEmail());
        sharedPreferences.putString("userName", user.getUserName());
        sharedPreferences.putString("points", user.getPoints());
        sharedPreferences.putString("userToken", user.getUserToken());
        sharedPreferences.putString("socialFacebook", user.getSocialFacebook());
        sharedPreferences.putBoolean("isLoggedIn", true);
        sharedPreferences.apply();
    }

    public boolean getBooleanFromJSONObj(String key, JSONObject obj) {
        try {
            return obj.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mEmailView.hasFocus()) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
        } else if (mPasswordView.hasFocus()) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
        }
    }
}

