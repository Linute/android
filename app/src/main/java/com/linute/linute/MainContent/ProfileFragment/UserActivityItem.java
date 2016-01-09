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
    private long mStartTime;            //when event starts
    private long mEndTime;              //when event ends
    private String mEventImagePath;     //exact url to image of event
    private boolean isImagePost;
    private String mPostDate;

    public UserActivityItem(JSONObject activityInfo, String profileImagePath, String userName) {
        mProfileImagePath = profileImagePath;
        mUserName = userName;

//        mDescription = getStringValue(activityInfo, "action").equals("host") ? "hosted an event" : "attended an event";

        mDescription = "";
        JSONObject event = getObject(activityInfo, "event");

        if (event != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            //try to get time
            try {
                mStartTime = dateFormat.parse(getStringValue(event, "timeStart")).getTime();
                mEndTime = 0; //dateFormat.parse(getStringValue(event, "timeEnd")).getTime();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date myDate;
                myDate = simpleDateFormat.parse(getStringValue(event, "timeStart"));
                mPostDate = Utils.getEventTime(myDate);

                mDescription = getStringValue(event, "title");

            } catch (ParseException e) {
                e.printStackTrace();
                mStartTime = 0;
                mEndTime = 0;
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
            mStartTime = 0;
            mEndTime = 0;
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


    //returns a nicely formated string about when event occurred
    public String getTimeString() {
        if (mStartTime == 0 || mEndTime == 0) return "";

        long nowTime = new Date().getTime();

        if (nowTime >= mStartTime && nowTime <= mEndTime) { //going on
            return "going on right now";
        } else { //in the future or past
            return DateUtils.getRelativeTimeSpanString(mEndTime, nowTime, DateUtils.SECOND_IN_MILLIS).toString();
        }
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
        dest.writeLong(mStartTime);
        dest.writeLong(mEndTime);
        dest.writeString(mEventImagePath);
    }

    private UserActivityItem(Parcel in) {
        mProfileImagePath = in.readString();
        mUserName = in.readString();
        mDescription = in.readString();
        mStartTime = in.readLong();
        mEndTime = in.readLong();
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

    public String getPostDate() {
        return mPostDate;
    }
}
