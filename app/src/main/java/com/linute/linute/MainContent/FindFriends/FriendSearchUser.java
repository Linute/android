package com.linute.linute.MainContent.FindFriends;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/16/16.
 */
public class FriendSearchUser implements Parcelable {

    public final String profileImage;
    public final String userId;
    public final String firstName;
    public final String lastName;
    public final String fullName;
    public final String collegeName;
    private boolean mIsFollowing = false;

    public FriendSearchUser(JSONObject user)throws JSONException{
        profileImage = getStringFromJson("profileImage", user);
        userId = user.getString("id");

        firstName = getStringFromJson("firstName", user);
        lastName = getStringFromJson("lastName", user);

        fullName = firstName + " " + lastName;

        String college;
        try{
            college = user.getJSONObject("college").getString("name");
        }catch (JSONException e){
            college = "";
        }

        collegeName = college;

        try {
            JSONObject friend = user.getJSONObject("friend");
            setFollowing(friend);
        }catch (JSONException e){
            mIsFollowing = false;
        }
    }


    public FriendSearchUser(JSONObject owner, JSONObject friend) throws JSONException {
        profileImage = getStringFromJson("profileImage", owner);
        userId = owner.getString("id");

        firstName = getStringFromJson("firstName", owner);
        lastName = getStringFromJson("lastName", owner);

        fullName = firstName + " " + lastName;

        String college;
        try{
            college = owner.getJSONObject("college").getString("name");
        }catch (JSONException e){
            college = "";
        }

        collegeName = college;

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
        mIsFollowing = friendUserId.equals(userId) || getBooleanFromJson("followedBack", friendship);
    }


    public static String getStringFromJson(String key, JSONObject object) {
        try {
            return object.getString(key);
        } catch (JSONException e) {
            return "";
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


    public boolean nameContains(String pre) {
        return fullName.toLowerCase().contains(pre.toLowerCase());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profileImage);
        dest.writeString(userId);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(collegeName);
        dest.writeByte((byte) (mIsFollowing ? 1 : 0));
    }

    private FriendSearchUser(Parcel in) {
        profileImage = in.readString();
        userId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        collegeName = in.readString();
        mIsFollowing = in.readByte() != 0;
        fullName = firstName + " " + lastName;
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
