package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;

/**
 * Created by QiFeng on 2/3/16.
 */
public class ImageFeedHolder extends BaseFeedHolder{

    public static final String TAG = ImageFeedHolder.class.getSimpleName();


    protected ImageView vPostImage;


    public ImageFeedHolder(final View itemView, Context context) {
        super(itemView, context);


        vPostImage = (ImageView) itemView.findViewById(R.id.feedDetail_event_image);
        vPostImage.setOnClickListener(new DoubleClickListener() {

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

                if (!vLikesHeart.isChecked()){
                    vLikesHeart.toggle();
                }
            }
        });
    }


    @Override
    public void bindModel(Post post) {
        super.bindModel(post);

        // Set Post Image
        getEventImage(post.getImage());

    }




    private void getEventImage(String image) {
        Glide.with(mContext)
                .load(image)
                .dontAnimate()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vPostImage);
    }
}
