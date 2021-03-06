package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.R;

/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpParentFragment extends Fragment {


    public final static String TAG = SignUpParentFragment.class.getSimpleName();
    public static final String SIGN_UP_INFO = "sign_up_info";
    public static final String INFO = "parent_info";
    private SignUpInfo mSignUpInfo;

    private Toolbar vToolbar;

    public static SignUpParentFragment newInstance(SignUpInfo signUpInfo) {
        SignUpParentFragment fragment = new SignUpParentFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SIGN_UP_INFO, signUpInfo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INFO, mSignUpInfo);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            mSignUpInfo = savedInstanceState.getParcelable(INFO);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mSignUpInfo == null) {
            if (getArguments() != null) {
                mSignUpInfo = getArguments().getParcelable(SIGN_UP_INFO);
                if (mSignUpInfo == null)
                    mSignUpInfo = new SignUpInfo();
            }else mSignUpInfo = new SignUpInfo();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PreLoginActivity activity = (PreLoginActivity) getActivity();
        if (activity != null) activity.setOnBackPressed(new PreLoginActivity.OnBackPressed() {
            @Override
            public void onBack() {
                backPressed();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        PreLoginActivity activity = (PreLoginActivity) getActivity();
        if (activity != null) activity.setOnBackPressed(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_parent, container, false);

        vToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        vToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPressed();
            }
        });

        if (getChildFragmentManager().getBackStackEntryCount() == 0) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SignUpNameFragment())
                    .commit();
        }

        return root;
    }

    public SignUpInfo getSignUpInfo() {
        return mSignUpInfo;
    }


    public void addFragment(Fragment fragment, String tag) {
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }


    public void addToTop(Fragment fragment, String tag){
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.frag_fade_in, R.anim.hold, R.anim.hold, R.anim.frag_fade_out)
                .add(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    public void showToolbar(boolean show){
        vToolbar.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    public void backPressed(){
        if (getChildFragmentManager().getBackStackEntryCount() == 0) {
            getFragmentManager().popBackStack();
        }
        else {
            getChildFragmentManager().popBackStack();
        }
    }
}
