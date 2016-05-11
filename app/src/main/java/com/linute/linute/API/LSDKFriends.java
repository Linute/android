package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 2/6/16.
 */
public class LSDKFriends {

    private static String mToken;


    public LSDKFriends(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }


    public Call getFriends(String userId, boolean following, String skip, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, String> param = new HashMap<>();
        param.put("action[0]", following ? "following" : "follower");
        param.put("owner", userId);
        param.put("limit", "25");
        param.put("skip", skip);

        return API_Methods.get(new String[] {"activities"},header, param, callback);
    }

    public Call getFriendsForMention(String userId, String fullname, String skip, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, String> param = new HashMap<>();
        param.put("owner", userId);
        param.put("limit", "25");
        param.put("skip", skip);
        param.put("fullName", fullname);

        return API_Methods.get(new String[] {"friends"},header, param, callback);
    }




}
