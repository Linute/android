package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Arman on 1/16/16.
 */
public class LSDKChat {
    private static SharedPreferences mSharedPreferences;
    private static String mEncodedToken;

    public LSDKChat(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }

    public Call getRooms(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"rooms"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call getUsers(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"friends"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call checkUserConvo(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"rooms"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call newChat(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.post("messages", header, param, callback);
    }

    public Call getChat(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"messages"};
        return API_Methods.get(path, header, param, callback);
    }


    public Call getPastMessages(JSONArray users,Callback callback){

        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        Map<String, Object> params = new HashMap<>();
        params.put("users", users);

        return API_Methods.post("rooms", header, params, callback);
    }
}
