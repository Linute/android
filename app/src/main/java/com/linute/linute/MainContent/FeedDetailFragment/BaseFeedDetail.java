package com.linute.linute.MainContent.FeedDetailFragment;

import com.linute.linute.MainContent.DiscoverFragment.BaseFeedItem;
import com.linute.linute.MainContent.DiscoverFragment.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */

public abstract class BaseFeedDetail {

    //Can contain a load more header
    private LinkedList<Object> mComments = new LinkedList<>();

    public abstract BaseFeedItem getFeedItem();

    public abstract int getNumOfComments();

    public abstract String getUserName();

    public LinkedList<Object> getComments() {
        return mComments;
    }

    public void setComments(List<Comment> comments){
        mComments.addAll(comments);
    }

    public abstract String getPostId();

    public abstract String getPostUserId();

    public abstract void setPostPrivacy(int privacy);

    public abstract void addComment();

    public abstract void removeComment();

    public abstract void setAnonImage(String image);

    public abstract boolean isAnon();

    public abstract boolean isAnonCommentsDisabled();

    public abstract boolean isHidden();

    public abstract boolean isMuted();

    public abstract void setMuted(boolean mute);

    public abstract void setHidden(boolean hide);

    public abstract void updateFeedItem(JSONObject object) throws JSONException;
}
