package com.linute.linute.API;

import android.content.Context;

import com.linute.linute.MainContent.Uploading.CountingRequestBody;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 12/12/15.
 */
public class LSDKEvents {

    private String mToken;

    public LSDKEvents(Context context) {
        mToken = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userToken","");
    }

    public Call getEvents(String ending, Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"events", ending /*"hot" : "discover"*/};
        return API_Methods.get(path, header, param, callback);
    }


    public Call getEventWithId(String id, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"events", id};
        return API_Methods.get(path, header, null, callback);
    }

    public Call getPollWithId(String id, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        String[] path = {"polls", id};
        return API_Methods.get(path, header, null, callback);
    }

    public Call getComments(Map<String, Object> param, Callback callback) {
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        String[] path = {"comments"};

        return API_Methods.get(path, header, param, callback);
    }

    public Response postEvent(String token, Map<String, Object> param, CountingRequestBody.Listener listener) throws IOException {
        Map<String, String> header = API_Methods.getMainHeader(token);
        return API_Methods.postWithProgress("events", header, param, listener);
    }

    public Call reportEvent(int reason, String postID, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, Object> params = new HashMap<>();
        params.put("event", postID);
        params.put("reason", reason);

        return API_Methods.post("reports", header, params, callback);
    }

    public Call deleteEvent(String postID, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, Object> params = new HashMap<>();
        params.put("id", postID);
        params.put("isDeleted", 1);

        return API_Methods.put("events/"+postID, header,params,callback);
    }

    public Call revealEvent(String postID, boolean makeAnon, boolean wasPrivacyChanged, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, Object> params = new HashMap<>();
        params.put("privacy", makeAnon ? 1 : 0);
        params.put("isPrivacyChanged", !wasPrivacyChanged);


        return API_Methods.put("events/"+postID, header,params,callback);
    }

    public Call reportComment(String commentId, String userId, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);

        Map<String, Object> params = new HashMap<>();
        params.put("owner", userId);
        params.put("comment", commentId);
        params.put("reason", 2);

        return API_Methods.post("reports", header, params, callback);
    }

    public Call revealComment(String commentId,boolean makeAnon, boolean wasPrivacyChanged, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        Map<String, Object> params = new HashMap<>();
        params.put("privacy", makeAnon ? 1 : 0);
        params.put("isPrivacyChanged", !wasPrivacyChanged);

        return API_Methods.put("comments/"+commentId, header, params, callback);
    }

    public Call deleteComment(String commentId, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", 1);

        return API_Methods.put("comments/"+commentId, header, params, callback);
    }

    public Call getPolls(String trend, Callback callback){
        Map<String, String> header = API_Methods.getMainHeader(mToken);
        Map<String, Object> params = new HashMap<>();

        if (trend != null){
            params.put("trend", trend);
        }

        return API_Methods.get(new String[]{"polls"}, header, params, callback);
    }

}
