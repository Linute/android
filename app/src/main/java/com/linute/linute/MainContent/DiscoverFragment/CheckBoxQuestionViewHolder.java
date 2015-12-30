package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

/**
 * Created by Arman on 12/27/15.
 */
public class CheckBoxQuestionViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener {
    private ChoiceCapableAdapter mCheckBoxChoiceCapableAdapters;

    protected TextView vPostUserName;
    protected TextView vPostText;
    protected TextView vLikesText;
    protected CheckBox vLikesHeart;
    protected ImageView vPostImage;
    protected CircularImageView vUserImage;

    protected List<Post> mPosts;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public CheckBoxQuestionViewHolder(ChoiceCapableAdapter adapter, View itemView, List<Post> posts, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mPosts = posts;
        mCheckBoxChoiceCapableAdapters = adapter;

        vPostUserName = (TextView) itemView.findViewById(R.id.postUserName);
        vPostText = (TextView) itemView.findViewById(R.id.postText);
        vLikesText = (TextView) itemView.findViewById(R.id.postNumLikes);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postHeartStatus);
        vPostImage = (ImageView) itemView.findViewById(R.id.postImage);
        vUserImage = (CircularImageView) itemView.findViewById(R.id.postUserImage);

        vLikesHeart.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && !mPosts.get(getAdapterPosition()).isPostLiked())
            Toast.makeText(mContext, "Update Server", Toast.LENGTH_SHORT).show();


    }

    void bindModel(Post post) {
        // Set User Image
        if (post.getPrivacy() == 1)
            getImage(post, 1);
        else
            vUserImage.setImageResource(R.drawable.profile_picture_placeholder);
        // Set User Name
        vPostUserName.setText(post.getUserName());
        if (!post.getImage().equals("")) {
            // Set Post Image
            getImage(post, 2);
            vPostImage.setVisibility(View.VISIBLE);
            vPostText.setVisibility(View.GONE);
        } else {
            // Set Post Text
            vPostText.setVisibility(View.VISIBLE);
            vPostImage.setVisibility(View.GONE);
            vPostText.setText(post.getTitle());
        }
        // Set Like/Number
        vLikesText.setText(post.getNumLike());
        vLikesHeart.setChecked(post.isPostLiked());
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