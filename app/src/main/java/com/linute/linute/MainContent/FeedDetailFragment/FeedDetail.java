package com.linute.linute.MainContent.FeedDetailFragment;

import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetail {
    private static final String TAG = FeedDetail.class.getSimpleName();
    private Post mPost;

    private List<Comment> mComments = new ArrayList<>();

    public FeedDetail(Post post) {
        mPost = post;
    }


    public Post getPost(){
        return mPost;
    }

    public String getPostImage() {
        return mPost.getImage();
    }

    public String getPostText() {
        return mPost.getTitle();
    }

    public String getUserImage() {
        return mPost.getUserImage();
    }

    public String getUserName() {
        return mPost.getUserName();
    }

    public int getPostPrivacy() {
        return mPost.getPrivacy();
    }

    public String getPostTime() {
        return Utils.getTimeAgoString(mPost.getPostLongTime());
    }

    public boolean isPostLiked() {
        return mPost.isPostLiked();
    }

    public void setIsPostLiked(boolean isPostLiked) {
        mPost.setPostLiked(isPostLiked);
    }

    public String getPostLikeNum() {
        return mPost.getNumLike();
    }

    public void setPostLikeNum(String postLikeNum) {
        mPost.setNumLike(Integer.parseInt(postLikeNum));
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
        return mPost.getPostId();
    }

    public String getPostUserId() {
        return mPost.getUserId();
    }

    public String getNumOfComments(){
        return mPost.getNumOfComments()+"";
    }

    public void setPostPrivacy(int privacy){
        mPost.setPostPrivacy(privacy);
    }

    public void refreshCommentCount(){
        mPost.setNumOfComments(mComments.size());
    }

    public void setNumComments(int comments){
        mPost.setNumOfComments(comments);
    }

    public void setAnonImage(String image){
        mPost.setAnonImage(image);
    }

    public boolean isAnon (){
        return mPost.getPrivacy() == 1;
    }

    public String getAnonPic(){
        return mPost.getAnonImage();
    }

    public String getVideoUrl(){
        return mPost.getVideoUrl();
    }
}
