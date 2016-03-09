package com.linute.linute.MainContent.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;


public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    //use a variable to keep track of if user made changes to account info
    //if user did change account information, let the parent "ProfileFragment" know, so it can update info
    public static String TAG = "SettingActivity";

    private boolean mUpdateNeeded;
    private Socket mSocket;
    private SharedPreferences mSharedPreferences;
    private boolean mConnecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_settings);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

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
        Preference mAttributions;

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
            mAttributions = findPreference("attributions");
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
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@tapt.io"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, "Replace this text with any feedback you'd like to give us!");
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
                    return true;
                }
            });
        }
    }


    // TODO: Copy below to other settings activities; related to "user online"
    @Override
    protected void onResume() {
        super.onResume();

        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;
            {
                try {
                    IO.Options op = new IO.Options();

                    DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(this);
                    op.query =
                            "token=" + mSharedPreferences.getString("userToken", "") +
                                    "&deviceToken="+device.getDeviceToken() +
                                    "&udid="+device.getUdid()+
                                    "&version="+device.getVersonName()+
                                    "&build="+device.getVersionCode()+
                                    "&os="+device.getOS()+
                                    "&type="+device.getType() +
                                    "&api="+ API_Methods.VERSION
                    ;

                    op.forceNew = true;
                    op.reconnectionDelay = 5;
                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(getString(R.string.SOCKET_URL), op);/*R.string.DEV_SOCKET_URL*/
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.connect();
            mConnecting = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "call: failed socket connection");
        }
    };

    // TODO: stop copy here
}

