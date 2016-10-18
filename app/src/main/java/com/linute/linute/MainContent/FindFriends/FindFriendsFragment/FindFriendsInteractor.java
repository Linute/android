package com.linute.linute.MainContent.FindFriends.FindFriendsFragment;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

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

    private int mInitLimit;
    private int mInitSkip;

    private int mCurrSkip;
    private int mCurrLimit;

    private boolean mInitCanLoadMore;

    @Override
    public void query(Context context, Map<String, Object> params, boolean loadMore, final OnFinishedRequest onFinishedQuery) {
        if (mCall != null) mCall.cancel();

        String query = (String) params.get("fullName");

        if (!query.equals(mQuery) || loadMore) {
            mQuery = query;
            Handler handler = new Handler(Looper.getMainLooper());
            if (mQuery.trim().isEmpty() && !loadMore) {
                if (mInitialListLoaded)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCurrSkip = mInitSkip;
                            mCurrLimit = mInitLimit;
                            onFinishedQuery.onSuccess(mUnfilteredList, mInitCanLoadMore, false);
                        }
                    });

                else initList(context, params, onFinishedQuery);
            } else {
                search(context, params, loadMore, onFinishedQuery);
            }
        }
    }

    private void initList(Context context, Map<String, Object> params, final OnFinishedRequest onFinishedQuery) {
        if (mInitCall != null)
            mInitCall.cancel();

        mInitCall = new LSDKFriendSearch(context).searchFriendByName(getFiltersAndParams(params, false), new Callback() {
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
                                JSONObject object = new JSONObject(response.body().string());

                                mInitSkip = object.getInt("skip");
                                mInitLimit = object.getInt("limit");

                                mCurrSkip = mInitSkip;
                                mCurrLimit = mInitLimit;

                                try {
                                    mInitCanLoadMore = object.getBoolean("lastRequest");
                                }catch (JSONException e){
                                    e.printStackTrace();
                                    mInitCanLoadMore = false;
                                }
//
//                                Log.i("test", "onResponse: "+mCurrLimit);
//                                Log.i("test", "onResponse: "+mCurrSkip);

                                mUnfilteredList = parseJson(object);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCall == null)
                                            onFinishedQuery.onSuccess(mUnfilteredList, mInitCanLoadMore, false);
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
    protected void search(Context context, Map<String, Object> params, final boolean loadMore, final OnFinishedRequest onFinishedQuery) {

        mCall = new LSDKFriendSearch(context).searchFriendByName(getFiltersAndParams(params, loadMore), new Callback() {
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

                            try {
                                JSONObject object = new JSONObject(response.body().string());
                                if (!loadMore) {
                                    mCurrSkip = object.getInt("skip");
                                }

                                mCurrLimit = object.getInt("limit");
                                mCurrSkip = object.getInt("skip");

                                boolean canLoadTemp;
                                try {
                                    canLoadTemp = object.getBoolean("lastRequest");
                                }catch (JSONException e){
                                    e.printStackTrace();
                                    canLoadTemp = false;
                                }

                                final boolean canLoad = canLoadTemp;

                                final ArrayList<FriendSearchUser> friendSearchUsers = parseJson(object);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFinishedQuery.onSuccess(friendSearchUsers, canLoad, loadMore);
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


    @Override
    public void cancelRequest() {
        super.cancelRequest();
        if (mInitCall != null) mInitCall.cancel();
    }


    private HashMap<String, Object> getFiltersAndParams(Map<String, Object> params, boolean loadMore) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("filters", params);

        filters.put("skip", mCurrSkip);

        if (mCurrLimit > 0) {
            filters.put("limit", mCurrLimit);
        }

        return filters;
    }
}
