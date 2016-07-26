package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.linute.linute.MainContent.SendTo.SendToFragment;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 3/8/16.
 */
public class BaseFeedHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener  {

    protected View vLikeButton;
    protected View vCommentButton;

    protected TextView vPostUserName;
    protected TextView vLikesText; //how many likes we have
    protected TextView vCommentText; //how many comments we have
    protected TextView vPostTime;
    protected CheckBox vLikesHeart; //toggle heart

    protected RoundedImageView vUserImage;
    protected Context mContext;
    protected View vShareButton;

    private String mUserId;
    private String mImageSignature;
    protected RequestManager mRequestManager;

    protected Post mPost;

    public BaseFeedHolder(final View itemView, final Context context, RequestManager manager) {
        super(itemView);
        mRequestManager = manager;
        mContext = context;
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID","");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

        vLikeButton = itemView.findViewById(R.id.feed_control_bar_like_button);
        vCommentButton = itemView.findViewById(R.id.feed_control_bar_comments_button);

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
        vUserImage = (RoundedImageView) itemView.findViewById(R.id.feedDetail_profile_image);
        vShareButton = itemView.findViewById(R.id.share);

        //vLikesHeart.setClickable(false);
        vLikesHeart.setOnCheckedChangeListener(this);

        vLikeButton.setOnClickListener(this);
        vCommentButton.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vUserImage.setOnClickListener(this);
        vShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseTaptActivity activity = (BaseTaptActivity) mContext;
                if (activity != null && mPost != null)
                    activity.addFragmentOnTop(SendToFragment.newInstance(mPost.getPostId()), "send_to");
            }
        });
    }

    public void bindModel(Post post){
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
        vLikesText.setText("Like (" + post.getNumLike() + ")");
        vCommentText.setText("Comment (" + post.getNumOfComments() + ")");
        ((ImageView)vCommentButton.findViewById(R.id.postComments)).setImageResource(post.getNumOfComments() > 0 ?
                R.drawable.ic_oval19_blue : R.drawable.ic_oval19);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (mPost == null) return;

        if (isChecked && !mPost.isPostLiked()) {

            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                try {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", mPost.getPostId());

                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);

                    mPost.setPostLiked(true);
                    mPost.setNumLike(Integer.parseInt(mPost.getNumLike()) + 1);
                    vLikesText.setText("Like ("+mPost.getNumLike()+")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (!isChecked && mPost.isPostLiked()) {

            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                try {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", mPost.getPostId());

                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);

                    mPost.setPostLiked(false);
                    mPost.setNumLike(Integer.parseInt(mPost.getNumLike()) - 1);
                    vLikesText.setText("Like ("+mPost.getNumLike()+")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    protected void getAnonImage(String image){
        mRequestManager
                .load(image)
                .dontAnimate()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    protected String getUserId(){
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
        }
        else if (v == vCommentButton) {
            activity.addFragmentToContainer(
                    FeedDetailPage.newInstance(mPost)
            );
        }
    }
}
