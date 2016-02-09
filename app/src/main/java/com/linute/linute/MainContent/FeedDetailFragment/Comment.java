package com.linute.linute.MainContent.FeedDetailFragment;

/**
 * Created by Arman on 1/13/16.
 */
public class Comment {
    private String mCommentUserId;
    private String mCommentUserProfileImage;
    private String mCommentUserName;
    private String mCommentUserPostText;
    private String mCommentUserPostId;
    private boolean mIsAnon;

    public Comment() {
        mCommentUserId = "";
        mCommentUserProfileImage = "";
        mCommentUserName = "";
        mCommentUserPostText = "";
        mCommentUserPostId = "";
    }

    public Comment(String commentUserId, String commentUserProfileImage, String commentUserName, String commentUserPostText, String commentUserPostId, boolean isAnon) {
        mCommentUserId = commentUserId;
        mCommentUserProfileImage = commentUserProfileImage;
        mCommentUserName = commentUserName;
        mCommentUserPostText = commentUserPostText;
        mCommentUserPostId = commentUserPostId;
        mIsAnon = isAnon;
    }

    public String getCommentUserId() {
        return mCommentUserId;
    }

    public String getCommentUserProfileImage() {
        return mCommentUserProfileImage;
    }

    public String getCommentUserName() {
        return mCommentUserName;
    }

    public String getCommentUserPostText() {
        return mCommentUserPostText;
    }

    public String getCommentUserPostId() {
        return mCommentUserPostId;
    }

    public boolean isAnon(){
        return mIsAnon;
    }
}
