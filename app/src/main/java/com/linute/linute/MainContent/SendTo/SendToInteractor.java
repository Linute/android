package com.linute.linute.MainContent.SendTo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.linute.linute.API.LSDKFriends;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.BaseInteractor;
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
 * Created by QiFeng on 10/20/16.
 */
public class SendToInteractor extends BaseInteractor<SendToItem> {

    private boolean mInitialListLoaded;
    private ArrayList<SendToItem> mInitList = new ArrayList<>();
    private String mQuery;

    @Override
    public void query(Context context, Map<String, Object> params, boolean loadMore, final OnFinishedRequest<SendToItem> onFinishedQuery) {
        String query = (String) params.get("fullName");

        if (!query.equals(mQuery) || loadMore) {
            mQuery = query;
            Handler handler = new Handler(Looper.getMainLooper());
            if (mQuery.trim().isEmpty() && !loadMore) {
                if (mInitialListLoaded)
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onFinishedQuery.onSuccess(mInitList, false, false);
                        }
                    });

                else initList(context, params, onFinishedQuery);
            } else if (mInitialListLoaded) {
                filter(handler, query.toLowerCase(), onFinishedQuery);
            }
        }
    }


    private void initList(Context context, Map<String, Object> params, final OnFinishedRequest<SendToItem> onFinishedQuery) {
        if (mCall != null)
            mCall.cancel();


        mCall = new LSDKFriends(context).getSendToList(params, new Callback() {
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

                        //mInitSkip = object.getInt("skip");

                        mInitList = parseJson(object);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onFinishedQuery.onSuccess(mInitList, false, false);
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
        });
    }

    private void filter(Handler handler, String name, final OnFinishedRequest<SendToItem> onFinishedQuery) {
        final ArrayList<SendToItem> filtered = new ArrayList<>();

        for (SendToItem item : mInitList) {
            if (item.getName().toLowerCase().contains(name))
                filtered.add(item);
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                onFinishedQuery.onSuccess(filtered, false, false);
            }
        });

    }


    private ArrayList<SendToItem> parseJson(JSONObject object) {

        ArrayList<SendToItem> sendToItems = new ArrayList<>();
        try {
            JSONArray friends = object.getJSONArray("friends");

            JSONObject owner;

            for (int i = 0; i < friends.length(); i++) {
                owner = friends.getJSONObject(i).getJSONObject("owner");
                sendToItems.add(new SendToItem(
                        SendToItem.TYPE_PERSON, owner.getString("fullName"), owner.getString("id"), owner.getString("profileImage"))
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sendToItems;
    }

}
