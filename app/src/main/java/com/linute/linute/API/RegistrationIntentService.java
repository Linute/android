package com.linute.linute.API;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "My";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

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

            /*
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
*/
            // [END get_token]

            //FIXME: Currently using test token
            String token = "123";

            Log.v(TAG, "GCM Registration Token: " + token);

            sendRegistrationDevice(token);

            // Subscribe to topic channels
            //subscribeTopics(token);

            sharedPreferences.edit().putString(QuickstartPreferences.OUR_TOKEN, token).apply();

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
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationDevice(String token) {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Content-Type", "application/json");

        Map<String,String> device = new HashMap<String,String>();
        device.put("token", token);
        device.put("os", "android");
        Device.createDevice(headers, device, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "failed registration");
            }

            @Override
            public void onResponse(Response response) throws IOException {
               /* if (response.isSuccessful()) {
                    int httpCode = response.code();
                    if (httpCode == 200) { //if valid query
                        Log.v(TAG, response.body().string());
                    }
                    else {
                        Log.e(TAG, "Invalid");
                    }
                }else {
                    Log.v(TAG, String.valueOf(response.code()));
                }*/
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

}
