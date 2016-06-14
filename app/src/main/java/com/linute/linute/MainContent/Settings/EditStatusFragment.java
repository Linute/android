package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class EditStatusFragment extends Fragment {

    private static final String TAG = "EditStatusFragment";
    private SharedPreferences mSharedPreferences;

    private ProgressBar mProgressBar;
    private EditText mStatusText;
    private Button mSaveButton;
    private TextView mCharCountTV;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_status, container, false);

        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        ((EditProfileInfoActivity)getActivity()).setTitle("Status");
        bindView(rootView);
        setDefaultValues();
        setUpOnClickListeners();
        setUpEditTextMaxLines();


        return rootView;
    }

    private void bindView(View rootView) {
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.editstatus_progressbar);
        mSaveButton = (Button) rootView.findViewById(R.id.editstatus_save);
        mStatusText = (EditText) rootView.findViewById(R.id.editstatus_status_text);
        mCharCountTV = (TextView) rootView.findViewById(R.id.text_char_count);
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

    private static final int MAX_CHARACTERS = 200;

    //sets max line number to 3
    private void setUpEditTextMaxLines() {
        updateCharCountView(mStatusText.getText());
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
                if(s.length() > MAX_CHARACTERS){
                    mStatusText.setText(text);
                }
                updateCharCountView(s);
            }
        });
    }


    private void updateCharCountView(Editable s){
        mCharCountTV.setText(String.valueOf(MAX_CHARACTERS-s.length()));
        mCharCountTV.setTextColor((s.length() >= MAX_CHARACTERS ? 0xFFCC0000 : 0xFFCCCCCC));
    }

    private void saveStatus() {
        String status = mStatusText.getText().toString();

        //if no changes made, do nothing
        if (!changesMadeToStatus(status)) return;

        LSDKUser user = new LSDKUser(getActivity());
        showProgress(true); //show  progress bar

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("status", status);

        //query server
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
                if (response.isSuccessful()) { //good response
                    try {
                        LinuteUser user = new LinuteUser(new JSONObject(response.body().string())); //create container
                        persistData(user); //save data

                        final EditProfileInfoActivity activity = (EditProfileInfoActivity) getActivity();
                        if (activity == null) return;

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showSavedToast(activity);
                                showProgress(false);

                                activity.setMainActivityNeedsToUpdate(true); //let's us know we need to update MainActivity
                                getFragmentManager().popBackStack();
                            }
                        });

                    } catch (JSONException e) { //error parsing data
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
                    Log.e(TAG, response.body().string());
                    if(getActivity() == null) return;
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

    //checks if any changes were made to status
    //if no changes were made, we won't query server
    private boolean changesMadeToStatus(String status) {
        return !mSharedPreferences.getString("status", "").equals(status);

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
            mStatusText.setFocusableInTouchMode(true);
        } else {
            mStatusText.setFocusable(false);
        }
    }

    //save status to Shared Prefs
    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("status", user.getStatus()).apply();
    }
}
