package com.linute.linute.MainContent.UpdateFragment;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/6/16.
 */

//TODO: implement mention
public class Update {

    public enum UpdateType {
        UNDEFINED,
        LIKE,
        COMMENT,
        FOLLOW,
        FACEBOOK_SHARE, //need icon
        MENTION, //need icon
        FRIEND_JOIN //need icon
    }

    private String mDescription;
    private UpdateType mUpdateType;

    //time stamp -- uncomment if we decide to use it
    //private long mActionTime; //i.e. when someone liked/commented on/etc post


    private boolean mIsRead;

    private String mActionID;

    //'User' refers to the person performing the action
    // i.e. if 'max liked your picture' -> user = max
    private String mUserId;
    private String mUserFullName;
    private String mUserProfileImageName;

    private String mEventImageName;
    private String mEventID;
    private String mEventTitle;


    public Update(JSONObject json) {
        mUpdateType = getUpdateTypeFromString(getStringFromJson(json, "action"));

        mIsRead = getBooleanFromJson(json, "isRead");

        //uncomment if we decide to use it
        //mActionTime = Utils.getTimeFromString(getStringFromJson(json, "date"));

        mActionID = getStringFromJson(json, "id");

        setUpUserInformation(json);

        //only set up events if we have to
        if (mUpdateType == UpdateType.MENTION ||
                mUpdateType == UpdateType.LIKE ||
                mUpdateType == UpdateType.COMMENT ||
                mUpdateType == UpdateType.FACEBOOK_SHARE)
            setUpEvent(json);

        setUpActionDescription();
    }

    private static UpdateType getUpdateTypeFromString(String action) {
        switch (action) {
            case "like":
                return UpdateType.LIKE;
            case "comment":
                return UpdateType.COMMENT;
            case "follower":
                return UpdateType.FOLLOW;
            case "facebook share":
                return UpdateType.FACEBOOK_SHARE;
            case "mentioned":
                return UpdateType.MENTION;
            case "friend_joined":
                return UpdateType.FRIEND_JOIN;
            default:
                return UpdateType.UNDEFINED;
        }
    }


    private void setUpEvent(JSONObject json) {
        JSONObject event = getJsonObjectFromJson(json, "event");

        if (event == null) return;

        mEventID = getStringFromJson(event, "id");
        mEventTitle = getStringFromJson(event, "title");

        //images can be null
        //if null, then it was a status post
        JSONArray images = getJsonArrayFromJson(event, "images");

        if (images != null) {
            try {
                mEventImageName = images.getString(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpUserInformation(JSONObject json) {
        JSONObject user = getJsonObjectFromJson(json, "user");
        if (user == null) return;

        mUserFullName = getStringFromJson(user, "fullName");
        mUserId = getStringFromJson(user, "id");
        mUserProfileImageName = getStringFromJson(user, "profileImage");
    }

    private void setUpActionDescription(){
        if (mUpdateType == UpdateType.LIKE) {
            if (mEventImageName == null || mEventImageName.equals(""))
                mDescription = "Liked your status";
            else mDescription = "Liked your photo";
        }else if (mUpdateType == UpdateType.COMMENT){
            if (mEventImageName == null || mEventImageName.equals(""))
                mDescription = "Commented on your status";
            else mDescription = "Commented on your photo";
        }else if (mUpdateType == UpdateType.FOLLOW){
            mDescription = "Started following you";
        }else if (mUpdateType == UpdateType.MENTION){
            mDescription = "Mentioned your post";
        }else if (mUpdateType == UpdateType.FACEBOOK_SHARE) {
            mDescription = "Shared your post on Facebook";
        }else if (mUpdateType == UpdateType.FRIEND_JOIN) {
            mDescription = "Has joined Tapt";
        }else mDescription = "";
    }

    public String getUserFullName() {
        return mUserFullName;
    }

    public UpdateType getUpdateType() {
        return mUpdateType;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getUserProfileImageName() {
        return mUserProfileImageName;
    }

    public String getEventImageName() {
        return mEventImageName;
    }

    /*
    public String getTimeString() {
        if (mActionTime == 0) return "";

        return DateUtils.getRelativeTimeSpanString(mActionTime,
                new Date().getTime(),  //time now
                DateUtils.SECOND_IN_MILLIS).toString();

    }*/

    public boolean isRead() {
        return mIsRead;
    }

    public String getEventID() {
        return mEventID;
    }

    public String getActionID() {
        return mActionID;
    }

    public String getEventTitle() {
        return mEventTitle;
    }

    public String getUserId() {
        return mUserId;
    }




    /* JSON Helpers */

    public static String getStringFromJson(JSONObject json, String key) {
        try {
            return json.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static JSONObject getJsonObjectFromJson(JSONObject json, String key) {
        try {
            return json.getJSONObject(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean getBooleanFromJson(JSONObject json, String key) {
        try {
            return json.getBoolean(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JSONArray getJsonArrayFromJson(JSONObject json, String key) {
        try {
            return json.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
