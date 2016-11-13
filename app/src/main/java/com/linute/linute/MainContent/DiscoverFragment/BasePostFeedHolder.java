package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomSnackbar;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.ProfileImageView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 3/8/16.
 */
public abstract class BasePostFeedHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {

    protected View vLikeButton;
    protected View vCommentButton;
    protected View vShareButton;

    protected TextView vPostUserName;
    protected TextView vLikesText; //how many likes we have
    protected TextView vCommentText; //how many comments we have
    protected TextView vShareText; //how many shares we have
    protected TextView vPostTime;
    protected CheckBox vLikesHeart; //toggle heart
    protected View vPrivacyChanged;

    protected ProfileImageView vUserImage;
    protected Context mContext;

    private String mUserId;
    private String mImageSignature;
    protected RequestManager mRequestManager;
    protected BaseFeedAdapter.PostAction mPostAction;

    protected int mFilterColor;

    protected Post mPost;

    protected boolean mEnableProfileView = true;

    public BasePostFeedHolder(final View itemView, final Context context, RequestManager manager, BaseFeedAdapter.PostAction action) {
        super(itemView);

        mFilterColor = ContextCompat.getColor(context, R.color.inactive_grey);
        mPostAction = action;
        mRequestManager = manager;
        mContext = context;
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

        vLikeButton = itemView.findViewById(R.id.feed_control_bar_like_button);
        vCommentButton = itemView.findViewById(R.id.feed_control_bar_comments_button);
        vShareButton = itemView.findViewById(R.id.feed_control_bar_share_button);

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
        vShareText = (TextView) itemView.findViewById(R.id.postNumShares);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
        vUserImage = (ProfileImageView) itemView.findViewById(R.id.feedDetail_profile_image);
        vPrivacyChanged = itemView.findViewById(R.id.privacy_changed);

        //vLikesHeart.setClickable(false);
        vLikesHeart.setOnCheckedChangeListener(this);

        vLikeButton.setOnClickListener(this);
        vCommentButton.setOnClickListener(this);
        vShareButton.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vUserImage.setOnClickListener(this);
        itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPostAction != null && mPost != null) {
                    mPostAction.clickedOptions(mPost, getAdapterPosition());
                }
            }
        });
    }

    public void bindModel(Post post) {
        mPost = post;

        if (post.getPrivacy() == 0) {
            getProfileImage(post.getUserImage());
            vPostUserName.setText(post.getUserName());
        } else {
            getAnonImage(post.getAnonImage());
            vPostUserName.setText("Anonymous");
        }

        vPostTime.setText(post.getPostTime());
        vLikesHeart.setChecked(post.isPostLiked());
        vLikesText.setText(String.valueOf(post.getNumLike()));
        vCommentText.setText(String.valueOf(post.getNumOfComments()));

        if (vPrivacyChanged != null)
            vPrivacyChanged.setVisibility(post.isPrivacyChanged() ? View.VISIBLE : View.GONE);

        if (post.getNumOfComments() > 0) {
            ((ImageView) vCommentButton.findViewById(R.id.postComments)).clearColorFilter();
        } else if (mContext != null) {
            ((ImageView) vCommentButton.findViewById(R.id.postComments))
                    .setColorFilter(mFilterColor, PorterDuff.Mode.SRC_ATOP);
        }

    }

    public void setEnableProfileView(boolean enableProfileView){
        mEnableProfileView = enableProfileView;
    }

    //Like Post
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (mPost == null || activity == null) return;
        boolean emit = false;

        if (isChecked && !mPost.isPostLiked()) {
            mPost.setPostLiked(true);
            mPost.setNumLike(Integer.parseInt(mPost.getNumLike()) + 1);
            vLikesText.setText(String.valueOf(mPost.getNumLike()));
            emit = true;
        } else if (!isChecked && mPost.isPostLiked()) {
            mPost.setPostLiked(false);
            mPost.setNumLike(Integer.parseInt(mPost.getNumLike()) - 1);
            vLikesText.setText(String.valueOf(mPost.getNumLike()));
            emit = true;
        }

        if (emit) {
            try {
                JSONObject body = new JSONObject();
                body.put("user", mUserId);
                body.put("room", mPost.getId());
                TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:like", body);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    protected void getProfileImage(String image) {
        mRequestManager
                .load(image)
                .dontAnimate()
                .signature(new StringSignature(mImageSignature))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    protected void getAnonImage(String image) {
        mRequestManager
                .load(image)
                .dontAnimate()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    protected String getUserId() {
        return mUserId;
    }

    @Override
    public void onClick(View v) {

        final BaseTaptActivity activity = (BaseTaptActivity) mContext;

        if (activity == null || mPost == null) return;

        //tap image or name
        if ((v == vUserImage || v == vPostUserName)) {
            if (mPost.getPrivacy() == 0 && mEnableProfileView)
                activity.addFragmentToContainer(
                        TaptUserProfileFragment.newInstance(
                                mPost.getUserName()
                                , mPost.getUserId())
                );
        }

        //like button pressed
        else if (v == vLikeButton) {
            vLikesHeart.toggle();
        } else if (v == vCommentButton) {
            vCommentButton.setAlpha(.5f);
            vCommentButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    vCommentButton.setAlpha(1);
                }
            }, 500);
            activity.addFragmentToContainer(
                    FeedDetailPage.newInstance(mPost)
            );
        }else if(v == vShareButton){
            vShareButton.findViewById(R.id.shareProgress).setVisibility(View.VISIBLE);
            vShareButton.findViewById(R.id.postShare).setVisibility(View.GONE);
            mPostAction.startShare(mPost, new BaseFeedAdapter.ShareProgressListener() {
                        @Override
                        public void updateShareProgress(final int progress) {
                            vShareButton.post(new Runnable() {
                                @Override
                                public void run() {
                                    final DonutProgress donutProgress = (DonutProgress) vShareButton.findViewById(R.id.shareProgress);
                                    donutProgress.setProgress(progress);
                                    if(progress == 100 || progress == -1){
                                        if(progress == -1){
                                            CustomSnackbar.make(activity.findViewById(android.R.id.content), "Share Failed", CustomSnackbar.LENGTH_SHORT).setBackgroundColor(R.color.white).show();
//                                            Toast.makeText(mContext, "Share failed", Toast.LENGTH_SHORT).show();
                                            donutProgress.setProgress(100);
                                            donutProgress.setText("X");
                                            int red = mContext.getResources().getColor(R.color.red);
                                            donutProgress.setTextColor(red);
                                            donutProgress.setFinishedStrokeColor(red);
                                        }else{
                                            donutProgress.setProgress(100);
                                            donutProgress.setText("✓");
                                            int green = mContext.getResources().getColor(R.color.green_color);
                                            donutProgress.setTextColor(green);
                                            donutProgress.setFinishedStrokeColor(green);

                                        }
                                        vShareButton.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                vShareButton.findViewById(R.id.shareProgress).setVisibility(View.GONE);
                                                donutProgress.setProgress(0);
                                                donutProgress.setText(null);
                                                int blue = mContext.getResources().getColor(R.color.blue_color);
                                                donutProgress.setTextColor(blue);
                                                donutProgress.setFinishedStrokeColor(blue);
                                                vShareButton.findViewById(R.id.postShare).setVisibility(View.VISIBLE);
                                            }
                                        },1500);
                                    }
                                }
                            });
                        }
                    });
        }
    }


}
