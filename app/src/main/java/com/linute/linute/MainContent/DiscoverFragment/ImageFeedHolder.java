package com.linute.linute.MainContent.DiscoverFragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.CustomOnTouchListener;
import com.linute.linute.UtilsAndHelpers.Utils;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends BaseFeedHolder {

    public static final String TAG = ImageFeedHolder.class.getSimpleName();
    public static final String FULL_VIEW = "full_view_image_feed";
    protected ImageView vPostImage;
    protected View vProgressBar;
    protected View vTopLayer;
    protected int mType;
    protected int mScreenWidth;

    public ImageFeedHolder(final View itemView, Context context, RequestManager manager, BaseFeedAdapter.PostAction action) {
        super(itemView, context, manager, action);
        mRequestManager = manager;
        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
        vProgressBar = itemView.findViewById(R.id.post_image_progress_bar);
        vTopLayer = itemView.findViewById(R.id.feed_detail_hidden_animation);
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
//                longPress();
            }

            @Override
            protected void onLongPressCancelled(boolean thresholdMet) {
//                cancelLongPress(thresholdMet);
            }
        });
    }

    private void doubleClick(float x, float y) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateLollipop(vTopLayer,
                    (int) x,
                    (int) y,
                    (float) getMax(Math.hypot(x, y),
                            Math.hypot(x, vTopLayer.getHeight() - y),
                            Math.hypot(vTopLayer.getWidth() - x, y),
                            Math.hypot(vTopLayer.getWidth() - x, vTopLayer.getHeight() - y)
                    ));
        } else {
            animatePreLollipop(vTopLayer);
        }

        if (!vLikesHeart.isChecked()) {
            vLikesHeart.toggle();
        }
    }

    private double getMax(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateLollipop(final View v, int x, int y, float radius) {
        Animator animator = ViewAnimationUtils.createCircularReveal(v, x, y, 0, radius);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                v.setVisibility(View.INVISIBLE);
            }
        });

        v.setVisibility(View.VISIBLE);
        animator.start();
    }

    private void animatePreLollipop(final View layer) {
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
        Bitmap bm = Utils.decodeImageBase64(post.imageBase64);
        vPostImage.setImageBitmap(bm);
        resizeViews(getNewViewHeight(post.getImageSize()));
        getEventImage(post.getImage());
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


    private void getEventImage(String image) {
        if (vProgressBar != null)
            vProgressBar.setVisibility(View.VISIBLE);

        mRequestManager
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (vProgressBar != null)
                            vProgressBar.setVisibility(View.GONE);
                        return false;

                    }
                })
                .into(vPostImage);
    }
}
