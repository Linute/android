package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 1/8/16.
 */
public class LSDKActivity {

    // where user information will be
    private static SharedPreferences mSharedPreferences;

    private static String mToken;


    public LSDKActivity(Context context) {

        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mToken = mSharedPreferences.getString("userToken","");
    }


    public Call getActivities(int skip, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, String> params = new HashMap<>();

        if (skip >= 0) {
            params.put("skip", skip + "");
        }

        params.put("limit", "50");

        params.put("action[0]", "commented photo");
        params.put("action[1]", "commented status");
        params.put("action[2]", "commented video");

        params.put("action[3]", "liked status");
        params.put("action[4]", "liked photo");
        params.put("action[5]", "liked video");

        params.put("action[6]", "also commented status");
        params.put("action[7]", "also commented photo");
        params.put("action[8]", "also commented video");

        params.put("action[9]", "mentioned");
        params.put("action[10]", "follower");
        params.put("action[11]", "friend joined");

//
        params.put("action[12]", "friend posted status");
        params.put("action[13]", "friend posted photo");
        params.put("action[14]", "friend posted video");

        params.put("action[15]", "matched");

        params.put("owner", mSharedPreferences.getString("userID", ""));

        String[] path = {"activities"};
        return API_Methods.get(path, header, params, callback);
    }

    public Call readActivities(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.post("activities/read", header, param, callback);
    }
}
