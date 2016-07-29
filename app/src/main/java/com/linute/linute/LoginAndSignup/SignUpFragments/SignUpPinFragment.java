package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by QiFeng on 7/29/16.
 */
class SignUpPinFragment extends Fragment {

    public static final String TAG = SignUpPinFragment.class.getSimpleName();
    public static final String PIN_KEY = "pin_key_sign_up";


    public static SignUpPinFragment newInstance(String pin){
        SignUpPinFragment fragment = new SignUpPinFragment();
        Bundle arg = new Bundle();
        arg.putString(PIN_KEY, pin);
        fragment.setArguments(arg);
        return fragment;
    }



}
