//package com.linute.linute.MainContent.Chat;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.widget.Toast;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.net.URISyntaxException;
//
//import de.greenrobot.event.EventBus;
//import de.greenrobot.event.Subscribe;
//import io.socket.client.IO;
//import io.socket.client.Socket;
//import io.socket.emitter.Emitter;
//
///**
// * Created by Arman on 1/18/16.
// */
//public class ChatService extends Service {
//
//    private static final String TAG = ChatService.class.getSimpleName();
//    public static final String CHAT_SERVER_URL = "https://campus.tapt.io/";
//
//    private Handler mHandler = new Handler(Looper.getMainLooper());
//    private Socket mSocket;
//
//    {
//        try {
//            mSocket = IO.socket(CHAT_SERVER_URL);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private EventBus mEventBus = EventBus.getDefault();
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (!mEventBus.isRegistered(this)) {
//            mEventBus.register(this);
//        }
//
//        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
//        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
//        mSocket.on("new message", onNewMessage);
//        mSocket.connect();
//        Log.d(TAG, "onStartCommand: ");
//
//        return Service.START_STICKY;
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        mSocket.disconnect();
//        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
//        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
//        mSocket.off("new message", onNewMessage);
//
//        if (mEventBus.isRegistered(this)) {
//            mEventBus.unregister(this);
//        }
//        super.onDestroy();
//    }
//
//    private Emitter.Listener onConnectError = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//
////            Toast.makeText(getActivity().getApplicationContext(),
////                    R.string.error_connect, Toast.LENGTH_LONG).show();
//            Log.d(TAG, "call: ERROR");
//        }
//    };
//
//    private Emitter.Listener onNewMessage = new Emitter.Listener() {
//        @Override
//        public void call(final Object... args) {
//            JSONObject data = (JSONObject) args[0];
//            String username;
//            String message;
//            try {
//                username = data.getString("username");
//                message = data.getString("message");
//                Log.d(TAG, "call: " + username + " " + message);
//                mEventBus.post(data);
//                // TODO: check if app is running, if not, send notif, else check if they are in chat
//                // if not send notif, else check if chat.username == username
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
////            removeTyping(username);
////            addMessage(username, message);
//        }
//    };
//
//    @Subscribe
//    public void onEvent(final JSONObject object) {
////        mHandler.post(new Runnable() {
////            @Override
////            public void run() {
////                try {
////                    Toast.makeText(ChatService.this, object.getString("username") + " " + object.getString("message"), Toast.LENGTH_SHORT).show();
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
////            }
////        });
//    }
//}
