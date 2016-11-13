package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.Chat.ChatFragment;
import com.linute.linute.MainContent.DiscoverFragment.ImageFeedHolder;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.StatusFeedHolder;
import com.linute.linute.MainContent.DiscoverFragment.VideoFeedHolder;
import com.linute.linute.MainContent.FriendsList.FriendsListHolder;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.Settings.EditProfileInfoActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.ImpressionHelper;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = ProfileAdapter.class.getSimpleName();

    private static final int TYPE_HEADER_IMAGE = 0;
    private static final int TYPE_HEADER_ACTIONS = 4;
    private static final int TYPE_ITEM_VIDEO = 1;
    private static final int TYPE_ITEM_IMAGE = 5;
    private static final int TYPE_EMPTY = 2;
    private static final int TYPE_ITEM_STATUS = 3;

    private static final int TYPE_ITEM_IMAGE_SMALL = 6;
    private static final int TYPE_ITEM_VIDEO_SMALL = 7;
    private static final int TYPE_ITEM_STATUS_SMALL = 8;

    private Context context;
    private ArrayList<Post> mPosts = new ArrayList<>();
    private LinuteUser mUser;
    private RequestManager mRequestManager;

    private OnClickFollow mOnClickFollow;

    public boolean showThumbnails = false;

    private String mUserid;
    private String mCollegeId;
    private short mLoadState = LoadMoreViewHolder.STATE_LOADING;

    private OnSwitchLayoutClicked mOnSwitchLayoutClicked;
    private BaseFeedAdapter.PostAction mPostAction;
    private View.OnClickListener mOnClickMore;
    private LoadMoreViewHolder.OnLoadMore mLoadMorePosts;


    public ProfileAdapter(ArrayList<Post> posts, LinuteUser user, Context context) {
        this.context = context;
        SharedPreferences preferences = context
                .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mUserid = preferences.getString("userID", "");
        mCollegeId = preferences.getString("collegeId", "");
        mPosts = posts;
        mUser = user;
    }

    public void setOnSwitchLayoutClicked(OnSwitchLayoutClicked onSwitchLayoutClicked) {
        mOnSwitchLayoutClicked = onSwitchLayoutClicked;
    }

    public void setOnClickMore(View.OnClickListener onClickMore) {
        mOnClickMore = onClickMore;
    }

    public void setPostAction(BaseFeedAdapter.PostAction action) {
        mPostAction = action;
    }

    public void setRequestManager(RequestManager manager) {
        mRequestManager = manager;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_ITEM_IMAGE:
                ImageFeedHolder iHolder = new ImageFeedHolder(
                        inflater.inflate(R.layout.feed_detail_header_image, parent, false),
                        context,
                        mRequestManager,
                        mPostAction
                );

                iHolder.setEnableProfileView(false);
                return iHolder;
            case TYPE_ITEM_IMAGE_SMALL:
                return new ProfileViewHolder(inflater.inflate(R.layout.profile_grid_item_2, parent, false), context);

            case TYPE_ITEM_VIDEO:
                VideoFeedHolder vHolder = new VideoFeedHolder(
                        inflater.inflate(R.layout.feed_detail_header_video, parent, false),
                        context,
                        mRequestManager,
                        mPostAction
                );
                vHolder.setEnableProfileView(false);
                return vHolder;

            case TYPE_ITEM_VIDEO_SMALL:
                return new ProfileViewHolder(inflater.inflate(R.layout.profile_grid_item_2, parent, false), context);

            case TYPE_ITEM_STATUS:
                StatusFeedHolder sHolder = new StatusFeedHolder(
                        inflater.inflate(R.layout.feed_detail_header_status, parent, false),
                        context,
                        mRequestManager,
                        mPostAction
                );
                sHolder.setEnableProfileView(false);
                return sHolder;

            case TYPE_ITEM_STATUS_SMALL:
                return new ProfileViewHolderNoImage(inflater.inflate(R.layout.profile_grid_item_no_image, parent, false), context);

            case TYPE_HEADER_IMAGE:
                return new ProfileHeaderViewHolder(inflater
                        .inflate(R.layout.fragment_profile_header, parent, false));

            case TYPE_HEADER_ACTIONS:
                return new ProfileHeaderActions(inflater
                        .inflate(R.layout.fragment_profile_header_part_2, parent, false));

            case TYPE_EMPTY:
                return new EmptyProfileHolder(inflater
                        .inflate(R.layout.empty_cell_holders, parent, false), mUser.getUserID().equals(mUserid));

            case LoadMoreViewHolder.FOOTER:
                return new LoadMoreViewHolder(inflater
                        .inflate(R.layout.wrapping_footer_dark, parent, false), "", "");

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position == mPosts.size() + 2) { //on last elem, need to load more
            if (mLoadMorePosts != null)
                mLoadMorePosts.loadMore();
        }

        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mLoadState);
        } else if (holder instanceof VideoFeedHolder) {
            Post p = mPosts.get(position - 2);
            ((VideoFeedHolder) holder).bindModel(p);
            ImpressionHelper.sendImpressionsAsync(mCollegeId, mUserid, p.getId());
        } else if (holder instanceof ImageFeedHolder) {
            Post p = mPosts.get(position - 2);
            ((ImageFeedHolder) holder).bindModel(p);
            ImpressionHelper.sendImpressionsAsync(mCollegeId, mUserid, p.getId());
        } else if (holder instanceof ProfileViewHolder) {
            ((ProfileViewHolder) holder).bindModel(mPosts.get(position - 2));
        } else if (holder instanceof ProfileHeaderViewHolder) {
            ((ProfileHeaderViewHolder) holder).bindModel(mUser);
        } else if (holder instanceof ProfileHeaderActions) {
            ((ProfileHeaderActions) holder).bindView(showThumbnails ? 0 : 1);
        } else if (holder instanceof StatusFeedHolder) {
            Post p = mPosts.get(position - 2);
            ((StatusFeedHolder) holder).bindModel(p);
            ImpressionHelper.sendImpressionsAsync(mCollegeId, mUserid, p.getId());
        } else if (holder instanceof ProfileViewHolderNoImage) {
            Post p = mPosts.get(position - 2);
            ((ProfileViewHolderNoImage) holder).bindModel(p);
            ImpressionHelper.sendImpressionsAsync(mCollegeId, mUserid, p.getId());
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size() + 3; //2 for header and 1 for loadmore footer
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER_IMAGE;
        else if (position == 1)
            return TYPE_HEADER_ACTIONS;
        else if (position == mPosts.size() + 2)
            return LoadMoreViewHolder.FOOTER;
        else if (mPosts.get(position - 2) == null)
            return TYPE_EMPTY;
        else if (mPosts.get(position - 2).isImagePost()) {
            if (showThumbnails) {
                if (mPosts.get(position - 2).isVideoPost())
                    return TYPE_ITEM_VIDEO_SMALL;
                return TYPE_ITEM_IMAGE_SMALL;
            }

            if (mPosts.get(position - 2).isVideoPost()) return TYPE_ITEM_VIDEO;
            else return TYPE_ITEM_IMAGE;
        }
        return showThumbnails ? TYPE_ITEM_STATUS_SMALL : TYPE_ITEM_STATUS;
    }


    public void setLoadMorePosts(LoadMoreViewHolder.OnLoadMore loadMorePosts) {
        mLoadMorePosts = loadMorePosts;
    }


    public void setOnClickFollow(OnClickFollow follow) {
        mOnClickFollow = follow;
    }

    public void setLoadState(short state) {
        mLoadState = state;
    }

    public class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {

        ImageView vProfilePicture;
        View vMessageButton;
        View vMoreButton;
        TextView vUserName;
        TextView vCollegeName;
        TextView vStatusText;

        TextView vPosts;
        TextView vFollowers;

        Button vFollowButton;

        public String mUserId;


        public ProfileHeaderViewHolder(View itemView) {
            super(itemView);
            vProfilePicture = (ImageView) itemView.findViewById(R.id.profile_image);
            vMessageButton = itemView.findViewById(R.id.message);
            vMoreButton = itemView.findViewById(R.id.more);
            vUserName = (TextView) itemView.findViewById(R.id.name);
            vCollegeName = (TextView) itemView.findViewById(R.id.college);
            vStatusText = (TextView) itemView.findViewById(R.id.bio);

            View followerContainer = itemView.findViewById(R.id.follow_container);
            vFollowers = (TextView) itemView.findViewById(R.id.followers);
            vPosts = (TextView) itemView.findViewById(R.id.posts);

            mUserId = Utils.getMyId(context);

            vFollowButton = (Button) itemView.findViewById(R.id.button);

            vMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mUser == null || mUser.getUserID() == null || mUser.getUserID().equals(mUserid))
                        return;

                    BaseTaptActivity activity = (BaseTaptActivity) context;
                    if (activity != null) {
                        activity.addFragmentToContainer(ChatFragment
                                .newInstance(
                                        null,
                                        mUser.getFirstName(),
                                        mUser.getLastName(),
                                        mUser.getUserID()));
                    }
                }
            });

            vFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mUser.getUserID() == null || !mUser.isInformationLoaded()) return;

                    if (!mUserid.equals(mUser.getUserID())) {
                        if (mOnClickFollow == null) return;

                        mOnClickFollow.followUser(vFollowButton, mUser, mUser.getFriend().equals(""));
                    } else { //user viewing own profile
                        MainActivity activity = (MainActivity) context;
                        if (activity != null) {
                            activity.startEditProfileActivity(EditProfileInfoActivity.class);
                        }
                    }
                }
            });

            vMoreButton.setOnClickListener(mOnClickMore);

            followerContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseTaptActivity activity = (BaseTaptActivity) context;
                    if (activity != null && mUser.isInformationLoaded()) {
                        activity.addFragmentToContainer(FriendsListHolder.newInstance(mUser.getUserID()));
                    }
                }
            });

        }

        void bindModel(LinuteUser user) {

            mRequestManager
                    .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                    .dontAnimate()
                    .signature(new StringSignature(context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000")))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vProfilePicture);

            String full = mUser.getFirstName() + " " + mUser.getLastName();
            vUserName.setText(full);

            if (mUserId.equals(user.getUserID())){
                vMoreButton.setVisibility(View.INVISIBLE);
                vMessageButton.setVisibility(View.INVISIBLE);
                vFollowButton.setText("Edit profile");

            }else {
                if (!user.isInformationLoaded()){
                    vFollowButton.setText("Loading");
                    vMoreButton.setVisibility(View.INVISIBLE);
                    vMessageButton.setVisibility(View.INVISIBLE);
                }else {
                    vMoreButton.setVisibility(View.VISIBLE);
                    vMessageButton.setVisibility(View.VISIBLE);
                    if (mUser.getFriend() == null || mUser.getFriend().equals("")) {
                        vFollowButton.setText("follow");
                    } else {
                        vFollowButton.setText("following");
                    }
                }
            }


            if (mUser.isInformationLoaded()){
                if (mUser.getStatus() != null)
                    vStatusText.setText(mUser.getStatus().equals("") ? "No bio... :|" : mUser.getStatus());
                vPosts.setText(String.valueOf(mUser.getPosts()));
                vFollowers.setText(String.valueOf(mUser.getFollowers()));

                vCollegeName.setVisibility(mUser.getIsCompany() ? View.GONE : View.VISIBLE);
                vCollegeName.setText(mUser.getCollegeName());


            }
        }
    }

    public class ProfileHeaderActions extends RecyclerView.ViewHolder implements View.OnClickListener {

        View mGridParent;
        View mLinearParent;

        final float selected = 1f;
        final float unselected = 0.3f;

        public ProfileHeaderActions(View itemView) {
            super(itemView);
            mGridParent = itemView.findViewById(R.id.grid);
            mLinearParent = itemView.findViewById(R.id.linear);


            mGridParent.setOnClickListener(this);
            mLinearParent.setOnClickListener(this);

        }

        public void bindView(int position) {
            if (position == 0){
                mGridParent.setAlpha(selected);
                mLinearParent.setAlpha(unselected);
            }else {
                mGridParent.setAlpha(unselected);
                mLinearParent.setAlpha(selected);
            }
        }

        @Override
        public void onClick(View view) {
            if (mOnSwitchLayoutClicked == null) return;
            if (view == mGridParent){
                mOnSwitchLayoutClicked.switchClicked(0);
                mGridParent.setAlpha(selected);
                mLinearParent.setAlpha(unselected);
            }else if (view == mLinearParent){
                mOnSwitchLayoutClicked.switchClicked(1);
                mGridParent.setAlpha(unselected);
                mLinearParent.setAlpha(selected);
            }
        }

    }


    public interface OnClickFollow {
        void followUser(TextView followingText, LinuteUser user, boolean follow);
    }

    public interface OnSwitchLayoutClicked{
        void switchClicked(int position);
    }

}
