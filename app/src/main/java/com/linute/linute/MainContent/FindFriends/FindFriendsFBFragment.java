package com.linute.linute.MainContent.FindFriends;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linute.linute.R;

import java.util.Collections;
import java.util.HashMap;

/**
 * Created by QiFeng on 9/27/16.
 */
public class FindFriendsFBFragment extends BaseFindFriendsFragment {

    private CallbackManager mCallbackManager;
    private String mToken;

    @Override
    protected void setUpPresenter() {
        mFindFriendsSearchPresenter = new FindFriendsSearchPresenter(this, FindFriendsSearchPresenter.TYPE_FB);
    }

    @Override
    protected void initScreen() {
        mFindFriendsRationale.setVisibility(View.VISIBLE);
        mReloadButton.setVisibility(View.VISIBLE);

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            mFindFriendsRationale.setVisibility(View.VISIBLE);
            mReloadButton.setOnClickListener(new View.OnClickListener() {
                private boolean loaded = false;

                @Override
                public void onClick(View v) {

                    if (!loaded) {
                        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
                        mCallbackManager = CallbackManager.Factory.create();
                        setUpFacebookCallback();
                        loaded = true;
                    }

                    mFindFriendsRationale.setVisibility(View.GONE);
                    loginFacebook();
                }
            });
        } else {
            mFindFriendsRationale.setVisibility(View.GONE);
            if (mFriendFoundList.isEmpty()) {
                mEmptyText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void searchWithQuery(final String query) {
        mSearchHandler.removeCallbacksAndMessages(null);
        mSearchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null) return;
                mFindFriendsSearchPresenter.request(getContext(), getParams(query));
            }
        }, 300);
    }

    private void setUpFacebookCallback() {

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    showRationalTextFacebook();
                    return;
                }

                mFindFriendsRationale.setVisibility(View.GONE);

                mToken = loginResult.getAccessToken().getToken();

            }

            @Override
            public void onCancel() {
                if (getContext() == null) return;
                showRationalTextFacebook();
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                if (getContext() == null) return;
                showFacebookErrorToast();
                showRationalTextFacebook();
            }
        });
    }

    private void setUpFacebookList(){
        if (getContext() == null) return;
        mProgressBar.setVisibility(View.VISIBLE);
        mFindFriendsSearchPresenter.request(getContext(), getParams(""));
    }

    private void showRationalTextFacebook() {
        mFindFriendsRationale.setVisibility(View.VISIBLE);

        ((TextView) mFindFriendsRationale.findViewById(R.id.findFriends_rat_text)).setText(R.string.facebook_rationale);

        TextView mLogin = (TextView) mFindFriendsRationale.findViewById(R.id.findFriends_turn_on);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFacebook();
                mFindFriendsRationale.setVisibility(View.GONE);
            }
        });
    }

    public void loginFacebook() {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null && token.getPermissions().contains("user_friends") && !token.isExpired())
            setUpFacebookList();

        else
            LoginManager.getInstance().logInWithReadPermissions(FindFriendsFBFragment.this, Collections.singletonList("user_friends"));
    }

    private HashMap<String, Object> getParams(String q){
        HashMap<String, Object> param = new HashMap<>();
        param.put("fullName", q);
        return param;
    }

    private HashMap<String, Object> getParams(){
        HashMap<String, Object> param = new HashMap<>();
        param.put("token", mToken);
        return param;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showFacebookErrorToast() {
        Toast.makeText(getActivity(), R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }



}
