package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 2/4/16.
 */
public class FeedDetailHeaderImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String TAG = FeedDetailHeaderImageViewHolder.class.getSimpleName();

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

    private String mUserId;
    private String mImageSignature;

    public FeedDetailHeaderImageViewHolder(RecyclerView.Adapter adapter, View itemView, Context context) {
        super(itemView);

        mContext = context;
        mAdapater = adapter;

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

        vLikesHeart.setOnCheckedChangeListener(this);
        vUserImage.setOnClickListener(this);
        vPostUserName.setOnClickListener(this);
        vLikeContainer.setOnClickListener(this);
        mPostImage.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
            }

            @Override
            public void onDoubleClick(View v) {
                vLikesHeart.toggle();
            }
        });
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

    void bindModel(FeedDetail feedDetail) {
        // Set User Image
        // Set User Name
        vFeedDetail = feedDetail;
        if (feedDetail.getPostPrivacy() == 0) {

            if (feedDetail.getUserImage() != null && !feedDetail.getUserImage().equals(""))
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

        getPostImage(feedDetail.getPostImage());
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            // ((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(true);
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) + 1) + "");
//            vFeedDetail.setIsPostLiked(true);
//            vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) + 1 + "");
//            vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");



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

//            Map<String, Object> postData = new HashMap<>();
//            postData.put("owner", mSharedPreferences.getString("userID", ""));
//            postData.put("event", ((FeedDetailAdapter) mAdapater).getFeedDetail().getPostId());
//            new LSDKEvents(mContext).postLike(postData, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Activity activity = (Activity) mContext;
//                    if (activity != null) {
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                vFeedDetail.setIsPostLiked(false);
//                                vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) - 1 + "");
//                                vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (!response.isSuccessful()) {
//                        Log.d("TAG", response.body().string());
//                        Activity activity = (Activity) mContext;
//                        if (activity != null) {
//                            activity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    vFeedDetail.setIsPostLiked(false);
//                                    vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) - 1 + "");
//                                    vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");
//                                }
//                            });
//                        }
//                    } else {
//                        response.body().close();
//                    }
//                }
//            });

        } else if (!isChecked && ((FeedDetailAdapter) mAdapater).getFeedDetail().isPostLiked()) {
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setIsPostLiked(false);
            //((FeedDetailAdapter) mAdapater).getFeedDetail().setPostLikeNum((Integer.parseInt(((FeedDetailAdapter) mAdapater).getFeedDetail().getPostLikeNum()) - 1) + "");
//            vFeedDetail.setIsPostLiked(false);
//            vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) - 1 + "");
//            vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");
//
//            Map<String, Object> postData = new HashMap<>();
//            postData.put("isDeleted", true);
//            new LSDKEvents(mContext).updateLike(postData, ((FeedDetailAdapter) mAdapater).getFeedDetail().getPostId(), new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                    Activity activity = (Activity) mContext;
//                    if (activity != null) {
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                vFeedDetail.setIsPostLiked(true);
//                                vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) + 1 + "");
//                                vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");
//                            }
//                        });
//                    }
//                    ;
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (!response.isSuccessful()) {
//                        Log.d("TAG", response.body().string());
//                        Activity activity = (Activity) mContext;
//                        if (activity != null) {
//                            activity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    vFeedDetail.setIsPostLiked(true);
//                                    vFeedDetail.setPostLikeNum(Integer.parseInt(vFeedDetail.getPostLikeNum()) + 1 + "");
//                                    vLikesText.setText("Like (" + vFeedDetail.getNumOfComments() + ")");
//                                }
//                            });
//                        }
//                    } else {
//                        response.body().close();
//                    }
//                }
//            });

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


    private void getProfileImage(String image) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(image))
                .asBitmap()
                .signature(new StringSignature(mImageSignature))
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    private void getPostImage(String image) {
        Glide.with(mContext)
                .load(Utils.getEventImageURL(image))
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(mPostImage);
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
