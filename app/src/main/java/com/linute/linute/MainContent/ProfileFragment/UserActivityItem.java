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

//    private String mProfileImagePath;   //exact url to image
//    private String userName;           //first name and last
//    private String mDescription;        //user hosted or attended event
//    private String mEventImagePath;     //exact url to image of event
//    private boolean isImagePost;
//    private boolean mHasVideo;
//    private String mVideoPath;
//    private long mPostDate;
//    private String mEventID;
//    private String mOwnerID;
//    private boolean mIsAnon;

    private Post mPost;

    public UserActivityItem(){

    }

    public UserActivityItem(JSONObject activityInfo) throws JSONException {
        mPost = new Post(activityInfo.getJSONObject("event"));
    }


    public String getProfileImagePath() {
        return mPost.getUserImage();
    }

//    public void setProfileImagePath(String profileImagePath) {
//         = profileImagePath;
//    }

    public String getUserName() {
        return mPost.getUserName();
    }

    public void setUserName(String userName) {
        mPost.setUserName(userName);
    }

    public String getPostText() {
        return mPost.getTitle();
    }

    public String getEventImagePath() {
        return mPost.getImage();
    }

    public String getEventID(){
        return mPost.getPostId();
    }

    public String getOwnerID(){
        return mPost.getUserId();
    }

    public boolean hasVideo(){
        return mPost.isVideoPost();
    }

    public boolean isImagePost() {
        return mPost.isImagePost();
    }

    //public long getPostDate() {
        //return mPost.get;
    //}

    public boolean isAnon() {
        return mPost.getPrivacy() == 1;
    }

    public Post getPost(){
        return mPost;
    }
}
