package com.linute.linute.MainContent.FeedDetailFragment;

import android.os.Parcel;
import android.os.Parcelable;

import com.linute.linute.MainContent.DiscoverFragment.Post;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */

public class FeedDetail implements Parcelable{
    private Post mPost;

    //Can contain a load more header
    private LinkedList<Object> mComments;

    public FeedDetail(Post post) {
        mPost = post;
        mComments = mPost.getComments();
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

    public LinkedList<Object> getComments() {
        return mComments;
    }

    public void setComments(List<Comment> comments){
        mComments.addAll(comments);
    }

    public String getPostId() {
        return mPost.getId();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPost, flags);
        dest.writeList(mComments);
    }

    public static final Parcelable.Creator<FeedDetail> CREATOR = new Parcelable.Creator<FeedDetail>(){
        @Override
        public FeedDetail createFromParcel(Parcel source) {
            FeedDetail feedDetail = new FeedDetail(
                    (Post) source.readParcelable(Post.class.getClassLoader())
            );
            ArrayList<Comment> outVal = new ArrayList<>();
            source.readList(outVal, Comment.class.getClassLoader());
            feedDetail.setComments(outVal);
            return feedDetail;
        }

        @Override
        public FeedDetail[] newArray(int size) {
            return new FeedDetail[size];
        }
    };
}
