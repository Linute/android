package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

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

public class EditGenderFragment extends Fragment {

    private static final String TAG = "EditGenderFragment";
    private Spinner mSpinner;

    private Button mSaveButton;

    private ProgressBar mProgressBar;

    private SharedPreferences mSharedPreferences;

    private int mSavedGender = 0; //gender saved to sharedPref

    //List of choices
    private String[] mGenders = {"Not Specified", "Male", "Female"};


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_gender, container, false);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        ((EditProfileInfoActivity)getActivity()).setTitle("Sex");
        bindViews(rootView);
        setUpSpinner();
        setDefaultValues();
        setUpOnClickListeners();

        return rootView;
    }



    private void bindViews(View rootView) {
        mSpinner = (Spinner) rootView.findViewById(R.id.editgender_spinner);
        mSaveButton = (Button) rootView.findViewById(R.id.editgender_save_button);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.editgender_progressbar);
    }

    private void setUpSpinner() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        getActivity(),
                        android.R.layout.simple_spinner_item,
                        mGenders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
    }

    private void setDefaultValues() {
        mSavedGender = mSharedPreferences.getInt("sex", 0);
        mSpinner.setSelection(mSavedGender);
    }

    private void setUpOnClickListeners() {

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGender();
            }
        });
    }

    private void saveGender() {
        final int gender = mSpinner.getSelectedItemPosition();

        //gender hasn't been editted
        if (!genderEditted(gender))
            return;

        LSDKUser user = new LSDKUser(getActivity());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sex", gender + "");

        showProgress(true);

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
                        persistData(user);
                        mSavedGender = gender;

                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(getActivity());
                                showProgress(false);

                                getFragmentManager().popBackStack();
                            }
                        });
                    } catch (JSONException e) { //caught error
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

                } else { //log error and show server error
                    Log.e(TAG, response.body().string());
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

    private boolean genderEditted(int gender) {
        return gender != mSavedGender;
    }

    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putInt("sex", user.getSex()).apply();
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
        mSpinner.setClickable(!show); //don't allow edit when querying
    }

    public static String getGenderFromIndex(int position) {
        switch (position) {
            case 0:
                return "Not Specified";
            case 1:
                return "Male";
            case 2:
                return "Female";
            default:
                return "Not Specified";
        }
    }

}
