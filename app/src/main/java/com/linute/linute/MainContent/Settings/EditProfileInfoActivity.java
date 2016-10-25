package com.linute.linute.MainContent.Settings;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;


public class EditProfileInfoActivity extends BaseSocketActivity {

    public static final String TAG = "EditProfileInfoActivity";


    private Toolbar mToolbar;

    private boolean mMainActivityNeedsToUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setUpToolBar();

        if (savedInstanceState == null) {
            LinuteEditProfileFragment profileEditor = new LinuteEditProfileFragment();
            getFragmentManager().beginTransaction().replace(R.id.setting_fragment, profileEditor).commit();
        }
    }

    private void setUpToolBar() {

        mToolbar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(mToolbar);

    }

    public void setTitle(String title){
        mToolbar.setTitle(title);
    }


    private static final String NEED_UPDATE_KEY = "need_update";
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(NEED_UPDATE_KEY, mMainActivityNeedsToUpdate);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMainActivityNeedsToUpdate = savedInstanceState.getBoolean(NEED_UPDATE_KEY);
    }

    //override up button to go back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStack();
        }
        else {
            setResult(mMainActivityNeedsToUpdate ? RESULT_OK : RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    public void addFragmentToStack(Fragment fragment){
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.setting_fragment, fragment)
                .addToBackStack("setting")
                .commit();
    }

    public void setMainActivityNeedsToUpdate(boolean needsToUpdate){
        mMainActivityNeedsToUpdate = needsToUpdate;
    }

    //fragment with our settings layout
    public static class LinuteEditProfileFragment extends PreferenceFragment
    {
        private Preference mEditName;
        private Preference mEditStatus;
        private Preference mDob;
        private Preference mEditGender;
        private Preference mPhoto;
        private SharedPreferences mSharedPreferences;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_edit_profile);

            mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

            bindPreferences();
            setOnClickListeners();
        }

        @Override
        public void onResume() {
            super.onResume();
            ((EditProfileInfoActivity)getActivity()).setTitle("Edit Profile");
            setSummaries();
        }


        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        private void bindPreferences() {
            mEditName = findPreference("edit_name_pref");
            mEditStatus = findPreference("edit_status_pref");
            mDob = findPreference("edit_birthday_pref");
            mEditGender = findPreference("edit_sex_pref");
            mPhoto = findPreference("edit_photo_pref");
        }

        //TODO: FIX
        private void setOnClickListeners(){
            final EditProfileInfoActivity activity = (EditProfileInfoActivity) getActivity();

            if(activity == null) return;

            mEditName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    activity.addFragmentToStack(new EditNameFragment());
                    return true;
                }
            });

            mEditStatus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    activity.addFragmentToStack(new EditStatusFragment());
                    return true;
                }
            });

            mDob.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    activity.addFragmentToStack(new EditBirthdayFragment());
                    return true;
                }
            });

            mEditGender.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    activity.addFragmentToStack(new EditGenderFragment());
                    return true;
                }
            });

            mPhoto.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    activity.addFragmentToStack(new ChangeProfileImageFragment());
                    return true;
                }
            });

        }

        private void setSummaries(){
            if (mSharedPreferences == null) return;
            mEditName.setSummary(mSharedPreferences.getString("firstName", " ")
                    + " " + mSharedPreferences.getString("lastName", " "));


            String status = mSharedPreferences.getString("status", "");
            mEditStatus.setSummary(status.equals("") ? "Tell us about yourself" : status);


            mPhoto.setSummary("Change your profile picture");

            String dob = mSharedPreferences.getString("dob", "");
            if (!dob.isEmpty() && !dob.equals("null"))
                dob = Utils.formatDateToReadableString(dob);
            else dob = "Tell us your birthday";
            mDob.setSummary(dob);

            mEditGender.setSummary(EditGenderFragment.getGenderFromIndex(mSharedPreferences.getInt("sex", 0)));
        }
    }


}
