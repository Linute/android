package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;

import java.util.List;

/**
 * Created by mikhail on 7/28/16.
 */
public class UserGroupSearchAdapter extends UserSelectAdapter {

    public List<ChatRoom> mSearchRoomsList;

    private OnRoomSelectedListener onRoomSelectedListener;


    private static final int TYPE_ITEM = 0;

    private static final int TYPE_HEADER = 1;



    public UserGroupSearchAdapter(Context context, List<ChatRoom> roomsList, List<User> usersList) {
        super(context, usersList);
        this.mSearchRoomsList = roomsList;
    }

    public void setOnRoomSelectedListener(OnRoomSelectedListener onRoomSelectedListener) {
        this.onRoomSelectedListener = onRoomSelectedListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType){
            case TYPE_ITEM:
                return new ItemVH(inflater.inflate(R.layout.list_item_user_w_college, parent, false));
            case TYPE_HEADER:
                return new HeaderVH(inflater.inflate(R.layout.list_header, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case TYPE_ITEM:
                if(position < getPeopleHeaderPosition()){
                    ((ItemVH)holder).bindModel(getChat(position));
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(onRoomSelectedListener != null){
                                onRoomSelectedListener.onRoomSelected(getChat(holder.getAdapterPosition()));
                            }
                        }
                    });
                }else{
                    final User user = getUser(position);
                    ItemStatus status =
                            User.findUser(mSelectedUserList, user) != -1 ?
                                    ItemStatus.Selected :
                                    User.findUser(mLockedUserList, user) != -1 ?
                                            ItemStatus.Locked :
                                            //else
                                            ItemStatus.None;

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mOnUserSelectedListener != null && User.findUser(mLockedUserList, user) == -1) {
                                mOnUserSelectedListener.onUserSelected(getUser(holder.getAdapterPosition()), holder.getAdapterPosition());
                            }
                        }
                    });

                    ((ItemVH)holder).bindModel(user, status);
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

    private ChatRoom getChat(int position) {
        return mSearchRoomsList.get(position-1);
    }

    @Override
    protected User getUser(int position) {
        return mSearchUserList.get(position-mSearchRoomsList.size()-(mSearchRoomsList.size() == 0 ? 0 : 1) - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == getPeopleHeaderPosition() || position == getRoomsHeaderPosition()){
            return TYPE_HEADER;
        }else {
            return TYPE_ITEM;
        }
    }

    private int getRoomsHeaderPosition(){return mSearchRoomsList.size() == 0 ? -1 : 0;}
    private int getPeopleHeaderPosition(){return mSearchUserList.size() == 0 ? -1 : mSearchRoomsList.size() == 0 ? 0 : mSearchRoomsList.size()+1;}

    @Override
    public int getItemCount() {
        return mSearchRoomsList.size()+mSearchUserList.size()
                +(mSearchRoomsList.size() == 0 ? 0 : 1)
                +(mSearchUserList.size() == 0 ? 0 : 1)
                /*+2 for headers*/;
    }

    public class ItemVH extends SearchViewHolder{

        protected TextView tvCollege;

        public ItemVH(View itemView) {
            super(itemView);

            tvCollege = (TextView)itemView.findViewById(R.id.text_college);
        }

        public void bindModel(User user, ItemStatus status) {
            super.bindModel(user, status);
            tvCollege.setText(user.collegeName);
        }

        public void bindModel(ChatRoom chat) {
            Glide.with(itemView.getContext())
                    .load(chat.getRoomImage())
                    .signature(new StringSignature(mImageSign))
                    .into(vUserImage);
            vUserName.setText(chat.getRoomName());
            tvCollege.setText(chat.users.size() + " Members");
            itemView.setBackgroundColor(0);
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

    public interface OnRoomSelectedListener{
        public void onRoomSelected(ChatRoom room);
    }

}
