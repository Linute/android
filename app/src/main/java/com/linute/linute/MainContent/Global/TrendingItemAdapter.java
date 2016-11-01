package com.linute.linute.MainContent.Global;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.DiscoverFragment.BaseFeedItem;
import com.linute.linute.MainContent.DiscoverFragment.ImageFeedHolder;
import com.linute.linute.MainContent.DiscoverFragment.Poll;
import com.linute.linute.MainContent.DiscoverFragment.PollViewHolder;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoFeedHolder;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by QiFeng on 5/14/16.
 */
public class TrendingItemAdapter extends BaseFeedAdapter {

    public static final int UNDEFINED = 666;

    private List<BaseFeedItem> mPosts;
    private Context mContext;

    private String mUserId;
    private String mCollege;
    private GlobalChoiceItem mGlobalItem;


    public TrendingItemAdapter(List<BaseFeedItem> posts, Context context, RequestManager manager, GlobalChoiceItem item) {
        mContext = context;
        mRequestManager = manager;
        mPosts = posts;
        mGlobalItem = item;

        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID", "");
        mCollege = mSharedPreferences.getString("collegeId", "");
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIDEO_POST) {
            return new VideoTrendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_item_video, parent, false),
                    mContext, mRequestManager, mPostAction);
        } else if (viewType == IMAGE_POST) {
            return new BaseTrendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_item, parent, false),
                    mContext, mRequestManager, mPostAction);
        } else if (viewType == LoadMoreViewHolder.FOOTER) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_load_more, parent, false),
                    "",
                    "That's all folks!"
            );
        }else if (viewType == POLL){
            return new PollViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.feeddetail_poll, parent, false)
                    //, mPostAction
            );
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof BaseTrendViewHolder) {
            ((BaseTrendViewHolder) holder).bindModel((Post) mPosts.get(position));
        } else if (holder instanceof VideoTrendViewHolder) {
            ((VideoFeedHolder) holder).bindModel((Post) mPosts.get(position));
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mLoadState);
        } else if (holder instanceof PollViewHolder){
            ((PollViewHolder) holder).bindView((Poll) mPosts.get(position));
        }

        if (position == mPosts.size() - 1) {
            loadMoreFeed();
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mPosts.size()) {
            return LoadMoreViewHolder.FOOTER;
        }

        if (mPosts.get(position) instanceof Post) {
            Post p = (Post) mPosts.get(position);
            if (p.isVideoPost()) {
                return VIDEO_POST;
            } else if (p.isImagePost()) {
                return IMAGE_POST;
            }
        } else if (mPosts.get(position) instanceof Poll) {
            return POLL;
        }

        return UNDEFINED;
    }

    @Override
    public int getItemCount() {
        return mPosts.size() == 0 ? 0 : mPosts.size() + 1;
    }


    private class BaseTrendViewHolder extends ImageFeedHolder {
        TextView vCollegeText;

        BaseTrendViewHolder(View itemView, Context context, RequestManager requestManager, PostAction action) {
            super(itemView, context, requestManager, action);
            vCollegeText = (TextView) itemView.findViewById(R.id.college_name);
        }

        @Override
        public void bindModel(Post post) {
            super.bindModel(post);
            vCollegeText.setText(post.getCollegeName());
            sendImpressionsAsync(post.getId());
        }
    }


    private class VideoTrendViewHolder extends VideoFeedHolder {

        TextView vCollegeName;

        VideoTrendViewHolder(View itemView, Context context, RequestManager manager, PostAction action) {
            super(itemView, context, manager, action);
            vCollegeName = (TextView) itemView.findViewById(R.id.college_name);
        }

        @Override
        public void bindModel(Post post) {
            super.bindModel(post);
            vCollegeName.setText(post.getCollegeName());
            sendImpressionsAsync(post.getId());
        }
    }


    private void sendImpressionsAsync(final String id) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject body = new JSONObject();

                    body.put("college", mCollege);
                    body.put("user", mUserId);

                    JSONArray mEventIds = new JSONArray();
                    mEventIds.put(id);
                    body.put("events", mEventIds);

                    if (mGlobalItem.key != null) {
                        body.put("trend", mGlobalItem.key);
                    }

                    BaseTaptActivity activity = (BaseTaptActivity) mContext;

                    if (activity != null) {
                        TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:impressions", body);
                        //Log.i(TAG, "run: impression sent");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
