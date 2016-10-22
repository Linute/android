package com.linute.linute.UtilsAndHelpers.BaseFeedClasses;

import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;

/**
 * Created by QiFeng on 9/2/16.
 */
public abstract class BaseFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int IMAGE_POST = 0;
    public static final int STATUS_POST = 1;
    public static final int VIDEO_POST = 2;
    public static final int POLL = 3;

    protected short mLoadState;
    protected RequestManager mRequestManager;
    protected LoadMoreViewHolder.OnLoadMore mGetMoreFeed; //interface that gets more feed
    protected PostAction mPostAction;

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        loadMoreFeed();
    }

    protected void loadMoreFeed(){
        if (mGetMoreFeed != null)
            mGetMoreFeed.loadMore();
    }


    public void setPostAction(PostAction action){
        mPostAction = action;
    }

    public void setLoadState(short loadState) {
        mLoadState = loadState;
    }

    public short getLoadState() {
        return mLoadState;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager manager) {
        mRequestManager = manager;
    }

    public void setGetMoreFeed(LoadMoreViewHolder.OnLoadMore moreFeed) {
        mGetMoreFeed = moreFeed;
    }


    public interface PostAction{
        void clickedOptions(Post p, int position);
    }
}
