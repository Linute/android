package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;

import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.SquareImageView;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SquareVideoView;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by QiFeng on 3/8/16.
 */
public class VideoFeedHolder extends ImageFeedHolder {

    private SquareVideoView mSquareVideoView;

    private String mCollegeId;

    private boolean videoPaused = true;


    public VideoFeedHolder(final View itemView, List<Post> posts, Context context) {
        super(itemView, posts, context);
        mSquareVideoView = (SquareVideoView) itemView.findViewById(R.id.feed_detail_video);


        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = mSharedPreferences.getString("collegeId", "");

        mSquareVideoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    videoPaused = true;
                    mSquareVideoView.setVisibility(View.GONE);
                    vPostImage.setVisibility(View.VISIBLE);
                }
            }
        });

        vPostImage.setOnClickListener(null); //let parent handle it

        itemView.findViewById(R.id.video_frame).setOnClickListener(
                new DoubleClickListener() {

                    @Override
                    public void onSingleClick(View v) {
                        if (vPostImage.getVisibility() == View.VISIBLE) { //image is there, so video hasnt been started yet
                            if (mVideoUrl == null) return;

                            vPostImage.setVisibility(View.GONE);
                            mSquareVideoView.setVisibility(View.VISIBLE);
                            mSquareVideoView.setVideoURI(mVideoUrl);
                            mSquareVideoView.start();
                            videoPaused = false;
                            if (mPostId != null) sendImpressionsAsync(mPostId);

                        } else { //video already visible
                            if (videoPaused) {
                                mSquareVideoView.start();
                                videoPaused = false;
                            } else {
                                mSquareVideoView.pause();
                                videoPaused = true;
                            }
                        }
                    }

                    @Override
                    public void onDoubleClick(View v) {
                        final View layer = itemView.findViewById(R.id.feed_detail_hidden_animation);

                        AlphaAnimation a = new AlphaAnimation(0.0f, 0.75f);
                        a.setDuration(400);

                        final AlphaAnimation a2 = new AlphaAnimation(0.75f, 0.0f);
                        a2.setDuration(200);

                        a.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                layer.startAnimation(a2);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        layer.startAnimation(a);

                        if (!vLikesHeart.isChecked()) {
                            vLikesHeart.toggle();
                        }
                    }
                }
        );

        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { //restart video when finished
            @Override
            public void onCompletion(MediaPlayer mp) {
                mSquareVideoView.seekTo(0);
                mSquareVideoView.start();

                if (mPostId != null) sendImpressionsAsync(mPostId);
            }
        });

    }


    private String mPostId;
    private Uri mVideoUrl;


    public void bindModel(final Post post) {

        super.bindModel(post);
        videoPaused = true;

        if (mSquareVideoView.getVisibility() == View.VISIBLE) {
            mSquareVideoView.setVisibility(View.GONE);
            vPostImage.setVisibility(View.VISIBLE);
        }

        mPostId = post.getPostId();

        //// TODO: 3/8/16 fix
        mVideoUrl = Uri.parse(Utils.getVideoURL(post.getVideoUrl()));

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
                    body.put("user", getUserId());

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
