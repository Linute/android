package com.linute.linute.MainContent.FindFriends.FindFriendsFragment;

import android.content.Context;

import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.BaseRequestPresenter;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.RequestCallbackView;

import java.util.Map;

/**
 * Created by QiFeng on 9/24/16.
 */
public class FindFriendsSearchPresenter extends BaseRequestPresenter<FriendSearchUser> {

    protected BaseFindFriendsInteractor mFindFriendsInteractor;

    public static final int TYPE_FB = 0;
    public static final int TYPE_SEARCH = 1;

    public FindFriendsSearchPresenter(RequestCallbackView<FriendSearchUser> friendsView, int type) {
        super(friendsView);
        mFindFriendsInteractor = type == TYPE_FB ? new FindFriendsFbInteractor() : new FindFriendsInteractor();
    }

    @Override
    public void request(Context context, Map<String, Object> params, boolean loadMore) {
        mFindFriendsInteractor.query(context, params, loadMore, this);
    }

    @Override
    public void cancelRequest() {
        mFindFriendsInteractor.cancelRequest();
    }

    public boolean originalListLoaded(){
        return mFindFriendsInteractor.mInitialListLoaded;
    }
}
