package com.linute.linute.MainContent.UpdateFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SectionedRecyclerViewAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

/**
 * Created by QiFeng on 1/6/16.
 */

public class UpdatesAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {

    //items in section one
    private List<Update> mRecentItems;

    //items in section two
    private List<Update> mOlderItems;

    private Context mContext;

    public UpdatesAdapter(Context context, List<Update> recentItems, List<Update> olderItems) {
        mRecentItems = recentItems;
        mOlderItems = olderItems;
        mContext = context;
    }


    @Override
    public int getSectionCount() {
        //if both empty, 0
        //one empty, 1
        //both non-empty, 2
        return (mRecentItems.isEmpty() ? 0 : 1) + (mOlderItems.isEmpty() ? 0 : 1);
    }

    @Override
    public int getItemCount(int section) {
        switch (section) {
            case 0:
                return mRecentItems.size();
            case 1:
                return mOlderItems.size();
            default:
                return 0;
        }
    }

    @Override //header view
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int section) {
        UpdateItemHeaderViewHolder tHolder = (UpdateItemHeaderViewHolder) holder;
        if (holder == null) return;

        if (getSectionCount() == 1) { //either recent or older is empty
            if (mRecentItems.isEmpty())  //older is not empty
                tHolder.setTitleText("OLDER");
            else //recent is not empty
                tHolder.setTitleText("RECENT");
        } else if (getSectionCount() == 2) { //both non-empty
            if (section == 0) //recent
                tHolder.setTitleText("RECENT");
            else //older
                tHolder.setTitleText("OLDER");
        }
    }

    @Override //non header views
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int section, int relativePosition, int absolutePosition) {
        UpdateItemViewHolder tHolder = (UpdateItemViewHolder) holder;

        if (holder == null) { //error
            Log.e("Updates adapter", "onBindViewHolder: problem binding s-" + section + " r-" + relativePosition + " a-" + absolutePosition);
            return;
        }

        if (section == 0) { //section 0 means recent items
            tHolder.bindView(mRecentItems.get(relativePosition));
        } else if (section == 1) { //section 1 means old items
            tHolder.bindView(mOlderItems.get(relativePosition));
        } else { //invalid section
            Log.e("Update adapter", "onBindViewHolder: invalid section" + section);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) { //use header holder
            return new UpdateItemHeaderViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.fragment_updates_section_header, parent, false)
            );
        } else if (viewType == VIEW_TYPE_ITEM) { //use item holder
            return new UpdateItemViewHolder(mContext,
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.fragment_updates_item_cells, parent, false)
            );
        }
        return null;
    }


    public static class UpdateItemHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleText;

        public UpdateItemHeaderViewHolder(View itemView) {
            super(itemView);
            mTitleText = (TextView) itemView.findViewById(R.id.updateFragment_section_title);
        }

        public void setTitleText(String title) {
            mTitleText.setText(title);
        }
    }

    public static class UpdateItemViewHolder extends RecyclerView.ViewHolder {

        private CircularImageView mProfileImage;
        private ImageView mIconImage;
        private ImageView mEventPicture;
        private TextView mNameText;
        private TextView mDescriptionText;

        private Context mContext;


        public UpdateItemViewHolder(Context context, View itemView) {
            super(itemView);
            mContext = context;

            mProfileImage = (CircularImageView) itemView.findViewById(R.id.updatesFragment_profile_picture);
            mEventPicture = (ImageView) itemView.findViewById(R.id.updatesFragment_update_picture);

            mIconImage = (ImageView) itemView.findViewById((R.id.updatesFragment_icon_image));
            mNameText = (TextView) itemView.findViewById(R.id.updatesFragment_name_text);
            mDescriptionText = (TextView) itemView.findViewById(R.id.updatesFragment_description);
        }

        //sets up view
        public void bindView(Update update) {
            mNameText.setText(update.getUserFullName());
            mIconImage.setImageDrawable(getUpdateTypeIconDrawable(update.getUpdateType()));
            mDescriptionText.setText(update.getDescription());

            setUpPictures(update);
            setUpOnClickListeners(update);
        }


        private void setUpPictures(Update update) {
            //image width / radius
            int imageSize = mContext.getResources().getDimensionPixelSize(R.dimen.updatefragment_picture_size);

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

            //set profile image
            Glide.with(mContext)
                    .load(Utils.getImageUrlOfUser(update.getUserProfileImageName()))
                    .asBitmap()
                    .override(imageSize, imageSize)
                    .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000"))) //so profile images update
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfileImage);


            Update.UpdateType type = update.getUpdateType();
            if (type == Update.UpdateType.UNDEFINED) return;

            if (type == Update.UpdateType.COMMENT || type == Update.UpdateType.LIKE || type == Update.UpdateType.MENTION) {
                //there is an event picture

                String imageName = update.getEventImageName();
                if (imageName != null && !imageName.equals(""))
                    //set event image
                    Glide.with(mContext)
                            .load(Utils.getEventImageURL(update.getEventImageName()))
                            .asBitmap()
                            .override(imageSize, imageSize)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                            .into(mEventPicture);


                else  //no event picture - so it's a status post
                    mEventPicture.setImageResource(R.drawable.quotation2);

            } else if (type == Update.UpdateType.FOLLOW || type == Update.UpdateType.FRIEND_JOIN) {
                setUpFollowPicture();
            }
        }

        private void setUpOnClickListeners(final Update update){

            View.OnClickListener goToProfile = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: go to profile page
                    Toast.makeText(mContext, "go to profile: " + update.getUserId(), Toast.LENGTH_SHORT).show();
                }
            };

            //clicking name or profile image takes person to user's profile
            mProfileImage.setOnClickListener(goToProfile);
            mNameText.setOnClickListener(goToProfile);


            setUpEventPicture(update);
        }


        private void setUpEventPicture(final Update update){
            Update.UpdateType type = update.getUpdateType();

            //if it was LIKE or COMMENT, it was either a status or photo
            //take them to the post
            if (type == Update.UpdateType.LIKE || type == Update.UpdateType.COMMENT){
                mEventPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: go to event
                        Toast.makeText(mContext, "Go to event "+update.getEventID(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //TODO: MENTION -- go to mentioned post
        }

        private void setUpFollowPicture(){
            //TODO : determine if following and set appropriate picture
            mEventPicture.setImageResource(R.drawable.follow_back);
        }


        //return small icon on top of the profile picture
        public Drawable getUpdateTypeIconDrawable(Update.UpdateType updateType) {
            int drawable;
            switch (updateType) {
                case LIKE:
                    drawable = R.drawable.icon_like;
                    break;
                case COMMENT:
                    drawable = R.drawable.icon_comment;
                    break;
                case FOLLOW:
                    drawable = R.drawable.icon_user;
                    break;
                case MENTION:
                    drawable = R.drawable.icon_user; //TODO: NEED ICON
                    break;
                case FRIEND_JOIN:
                    drawable = R.drawable.icon_user; //TODO: NEED ICON
                    break;
                case FACEBOOK_SHARE:
                    drawable = R.drawable.icon_user; //TODO: NEED ICON
                    break;
                default:
                    drawable = R.drawable.icon_user;
                    break;

            }
            return ContextCompat.getDrawable(mContext, drawable);
        }
    }

}
