package com.linute.linute.MainContent.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EditBirthdayFragment extends Fragment {

    private static final String TAG = "EditBirthdayFragment";
    private ProgressBar mProgressBar;
    private DatePicker mDatePicker;
    private Button mSaveButton;

    private String mDob;

    private SharedPreferences mSharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_birthday, container, false);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        ((EditProfileInfoActivity)getActivity()).setTitle("Birthday");
        bindViews(rootView);
        setDefaultValues();
        setUpOnClickListeners();

        return rootView;
    }

    private void bindViews(View rootView) {
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.editbirthday_progressbar);
        mDatePicker = (DatePicker) rootView.findViewById(R.id.editbirthday_datepicker);
        mSaveButton = (Button) rootView.findViewById(R.id.editbirthday_save_button);
    }

    private void setDefaultValues() {
        String dob = mSharedPreferences.getString("dob", "");

        Calendar c = Calendar.getInstance();

        //try to set date picker to person's birthday
        try {
            if (!dob.equals("null")) {
                c.setTime(Utils.DATE_FORMAT.parse(dob));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        mDob = formatDateFromInts(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
    }


    private void setUpOnClickListeners() {
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBirthday();
            }
        });
    }


    public String formatDateFromInts(int year, int month, int day) {
        String date = year + "-";
        month++; //month is in range 0-11 so we need to add one
        date += (month < 10 ? "0" + month : month) + "-";
        date += day < 10 ? "0" + day : day;
        return date;
    }

    private void saveBirthday() {

        final String dob = formatDateFromInts(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());

        if (!birthdayHasBeenEditted(dob))
            return;

        LSDKUser user = new LSDKUser(getActivity());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("dob", dob);
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
                        mDob = dob;

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

    private void persistData(LinuteUser user) {
        mSharedPreferences.edit().putString("dob", user.getDob()).apply();
    }

    private boolean birthdayHasBeenEditted(String birthday) {
        return !mDob.equals(birthday);
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


        mDatePicker.setClickable(!show); //don't allow edit when querying
    }

}
