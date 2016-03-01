package com.linute.linute.MainContent.UpdateFragment;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/6/16.
 */



/* Our Features *
    -mentioned
    -commented status
    -commented photo
    -liked status
    -liked photo
    -follower
    -friend joined


    TODO: implement later
    -posted status
    -posted photo
 */
public class Update {

    private static final String TAG = Update.class.getSimpleName();

    public enum UpdateType {
        UNDEFINED,
        LIKED_STATUS,
        LIKED_PHOTO,
        COMMENTED_STATUS,
        COMMENTED_PHOTO,
        FOLLOWER,
        MENTIONED, //need icon
        FRIEND_JOINED, //need icon
        POSTED_STATUS,
        POSTED_PHOTO
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

    private boolean mIsAnon;
    private String mAnonImage;

    public Update(){

    }


    public Update(JSONObject json) {
        mUpdateType = getUpdateTypeFromString(getStringFromJson(json, "action"));

        mIsRead = getBooleanFromJson(json, "isRead");

        //uncomment if we decide to use it
        //mActionTime = Utils.getTimeFromString(getStringFromJson(json, "date"));

        mActionID = getStringFromJson(json, "id");

        mIsAnon = getIntFromJson(json, "privacy") == 1;

        mAnonImage = getStringFromJson(json,"anonymousImage");

        setUpUserInformation(json);

        //only set up if we have friend info
        if (hasFriendShipInformation())
            setFriendshipIdAndFollowedBack(json);

        //only set up events if we have to
        if (hasEventInformation())
            setUpEvent(json);

        mDescription = getActionDescription(mUpdateType);
    }

    private static UpdateType getUpdateTypeFromString(String action) {
        switch (action) {
            case "liked status":
                return UpdateType.LIKED_STATUS;
            case "liked photo":
                return UpdateType.LIKED_PHOTO;
            case "commented status":
                return UpdateType.COMMENTED_STATUS;
            case "commented photo":
                return UpdateType.COMMENTED_PHOTO;
            case "follower":
                return UpdateType.FOLLOWER;
            case "mentioned":
                return UpdateType.MENTIONED;
            case "friend joined":
                return UpdateType.FRIEND_JOINED;
            case "posted status":
                return UpdateType.POSTED_STATUS;
            case "posted photo":
                return UpdateType.POSTED_PHOTO;
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
        return mUpdateType == UpdateType.LIKED_PHOTO || mUpdateType == UpdateType.LIKED_STATUS ||
                mUpdateType == UpdateType.COMMENTED_PHOTO || mUpdateType == UpdateType.COMMENTED_STATUS
                || mUpdateType == UpdateType.POSTED_PHOTO || mUpdateType == UpdateType.POSTED_STATUS || mUpdateType == UpdateType.MENTIONED;
    }

    public final boolean hasFriendShipInformation(){
        return mUpdateType == UpdateType.FOLLOWER || mUpdateType == UpdateType.FRIEND_JOINED;
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
                mIsPicturePost = true;
            } catch (JSONException e) {
                e.printStackTrace();
                mEventImageName = null;
                mIsPicturePost = false;
            }
        }
    }

    private void setUpUserInformation(JSONObject json) {
        JSONObject user = getJsonObjectFromJson(json, "user");

        //no user info, try getting owner info
        if (user == null) user = getJsonObjectFromJson(json, "owner");

        //still null, just return
        if (user == null) return;

        mUserFullName = getStringFromJson(user, "fullName");
        mUserId = getStringFromJson(user, "id");
        mUserProfileImageName = getStringFromJson(user, "profileImage");
    }

    public String getAnonImage(){
        return mAnonImage;
    }

    private static String getActionDescription(UpdateType type) {
        switch (type){
            case LIKED_STATUS:
                return "Liked your status";
            case LIKED_PHOTO:
                return "Liked your photo";
            case COMMENTED_PHOTO:
                return  "Commented on your photo";
            case COMMENTED_STATUS:
                return "Commented on your status";
            case FOLLOWER:
                return "Started Following you";
            case FRIEND_JOINED:
                return  "Has joined Tapt";
            case MENTIONED:
                return "Mentioned you in a post";
            case POSTED_PHOTO:
                return "Posted a photo";
            case POSTED_STATUS:
                return "Posted a status";
            default:
                return  "";
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

    public void setFollowedBack(boolean follow){
        mFollowedBack = follow;
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

    public boolean isAnon() {
        return mIsAnon;
    }

}
