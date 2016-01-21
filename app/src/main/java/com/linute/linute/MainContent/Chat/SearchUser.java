package com.linute.linute.MainContent.Chat;

/**
 * Created by Arman on 1/19/16.
 */
public class SearchUser {
    private String mUserId;
    private String mUserImage;
    private String mUserName;

    public SearchUser() {
        mUserId = mUserImage = mUserName = "";
    }

    public SearchUser(String userId, String userImage, String userName) {
        mUserId = userId;
        mUserImage = userImage;
        mUserName = userName;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserImage() {
        return mUserImage;
    }

    public String getUserName() {
        return mUserName;
    }
}
