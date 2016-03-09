package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewAnimation;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends BaseFeedHolder{

    public static final String TAG = ImageFeedHolder.class.getSimpleName();

//    protected View vLikeButton;
//    protected View vCommentButton;
//
//    protected TextView vPostUserName;
//    protected TextView vLikesText; //how many likes we have
//    protected TextView vCommentText; //how many comments we have
//    protected TextView vPostTime;
//    protected CheckBox vLikesHeart; //toggle heart

    protected ImageView vPostImage;
//    protected CircleImageView vUserImage;

    //protected List<Post> mPosts;

//    private Context mContext;
//
//    private String mUserId;
//    private String mImageSignature;
    //private SharedPreferences mSharedPreferences;


    public ImageFeedHolder(final View itemView, List<Post> posts, Context context) {
        super(itemView, posts, context);

//        mContext = context;
//        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//        mUserId = mSharedPreferences.getString("userID","");
//        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");
//
//        mPosts = posts;
//
//        vLikeButton = itemView.findViewById(R.id.feed_control_bar_like_button);
//        vCommentButton = itemView.findViewById(R.id.feed_control_bar_comments_button);
//
//        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
//        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
//        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
//        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
//        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);
//
//        //vLikesHeart.setClickable(false);
//        vLikesHeart.setOnCheckedChangeListener(this);

        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
//        vUserImage = (CircleImageView) itemView.findViewById(R.id.feedDetail_profile_image);
//
//        vLikeButton.setOnClickListener(this);
//        vCommentButton.setOnClickListener(this);
//        vPostUserName.setOnClickListener(this);
//        vUserImage.setOnClickListener(this);
        vPostImage.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
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

                if (!vLikesHeart.isChecked()){
                    vLikesHeart.toggle();
                }
            }
        });
    }

//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (isChecked && !mPosts.get(getAdapterPosition()).isPostLiked()) {
//
//            BaseTaptActivity activity = (BaseTaptActivity) mContext;
//            if (activity != null) {
//                try {
//                    JSONObject body = new JSONObject();
//                    body.put("user", mUserId);
//                    body.put("room", mPosts.get(getAdapterPosition()).getPostId());
//
//                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);
//
//                    Post p = mPosts.get(getAdapterPosition());
//                    p.setPostLiked(true);
//                    p.setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) + 1);
//                    vLikesText.setText("Like ("+p.getNumLike()+")");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        } else if (!isChecked && mPosts.get(getAdapterPosition()).isPostLiked()) {
//
//            BaseTaptActivity activity = (BaseTaptActivity) mContext;
//            if (activity != null) {
//                try {
//                    JSONObject body = new JSONObject();
//                    body.put("user", mUserId);
//                    body.put("room", mPosts.get(getAdapterPosition()).getPostId());
//
//                    activity.emitSocket(API_Methods.VERSION+":posts:like", body);
//
//                    Post p = mPosts.get(getAdapterPosition());
//                    p.setPostLiked(false);
//                    p.setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) - 1);
//                    vLikesText.setText("Like ("+p.getNumLike()+")");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    @Override
//    public void onClick(View v) {
//
//        BaseTaptActivity activity = (BaseTaptActivity) mContext;
//
//        if (activity == null) return;
//
//        //tap image or name
//        if ((v == vUserImage || v == vPostUserName) && mPosts.get(getAdapterPosition()).getPrivacy() == 0) {
//            activity.addFragmentToContainer(
//                    TaptUserProfileFragment.newInstance(
//                            mPosts.get(getAdapterPosition()).getUserName()
//                            , mPosts.get(getAdapterPosition()).getUserId())
//            );
//        }
//
//        //like button pressed
//        else if (v == vLikeButton) {
//            vLikesHeart.toggle();
//        }
//        else if (v == vCommentButton) {
//            activity.addFragmentToContainer(
//                    FeedDetailPage.newInstance(false, true
//                            , mPosts.get(getAdapterPosition()).getPostId()
//                            , mPosts.get(getAdapterPosition()).getUserId())
//            );
//        }
//    }


    void bindModel(Post post) {
        if (post.getPrivacy() == 0) {
            getProfileImage(post.getUserImage());
            vPostUserName.setText(post.getUserName());
        } else {
            getAnonImage(post.getAnonImage());
            vPostUserName.setText("Anonymous");
        }

        // Set Post Image
        getEventImage(post.getImage());
        vPostTime.setText(post.getPostTime());
        vLikesHeart.setChecked(post.isPostLiked());
        vLikesText.setText("Like (" + post.getNumLike() + ")");
        vCommentText.setText("Comment (" + post.getNumOfComments() + ")");
    }




    private void getEventImage(String image) {
        Glide.with(mContext)
                .load(Utils.getEventImageURL(image))
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vPostImage);
    }
}
