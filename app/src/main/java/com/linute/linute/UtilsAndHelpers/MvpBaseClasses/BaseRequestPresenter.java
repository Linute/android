package com.linute.linute.UtilsAndHelpers.MvpBaseClasses;

import android.content.Context;

import com.linute.linute.MainContent.FindFriends.FindFriendsFragment.FriendSearchUser;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by QiFeng on 9/24/16.
 */
public abstract class BaseRequestPresenter<T> implements OnFinishedRequest<T> {

    protected RequestCallbackView<T> mRequestCallbackView;

    public BaseRequestPresenter(RequestCallbackView<T> callbackView){
        mRequestCallbackView = callbackView;
    }

    @Override
    public void onSuccess(ArrayList<T> users, boolean canLoadMore, boolean addToBack) {
        mRequestCallbackView.onSuccess(users, canLoadMore, addToBack);
    }

    @Override
    public void onFailure() {
        mRequestCallbackView.onFailure();
    }

    @Override
    public void onError(String error) {
        mRequestCallbackView.onError(error);
    }


    public abstract void request(Context context, Map<String, Object> params, boolean loadMore);

    public abstract void cancelRequest();
}
