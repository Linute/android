package com.linute.linute.MainContent.FriendsList;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by QiFeng on 2/6/16.
 */
public class FriendsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Friend> mFriendsList;

    private Context aContext;

    private OnLoadMoreListener mOnLoadMoreListener;

    private boolean mAutoLoad = true;

    private boolean mFollowing;

    private SharedPreferences mSharedPreferences;
    private RequestManager mRequestManager;

    public FriendsListAdapter(List<Friend> friends, Context context, boolean following) {
        mFriendsList = friends;
        aContext = context;
        mFollowing = following;
        mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setRequestManager(RequestManager manager){
        mRequestManager = manager;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM)
            return new FriendsListViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.friends_list_item, parent, false)
            );
        else
            return new ViewHolderFooter(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.load_more_footer, parent, false)
            );
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FriendsListViewHolder)
            ((FriendsListViewHolder) holder).bindView(mFriendsList.get(position));


        else {
            if (mAutoLoad) {
                mOnLoadMoreListener.loadMore();
            } else {
                final ViewHolderFooter footer = (ViewHolderFooter) holder;
                footer.showButton(true);
                footer.setButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        footer.showButton(false);
                        mOnLoadMoreListener.loadMore();
                        mAutoLoad = true;
                    }
                });
            }
        }

    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public int getItemCount() {
        return mFriendsList.size();
    }


    private static final int FOOTER = 1;
    private static final int ITEM = 0;

    @Override
    public int getItemViewType(int position) {
        if (mFriendsList.get(position) == null) {
            return FOOTER;
        } else {
            return ITEM;
        }
    }

    public class FriendsListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView mProfilePicture;
        //private ImageView mStateImage;
        private TextView mUserName;

        private String mFriendId;
        private String mFriendName;

        public FriendsListViewHolder(View itemView) {
            super(itemView);

            mProfilePicture = (CircleImageView) itemView.findViewById(R.id.friend_user_image);
            mUserName = (TextView) itemView.findViewById(R.id.friend_name);
            itemView.setOnClickListener(this);
        }

        public void bindView(Friend friend) {
            mRequestManager
                    .load(Utils.getImageUrlOfUser(mFollowing ? friend.getOwnerProfile() : friend.getUserProfile()))
                    .dontAnimate()
                    .placeholder(R.drawable.image_loading_background)
                    .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000"))) //so profile images update
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(mProfilePicture);

            mUserName.setText(mFollowing ? friend.getOwnerName() : friend.getUserName());

            if (mFollowing) {
                mFriendId = friend.getOwnerId();
                mFriendName = friend.getOwnerName();
            } else {
                mFriendId = friend.getUserId();
                mFriendName = friend.getUserName();
            }
        }

        @Override
        public void onClick(View v) {
            BaseTaptActivity activity = (BaseTaptActivity) aContext;
            if (activity != null && mFriendId != null && mFriendName != null) {
                activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mFriendName, mFriendId));
            }
        }
    }


    public static class ViewHolderFooter extends RecyclerView.ViewHolder {

        private Button mRetryButton;
        private ProgressBar mProgressBar;

        public ViewHolderFooter(View itemView) {
            super(itemView);
            mRetryButton = (Button) itemView.findViewById(R.id.updatesFragment_reload_button);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.updateFragment_progress_bar);
        }

        public void showButton(boolean show) {
            mRetryButton.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        }

        public void setButtonListener(View.OnClickListener lis) {
            mRetryButton.setOnClickListener(lis);
        }

    }

    public interface OnLoadMoreListener {
        public void loadMore();
    }

    public void setAutoLoad(boolean autoLoad) {
        mAutoLoad = autoLoad;
    }
}
