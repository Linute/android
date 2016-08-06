package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
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
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.MainContent.Chat.ChatFragment;
import com.linute.linute.MainContent.FriendsList.FriendsListHolder;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.Settings.EditProfileInfoActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 12/30/15.
 */
public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = ProfileAdapter.class.getSimpleName();

    private static final int TYPE_HEADER_IMAGE = 0;
    private static final int TYPE_HEADER_ACTIONS = 4;
    private static final int TYPE_ITEM_WITH_IMAGE = 1;
    private static final int TYPE_EMPTY = 2;
    private static final int TYPE_ITEM_WITHOUT_IMAGE = 3;

    private Context context;
    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();
    private LinuteUser mUser;
    private RequestManager mRequestManager;

    private String mUserid;
    private short mLoadState = LoadMoreViewHolder.STATE_LOADING;


    private LoadMoreViewHolder.OnLoadMore mLoadMorePosts;

    private TitleTextListener mTitleTextListener;


    public ProfileAdapter(ArrayList<UserActivityItem> userActivityItems, LinuteUser user,Context context) {
        this.context = context;
        mUserid = context
                .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString("userID", "");
        mUserActivityItems = userActivityItems;
        mUser = user;
    }

    public void setRequestManager(RequestManager manager){
        mRequestManager = manager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM_WITH_IMAGE) {
            //inflate your layout and pass it to view holder
            return new ProfileViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.profile_grid_item_2, parent, false), context, mRequestManager);
        } else if (viewType == TYPE_ITEM_WITHOUT_IMAGE) {
            return new ProfileViewHolderNoImage(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.profile_grid_item_no_image, parent, false), context);
        } else if (viewType == TYPE_HEADER_IMAGE) {
            //inflate your layout and pass it to view holder
            return new ProfileHeaderViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_profile_header3, parent, false));
        } else if (viewType == TYPE_HEADER_ACTIONS) {
            return new ProfileHeaderActions(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.fragment_profile_header_part_2, parent, false));
        } else if (viewType == TYPE_EMPTY) {
            return new EmptyProfileHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.empty_cell_holders, parent, false)
            );
        } else if (viewType == LoadMoreViewHolder.FOOTER) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.wrapping_footer_dark, parent, false), "", "");
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position == mUserActivityItems.size() + 2) { //on last elem, need to load more
            if (mLoadMorePosts != null)
                mLoadMorePosts.loadMore();
        }
        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView(mLoadState);
        } else if (holder instanceof ProfileViewHolder) {
            ((ProfileViewHolder) holder).bindModel(mUserActivityItems.get(position - 2));
        } else if (holder instanceof ProfileHeaderViewHolder) {
            ((ProfileHeaderViewHolder) holder).bindModel(mUser);
        } else if (holder instanceof ProfileHeaderActions) {
            ((ProfileHeaderActions) holder).bindView();
        } else if (holder instanceof ProfileViewHolderNoImage) {
            ((ProfileViewHolderNoImage) holder).bindModel(mUserActivityItems.get(position - 2));
        }
    }

    @Override
    public int getItemCount() {
        //2 for headers and 1 for footer
        int size = mUserActivityItems.size() + 2;
        return size == 2 ? 2 : size + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER_IMAGE;
        else if (position == 1)
            return TYPE_HEADER_ACTIONS;
        else if (position == mUserActivityItems.size() + 2)
            return LoadMoreViewHolder.FOOTER;
        else if (mUserActivityItems.get(position - 2) instanceof EmptyUserActivityItem)
            return TYPE_EMPTY;
        else {
            if (mUserActivityItems.get(position - 2).getEventImagePath() != null && !mUserActivityItems.get(position - 2).getEventImagePath().equals(""))
                return TYPE_ITEM_WITH_IMAGE;
            else return TYPE_ITEM_WITHOUT_IMAGE;
        }
    }

    public void setLoadMorePosts(LoadMoreViewHolder.OnLoadMore loadMorePosts) {
        mLoadMorePosts = loadMorePosts;
    }

    public void setTitleTextListener(TitleTextListener t) {
        mTitleTextListener = t;
    }

    public boolean titleShown() {
        return mTitleTextListener != null && mTitleTextListener.shown;
    }

    public void setLoadState(short state) {
        mLoadState = state;
    }

    //when photo disappears
    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof ProfileHeaderViewHolder && mTitleTextListener != null)
            mTitleTextListener.runShowTitle(true);
    }

    //when photo appears
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof ProfileHeaderViewHolder && mTitleTextListener != null) {
            mTitleTextListener.runShowTitle(false);
        }
    }

    //horrible hack
    //public FloatingActionButton vMessageButton;

    public class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        protected ImageView vProfilePicture;

        public ProfileHeaderViewHolder(View itemView) {
            super(itemView);
            vProfilePicture = (ImageView) itemView.findViewById(R.id.profile_image);
//            vMessageButton = (FloatingActionButton) itemView.findViewById(R.id.chat_button);
        }

        void bindModel(LinuteUser user) {
            mRequestManager
                    .load(Utils.getImageUrlOfUser(user.getProfileImage()))
                    .dontAnimate()
                    .signature(new StringSignature(context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000")))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vProfilePicture);
        }
    }

    public class ProfileHeaderActions extends RecyclerView.ViewHolder {

        protected TextView vStatusText;
        protected TextView vPosts;
        protected TextView vFollowers;
        protected TextView vCollegeName;
        protected View mFollowButton;
        protected TextView mFollowingButtonText;

        private TextView vUserName;
        protected FloatingActionButton vMessageButton;

        public ProfileHeaderActions(View itemView) {
            super(itemView);

            vStatusText = (TextView) itemView.findViewById(R.id.profilefrag_status);
            vPosts = (TextView) itemView.findViewById(R.id.profilefrag_num_posts);
            vFollowers = (TextView) itemView.findViewById(R.id.profilefrag_num_followers);

            mFollowButton = itemView.findViewById(R.id.follow_button);
            vMessageButton = (FloatingActionButton) itemView.findViewById(R.id.chat_button);
            mFollowingButtonText = (TextView) itemView.findViewById(R.id.follow_button_text);

            vUserName = (TextView) itemView.findViewById(R.id.username);
            vCollegeName = (TextView) itemView.findViewById(R.id.college_name);

            vMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUser == null ||
                            mUser.getUserID() == null ||
                            mUser.getUserID().equals(mUserid) ||
                            mUser.getFriend().equals(""))
                        return;

                    BaseTaptActivity activity = (BaseTaptActivity) context;
                    if (activity != null) {
                        activity.addFragmentToContainer(ChatFragment
                                .newInstance(
                                        null
                                        , mUser.getFirstName()
                                        , mUser.getLastName()
                                        , mUser.getUserID()));
                    }
                }
            });

            //follow someone
            mFollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mUser.getUserID() == null || !mUser.isInformationLoaded()) return;

                    if (!mUserid.equals(mUser.getUserID())) {
                        if (mUser.getFriend().equals("")) {
                            Map<String, Object> postData = new HashMap<>();
                            postData.put("user", mUser.getUserID());
                            mFollowingButtonText.setText("loading");

                            new LSDKPeople(context).postFollow(postData, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    final BaseTaptActivity activity = (BaseTaptActivity) context;
                                    if (activity != null) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Utils.showBadConnectionToast(activity);
                                                mFollowingButtonText.setText("follow");
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    final BaseTaptActivity activity = (BaseTaptActivity) context;

                                    if (activity == null) return;

                                    if (!response.isSuccessful()) {
                                        Log.d(TAG, response.body().string());
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mFollowingButtonText.setText("follow");
                                            }
                                        });
                                        return;
                                    }
