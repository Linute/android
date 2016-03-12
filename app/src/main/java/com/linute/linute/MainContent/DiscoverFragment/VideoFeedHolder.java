package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleAndSingleClickListener;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SquareVideoView;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.TextureVideoView;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.MediaPlayerWrapper;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by QiFeng on 3/8/16.
 */
public class VideoFeedHolder extends ImageFeedHolder {

    private SquareVideoView mSquareVideoView;

    private String mCollegeId;

    private boolean videoProcessing = false;


    public VideoFeedHolder(final View itemView, List<Post> posts, Context context, final SingleVideoPlaybackManager manager) {
        super(itemView, posts, context);
        mSquareVideoView = (SquareVideoView) itemView.findViewById(R.id.feed_detail_video);


        final SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = mSharedPreferences.getString("collegeId", "");


        mSquareVideoView.setCustomSurfaceTextureListener(new TextureVideoView.CustomSurfaceTextureListener() {
            @Override
            public void onSurfaceDestroyed() {
                //when video surface destroyed, hide the video and show image
                vPostImage.setVisibility(View.VISIBLE);
                mSquareVideoView.setVisibility(View.GONE);
            }
        });

        mSquareVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { //when video ready to be played
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoProcessing = false;
                sendImpressionsAsync(mPostId);
            }
        });

        mSquareVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mSquareVideoView.start();
                sendImpressionsAsync(mPostId);
            }
        });

        mSquareVideoView.setHideVideo(new TextureVideoView.HideVideo() {
            @Override
            public void hideVideo() {
                videoProcessing = false;
                vPostImage.setVisibility(View.VISIBLE);
                mSquareVideoView.setVisibility(View.GONE);
            }
        });

        itemView.findViewById(R.id.video_frame).setOnClickListener(new DoubleAndSingleClickListener() {

            @Override
            public void onSingleClick(View v) {
                if (mVideoUrl == null || videoProcessing) return;

                if (vPostImage.getVisibility() == View.VISIBLE) { //image is there, so video hasnt been started yet
                    mSquareVideoView.setVisibility(View.VISIBLE);
                    manager.playNewVideo(mSquareVideoView, mVideoUrl);
                    videoProcessing = true;
                } else {
                    if (mSquareVideoView.isPlaying()){
                        mSquareVideoView.pause();
                    }else {
                        mSquareVideoView.start();
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


    @Override
    public void onClick(View v) {

        BaseTaptActivity activity = (BaseTaptActivity) mContext;

        if (activity == null) return;

        //tap image or name
        if ((v == vUserImage || v == vPostUserName) && mPosts.get(getAdapterPosition()).getPrivacy() == 0) {
            activity.addFragmentToContainer(
                    TaptUserProfileFragment.newInstance(
                            mPosts.get(getAdapterPosition()).getUserName()
                            , mPosts.get(getAdapterPosition()).getUserId())
            );
        }

        //like button pressed
        else if (v == vLikeButton) {
            vLikesHeart.toggle();
        }
        else if (v == vCommentButton) {
            activity.addFragmentToContainer(
                    FeedDetailPage.newInstance(true, true
                            , mPosts.get(getAdapterPosition()).getPostId()
                            , mPosts.get(getAdapterPosition()).getUserId())
            );
        }
    }


    private String mPostId;
    private Uri mVideoUrl;


    public void bindModel(final Post post) {
        super.bindModel(post);
        videoProcessing = false;

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
