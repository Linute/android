package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Arman on 1/19/16.
 */
public class UserSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = UserSelectAdapter.class.getSimpleName();
    private String mImageSign;
    private Context aContext;
    protected List<SearchUser> mSelectedUsers;
    protected List<SearchUser> mSearchUserList;
    private OnUserSelectedListener mOnUserSelectedListener;

    public void setOnUserSelectedListener(OnUserSelectedListener onUserSelectedListener) {
        this.mOnUserSelectedListener = onUserSelectedListener;
    }

    public UserSelectAdapter(Context aContext, List<SearchUser> searchUserList) {
        this.aContext = aContext;
        mSelectedUsers = new ArrayList<>();
        mSearchUserList = searchUserList;
        mImageSign = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000");

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_search_user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((SearchViewHolder) holder).bindModel(getUser(position));
        holder.itemView.setBackgroundColor(position < mSelectedUsers.size() ? 0x2284CFDF : 0);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveUser(holder.getAdapterPosition());
                if (mOnUserSelectedListener != null) {
                    mOnUserSelectedListener.onUserSelected(getUser(holder.getAdapterPosition()));
                }
            }
        });
    }

    private SearchUser getUser(int position) {
        if (position < mSelectedUsers.size()) {
            return mSelectedUsers.get(position);
        } else {
            return mSearchUserList.get(position - mSelectedUsers.size());
        }
    }

    private void moveUser(int position) {
        SearchUser user = getUser(position);

        if (mSelectedUsers.contains(user)) {
            int existPos = mSelectedUsers.indexOf(user);
            mSelectedUsers.remove(user);
            notifyItemRemoved(existPos);
        } else {
            mSelectedUsers.add(user);
            notifyItemInserted(mSelectedUsers.size() - 1);
        }

        //todo remove selected users from search list
    }

    @Override
    public int getItemCount() {
        return mSelectedUsers.size() + mSearchUserList.size();
    }

    public List<SearchUser> getSelectedUsers(){
        return mSelectedUsers;
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vSearchItemLinear;
        protected CircleImageView vUserImage;
        protected TextView vUserName;
        protected String mUserId;
        protected String mUserName;

        public SearchViewHolder(View itemView) {
            super(itemView);

            vSearchItemLinear = (LinearLayout) itemView.findViewById(R.id.search_users_list_layout);
            vUserImage = (CircleImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);

        }


        void bindModel(SearchUser user) {
            Glide.with(aContext)
                    .load(Utils.getImageUrlOfUser(user.getUserImage()))
                    .dontAnimate()
                    .signature(new StringSignature(mImageSign))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            mUserId = user.getUserId();
            mUserName = user.getUserName();

            vUserName.setText(user.getUserName());
        }
    }

    public interface OnUserSelectedListener {
        void onUserSelected(SearchUser user);
    }
}
