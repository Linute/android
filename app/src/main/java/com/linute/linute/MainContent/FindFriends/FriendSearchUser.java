package com.linute.linute.MainContent.FindFriends;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/16/16.
 */
public class FriendSearchUser implements Parcelable{

    private String mProfileImage;
    private String mUserId;
    private String mFirstName;
    private String mLastName;
    private String mFullName;
    private boolean mIsFollowing = false;


    public FriendSearchUser(JSONObject json){
        mProfileImage = getStringFromJson("profileImage", json);
        mUserId = getStringFromJson("id", json);

        mFirstName = getStringFromJson("firstName", json);
        mLastName = getStringFromJson("lastName", json);

        if (mFirstName == null) mFirstName = "";
        if (mLastName == null) mLastName = "";

        mFullName = mFirstName + " "+ mLastName;

        JSONObject friend = getJsonObjectFromJson("friend", json);

        if (friend != null) {
            String friendUserId = getStringFromJson("user", friend);
            if (friendUserId == null) return;


            if (friendUserId.equals(mUserId)) //means you already followed him
                mIsFollowing = true; //your following him

            else  //he followed you first, check if your following him back
                mIsFollowing = getBooleanFromJson("followedBack", friend);

        }

    }

    public static JSONObject getJsonObjectFromJson(String key, JSONObject jsonObject){
        try {
            return jsonObject.getJSONObject(key);
        }catch (JSONException e) {
            return null;
        }
    }

    public static String getStringFromJson(String key, JSONObject object){
        try {
            return object.getString(key);
        }catch (JSONException e){
            return null;
        }
    }

    public static boolean getBooleanFromJson(String key, JSONObject object){
        try {
            return object.getBoolean(key);
        }catch (JSONException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFollowing(){
        return mIsFollowing;
    }

    public void setFollowing(boolean following){
        mIsFollowing = following;
    }


    public String getProfileImage() {
        return mProfileImage;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getFullName() {
        return mFullName;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public boolean nameContains(String pre){
        return mFullName.toLowerCase().contains(pre.toLowerCase());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProfileImage);
        dest.writeString(mUserId);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeByte((byte) (mIsFollowing ? 1 : 0));
    }

    private FriendSearchUser(Parcel in){
        mProfileImage = in.readString();
        mUserId = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mIsFollowing = in.readByte() != 0;

        mFullName = mFirstName + " " + mLastName;
    }

    public static final Creator<FriendSearchUser> CREATOR = new Creator<FriendSearchUser>() {
        @Override
        public FriendSearchUser createFromParcel(Parcel source) {
            return new FriendSearchUser(source);
        }

        @Override
        public FriendSearchUser[] newArray(int size) {
            return new FriendSearchUser[size];
        }
    };
}
