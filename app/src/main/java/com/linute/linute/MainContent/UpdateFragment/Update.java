package com.linute.linute.MainContent.UpdateFragment;


import android.util.Log;

import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 1/6/16.
 */


public class Update {

    private static final String TAG = Update.class.getSimpleName();

    //enum of update types
    public enum UpdateType {
        UNDEFINED,
        LIKED_STATUS,
        LIKED_PHOTO,
        LIKED_VIDEO,
        COMMENTED_STATUS,
        COMMENTED_PHOTO,
        COMMENTED_VIDEO,
        FOLLOWER,
        MENTIONED, //need icon
        FRIEND_JOINED, //need icon
        FRIEND_POSTED_STATUS,
        FRIEND_POSTED_PHOTO,
        FRIEND_POSTED_VIDEO,
        AlSO_COMMENTED_STATUS,
        ALSO_COMMENTED_IMAGE,
        ALSO_COMMENTED_VIDEO,
        LIKED_COMMENT
    }

    private String mActionID;           // id of action
    private String mDescription;        // description of action. ie - "max liked your status;
    private UpdateType mUpdateType;     // type of update

    private boolean mIsRead;            //determine if viewer has seen this update before
    private boolean mIsViewed;          //determine if the viewer has opened this update before

    //'User' refers to the person performing the action
    // i.e. if 'max liked your picture' -> user = max
    private String mUserId;                 // if of user
    private String mUserFullName;           // full name
    private String mUserProfileImageName;   // profile image of user
    private String mAnonImage;              // anon image of user
    private boolean mIsAnon;                // determine if user is anon


    //will not be empty if you are following person
    //used for FOLLOWER action
    //will be null if not a FOLLOWER action
    private String mFriendshipID;
    private boolean mFollowedBack;


    // Post object will contain event info.
    // ie. "Max liked your image." Post will contain the image url and other information
    private Post mPost;

    //time stamp
    private long mActionTime; //i.e. when someone liked/commented on/etc post


    /**
     *
     * @param json   - update json object retrieved from backend
     * @throws JSONException
     */
    public Update(JSONObject json) throws JSONException {

        mUpdateType = getUpdateTypeFromString(getStringFromJson(json,"action"));

        mIsRead = getBooleanFromJson(json, "isRead");

        //uncomment if we decide to use it
        mActionTime = Utils.getTimeFromString(getStringFromJson(json, "date"));

        mActionID = getStringFromJson(json,"id");

        mIsAnon = getIntFromJson(json, "privacy") == 1;

        mAnonImage = getStringFromJson(json,"anonymousImage");

        setUpUserInformation(json);

        //only set up if we have friend info
        if (hasFriendShipInformation())
            setFriendshipIdAndFollowedBack(json);

        //only set up events if we have to
        if (hasEventInformation())
            setUpEvent(json);

        String text = getStringFromJson(json, "text");
        Log.i(TAG, "Update: "+text);
        if(!mUserFullName.isEmpty()){
            mDescription = getStringFromJson(json, "text").replace(mUserFullName+" ","");
        }

        mDescription = getStringFromJson(json, "text").replaceFirst(mUserFullName+" ","");

        mIsViewed = getBooleanFromJson(json, "isViewed");
    }

    //parse action String and return UpdateType
    private static UpdateType getUpdateTypeFromString(String action) {
        switch (action) {
            case "liked status":
                return UpdateType.LIKED_STATUS;
            case "liked photo":
                return UpdateType.LIKED_PHOTO;
            case "liked video":
                return UpdateType.LIKED_VIDEO;
            case "commented status":
                return UpdateType.COMMENTED_STATUS;
            case "commented photo":
                return UpdateType.COMMENTED_PHOTO;
            case "commented video":
                return UpdateType.COMMENTED_VIDEO;
            case "follower":
                return UpdateType.FOLLOWER;
            case "mentioned":
                return UpdateType.MENTIONED;
            case "friend joined":
                return UpdateType.FRIEND_JOINED;
            case "friend posted status":
                return UpdateType.FRIEND_POSTED_STATUS;
            case "friend posted photo":
                return UpdateType.FRIEND_POSTED_PHOTO;
            case "friend posted video":
                return UpdateType.FRIEND_POSTED_VIDEO;
            case "also commented status":
                return UpdateType.AlSO_COMMENTED_STATUS;
            case "also commented photo":
                return UpdateType.ALSO_COMMENTED_IMAGE;
            case "also commented video":
                return UpdateType.ALSO_COMMENTED_VIDEO;
            case "liked comment":
                return UpdateType.LIKED_COMMENT;
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

    //following activities will have event info
    public final boolean hasEventInformation(){
        return mUpdateType != UpdateType.UNDEFINED &&
                mUpdateType != UpdateType.FOLLOWER &&
                mUpdateType != UpdateType.FRIEND_JOINED;
    }

    public final boolean hasFriendShipInformation(){
        return mUpdateType == UpdateType.FOLLOWER || mUpdateType == UpdateType.FRIEND_JOINED;
    }


    private void setUpEvent(JSONObject json) throws JSONException{
        JSONObject event = json.getJSONObject("event");
        mPost = new Post(event);
    }

    private void setUpUserInformation(JSONObject json) throws JSONException {
        JSONObject user = getJsonObjectFromJson(json, "user");

        //no user info, try getting owner info
        if (user == null) user = json.getJSONObject("owner");

        mUserFullName = getStringFromJson(user, "fullName");
        mUserId = user.getString("id");
        mUserProfileImageName = getStringFromJson(user, "profileImage");
    }

    public String getAnonImage(){
        return mAnonImage;
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
        return mPost.getImage();
    }

    public boolean isPicturePost(){
        return mPost.isImagePost();
    }


    public boolean isRead() {
        return mIsRead;
    }

    public boolean isViewed(){
        return mIsViewed;
    }

    public void markViewed(){mIsViewed = true;}

    public String getEventID() {
        return mPost.getPostId();
    }

    public String getActionID() {
        return mActionID;
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

    public boolean isAnon() {
        return mIsAnon;
    }

    public String getEventUserId() {
        return mPost.getUserId();
    }

    public long getActionTime() {
        return mActionTime;
    }

    public Post getPost(){
        return mPost;
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
}
