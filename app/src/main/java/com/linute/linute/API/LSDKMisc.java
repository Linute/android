package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by mikhail on 11/18/16.
 */

public class LSDKMisc {
    private String mToken;

    public LSDKMisc(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }

    public Call getStickers(Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        return API_Methods.get(new String[]{"memes"}, header, new HashMap<String, Object>(), callback);
    }

    public Call getFilters(Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        return API_Methods.get(new String[]{"filters"}, header, new HashMap<String, Object>(), callback);
    }

}
