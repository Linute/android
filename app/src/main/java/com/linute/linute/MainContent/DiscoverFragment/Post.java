package com.linute.linute.MainContent.DiscoverFragment;

/**
 * Created by Arman on 12/27/15.
 */
public class Post {
    private String mUserName;
    private String mUserImage;
    private String mTitle;
    private String mImage;
    private int mPrivacy;
    private int mNumLikes;
    private String mUserLiked;
    private String mPostTime;
    private String mPostId;

    private boolean mPostLiked;

    public Post(String userName, String userImage, String title, String image, int privacy, int numLike, String userLiked, String postTime, String postId) {
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

        mPostLiked = !mUserLiked.equals("");
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

    public String getUserLiked() {
        return mUserLiked;
    }
}
