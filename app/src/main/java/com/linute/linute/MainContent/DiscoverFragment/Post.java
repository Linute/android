package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Arman on 12/27/15.
 */
public class Post implements Parcelable {

    public static int POST_TYPE_STATUS = 0;
    public static int POST_TYPE_IMAGE = 1;
    public static int POST_TYPE_VIDEO = 2;

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String mUserId;
    private String mUserName;
    private String mUserImage;
    private String mTitle;
    private String mImage = "";
    private int mPrivacy;
    private int mNumLikes;
    private boolean mUserLiked;
    private long mPostTime;
    private String mPostId;
    private int mNumOfComments;
    private String mAnonImage;

    private boolean mPostLiked;

    private String mVideoURL = "";

    public Post(){

    }

//    public Post(String userId,
//                String userName,
//                String userImage,
//                String title,
//                String image,
//                int privacy,
//                int numLike,
//                boolean userLiked,
//                long postTime,
//                String postId,
//                int numComments,
//                String anonImage,
//                String video
//    ) {
//
//        mUserId = userId;
//        mUserName = userName;
//        mImage = "";
//        mUserImage = userImage;
//        mTitle = title;
//        mImage = image;
//        mPrivacy = privacy;
//        mNumLikes = numLike;
//        mUserLiked = userLiked;
//        mPostTime = postTime;
//        mPostId = postId;
//        mNumOfComments = numComments;
//        mAnonImage = anonImage;
//
//        mPostLiked = mUserLiked;
//        mVideoURL = video;
//
//    }

    public Post(JSONObject jsonObject)throws JSONException{

        int type = jsonObject.getInt("type");

        if (jsonObject.getJSONArray("images").length() > 0)
            mImage = (String) jsonObject.getJSONArray("images").get(0);

        if (type == POST_TYPE_VIDEO && jsonObject.getJSONArray("videos").length() > 0)
            mVideoURL = (String) jsonObject.getJSONArray("videos").get(0);

        Date myDate;

        try {
            myDate = simpleDateFormat.parse(jsonObject.getString("date"));
        }catch (ParseException w){
            w.printStackTrace();
            myDate = null;
        }

        mPostTime = (myDate == null ? 0 : myDate.getTime());

        JSONObject owner = jsonObject.getJSONObject("owner");

        mUserId = owner.getString("id");
        mUserName = owner.getString("fullName");
        mUserImage = owner.getString("profileImage");
        mTitle = jsonObject.getString("title");
        mPrivacy = jsonObject.getInt("privacy");

        mPostId = jsonObject.getString("id");

        mAnonImage = jsonObject.getString("anonymousImage");

        try {
            mNumLikes = jsonObject.getInt("numberOfLikes");
            mNumOfComments = jsonObject.getInt("numberOfComments");
            mPostLiked = jsonObject.getBoolean("isLiked");
        }catch (JSONException e){
            mNumLikes = 0;
            mNumOfComments = 0;
            mPostLiked = false;
        }
    }

    public String getNumLike() {
        return mNumLikes + "";
    }

    public void setNumLike(int numLike) {
        mNumLikes = numLike;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserImage() {
        return mUserImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getImage() {
        return mImage;
    }

    public int getPrivacy() {
        return mPrivacy;
    }

    public boolean isPostLiked() {
        return mPostLiked;
    }

    public void setPostLiked(boolean postLiked) {
        mPostLiked = postLiked;
    }

    public String getPostTime() {
        return Utils.getTimeAgoString(mPostTime);
    }

    public String getPostId() {
        return mPostId;
    }

    public boolean getUserLiked() {
        return mUserLiked;
    }

    public String getUserId() {
        return mUserId;
    }

    public boolean isImagePost(){
        return mImage != null && !mImage.equals("");
    }

    public boolean isVideoPost() {
        return mVideoURL != null && !mVideoURL.equals("");
    }


    public String getVideoUrl() {
        return mVideoURL;
    }

    public int getNumOfComments() {
        return mNumOfComments;
    }

    public String getAnonImage(){
        return mAnonImage;
    }

    @Override
    public String toString() {
        return getImage().equals("") ? getTitle() : "Content: Image - " + getTitle();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mUserName);
        dest.writeString(mUserImage);
        dest.writeString(mTitle);
        dest.writeString(mImage);
        dest.writeInt(mPrivacy);
        dest.writeInt(mNumLikes);
        dest.writeByte((byte) (mUserLiked ? 1 : 0)); //boolean
        dest.writeLong(mPostTime);
        dest.writeString(mPostId);
        dest.writeInt(mNumOfComments);
        dest.writeString(mAnonImage);
        dest.writeByte((byte) (mPostLiked ? 1 : 0)); //boolean
        dest.writeString(mVideoURL);
    }

    private Post(Parcel in){
        mUserId = in.readString();
        mUserName = in.readString();
        mUserImage = in.readString();
        mTitle = in.readString();
        mImage = in.readString();
        mPrivacy = in.readInt();
        mNumLikes = in.readInt();
        mUserLiked = in.readByte() != 0; //true if byte != 0
        mPostTime = in.readLong();
        mPostId = in.readString();
        mNumOfComments = in.readInt();
        mAnonImage = in.readString();
        mPostLiked = in.readByte() != 0; //true if byte != 0
        mVideoURL = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
}
