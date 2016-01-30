package com.linute.linute.API;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "Registration";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    private LocationManager mLocationManager;

    private String mToken;



    @Override
    protected void onHandleIntent(Intent intent) {
        //SharedPreferences sharedPreferences = new SecurePreferences(getApplicationContext(), "BtE3eRHzZq", LinuteConstants.SharedPrefName);

        SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]


            /*FIXME: REMOVE BEFORE RELEASE*/
            InstanceID instanceID = InstanceID.getInstance(this);
            mToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            //String token = "1234";
//            Log.v(TAG, "GCM Registration Token: " + token);

            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


            requestServices();

//            sendRegistrationDevice(token);


            // Subscribe to topic channels
            //subscribeTopics(token);

            sharedPreferences.edit().putString(QuickstartPreferences.OUR_TOKEN, mToken).apply();

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     */
    private void sendRegistrationDevice( JSONObject coord) {
        final String token = mToken;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String versionName = "";
        String versionCode = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Map<String, Object> device = new HashMap<>();
        device.put("token", token);
        device.put("version", versionName);
        device.put("build", versionCode);
        device.put("os", Build.VERSION.SDK_INT + "");
        device.put("type", "android");

        if (coord != null)
            device.put("coordinates", coord);

        Device.createDevice(headers, device, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "failed registration");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, response.body().string());
                    Log.e(TAG, "ERROR REGISTERING TOKEN");
                } else {
                    Log.v(TAG, response.body().string());
                    Log.d(TAG, "sendRegistrationDevice");
                }
            }
        });

    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]


    private Handler mStopGettingLocationHandler;

    private Runnable mStopGettingLocation = new Runnable() {
        @Override
        public void run() {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Took too long to get location");
            sendRegistrationDevice(null);
        }
    };


    public void requestServices() {
        if (!Utils.isNetworkAvailable(this)) {
            sendRegistrationDevice(null);
        }

        else {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            } else {
                sendRegistrationDevice(null);
            }
        }
    }

    private void requestLocation() throws SecurityException {
        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
        mStopGettingLocationHandler = new Handler();
        mStopGettingLocationHandler.postDelayed(mStopGettingLocation, 2000); //if taking longer than 1.5 seconds to get location, stop it
    }


    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location.toString());
            mStopGettingLocationHandler.removeCallbacks(mStopGettingLocation);

            SharedPreferences sharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);

            sharedPreferences.edit()
                    .putString("geoLongitude", location.getLongitude() + "") //will be saved as String. Cant store double
                    .putString("geoLatitude", location.getLatitude() + "")
                    .apply();


            if (sharedPreferences.getBoolean("isLoggedIn", false)) {
                JSONArray coord = new JSONArray();
                try {
                    coord.put(location.getLatitude());
                    coord.put(location.getLongitude());

                    JSONObject coordinates = new JSONObject();
                    coordinates.put("geo", coord);

                    sendRegistrationDevice(coordinates);

                } catch (JSONException e) {
                    e.printStackTrace();
                    sendRegistrationDevice(null);
                }
            } else {
                sendRegistrationDevice(null);
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            mStopGettingLocationHandler.removeCallbacks(mStopGettingLocation);
            sendRegistrationDevice(null);
        }
    };

}
