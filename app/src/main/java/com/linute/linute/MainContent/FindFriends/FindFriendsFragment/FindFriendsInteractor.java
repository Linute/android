package com.linute.linute.MainContent.FindFriends.FindFriendsFragment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.linute.linute.API.LSDKFriendSearch;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.OnFinishedRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 9/24/16.
 */
public class FindFriendsInteractor extends BaseFindFriendsInteratctor {

    private Call mInitCall;

    @Override
    public void query(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {
        if (mCall != null) mCall.cancel();

        String query = (String) params.get("fullName");

        if (!query.equals(mQuery)) {
            mQuery = query;
            Handler handler = new Handler(Looper.getMainLooper());
            if (mQuery.trim().isEmpty()) {
                if (mInitialListLoaded)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onFinishedQuery.onSuccess(mUnfilteredList, false);
                        }
                    });
                else initList(context, params, onFinishedQuery);
            } else {
                search(context, params, onFinishedQuery);
            }
        }
    }

    private void initList(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {

        mInitCall = new LSDKPeople(context).getPeople(params, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (!call.isCanceled()) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onFinishedQuery.onFailure();
                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Handler handler = new Handler(Looper.getMainLooper());

                        if (response.isSuccessful()) {
                            mInitialListLoaded = true;
                            try {
                                mUnfilteredList = parseInitJson(new JSONObject(response.body().string()));
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCall == null)
                                            onFinishedQuery.onSuccess(mUnfilteredList, false);
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFinishedQuery.onError("error parsing");
                                    }
                                });
                            }
                        } else {
                            final String res = response.body().string();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onFinishedQuery.onError(res);
                                }
                            });
                        }
                    }
                }

        );
    }

    @Override
    protected void search(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("filters", params);

        mCall = new LSDKFriendSearch(context).searchFriendByName(filters, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (!call.isCanceled()) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onFinishedQuery.onFailure();
                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Handler handler = new Handler(Looper.getMainLooper());
                        if (response.isSuccessful()) {
                            mInitialListLoaded = true;
                            try {
                                final ArrayList<FriendSearchUser> friendSearchUsers = parseJson(new JSONObject(response.body().string()));
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFinishedQuery.onSuccess(friendSearchUsers, false);
                                    }
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFinishedQuery.onError("error parsing");
                                    }
                                });
                            }
                        } else {
                            final String res = response.body().string();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onFinishedQuery.onError(res);
                                }
                            });
                        }
                    }
                }

        );
    }


    @Override
    protected ArrayList<FriendSearchUser> parseJson(JSONObject object) throws JSONException {

        JSONArray friends = object.getJSONArray("friends");

        ArrayList<FriendSearchUser> tempFriends = new ArrayList<>();

        if (friends != null) {
            for (int i = 0; i < friends.length(); i++) {
                try {
                    tempFriends.add(new FriendSearchUser(friends.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return tempFriends;
    }

    protected ArrayList<FriendSearchUser> parseInitJson(JSONObject object) throws JSONException {
        JSONArray friends = object.getJSONArray("people");

        ArrayList<FriendSearchUser> users = new ArrayList<>();
        JSONObject user;
        JSONObject friend;
        if (friends != null){
            for (int i = 0 ; i < friends.length(); i++){
                try {
                    user = friends.getJSONObject(i);

                    try{
                        friend = user.getJSONObject("friend");
                    }catch (JSONException e){
                        friend = null;
                    }

                    users.add(new FriendSearchUser(user.getJSONObject("owner"), friend));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        return users;
    }


    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mInitCall != null) mInitCall.cancel();
    }
}
