package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    protected TextView vPostText;
    protected TextView vLikesText;
    protected CheckBox vLikesHeart;
    protected ImageView vPostImage;
    protected CircularImageView vUserImage;
    protected View vBottomBorder;
    private final RelativeLayout vLikesLayout;

    protected List<Post> mPosts;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public CheckBoxQuestionViewHolder(ChoiceCapableAdapter adapter, View itemView, List<Post> posts, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME , Context.MODE_PRIVATE);

        mPosts = posts;
        mCheckBoxChoiceCapableAdapters = adapter;

        vPostText = (TextView) itemView.findViewById(R.id.postText);
//        vLikesText = (TextView) itemView.findViewById(R.id.postLikesNum);
        vLikesHeart = (CheckBox) itemView.findViewById(R.id.postLikesHeart);
        vPostImage = (ImageView) itemView.findViewById(R.id.postPicture);
        vUserImage = (CircularImageView) itemView.findViewById(R.id.eventUserImage);

        vLikesHeart.setOnCheckedChangeListener(this);

        vBottomBorder = (View) itemView.findViewById(R.id.postBottomBorder);
        vLikesLayout = (RelativeLayout) itemView.findViewById(R.id.likesRelativeLayout);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCheckBoxChoiceCapableAdapters.onChecked(getAdapterPosition(), isChecked);
        if (mCheckBoxChoiceCapableAdapters.isChecked(getAdapterPosition())) {
            mPosts.get(getAdapterPosition()).setNumLike(mPosts.get(getAdapterPosition()).getNumLike() + 1);
        } else {
            mPosts.get(getAdapterPosition()).setNumLike(mPosts.get(getAdapterPosition()).getNumLike() - 1);
        }
    }

    void bindModel(Post post) {
        // Set User Image
        getImage(post, 1);
        if (!post.getImage().equals("")) {
            // Set Post Image
            getImage(post, 2);
//            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            p.addRule(RelativeLayout.BELOW, R.id.postPicture);
//            vBottomBorder.setLayoutParams(p);
//            vPostImage.setVisibility(View.VISIBLE);
        } else {
//            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//            p.addRule(RelativeLayout.BELOW, R.id.postText);
//            vBottomBorder.setLayoutParams(p);
//            vPostImage.setVisibility(View.GONE);
            vPostText.setText(post.getTitle());
        }
//        vLikesText.setText(post.getNumLike());
        if (post.getNumLike() != 0) {
            mCheckBoxChoiceCapableAdapters.onChecked(getAdapterPosition(), true);
        }
        vLikesHeart.setChecked(mCheckBoxChoiceCapableAdapters.isChecked(getAdapterPosition()));
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