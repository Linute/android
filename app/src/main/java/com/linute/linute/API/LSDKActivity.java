package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by QiFeng on 1/8/16.
 */
public class LSDKActivity {

    // where user information will be
    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;


    public LSDKActivity(Context context) {

        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }


    public Call getActivities(Integer skip, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        Map<String, String> params = new HashMap<>();
        params.put("skip", skip.toString());
        params.put("limit", "25");

        params.put("action[0]", "commented photo");
        params.put("action[1]", "commented status");
        params.put("action[2]", "liked status");
        params.put("action[3]", "liked photo");
        params.put("action[4]", "facebook share");
        params.put("action[5]", "mentioned");
        params.put("action[6]", "follower");
        params.put("action[7]", "friend joined");

        params.put("owner", mSharedPreferences.getString("userID", ""));

        String[] path = {"activities"};
        return API_Methods.get(path, header, params, callback);
    }

    public Call readActivities(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.post("activities/read", header, param, callback);
    }
}
