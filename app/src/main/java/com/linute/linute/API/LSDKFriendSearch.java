package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 1/16/16.
 */
public class LSDKFriendSearch {


    private String mToken;


    public LSDKFriendSearch(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }


    public Call searchFriendByName(String name, Callback callback){

        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, String> fullName = new HashMap<>();
        fullName.put("fullName", name);

        Map<String, Object> param = new HashMap<>();
        param.put("filters", fullName);

        return API_Methods.post("friends/search", header, param, callback);
    }


    public Call searchFriendByName(Map<String, Object> params, Callback callback){

        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.post("friends/search", header, params, callback);
    }


    public Call searchFriendByFacebook(Map<String, Object> param, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.post("friends/facebook", header, param, callback);
    }

}