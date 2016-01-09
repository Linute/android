package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder {
    protected ImageView vTextIcon;
    protected TextView vDescriptionLabel;
    protected TextView vTimeLabel;
    protected ImageView vEventImage;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public ProfileViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vTextIcon = (ImageView) itemView.findViewById(R.id.profilelistitem_event_text);
        vDescriptionLabel = (TextView) itemView.findViewById(R.id.activities_text);
        vTimeLabel = (TextView) itemView.findViewById(R.id.activities_date);
        vEventImage = (ImageView) itemView.findViewById(R.id.profilelistitem_event_image);
    }

    void bindModel(UserActivityItem userActivityItem) {
        if (userActivityItem.isImagePost()) {
            vTextIcon.setVisibility(View.GONE);
            vEventImage.setVisibility(View.VISIBLE);
        } else {
            vTextIcon.setVisibility(View.VISIBLE);
            vEventImage.setVisibility(View.GONE);
        }

        vDescriptionLabel.setText(userActivityItem.getDescription());
        vTimeLabel.setText(userActivityItem.getPostDate());

        //profile image on the right
        Glide.with(mContext)
                .load(userActivityItem.getEventImagePath())
                .asBitmap()
                .placeholder(R.drawable.no_image_placeholder)
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(vEventImage);
    }
}
