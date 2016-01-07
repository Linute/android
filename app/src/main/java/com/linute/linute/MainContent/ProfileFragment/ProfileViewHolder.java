package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder {
    protected CircularImageView vProfileImage;
    protected TextView vUsernameLabel;
    protected TextView vDescriptionLabel;
    protected TextView vTimeLabel;
    protected ImageView vEventImage;

    private Context mContext;
    private int mActivityProfilePictureRadius;
    private int mEventPictureSide;
    private SharedPreferences mSharedPreferences;

    public ProfileViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mActivityProfilePictureRadius = mContext.getResources().getDimensionPixelSize(R.dimen.profilefragment_list_item_profile_radius);
        mEventPictureSide = mContext.getResources().getDimensionPixelSize(R.dimen.profilefragment_list_item_event_side);

        vProfileImage = (CircularImageView) itemView.findViewById(R.id.profilelistitem_prof_image);
        vUsernameLabel = (TextView) itemView.findViewById(R.id.profilelistitem_name);
        vDescriptionLabel = (TextView) itemView.findViewById(R.id.profilelistitem_description);
        vTimeLabel = (TextView) itemView.findViewById(R.id.profilelistitem_time);
        vEventImage = (ImageView) itemView.findViewById(R.id.profilelistitem_event_image);
    }

    void bindModel(UserActivityItem userActivityItem) {
        vUsernameLabel.setText(userActivityItem.getUserName());
        vDescriptionLabel.setText(userActivityItem.getDescription());
        vTimeLabel.setText(userActivityItem.getTimeString());

        //profile image on the left
        Glide.with(mContext)
                .load((Utils.getImageUrlOfUser(userActivityItem.getProfileImagePath())))
                .asBitmap()
                .override(mActivityProfilePictureRadius, mActivityProfilePictureRadius) //change image to the size we want
                .placeholder(R.drawable.profile_picture_placeholder)
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(vProfileImage);

        //profile image on the right
        Glide.with(mContext)
                .load(userActivityItem.getEventImagePath())
                .asBitmap()
                .override(mEventPictureSide, mEventPictureSide)
                .placeholder(R.drawable.no_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(vEventImage);
    }
}
