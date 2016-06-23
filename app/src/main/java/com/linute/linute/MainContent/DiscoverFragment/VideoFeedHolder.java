package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

/**
 * Created by QiFeng on 3/8/16.
 */
public class VideoFeedHolder extends ImageFeedHolder {

    public VideoFeedHolder(final View itemView, Context context, SingleVideoPlaybackManager manager) {
        super(itemView, context, manager);
    }

    @Override
    protected void singleClick() {
        if (mVideoUrl == null || mPostId == null) return;
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null) {
            activity.addFragmentOnTop(
                    ViewFullScreenFragment.newInstance(
                            mPostId,
                            Uri.parse(mPost.getVideoUrl()),
                            mPost.getType()
                    )
            );
        }

    }


    private String mPostId;
    private Uri mVideoUrl;

    @Override
    public void bindModel(final Post post) {
        super.bindModel(post);

        mPostId = post.getPostId();
        mVideoUrl = Uri.parse(post.getVideoUrl());

    }

}
