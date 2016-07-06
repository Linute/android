package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 1/8/16.
 */
public class LSDKActivity {


    private String mToken;
    private String mUserId;


    public LSDKActivity(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mToken = sharedPreferences.getString("userToken","");
        mUserId = sharedPreferences.getString("userID", "");
    }


    public Call getActivities(int skip, int limit, Callback callback) {
        HashMap<String, String> header = API_Methods.getMainHeader(mToken);

        HashMap<String, String> params = new HashMap<>();

        if (skip >= 0) {
            params.put("skip", skip + "");
        }

        params.put("limit", limit+"");

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

        params.put("action[15]", "liked comment");

        params.put("owner", mUserId);

        String[] path = {"activities"};
        return API_Methods.get(path, header, params, callback);
    }
}
