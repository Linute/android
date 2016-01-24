package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetailHeaderViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = FeedDetailHeaderViewHolder.class.getSimpleName();
    private Context mContext;
    private RecyclerView.Adapter mAdapater;
    private SharedPreferences mSharedPreferences;

    protected TextView vPostUserName;
    protected TextView vPostText;
    protected TextView vLikesText;
    protected TextView vPostTime;
    protected CheckBox vLikesHeart;
    protected ImageView vPostImage;
    protected CircularImageView vUserImage;

    protected LinearLayout vPostImageLinear;
    protected TextView vPostTimeImage;
    protected TextView vLikesTextImage;
    protected CheckBox vLikesHeartImage;
    private FeedDetail vFeedDetail;

    public FeedDetailHeaderViewHolder(RecyclerView.Adapter adapter, View itemView, Context context) {
        super(itemView);

        mContext = context;
        mAdapater = adapter;

        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vPostUserName = (TextView) itemView.findViewById(R.id.postUserNameDetail);
        vPostText = (TextView) itemView.findViewById(R.id.postTextDetail);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumLikesDetail);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeartStatusDetail);
        vPostImage = (ImageView) itemView.findViewById(R.id.postImageDetail);
        vUserImage = (CircularImageView) itemView.findViewById(R.id.comment_head_post_user_image);
        vPostTime = (TextView) itemView.findViewById(R.id.postTimeElapsedDetail);

        vPostImageLinear = (LinearLayout) itemView.findViewById(R.id.post_image_linear_detail);
        vPostTimeImage = (TextView) itemView.findViewById(R.id.postTimeElapsedImageViewDetail);
        vLikesTextImage = (TextView) itemView.findViewById(R.id.postNumLikesImageDetail);
        vLikesHeartImage = (CheckBox) itemView.findViewById(R.id.postHeartStatusImageDetail);

        vLikesHeart.setOnCheckedChangeListener(this);
        vLikesHeartImage.setOnCheckedChangeListener(this);
        vUserImage.setOnClickListener(this);
    }

    void bindModel(FeedDetail feedDetail) {
        // Set User Image
        // Set User Name
        vFeedDetail = feedDetail;
        if (feedDetail.getPostPrivacy() == 0) {
            getImage(feedDetail, 1);
            vPostUserName.setText(feedDetail.getUserName());
        } else {
            vUserImage.setImageResource(R.drawable.profile_picture_placeholder);
            vPostUserName.setText("Anonymous");
        }

        if (feedDetail.getPostImage() != null && !feedDetail.getPostImage().equals("")) {
            // Set Post Image
            getImage(feedDetail, 2);
            vPostImage.setVisibility(View.VISIBLE);
            vPostText.setVisibility(View.GONE);
            vPostTimeImage.setText(feedDetail.getPostTime());
            // Set Like/Number/Time
            vPostTime.setVisibility(View.GONE);
            vLikesText.setVisibility(View.GONE);
            vLikesHeart.setVisibility(View.GONE);
            vLikesHeartImage.setChecked(feedDetail.isPostLiked());
            vLikesTextImage.setText(feedDetail.getPostLikeNum());
            vPostTimeImage.setText(feedDetail.getPostTime());

            vPostImageLinear.setVisibility(View.VISIBLE);
            vPostTimeImage.setVisibility(View.VISIBLE);
        } else {
            // Set Post Text
            vPostImage.setVisibility(View.GONE);
            vPostText.setVisibility(View.VISIBLE);
            vPostText.setText(feedDetail.getPostText());
            // Set Like/Number/Time
            vPostTime.setVisibility(View.VISIBLE);
            vLikesText.setVisibility(View.VISIBLE);
            vLikesHeart.setVisibility(View.VISIBLE);
            vLikesHeart.setChecked(feedDetail.isPostLiked());
            vLikesText.setText(feedDetail.getPostLikeNum());
            vPostTime.setText(feedDetail.getPostTime());

            vPostImageLinear.setVisibility(View.GONE);
            vPostTimeImage.setVisibility(View.GONE);
        }
    }

    private void getImage(FeedDetail feedDetail, int type) {
        Glide.with(mContext)
                .load(type == 1 ? Utils.getImageUrlOfUser(feedDetail.getUserImage()) : Utils.getEventImageURL(feedDetail.getPostImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(type == 1 ? vUserImage : vPostImage);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            ((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(true);
            ((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) + 1) + "");

            Map<String, Object> postData = new HashMap<>();
            postData.put("owner", mSharedPreferences.getString("userID", ""));
            postData.put("event", ((FeedDetailAdapter) mAdapater).getFeedDetail().getPostId());
            new LSDKEvents(mContext).postLike(postData, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("TAG", response.body().string());
                    }

                }
            });

            mAdapater.notifyDataSetChanged();
        } else if (!isChecked && ((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            ((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(false);
            ((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) - 1) + "");

            Map<String, Object> postData = new HashMap<>();
            postData.put("isDeleted", true);
            new LSDKEvents(mContext).updateLike(postData, ((FeedDetailAdapter) mAdapater).getFeedDetail().getUserLiked(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("TAG", response.body().string());
                    }
                    Log.d(TAG, response.body().string());
                }
            });

            mAdapater.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick: outside");
        if (v == vUserImage) {
            Log.i(TAG, "onClick: inside");
            if (vFeedDetail.getPostPrivacy() == 0) {
                Log.i(TAG, "onClick: privacy 0");
                ((MainActivity)mContext).addFragmentToContainer(
                        TaptUserProfileFragment.newInstance(
                                vFeedDetail.getUserName()
                                , vFeedDetail.getPostUserId()
                        ));
            }
        }
    }
}
