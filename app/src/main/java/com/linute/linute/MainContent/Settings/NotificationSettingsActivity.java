package com.linute.linute.MainContent.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

/**
 * Created by QiFeng on 4/17/16.
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private static final String TAG = NotificationSettingsActivity.class.getSimpleName();
    private Socket mSocket;
    private boolean mConnecting;

    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

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
                                    "&deviceToken=" + device.getDeviceToken() +
                                    "&udid=" + device.getUdid() +
                                    "&version=" + device.getVersonName() +
                                    "&build=" + device.getVersionCode() +
                                    "&os=" + device.getOS() +
                                    "&type=" + device.getType() +
                                    "&api=" + API_Methods.VERSION
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

    public boolean emitSocket(String key, Object obj) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit(key, obj);
            return true;
        }
        return false;
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NotificationSettingsActivity.this, R.string.error_connect, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
    }


    public static class NotificationFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        SwitchPreference like;
        SwitchPreference comment;
        SwitchPreference alsoComment;
        SwitchPreference mention;
        SwitchPreference follow;
        SwitchPreference message;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LinuteConstants.SHARED_PREF_NAME);
            addPreferencesFromResource(R.xml.pref_notifications);

            like = (SwitchPreference) findPreference("notif_like");
            comment = (SwitchPreference) findPreference("notif_comment");
            alsoComment = (SwitchPreference) findPreference("notif_alsoComment");
            mention = (SwitchPreference) findPreference("notif_mention");
            follow = (SwitchPreference) findPreference("notif_follow");
            message = (SwitchPreference) findPreference("notif_message");

            like.setOnPreferenceClickListener(this);
            comment.setOnPreferenceClickListener(this);
            alsoComment.setOnPreferenceClickListener(this);
            mention.setOnPreferenceClickListener(this);
            follow.setOnPreferenceClickListener(this);
            message.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            NotificationSettingsActivity activity = (NotificationSettingsActivity) getActivity();
            if (activity != null) {
                try {
                    JSONObject object = new JSONObject();

                    object.put(getKey(like.getKey()), like.isChecked());
                    object.put(getKey(comment.getKey()), comment.isChecked());
                    object.put(getKey(alsoComment.getKey()), alsoComment.isChecked());
                    object.put(getKey(mention.getKey()), mention.isChecked());
                    object.put(getKey(message.getKey()), message.isChecked());
                    object.put(getKey(follow.getKey()), follow.isChecked());

                    if (activity.emitSocket(API_Methods.VERSION + ":users:notification settings", object)){
                        return true;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            SwitchPreference pref = (SwitchPreference) preference;
            pref.setChecked(!pref.isChecked());
            return false;
        }


        public static String getKey(String key) {
            return key.substring(6);
        }
    }


}
