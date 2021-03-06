package com.linute.linute.MainContent.Uploading;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import okhttp3.Response;

/**
 * Created by QiFeng on 6/25/16.
 */
public class UploadIntentService extends IntentService {

    public static final int ID = 0;

    private static int notificationId = 1;

    public static final String TAG = UploadIntentService.class.getSimpleName();

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private int mPendingFiles;

    public UploadIntentService() {
        super("UploadIntentService");
        setIntentRedelivery(true);
    }

    //// TODO: 6/30/16 move processing of video to this service?

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        mPendingFiles = 0;
        notificationId = new Random().nextInt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ++mPendingFiles;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PendingUploadPost p = intent.getParcelableExtra(PendingUploadPost.PENDING_POST_KEY);
        if (p != null) {
            sendNextFile(p);
        }
    }

    private void sendNextFile(PendingUploadPost p) {
        try {

            Uri imageUri = Uri.parse(p.getImagePath());
            File file = new File(imageUri.getPath());
            if (!file.exists()){
                failedToPost(p);
                Log.i(TAG, "sendNextFile: no image");
                return;
            }

            Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            mBuilder.setContentTitle("Preparing for upload")
                    .setContentText("")
                    .setLargeIcon(Bitmap.createScaledBitmap(image, 100, (int) (100f * (float) image.getHeight() / image.getWidth()), false))
                    .setProgress(0, 0, true)
                    .setContentIntent(null)
                    .setAutoCancel(false)
                    .setContentIntent(null);

            mNotificationManager.notify(ID, mBuilder.build());

            HashMap<String, Object> params = new HashMap<>();

            params.put("id", p.getId());
            params.put("college", p.getCollegeId());
            params.put("privacy", p.getPrivacy());
            params.put("isAnonymousCommentsDisabled", p.getIsAnonymousCommentsDisabled());
            params.put("title", p.getTitle());
            params.put("memes", new JSONArray(p.getStickers()));
            params.put("filters", new JSONArray(p.getFilters()));

            JSONArray coord = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            coord.put(p.getLatitude());
            coord.put(p.getLongitude());

            params.put("trends", new JSONArray(p.getTrends()));
            params.put("users", new JSONArray(p.getPeople()));

            if(p.getTrendId() != null){
                params.put("trend", p.getTrendId());
            }

            try {
                jsonObject.put("coordinates", coord);
                params.put("geo", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            params.put("type", p.getType());
            params.put("owner", p.getOwner());
            JSONArray imageArray = new JSONArray();

            imageArray.put(
                    p.getType() == 0 ?
                            Utils.encodeImageBase64HighRes(image) : Utils.encodeImageBase64(image));

            params.put("images", imageArray);
            if (p.getVideoPath() != null) {
                file = new File(p.getVideoPath());

                if (!file.exists()){
                    failedToPost(p);
                    Log.d(TAG, "sendNextFile: no vid");
                    return;
                }

                JSONArray videoArray = new JSONArray();
                videoArray.put(Utils.encodeFileBase64(file));
                params.put("videos", videoArray);
            }

            Response r = new LSDKEvents(this).postEvent(p.getUserToken(), params, new CountingRequestBody.Listener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength) {
                    mBuilder.setContentTitle("Uploading content")
                            .setContentText(mPendingFiles + (mPendingFiles > 1 ? " files remaining" : " file remaining"))
                            .setProgress(100, (int) (100 * bytesWritten / contentLength), false);
                    mNotificationManager.notify(ID, mBuilder.build());
                }
            });

            if (r.isSuccessful()) {
                mBuilder.setContentTitle("Upload complete")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_stat_untitled_4_01)
                        .setProgress(0, 0, false)
                        .setAutoCancel(true)
                        .setContentIntent(getIntent(p.getId(), p.getCollegeId()))
                        .setContentText(getPostText(p.getType()));
                mNotificationManager.notify(ID, mBuilder.build());
                stopSelf();
            } else {
                failedToPost(p);
                stopSelf();

            }

            image.recycle();
        } catch (IOException e) {
            failedToPost(p);

        }

        --mPendingFiles;
    }

    private PendingIntent getIntent(String eventID, String college) {
        Intent intent;
        boolean isLoggedIn = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            intent = new Intent(this, PreLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("NOTIFICATION", LinuteConstants.FEED_DETAIL);
            intent.putExtra("show_update", false);
            intent.putExtra("event", eventID);
        }

        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private String getPostText(int type) {
        String text;
        switch (type) {
            case 0:
                text = "Status";
                break;
            case 1:
                text = "Image";
                break;
            case 2:
                text = "Video";
                break;
            default:
                text = "Post";
                break;
        }
        return text + " has been posted";
    }


    private void failedToPost(final PendingUploadPost p) {
        Intent i = new Intent(this, UploadIntentService.class);
        i.putExtra(PendingUploadPost.PENDING_POST_KEY, p);
        mBuilder.setProgress(0, 0, false)
                .setSmallIcon(R.drawable.ic_stat_untitled_4_01)
                .setContentTitle("File failed to upload")
                .setContentText("Tap to retry")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getService(this, notificationId, i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT));

        mNotificationManager.cancel(ID);
        mNotificationManager.notify(notificationId++, mBuilder.build());
    }
}
