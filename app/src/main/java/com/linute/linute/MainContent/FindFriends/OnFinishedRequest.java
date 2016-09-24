package com.linute.linute.MainContent.FindFriends;

import java.util.ArrayList;

/**
 * Created by QiFeng on 9/24/16.
 */
public interface OnFinishedRequest {

    void onSuccess(ArrayList<FriendSearchUser> users, boolean canLoadMore);
    void onFailure();
    void onError(String error);
}
