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
import com.linute.linute.MainContent.DiscoverFragment.ImageFeedHolder;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoFeedHolder;
import com.linute.linute.R;
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

    List<Post> mPosts;
    Context mContext;

    String mUserId;
    String mImageSignature;
    String mCollege;
    String mTrendId;


    TrendingItemAdapter(List<Post> posts, Context context, RequestManager manager,String trendId) {
        mContext = context;
        mRequestManager = manager;
        mPosts = posts;
        mTrendId = trendId;

        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID", "");
        mCollege = mSharedPreferences.getString("collegeId", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == Post.POST_TYPE_VIDEO) {
            return new VideoTrendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_item_video, parent, false),
                    mContext, mRequestManager);
        } else if (viewType == Post.POST_TYPE_IMAGE) {
            return new BaseTrendViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_item, parent, false),
                    mContext, mRequestManager);
        } else if (viewType == LoadMoreViewHolder.FOOTER) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_footer, parent, false),
                    "",
                    "Come back later for more!"
            );
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseTrendViewHolder) {
            ((BaseTrendViewHolder) holder).bindModel(mPosts.get(position));
        }else if (holder instanceof VideoTrendViewHolder){
            ((VideoFeedHolder)holder).bindModel(mPosts.get(position));
        }
        else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mLoadState);
        }

        if (position == mPosts.size() - 1)
            loadMoreFeed();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mPosts.size()) {
            return LoadMoreViewHolder.FOOTER;
        } else if (mPosts.get(position).isVideoPost()) {
            return Post.POST_TYPE_VIDEO;
        } else if (mPosts.get(position).isImagePost()) {
            return Post.POST_TYPE_IMAGE;
        } else {
            return UNDEFINED;
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size() == 0 ? 0 : mPosts.size() + 1;
    }


    public class BaseTrendViewHolder extends ImageFeedHolder {
        protected TextView vCollegeText;

        public BaseTrendViewHolder(View itemView, Context context, RequestManager requestManager) {
            super(itemView, context, requestManager);
            vCollegeText = (TextView) itemView.findViewById(R.id.college_name);
        }


        @Override
        public void bindModel(Post post) {
            super.bindModel(post);
            vCollegeText.setText(post.getCollegeName());
            sendImpressionsAsync(post.getPostId());
        }
    }


    public class VideoTrendViewHolder extends VideoFeedHolder{

        protected TextView vCollegeName;

        public VideoTrendViewHolder(View itemView, Context context, RequestManager manager) {
            super(itemView, context, manager);
            vCollegeName = (TextView) itemView.findViewById(R.id.college_name);
        }

        @Override
        public void bindModel(Post post) {
            super.bindModel(post);
            vCollegeName.setText(post.getCollegeName());
            sendImpressionsAsync(post.getPostId());
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

                    if (mTrendId != null) {
                        body.put("trend", mTrendId);
                    }

                    BaseTaptActivity activity = (BaseTaptActivity) mContext;

                    if (activity != null) {
                        activity.emitSocket(API_Methods.VERSION + ":posts:impressions", body);
                        //Log.i(TAG, "run: impression sent");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
