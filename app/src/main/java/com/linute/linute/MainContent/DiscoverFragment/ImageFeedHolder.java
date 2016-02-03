package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {

    public static final String TAG = ImageFeedHolder.class.getSimpleName();

    private ChoiceCapableAdapter mCheckBoxChoiceCapableAdapters;

    protected View vLikeButton;
    protected View vCommentButton;

    protected TextView vPostUserName;
    protected TextView vLikesText; //how many likes we have
    protected TextView vCommentText; //how many comments we have
    protected TextView vPostTime;
    protected CheckBox vLikesHeart; //toggle heart

    protected ImageView vPostImage;
    protected CircularImageView vUserImage;

    protected List<Post> mPosts;

    private Context mContext;
    private SharedPreferences mSharedPreferences;


    public ImageFeedHolder(ChoiceCapableAdapter adapter, View itemView, List<Post> posts, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mPosts = posts;
        mCheckBoxChoiceCapableAdapters = adapter;

        vLikeButton = itemView.findViewById(R.id.feed_control_bar_like_button);
        vCommentButton = itemView.findViewById(R.id.feed_control_bar_comments_button);

        vPostUserName = (TextView) itemView.findViewById(R.id.feedDetail_user_name);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumHearts);
        vCommentText = (TextView) itemView.findViewById(R.id.postNumComments);
        vPostTime = (TextView) itemView.findViewById(R.id.feedDetail_time_stamp);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeart);

        //vLikesHeart.setClickable(false);

        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
        vUserImage = (CircularImageView) itemView.findViewById(R.id.feedDetail_profile_image);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !mPosts.get(getAdapterPosition()).isPostLiked()) {
            mPosts.get(getAdapterPosition()).setPostLiked(true);
            mPosts.get(getAdapterPosition()).setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) + 1);

            Map<String, Object> postData = new HashMap<>();
            postData.put("owner", mSharedPreferences.getString("userID", ""));
            postData.put("event", mPosts.get(getAdapterPosition()).getPostId());
            new LSDKEvents(mContext).postLike(postData, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
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

            mCheckBoxChoiceCapableAdapters.notifyItemChanged(getAdapterPosition());
        } else if (!isChecked && mPosts.get(getAdapterPosition()).isPostLiked()) {
            mPosts.get(getAdapterPosition()).setPostLiked(false);
            mPosts.get(getAdapterPosition()).setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) - 1);

            Map<String, Object> postData = new HashMap<>();
            postData.put("event", mPosts.get(getAdapterPosition()).getPostId());
            new LSDKEvents(mContext).updateLike(postData, mPosts.get(getAdapterPosition()).getPostId(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
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

            mCheckBoxChoiceCapableAdapters.notifyItemChanged(getAdapterPosition());
        }
    }

    @Override
    public void onClick(View v) {

        //create a base activity to support this?
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

        //status or image tapped
        else if (v == vPostImage) {
            activity.addFragmentToContainer(
                    FeedDetailPage.newInstance(true
                            , mPosts.get(getAdapterPosition()).getPostId()
                            , mPosts.get(getAdapterPosition()).getUserId())
            );
        }

        //like button pressed
        else if (v == vLikeButton) {
            vLikesHeart.toggle();
        } else if (v == vCommentButton) {
            Toast.makeText(mContext, "Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }


    void bindModel(Post post) {
        if (post.getPrivacy() == 0) {
            getProfileImage(post.getUserImage());
            vPostUserName.setText(post.getUserName());
        } else {
            vUserImage.setImageResource(R.drawable.profile_picture_placeholder);
            vPostUserName.setText("Anonymous");
        }

        // Set Post Image
        getEventImage(post.getImage());
        vPostTime.setText(post.getPostTime());
        vLikesHeart.setChecked(post.isPostLiked());
        vLikesText.setText("Like ("+post.getNumLike()+")");
        vCommentText.setText("Comment (0)");
    }


    private void getProfileImage(String image) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(image))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vUserImage);
    }

    private void getEventImage(String image) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(image))
                .asBitmap()
                .placeholder(R.drawable.no_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vPostImage);
    }


}
