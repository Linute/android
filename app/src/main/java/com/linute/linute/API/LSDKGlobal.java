package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by QiFeng on 5/14/16.
 */
public class LSDKGlobal {
    private static String mToken;

    public LSDKGlobal(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }

    public Call getTrending(Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        return API_Methods.get(new String[] {"trends"}, header, new HashMap<String, String>(), callback);
    }


    public Call getPosts(Map<String, String> param, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        return API_Methods.get(new String[]{"events", "trend"}, header, param, callback);
    }
}
