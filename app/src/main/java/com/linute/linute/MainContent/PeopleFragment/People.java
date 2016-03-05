package com.linute.linute.MainContent.PeopleFragment;

import org.json.JSONObject;

/**
 * Created by Arman on 1/8/16.
 */
public class People {
    private String mProfileImage;
    private String mName;
    private String mID;
    private String mDate; //will we used as distance as well

    private boolean mFriend;

    private String mStatus;

    public People(String profileImage, String name, String ID, String date, boolean friend, String status) {
        mProfileImage = profileImage;
        mName = name;
        mID = ID;
        mDate = date;
        mFriend = friend;
        mStatus = status;
    }

    public String getStatus(){
        return mStatus;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public String getName() {
        return mName;
    }

    public String getID() {
        return mID;
    }

    public String getDate() {
        return mDate;
    }

    public boolean isFriend() {
        return mFriend;
    }

    public void setFriend(boolean friend) {
        mFriend = friend;
    }
}
