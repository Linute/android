package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SquareVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by QiFeng on 3/8/16.
 */
public class VideoFeedHolder extends RecyclerView.ViewHolder {

    private SquareVideoView mSquareVideoView;


    protected View vLikeButton;
    protected View vCommentButton;

    protected CircleImageView vUserImage;
    protected TextView vPostUserName;
    protected TextView vLikesText; //how many likes we have

    protected TextView vCommentText; //how many comments we have
    protected TextView vPostTime;
    protected TextView vLikesHeart;

    private Context mContext;

    private String mUserId;
    private String mImageSignature;
    private String mCollegeId;



    public VideoFeedHolder(View itemView, Context context) {
        super(itemView);
        mSquareVideoView = (SquareVideoView) itemView.findViewById(R.id.feed_detail_video);
        mContext = context;

        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID", "");
        mCollegeId = mSharedPreferences.getString("collegeId", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);

        vUserImage = (CircleImageView) itemView.findViewById(R.id.feedDetail_profile_image);

        mSquareVideoView.setKeepScreenOn(true);

        itemView.findViewById(R.id.video_frame).setOnClickListener(new View.OnClickListener() {
            private boolean videoPaused = true;

            @Override
            public void onClick(View v) {
                if (videoPaused) {
                    mSquareVideoView.start();
                    videoPaused = false;
                } else {
                    mSquareVideoView.pause();
                    videoPaused = true;
                }
            }
        });


        //// TODO: 3/8/16 onclick listeners
    }


    public void bindModel(final Post post) {

        mSquareVideoView.setVideoURI(Uri.parse("https://scontent-lga3-1.cdninstagram.com/t50.2886-16/12630057_104029733314966_194904134_n.mp4"));

        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { //restart video when finished
            @Override
            public void onCompletion(MediaPlayer mp) {
                mSquareVideoView.seekTo(0);
                mSquareVideoView.start();
                sendImpressionsAsync(post.getPostId());
            }
        });

    }


    //sends info on how many times looped
    private void sendImpressionsAsync(final String id) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseTaptActivity activity = (BaseTaptActivity) mContext;

                    if (activity == null) return;

                    JSONObject body = new JSONObject();

                    body.put("college", mCollegeId);
                    body.put("user", mUserId);

                    JSONArray mEventIds = new JSONArray();
                    mEventIds.put(id);
                    body.put("events", mEventIds);

                    activity.emitSocket(API_Methods.VERSION + ":posts:loops", body);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
