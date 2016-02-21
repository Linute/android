package com.linute.linute.API;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 1/12/16.
 */
public class LSDKCollege {
    private static SharedPreferences mSharedPreferences;

    private static String mToken;


    public LSDKCollege(Context context) {
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mToken = mSharedPreferences.getString("userToken","");
    }

    public Call getColleges(Map<String, String> params, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"colleges"};
        return API_Methods.get(path, header,params, callback);
    }

}
