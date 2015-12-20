package com.linute.linute.MainContent.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

public class EditProfileInfoActivity extends AppCompatActivity {

    public static final String TAG = "EditProfileInfoActivity";

    private Toolbar mToolbar;

    private SharedPreferences mSharedPreferences;

    private boolean mEdittedProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

        setUpToolBar();

        LinuteEditProfileFragment profileEditor = new LinuteEditProfileFragment();
        profileEditor.setDefault(mSharedPreferences);

        getFragmentManager().beginTransaction().replace(R.id.setting_fragment, profileEditor).commit();
    }

    private void setUpToolBar() {

        mToolbar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        mToolbar.setTitle("Edit Profile");

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(mToolbar);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(this.getClass().toString(), mEdittedProfile);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            mEdittedProfile = savedInstanceState.getBoolean(this.getClass().toString());
        }
    }



    //fragment with our settings layout
    public static class LinuteEditProfileFragment extends PreferenceFragment
    {
        private Preference mEditName;
        private Preference mEditStatus;
        private Preference mDob;
        private SharedPreferences mSharedPreferences;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_edit_profile);


            bindPreferences();
            setOnClickListeners();
            setSummaries();

            setUpPrefChangeListeners();
        }

        public void setDefault(SharedPreferences sharedPreferences){
            mSharedPreferences = sharedPreferences;
        }



        private void bindPreferences(){
            mEditName = (Preference) findPreference("edit_name_pref");
            mEditStatus = (Preference) findPreference("edit_status_pref");
            mDob = (Preference) findPreference("edit_birthday_pref");
        }

        //checks if preferences where changed
        private void setUpPrefChangeListeners(){
            mSharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("firstName") || key.equals("lastName"))
                        mEditName.setSummary(sharedPreferences.getString("firstName", "") + " " + sharedPreferences.getString("lastName", ""));
                    else if (key.equals("status"))
                        mEditStatus.setSummary(sharedPreferences.getString("status", ""));
                    else if (key.equals("dob"))
                        mDob.setSummary(Utils.formatToReadableString(sharedPreferences.getString("dob", "")));

                }
            });
        }


        private void setOnClickListeners(){

            mEditName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), EditNameActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    getActivity().overridePendingTransition(0,0); //NOTE: FIX
                    return true;
                }
            });

            mEditStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), EditStatusActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    getActivity().overridePendingTransition(0, 0); //NOTE: FIX
                    return true;
                }
            });

            mDob.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), EditBirthdayActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    getActivity().overridePendingTransition(0, 0); //NOTE: FIX
                    return true;
                }
            });

        }

        private void setSummaries(){
            mEditName.setSummary(mSharedPreferences.getString("firstName", " ")
                    + " " + mSharedPreferences.getString("lastName", " "));

            String status = mSharedPreferences.getString("status", "");
            mEditStatus.setSummary(status.equals("") ? "Tell us about yourself" : status);
        }

    }

}
