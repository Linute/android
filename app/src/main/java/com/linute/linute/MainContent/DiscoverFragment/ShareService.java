package com.linute.linute.MainContent.DiscoverFragment;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;

import com.linute.linute.API.API_Methods;
import com.linute.linute.Socket.TaptSocket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class ShareService extends IntentService {

    public static final String TAG = ShareService.class.getSimpleName();

    public static final int REQ_CODE = 25;
    public static final String EXTRA_POST_ID = "pos_id";


    public ShareService() {
        super("ShareService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String mPostId = intent.getStringExtra(EXTRA_POST_ID);
            ComponentName mChosenComponent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mChosenComponent = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
            }
//            Log.d(TAG, mChosenComponent.flattenToString());

            try {
                JSONObject object = new JSONObject();

//                Log.d(TAG, mPostId);
//                Log.d(TAG, mChosenComponent.flattenToString());

                object.put("post", mPostId);
                if (mChosenComponent != null) {
                    object.put("type", mChosenComponent.getPackageName());
                }else{
                    object.put("type", "undefined");
                }
                TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:social", object);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            stopSelf();
        }
    }
}
