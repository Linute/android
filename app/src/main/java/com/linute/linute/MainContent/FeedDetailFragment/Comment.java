package com.linute.linute.MainContent.FeedDetailFragment;

/**
 * Created by Arman on 1/13/16.
 */
public class Comment {
    private String mCommentUserProfileImage;
    private String mCommentUserName;
    private String mCommentUserPostText;

    public Comment() {
        mCommentUserProfileImage = "";
        mCommentUserName = "";
        mCommentUserPostText = "";
    }

    public Comment(String commentUserProfileImage, String commentUserName, String commentUserPostText) {
        mCommentUserProfileImage = commentUserProfileImage;
        mCommentUserName = commentUserName;
        mCommentUserPostText = commentUserPostText;
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
}
