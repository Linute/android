package com.linute.linute.MainContent.FindFriends;

import android.content.Context;

import com.linute.linute.API.LSDKFriendSearch;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 9/24/16.
 */
public class FindFriendsInteractor {


    private Call mCall;

    public void search(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {
        if (mCall != null) mCall.cancel();

        mCall = new LSDKFriendSearch(context).searchFriendByName(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    onFinishedQuery.onFailure();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        boolean canLoadMore = false; //// TODO: 9/24/16 fix
                        onFinishedQuery.onSuccess(parseJson(new JSONObject(response.body().string())), canLoadMore);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFinishedQuery.onError("error parsing");
                    }
                } else {
                    onFinishedQuery.onError(response.body().string());
                }
            }
        });
    }

    public ArrayList<FriendSearchUser> parseJson(JSONObject object) {

        return null;
    }

    public void cancelRequest(){
        mCall.cancel();
    }

}
