package com.linute.linute.MainContent.DiscoverFragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
import com.linute.linute.SquareCamera.ImageUtility;
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

    @Override
    protected Uri getShareUri() {

        ShareViewHolder holder = new ShareViewHolder(LayoutInflater.from(mContext).inflate(R.layout.trending_item, (ViewGroup)itemView.getParent(), false),
                mContext, mRequestManager, mPostAction);
        holder.bindModel(mPost);

        holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(itemView.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(itemView.getHeight(), View.MeasureSpec.EXACTLY));
        holder.itemView.layout(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom());

        View headerFeedDetail = holder.itemView.findViewById(R.id.header_feed_detail);
        Bitmap returnedBitmap = Bitmap.createBitmap(headerFeedDetail.getWidth(), headerFeedDetail.getHeight()+holder.vPostImage.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = headerFeedDetail.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        headerFeedDetail.draw(canvas);

        int savecount = canvas.save();
        canvas.translate(0, headerFeedDetail.getHeight());
        vPostImage.draw(canvas);
        canvas.restoreToCount(savecount);

        Uri uri = ImageUtility.savePicture(mContext, returnedBitmap);
        Log.i(TAG, uri.getPath());
        returnedBitmap.recycle();
        return uri;
    }

    private static final class ShareViewHolder extends ImageFeedHolder {
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
