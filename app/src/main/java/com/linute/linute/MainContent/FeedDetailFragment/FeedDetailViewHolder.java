package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetailViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    protected CircularImageView vCommentUserImage;
    protected TextView vCommentUserName;
    protected TextView vCommentUserText;

    private String mCommenterUserId;
    private String mUserName;

    public FeedDetailViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vCommentUserImage = (CircularImageView) itemView.findViewById(R.id.comment_user_image);
        vCommentUserName = (TextView) itemView.findViewById(R.id.comment_user_name);
        vCommentUserText = (TextView) itemView.findViewById(R.id.comment);

        vCommentUserName.setOnClickListener(this);
        vCommentUserImage.setOnClickListener(this);
    }

    void bindModel(Comment comment) {
        Glide.with(mContext)
                .load(Utils.getImageUrlOfUser(comment.getCommentUserProfileImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vCommentUserImage);
        vCommentUserName.setText(comment.getCommentUserName());
        vCommentUserText.setText(comment.getCommentUserPostText());
        if (comment.getCommentUserId().equals(mSharedPreferences.getString("userID", ""))) {
            vCommentUserName.setTextColor(Color.parseColor("#56bb1d"));
        } else {
            vCommentUserName.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }

        mCommenterUserId = comment.getCommentUserId();
        mUserName = comment.getCommentUserName();
    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null){
            activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mUserName, mCommenterUserId));
        }
    }
}
