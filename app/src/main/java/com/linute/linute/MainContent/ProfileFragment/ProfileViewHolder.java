package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    protected ImageView vEventImage;

    private Context mContext;

    private UserActivityItem mUserActivityItem;

    private View vAnonIcon;
    private View vGradient;
    private View vMovieIcon;


    public ProfileViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        vEventImage = (ImageView) itemView.findViewById(R.id.profile_grid_item_image);
        vGradient = itemView.findViewById(R.id.gradient);
        vAnonIcon = itemView.findViewById(R.id.anon_icon);
        vMovieIcon = itemView.findViewById(R.id.movie_icon);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    void bindModel(UserActivityItem userActivityItem) {
        //profile image on the right
        Glide.with(mContext)
                .load(userActivityItem.getEventImagePath())
                .dontAnimate()
                .placeholder(R.drawable.image_loading_background)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(vEventImage);

        if (userActivityItem.isAnon()) {
            vAnonIcon.setVisibility(View.VISIBLE);
            vGradient.setVisibility(View.VISIBLE);
            vMovieIcon.setVisibility(userActivityItem.hasVideo() ? View.VISIBLE : View.GONE);
        } else {
            vAnonIcon.setVisibility(View.GONE);

            if (userActivityItem.hasVideo()){
                vMovieIcon.setVisibility(View.VISIBLE);
                vGradient.setVisibility(View.VISIBLE);
            }
            else {
                vGradient.setVisibility(View.GONE);
                vMovieIcon.setVisibility(View.GONE);
            }
        }


        mUserActivityItem = userActivityItem;

    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null && mUserActivityItem != null) {
            activity.addFragmentToContainer(FeedDetailPage.newInstance(mUserActivityItem.getPost()));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mUserActivityItem.getPost().getType() != Post.POST_TYPE_STATUS){
            MainActivity activity = (MainActivity) mContext;
            VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
            activity.addFragmentOnTop(
                    ViewFullScreenFragment.newInstance(
                            Uri.parse(mUserActivityItem.getPost().getType() == Post.POST_TYPE_VIDEO ?
                                    mUserActivityItem.getPost().getVideoUrl() : mUserActivityItem.getPost().getImage()),
                            mUserActivityItem.getPost().getType()
                    )
            );
            vEventImage.getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }
}
