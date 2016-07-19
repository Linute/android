package com.linute.linute.MainContent.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;

/**
 * Created by mikhail on 7/18/16.
 */
public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.UserVH>{


    ArrayList<User> mUsers;

    public SelectedUsersAdapter(ArrayList<User> mUsers) {
        this.mUsers = mUsers;
    }


    @Override
    public UserVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new UserVH(inflater.inflate(R.layout.fragment_create_chat_selected_users, parent, false));
    }

    @Override
    public void onBindViewHolder(UserVH holder, int position) {
        holder.bind(mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    protected static class UserVH extends RecyclerView.ViewHolder{

        public final ImageView userIV;

        public UserVH(View itemView) {
            super(itemView);
            userIV = (ImageView)itemView.findViewById(R.id.image_user);
        }

        public void bind(User user){
            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .asBitmap()
                    .into(userIV);
        }
    }


}
