package com.linute.linute.MainContent.FeedDetailFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetail {
    private static final String TAG = FeedDetail.class.getSimpleName();
    private String mPostId;
    private String mPostUserId;
    private String mPostImage;
    private String mPostText;
    private String mUserImage;
    private String mUserName;
    private int mPostPrivacy = 2;
    private String mPostTime;
    private boolean isPostLiked;
    private String mPostLikeNum = "0";
    private String mNumOfComments = "0";

    private String mAnonPic;

    private List<Comment> mComments = new ArrayList<>();

    public FeedDetail() {

    }

    public void setFeedDetail(String postImage, String postText, String userImage, String userName, int postPrivacy, String postTime, boolean postLiked, String postLikeNum, String numComments, String anonPic) {
        mPostImage = postImage;
        mPostText = postText;
        mUserImage = userImage;
        mUserName = userName;
        mPostPrivacy = postPrivacy;
        mPostTime = postTime;
        isPostLiked = postLiked;
        mPostLikeNum = postLikeNum;
        mNumOfComments = numComments;
        mAnonPic = anonPic;
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

    public void setIsPostLiked(boolean isPostLiked) {
        this.isPostLiked = isPostLiked;
    }

    public String getPostLikeNum() {
        return mPostLikeNum;
    }

    public void setPostLikeNum(String postLikeNum) {
        mPostLikeNum = postLikeNum;
    }

    public List<Comment> getComments() {
        return mComments;
    }

    public void setComments(List<Comment> comments){
        mComments.addAll(comments);
    }

    //public String getUserLiked() {
        //return mUserLiked;
    //}

    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String postId) {
        mPostId = postId;
    }

    public String getPostUserId() {
        return mPostUserId;
    }

    public String getNumOfComments(){
        return mNumOfComments;
    }

    public void setPostUserId(String postUserId) {
        mPostUserId = postUserId;
    }

    public void setPostPrivacy(int privacy){
        mPostPrivacy = privacy;
    }

    public String getAnonPic(){
        return mAnonPic;
    }
}
