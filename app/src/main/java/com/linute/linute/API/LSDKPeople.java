package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by Arman on 1/8/16.
 */
public class LSDKPeople {

    private static SharedPreferences mSharedPreferences;
    private static String mToken;

    public LSDKPeople(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mToken = mSharedPreferences.getString("userToken","");
    }

    public Call getPeople(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"people"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call postFollow(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.post("friends", header, param, callback);
    }

    public Call putUnfollow(Map<String, Object> param, String friendshipID, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.put("friends/" + friendshipID, header, param, callback);
    }


    public Call getPeoplNearMe(Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, String> param = new HashMap<>();
        param.put("skip", "0");
        param.put("limit", "20");

        return API_Methods.get(new String[] {"geo"},header, param, callback);
    }
}
