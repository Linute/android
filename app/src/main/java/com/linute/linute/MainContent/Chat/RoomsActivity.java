package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import de.greenrobot.event.Subscribe;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class RoomsActivity extends BaseTaptActivity {

    private static final String TAG = RoomsActivity.class.getSimpleName();
    private TextView mTitle;
    Handler mHandler = new Handler(Looper.getMainLooper());
    //private EventBus mEventBus = EventBus.getDefault();
    private FloatingActionButton mFab;
    private Socket mSocket;
    private SharedPreferences mSharedPreferences;
    private boolean mConnecting;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mToolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
        mToolbar.setTitle("Inbox");
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(mToolbar);

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

    public void hideFab(boolean hide) {
        if (hide && mFab.isShown()) {
            mFab.hide();
        } else if (!hide && !mFab.isShown()) {
            mFab.show();
        }
    }

//    @Override
//    public void onBackPressed() {
//        int count = getSupportFragmentManager().getBackStackEntryCount();
//        if (count == 0) {
//            super.onBackPressed();
//        } else {
//            if (count == 1){
//                toggleFab(true);
//            }
//            getSupportFragmentManager().popBackStack();
//        }
//    }


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

        if ((mSocket == null || !mSocket.connected()) && !mConnecting) {
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
                                    "&api=" + API_Methods.VERSION +
                                    "&model=" + device.getModel();

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


    //gives access to this from our fragments
    public void changeToolbarTitle(String title){
        if (mToolbar != null) mToolbar.setTitle(title);
    }



    @Override
    public void connectSocket(String event, Emitter.Listener emitter) {
        if (mSocket != null) {
            mSocket.on(event, emitter);
        }
    }

    @Override
    public void emitSocket(String event, Object arg) {
        if (mSocket != null)
            mSocket.emit(event, arg);
    }

    @Override
    public void disconnectSocket(String event, Emitter.Listener emitter) {
        if (mSocket != null) {
            mSocket.off(event, emitter);
        }
    }


    @Override
    public boolean socketConnected() {
        return mSocket != null && mSocket.connected();
    }


}
