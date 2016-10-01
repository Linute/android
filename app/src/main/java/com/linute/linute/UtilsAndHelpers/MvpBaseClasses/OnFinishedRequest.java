package com.linute.linute.UtilsAndHelpers.MvpBaseClasses;

import com.linute.linute.MainContent.FindFriends.FindFriendsFragment.FriendSearchUser;

import java.util.ArrayList;

/**
 * Created by QiFeng on 9/24/16.
 */
public interface OnFinishedRequest {

    void onSuccess(ArrayList<FriendSearchUser> users, boolean canLoadMore);
    void onFailure();
    void onError(String error);
}
