package com.linute.linute.MainContent.ProfileFragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by QiFeng on 12/4/15.
 */
public class UserActivityItem{

    private String mProfileImagePath;   //exact url to image
    private String mUserName;           //first name and last
    private String mDescription;        //user hosted or attended event
    private String mEventImagePath;     //exact url to image of event
    private boolean isImagePost;
    private boolean mHasVideo;
    private String mVideoPath;
    private long mPostDate;
    private String mEventID;
    private String mOwnerID;
    private boolean mIsAnon;

    private Post mPost;

    public UserActivityItem(){

    }

    public UserActivityItem(JSONObject activityInfo, String profileImagePath, String userName) {
        mProfileImagePath = profileImagePath;
        mUserName = userName;

//        mDescription = getStringValue(activityInfo, "action").equals("host") ? "hosted an event" : "attended an event";

        mIsAnon = (getIntFromJson(activityInfo, "privacy") == 1);
//        mDescription = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


        try {
            mPostDate = dateFormat.parse(getStringValue(activityInfo, "date")).getTime();
        } catch (ParseException e) {
            mPostDate = 0;
            e.printStackTrace();
        }

        JSONObject event = getObject(activityInfo, "event");

        if (event != null) {

            mDescription = getStringValue(event, "title");
            mEventID = getStringValue(event, "id");


            JSONObject owner = getObject(activityInfo, "owner");
//
            if (owner!=null){
                mOwnerID = getStringValue(owner, "id");
            }

            //try to get event image
            try {
                JSONArray eventImages = event.getJSONArray("images");
                if (eventImages != null && eventImages.length() > 0) {
                    mEventImagePath = Utils.getEventImageURL(eventImages.getString(0)); //get the first image
                    isImagePost = true;
                } else {
                    isImagePost = false;
                }
                JSONArray videos = event.getJSONArray("videos");
                if (videos != null && videos.length() > 0){
                    mVideoPath = videos.getString(0);
                    mHasVideo = true;
                }else {
                    mVideoPath = "";
                    mHasVideo = false;
                }
            } catch (JSONException e) { //counld't get image
                e.printStackTrace();
                mEventImagePath = null;
            }

        }

    }


    private JSONObject getObject(JSONObject obj, String key) {
        try {
            return obj.getJSONObject(key);
        } catch (JSONException e) {
            return null;
        }
    }


    private String getStringValue(JSONObject obj, String key) {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    public int getIntFromJson(JSONObject json, String key){
        try {
            return json.getInt(key);
        }catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public JSONArray getJSONArray(JSONObject obj, String key){
        try {
            return obj.getJSONArray(key);
        } catch (JSONException e) {
            return null;
        }
    }

    public String getProfileImagePath() {
        return mProfileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        mProfileImagePath = profileImagePath;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getEventImagePath() {
        return mEventImagePath;
    }

    public void setEventImagePath(String eventImagePath) {
        mEventImagePath = eventImagePath;
    }

    public String getEventID(){
        return mEventID;
    }

    public String getOwnerID(){
        return mOwnerID;
    }

    public boolean hasVideo(){
        return mHasVideo;
    }


    public boolean isImagePost() {
        return isImagePost;
    }

    public long getPostDate() {
        return mPostDate;
    }

    public boolean isAnon() {
        return mIsAnon;
    }
}
