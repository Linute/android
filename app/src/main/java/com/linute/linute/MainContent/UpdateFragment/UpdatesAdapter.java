package com.linute.linute.MainContent.UpdateFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.SectionedRecyclerViewAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private RequestManager mRequestManager;

    //load more
    private short mFooterState = LoadMoreViewHolder.STATE_LOADING;
    private LoadMoreViewHolder.OnLoadMore mOnLoadMore;

    public UpdatesAdapter(Context context, List<Update> recentItems, List<Update> olderItems) {
        mRecentItems = recentItems;
        mOlderItems = olderItems;
        mContext = context;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
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
                if (!mRecentItems.isEmpty()){
                    //if olderItems is not empty, the load more is below recentItems
                    //else the load more is under the olderItems
                    return mOlderItems.isEmpty() ? mRecentItems.size() + 1 : mRecentItems.size();
                }else {
                    return mOlderItems.isEmpty() ? 0 : mOlderItems.size() + 1;
                }
            case 1:
                //section 1 means there was a section before
                //loader must be in this section
                return mOlderItems.size() + 1;
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

        if (holder instanceof UpdateItemViewHolder) {
            UpdateItemViewHolder tHolder = (UpdateItemViewHolder) holder;
            if (section == 0) {
                tHolder.bindView(mRecentItems.isEmpty() ?
                        mOlderItems.get(relativePosition)
                        : mRecentItems.get(relativePosition));
            } else if (section == 1) { //section 1 will always mean old items
                tHolder.bindView(mOlderItems.get(relativePosition));
            } else { //invalid section
                Log.e("Update adapter", "onBindViewHolder: invalid section" + section);
            }
        }else if (holder instanceof LoadMoreViewHolder){
            ((LoadMoreViewHolder) holder).bindView(mFooterState);
            if (mOnLoadMore != null) mOnLoadMore.loadMore();
        }
    }

    @Override
    public int getItemViewType(int section, int relativePosition, int absolutePosition) {

        if (absolutePosition == mRecentItems.size() + mOlderItems.size()){
            return LoadMoreViewHolder.FOOTER;
        }

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
        } else if (viewType == LoadMoreViewHolder.FOOTER){
            return new LoadMoreViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.wrapping_footer_dark, parent, false), "","");
        }

        return null;
    }

    public void setFooterState(short state){
        mFooterState = state;
    }

    public void setOnLoadMore(LoadMoreViewHolder.OnLoadMore l){
        mOnLoadMore = l;
    }

    public short getFooterState(){
        return mFooterState;
    }


    public class UpdateItemHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleText;

        public UpdateItemHeaderViewHolder(View itemView) {
            super(itemView);
            mTitleText = (TextView) itemView.findViewById(R.id.updateFragment_section_title);
        }

        public void setTitleText(String title) {
            mTitleText.setText(title);
        }
    }


    public class UpdateItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mProfileImage;
        private ImageView mIconImage;
        private ImageView mEventPicture;
        private TextView mNameText;
        private TextView mDescriptionText;
        private TextView mTimeView;

        private Update mUpdate;


        public UpdateItemViewHolder(View itemView) {
            super(itemView);

            mProfileImage = (ImageView) itemView.findViewById(R.id.updatesFragment_profile_picture);
            mEventPicture = (ImageView) itemView.findViewById(R.id.updatesFragment_update_picture);

            mIconImage = (ImageView) itemView.findViewById((R.id.updatesFragment_icon_image));
            mNameText = (TextView) itemView.findViewById(R.id.updatesFragment_name_text);
            mDescriptionText = (TextView) itemView.findViewById(R.id.updatesFragment_description);
            mTimeView = (TextView) itemView.findViewById(R.id.time);

            //clicking name or profile image takes person to user's profile
            mProfileImage.setOnClickListener(this);
            mNameText.setOnClickListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUpdate != null) {
                        if (mUpdate.getUpdateType() == Update.UpdateType.FOLLOWER){
                            ((MainActivity) mContext).addFragmentToContainer(
                                    TaptUserProfileFragment.newInstance(mUpdate.getUserFullName(), mUpdate.getUserId())
                            );
                        }else if (mUpdate.getPost() != null) {
                            ((MainActivity) mContext).addFragmentToContainer(
                                    FeedDetailPage.newInstance(mUpdate.getPost())
                            );
                        }
                        if(!mUpdate.isViewed()){
                            sendReadAsync(mUpdate.getActionID());
                            mUpdate.markViewed();
                        }
                    }
                }
            });
        }

        //sets up view
        public void bindView(Update update) {
            mUpdate = update;

            mNameText.setText(update.isAnon() ? "Anonymous" : update.getUserFullName());
            mIconImage.setImageResource(getUpdateTypeIcon(update.getUpdateType()));

            mDescriptionText.setText(update.getDescription());
            mTimeView.setText(Utils.getTimeAgoString(update.getActionTime()));

            itemView.setBackgroundColor(update.isViewed() ? 0 : 0x2284CFDF);

            setUpPictures(update); //profile and event image
        }


        private void setUpPictures(final Update update) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

            //set profile image
            mRequestManager
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
                    mRequestManager
                            .load(R.drawable.quotation2)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(mEventPicture);
                } else { //picture post
                    //set event image
                    mRequestManager
                            .load(update.getEventImageName())
                            .dontAnimate()
                            .placeholder(R.drawable.image_loading_background)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                            .into(mEventPicture);
                }

                mEventPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) mContext).addFragmentToContainer(
                                FeedDetailPage.newInstance(mUpdate.getPost())
                        );
                        update.markViewed();
                    }
                });

                if (mEventPicture.getVisibility() == View.INVISIBLE) {
                    mEventPicture.setVisibility(View.VISIBLE);
                }
            }

            //FOLLOWER or FRIEND JOIN - give option to follow back
            else if (update.hasFriendShipInformation()) {
                setUpFollowButton(update.getFollowedBack());
            }else {
                mEventPicture.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (!mUpdate.isAnon()) {
                //show profile fragment
                ((MainActivity) mContext)
                        .addFragmentToContainer(
                                TaptUserProfileFragment.newInstance(mUpdate.getUserFullName(), mUpdate.getUserId()));
            }

        }

        private void sendReadAsync(final String id) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                        JSONObject obj = new JSONObject();
                        try {
                            JSONArray activities = new JSONArray();
                            activities.put(id);
                            obj.put("activities", activities);
                            TaptSocket.getInstance().emit(API_Methods.VERSION + ":activities:viewed", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            });
        }



        private void setUpFollowButton(boolean followedBack) {
            //you are not following person
            if (!followedBack) {
                mEventPicture.setImageResource(R.drawable.follow_back); //plus icon
                if (mEventPicture.getVisibility() == View.INVISIBLE) {
                    mEventPicture.setVisibility(View.VISIBLE);
                }

                mEventPicture.setOnClickListener(new View.OnClickListener() { //when pressed
                    boolean mFollowed = false; //if we are following other person

                    @Override
                    public void onClick(View v) {

                        if (mFollowed) return; //won't do anything if button pressed twice

                        mFollowed = true;
                        mEventPicture.setVisibility(View.INVISIBLE);
                        Map<String, Object> params = new HashMap<>();
                        params.put("user", mUpdate.getUserId());

                        new LSDKPeople(mContext).postFollow(params, new Callback() {
                            final String temp = mUpdate.getActionID();

                            @Override
                            public void onFailure(Call call, IOException e) {
                                mFollowed = false;

                                final MainActivity activity = ((MainActivity) mContext);
                                if (activity == null) return;

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showBadConnectionToast(activity);

                                        if (temp.equals(mUpdate.getActionID())) {
                                            mEventPicture.setImageResource(R.drawable.follow_back);
                                            mUpdate.setFollowedBack(false);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                response.body().close();

                                final MainActivity activity  = (MainActivity) mContext;
                                if (!response.isSuccessful()) { //unsuccessful, undo button change
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFollowed = false;
                                            if (activity != null) {
                                                Utils.showServerErrorToast(activity);

                                                if (temp.equals(mUpdate.getActionID())) {
                                                    mEventPicture.setImageResource(R.drawable.follow_back);
                                                    mUpdate.setFollowedBack(false);
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    if (temp.equals(mUpdate.getActionID())) mUpdate.setFollowedBack(true);
                                }
                            }
                        });
                    }
                });

            }
            //are following person so hide button
            else {
                mEventPicture.setVisibility(View.INVISIBLE);
            }
        }

        //return small icon on top of the profile picture
        public int getUpdateTypeIcon(Update.UpdateType updateType) {
            int drawable;
            switch (updateType) {
                case LIKED_PHOTO:
                    drawable = R.drawable.fire_icon_updates;
                    break;
                case LIKED_STATUS:
                    drawable = R.drawable.fire_icon_updates;
                    break;
                case LIKED_VIDEO:
                    drawable = R.drawable.fire_icon_updates;
                    break;
                case COMMENTED_PHOTO:
                    drawable = R.drawable.icon_comment;
                    break;
                case COMMENTED_STATUS:
                    drawable = R.drawable.icon_comment;
                    break;
                case COMMENTED_VIDEO:
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
                case FRIEND_POSTED_PHOTO:
                    drawable = R.drawable.icon_comment;
                    break;
                case FRIEND_POSTED_STATUS:
                    drawable = R.drawable.icon_comment;
                    break;
                case FRIEND_POSTED_VIDEO:
                    drawable = R.drawable.icon_comment;
                    break;
                case ALSO_COMMENTED_IMAGE:
                    drawable = R.drawable.icon_comment;
                    break;
                case AlSO_COMMENTED_STATUS:
                    drawable = R.drawable.icon_comment;
                    break;
                case ALSO_COMMENTED_VIDEO:
                    drawable = R.drawable.icon_comment;
                    break;
                case LIKED_COMMENT:
                    drawable = R.drawable.fire_icon_updates;
                    break;
                default:
                    drawable = R.drawable.icon_user;
                    break;

            }
            return drawable;
        }
    }
}
