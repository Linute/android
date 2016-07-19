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
    private String mImageSign;
    private Context aContext;
    protected List<User> mSearchUserList;
    private OnUserSelectedListener mOnUserSelectedListener;

    public void setOnUserSelectedListener(OnUserSelectedListener onUserSelectedListener) {
        this.mOnUserSelectedListener = onUserSelectedListener;
    }

    public UserSelectAdapter(Context aContext, List<User> searchUserList) {
        this.aContext = aContext;
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
        ((SearchViewHolder) holder).bindModel(getUser(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int position = holder.getAdapterPosition();
                if (mOnUserSelectedListener != null) {
                    mOnUserSelectedListener.onUserSelected(getUser(holder.getAdapterPosition()));
                }
            }
        });
    }

    private User getUser(int position) {
        return mSearchUserList.get(position);
    }


    @Override
    public int getItemCount() {
        return mSearchUserList.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vSearchItemLinear;
        protected ImageView vUserImage;
        protected TextView vUserName;
        protected String mUserId;
        protected String mUserName;

        public SearchViewHolder(View itemView) {
            super(itemView);

            vSearchItemLinear = (LinearLayout) itemView.findViewById(R.id.search_users_list_layout);
            vUserImage = (ImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);

        }


        void bindModel(User user) {
            Glide.with(aContext)
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .asBitmap()
                    .dontAnimate()
                    .signature(new StringSignature(mImageSign))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            mUserId = user.userId;
            mUserName = user.userName;

            vUserName.setText(user.userName);
        }
    }

    public interface OnUserSelectedListener {
        void onUserSelected(User user);
    }
}
