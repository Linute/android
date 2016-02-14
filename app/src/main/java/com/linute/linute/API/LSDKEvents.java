package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by QiFeng on 12/12/15.
 */
public class LSDKEvents {

    // where user information will be
    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;

    public LSDKEvents(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }

    public Call getEvents(boolean friendsOnly,Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        String[] path = {"events", friendsOnly ? "friends" : "discover"};


        return API_Methods.get(path, header, param, callback);
    }

    public Call getEventWithId(String id, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        String[] path = {"events", id};

        return API_Methods.get(path, header, null, callback);
    }

    public Call getComments(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        String[] path = {"comments"};

        return API_Methods.get(path, header, param, callback);
    }

    public Call postEvent(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        return API_Methods.post("events", header, param, callback);
    }

    public Call postComment(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        return API_Methods.post("comments", header, param, callback);
    }

    public Call postLike(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        return API_Methods.post("likes", header, param, callback);
    }

    public Call updateLike(Map<String, Object> param, String eventId,
                           Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        return API_Methods.delete("likes/" + eventId, header, param, callback);
    }

    public Call reportEvent(int reason, String postID, Callback callback){
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        Map<String, Object> params = new HashMap<>();
        params.put("event", postID);
        params.put("reason", reason);

        return API_Methods.post("reports", header, params, callback);
    }

    public Call deleteEvent(String postID, Callback callback){
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        Map<String, Object> params = new HashMap<>();
        params.put("id", postID);
        params.put("isDeleted", 1);

        return API_Methods.put("events/"+postID, header,params,callback);
    }

}
