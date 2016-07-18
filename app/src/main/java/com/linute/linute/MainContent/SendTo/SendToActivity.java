package com.linute.linute.MainContent.SendTo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashSet;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;


/**
 * Created by QiFeng on 7/18/16.
 */

/**
 * Note: Use this after CameraActivity to send and share posts
 * <p/>
 * <p/>
 * TODO: Does the post need to exist before we share it ?
 */
public class SendToActivity extends AppCompatActivity implements SendToFragment.OnSendItems {

    public static final String PENDING_POST_KEY = "pending_post_key";
    private PendingUploadPost mPendingUploadPost;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        mPendingUploadPost = getIntent().getParcelableExtra(PENDING_POST_KEY);

        SendToFragment fragment = SendToFragment.newInstance(mPendingUploadPost.getId(), true);
        fragment.setOnSendItems(this);
        replaceContainerWithFragment(fragment);
    }


    private boolean mConnecting;
    private Socket mSocket;

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
                                    "&deviceToken=" + device.getDeviceToken() +
                                    "&udid=" + device.getUdid() +
                                    "&version=" + device.getVersionName() +
                                    "&build=" + device.getVersionCode() +
                                    "&os=" + device.getOS() +
                                    "&platform=" + device.getType() +
                                    "&api=" + API_Methods.VERSION +
                                    "&model=" + device.getModel();

                    op.reconnectionDelay = 5;
                    op.secure = true;
                    op.transports = new String[]{WebSocket.NAME};

                    mSocket = IO.socket(API_Methods.getURL(), op);/*R.string.DEV_SOCKET_URL*/

                    mSocket.on(Socket.EVENT_ERROR, onEventError);
                    mSocket.connect();
                    mConnecting = false;

                    mSocket.emit(API_Methods.VERSION + ":posts:refresh", new JSONObject());
                    //emitSocket(API_Methods.VERSION + ":messages:unread", new JSONObject());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onSocketTimeOut);
            mSocket.off(Socket.EVENT_ERROR, onEventError);
        }
    }


    private Emitter.Listener onSocketTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showBadConnectionToast(SendToActivity.this);
                }
            });
        }
    };

    private Emitter.Listener onEventError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showServerErrorToast(SendToActivity.this);
                }
            });
        }
    };


    public void replaceContainerWithFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.parent, fragment)
                .commit();
    }


    @Override
    public void sendItems(HashSet<SendToItem> items) {
        JSONArray people = new JSONArray();
        JSONArray trends = new JSONArray();
        for (SendToItem sendToItem : items) {
            if (sendToItem.getType() == SendToItem.TYPE_PERSON) {
                people.put(sendToItem.getId());
            } else if (sendToItem.getType() == SendToItem.TYPE_TREND) {
                trends.put(sendToItem.getId());
            }
        }

        JSONObject send = new JSONObject();
        try {
            send.put("users", people);
            send.put("trends", trends);
            send.put("post", mPendingUploadPost.getId());

            if (!mSocket.connected()) {
                Utils.showBadConnectionToast(SendToActivity.this);
            } else {

                //// TODO: 7/18/16 post the item
                //   note:      does it need to be posted before the emit ?

                mSocket.emit(API_Methods.VERSION + ":posts:share", send);
                Toast.makeText(SendToActivity.this, "Post has been shared", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
