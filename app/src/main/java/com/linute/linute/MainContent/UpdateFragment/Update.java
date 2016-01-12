package com.linute.linute.MainContent.UpdateFragment;


import android.util.Log;

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
        LIKE_STATUS,
        LIKE_PHOTO,
        COMMENT_STATUS,
        COMMENT_PHOTO,
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
    private boolean mIsPicturePost;

    private String mActionID;

    //'User' refers to the person performing the action
    // i.e. if 'max liked your picture' -> user = max
    private String mUserId;
    private String mUserFullName;
    private String mUserProfileImageName;

    private String mEventImageName;
    private String mEventID;
    private String mEventTitle;

    //will not be empty if you are following person
    private String mFriendshipID;
    private boolean mFollowedBack;


    public Update(JSONObject json) {
        mUpdateType = getUpdateTypeFromString(getStringFromJson(json, "action"));

        mIsRead = getBooleanFromJson(json, "isRead");

        //uncomment if we decide to use it
        //mActionTime = Utils.getTimeFromString(getStringFromJson(json, "date"));

        mActionID = getStringFromJson(json, "id");

        mIsPicturePost = getIntFromJson(json, "type") == 0 ? false : true;

        setUpUserInformation(json);

        //only set up if we have friend info
        if (hasFriendShipInformation())
            setFriendshipIdAndFollowedBack(json);

        //only set up events if we have to
        if (hasEventInformation())
            setUpEvent(json);

        setUpActionDescription();
    }

    private static UpdateType getUpdateTypeFromString(String action) {
        switch (action) {
            case "liked status":
                return UpdateType.LIKE_STATUS;
            case "liked photo":
                return UpdateType.LIKE_PHOTO;
            case "commented status":
                return UpdateType.COMMENT_STATUS;
            case "commented photo":
                return UpdateType.COMMENT_PHOTO;
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

    private void setFriendshipIdAndFollowedBack(JSONObject json) {

        try {
            JSONObject friend = json.getJSONObject("friend");
            mFollowedBack = friend.getBoolean("followedBack");
            mFriendshipID = json.getString("id");
        } catch (JSONException e) {
            mFollowedBack = true; //so buttons will be hidden and no crash will occur
            mFriendshipID = "";
        }
    }

    public final boolean hasEventInformation(){
        //TODO: WILL IMPLEMENT MENTIONED AND FACEBOOK SHARED IN THE FURTURE
        return mUpdateType == UpdateType.LIKE_PHOTO || mUpdateType == UpdateType.LIKE_STATUS ||
                mUpdateType == UpdateType.COMMENT_PHOTO || mUpdateType == UpdateType.COMMENT_STATUS;
    }

    public final boolean hasFriendShipInformation(){
        return mUpdateType == UpdateType.FOLLOW || mUpdateType == UpdateType.FRIEND_JOIN;
    }


    private void setUpEvent(JSONObject json) {
        JSONObject event = getJsonObjectFromJson(json, "event");

        if (event == null) return;

        mEventID = getStringFromJson(event, "id");
        mEventTitle = getStringFromJson(event, "title");

        //images can be null
        //if null, then it was a status post
        JSONArray images = getJsonArrayFromJson(event, "images");

        if (images != null && images.length() > 0) {
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

    private void setUpActionDescription() {
        switch (mUpdateType){
            case LIKE_STATUS:
                mDescription = "Liked your photo";
                break;
            case LIKE_PHOTO:
                mDescription = "Liked your photo";
                break;
            case COMMENT_PHOTO:
                mDescription = "Commented on your photo";
                break;
            case COMMENT_STATUS:
                mDescription = "Commented on your status";
                break;
            case FOLLOW:
                mDescription = "Started Following you";
                break;
            case FRIEND_JOIN:
                mDescription = "Has joined Tapt";
                break;
            case MENTION:
                mDescription = "Mentioned your post";
                break;
            case FACEBOOK_SHARE:
                mDescription = "Shared your post on Facebook";
                break;
            default:
                mDescription = "";
                break;
        }
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

    public boolean isPicturePost(){
        return mIsPicturePost;
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

    public String getFriendshipID() {
        return mFriendshipID;
    }

    public boolean getFollowedBack(){
        return mFollowedBack;
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
            return null;
        }
    }

    public static int getIntFromJson(JSONObject json, String key){
        try {
            return json.getInt(key);
        }catch (JSONException e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int hashCode() {
        return mActionID.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o == null || o.getClass() != getClass()) {
            result = false;
        }

        else {
            if (mActionID == ((Update)o).mActionID) {
                result = true;
            }
        }
        return result;
    }
}
