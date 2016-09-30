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
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.ProfileImageView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 3/8/16.
 */
public class BaseFeedHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {

    protected View vLikeButton;
    protected View vCommentButton;

    protected TextView vPostUserName;
    protected TextView vLikesText; //how many likes we have
    protected TextView vCommentText; //how many comments we have
    protected TextView vPostTime;
    protected CheckBox vLikesHeart; //toggle heart

    protected ProfileImageView vUserImage;
    protected Context mContext;

    private String mUserId;
    private String mImageSignature;
    protected RequestManager mRequestManager;
    protected BaseFeedAdapter.PostAction mPostAction;

    protected int mFilterColor;

    protected Post mPost;

    public BaseFeedHolder(final View itemView, final Context context, RequestManager manager, BaseFeedAdapter.PostAction action) {
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

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
        vUserImage = (ProfileImageView) itemView.findViewById(R.id.feedDetail_profile_image);

        //vLikesHeart.setClickable(false);
        vLikesHeart.setOnCheckedChangeListener(this);

        vLikeButton.setOnClickListener(this);
        vCommentButton.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vUserImage.setOnClickListener(this);
        itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPostAction != null && mPost != null){
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
//        ((ImageView) vCommentButton.findViewById(R.id.postComments)).setImageResource(post.getNumOfComments() > 0 ?
//                R.drawable.ic_oval19_blue : R.drawable.ic_oval19);
        if (post.getNumOfComments() > 0) {
            //979797
            ((ImageView) vCommentButton.findViewById(R.id.postComments)).clearColorFilter();
        } else if (mContext != null) {
            ((ImageView) vCommentButton.findViewById(R.id.postComments))
                    .setColorFilter(mFilterColor, PorterDuff.Mode.SRC_ATOP);
        }
    }


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
                body.put("room", mPost.getPostId());
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

        BaseTaptActivity activity = (BaseTaptActivity) mContext;

        if (activity == null || mPost == null) return;

        //tap image or name
        if ((v == vUserImage || v == vPostUserName) && mPost.getPrivacy() == 0) {
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
        }
    }
}
