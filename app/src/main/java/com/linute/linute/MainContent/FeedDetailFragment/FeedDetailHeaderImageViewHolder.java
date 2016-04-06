package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.view.View;

import com.linute.linute.MainContent.DiscoverFragment.ImageFeedHolder;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

/**
 * Created by QiFeng on 2/4/16.
 */
public class FeedDetailHeaderImageViewHolder extends ImageFeedHolder {

    public static final String TAG = FeedDetailHeaderImageViewHolder.class.getSimpleName();


    public FeedDetailHeaderImageViewHolder(final View itemView, Context context) {
        super(itemView, context);
    }

    @Override
    public void onClick(View v) {

        BaseTaptActivity activity = (BaseTaptActivity) mContext;

        if (activity == null || mPost == null) return;

        //tap image or name
        if ((v == vUserImage || v == vPostUserName) && mPost.getPrivacy() == 0) {
            activity.addFragmentToContainer(
                    TaptUserProfileFragment.newInstance(
                            mPost.getUserName()
                            , mPost.getUserId())
            );
        }

        //like button pressed
        else if (v == vLikeButton) {
            vLikesHeart.toggle();
        }
    }



}
