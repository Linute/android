package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
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

    public Call getRoom(String id, Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"rooms", id};
        return API_Methods.get(path, header, param, callback);
    }

    public Call getRooms(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"rooms"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call getUsers(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        String[] path = {"friends"};
        return API_Methods.get(path, header, param, callback);
    }

    public Call getUsersAndRooms(Map<String, Object> param, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        String path = "rooms/search";
        return API_Methods.post(path, header, param, callback);
    }

    public Call getChat(Map<String, Object> param, Callback callback) {
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

    public Call setGroupNameAndPhoto(String roomId, String name, String imagePath, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        Map<String, Object> params = new HashMap<>();
        if(name != null) {
            params.put("name", name);
        }
        if(imagePath != null) {
            try {
                params.put("image", Utils.encodeFileBase64(new File(imagePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return API_Methods.put("/rooms/"+roomId,header, params, callback);

    }
}
