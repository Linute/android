package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class RoomsActivity extends AppCompatActivity {

    private static final String TAG = RoomsActivity.class.getSimpleName();
    private TextView mTitle;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private EventBus mEventBus = EventBus.getDefault();
    private FloatingActionButton mFab;
    private Socket mSocket;
    private SharedPreferences mSharedPreferences;
    private boolean mConnecting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
        toolbar.setTitle("Messages");
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null){
           getSupportFragmentManager()
                   .beginTransaction()
                   .replace(R.id.fragment, new RoomsActivityFragment())
                   .commit();
        }


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setImageResource(R.drawable.ic_action_new_message);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Create fragment and give it an argument specifying the article it should show
                SearchUsers newFragment = SearchUsers.newInstance("Hi", "Hello");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                toggleFab(false);
            }
        });
    }

    @Subscribe
    public void onEvent(final JSONObject object) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(RoomsActivity.this, object.getString("username") + " " + object.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void toggleFab(boolean toggleFab) {
        if (!toggleFab && mFab.isShown()) {
            mFab.hide();
        } else if (toggleFab && !mFab.isShown()) {
            mFab.show();
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            finish();
        } else {
            if (count == 1){
                toggleFab(true);
            }
            getSupportFragmentManager().popBackStack();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;
            {
                try {
                    IO.Options op = new IO.Options();
                    op.query = "token=" + mSharedPreferences.getString("userToken", "");
                    op.forceNew = true;
                    op.reconnectionDelay = 5;
                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(getString(R.string.SOCKET_URL), op);/*R.string.DEV_SOCKET_URL*/
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.connect();
            mConnecting = false;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", mSharedPreferences.getString("userID", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

}
