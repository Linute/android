package com.linute.linute.MainContent.FindFriends;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.linute.linute.API.LSDKFriendSearch;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.OnFinishedRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 9/27/16.
 */
public class FindFriendsFbInteractor extends BaseFindFriendsInteratctor {

    @Override
    public void query(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {
        Handler handler = new Handler(Looper.getMainLooper());
        String query = (String) params.get("fullName");

        if (mQuery == null || !query.equals(mQuery)) {
            mQuery = query;
            if (mInitialListLoaded) {
                if (mQuery.trim().isEmpty()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onFinishedQuery.onSuccess(mUnfilteredList, false);
                        }
                    });
                } else {
                    filterList(mQuery, onFinishedQuery);
                }
            } else if (mCall == null) {
                search(context, params, onFinishedQuery);
            }
        }
    }

    @Override
    protected void search(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {
        mCall = new LSDKFriendSearch(context).searchFriendByFacebook(params, new Callback() {
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
                                mUnfilteredList = parseJson(new JSONObject(response.body().string()));
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFinishedQuery.onSuccess(mQuery == null || mQuery.isEmpty() ? mUnfilteredList : getFilteredList(mQuery), false);
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

    private void filterList(final String name, final OnFinishedRequest request) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                request.onSuccess(getFilteredList(name), false);
            }
        });
    }

    private ArrayList<FriendSearchUser> getFilteredList(String name) {
        ArrayList<FriendSearchUser> users = new ArrayList<>();
        for (FriendSearchUser user : mUnfilteredList) {
            if (user.nameContains(name)) {
                users.add(user);
            }
        }

        return users;
    }

    @Override
    protected ArrayList<FriendSearchUser> parseJson(JSONObject object) throws JSONException {
        final JSONArray friends = object.getJSONArray("friends");
        final ArrayList<FriendSearchUser> users = new ArrayList<>();

        for (int i = 0; i < friends.length(); i++) {
            try {
                FriendSearchUser user = new FriendSearchUser(friends.getJSONObject(i));
                users.add(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return users;

    }
}
