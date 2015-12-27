package com.linute.linute.MainContent.DiscoverFragment;

/**
 * Created by Arman on 12/27/15.
 */
public class Post {
    private String mUserImage;
    private String mTitle;
    private String mImage;
    private int mPrivacy;
    private int mNumLikes;

    public Post(String userImage, String title, String image, int privacy, int numLike) {
        mImage = "";
        mUserImage = userImage;
        mTitle = title;
        mImage = image;
        mPrivacy = privacy;
        mNumLikes = numLike;
    }

    public String getNumLike() {
        return mNumLikes + "";
    }

    public void setNumLike(int numLike) {
        mNumLikes = numLike;
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
}
