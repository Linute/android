package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 1/12/16.
 */
public class LSDKCollege {

    private static String mToken;


    public LSDKCollege(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }

    public Call getColleges(Map<String, String> params, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"colleges"};
        return API_Methods.get(path, header,params, callback);
    }

}
