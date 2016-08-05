package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
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

    private static final String FULL_VIEW_TAG = "profile_full_viewer";

    protected ImageView vEventImage;

    private Context mContext;

    private UserActivityItem mUserActivityItem;

    private View vAnonIcon;
    private View vGradient;
    private View vMovieIcon;

    private RequestManager mRequestManager;


    public ProfileViewHolder(View itemView, final Context context, RequestManager manager) {
        super(itemView);

        mContext = context;
        vEventImage = (ImageView) itemView.findViewById(R.id.profile_grid_item_image);
        vGradient = itemView.findViewById(R.id.gradient);
        vAnonIcon = itemView.findViewById(R.id.anon_icon);
        vMovieIcon = itemView.findViewById(R.id.movie_icon);
        mRequestManager = manager;

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
        itemView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            MainActivity activity = (MainActivity) context;
                            if (activity != null &&
                                    activity.getSupportFragmentManager().findFragmentByTag(FULL_VIEW_TAG) != null) {
                                activity.getSupportFragmentManager().popBackStack();

                            }
                        }
                        return false;
                    }
                }
        );
    }

    void bindModel(UserActivityItem userActivityItem) {
        //profile image on the right
        mRequestManager
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

            if (userActivityItem.hasVideo()) {
                vMovieIcon.setVisibility(View.VISIBLE);
                vGradient.setVisibility(View.VISIBLE);
            } else {
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
        if (mUserActivityItem.getPost().getType() != Post.POST_TYPE_STATUS) {
            MainActivity activity = (MainActivity) mContext;
            VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
            activity.addFragmentOnTop(
                    ViewFullScreenFragment.newInstance(
                            Uri.parse(mUserActivityItem.getPost().getType() == Post.POST_TYPE_VIDEO ?
                                    mUserActivityItem.getPost().getVideoUrl() : mUserActivityItem.getPost().getImage()),
                            mUserActivityItem.getPost().getType(),
                            0
                    ),
                    FULL_VIEW_TAG
            );

            v.getParent().requestDisallowInterceptTouchEvent(true);
        }
        return true;
    }
}
