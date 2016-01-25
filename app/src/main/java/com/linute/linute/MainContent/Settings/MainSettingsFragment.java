package com.linute.linute.MainContent.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;


public class MainSettingsFragment extends PreferenceFragmentCompat {
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
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.pref_profile_frag_main);

        bindPreferences();
        setOnClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null){
            activity.setTitle("Settings");
        }
    }

    private void bindPreferences() {
        mFindFriendsFacebook = findPreference("find_friends_facebook_pref");
        mFindFriendsContacts = findPreference("find_friends_contacts_pref");
        mEditProfileInfo = findPreference("edit_profile");
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
         */

    private static final int NEED_UPDATE_REQUEST = 1;

    private void setOnClickListeners() {
        //logout //TODO: unregister phone
        mLogOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //clear saved information
                Utils.resetUserInformation(getActivity()
                        .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).edit());
                Utils.deleteTempSharedPreference(getActivity()
                        .getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, Context.MODE_PRIVATE).edit());

                if (AccessToken.getCurrentAccessToken() != null) //log out facebook if logged in
                    LoginManager.getInstance().logOut();


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
                startActivityForResult(i, NEED_UPDATE_REQUEST);
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

        mChangePhoneNumber.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getActivity(), ChangePhoneActivity.class);
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

        mGiveFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@linute.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Linute Feedback");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "Replace this text with any feedback you'd like to give us!");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEED_UPDATE_REQUEST && resultCode == Activity.RESULT_OK){
            MainActivity activity = (MainActivity) getActivity();

            if (activity != null){
                //NOTE: do other fragments need reloading?
                activity.setFragmentOfIndexNeedsUpdating(true, MainActivity.FRAGMENT_INDEXES.PROFILE);
                activity.loadDrawerHeader();
            }
        }
    }
}
//}
