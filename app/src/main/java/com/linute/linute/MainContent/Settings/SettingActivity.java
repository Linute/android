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

    private boolean mUpdateNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_settings);

        //get toolbar
        setUpToolbar();

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.setting_fragment, new LinutePreferenceFragment()).commit();

    }

    private void setUpToolbar() {
        mToolBar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        mToolBar.setTitle("Settings");
        mToolBar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(mToolBar);
    }


    //override up button to go back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(mUpdateNeeded ? RESULT_OK : RESULT_CANCELED);
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("NeedUpdate", mUpdateNeeded);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUpdateNeeded = savedInstanceState.getBoolean("NeedUpdate");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LinutePreferenceFragment.NEED_UPDATE_REQUEST && resultCode == RESULT_OK){
            mUpdateNeeded = true;
        }
    }

    //fragment with our settings layout
    public static class LinutePreferenceFragment extends PreferenceFragment {
        Preference mEditProfileInfo;
        Preference mChangeEmail;
        Preference mChangePhoneNumber;
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
            mEditProfileInfo = findPreference("edit_profile");
            mChangeEmail = findPreference("change_email");
            mChangePhoneNumber = findPreference("change_phone_number");
            mGiveFeedback = findPreference("give_feedback");
            mPrivacyPolicy = findPreference("privacy policy");
            mTermsOfService = findPreference("terms_of_service");
            mLogOut = findPreference("logout");
        }

        /* TODO: still need to add the following on click listeners:
                mFindFriendFacebook
                mFindFriendsContacts
         */

        public static final int NEED_UPDATE_REQUEST = 1;

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

