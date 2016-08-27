package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.RequestManager;
import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.ScalableVideoView;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by QiFeng on 3/8/16.
 */
public class VideoFeedHolder extends ImageFeedHolder implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, ScalableVideoView.HideVideo {


    private ScalableVideoView vSquareVideoView;
    private String mCollegeId;
    private boolean videoProcessing = false;

    private View vCinemaIcon;

    public VideoFeedHolder(final View itemView, Context context, RequestManager manager) {
        super(itemView, context, manager);
        //weird thing with this library where we have to seat a source before we do anything else
        vSquareVideoView = (ScalableVideoView) itemView.findViewById(R.id.video);
        vSquareVideoView.setHideVideo(this);

        final SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = mSharedPreferences.getString("collegeId", "");

        vCinemaIcon = itemView.findViewById(R.id.cinema_icon);
    }

    @Override
    protected void singleClick() {
        if (mVideoUrl == null || videoProcessing) return;
        if (vSquareVideoView.getVisibility() == View.GONE) { //image is there, so video hasnt been started yet
            VideoPlayerSingleton.getSingleVideoPlaybackManager().playNewVideo(mContext, vSquareVideoView, mVideoUrl);

            vSquareVideoView.prepareAsync(this);
            vSquareVideoView.setOnCompletionListener(this);

            videoProcessing = true;
            vCinemaIcon.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in_fade_out));
            vSquareVideoView.setVisibility(View.VISIBLE);
        } else {
            if (vSquareVideoView.isPlaying()) {
                vSquareVideoView.pause();
                vCinemaIcon.setAlpha(1);
            } else {
                vSquareVideoView.start();
                vCinemaIcon.setAlpha(0);
            }
        }
    }


    private String mPostId;
    private Uri mVideoUrl;

    @Override
    public void bindModel(final Post post) {
        super.bindModel(post);

        videoProcessing = false;

        if (vSquareVideoView.getVisibility() == View.VISIBLE) {
            vSquareVideoView.setVisibility(View.GONE);
            //vPostImage.setVisibility(View.VISIBLE);
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        //if video is paused AND finishes at the same time, video won't pause
        //if icon is showing, then user has paused video
        if (vCinemaIcon.getAlpha() != 1) {
            vSquareVideoView.start();
            sendImpressionsAsync(mPostId);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!vSquareVideoView.isVideoStopped()) {
            videoProcessing = false;
            vCinemaIcon.clearAnimation();
            vCinemaIcon.setAlpha(0);
            sendImpressionsAsync(mPostId);
            mp.start();
        }
    }

    @Override
    public void hideVideo() {
        videoProcessing = false;
        vPostImage.setVisibility(View.VISIBLE);
        vSquareVideoView.setVisibility(View.GONE);
        vCinemaIcon.clearAnimation();
        vCinemaIcon.setAlpha(1);
    }

}
