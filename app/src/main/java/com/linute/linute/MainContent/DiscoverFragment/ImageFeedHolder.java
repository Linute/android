package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomOnTouchListener;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends BaseFeedHolder {

    public static final String TAG = ImageFeedHolder.class.getSimpleName();
    public static final String FULL_VIEW = "full_view_image_feed";
    protected ImageView vPostImage;
    protected int mType;

    public ImageFeedHolder(final View itemView, Context context, RequestManager manager) {
        super(itemView, context, manager);
        mRequestManager = manager;
        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
        setUpOnClicks(itemView.findViewById(R.id.parent));
    }

    protected final void setUpOnClicks(View v) {
        v.setOnTouchListener(new CustomOnTouchListener(3200) {
            @Override
            protected void onSingleTap() {
                //Log.i(TAG, "onSingleTap: ");
                singleClick();
            }

            @Override
            protected void onDoubleTap(float x, float y) {
                //Log.i(TAG, "onDoubleTap: ");
                doubleClick();
            }

            @Override
            protected void onLongPress() {
                //Log.i(TAG, "onLongPress: ");
                longPress();
            }

            @Override
            protected void onLongPressCancelled(boolean thresholdMet) {
                cancelLongPress(thresholdMet);
            }
        });
    }

    private void doubleClick() {
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

        if (!vLikesHeart.isChecked()) {
            vLikesHeart.toggle();
        }
    }

    private void longPress() {
        if (mPost.getType() != Post.POST_TYPE_STATUS) {
            MainActivity activity = (MainActivity) mContext;
            VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
            activity.addFragmentOnTop(
                    ViewFullScreenFragment.newInstance(
                            Uri.parse(mPost.getType() == Post.POST_TYPE_VIDEO ? mPost.getVideoUrl() : mPost.getImage()),
                            mPost.getType(),
                            3000
                    ),
                    FULL_VIEW
            );

            //so parent doesn't intercept touch
            vPostImage.getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private void cancelLongPress(boolean thresholdMet) {
        if (!thresholdMet) {
            MainActivity activity = (MainActivity) mContext;
            if (activity != null && activity.getSupportFragmentManager().findFragmentByTag(FULL_VIEW) != null) {
               activity.getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void bindModel(Post post) {
        super.bindModel(post);

        // Set Post Image
        mType = post.getType();
        getEventImage(post.getImage());
    }

    protected void singleClick() {
    }


    private void getEventImage(String image) {
        mRequestManager
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(vPostImage);
    }
}
