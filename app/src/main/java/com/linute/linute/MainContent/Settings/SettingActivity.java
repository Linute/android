package com.linute.linute.MainContent.Settings;

import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;


public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    //use a variable to keep track of if user made changes to account info
    //if user did change account information, let the parent "ProfileFragment" know, so it can update info
    public static String TAG = "SettingActivity";

    public static final int NEED_UPDATE_REQUEST = 7;

    private boolean mMainActivityNeedsToUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_settings);

        //get toolbar
        setUpToolbar();

        getFragmentManager().beginTransaction().replace(R.id.setting_fragment, new LinutePreferenceFragment()).commit();
    }

    private void setUpToolbar() {
        mToolBar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        mToolBar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolBar.setTitle("Settings");

        setSupportActionBar(mToolBar);
    }

    private static final String NEED_UPDATE_KEY = "need_updating";

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

    @Override
    public void onBackPressed() {
        setResult(mMainActivityNeedsToUpdate ? RESULT_OK : RESULT_CANCELED);
        super.onBackPressed();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: got something");
        if (requestCode == NEED_UPDATE_REQUEST && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: settings good");
            mMainActivityNeedsToUpdate = true;
        }
    }

    //fragment with our settings layout/ main options
    public static class LinutePreferenceFragment extends PreferenceFragment {
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
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_profile_frag_main);

            bindPreferences();
            setOnClickListeners();
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

        private void setOnClickListeners() {
            //logout //TODO: unregister phone
            mLogOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //clear saved information
                    Utils.resetUserInformation(getActivity()
                            .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit());
                    Utils.deleteTempSharedPreference(getActivity()
                            .getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE).edit());

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
                    getActivity().startActivityForResult(i, NEED_UPDATE_REQUEST);
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
    }
}
