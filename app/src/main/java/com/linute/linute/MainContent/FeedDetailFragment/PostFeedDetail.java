package com.linute.linute.MainContent.FeedDetailFragment;

import com.linute.linute.MainContent.DiscoverFragment.BaseFeedItem;
import com.linute.linute.MainContent.DiscoverFragment.Post;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 11/1/16.
 */

public class PostFeedDetail extends BaseFeedDetail{

    private Post mPost;

    public PostFeedDetail(Post post){
        mPost = post;
    }

    @Override
    public BaseFeedItem getFeedItem() {
        return mPost;
    }

    @Override
    public int getNumOfComments() {
        return mPost.getNumOfComments();
    }

    @Override
    public String getUserName() {
        return mPost.getUserName();
    }

    @Override
    public String getPostId() {
        return mPost.getId();
    }

    @Override
    public String getPostUserId() {
        return mPost.getUserId();
    }

    @Override
    public void setPostPrivacy(int privacy) {
        mPost.setPostPrivacy(privacy);
    }

    @Override
    public void addComment() {
        mPost.setNumOfComments(mPost.getNumOfComments() + 1);
    }

    @Override
    public void removeComment() {
        mPost.setNumOfComments(mPost.getNumOfComments() - 1);
    }

    @Override
    public void setAnonImage(String image) {
        mPost.setAnonImage(image);
    }

    @Override
    public boolean isAnon() {
        return mPost.getPrivacy() == 1;
    }

    @Override
    public boolean isAnonCommentsDisabled() {
        return mPost.isCommentAnonDisabled();
    }

    @Override
    public boolean isHidden() {
        return mPost.isPostHidden();
    }

    @Override
    public boolean isMuted() {
        return mPost.isPostMuted();
    }

    @Override
    public void setMuted(boolean mute) {
        mPost.setPostMuted(mute);
    }

    @Override
    public void setHidden(boolean hide) {
        mPost.setPostHidden(hide);
    }

    @Override
    public void updateFeedItem(JSONObject object) throws JSONException {
        mPost.updateInfo(object);
    }

    @Override
    public boolean isPrivacyChanged() {
        return mPost.isPrivacyChanged();
    }

    @Override
    public void setIsPrivacyChanged(boolean isPrivacyChanged) {
        mPost.setPrivacyChanged(isPrivacyChanged);
    }
}
