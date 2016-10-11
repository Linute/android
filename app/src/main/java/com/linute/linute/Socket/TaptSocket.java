package com.linute.linute.Socket;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.DeviceInfoSingleton;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

/**
 * Created by QiFeng on 9/29/16.
 */
public class TaptSocket {

    private static final String TAG = TaptSocket.class.getSimpleName();
    private Socket mSocket;

    // counts how many Activities are using this connection
    // in Activity onResume, increment this
    // in Activity onStop, decrement this
    // if 0 connections are left, we disconnect from socket
    private int mConnections;

    private static TaptSocket mTaptSocket;


    private TaptSocket(Context context) {
        try {
            IO.Options op = new IO.Options();
            DeviceInfoSingleton device = DeviceInfoSingleton.getInstance(context);
            String token = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken", null);
            if (token == null) throw new NullPointerException("No user token");

            JSONArray coord = new JSONArray();

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                Location loca = ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (loca != null) {
                    try {
                        coord.put(loca.getLatitude());
                        coord.put(loca.getLongitude());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            op.query = "token=" + token +
                    "&deviceToken=" + device.getDeviceToken() +
                    "&udid=" + device.getUdid() +
                    "&version=" + device.getVersionName() +
                    "&build=" + device.getVersionCode() +
                    "&os=" + device.getOS() +
                    "&platform=" + device.getType() +
                    "&api=" + API_Methods.VERSION +
                    "&model=" + device.getModel() +
                    "&geo=" + coord;

            op.reconnectionDelay = 5;
            op.secure = true;
            op.transports = new String[]{WebSocket.NAME};

            mSocket = IO.socket(API_Methods.getURL(), op);/*R.string.DEV_SOCKET_URL*/
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // NOTE: CLEAR SOCKET IF PERSON IS LOGGED OUT
    public static void clear() {
        mTaptSocket = null;
    }

    public static void initSocketConnection(Context context) {
        if (mTaptSocket == null)
            mTaptSocket = new TaptSocket(context);
    }

    public void connectSocket() {
        if (mSocket == null) return;
        if (!mSocket.connected()) {
            Log.d(TAG, "connectSocket: ");
            mSocket.connect();
        }
        mConnections++;
    }

    public void disconnectSocket() {
        if (mSocket == null) return;

        mConnections--;
        // disconnect when no more activities need it
        if (mConnections < 1) {
            Log.d(TAG, "disconnectSocket: ");
            mSocket.disconnect();
        }
    }


    public void forceDisconnect() {
        mConnections = 0;
        mSocket.disconnect();
        Log.d(TAG, "forceDisconnect: ");
    }


    public static TaptSocket getInstance() {
        return mTaptSocket;
    }

    public void on(String event, Emitter.Listener listener) {
        mSocket.on(event, listener);
    }

    public void off(String event, Emitter.Listener listener) {
        mSocket.off(event, listener);
    }

    public void emit(String event, Object arg) {
        mSocket.emit(event, arg);
    }

    public void emit(String event) {
        mSocket.emit(event);
    }

    public boolean socketConnected() {
        return mSocket != null && mSocket.connected();
    }

}
