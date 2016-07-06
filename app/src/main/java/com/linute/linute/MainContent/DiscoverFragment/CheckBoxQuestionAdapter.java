package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.MultiChoiceMode;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Arman on 12/27/15.
 */
public class CheckBoxQuestionAdapter extends ChoiceCapableAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = CheckBoxQuestionAdapter.class.getSimpleName();
    private List<Post> mPosts;
    private Context context;

    private LoadMoreViewHolder.OnLoadMore mGetMoreFeed; //interface that gets more feed
    private short mLoadState;

    private String mCollege;
    private String mUserId;

    private boolean mSectionTwo;


    public CheckBoxQuestionAdapter(List<Post> posts, Context context, boolean sectiontwo) {
        super(new MultiChoiceMode());
        mSectionTwo = sectiontwo;
        mPosts = posts;
        this.context = context;

        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollege = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", "");
    }

    public void setGetMoreFeed(LoadMoreViewHolder.OnLoadMore moreFeed) {
        mGetMoreFeed = moreFeed;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case LoadMoreViewHolder.FOOTER:
                return new LoadMoreViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.wrapping_footer_dark, parent, false),
                        "", "You have reached the end. Come back later for more!");

            case IMAGE_POST:
                return new ImageFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_image, parent, false),
                        context
                );

            case VIDEO_POST:
                return new VideoFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_video, parent, false),
                        context
                );

            default: //status post
                return new StatusFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_status, parent, false),
                        context);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean sendImpression = true;
        if (holder instanceof VideoFeedHolder) {
            ((VideoFeedHolder) holder).bindModel(mPosts.get(position));
        } else if (holder instanceof ImageFeedHolder) {
            ((ImageFeedHolder) holder).bindModel(mPosts.get(position));
        } else if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mLoadState);
            sendImpression = false;
        } else {
            ((StatusFeedHolder) holder).bindModel(mPosts.get(position));
        }

        if (position + 1 == mPosts.size()) {
            if (mGetMoreFeed != null)
                mGetMoreFeed.loadMore();
        } else if (!mSectionTwo && position == 0 && !NotificationsCounterSingleton.getInstance().discoverNeedsRefreshing()) {
            MainActivity activity = (MainActivity) context;
            if (activity != null) {
                activity.setFeedNotification(0);
                NotificationsCounterSingleton.getInstance().setNumOfNewPosts(0);

                if (!NotificationsCounterSingleton.getInstance().hasNotifications()) {
                    NotificationEventBus.getInstance().setNotification(new NotificationEvent(false));
                }
            }
        }

        //tracking impressions
        if (sendImpression)
            sendImpressionsAsync(mPosts.get(position).getPostId());

    }

    @Override
    public int getItemCount() {
        return mPosts.isEmpty() ? 0 : mPosts.size() + 1;
    }

    public static final int IMAGE_POST = 0;
    public static final int STATUS_POST = 1;
    public static final int VIDEO_POST = 2;

    @Override
    public int getItemViewType(int position) {
        if (position == mPosts.size()) {
            return LoadMoreViewHolder.FOOTER;
        } else if (mPosts.get(position).isImagePost()) {
            return mPosts.get(position).isVideoPost() ? VIDEO_POST : IMAGE_POST;
        }
        return STATUS_POST;
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

                    BaseTaptActivity activity = (BaseTaptActivity) context;

                    if (activity != null) {
                        activity.emitSocket(API_Methods.VERSION + ":posts:impressions", body);
                        //Log.i(TAG, "run: impression sent");
                    } else {
                        Log.i(TAG, "impression not sent: no activity");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setLoadState(short loadState) {
        mLoadState = loadState;
    }

}
