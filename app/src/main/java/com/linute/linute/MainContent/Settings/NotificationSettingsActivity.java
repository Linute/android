package com.linute.linute.MainContent.Settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by QiFeng on 4/17/16.
 */
public class NotificationSettingsActivity extends BaseSocketActivity {

    private static final String TAG = NotificationSettingsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        //get toolbar
        setUpToolbar();

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.setting_fragment, new NotificationFragment()).commit();

    }

    private void setUpToolbar() {
        Toolbar toolBar = (Toolbar) findViewById(R.id.settingactivity_toolbar);
        toolBar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolBar.setTitle("Notifications");
        setSupportActionBar(toolBar);
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
            NotificationSettingsActivity activity = (NotificationSettingsActivity) getActivity();
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


}
