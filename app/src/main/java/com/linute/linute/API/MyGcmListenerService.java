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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.linute.linute.MainContent.Chat.ChatHead;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String action = data.getString("action");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        for (String key : data.keySet()) {
            Log.d(TAG, key + " is a key in the bundle");
        }
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(data, action);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param data GCM Bundle received.
     */
    private void sendNotification(Bundle data, String action) {
        Intent intent = null;
        PendingIntent pendingIntent = null;
        String message = data.getString("message");

        //TODO: CAUSING CRASH

        if (action != null &&  action.equals("messages")) { //<---
            intent = new Intent(this, RoomsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("ROOMS", "SOMEMESSAGE");
            intent.putExtra("roomId", data.getString("room"));
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(data.getString("user"));
                intent.putExtra("ownerName", jsonObject.getString("fullName"));
                intent.putExtra("ownerId", jsonObject.getString("id"));

                JSONArray jsonArray = null;
                jsonArray = new JSONArray(data.getString("users"));
                intent.putExtra("roomCnt", jsonArray.length() + "");

                ArrayList<ChatHead> chatHeadList = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    chatHeadList.add(new ChatHead(
                            ((JSONObject) jsonArray.get(i)).getString("fullName"),
                            ((JSONObject) jsonArray.get(i)).getString("profileImage"),
                            ((JSONObject) jsonArray.get(i)).getString("id")));
                }
                intent.putParcelableArrayListExtra("chatHeads", chatHeadList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);
        }

        Log.d(TAG, message);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_profile)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        Notification notifications = notificationBuilder.build();
        NotificationManagerCompat.from(this).notify(0, notifications);
    }
}
