package com.linute.linute.MainContent.Settings;

import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;


public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    //use a variable to keep track of if user made changes to account info
    //if user did change account information, let the parent "ProfileFragment" know, so it can update info
    public static String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        //get toolbar
        setUpToolbar();

        getFragmentManager().beginTransaction().replace(R.id.setting_fragment, new LinutePreferenceFragment()).commit();
    }

    private void setUpToolbar(){
        mToolBar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Settings");

        mToolBar.setNavigationIcon(R.drawable.ic_back);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    //fragment with our settings layout
    public static class LinutePreferenceFragment extends PreferenceFragment
    {
        Preference mFindFriendsFacebook;
        Preference mFindFriendsContacts;
        Preference mEditProfileInfo;
        Preference mChangeEmail;
        Preference mChangePhoneNumber;
        Preference mTalkToUs;
        Preference mGiveFeedback;
        Preference mPrivacyPolicy;
        Preference mTermsOfService;
        Preference mLogOut;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_profile_frag_main);

            bindPreferences();
            setOnClickListeners();
        }

        private void bindPreferences(){
            mFindFriendsFacebook = findPreference("find_friends_facebook_pref");
            mFindFriendsContacts = findPreference("find_friends_contacts_pref");
            mEditProfileInfo =  findPreference("edit_profile");
            mChangeEmail = findPreference("change_email");
            mChangePhoneNumber = findPreference("change_phone_number");
            mTalkToUs = findPreference("talk_to_us");
            mGiveFeedback = findPreference("give_feedback");
            mPrivacyPolicy = findPreference("privacy policy");
            mTermsOfService = findPreference("terms_of_service");
            mLogOut = findPreference("logout");
        }




        /* TODO: still need to add the following on click listeners:

                mFindFriendFacebook
                mFindFriendsContacts
                mTalkToUs
                mGiveFeedback
                mPrivacyPolicy
                mTermsOfService

                mEditPublic
                mEditPrivate
                mChangeEmail
                mChangePhoneNumber
         */

        private void setOnClickListeners(){
            //logout
            mLogOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //clear saved information
                    Utils.resetUserInformation(getActivity()
                            .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit());
                    //start new
                    Intent i = new Intent(getActivity(), PreLoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //don't let them come back
                    startActivity(i);
                    getActivity().finish();
                    return true;
                }
            });

            mEditProfileInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), EditProfileInfoActivity.class);
                    startActivity(i);
                    return true;
                }
            });

            mChangeEmail.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), ChangeEmailActivity.class);
                    startActivity(i);
                    return true;
                }
            });


            //privacy policy
            //FIXME : open in browser
            mPrivacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), PrivacyPolicyActivity.class);
                    startActivity(i);
                    return true;
                }
            });


            //terms of service
            mTermsOfService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), TermsOfServiceActivity.class);
                    startActivity(i);
                    return true;
                }
            });

        }
    }
}
