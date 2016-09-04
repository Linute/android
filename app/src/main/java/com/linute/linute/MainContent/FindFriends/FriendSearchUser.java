package com.linute.linute.MainContent.FindFriends;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/16/16.
 */
public class FriendSearchUser implements Parcelable {

    private String mProfileImage;
    private String mUserId;
    private String mFirstName;
    private String mLastName;
    private String mFullName;
    private boolean mIsFollowing = false;

    public FriendSearchUser(JSONObject user)throws JSONException{
        mProfileImage = getStringFromJson("profileImage", user);
        mUserId = user.getString("id");

        mFirstName = getStringFromJson("firstName", user);
        mLastName = getStringFromJson("lastName", user);

        if (mFirstName == null) mFirstName = "";
        if (mLastName == null) mLastName = "";

        mFullName = mFirstName + " " + mLastName;

        try {
            JSONObject friend = user.getJSONObject("friend");
            setFollowing(friend);
        }catch (JSONException e){
            mIsFollowing = false;
        }
    }


    public FriendSearchUser(JSONObject owner, JSONObject friend) throws JSONException {
        mProfileImage = getStringFromJson("profileImage", owner);
        mUserId = owner.getString("id");

        mFirstName = getStringFromJson("firstName", owner);
        mLastName = getStringFromJson("lastName", owner);

        if (mFirstName == null) mFirstName = "";
        if (mLastName == null) mLastName = "";

        mFullName = mFirstName + " " + mLastName;

        if (friend != null) {
            try {
                setFollowing(friend);
            } catch (JSONException e) {
                mIsFollowing = false;
            }
        }else {
            mIsFollowing = false;
        }
    }

    public void setFollowing(JSONObject friendship) throws JSONException {
        String friendUserId = friendship.getString("user");
        mIsFollowing = friendUserId.equals(mUserId) || getBooleanFromJson("followedBack", friendship);
    }


    public static String getStringFromJson(String key, JSONObject object) {
        try {
            return object.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public static boolean getBooleanFromJson(String key, JSONObject object) {
        try {
            return object.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isFollowing() {
        return mIsFollowing;
    }

    public void setFollowing(boolean following) {
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

    public boolean nameContains(String pre) {
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

    private FriendSearchUser(Parcel in) {
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
