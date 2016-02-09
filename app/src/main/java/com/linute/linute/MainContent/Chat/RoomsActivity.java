package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
        toolbar.setTitle("Rooms");
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setImageResource(R.drawable.add_friend);
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

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

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
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            toggleFab(true);
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSocket == null || !mSocket.connected() && !mConnecting) {
            mConnecting = true;
            {
                try {
                    mSocket = IO.socket(getString(R.string.DEV_SOCKET_URL));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            mSocket.on("authorization", authorization);
            mSocket.connect();
            mConnecting = false;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user", mSharedPreferences.getString("userID", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("authorization", jsonObject);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.disconnect();

        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("authorization", authorization);
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

    private Emitter.Listener authorization = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "runAuthorization: " + ((JSONObject) args[0]).toString(4));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
