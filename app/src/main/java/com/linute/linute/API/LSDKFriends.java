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
 * Created by QiFeng on 2/6/16.
 */
public class LSDKFriends {

    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;


    public LSDKFriends(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }


    public Call getFriends(String userId, boolean following, String skip, Callback callback){
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        Map<String, String> param = new HashMap<>();
        param.put("action[0]", following ? "following" : "follower");
        param.put("owner", userId);
        param.put("limit", "25");
        param.put("skip", skip);

        return API_Methods.get(new String[] {"activities"},header, param, callback);
    }

}
