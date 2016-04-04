package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

public class RoomsActivity extends BaseTaptActivity {

    private static final String TAG = RoomsActivity.class.getSimpleName();
    //private TextView mTitle;
    //Handler mHandler = new Handler(Looper.getMainLooper());
    //private EventBus mEventBus = EventBus.getDefault();
    //private FloatingActionButton mFab;
    private Socket mSocket;
    private SharedPreferences mSharedPreferences;
    private boolean mConnecting; //socket is trying to connect

    private boolean mSafeForFragmentTransaction; //if safe for fragment transitions.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        mSafeForFragmentTransaction = false;

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        if (savedInstanceState == null){
           getSupportFragmentManager()
                   .beginTransaction()
                   .replace(R.id.chat_container, new RoomsActivityFragment())
                   .commit();
        }


    }

//    @Subscribe
//    public void onEvent(final JSONObject object) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Toast.makeText(RoomsActivity.this, object.getString("username") + " " + object.getString("message"), Toast.LENGTH_SHORT).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }


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

        mSafeForFragmentTransaction = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSafeForFragmentTransaction = false;

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
                            R.string.error_connect, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


    //change toolbar title
    @Override
    public void setTitle(String title) {

    }

    @Override
    public void addFragmentToContainer(Fragment fragment) {
        if (!mSafeForFragmentTransaction) return;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void replaceContainerWithFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_container, fragment)
                .commit();
    }

    @Override
    public void setToolbarOnClickListener(View.OnClickListener listener) {
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
    public void setSocketErrorResponse(SocketErrorResponse error) {

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
