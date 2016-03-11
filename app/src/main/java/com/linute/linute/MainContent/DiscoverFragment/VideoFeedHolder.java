package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.SquareImageView;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SquareVideoView;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;
import com.volokh.danylo.video_player_manager.ui.MediaPlayerWrapper;
import com.volokh.danylo.video_player_manager.ui.VideoPlayerView;

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

    private boolean videoProcessing = false;


    public VideoFeedHolder(final View itemView, List<Post> posts, Context context, final VideoPlayerManager<MetaData> mVideoPlayerManager ) {
        super(itemView, posts, context);
        mSquareVideoView = (SquareVideoView) itemView.findViewById(R.id.feed_detail_video);


        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = mSharedPreferences.getString("collegeId", "");

        mSquareVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (vPostImage.getVisibility() == View.GONE) {
                    mVideoPlayerManager.stopAnyPlayback();
                    mSquareVideoView.setVisibility(View.GONE);
                    vPostImage.setVisibility(View.VISIBLE);
                    videoProcessing = false;
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });


        mSquareVideoView.addMediaPlayerListener(new MediaPlayerWrapper.MainThreadMediaPlayerListener() {
            @Override
            public void onVideoSizeChangedMainThread(int width, int height) {

            }

            @Override
            public void onVideoPreparedMainThread() {
                vPostImage.setVisibility(View.GONE);
                mSquareVideoView.setVisibility(View.VISIBLE);
                videoProcessing = false;
                if (mPostId != null) sendImpressionsAsync(mPostId);
            }

            @Override
            public void onVideoCompletionMainThread() {
                mSquareVideoView.start();
                if (mPostId != null) sendImpressionsAsync(mPostId);
            }

            @Override
            public void onErrorMainThread(int what, int extra) {

            }

            @Override
            public void onBufferingUpdateMainThread(int percent) {

            }

            @Override
            public void onVideoStoppedMainThread() {
                videoProcessing = false;
                vPostImage.setVisibility(View.VISIBLE);
                mSquareVideoView.setVisibility(View.GONE);
            }
        });


        View.OnClickListener onClickListener = new DoubleClickListener() {

            @Override
            public void onSingleClick(View v) {
                if (mVideoUrl == null || videoProcessing) return;
                if (vPostImage.getVisibility() == View.VISIBLE) { //image is there, so video hasnt been started yet
                    mVideoPlayerManager.playNewVideo(null, mSquareVideoView, mVideoUrl);
                    videoProcessing = true;
                } else {
                    mVideoPlayerManager.stopAnyPlayback();
                    mSquareVideoView.setVisibility(View.GONE);
                    vPostImage.setVisibility(View.VISIBLE);
                    videoProcessing = false;
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
        };

        mSquareVideoView.setOnClickListener(onClickListener);
        vPostImage.setOnClickListener(onClickListener);

               //todo if (mPostId != null) sendImpressionsAsync(mPostId);
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
    private String mVideoUrl;


    public void bindModel(final Post post) {
        super.bindModel(post);
        videoProcessing = false;

        if (mSquareVideoView.getVisibility() == View.VISIBLE) {
            mSquareVideoView.setVisibility(View.GONE);
            vPostImage.setVisibility(View.VISIBLE);
        }

        mPostId = post.getPostId();

        //// TODO: 3/8/16 fix
        mVideoUrl = Utils.getVideoURL(post.getVideoUrl());

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
