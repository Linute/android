package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by Arman on 1/8/16.
 */
public class LSDKPeople {

    private static String mToken;

    public LSDKPeople(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }

    public Call postFollow(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.post("friends", header, param, callback);
    }

    public Call putUnfollow(Map<String, Object> param, String friendshipID, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        return API_Methods.put("friends/" + friendshipID, header, param, callback);
    }

    public Call getPeople(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"people"};
        return API_Methods.get(path, header, param, callback);
    }

}
