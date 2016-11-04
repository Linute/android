package com.linute.linute.MainContent.FeedDetailFragment;

import com.linute.linute.MainContent.DiscoverFragment.BaseFeedItem;
import com.linute.linute.MainContent.DiscoverFragment.Poll;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 11/1/16.
 */

public class PollFeedDetail extends BaseFeedDetail {

    private Poll mPoll;

    public PollFeedDetail(Poll poll){
        mPoll = poll;
    }

    @Override
    public BaseFeedItem getFeedItem() {
        return mPoll;
    }

    @Override
    public int getNumOfComments() {
        return mPoll.getNumberOfComments();
    }

    @Override
    public String getUserName() {
        return "";
    }

    @Override
    public String getPostId() {
        return mPoll.getPostId();
    }

    @Override
    public String getPostUserId() {
        return null;
    }

    @Override
    public void setPostPrivacy(int privacy) {

    }

    @Override
    public void addComment() {
        mPoll.setNumberOfComments(mPoll.getNumberOfComments() + 1);
    }

    @Override
    public void removeComment() {
        mPoll.setNumberOfComments(mPoll.getNumberOfComments() - 1);
    }

    @Override
    public void setAnonImage(String image) {

    }

    @Override
    public boolean isAnon() {
        return true;
    }

    @Override
    public boolean isAnonCommentsDisabled() {
        return mPoll.isAnonymousCommentsDisabled();
    }

    @Override
    public boolean isHidden() {
        return mPoll.isHidden();
    }

    @Override
    public boolean isMuted() {
        return mPoll.isMuted();
    }

    @Override
    public void setMuted(boolean mute) {
        mPoll.setMuted(mute);
    }

    @Override
    public void setHidden(boolean hide) {
        mPoll.setHidden(hide);
    }

    @Override
    public void updateFeedItem(JSONObject object) throws JSONException {
        mPoll.update(object);
    }

    @Override
    public boolean isPrivacyChanged() {
        return false;
    }

    @Override
    public void setIsPrivacyChanged(boolean isPrivacyChanged) {

    }
}
