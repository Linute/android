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
 * Created by QiFeng on 1/8/16.
 */
public class LSDKActivity {

    // where user information will be
    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;


    public LSDKActivity(Context context){

        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }


    public Call getActivities(Callback callback){
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

//
//        actions.put("comment");
//        actions.put("like");
//        actions.put("facebook share");
//        actions.put("mentioned");
//        actions.put("follower");
//        actions.put("friend joined");

        Map<String, String> params = new HashMap<>();
        params.put("skip", "0");
        params.put("limit", "25");
        params.put("action[0]", "comment");
        params.put("action[1]", "like");
        params.put("action[2]", "facebook share");
        params.put("action[3]", "mentioned");
        params.put("action[4]", "follower");
        params.put("action[5]", "friend joined");
        params.put("owner", mSharedPreferences.getString("userID", ""));

        String[] path = {"activities"};
        return API_Methods.get(path, header, params, callback);
    }
}
