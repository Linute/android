package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by mikhail on 11/5/16.
 */

public class LSDKAnalytics {

    private String mToken;
    private String mUserId;


    public LSDKAnalytics(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mToken = sharedPreferences.getString("userToken","");
        mUserId = sharedPreferences.getString("userID", "");
    }

    public Call postOpenedNotification(String notificationId, Callback cb) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("notification", notificationId);
        params.put("action", "opened");
        params.put("user", mUserId);

        return API_Methods.post("analytics/notification-global", API_Methods.getMainHeader(mToken), params, cb);
    }

    public Call postRecievedNotification(String notificationId, Callback cb) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("notification", notificationId);
        params.put("action", "delivered");
        params.put("user", mUserId);

        return API_Methods.post("analytics/notification-global", API_Methods.getMainHeader(mToken), params, cb);
    }

}
