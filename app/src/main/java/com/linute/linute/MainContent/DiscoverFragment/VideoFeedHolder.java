package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
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


    private TextureVideoView vSquareVideoView;
    private String mCollegeId;
    private boolean videoProcessing = false;

    private View vCinemaIcon;


    public VideoFeedHolder(final View itemView, Context context, SingleVideoPlaybackManager manager) {
        super(itemView, context, manager);
        vSquareVideoView = (TextureVideoView) itemView.findViewById(R.id.feed_detail_video);
        final SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = mSharedPreferences.getString("collegeId", "");

        vCinemaIcon = itemView.findViewById(R.id.cinema_icon);

        vSquareVideoView.setCustomSurfaceTextureListener(new TextureVideoView.CustomSurfaceTextureListener() {
            @Override
            public void onSurfaceDestroyed() {
                //when video surface destroyed, hide the video and show image
                //vPostImage.setVisibility(View.VISIBLE);
                vSquareVideoView.stopPlayback();
                vSquareVideoView.setVisibility(View.GONE);
                vCinemaIcon.setAlpha(1);
            }
        });

        //when video is loaded and ready to play
        vSquareVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { //when video ready to be played
            @Override
            public void onPrepared(final MediaPlayer mp) {
                // <shoddy-fix>
                //vCinemaIcon.postDelayed(new Runnable() {
                  //  @Override
                    //public void run() {
                        videoProcessing = false;
                        vCinemaIcon.clearAnimation();
                        vCinemaIcon.setAlpha(0);
                        sendImpressionsAsync(mPostId);
                        //vPostImage.setVisibility(View.GONE);
               //     }
                //}, 500);
                //</shoddy-fix>
            }
        });

        //when video finishes, restart video
        vSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //if video is paused AND finishes at the same time, video won't pause
                //if icon is showing, then user has paused video
                if (vCinemaIcon.getAlpha() != 1) {
                    vSquareVideoView.start();
                    sendImpressionsAsync(mPostId);
                }
            }
        });


        //hide the texture view
        vSquareVideoView.setHideVideo(new TextureVideoView.HideVideo() {
            @Override
            public void hideVideo() {
                videoProcessing = false;
                //vPostImage.setVisibility(View.VISIBLE);
                vSquareVideoView.setVisibility(View.GONE);
                vCinemaIcon.clearAnimation();
                vCinemaIcon.setAlpha(1);
            }
        });
    }

    @Override
    protected void setUpOnClicks() {
        setUpOnClicks(itemView.findViewById(R.id.video_frame));
    }

    @Override
    protected void singleClick() {
        if (mVideoUrl == null || videoProcessing) return;
        if (vSquareVideoView.getVisibility() == View.GONE) { //image is there, so video hasnt been started yet
            mSingleVideoPlaybackManager.playNewVideo(vSquareVideoView, mVideoUrl);
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
            vSquareVideoView.getSurfaceTexture().setDefaultBufferSize(0,0);
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

}
