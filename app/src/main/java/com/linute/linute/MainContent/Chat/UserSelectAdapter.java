package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by Arman on 1/19/16.
 */
public class UserSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = UserSelectAdapter.class.getSimpleName();
    protected String mImageSign;
    protected List<User> mSearchUserList;


    protected List<User> mLockedUserList;
    protected List<User> mSelectedUserList;
    protected OnUserSelectedListener mOnUserSelectedListener;

    public void setOnUserSelectedListener(OnUserSelectedListener onUserSelectedListener) {
        this.mOnUserSelectedListener = onUserSelectedListener;
    }

    public UserSelectAdapter(Context aContext, List<User> searchUserList) {
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final User user = getUser(position);
        ItemStatus status =
                User.findUser(mSelectedUserList, user) != -1 ?
                        ItemStatus.Selected :
                User.findUser(mLockedUserList, user) != -1 ?
                        ItemStatus.Locked :
                //else
                        ItemStatus.None;

        ((SearchViewHolder) holder).bindModel(user, status);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnUserSelectedListener != null && User.findUser(mLockedUserList, user) == -1) {
                    mOnUserSelectedListener.onUserSelected(getUser(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
            }
        });
    }

    protected User getUser(int position) {
        return mSearchUserList.get(position);
    }


    @Override
    public int getItemCount() {
        return mSearchUserList.size();
    }

    public enum ItemStatus{
        None,
        Selected,
        Locked
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vSearchItemLinear;
        protected ImageView vUserImage;
        protected TextView vUserName;

        public SearchViewHolder(View itemView) {
            super(itemView);

            vSearchItemLinear = (LinearLayout) itemView.findViewById(R.id.search_users_list_layout);
            vUserImage = (ImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);

        }


        void bindModel(User user, ItemStatus status) {
            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .dontAnimate()
                    .signature(new StringSignature(mImageSign))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            vUserName.setText(user.userName);

            switch (status){
                case None:
                    itemView.setClickable(true);
                    itemView.setBackgroundColor(0);
                    break;
                case Selected:
                    itemView.setClickable(true);
                    itemView.setBackgroundColor(0x4484CFDF);
                    break;
                case Locked:
                    itemView.setClickable(false);
                    itemView.setBackgroundColor(0x1184CFDF);

                    break;
            }
        }
    }


    public void setLockedUserList(List<User> mLockedUserList) {
        this.mLockedUserList = mLockedUserList;
    }

    public void setSelectedUserList(List<User> mSelectedUserList) {
        this.mSelectedUserList = mSelectedUserList;
    }

    public interface OnUserSelectedListener {
        void onUserSelected(User user, int position);
    }
}
