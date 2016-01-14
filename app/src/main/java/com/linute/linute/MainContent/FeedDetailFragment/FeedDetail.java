package com.linute.linute.MainContent.FeedDetailFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetail {
    private String mPostImage;
    private String mPostText;
    private String mUserImage;
    private String mUserName;
    private int mPostPrivacy;
    private String mPostTime;
    private boolean isPostLiked;
    private String mPostLikeNum;

    private List<Comment> mComments;

    public FeedDetail(String postImage, String postText, String userImage, String userName, int postPrivacy, String postTime, boolean postLiked, String postLikeNum) {
        mPostImage = postImage;
        mPostText = postText;
        mUserImage = userImage;
        mUserName = userName;
        mPostPrivacy = postPrivacy;
        mPostTime = postTime;
        isPostLiked = postLiked;
        mPostLikeNum = postLikeNum;
        mComments = new ArrayList<>();
    }

    public String getPostImage() {
        return mPostImage;
    }

    public String getPostText() {
        return mPostText;
    }

    public String getUserImage() {
        return mUserImage;
    }

    public String getUserName() {
        return mUserName;
    }

    public int getPostPrivacy() {
        return mPostPrivacy;
    }

    public String getPostTime() {
        return mPostTime;
    }

    public boolean isPostLiked() {
        return isPostLiked;
    }

    public String getPostLikeNum() {
        return mPostLikeNum;
    }

    public List<Comment> getComments() {
        return mComments;
    }
}
