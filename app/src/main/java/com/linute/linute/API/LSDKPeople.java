package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import java.util.Map;

/**
 * Created by Arman on 1/8/16.
 */
public class LSDKPeople {

    private static SharedPreferences mSharedPreferences;
    private static String mEncodedToken;

    public LSDKPeople(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }

    public Call getPeople(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        String[] path = {"people"};

        return API_Methods.get(path, header, param, callback);
    }

    public Call postFollow(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.post("friends", header, param, callback);
    }

    public Call putUnfollow(Map<String, Object> param, String friendshipID, Callback callback) {
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", null),
                mSharedPreferences.getString("password", null),
                mEncodedToken);

        return API_Methods.put("friends/" + friendshipID, header, param, callback);
    }
}
