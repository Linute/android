package com.linute.linute.MainContent.FeedDetailFragment;

import com.linute.linute.MainContent.DiscoverFragment.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */

public class FeedDetail {
    private Post mPost;

    //Can contain a load more header
    private List<Object> mComments = new ArrayList<>();

    public FeedDetail(Post post) {
        mPost = post;
    }

    public Post getPost(){
        return mPost;
    }

    public String getUserName() {
        return mPost.getUserName();
    }

    public int getPostPrivacy() {
        return mPost.getPrivacy();
    }

    public List<Object> getComments() {
        return mComments;
    }

    public void setComments(List<Comment> comments){
        mComments.addAll(comments);
    }

    public String getPostId() {
        return mPost.getPostId();
    }

    public String getPostUserId() {
        return mPost.getUserId();
    }

    public void setPostPrivacy(int privacy){
        mPost.setPostPrivacy(privacy);
    }

    public void addComment(){
        mPost.setNumOfComments(mPost.getNumOfComments()+1);
    }

    public void removeComment(){
        mPost.setNumOfComments(mPost.getNumOfComments()-1);
    }

    public void setAnonImage(String image){
        mPost.setAnonImage(image);
    }

    public boolean isAnon (){
        return mPost.getPrivacy() == 1;
    }
}
