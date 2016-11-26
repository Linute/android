package com.linute.linute.MainContent.Settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by QiFeng on 4/17/16.
 */
public class BasicSettingsActivity extends BaseSocketActivity {

    private static final String TAG = BasicSettingsActivity.class.getSimpleName();

    public static final String ARG_TYPE = "activity_type";
    public static final int NOTIFICATIONS = 0;
    public static final int CONTENT = 1;

    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mType = getIntent().getIntExtra(ARG_TYPE, NOTIFICATIONS);
        setContentView(R.layout.activity_settings);

        //get toolbar
        setUpToolbar();

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.setting_fragment, getFragment()).commit();

    }

    private void setUpToolbar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        toolBar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolBar.setTitle(getToolbarTitle());
        setSupportActionBar(toolBar);
    }


    private PreferenceFragment getFragment(){
        switch (mType){
            case NOTIFICATIONS:
                return new NotificationFragment();
            case CONTENT:
                return new ContentFragment();
            default:
                return new NotificationFragment();
        }
    }

    private String getToolbarTitle(){
        switch (mType){
            case NOTIFICATIONS:
                return "Notifications";
            case CONTENT:
                return "Content";
            default:
                return "";

        }
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


    public boolean emitSocket(String key, Object obj) {
        TaptSocket socket = TaptSocket.getInstance();
        if (socket.socketConnected()) {
            socket.emit(key, obj);
            return true;
        }
        return false;
    }



    public static class NotificationFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        HashMap<String, Boolean> mSettings = new HashMap<>();



        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LinuteConstants.SHARED_PREF_NAME);
            addPreferencesFromResource(R.xml.pref_notifications);

            SwitchPreference t;

            t = ((SwitchPreference) findPreference("notif_like"));
            t.setOnPreferenceChangeListener(this);
            mSettings.put("like", t.isChecked());

            t = (SwitchPreference) findPreference("notif_comment");
            t.setOnPreferenceChangeListener(this);
            mSettings.put("comment", t.isChecked());

            t = (SwitchPreference) findPreference("notif_alsoComment");
            t.setOnPreferenceChangeListener(this);
            mSettings.put("alsoComment", t.isChecked());

            t = (SwitchPreference) findPreference("notif_mention");
            t.setOnPreferenceChangeListener(this);
            mSettings.put("mention", t.isChecked());

            t = (SwitchPreference) findPreference("notif_follow");
            t.setOnPreferenceChangeListener(this);
            mSettings.put("follow", t.isChecked());

            t = (SwitchPreference) findPreference("notif_message");
            t.setOnPreferenceChangeListener(this);
            mSettings.put("message", t.isChecked());
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            BasicSettingsActivity activity = (BasicSettingsActivity) getActivity();
            if (activity != null) {
                try {
                    JSONObject object = new JSONObject();

                    mSettings.put(getKey(preference.getKey()), (Boolean)newValue);

                    for (HashMap.Entry<String, Boolean> entry : mSettings.entrySet()){
                        object.put(entry.getKey(), entry.getValue());
                    }

                    if (activity.emitSocket(API_Methods.VERSION + ":users:notification settings", object)) {
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }


        //gets everything after 6th place
        public static String getKey(String key) {
            return key.substring(6);
        }
    }

    public static class ContentFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LinuteConstants.SHARED_PREF_NAME);
            addPreferencesFromResource(R.xml.pref_content);

            findPreference("autoPlay").setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            if (preference.getKey().equals("autoPlay")) {
                SingleVideoPlaybackManager.setAutoPlay((Boolean) o);
                return true;
            }
            return false;
        }
    }

}
