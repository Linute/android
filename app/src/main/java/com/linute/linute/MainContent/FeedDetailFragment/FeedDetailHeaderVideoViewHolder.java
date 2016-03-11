package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SquareVideoView;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.volokh.danylo.video_player_manager.manager.PlayerItemChangeListener;
import com.volokh.danylo.video_player_manager.manager.SingleVideoPlayerManager;
import com.volokh.danylo.video_player_manager.manager.VideoPlayerManager;
import com.volokh.danylo.video_player_manager.meta.MetaData;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by QiFeng on 3/10/16.
 */
public class FeedDetailHeaderVideoViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {


    private Context mContext;
    private RecyclerView.Adapter mAdapater;

    private View vLikeContainer;


    protected TextView vPostUserName;
    protected TextView vLikesText;
    protected TextView vCommentsText;
    protected TextView vPostTime;
    protected CheckBox vLikesHeart;
    protected CircleImageView vUserImage;

    protected ImageView mPostImage;

    private FeedDetail vFeedDetail;

    private SquareVideoView vSquareVideo;

    private String mUserId;
    private String mImageSignature;

    private String mVideoUrl;

    private boolean videoProcessing = false;

    private VideoPlayerManager<MetaData> mVideoPlayerManager = new SingleVideoPlayerManager(new PlayerItemChangeListener() {
        @Override
        public void onPlayerItemChanged(MetaData metaData) {

        }
    });

    public FeedDetailHeaderVideoViewHolder(final View itemView, Context context, RecyclerView.Adapter adapater ) {
        super(itemView);

        mContext = context;
        mAdapater = adapater;

        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mUserId = mSharedPreferences.getString("userID","");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentsText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
        vLikeContainer = itemView.findViewById(R.id.feed_control_bar_like_button);

        vUserImage = (CircleImageView) itemView.findViewById(R.id.feedDetail_profile_image);

        mPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);

        vSquareVideo = (SquareVideoView) itemView.findViewById(R.id.feed_detail_video);

        vLikesHeart.setOnCheckedChangeListener(this);
        vUserImage.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vLikeContainer.setOnClickListener(this);


        View.OnClickListener onClickListener = new DoubleClickListener() {

            @Override
            public void onSingleClick(View v) {
                if (mVideoUrl == null || videoProcessing) return;
                if (mPostImage.getVisibility() == View.VISIBLE) { //image is there, so video hasnt been started yet
                    mVideoPlayerManager.playNewVideo(null, vSquareVideo, mVideoUrl);
                    videoProcessing = true;
                } else {
                    mVideoPlayerManager.stopAnyPlayback();
                    vSquareVideo.setVisibility(View.GONE);
                    mPostImage.setVisibility(View.VISIBLE);
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

        mPostImage.setOnClickListener(onClickListener);
    }


    public void bindViews(FeedDetail feedDetail){
        // Set User Image
        vFeedDetail = feedDetail;
        if (feedDetail.getPostPrivacy() == 0) {
            if (feedDetail.getUserImage() != null)
                getProfileImage(feedDetail.getUserImage());

            vPostUserName.setText(feedDetail.getUserName());
        } else if (feedDetail.getPostPrivacy() == 1){
            if (feedDetail.getAnonPic() != null && !feedDetail.getAnonPic().equals(""))
                getAnonImage(feedDetail.getAnonPic());

            vPostUserName.setText("Anonymous");
        }

        vLikesHeart.setChecked(feedDetail.isPostLiked());
        vLikesText.setText("Like (" + feedDetail.getPostLikeNum() + ")");

        vCommentsText.setText("Comment (" + feedDetail.getNumOfComments() + ")");

        vPostTime.setText(feedDetail.getPostTime());

        mVideoUrl = Utils.getVideoURL(feedDetail.getVideoUrl());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(true);
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) + 1) + "");

            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                try {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", vFeedDetail.getPostId());

                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);

                    vFeedDetail.setIsPostLiked(true);
                    vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) + 1 + "");
                    mAdapater.notifyItemChanged(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (!isChecked && ((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(false);
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) - 1) + "");

            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                try {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", vFeedDetail.getPostId());

                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);

                    vFeedDetail.setIsPostLiked(false);
                    vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) - 1 + "");
                    mAdapater.notifyItemChanged(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if ((v == vUserImage || v == vPostUserName) && vFeedDetail.getPostPrivacy() == 0) {
            ((BaseTaptActivity) mContext).addFragmentToContainer(
                    TaptUserProfileFragment.newInstance(
                            vFeedDetail.getUserName()
                            , vFeedDetail.getPostUserId()
                    ));
        } else if (v == vLikeContainer) {
            vLikesHeart.toggle();
        }
    }



    private void getProfileImage(String image) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(image))
                .asBitmap()
                .signature(new StringSignature(mImageSignature))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    private void getAnonImage(String image){
        Glide.with(mContext)
                .load(Utils.getAnonImageUrl(image))
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }
}
