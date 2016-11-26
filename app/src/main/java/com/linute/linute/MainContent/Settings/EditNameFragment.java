package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
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

public class EditNameFragment extends Fragment {

    public static final String TAG = EditNameFragment.class.getSimpleName();

    private EditText mFirstName;
    private EditText mLastName;
    private SharedPreferences mSharedPreferences;
    private Button mSaveButton;
    private ProgressBar mProgressBar;

    public EditNameFragment(){

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_name, container, false);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        ((EditProfileInfoActivity)getActivity()).setTitle("Name");
        bindView(rootView);
        setDefaultValues();
        setUpOnClickListeners();

        return rootView;
    }

    private void setDefaultValues() {
        mFirstName.append(mSharedPreferences.getString("firstName", ""));
        mLastName.append(mSharedPreferences.getString("lastName", ""));
    }

    private void bindView(View rootView) {
        mFirstName = (EditText) rootView.findViewById(R.id.prof_edit_fname_text);
        mLastName = (EditText) rootView.findViewById(R.id.prof_edit_lname_text);
        InputFilter[] inputFilters = {new InputFilter.LengthFilter(16)};

        mFirstName.setFilters(inputFilters); //set char limit to 16
        mLastName.setFilters(inputFilters);

        mSaveButton = (Button) rootView.findViewById(R.id.editname_save_button);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.prof_edit_name_progressbar);
    }

    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveName();
            }
        });
    }


    private boolean areValidFields(String firstName, String lastName) {
        boolean areValid = true;
        //no changes made
        if (lastName.equals(mSharedPreferences.getString("lastName", "")) &&
                firstName.equals(mSharedPreferences.getString("firstName", ""))) {

            //if unchanged, just say we saved it
            if (getActivity() != null)
                Utils.showSavedToast(getActivity());

            return false;
        }

        if (lastName.isEmpty()) {
            mLastName.setError(getString(R.string.error_field_required));
            mLastName.requestFocus();
            areValid = false;
        }
        if (firstName.isEmpty()) {
            mFirstName.setError(getString(R.string.error_field_required));
            mFirstName.requestFocus();
            areValid = false;
        }
        return areValid;
    }

    private void saveName() {
        String lastName = mLastName.getText().toString().trim();
        String firstName = mFirstName.getText().toString().trim();

        if (areValidFields(firstName, lastName)) {
            LSDKUser user = new LSDKUser(getActivity());
            showProgress(true);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("firstName", firstName);
            userInfo.put("lastName", lastName);
            user.updateUserInfo(userInfo, null, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            showProgress(false);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            LinuteUser user = new LinuteUser(new JSONObject(response.body().string()));
                            saveInfo(user);

                            final EditProfileInfoActivity activity = (EditProfileInfoActivity) getActivity();
                            if (activity == null) return;

                            //we changed name, we will need to update things in MainActivity
                            activity.setMainActivityNeedsToUpdate(true);

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showSavedToast(getActivity());
                                    showProgress(false);

                                    getFragmentManager().popBackStack(); //pop this fragment
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
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
                        Log.i(TAG, "onResponse: "+response.body().string());
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
            mFirstName.setFocusableInTouchMode(true);
            mLastName.setFocusableInTouchMode(true);
        } else {
            mFirstName.setFocusable(false);
            mLastName.setFocusable(false);
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        if (getActivity() != null){
            View v = getActivity().getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    private void saveInfo(LinuteUser user) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.apply();
    }

}
