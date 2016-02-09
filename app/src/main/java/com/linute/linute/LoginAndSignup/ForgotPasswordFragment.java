package com.linute.linute.LoginAndSignup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by QiFeng on 2/6/16.
 */
public class ForgotPasswordFragment extends Fragment {

    public final String TAG = ForgotPasswordFragment.class.getSimpleName();

    private ViewFlipper mViewFlipper;

    private String mPinCode;
    private String mEmailString;

    private int mCurrentViewFlipperIndex;

    private ProgressBar mProgressBar1;
    private ProgressBar mProgressBar2;
    private ProgressBar mProgressBar3;

    private EditText mEmailView;
    private EditText mPinCodeView;
    private EditText mPasswordView;

    //layer 1
    private Button mNextButton;

    //layer 2
    private View mLayerTwoButtons;

    //layer 3
    private Button mChangePassButton;

    private TextView mEmailConfirmTextView;

    private String mTempPassword;

    private String mUserID;

    public ForgotPasswordFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        mViewFlipper = (ViewFlipper) rootView.findViewById(R.id.forgotPass_view_flipper);
        mPasswordView = (EditText) rootView.findViewById(R.id.forgotPas_password_text);

        setUpViewFlipper(rootView);
        bindViews(rootView);

        setUpOnClickListeners();

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mViewFlipper.getDisplayedChild() != mCurrentViewFlipperIndex)
            mViewFlipper.setDisplayedChild(mCurrentViewFlipperIndex);
    }

    private void setUpViewFlipper(View root) {
        mViewFlipper.setInAnimation(getActivity(), R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(getActivity(), R.anim.slide_out_left);
    }

    private void setUpOnClickListeners() {
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmailAndGetPinCode();
            }
        });

        //attempt to sign up when button pressed
        mChangePassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo();
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("mSavedEmail", mEmailString);
        outState.putString("mSavedPin", mPinCode);
        outState.putInt("mCurrentFlipperIndex", mCurrentViewFlipperIndex);
        outState.putString("mPassword", mTempPassword);
        outState.putString("mUserID", mUserID);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mPinCode = savedInstanceState.getString("mSavedPin");
            mCurrentViewFlipperIndex = savedInstanceState.getInt("mCurrentFlipperIndex");
            mEmailString = savedInstanceState.getString("mSavedEmail");
            mTempPassword = savedInstanceState.getString("mPassword");
            mUserID = savedInstanceState.getString("mUserID");
            if (mEmailString != null) {
                mEmailView.setText(mEmailString);
                mEmailConfirmTextView.setText(mEmailString);
            }
        }
    }


    private void bindViews(View root) {
        mEmailView = (EditText) root.findViewById(R.id.forgot_password_email_text);

        mProgressBar1 = (ProgressBar) root.findViewById(R.id.forgotPass_progress_bar0);
        mProgressBar2 = (ProgressBar) root.findViewById(R.id.forgotPass_progress_bar1);
        mProgressBar3 = (ProgressBar) root.findViewById(R.id.forgotPass_progress_bar2);

        mNextButton = (Button) root.findViewById(R.id.forgot_password_verify_email_button);
        mLayerTwoButtons = root.findViewById(R.id.forgotPass_code_verify_buttons);

        mPinCodeView = (EditText) root.findViewById(R.id.forgotPass_verify_code);

        mEmailConfirmTextView = (TextView) root.findViewById(R.id.forgotPass_email_confirm_text_view);

        mChangePassButton = (Button) root.findViewById(R.id.forgotPass_change_password);


        root.findViewById(R.id.forgotPass_enter_new_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterNewEmail();
            }
        });


        root.findViewById(R.id.forgotPass_verify_code_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
    }


    public void enterNewEmail() {
        setToGoBackAnimation(true); //change animations
        mPinCodeView.setText("");
        mViewFlipper.showPrevious();
        mCurrentViewFlipperIndex--;
        mEmailString = null;
        mPinCode = null;
        setToGoBackAnimation(false);  //change animations
    }


    public void verifyCode() {
        showProgress(true, 1);

        if (mPinCode != null && mPinCodeView.getText().toString().equals(mPinCode)) {
            mViewFlipper.showNext();
            mCurrentViewFlipperIndex++;
        } else {
            mPinCodeView.setError("Invalid code");
            mPinCodeView.requestFocus();
        }

        showProgress(false, 1);
    }

    private void setToGoBackAnimation(boolean goBack) {
        if (getActivity() == null) return;
        mViewFlipper.setInAnimation(getActivity(), goBack ? R.anim.slide_in_left : R.anim.slide_in_right);
        mViewFlipper.setOutAnimation(getActivity(), goBack ? R.anim.slide_out_right : R.anim.slide_out_left);

    }


    private void showProgress(final boolean show, final int currentViewIndex) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final View progressBar;
        final View button;

        switch (currentViewIndex) {
            case 0:
                progressBar = mProgressBar1;
                button = mNextButton;
                break;
            case 1:
                progressBar = mProgressBar2;
                button = mLayerTwoButtons;
                break;
            case 2:
                progressBar = mProgressBar3;
                button = mChangePassButton;
                break;
            default:
                return;
        }

        button.setVisibility(show ? View.GONE : View.VISIBLE);
        button.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                button.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    private void checkEmailAndGetPinCode() {

        final String email = mEmailView.getText().toString().trim();

        if (checkEmail(email)) {

            mEmailString = email;
            mEmailConfirmTextView.setText(mEmailString);

            if (getActivity() == null) return;

            showProgress(true, 0);

            new LSDKUser(getActivity()).resetPassword(email, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    failedConnectionWithCurrentView(0);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            mTempPassword = jsonObject.getString("tempPassword");
                            mPinCode = jsonObject.getString("pinCode");

                            mUserID = jsonObject.getJSONObject("user").getString("id");

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewFlipper.showNext();
                                        mCurrentViewFlipperIndex++;
                                        showProgress(false, 0);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgress(false, 0);
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                        }
                    } else {
                        Log.i(TAG, "onResponse: "+response.body().string());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mEmailView.setError("No account with this email");
                                    showProgress(false, 0);
                                }
                            });
                        }
                    }
                }
            });
        } else {
            showProgress(false, 0);
        }
    }


    private boolean checkEmail(String emailString) {
        //if empty return false
        if (TextUtils.isEmpty(emailString)) {
            mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.requestFocus();
            return false;
        }

        //invalid email
        else if (!emailString.contains("@") || emailString.startsWith("@") || emailString.contains("@.")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mEmailView.requestFocus();
            return false;
        }

        /*TODO: uncomment this
        //not edu email
        else if (!emailString.endsWith(".edu")){
            mEmailView.setError("Must be a valid edu email");
            mEmailView.requestFocus();
            return false;
        }
        */

        //good email
        else {
            return true;
        }
    }


    private boolean isPasswordValid(String password) {
        //longer than 5 and doesn't contain whitespace
        return password.length() >= 6 && !password.contains(" ");
    }


    private void serverErrorCurrentView(final int index) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showServerErrorToast(getActivity());
            }
        });


    }

    private void failedConnectionWithCurrentView(final int index) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false, index);
                Utils.showBadConnectionToast(getActivity());
            }
        });
    }


    private void updateUserInfo() {
        final String password = mPasswordView.getText().toString();

        if (isPasswordValid(password)) {
            if (getActivity() == null) return;

            showProgress(true, 2);
            new LSDKUser(getActivity()).changePassword(mTempPassword, mEmailString, mUserID, password, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    if (getActivity() == null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                                showProgress(false, 2);
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()){
                        Log.i(TAG, "onResponse: "+response.body().string());
                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false, 2);
                                    Toast.makeText(getActivity(), "Password changed", Toast.LENGTH_LONG).show();
                                    getFragmentManager().popBackStack();
                                }
                            });
                        }
                    }else {
                        Log.i(TAG, "onResponse: "+response.body().string());
                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false, 2);
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mEmailView.hasFocus()){
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mEmailView.getWindowToken(), 0);
        }
    }
}
