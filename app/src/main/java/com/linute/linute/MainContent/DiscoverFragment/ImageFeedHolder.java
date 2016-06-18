package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DoubleAndSingleClickListener;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends BaseFeedHolder {

    public static final String TAG = ImageFeedHolder.class.getSimpleName();

    protected ImageView vPostImage;
    protected ImageView vBlurred;

    protected int mType;
    protected SingleVideoPlaybackManager mSingleVideoPlaybackManager;

    public ImageFeedHolder(final View itemView, Context context, SingleVideoPlaybackManager manager) {
        super(itemView, context);
        mSingleVideoPlaybackManager = manager;
        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
        vBlurred = (ImageView) itemView.findViewById(R.id.blurred);
        setUpOnClicks();
    }

    protected void setUpOnClicks() {
        setUpOnClicks(vPostImage);
    }


    protected final void setUpOnClicks(View v) {
        v.setOnClickListener(new DoubleAndSingleClickListener() {

            @Override
            public void onSingleClick(View v) {
                singleClick();
            }

            @Override
            public void onDoubleClick(View v) {
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
        });

        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity activity = (MainActivity) mContext;
                if (mPost != null && activity != null && mPost.getType() != Post.POST_TYPE_STATUS) {

                    if (mSingleVideoPlaybackManager != null)
                        mSingleVideoPlaybackManager.stopPlayback();

                    activity.addFragmentOnTop(
                            ViewFullScreenFragment.newInstance(
                                    Uri.parse(mPost.getType() == Post.POST_TYPE_IMAGE ? mPost.getImage() : mPost.getVideoUrl()),
                                    mPost.getType()
                            )
                    );
                }
                return true;
            }
        });
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

        if (mType != Post.POST_TYPE_STATUS) {
            Glide.with(mContext)
                    .load(image)
                    .override(100, 100)
                    .bitmapTransform(new BlurTransformation(mContext))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(vBlurred);
        }else {
            vBlurred.setImageDrawable(null);
        }

        Glide.with(mContext)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(vPostImage);

    }
}
