package com.linute.linute.MainContent.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;

/**
 * Created by mikhail on 7/28/16.
 */
public class UserGroupSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    public ArrayList<ChatRoom> roomsList;
    public ArrayList<User> usersList;

    private boolean showRooms;

    private static final int TYPE_ITEM = 0;

    private static final int TYPE_HEADER = 1;


    public UserGroupSearchAdapter() {
        this.roomsList = new ArrayList<>(0);
        this.usersList = new ArrayList<>(0);
        showRooms = true;
    }


    public UserGroupSearchAdapter(ArrayList<ChatRoom> roomsList, ArrayList<User> usersList) {
        this.roomsList = roomsList;
        this.usersList = usersList;
        showRooms = true;
    }



    public

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType){
            case TYPE_ITEM:
                return new ItemVH(inflater.inflate(R.layout.fragment_search_user_list_item, parent, false));
            case TYPE_HEADER:
                return new HeaderVH(inflater.inflate(R.layout.list_header, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case TYPE_ITEM:
                if(position <getPeopleHeaderPosition()){
                    ((ItemVH)holder).bind(roomsList.get(position-1));
                }else{
                    ((ItemVH)holder).bind(roomsList.get(position-roomsList.size()-1));
                }
                return;
            case TYPE_HEADER:
                if(position == getRoomsHeaderPosition()){
                    ((HeaderVH)holder).bind("Rooms");
                }else if(position == getPeopleHeaderPosition()){
                    ((HeaderVH)holder).bind("People");
                }
        }
    }


    @Override
    public int getItemViewType(int position) {
        if(position == getPeopleHeaderPosition() || position == getRoomsHeaderPosition()){
            return TYPE_HEADER;
        }else {
            return TYPE_ITEM;
        }
    }

    private int getRoomsHeaderPosition(){return 0;}
    private int getPeopleHeaderPosition(){return roomsList.size()+1;}

    @Override
    public int getItemCount() {
        return roomsList.size()+usersList.size()+2 /*+2 for headers*/;
    }

    public static class ItemVH extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;

        public ItemVH(View itemView) {
            super(itemView);

            ivProfile = (ImageView) itemView.findViewById(R.id.search_users_list_image);
            tvName = (TextView) itemView.findViewById(R.id.search_users_list_name);
        }

        public void bind(User user) {
            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .into(ivProfile);
            tvName.setText(user.userName);
        }

        public void bind(ChatRoom chat) {
            Glide.with(itemView.getContext())
                    .load(chat.getRoomImage())
                    .into(ivProfile);
            tvName.setText(chat.getRoomName());
        }
    }


    public static class HeaderVH extends RecyclerView.ViewHolder{
        TextView tvHeader;
        public HeaderVH(View itemView) {
            super(itemView);
            tvHeader = (TextView)itemView.findViewById(R.id.text_header);
        }

        public void bind(String headerText){
            tvHeader.setText(headerText);
        }
    }


}
