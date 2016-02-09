package com.linute.linute.MainContent.FriendsList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 2/6/16.
 */
public class Friend {

    private String mUserProfile;
    private String mUserName;
    private String mUserId;

    private String mOwnerProfile;
    private String mOwnerName;
    private String mOwnerId;


    public Friend(JSONObject object){
        JSONObject user = getJsonObject(object, "user");
        JSONObject owner = getJsonObject(object, "owner");

        if (user != null){
            mUserProfile = getString(user, "profileImage");
            mUserName = getString(user, "fullName");
            mUserId = getString(user, "id");
        }
        if (owner != null){
            mOwnerProfile = getString(owner, "profileImage");
            mOwnerName = getString(owner, "fullName");
            mOwnerId = getString(owner, "id");
        }
    }

    public static JSONObject getJsonObject(JSONObject json, String key){
        try {
            return json.getJSONObject(key);
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getString(JSONObject jsonObject, String key){
        try {
            return jsonObject.getString(key);
        }catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    //NOTE: if action == following -- owner is the person you are following
    //NOTE:    action == follower  -- user is the follower

    public String getUserProfile() {
        return mUserProfile;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getOwnerProfile() {
        return mOwnerProfile;
    }

    public String getOwnerName() {
        return mOwnerName;
    }

    public String getOwnerId() {
        return mOwnerId;
    }
}