//                                Log.d(TAG, "onResponse: " + response.body().string());


                                    try {
                                        final JSONObject jsonObject = new JSONObject(response.body().string());

                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mFollowingButtonText.setText("following");
                                                vMessageButton.show();
                                                try {
                                                    mUser.setFriendship(jsonObject.getString("id"));
                                                    mUser.setFriend("NotEmpty");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mFollowingButtonText.setText("follow");
                                                Utils.showServerErrorToast(activity);
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            Map<String, Object> putData = new HashMap<>();
                            putData.put("isDeleted", true);
                            mFollowingButtonText.setText("loading");

                            new LSDKPeople(context).putUnfollow(putData, mUser.getFriendship(), new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    final BaseTaptActivity activity = (BaseTaptActivity) context;
                                    if (activity != null) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mFollowingButtonText.setText("following");
                                                Utils.showBadConnectionToast(activity);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    final BaseTaptActivity activity1 = (BaseTaptActivity) context;

                                    if (!response.isSuccessful()) {
                                        Log.d(TAG, response.body().string());
                                        if (activity1 != null) {
                                            activity1.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Utils.showServerErrorToast(activity1);
                                                    mFollowingButtonText.setText("following");
                                                }
                                            });
                                        }
                                    } else {
                                        response.body().close();

                                        if (activity1 == null) return;

                                        activity1.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mFollowingButtonText.setText("follow");
                                                vMessageButton.hide();
                                                mUser.setFriend("");
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    } else { //user viewing own profile
                        MainActivity activity = (MainActivity) context;
                        if (activity != null) {
                            activity.startEditProfileActivity(EditProfileInfoActivity.class);
                        }
                    }
                }
            });

            final BaseTaptActivity activity = (BaseTaptActivity) context;
            itemView.findViewById(R.id.prof_header_followers_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activity != null && mUser.isInformationLoaded()) {
                        activity.addFragmentToContainer(FriendsListHolder.newInstance(mUser.getUserID()));
                    }
                }
            });
        }

        public void bindView() {
            String full = mUser.getFirstName() + " " + mUser.getLastName();
            vUserName.setText(full);
            vMessageButton.hide();

            if (!mUser.isInformationLoaded()) { //information hasn't loaded yet
                mFollowingButtonText.setText("Loading");
            } else { //information loaded
                if (mUser.getStatus() != null)
                    vStatusText.setText(mUser.getStatus().equals("") ? "No bio... :|" : mUser.getStatus());

                vPosts.setText(mUser.getPosts()+"");
                vFollowers.setText(mUser.getFollowers()+"");
                vCollegeName.setText(mUser.getCollegeName());

                if (mUser.getUserID().equals(mUserid)) { //viewer is viewing own profile
                    mFollowingButtonText.setText("Edit Profile");
                } else {
                    if (mUser.getFriend() == null || mUser.getFriend().equals("")) {
                        mFollowingButtonText.setText("follow");
                    } else {
                        mFollowingButtonText.setText("following");
                        vMessageButton.show(); //show message button
                    }
                }
            }
        }
    }

    public static abstract class TitleTextListener {
        public boolean shown = false;

        public void runShowTitle(boolean show) {
            shown = show;
            showTitle(show);
        }

        protected abstract void showTitle(boolean show);
    }
}
