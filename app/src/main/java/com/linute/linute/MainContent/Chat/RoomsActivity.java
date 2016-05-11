//package com.linute.linute.MainContent.Chat;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.WindowManager;
//import android.widget.Toast;
//
//import com.linute.linute.API.API_Methods;
//import com.linute.linute.API.DeviceInfoSingleton;
//import com.linute.linute.R;
//import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
//import com.linute.linute.UtilsAndHelpers.LinuteConstants;
//
//import java.net.URISyntaxException;
//
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import io.socket.emitter.Emitter;
//import io.socket.engineio.client.transports.WebSocket;
//
//public class RoomsActivity extends BaseTaptActivity {
//
//    private static final String TAG = RoomsActivity.class.getSimpleName();
//    private Socket mSocket;
//    private SharedPreferences mSharedPreferences;
//    private boolean mConnecting; //socket is trying to connect
//
//    private boolean mSafeForFragmentTransaction; //if safe for fragment transitions.
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE);
//
//        setContentView(R.layout.activity_rooms);
//
//        mSafeForFragmentTransaction = false;
//
//        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//
//        Intent i = getIntent();
//        if (i != null && i.getIntExtra("NOTIFICATION", LinuteConstants.UNDEFINED) == LinuteConstants.MESSAGE) {
//            replaceWithChatFragment(i);
//        } else if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.chat_container, new RoomsActivityFragment())
//                    .commit();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if ((mSocket == null || !mSocket.connected()) && !mConnecting) {
//            mConnecting = true;
//            {
//                try {
//                    IO.Options op = new IO.Options();
//
//                    DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(this);
//                    op.query =
//                            "token=" + mSharedPreferences.getString("userToken", "") +
//                                    "&deviceToken=" + device.getDeviceToken() +
//                                    "&udid=" + device.getUdid() +
//                                    "&version=" + device.getVersonName() +
//                                    "&build=" + device.getVersionCode() +
//                                    "&os=" + device.getOS() +
//                                    "&platform=" + device.getType() +
//                                    "&api=" + API_Methods.VERSION +
//                                    "&model=" + device.getModel() ;
//
//                    op.forceNew = true;
//                    op.reconnectionDelay = 5;
//                    op.transports = new String[]{WebSocket.NAME};
//
//                    mSocket = IO.socket(API_Methods.getURL(), op);/*R.string.DEV_SOCKET_URL*/
//                } catch (URISyntaxException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
//            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
//            mSocket.connect();
//            mConnecting = false;
//        }
//
//        mSafeForFragmentTransaction = true;
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mSafeForFragmentTransaction = false;
//
//        mSocket.disconnect();
//
//        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
//        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
//    }
//
//    private Emitter.Listener onConnectError = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(getApplicationContext(),
//                            R.string.error_connect, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
//
//
//    @Override
//    public void addFragmentToContainer(Fragment fragment) {
//        if (!mSafeForFragmentTransaction) return;
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.chat_container, fragment)
//                .addToBackStack(null)
//                .commit();
//    }
//
//    @Override
//    public void replaceContainerWithFragment(Fragment fragment) {
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.chat_container, fragment)
//                .commit();
//    }
//
//    @Override
//    public void connectSocket(String event, Emitter.Listener emitter) {
//        if (mSocket != null) {
//            mSocket.on(event, emitter);
//        }
//    }
//
//    @Override
//    public void emitSocket(String event, Object arg) {
//        if (mSocket != null)
//            mSocket.emit(event, arg);
//    }
//
//    @Override
//    public void setSocketErrorResponse(SocketErrorResponse error) {
//
//    }
//
//    @Override
//    public void disconnectSocket(String event, Emitter.Listener emitter) {
//        if (mSocket != null) {
//            mSocket.off(event, emitter);
//        }
//    }
//
//    @Override
//    public boolean socketConnected() {
//        return mSocket != null && mSocket.connected();
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        if (intent != null && intent.getIntExtra("NOTIFICATION", LinuteConstants.UNDEFINED) == LinuteConstants.MESSAGE) {
//            replaceWithChatFragment(intent);
//        }
//    }
//
//    private void replaceWithChatFragment(Intent intent) {
//        String room = intent.getStringExtra("room");
//        String userId = intent.getStringExtra("ownerID");
//        String userName = intent.getStringExtra("ownerFullName");
//        mSafeForFragmentTransaction = true;
//        replaceContainerWithFragment(ChatFragment.newInstance(room == null || room.isEmpty() ? null : room,
//                userName, userId.isEmpty() ? null : userId));
//
//    }
//}
