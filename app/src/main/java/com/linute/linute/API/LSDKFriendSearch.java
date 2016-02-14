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

/**
 * Created by QiFeng on 1/16/16.
 */
public class LSDKFriendSearch {

    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;


    public LSDKFriendSearch(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }


    public Call searchFriendByName(String name, Callback callback){

        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        Map<String, String> fullName = new HashMap<>();
        fullName.put("fullName", name);

        Map<String, Object> param = new HashMap<>();
        param.put("filters", fullName);

        return API_Methods.post("friends/search", header, param, callback);
    }


    public Call searchFriendByFacebook(String fbToken, Callback callback){

        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        Map<String, Object> param = new HashMap<>();
        param.put("token", fbToken);


        return API_Methods.post("friends/facebook", header, param, callback);
    }

    public Call searchFriendByContacts(JSONArray phone, JSONArray email, Callback callback){

        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);

        Map<String, Object> param = new HashMap<>();
        param.put("phones", phone);
        param.put("emails", email);

        return API_Methods.post("friends/contacts", header, param, callback);
    }
}