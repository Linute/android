package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.RequestManager;
import com.linute.linute.MainContent.DiscoverFragment.VideoFeedHolder;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;


/**
 * Created by QiFeng on 3/10/16.
 */
public class FeedDetailHeaderVideoViewHolder extends VideoFeedHolder {

    public FeedDetailHeaderVideoViewHolder(View view, Context context, RequestManager manager){
        super(view, context, manager, null);
        view.findViewById(R.id.more).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;

        if (activity == null || mPost == null || mPost.getUserId() == null) return;

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
