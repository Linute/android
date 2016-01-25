package com.linute.linute.MainContent.DiscoverFragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
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
 * Created by Arman on 12/27/15.
 */
public class CheckBoxQuestionViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = CheckBoxQuestionViewHolder.class.getSimpleName();
    private ChoiceCapableAdapter mCheckBoxChoiceCapableAdapters;

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

    protected List<Post> mPosts;

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private LSDKUser mUser;

    public CheckBoxQuestionViewHolder(ChoiceCapableAdapter adapter, View itemView, List<Post> posts, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUser = new LSDKUser(mContext);

        mPosts = posts;
        mCheckBoxChoiceCapableAdapters = adapter;

        vPostUserName = (TextView) itemView.findViewById(R.id.postUserName);
        vPostText = (TextView) itemView.findViewById(R.id.postText);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumLikes);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeartStatus);
        vPostImage = (ImageView) itemView.findViewById(R.id.postImage);
        vUserImage = (CircularImageView) itemView.findViewById(R.id.postUserImage);
        vPostTime = (TextView) itemView.findViewById(R.id.postTimeElapsed);

        vPostImageLinear = (LinearLayout) itemView.findViewById(R.id.post_image_linear);
        vPostTimeImage = (TextView) itemView.findViewById(R.id.postTimeElapsedImageView);
        vLikesTextImage = (TextView) itemView.findViewById(R.id.postNumLikesImage);
        vLikesHeartImage = (CheckBox) itemView.findViewById(R.id.postHeartStatusImage);

        vLikesHeart.setOnCheckedChangeListener(this);
        vLikesHeartImage.setOnCheckedChangeListener(this);
        vPostText.setOnClickListener(this);
        vPostImage.setOnClickListener(this);
        vUserImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        //create a base activity to support this?
        MainActivity activity = (MainActivity) mContext;

        if(activity == null) return;

        //tap image or name
        if (v == vUserImage || v == vPostUserName){
            activity.addFragmentToContainer(
                    TaptUserProfileFragment.newInstance(
                            mPosts.get(getAdapterPosition()).getUserName()
                            ,mPosts.get(getAdapterPosition()).getUserId())
            );
        }

        //status or image tapped
        else if (v == vPostImage || v == vPostText){
            activity.addFragmentToContainer(
                    FeedDetailPage.newInstance(v == vPostImage
                            , mPosts.get(getAdapterPosition()).getPostId()
                            , mPosts.get(getAdapterPosition()).getUserId())
            );
        }

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
                    }

                }
            });

            mCheckBoxChoiceCapableAdapters.notifyItemChanged(getAdapterPosition());
        } else if (!isChecked && mPosts.get(getAdapterPosition()).isPostLiked()) {
            mPosts.get(getAdapterPosition()).setPostLiked(false);
            mPosts.get(getAdapterPosition()).setNumLike(Integer.parseInt(mPosts.get(getAdapterPosition()).getNumLike()) - 1);

            Map<String, Object> postData = new HashMap<>();
            postData.put("isDeleted", true);
            new LSDKEvents(mContext).updateLike(postData, mPosts.get(getAdapterPosition()).getUserLiked(), new Callback() {
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

            mCheckBoxChoiceCapableAdapters.notifyItemChanged(getAdapterPosition());
        }
    }

    void bindModel(Post post) {
        // Set User Image
        // Set User Name
        if (post.getPrivacy() == 0) {
            getImage(post, 1);
            vPostUserName.setText(post.getUserName());
        } else {
            vUserImage.setImageResource(R.drawable.profile_picture_placeholder);
            vPostUserName.setText("Anonymous");
        }

        if (!post.getImage().equals("")) {
            // Set Post Image
            getImage(post, 2);
            vPostImage.setVisibility(View.VISIBLE);
            vPostText.setVisibility(View.GONE);
            vPostTimeImage.setText(post.getPostTime());
            // Set Like/Number/Time
            vPostTime.setVisibility(View.GONE);
            vLikesText.setVisibility(View.GONE);
            vLikesHeart.setVisibility(View.GONE);
            vLikesHeartImage.setChecked(post.isPostLiked());
            vLikesTextImage.setText(post.getNumLike());
            vPostTimeImage.setText(post.getPostTime());

            vPostImageLinear.setVisibility(View.VISIBLE);
            vPostTimeImage.setVisibility(View.VISIBLE);
        } else {
            // Set Post Text
            vPostImage.setVisibility(View.GONE);
            vPostText.setVisibility(View.VISIBLE);
            vPostText.setText(post.getTitle());
            // Set Like/Number/Time
            vPostTime.setVisibility(View.VISIBLE);
            vLikesText.setVisibility(View.VISIBLE);
            vLikesHeart.setVisibility(View.VISIBLE);
            vLikesHeart.setChecked(post.isPostLiked());
            vLikesText.setText(post.getNumLike());
            vPostTime.setText(post.getPostTime());

            vPostImageLinear.setVisibility(View.GONE);
            vPostTimeImage.setVisibility(View.GONE);
        }
    }

    private void getImage(Post post, int type) {
        Glide.with(mContext)
                .load(type == 1 ? Utils.getImageUrlOfUser(post.getUserImage()) : Utils.getEventImageURL(post.getImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(type == 1 ? vUserImage : vPostImage);
    }
}