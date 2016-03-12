package com.linute.linute.MainContent.UpdateFragment;

import android.app.Activity;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SectionedRecyclerViewAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 1/6/16.
 */

public class UpdatesAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {

    //items in section one
    private List<Update> mRecentItems;

    //items in section two
    private List<Update> mOlderItems;

    private Context mContext;

    //private boolean mAutoLoad = true;


    //image width / radius

    public UpdatesAdapter(Context context, List<Update> recentItems, List<Update> olderItems) {
        mRecentItems = recentItems;
        mOlderItems = olderItems;
        mContext = context;
    }

    //private onLoadMoreListener mLoadMore;


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
            case 0: //if mRecentItems is empty, mOlderItems becomes section 0
                return mRecentItems.isEmpty() ? mOlderItems.size() : mRecentItems.size();
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
            tHolder.setTitleText(mRecentItems.isEmpty() ? "OLDER" : "RECENT");
        } else if (getSectionCount() == 2) { //both non-empty
            tHolder.setTitleText(section == 0 ? "RECENT" : "OLDER");
        }
    }

    @Override //non header views
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int section, int relativePosition, int absolutePosition) {



        if(absolutePosition == 0) {
            MainActivity activity = (MainActivity) mContext;
            if (activity != null) {
                activity.setUpdateNotification(0);
                activity.setNumNewActivities(0);
            }
        }

        UpdateItemViewHolder tHolder = (UpdateItemViewHolder) holder;
        if (holder == null) { //error
            Log.e("Updates adapter", "onBindViewHolder: problem binding s-" + section + " r-" + relativePosition + " a-" + absolutePosition);
            return;
        }

        if (section == 0) {
            tHolder.bindView(mRecentItems.isEmpty() ?
                    mOlderItems.get(relativePosition)
                    : mRecentItems.get(relativePosition));

        } else if (section == 1) { //section 1 will always mean old items
            tHolder.bindView(mOlderItems.get(relativePosition));
        } else { //invalid section
            Log.e("Update adapter", "onBindViewHolder: invalid section" + section);
        }
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {


        return super.getItemViewType(section, relativePosition, absolutePosition);
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
            return new UpdateItemViewHolder(
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

    public class UpdateItemViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView mProfileImage;
        private ImageView mIconImage;
        private ImageView mEventPicture;
        private TextView mNameText;
        private TextView mDescriptionText;

        private Update mUpdate;


        public UpdateItemViewHolder(View itemView) {
            super(itemView);

            mProfileImage = (CircleImageView) itemView.findViewById(R.id.updatesFragment_profile_picture);
            mEventPicture = (ImageView) itemView.findViewById(R.id.updatesFragment_update_picture);

            mIconImage = (ImageView) itemView.findViewById((R.id.updatesFragment_icon_image));
            mNameText = (TextView) itemView.findViewById(R.id.updatesFragment_name_text);
            mDescriptionText = (TextView) itemView.findViewById(R.id.updatesFragment_description);
        }

        //sets up view
        public void bindView(Update update) {
            mUpdate = update;

            mNameText.setText(update.isAnon() ? "Anonymous" : update.getUserFullName());
            mIconImage.setImageDrawable(getUpdateTypeIconDrawable(update.getUpdateType()));

            mDescriptionText.setText(update.getDescription());

            setUpPictures(update); //profile and event image

            if (!mUpdate.isAnon()) {
                setUpProfileOnClickListeners(update.getUserFullName(), update.getUserId());
            }

            setUpEventPictureTouchListener(update);
        }


        private void setUpPictures(Update update) {

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);


            //set profile image
            Glide.with(mContext)
                    .load(update.isAnon() ? ((update.getAnonImage() == null || update.getAnonImage().equals(""))
                            ? R.drawable.profile_picture_placeholder : Utils.getAnonImageUrl(update.getAnonImage()))
                            : Utils.getImageUrlOfUser(update.getUserProfileImageName()))

                    .asBitmap()
                    .placeholder(R.drawable.image_loading_background)
                    .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000"))) //so profile images update
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfileImage);


            Update.UpdateType type = update.getUpdateType();
            if (type == Update.UpdateType.UNDEFINED) return;

            //COMMENT or LIKE  - show image or post
            if (update.hasEventInformation()) {

                if (update.getEventImageName() == null || update.getEventImageName().equals("")) { //not a picture post; status post
                    Glide.with(mContext)
                            .load(R.drawable.quotation2)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(mEventPicture);
                } else { //picture post
                    //set event image
                    Glide.with(mContext)
                            .load(update.getEventImageName())
                            .asBitmap()
                            .placeholder(R.drawable.image_loading_background)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                            .into(mEventPicture);
                }

                if (mEventPicture.getVisibility() == View.GONE) {
                    mEventPicture.setVisibility(View.VISIBLE);
                }
            }

            //FOLLOWER or FRIEND JOIN - give option to follow back
            else if (update.hasFriendShipInformation()) {
                setUpFollowButton(update);
            }
        }

        private void setUpProfileOnClickListeners(final String name, final String id) {
            View.OnClickListener goToProfile = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mUpdate.isAnon()) {
                        //show profile fragment
                        ((MainActivity) mContext)
                                .addFragmentToContainer(
                                        TaptUserProfileFragment.newInstance(name, id)
                                );
                    }
                }
            };

            //clicking name or profile image takes person to user's profile
            mProfileImage.setOnClickListener(goToProfile);
            mNameText.setOnClickListener(goToProfile);
        }


        private void setUpEventPictureTouchListener(final Update update) {

            //if it was LIKE or COMMENT, it was either a status or photo
            //take them to the post
            if (update.hasEventInformation()) {
                mEventPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) mContext).addFragmentToContainer(
                                FeedDetailPage.newInstance(update.getPost())
                        );
                    }
                });
            }
        }


        private void setUpFollowButton(final Update update) {
            //you are not following person

            if (!update.getFollowedBack()) {

                mEventPicture.setImageResource(R.drawable.follow_back); //plus icon

                if (mEventPicture.getVisibility() == View.GONE) {
                    mEventPicture.setVisibility(View.VISIBLE);
                }

                mEventPicture.setOnClickListener(new View.OnClickListener() { //when pressed

                    boolean mFollowed = false; //if we are following other person

                    @Override
                    public void onClick(View v) {

                        if (mFollowed) return; //won't do anything if button pressed twice

                        mFollowed = true;
                        mEventPicture.setImageResource(R.drawable.done); //change icon
                        Map<String, Object> params = new HashMap<>();
                        params.put("user", update.getUserId());

                        final Activity activity = ((Activity) mContext);

                        new LSDKPeople(mContext).postFollow(params, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("UpdatesAdapter", "No internet connection");
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFollowed = false;
                                        Utils.showBadConnectionToast(activity);
                                        mEventPicture.setImageResource(R.drawable.follow_back);
                                        mUpdate.setFollowedBack(false);

                                    }
                                });

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                response.body().close();
                                if (!response.isSuccessful()) { //unsuccessful, undo button change
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFollowed = false;
                                            Utils.showServerErrorToast(activity);
                                            mEventPicture.setImageResource(R.drawable.follow_back);
                                            mUpdate.setFollowedBack(false);
                                        }
                                    });
                                } else {
                                    mUpdate.setFollowedBack(true);
                                }
                            }
                        });
                    }
                });
            }

            //are following person so hide button
            else {
                mEventPicture.setVisibility(View.GONE);
            }
        }

        //return small icon on top of the profile picture
        public Drawable getUpdateTypeIconDrawable(Update.UpdateType updateType) {
            int drawable;
            switch (updateType) {
                case LIKED_PHOTO:
                    drawable = R.drawable.icon_like;
                    break;
                case LIKED_STATUS:
                    drawable = R.drawable.icon_like;
                    break;
                case COMMENTED_PHOTO:
                    drawable = R.drawable.icon_comment;
                    break;
                case COMMENTED_STATUS:
                    drawable = R.drawable.icon_comment;
                    break;
                case FOLLOWER:
                    drawable = R.drawable.icon_user;
                    break;
                case MENTIONED:
                    drawable = R.drawable.icon_comment;
                    break;
                case FRIEND_JOINED:
                    drawable = R.drawable.icon_user;
                    break;
                case POSTED_PHOTO:
                    drawable = R.drawable.icon_comment;
                    break;
                case POSTED_STATUS:
                    drawable = R.drawable.icon_comment;
                    break;
                case ALSO_COMMENTED_IMAGE:
                    drawable = R.drawable.icon_comment;
                    break;
                case AlSO_COMMENTED_STATUS:
                    drawable = R.drawable.icon_comment;
                    break;
                default:
                    drawable = R.drawable.icon_user;
                    break;

            }
            return ContextCompat.getDrawable(mContext, drawable);
        }
    }



}
