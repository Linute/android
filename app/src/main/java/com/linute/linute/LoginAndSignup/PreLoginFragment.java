package com.linute.linute.LoginAndSignup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.R;

/**
 * Created by QiFeng on 2/6/16.
 */
public class PreLoginFragment extends Fragment {

    private View mFacebookLogin;
    private View mTaptLogin;

    public PreLoginFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_prelogin, container, false);

        mFacebookLogin =  rootView.findViewById(R.id.preLogin_facebook_login);
        mTaptLogin = rootView.findViewById(R.id.prelogin_linute_login);

        mFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null){
                    activity.selectedFacebookLogin();
                }
            }
        });

        mTaptLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreLoginActivity activity = (PreLoginActivity) getActivity();
                if (activity != null){
                    activity.selectedTaptLogin();
                }
            }
        });

        return rootView;
    }
}
