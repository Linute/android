package com.linute.linute.MainContent.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.BuildConfig;
import com.linute.linute.Database.TaptUser;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.Chat.ChatFragment;
import com.linute.linute.MainContent.Chat.User;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.WebViewActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;


public class SettingActivity extends BaseSocketActivity {

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
        Toolbar toolBar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        toolBar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolBar.setTitle("Settings");
        setSupportActionBar(toolBar);
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
        if (requestCode == LinutePreferenceFragment.NEED_UPDATE_REQUEST && resultCode == RESULT_OK) {
            mUpdateNeeded = true;
        }
    }

    @Override
    public void addFragmentToContainer(final Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.window, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void addFragmentToContainer(final Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.window, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void addFragmentOnTop(Fragment fragment, String tag) {
//        hideKeyboard();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.frag_fade_in, R.anim.hold, R.anim.hold, R.anim.frag_fade_out)
                .add(R.id.window, fragment, tag)
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void replaceContainerWithFragment(final Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.frag_fade_in, R.anim.hold)
                .replace(R.id.window, fragment)
                .addToBackStack(null)
                .commit();

    }



    //fragment with our settings layout
    public static class LinutePreferenceFragment extends PreferenceFragment {
        Preference mEditProfileInfo;
        Preference mChangeEmail;
        Preference mChangePhoneNumber;
        Preference mManageAccount;
        Preference mTalkToUs;
        Preference mGiveFeedback;
        Preference mPrivacyPolicy;
        Preference mTermsOfService;
        Preference mLogOut;
        Preference mAttributions;
        Preference mNotification;
        Preference mAbout;

        private Realm mRealm;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mRealm = Realm.getDefaultInstance();
            addPreferencesFromResource(R.xml.pref_profile_frag_main);

            bindPreferences();
            setOnClickListeners();
        }

        private void bindPreferences() {
            mEditProfileInfo = findPreference("edit_profile");
            mChangeEmail = findPreference("change_email");
            mChangePhoneNumber = findPreference("change_phone_number");
            mManageAccount = findPreference("manage_account");
            mTalkToUs = findPreference("talk_to_us");
            mGiveFeedback = findPreference("give_feedback");
            mPrivacyPolicy = findPreference("privacy policy");
            mTermsOfService = findPreference("terms_of_service");
            mLogOut = findPreference("logout");
            mAttributions = findPreference("attributions");
            mNotification = findPreference("notifications");
            mAbout = findPreference("version");

            DeviceInfoSingleton info = DeviceInfoSingleton.getInstance(getActivity());
            mAbout.setSummary("v" + info.getVersionCode() + "api" + API_Methods.VERSION + (API_Methods.IS_DEV_BUILD ? " @"+new Date(BuildConfig.TIMESTAMP).toLocaleString() : ""));
        }

        /* TODO: still need to add the following on click listeners:
                mFindFriendFacebook
                mFindFriendsContacts
         */

        public static final int NEED_UPDATE_REQUEST = 1;

        private void setOnClickListeners() {
            mLogOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //clear saved information
                    TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:logout", new JSONObject());
                    Utils.resetUserInformation(getActivity()
                            .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE));
                    Utils.deleteTempSharedPreference(getActivity()
                            .getSharedPreferences(LinuteConstants.SHARED_TEMP_NAME, MODE_PRIVATE));

                    if (AccessToken.getCurrentAccessToken() != null) //log out facebook if logged in
                        LoginManager.getInstance().logOut();

                    TaptSocket.getInstance().forceDisconnect();
                    TaptSocket.clear();

                    mRealm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.delete(TaptUser.class);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i = new Intent(getActivity(), PreLoginActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); //don't let them come back
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                });
                            }
                        }
                    });

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

            mManageAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), ManageAccountActivity.class);
                    startActivity(i);
                    return true;
                }
            });

            //privacy policy
            //FIXME : open in browser
            mPrivacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), WebViewActivity.class);
                    i.putExtra(WebViewActivity.LOAD_URL, "https://www.tapt.io/privacy-policy");
                    startActivity(i);
                    return true;
                }
            });

            mAttributions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), AttributionsActivity.class);
                    startActivity(i);
                    return true;
                }
            });


            //terms of service
            mTermsOfService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), WebViewActivity.class);
                    i.putExtra(WebViewActivity.LOAD_URL, "https://www.tapt.io/terms-of-service");
                    startActivity(i);
                    return true;
                }
            });

            mTalkToUs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ArrayList<User> users = new ArrayList();
                    users.add(new User("56be0e665e504abb121290b0","TaptHQ", "", null));

                    ChatFragment.newInstance(null, users);
//                    ((AppCompatActivity)getActivity()).getSupportFragmentManager().beginTransaction().add(ChatFragment.newInstance(null, users),"").commit();
                    ((BaseTaptActivity)getActivity())
                            .addFragmentOnTop(ChatFragment.newInstance(null, users), "Chat");
                    return true;
                }
            });

            mGiveFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    /*Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@tapt.io"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, "Replace this text with any feedback you'd like to give us!");
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }*/
                    Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            mNotification.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), NotificationSettingsActivity.class);
                    startActivity(i);
                    return true;
                }
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mRealm.close();
        }
    }
}

