package com.linute.linute.UtilsAndHelpers;

import android.os.AsyncTask;
import android.util.Log;

import com.linute.linute.API.API_Methods;
import com.linute.linute.Socket.TaptSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 10/10/16.
 */
public class ImpressionHelper {

    public static final String TAG = ImpressionHelper.class.getSimpleName();

    public static void sendImpressionsAsync(final String collegeId, final String userId, final String postId) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject body = new JSONObject();

                    body.put("college", collegeId);
                    body.put("user", userId);

                    JSONArray mEventIds = new JSONArray();
                    mEventIds.put(postId);
                    body.put("events", mEventIds);

                    TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:impressions", body);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
