package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleAndSingleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SquareVideoView;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 3/8/16.
 */
public class VideoFeedHolder extends ImageFeedHolder {

    private SquareVideoView mSquareVideoView;

    private String mCollegeId;

    private boolean videoProcessing = false;

    private View vCinemaIcon;


    public VideoFeedHolder(final View itemView, Context context, final SingleVideoPlaybackManager manager) {
        super(itemView, context);
        mSquareVideoView = (SquareVideoView) itemView.findViewById(R.id.feed_detail_video);


        final SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = mSharedPreferences.getString("collegeId", "");

        vCinemaIcon = itemView.findViewById(R.id.cinema_icon);

        mSquareVideoView.setCustomSurfaceTextureListener(new TextureVideoView.CustomSurfaceTextureListener() {
            @Override
            public void onSurfaceDestroyed() {
                //when video surface destroyed, hide the video and show image
                vPostImage.setVisibility(View.VISIBLE);
                mSquareVideoView.setVisibility(View.GONE);
                vCinemaIcon.setAlpha(1);
            }
        });

        //when video is loaded and ready to play
        mSquareVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { //when video ready to be played
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoProcessing = false;
                vPostImage.setVisibility(View.GONE);
                vCinemaIcon.clearAnimation();
                vCinemaIcon.setAlpha(0);
                sendImpressionsAsync(mPostId);
            }
        });

        //when video finishes, restart video
        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //if video is paused AND finishes at the same time, video won't pause
                //if icon is showing, then user has paused video
                if (vCinemaIcon.getAlpha() != 1) {
                    mSquareVideoView.start();
                    sendImpressionsAsync(mPostId);
                }
            }
        });

        //hide the texture view
        mSquareVideoView.setHideVideo(new TextureVideoView.HideVideo() {
            @Override
            public void hideVideo() {
                videoProcessing = false;
                vPostImage.setVisibility(View.VISIBLE);
                mSquareVideoView.setVisibility(View.GONE);
                vCinemaIcon.clearAnimation();
                vCinemaIcon.setAlpha(1);
            }
        });

        itemView.findViewById(R.id.video_frame).setOnClickListener(new DoubleAndSingleClickListener() {

            @Override
            public void onSingleClick(View v) {
                if (mVideoUrl == null || videoProcessing) return;
                if (mSquareVideoView.getVisibility() == View.GONE) { //image is there, so video hasnt been started yet
                    mSquareVideoView.setVisibility(View.VISIBLE);
                    manager.playNewVideo(mSquareVideoView, mVideoUrl);
                    videoProcessing = true;
                    vCinemaIcon.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in_fade_out));
                } else {
                    if (mSquareVideoView.isPlaying()){
                        mSquareVideoView.pause();
                        vCinemaIcon.setAlpha(1);
                    }else {
                        mSquareVideoView.start();
                        vCinemaIcon.setAlpha(0);
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
        });

    }


    private String mPostId;
    private Uri mVideoUrl;


    @Override
    public void bindModel(final Post post) {
        super.bindModel(post);

        videoProcessing = false;

        if (mSquareVideoView.getVisibility() == View.VISIBLE) {
            mSquareVideoView.setVisibility(View.GONE);
            vPostImage.setVisibility(View.VISIBLE);
            vCinemaIcon.clearAnimation();
            vCinemaIcon.setAlpha(1f);
        }

        mPostId = post.getPostId();
        mVideoUrl = Uri.parse(post.getVideoUrl());

    }


    //sends info on how many times looped
    private void sendImpressionsAsync(final String id) {
        if (id == null) return;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BaseTaptActivity activity = (BaseTaptActivity) mContext;

                    if (activity == null) return;

                    JSONObject body = new JSONObject();

                    body.put("college", mCollegeId);
                    body.put("user", getUserId());

                    body.put("room", id);

                    activity.emitSocket(API_Methods.VERSION + ":posts:loops", body);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
