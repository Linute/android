package com.linute.linute.UtilsAndHelpers.MvpBaseClasses;

import com.linute.linute.MainContent.FindFriends.FriendSearchUser;

import java.util.ArrayList;

/**
 * Created by QiFeng on 9/24/16.
 */
public interface RequestCallbackView {
    void onSuccess(ArrayList<FriendSearchUser> list, boolean canLoadMore);
    void onError(String response);
    void onFailure();
}
