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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.gcm.GcmListenerService;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.Chat.ChatRoom;
import com.linute.linute.MainContent.Chat.User;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


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

        //doesn't post notifications if user is logged out
        SharedPreferences pref = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        if (pref.getString("userID", null) == null) {
            return;
        }

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
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);


        //Log.d(TAG, data.toString());

        String message = data.getString("message");


        //int type = gettNotificationType(data.getString("action"));
        //String name = data.getString("ownerFullName");
        boolean isAnon = "1".equals(data.getString("privacy"));
        String profileImage = null;
        switch (action) {
            case "messager":
                try {
                    JSONObject image = new JSONObject(data.getString("roomProfileImage"));
                    profileImage = image.getString("original");
                } catch (JSONException | NullPointerException e) {
                }
                break;
            default:
                profileImage = data.getString("ownerProfileImage");
                profileImage =
                        (isAnon
                                ? Utils.getAnonImageUrl(String.valueOf(profileImage))
                                : Utils.getImageUrlOfUser(String.valueOf(profileImage))
                        );
        }


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ChatRoom chatRoom = (ChatRoom) intent.getParcelableExtra("chatRoom");
        String title = chatRoom != null ? chatRoom.getRoomName() : "Tapt";
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_untitled_4_01)
                .setColor(Color.BLACK)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        if (profileImage != null) {
            File image = null;
            try {
                image = Glide.with(this).load(profileImage).downloadOnly(256, 256).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (image != null) {
                /*ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
                manager.getMemoryInfo(info);*/

                notificationBuilder.setLargeIcon(getCircleBitmap(image));

            }
        }

        BigInteger notificationId;

        Object ownerId = data.get("room");
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

    private static Bitmap getCircleBitmap(Bitmap bitmap) {
        //returns square image for older phones that prefer square notifs
        /*if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
            return bitmap;
        }*/
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    private static Bitmap getCircleBitmap(File file) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        opts.inSampleSize = Math.max(opts.outWidth / 192, opts.outHeight / 192);
        opts.inJustDecodeBounds = false;
        return getCircleBitmap(BitmapFactory.decodeFile(file.getAbsolutePath(), opts));
    }


    private Intent buildIntent(Bundle data, String action) {
        Intent intent;

        //Log.i(TAG, "action : "  + action);
        Log.d(TAG, "sendNotification: " + data.toString());

        boolean isLoggedIn = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            intent = new Intent(this, PreLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return intent;
        }

        if (action == null) {
            intent = new Intent(this, MainActivity.class);
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

            try {
                JSONObject image = new JSONObject(data.getString("roomProfileImage", "{original:'', thumbnail:''}"));
                JSONArray users = new JSONArray(data.getString("roomUsers", "[]"));

                String myId = Utils.getMyId(getApplicationContext());

                ArrayList<User> usersList = new ArrayList<>(users.length());
                for (int u = 0; u < users.length(); u++) {
                    JSONObject userJson = users.getJSONObject(u);
                    if (!myId.equals(userJson.getString("id"))) {
                        usersList.add(new User(
                                userJson.getString("id"),
                                userJson.getString("firstName"),
                                "",
                                ""
                        ));
                    }
                }

                Log.d("AAA", data.getString("room", ""));

                ChatRoom chatRoom = new ChatRoom(
                        data.getString("room", ""),
                        Integer.parseInt(data.getString("roomType", "" + ChatRoom.ROOM_TYPE_GROUP)),
                        data.getString("roomNameOfGroup", null),
                        image.getString("thumbnail"),
                        usersList,
                        "",
                        true,
                        0,
                        false,
                        0
                );

                intent.putExtra("chatRoom", chatRoom);

                /*intent.putExtra("ownerID", data.getString("ownerID"));
                intent.putExtra("ownerFirstName", data.getString("ownerFullName"));
                intent.putExtra("ownerLastName", data.getString("ownerLastName"));
                intent.putExtra("room", data.getString("room"));*/
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
            case "liked comment":
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
