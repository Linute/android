package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
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
    private GetMoreFeed mGetMoreFeed; //interface that gets more feed

    private boolean mSendImpressions = true; //when scrolling back up, we don't want to send impressions

    private String mCollege;
    private String mUserId;


    public CheckBoxQuestionAdapter(List<Post> posts, Context context) {
        super(new MultiChoiceMode());
        mPosts = posts;
        this.context = context;

        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollege = sharedPreferences.getString("collegeId","");
        mUserId = sharedPreferences.getString("userID", "");
    }

    public void setGetMoreFeed(GetMoreFeed moreFeed){
        mGetMoreFeed = moreFeed;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        
        switch (viewType){
            case IMAGE_POST:
                return new ImageFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_image, parent, false),
                        mPosts,
                        context);
            
            case VIDEO_POST:
                return new VideoFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_video, parent, false),
                        mPosts,
                        context);
            
            default: //status post
                return new StatusFeedHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_detail_status, parent, false),
                        mPosts,
                        context);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof  VideoFeedHolder){
            ((VideoFeedHolder) holder).bindModel(mPosts.get(position));
        }else if (holder instanceof  ImageFeedHolder){
            ((ImageFeedHolder) holder).bindModel(mPosts.get(position));
        }else {
            ((StatusFeedHolder) holder).bindModel(mPosts.get(position));
        }


        if (position + 1 == mPosts.size()) {
            mGetMoreFeed.getMoreFeed();
        }else if (position == 0){
            MainActivity activity = (MainActivity) context;
            if (activity != null){
                activity.setFeedNotification(0);
                activity.setNumNewPostsInDiscover(0);
            }
        }

        //tracking impressions
        if (!mSendImpressions){ //scrolled back to the top
            if (position == 0)
                mSendImpressions = true;
        }else {
            sendImpressionsAsync(mPosts.get(position).getPostId());
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static final int IMAGE_POST = 0;
    public static final int STATUS_POST = 1;
    public static final int VIDEO_POST = 2;

    @Override
    public int getItemViewType(int position) {
        if (mPosts.get(position).isImagePost()){
            return mPosts.get(position).isVideoPost() ? VIDEO_POST : IMAGE_POST;
        }
        return  STATUS_POST;
    }

    public void setSendImpressions(boolean set){
        mSendImpressions = set;
    }

    private void sendImpressionsAsync (final String id){
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
                    }else {
                        Log.i(TAG, "impression not sent: no activity");
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }


    public interface GetMoreFeed {
        void getMoreFeed();
    }

}
