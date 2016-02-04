package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arman on 12/27/15.
 */
public class Post implements Parcelable {
    private String mUserId;
    private String mUserName;
    private String mUserImage;
    private String mTitle;
    private String mImage;
    private int mPrivacy;
    private int mNumLikes;
    private boolean mUserLiked;
    private String mPostTime;
    private String mPostId;
    private int mNumOfComments;

    private boolean mPostLiked;

    public Post(String userId, String userName, String userImage, String title,
                String image, int privacy, int numLike, boolean userLiked,
                String postTime, String postId, int numComments) {
        mUserId = userId;
        mUserName = userName;
        mImage = "";
        mUserImage = userImage;
        mTitle = title;
        mImage = image;
        mPrivacy = privacy;
        mNumLikes = numLike;
        mUserLiked = userLiked;
        mPostTime = postTime;
        mPostId = postId;
        mNumOfComments = numComments;

        mPostLiked = mUserLiked;
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
        return mPostTime;
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
        return !mImage.equals("");
    }

    public int getNumOfComments() {
        return mNumOfComments;
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
        dest.writeString(mPostTime);
        dest.writeString(mPostId);
        dest.writeByte((byte) (mPostLiked ? 1 : 0)); //boolean
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
        mPostTime = in.readString();
        mPostId = in.readString();
        mPostLiked = in.readByte() != 0; //true if byte != 0
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
