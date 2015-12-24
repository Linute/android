package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

/**
 * Created by QiFeng on 12/4/15.
 */
public class ProfileActivityListAdapter extends BaseAdapter {

    private Context mContext;
    private List<UserActivityItem> mUserActivityItems;
    private int mActivityProfilePictureRadius;
    private int mEventPictureSide;

    public ProfileActivityListAdapter(Context context, List<UserActivityItem> userActivityItems){
        mContext = context;
        mUserActivityItems = userActivityItems;
        mActivityProfilePictureRadius = mContext.getResources().getDimensionPixelSize(R.dimen.profilefragment_list_item_profile_radius);
        mEventPictureSide = mContext.getResources().getDimensionPixelSize(R.dimen.profilefragment_list_item_event_side);
    }

    @Override
    public int getCount() {
        return mUserActivityItems.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Object getItem(int position) {
        //if (mUserActivityItems.size() == 0) return  null;
        return mUserActivityItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //if (mUserActivityItems.size() == 0) return  null;

        ActivityItemViewHolder holder;

        if (convertView == null){ //if current disappeared cell == null
            //create new cell
            convertView = LayoutInflater.from((mContext)).inflate(R.layout.list_item_profile_frag, null);
            holder = new ActivityItemViewHolder(convertView);

            convertView.setTag(holder);
        }else{ //get the disappeared one
            holder = (ActivityItemViewHolder)convertView.getTag();
        }
        //set new values
        holder.bindToActivityItem(mUserActivityItems.get(position));


        return convertView;
    }


    private class ActivityItemViewHolder{

        private CircularImageView mProfileImage;
        private TextView mUsernameLabel;
        private TextView mDescriptionLabel;
        private TextView mTimeLabel;
        private ImageView mEventImage;

        public ActivityItemViewHolder(View itemView) { //sets the views
            mProfileImage = (CircularImageView)itemView.findViewById(R.id.profilelistitem_prof_image);
            mUsernameLabel = (TextView) itemView.findViewById(R.id.profilelistitem_name);
            mDescriptionLabel = (TextView)itemView.findViewById(R.id.profilelistitem_description);
            mTimeLabel = (TextView)itemView.findViewById(R.id.profilelistitem_time);
            mEventImage = (ImageView)itemView.findViewById(R.id.profilelistitem_event_image);
        }

        public void bindToActivityItem(UserActivityItem item){ //creates the views

            mUsernameLabel.setText(item.getUserName());
            mDescriptionLabel.setText(item.getDescription());
            mTimeLabel.setText(item.getTimeString());

            //profile image on the left
            Glide.with(mContext)
                    .load(item.getProfileImagePath())
                    .asBitmap()
                    .override(mActivityProfilePictureRadius, mActivityProfilePictureRadius) //change image to the size we want
                    .placeholder(R.drawable.profile_picture_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfileImage);

            //profile image on the right
            Glide.with(mContext)
                    .load(item.getEventImagePath())
                    .asBitmap()
                    .signature(new StringSignature(mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000")))
                    .override(mEventPictureSide, mEventPictureSide)
                    .placeholder(R.drawable.no_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(mEventImage);

        }
    }
}
