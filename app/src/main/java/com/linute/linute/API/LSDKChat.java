package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Created by Arman on 1/16/16.
 */
public class LSDKChat {
    private String mToken;

    public LSDKChat(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }

    public Call getRooms(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"rooms"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call getUsers(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        String[] path = {"friends"};
        return API_Methods.get(path, header, param, callback);
    }


    public Call getChat(Map<String, String> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"messages"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call getPastMessages(JSONArray users,Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        Map<String, Object> params = new HashMap<>();
        params.put("users", users);
        return API_Methods.post("rooms", header, params, callback);
    }
}
