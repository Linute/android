package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.AnimationUtils;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.CustomOnTouchListener;
import com.linute.linute.UtilsAndHelpers.Utils;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends BasePostFeedHolder {

    public static final String TAG = ImageFeedHolder.class.getSimpleName();
    public static final String FULL_VIEW = "full_view_image_feed";
    protected ImageView vPostImage;
    protected ProgressBar vProgressBar;
    protected View vTopLayer;
    protected int mType;
    protected int mScreenWidth;
    private final View vDarkOverlay;

    public ImageFeedHolder(final View itemView, Context context, RequestManager manager, BaseFeedAdapter.PostAction action) {
        super(itemView, context, manager, action);
        mRequestManager = manager;
        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
        vTopLayer = itemView.findViewById(R.id.feed_detail_hidden_animation);
        vProgressBar = (ProgressBar)itemView.findViewById(R.id.progress_bar);
        vDarkOverlay = itemView.findViewById(R.id.overlay_darken);
        setUpOnClicks(itemView.findViewById(R.id.parent));

        DisplayMetrics metrics = new DisplayMetrics();
        ((AppCompatActivity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mScreenWidth = metrics.widthPixels;
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
                doubleClick(x, y);
            }

            @Override
            protected void onLongPress() {
                //Log.i(TAG, "onLongPress: ");
                longPress();
            }

            @Override
            protected void onLongPressCancelled(boolean thresholdMet) {
//                cancelLongPress(thresholdMet);
            }
        });
    }

    private void doubleClick(float x, float y) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimationUtils.animateLollipop(vTopLayer,
                    (int) x,
                    (int) y,
                    (float) AnimationUtils.getMax(Math.hypot(x, y),
                            Math.hypot(x, vTopLayer.getHeight() - y),
                            Math.hypot(vTopLayer.getWidth() - x, y),
                            Math.hypot(vTopLayer.getWidth() - x, vTopLayer.getHeight() - y)
                    ));
        } else {
            AnimationUtils.animatePreLollipop(vTopLayer);
        }

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
                            0
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
        resizeViews(getNewViewHeight(post.getImageSize()));
        setEventImage(post.getImage());
    }

    protected void singleClick() {
    }

    protected void resizeViews(int height) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mScreenWidth, height);
        vPostImage.setLayoutParams(params);
        vTopLayer.setLayoutParams(params);
    }

    private int getNewViewHeight(PostSize s) {
        return (int) ((float) mScreenWidth * s.height / s.width);
    }


    private void setEventImage(String image) {
        if (vProgressBar != null)
            vProgressBar.setVisibility(View.VISIBLE);
        if(vDarkOverlay != null)
            vDarkOverlay.setVisibility(View.VISIBLE);

        //vPostImage.setImageBitmap(mPost.imageBase64 == null ? null :  Utils.decodeImageBase64(mPost.imageBase64));
        mRequestManager
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .dontAnimate()
                .placeholder(mPost.imageBase64 == null ? null : new BitmapDrawable(mContext.getResources(), Utils.decodeImageBase64(mPost.imageBase64)))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (vProgressBar != null)
                            vProgressBar.setVisibility(View.GONE);
                        if(vDarkOverlay != null)
                            vDarkOverlay.setVisibility(View.GONE);

                        return false;
                    }
                })
                .into(vPostImage);
    }

    public static final class ShareViewHolder extends ImageFeedHolder {
            TextView vCollegeText;

        ShareViewHolder(View itemView, Context context, RequestManager requestManager, BaseFeedAdapter.PostAction action) {
                super(itemView, context, requestManager, action);
                vCollegeText = (TextView) itemView.findViewById(R.id.college_name);
            vPostTime.setVisibility(View.GONE);
        }

            @Override
            public void bindModel(Post post) {
                super.bindModel(post);
                vCollegeText.setText(post.getCollegeName());
            }
    }

}
