package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 7/29/16.
 */
public class SignUpPinFragment extends Fragment {

    public static final String TAG = SignUpPinFragment.class.getSimpleName();
    public static final String PIN_KEY = "pin_key_sign_up";

    private Button vResend;
    private EditText vPinCode;

    private boolean mCanResend = true;
    private int mTime = 30;

    private ButtonCountDownTimer mButtonCountDownTimer;

    private String mPincode;

    private Button vConfirm;

    private SignUpInfo mSignUpInfo;


    public static SignUpPinFragment newInstance(String pin){
        SignUpPinFragment fragment = new SignUpPinFragment();
        Bundle arg = new Bundle();
        arg.putString(PIN_KEY, pin);
        fragment.setArguments(arg);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            mPincode = getArguments().getString(PIN_KEY, null);
        }else {
            mPincode = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_pin_code, container, false);
        
        mSignUpInfo = ((SignUpParentFragment) getParentFragment()).getSignUpInfo();
        vResend = (Button) root.findViewById(R.id.resend);

        vConfirm = (Button) root.findViewById(R.id.confirm);
        vPinCode = (EditText) root.findViewById(R.id.pincode);

        if (mButtonCountDownTimer != null) mButtonCountDownTimer.cancel();

        if (vPinCode.getText().length() < 4) {
            mButtonCountDownTimer = new ButtonCountDownTimer(30000, 1000);
            mButtonCountDownTimer.start();
        }else {
            vConfirm.setVisibility(View.VISIBLE);
            vResend.setVisibility(View.GONE);
        }

        vResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCanResend){
                    resendPin();
                }
            }
        });

        vConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPin();
            }
        });


        vPinCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4){
                    vResend.setVisibility(View.GONE);
                    vConfirm.setVisibility(View.VISIBLE);
                }else {
                    vResend.setVisibility(View.VISIBLE);
                    vConfirm.setVisibility(View.GONE);
                }
            }
        });

        vPinCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    checkPin();
                }
                return false;
            }
        });

        return root;
    }


    private void checkPin(){
        vPinCode.setError(null);
        if (mPincode != null && vPinCode.getText().toString().equals(mPincode)){
            SignUpParentFragment frag = (SignUpParentFragment) getParentFragment();
            if (frag != null){
                frag.addFragment(new SignUpProfilePicture(), SignUpProfilePicture.TAG);
            }
        }else {
            vPinCode.setError("Invalid pin");
            vPinCode.requestFocus();
        }
    }


    public void resendPin(){
        if (getContext() == null) return;

        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mButtonCountDownTimer != null) mButtonCountDownTimer.cancel();
                    mButtonCountDownTimer = new ButtonCountDownTimer(30000, 1000);
                    mButtonCountDownTimer.start();
                    Toast.makeText(getActivity(), "Pincode Resent", Toast.LENGTH_SHORT).show();
                }
            });
        }

        new LSDKUser(getActivity()).getConfirmationCodeForEmail(mSignUpInfo.getEmail(), mSignUpInfo.getFirstName(), mSignUpInfo.getLastName(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getContext());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String stringResp = response.body().string();
                        mPincode = (new JSONObject(stringResp).getString("pinCode"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getContext());
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getContext());
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard();
    }

    private void hideKeyboard(){
        if (getActivity() != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(vPinCode.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mButtonCountDownTimer != null) mButtonCountDownTimer.cancel();
    }


    public class ButtonCountDownTimer extends CountDownTimer {
        public ButtonCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
            mCanResend = false;
            vResend.setText("Resend in " + mTime);
        }

        @Override
        public void onFinish() {
            vResend.setText("Resend pin");
            mCanResend = true;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mTime = (int)(millisUntilFinished / 1000);
            vResend.setText("Resend in "+mTime);
        }
    }

}
