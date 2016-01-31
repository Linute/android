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
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetail;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    protected ImageView vTextIcon;
    protected TextView vDescriptionLabel;
    protected TextView vTimeLabel;
    protected ImageView vEventImage;

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    private boolean mIsImagePost;
    private String mPostId;
    private String mUserId;


    public ProfileViewHolder(View itemView, Context context) {
        super(itemView);

        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vTextIcon = (ImageView) itemView.findViewById(R.id.profilelistitem_event_text);
        vDescriptionLabel = (TextView) itemView.findViewById(R.id.activities_text);
        vTimeLabel = (TextView) itemView.findViewById(R.id.activities_date);
        vEventImage = (ImageView) itemView.findViewById(R.id.profilelistitem_event_image);

        itemView.setOnClickListener(this);
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
        vTimeLabel.setText(Utils.getTimeAgoString(userActivityItem.getPostDate()));

        //profile image on the right
        Glide.with(mContext)
                .load(userActivityItem.getEventImagePath())
                .asBitmap()
                .placeholder(R.drawable.no_image_placeholder)
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(vEventImage);

        mIsImagePost = userActivityItem.isImagePost();
        mPostId = userActivityItem.getEventID();
        mUserId = userActivityItem.getOwnerID();


    }

    @Override
    public void onClick(View v) {
        BaseTaptActivity activity = (BaseTaptActivity) mContext;
        if (activity != null){
            activity.addFragmentToContainer(FeedDetailPage.newInstance(mIsImagePost, mPostId, mUserId ));
        }
    }
}
