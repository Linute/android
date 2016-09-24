package com.linute.linute.MainContent.FindFriends;

import android.content.Context;

import java.util.Map;

/**
 * Created by QiFeng on 9/24/16.
 */
public class FindFriendsSearchPresenter extends BaseRequestPresenter {

    protected FindFriendsInteractor mFindFriendsInteractor;

    public FindFriendsSearchPresenter(RequestCallbackView friendsView) {
        super(friendsView);
        mFindFriendsInteractor = new FindFriendsInteractor();
    }

    @Override
    public void request(Context context, Map<String, Object> params) {
        mFindFriendsInteractor.search(context,params, this);
    }

    @Override
    public void cancelRequest() {
        mFindFriendsInteractor.cancelRequest();
    }


}
