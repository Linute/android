package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
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
 * Created by QiFeng on 2/4/16.
 */
public class FeedDetailHeaderStatusViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {


    public static final String TAG = FeedDetailHeaderStatusViewHolder.class.getSimpleName();

    private Context mContext;
    private RecyclerView.Adapter mAdapater;
    private SharedPreferences mSharedPreferences;

    private View vLikeContainer;

    protected TextView vPostUserName;
    protected TextView vPostText;
    protected TextView vLikesText;
    protected TextView vCommentsText;
    protected TextView vPostTime;
    protected CheckBox vLikesHeart;
    protected CircularImageView vUserImage;

    private FeedDetail vFeedDetail;

    public FeedDetailHeaderStatusViewHolder(RecyclerView.Adapter adapter, View itemView, Context context) {
        super(itemView);

        mContext = context;
        mAdapater = adapter;

        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vPostText = (TextView) itemView.findViewById(R.id.feedDetail_status_post);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentsText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
        vLikeContainer = itemView.findViewById(R.id.feed_control_bar_like_button);

        vUserImage = (CircularImageView) itemView.findViewById(R.id.feedDetail_profile_image);

        vLikesHeart.setOnCheckedChangeListener(this);
        vUserImage.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vLikeContainer.setOnClickListener(this);

        itemView.findViewById(R.id.feedDetail_status_container).setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
            }

            @Override
            public void onDoubleClick(View v) {
                vLikesHeart.toggle();
            }
        });
    }

    void bindModel(FeedDetail feedDetail) {
        // Set User Image
        vFeedDetail = feedDetail;
        if (feedDetail.getPostPrivacy() == 0) {
            if (feedDetail.getUserImage() != null)
                getProfileImage(feedDetail.getUserImage());
            vPostUserName.setText(feedDetail.getUserName());
        } else if (feedDetail.getPostPrivacy() == 1){
            vUserImage.setImageResource(R.drawable.profile_picture_placeholder);
            vPostUserName.setText("Anonymous");
        }

        vPostText.setText(feedDetail.getPostText());
        vLikesHeart.setChecked(feedDetail.isPostLiked());
        vLikesText.setText("Like (" + feedDetail.getPostLikeNum() + ")");

        vCommentsText.setText("Comment (" + feedDetail.getNumOfComments() + ")");

        vPostTime.setText(feedDetail.getPostTime());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(true);
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) + 1) + "");

            vFeedDetail.setIsPostLiked(true);
            vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) + 1 + "");
            vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");

            Map<String, Object> postData = new HashMap<>();
            postData.put("owner", mSharedPreferences.getString("userID", ""));
            postData.put("event", ((FeedDetailAdapter) mAdapater).getFeedDetail().getPostId());
            new LSDKEvents(mContext).postLike(postData, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.i(TAG, "onFailure: ");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("TAG", response.body().string());
                    } else {
                        response.body().close();
                    }
                }
            });

            mAdapater.notifyDataSetChanged();
        } else if (!isChecked && ((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(false);
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) - 1) + "");

            vFeedDetail.setIsPostLiked(false);
            vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) - 1 + "");
            vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");

            Map<String, Object> postData = new HashMap<>();
            postData.put("isDeleted", true);
            new LSDKEvents(mContext).updateLike(postData, ((FeedDetailAdapter) mAdapater).getFeedDetail().getPostId(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.i(TAG, "onFailure: ");
                }
                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.d("TAG", response.body().string());
                    } else {
                        response.body().close();
                    }
                }
            });
            mAdapater.notifyDataSetChanged();
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
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

}
