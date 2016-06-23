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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.gcm.GcmListenerService;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;


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
        //String message = data.getString("message");
        String action = data.getString("action");
//        Log.d(TAG, "From: " + from);
//        Log.d(TAG, "Message: " + message);
//        for (String key : data.keySet()) {
//            Log.d(TAG, key + " is a key in the bundle");
//        }
//        if (from.startsWith("/topics/")) {
//            // message received from some topic.
//        } else {
//            // normal downstream message.
//        }

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

        Intent intent = buildIntent(data, action);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Log.d(TAG, message);

        Log.i("AAA", data.toString());


        String message = data.getString("message");
        int type = gettNotificationType(data.getString("action"));
        String name = data.getString("ownerFullName");
        boolean isAnon = "1".equals(data.getString("privacy"));
        Log.i("AAA", data.getInt("privacy") + " " + data.getString("privacy") + " " + isAnon);
        Object profileImage = data.get("ownerProfileImage");

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)

                .setSmallIcon(R.drawable.ic_stat_untitled_4_01)
                .setColor(Color.BLACK)
                .setContentTitle("Tapt")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        if (profileImage != null) {
            File image = null;
            try {
                String url =
                        (isAnon
                                ? Utils.getAnonImageUrl(String.valueOf(profileImage))
                                : Utils.getImageUrlOfUser(String.valueOf(profileImage))
                        );
                Log.i("AAA", url);
                image = Glide.with(this).load(url).downloadOnly(64, 64).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (image != null)
                notificationBuilder.setLargeIcon(BitmapFactory.decodeFile(image.getAbsolutePath()));
        }

        BigInteger notificationId;

        Object ownerId = data.get("ownerID");
        Object eventId = data.get("event");
        if (eventId != null) {
            notificationId = new BigInteger(String.valueOf(eventId), 16);
        } else if (ownerId != null) {
            notificationId = new BigInteger(String.valueOf(ownerId), 16);
        } else {
            notificationId = BigInteger.ZERO;
        }

        final int notifId = notificationId.intValue();
        Notification notifications = notificationBuilder.build();
        NotificationManagerCompat.from(this).notify(notificationId.intValue(), notifications);
    }


    private Intent buildIntent(Bundle data, String action) {
        Intent intent;

        //Log.i(TAG, "action : "  + action);
        Log.i(TAG, "sendNotification: " + data.toString());

        boolean isLoggedIn = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).getBoolean("isLoggedIn", false);


        if (action == null) {
            intent = new Intent(this, isLoggedIn ? MainActivity.class : PreLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }
        if (!isLoggedIn) {
            intent = new Intent(this, PreLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }

        int type = gettNotificationType(data.getString("action"));
        if (type == LinuteConstants.MISC) {
            intent = new Intent(this, PreLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }

        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("NOTIFICATION", type);
        if (type == LinuteConstants.MESSAGE) {
            intent.putExtra("NOTIFICATION", type);
            intent.putExtra("ownerID", data.getString("ownerID"));
            intent.putExtra("ownerFullName", data.getString("ownerFullName"));
            intent.putExtra("room", data.getString("room"));

        } else if (type == LinuteConstants.FEED_DETAIL) {
            intent.putExtra("event", data.getString("event"));
        } else {
            intent.putExtra("user", data.getString("user"));
        }

        return intent;
    }


    private int gettNotificationType(String action) {
        switch (action) {
            case "commented status":
                return LinuteConstants.FEED_DETAIL;
            case "liked status":
                return LinuteConstants.FEED_DETAIL;
            case "commented photo":
                return LinuteConstants.FEED_DETAIL;
            case "liked photo":
                return LinuteConstants.FEED_DETAIL;
            case "commented video":
                return LinuteConstants.FEED_DETAIL;
            case "liked video":
                return LinuteConstants.FEED_DETAIL;
            case "also commented status":
                return LinuteConstants.FEED_DETAIL;
            case "also commented video":
                return LinuteConstants.FEED_DETAIL;
            case "also commented photo":
                return LinuteConstants.FEED_DETAIL;
            case "mentioned":
                return LinuteConstants.FEED_DETAIL;
            case "friend posted status":
                return LinuteConstants.FEED_DETAIL;
            case "friend posted video":
                return LinuteConstants.FEED_DETAIL;
            case "friend posted photo":
                return LinuteConstants.FEED_DETAIL;
            case "friend joined":
                return LinuteConstants.PROFILE;
            case "follower":
                return LinuteConstants.PROFILE;
            case "matched":
                return LinuteConstants.PROFILE;
            case "messager":
                return LinuteConstants.MESSAGE;
            default:
                return LinuteConstants.MISC;
        }
    }
}
