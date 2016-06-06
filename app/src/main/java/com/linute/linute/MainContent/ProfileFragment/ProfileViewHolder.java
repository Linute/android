package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
}
