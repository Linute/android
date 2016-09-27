package com.linute.linute.MainContent.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;

/**
 * Created by mikhail on 7/18/16.
 */
public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.UserVH>{


    ArrayList<User> mUsers;
    RequestManager mRequestManager;


    public SelectedUsersAdapter(ArrayList<User> mUsers ) {
        this.mUsers = mUsers;
    }

    private UserSelectAdapter2.OnUserClickListener mUserSelectedListener;


    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    @Override
    public UserVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new UserVH(inflater.inflate(R.layout.fragment_create_chat_selected_users, parent, false), mRequestManager);
    }

    @Override
    public void onBindViewHolder(final UserVH holder, int position) {
        holder.bind(mUsers.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserSelectedListener != null){
                    int position = holder.getAdapterPosition();
                    //Log.i("AAA", position+"");
                    mUserSelectedListener.onUserClick(mUsers.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    protected static class UserVH extends RecyclerView.ViewHolder{

        public final ImageView userIV;
        private RequestManager mRequestManager;

        public UserVH(View itemView, RequestManager manager) {
            super(itemView);
            mRequestManager = manager;
            userIV = (ImageView)itemView.findViewById(R.id.image_user);
        }

        public void bind(User user){
            mRequestManager
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .dontAnimate()
                    .placeholder(R.color.seperator_color)
                    .into(userIV);
        }
    }

    public void setUserSelectedListener(UserSelectAdapter2.OnUserClickListener mUserSelectedListener) {
        this.mUserSelectedListener = mUserSelectedListener;
    }

}
