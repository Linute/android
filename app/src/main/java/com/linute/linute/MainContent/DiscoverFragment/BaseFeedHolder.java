package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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

    protected CircleImageView vUserImage;

    protected List<Post> mPosts;

    protected Context mContext;

    private String mUserId;
    private String mImageSignature;

    public BaseFeedHolder(final View itemView, List<Post> posts, Context context) {
        super(itemView);

        mContext = context;
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserId = mSharedPreferences.getString("userID","");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

        mPosts = posts;

        vLikeButton = itemView.findViewById(R.id.feed_control_bar_like_button);
        vCommentButton = itemView.findViewById(R.id.feed_control_bar_comments_button);

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
        vUserImage = (CircleImageView) itemView.findViewById(R.id.feedDetail_profile_image);

        //vLikesHeart.setClickable(false);
        vLikesHeart.setOnCheckedChangeListener(this);

        vLikesHeart.setOnCheckedChangeListener(this);

        vLikeButton.setOnClickListener(this);
        vCommentButton.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vUserImage.setOnClickListener(this);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !mPosts.get(getAdapterPosition()).isPostLiked()) {

            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                try {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", mPosts.get(getAdapterPosition()).getPostId());

                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);

                    Post p = mPosts.get(getAdapterPosition());
                    p.setPostLiked(true);
                    p.setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) + 1);
                    vLikesText.setText("Like ("+p.getNumLike()+")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (!isChecked && mPosts.get(getAdapterPosition()).isPostLiked()) {

            BaseTaptActivity activity = (BaseTaptActivity) mContext;
            if (activity != null) {
                try {
                    JSONObject body = new JSONObject();
                    body.put("user", mUserId);
                    body.put("room", mPosts.get(getAdapterPosition()).getPostId());

                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);

                    Post p = mPosts.get(getAdapterPosition());
                    p.setPostLiked(false);
                    p.setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) - 1);
                    vLikesText.setText("Like ("+p.getNumLike()+")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
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
                    FeedDetailPage.newInstance(false, true
                            , mPosts.get(getAdapterPosition()).getPostId()
                            , mPosts.get(getAdapterPosition()).getUserId())
            );
        }
    }



    protected void getProfileImage(String image) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(image))
                .asBitmap()
                .signature(new StringSignature(mImageSignature))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    protected void getAnonImage(String image){
        Glide.with(mContext)
                .load(Utils.getAnonImageUrl(image))
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    protected String getUserId(){
        return mUserId;
    }

}
