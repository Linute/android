package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

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

    public Call getEvents(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"events", "discover"};

        return API_Methods.get(path, header, param, callback);
    }

    public Call getEvent(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"comments"};

        return API_Methods.get(path, header, param, callback);
    }

    public Call postEvent(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.post("events", header, param, callback);
    }

    public Call postLike(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.post("likes", header, param, callback);
    }

    public Call updateLike(Map<String, Object> param, String userLiked,
                           Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.put("likes/" + userLiked, header, param, callback);
    }

}
