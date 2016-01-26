package com.linute.linute.MainContent.UpdateFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by QiFeng on 1/6/16.
 */

public class UpdatesAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {

    //items in section one
    private List<Update> mRecentItems;

    //items in section two
    private List<Update> mOlderItems;

    private Context mContext;

    private boolean mAutoLoad = true;


    //image width / radius
    private int mImageSize;

    public UpdatesAdapter(Context context, List<Update> recentItems, List<Update> olderItems) {
        mRecentItems = recentItems;
        mOlderItems = olderItems;
        mContext = context;
        mImageSize = mContext.getResources().getDimensionPixelSize(R.dimen.updatefragment_picture_size);
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

    public void setAutoLoadMore(boolean autoLoad) {
        mAutoLoad = autoLoad;
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

        //NOTE: Below is the code for loading more

        /*if (holder instanceof UpdatesViewHolderFooter){
            final UpdatesViewHolderFooter footer = (UpdatesViewHolderFooter) holder;
            if (mAutoLoad) {
                Log.i("test", "onBindViewHolder: ");
                mLoadMore.loadMore();
            }
            else {
                footer.showButton(true);
                footer.setButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        footer.showButton(false);
                        mLoadMore.loadMore();
                    }
                });
            }
            return;
        }*/

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

    //private static int VIEW_FOOTER = 1;

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {

        //NOTE: Below is the code for loading more

        /*
        if(!mRecentItems.isEmpty() && mOlderItems.isEmpty()){
            if (mRecentItems.get(relativePosition) == null) return VIEW_FOOTER;
        }else if (mRecentItems.isEmpty() && !mOlderItems.isEmpty()){
            if (mOlderItems.get(relativePosition) == null) return VIEW_FOOTER;
        }else if (section == 1){
            if (mOlderItems.get(relativePosition) == null) return VIEW_FOOTER;
        }*/

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


        //NOTE: Below is the code for load more
        /*
        else if (viewType == VIEW_FOOTER){
            return new UpdatesViewHolderFooter(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.fragment_updates_footer, parent, false)
            );
        }*/
        return null;
    }

    /*
    public void setOnLoadMoreListener(onLoadMoreListener listener){
        mLoadMore = listener;
    }*/


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

        private CircularImageView mProfileImage;
        private ImageView mIconImage;
        private ImageView mEventPicture;
        private TextView mNameText;
        private TextView mDescriptionText;


        public UpdateItemViewHolder(View itemView) {
            super(itemView);

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

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

            //set profile image
            Glide.with(mContext)
                    .load(Utils.getImageUrlOfUser(update.getUserProfileImageName()))
                    .asBitmap()
                    .override(mImageSize, mImageSize)
                    .signature(new StringSignature(sharedPreferences.getString("imageSigniture", "000"))) //so profile images update
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfileImage);


            Update.UpdateType type = update.getUpdateType();
            if (type == Update.UpdateType.UNDEFINED) return;

            //COMMENT or LIKE  - show image or post
            if (update.hasEventInformation()) {

                if (!update.isPicturePost()) { //not a picture post; status post
                    Glide.with(mContext)
                            .load(R.drawable.quotation2)
                            .override(mImageSize, mImageSize)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(mEventPicture);
                } else { //picture post
                    //set event image
                    Glide.with(mContext)
                            .load(Utils.getEventImageURL(update.getEventImageName()))
                            .asBitmap()
                            .override(mImageSize, mImageSize)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                            .into(mEventPicture);
                }

                //NOTE: THIS WAS DRIVING ME NUTS.
                //NOTE: When you scrolled up and down, some images would disappear
                //NOTE: I made sure i wasn't setting it to gone
                //NOTE: YET they'd set themselves to gone
                //NOTE: The only solution I thought of was to reset it to visible..
                if (mEventPicture.getVisibility() == View.GONE) {
                    mEventPicture.setVisibility(View.VISIBLE);
                }
            }

            //FOLLOWER or FRIEND JOIN - give option to follow back
            else if (update.hasFriendShipInformation()) {
                setUpFollowButton(update);
            }
        }

        private void setUpOnClickListeners(final Update update) {
            View.OnClickListener goToProfile = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //show profile fragment
                    ((MainActivity)mContext)
                            .addFragmentToContainer(
                                    TaptUserProfileFragment.newInstance(update.getUserFullName(), update.getUserId())
                            );
                }
            };

            //clicking name or profile image takes person to user's profile
            mProfileImage.setOnClickListener(goToProfile);
            mNameText.setOnClickListener(goToProfile);


            setUpEventPictureTouchListener(update);
        }


        private void setUpEventPictureTouchListener(final Update update) {

            //if it was LIKE or COMMENT, it was either a status or photo
            //take them to the post
            if (update.hasEventInformation()) {
                mEventPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)mContext).addFragmentToContainer(
                                FeedDetailPage.newInstance(
                                        update.isPicturePost()
                                        ,update.getEventID()
                                        ,update.getUserId()
                                ));
                    }
                });

            }
        }


        private void setUpFollowButton(final Update update) {
            //you are not following person

            if (!update.getFollowedBack()) {

                mEventPicture.setImageResource(R.drawable.follow_back); //plus icon

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
                            public void onFailure(Request request, IOException e) {
                                Log.e("UpdatesAdapter", "No internet connection");

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFollowed = false;
                                        Utils.showBadConnectionToast(activity);
                                        mEventPicture.setImageResource(R.drawable.follow_back);
                                    }
                                });

                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                Log.i("Update Adapter", "onResponse: " + response.body().string());
                                if (!response.isSuccessful()) { //unsuccessful, undo button change
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFollowed = false;
                                            Utils.showServerErrorToast(activity);
                                            mEventPicture.setImageResource(R.drawable.follow_back);
                                        }
                                    });
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
                    drawable = R.drawable.icon_user; //TODO: NEED ICON
                    break;
                case FRIEND_JOINED:
                    drawable = R.drawable.icon_user; //TODO: NEED ICON
                    break;
                case POSTED_PHOTO:
                    drawable = R.drawable.icon_comment; //TODO: NEED ICON
                    break;
                case POSTED_STATUS:
                    drawable = R.drawable.icon_comment; //TODO: NEED ICON
                    break;
                default:
                    drawable = R.drawable.icon_user;
                    break;

            }
            return ContextCompat.getDrawable(mContext, drawable);
        }
    }

//    public static class UpdatesViewHolderFooter extends RecyclerView.ViewHolder {
//
//        private Button mRetryButton;
//        private ProgressBar mProgressBar;
//
//        public UpdatesViewHolderFooter(View itemView) {
//            super(itemView);
//            mRetryButton =  (Button) itemView.findViewById(R.id.updatesFragment_reload_button);
//            mProgressBar = (ProgressBar) itemView.findViewById(R.id.updateFragment_progress_bar);
//        }
//
//        public void showButton(boolean show){
//            mRetryButton.setVisibility(show? View.VISIBLE : View.GONE);
//            mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//
//        public void setButtonListener(View.OnClickListener lis){
//            mRetryButton.setOnClickListener(lis);
//        }
//
//    }
//
//    public interface onLoadMoreListener{
//        public void loadMore();
//    }

}
