package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected ImageView vEventImage;
    protected View vCinemaIcon;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private Post mUserActivityItem;


    public ProfileViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vEventImage = (ImageView) itemView.findViewById(R.id.profile_grid_item_image);
        vCinemaIcon = itemView.findViewById(R.id.icon);

        itemView.setOnClickListener(this);
    }

    void bindModel(Post userActivityItem) {
        //profile image on the right
        Glide.with(mContext)
                .load(userActivityItem.getImage())
                .asBitmap()
                .placeholder(R.drawable.image_loading_background)
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(vEventImage);

        vCinemaIcon.setVisibility(userActivityItem.isVideoPost() ? View.VISIBLE : View.GONE);
        mUserActivityItem = userActivityItem;

    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null && mUserActivityItem != null) {
            activity.addFragmentToContainer(FeedDetailPage.newInstance(mUserActivityItem));
        }
    }
}
