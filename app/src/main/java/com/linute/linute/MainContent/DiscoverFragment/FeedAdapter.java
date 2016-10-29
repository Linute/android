package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.ImpressionHelper;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Arman on 12/27/15.
 */
public class FeedAdapter extends BaseFeedAdapter {
    private static final String TAG = FeedAdapter.class.getSimpleName();
    private List<BaseFeedItem> mPosts;
    private Context context;

    private String mCollege;
    private String mUserId;

    private boolean mSectionTwo;

    public FeedAdapter(List<BaseFeedItem> posts, Context context, boolean sectiontwo) {
        mSectionTwo = sectiontwo;
        mPosts = posts;
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollege = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) return null;

        switch (viewType) {
            case LoadMoreViewHolder.FOOTER:
                return new LoadMoreViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_load_more, parent, false),
                        "", "That's all folks!");

            case POLL:
                return new PollViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feeddetail_poll, parent, false),
                        mPostAction
                );
            case IMAGE_POST:
                return new ImageFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_image, parent, false),
                        context,
                        mRequestManager,
                        mPostAction
                );

            case VIDEO_POST:
                return new VideoFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_video, parent, false),
                        context,
                        mRequestManager,
                        mPostAction
                );

            default: //status post
                return new StatusFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_status, parent, false),
                        context,
                        mRequestManager,
                        mPostAction
                );
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean sendImpression = true;

        if (holder instanceof PollViewHolder){
            ((PollViewHolder) holder).bindView((Poll)mPosts.get(position));
            sendImpression = false;
        }else if (holder instanceof VideoFeedHolder) {
            ((VideoFeedHolder) holder).bindModel((Post) mPosts.get(position));
        } else if (holder instanceof ImageFeedHolder) {
            ((ImageFeedHolder) holder).bindModel((Post) mPosts.get(position));
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mLoadState);
            sendImpression = false;
        } else {
            ((StatusFeedHolder) holder).bindModel((Post) mPosts.get(position));
        }

        if (position + 1 == mPosts.size()) {
            loadMoreFeed();
        } else if (!mSectionTwo && position == 0 && !NotificationsCounterSingleton.getInstance().discoverNeedsRefreshing()) {
            MainActivity activity = (MainActivity) context;
            if (activity != null) {
                activity.setFeedNotification(0);
                NotificationsCounterSingleton.getInstance().setNumOfNewPosts(0);
                NotificationEventBus.getInstance().setNotification(new NotificationEvent(NotificationEvent.DISCOVER, false));
            }
        }

        //tracking impressions
        if (sendImpression)
            ImpressionHelper.sendImpressionsAsync(mCollege, mUserId ,mPosts.get(position).getId());

    }

    @Override
    public int getItemCount() {
        return mPosts.isEmpty() ? 0 : mPosts.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mPosts.size()) {
            return LoadMoreViewHolder.FOOTER;
        }

        if (mPosts.get(position) instanceof Post) {
            Post p = (Post) mPosts.get(position);

            if (p.isImagePost())
                return p.isVideoPost() ? VIDEO_POST : IMAGE_POST;

            return STATUS_POST;
        } else if (mPosts.get(position) instanceof Poll){
            return POLL;
        }

        return -1;
    }


    public void setLoadState(short loadState) {
        mLoadState = loadState;
    }

}
