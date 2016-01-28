package com.linute.linute.MainContent.ProfileFragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

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
public class UserActivityItem implements Parcelable {

    private String mProfileImagePath;   //exact url to image
    private String mUserName;           //first name and last
    private String mDescription;        //user hosted or attended event
    private String mEventImagePath;     //exact url to image of event
    private boolean isImagePost;
    private long mPostDate;
    private String mEventID;
    private String mOwnerID;

    public UserActivityItem(JSONObject activityInfo, String profileImagePath, String userName) {
        mProfileImagePath = profileImagePath;
        mUserName = userName;

//        mDescription = getStringValue(activityInfo, "action").equals("host") ? "hosted an event" : "attended an event";

        mDescription = "";

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

            if (owner!=null){
                mOwnerID = getStringValue(owner, "id");
            }

            //try to get event image
            try {
                JSONArray eventImages = event.getJSONArray("images");
                if (eventImages.length() > 0) {
                    mEventImagePath = Utils.getEventImageURL(eventImages.getString(0)); //get the first image
                    isImagePost = true;
                } else {
                    Log.i("UserActivityItem", "eventImages was empty");
                    isImagePost = false;
                }
            } catch (JSONException e) { //counld't get image
                e.printStackTrace();
                mEventImagePath = null;
            }

        } else {
            mEventImagePath = "";
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mProfileImagePath);
        dest.writeString(mUserName);
        dest.writeString(mDescription);
        dest.writeLong(mPostDate);
        dest.writeString(mEventImagePath);
    }

    private UserActivityItem(Parcel in) {
        mProfileImagePath = in.readString();
        mUserName = in.readString();
        mDescription = in.readString();
        mPostDate = in.readLong();
        mEventImagePath = in.readString();
    }

    public static final Creator<UserActivityItem> CREATOR = new Creator<UserActivityItem>() {
        @Override
        public UserActivityItem createFromParcel(Parcel source) {
            return new UserActivityItem(source);
        }

        @Override
        public UserActivityItem[] newArray(int size) {
            return new UserActivityItem[size];
        }
    };

    public boolean isImagePost() {
        return isImagePost;
    }

    public long getPostDate() {
        return mPostDate;
    }
}
