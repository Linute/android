package com.linute.linute.MainContent.FindFriends;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by QiFeng on 9/24/16.
 */
public abstract class BaseRequestPresenter implements OnFinishedRequest {

    protected RequestCallbackView mRequestCallbackView;

    public BaseRequestPresenter(RequestCallbackView callbackView){
        mRequestCallbackView = callbackView;
    }

    @Override
    public void onSuccess(ArrayList<FriendSearchUser> users, boolean canLoadMore) {
        mRequestCallbackView.onSuccess(users, canLoadMore);
    }

    @Override
    public void onFailure() {
        mRequestCallbackView.onFailure();
    }

    @Override
    public void onError(String error) {
        mRequestCallbackView.onError(error);
    }


    public abstract void request(Context context, Map<String, Object> params);

    public abstract void cancelRequest();
}
