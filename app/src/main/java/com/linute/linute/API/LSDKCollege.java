package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import java.util.Map;

/**
 * Created by QiFeng on 1/12/16.
 */
public class LSDKCollege {
    private static SharedPreferences mSharedPreferences;

    private static String mEncodedToken;


    public LSDKCollege(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mEncodedToken = Utils.encode_base64(mSharedPreferences.getString(QuickstartPreferences.OUR_TOKEN, null));
    }

    public Call getColleges(Map<String, String> params, Callback callback){
        Map<String, String> header = API_Methods.getHeaderWithAuthUser(
                mSharedPreferences.getString("email", ""),
                mSharedPreferences.getString("password", ""),
                mEncodedToken);
        String[] path = {"colleges"};
        return API_Methods.get(path, header,params, callback);
    }

}
