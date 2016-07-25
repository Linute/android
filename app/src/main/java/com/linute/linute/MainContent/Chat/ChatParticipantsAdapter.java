package com.linute.linute.MainContent.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by mikhail on 7/12/16.
 */
public class ChatParticipantsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int TYPE_ADD = 0;
    private static final int TYPE_PARTICIPANT = 1;

    private List<User> mParticipants;

    private View.OnClickListener mAddPeopleListener;
    private OnUserClickListener mUserClickListener;


    public void setAddPeopleListener(View.OnClickListener addPeopleListener) {
        this.mAddPeopleListener = addPeopleListener;
    }

    public void setUserClickListener(OnUserClickListener mPeopleClickListener) {
        this.mUserClickListener = mPeopleClickListener;
    }




    public ChatParticipantsAdapter(List<User> participants) {
        this.mParticipants = participants;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case TYPE_ADD:
                return new AddVH(inflater.inflate(R.layout.fragment_chat_settings_add_user, parent, false));
            case TYPE_PARTICIPANT:
                return new ParticipantVH(inflater.inflate(R.layout.fragment_search_user_list_item, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case TYPE_PARTICIPANT:
                final User user = getItem(position);
                ((ParticipantVH)holder).bind(getItem(position));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mUserClickListener != null){
                            mUserClickListener.OnUserClick(user);
                        }
                    }
                });
                holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        if(mUserClickListener != null) {
                            mUserClickListener.onCreateContextMenu(contextMenu,user,contextMenuInfo);
                        }
                    }
                });
                return;
            case TYPE_ADD:
                ((AddVH)holder).bind();
                holder.itemView.setOnClickListener(mAddPeopleListener);
                return;

        }
    }

    public User getItem(int position){
        //-1 to accommodate for add-participant list item
        return mParticipants.get(position-1);
    }

    @Override
    public int getItemCount() {
        //+1 to accommodate for add-participant list item
        return mParticipants.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_ADD : TYPE_PARTICIPANT;
    }

    public static class ParticipantVH extends RecyclerView.ViewHolder{
        public final ImageView profileImageIV;
        public final TextView nameTV;

        public ParticipantVH(View itemView) {
            super(itemView);
            profileImageIV = (ImageView)itemView.findViewById(R.id.search_users_list_image);
            nameTV = (TextView) itemView.findViewById(R.id.search_users_list_name);
        }

        public void bind(User user){
            String imageUrlOfUser = Utils.getImageUrlOfUser(user.userImage);
            Glide.with(itemView.getContext())
                    .load(imageUrlOfUser)
//                    .asBitmap()
                    .into(profileImageIV);
            nameTV.setText(user.userName);
        }
    }

    public static class AddVH extends RecyclerView.ViewHolder{
        public final ImageView profileImageIV;
        public final TextView nameTV;

        public AddVH(View itemView) {
            super(itemView);
            profileImageIV = (ImageView)itemView.findViewById(R.id.search_users_list_image);
            nameTV = (TextView) itemView.findViewById(R.id.search_users_list_name);
        }

        public void bind(){
            profileImageIV.setImageResource(R.mipmap.ic_add_blue);
            nameTV.setText("Add People");
        }

    }


    interface OnUserClickListener {
        void OnUserClick(User user);
        void onCreateContextMenu(ContextMenu contextMenu, User user, ContextMenu.ContextMenuInfo contextMenuInfo);
    }




}
