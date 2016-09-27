package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by mikhail on 9/25/16.
 */
public class UserGroupSearchAdapter2 extends UserSelectAdapter2 {


    public static final int ROOMS_LIST_INDEX = 1;
    public static final int USERS_LIST_INDEX = 2;
    private final StringSignature mImageSign;
    private OnUserClickListener onUserClickListener;
    private OnRoomClickListener onRoomClickListener;

    public UserGroupSearchAdapter2(Context aContext) {
        super(aContext);
        mImageSign = new StringSignature(aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000"));
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderVH(inflater.inflate(R.layout.list_header, parent, false));
            default:
                return new ItemVH(inflater.inflate(R.layout.fragment_search_user_list_item, parent, false));
        }
    }

    @Override
    public void onItemSelected(int position, int type, Object item) {
        if (type != HeadedSelectableListAdapter.TYPE_HEADER) {
            if(item instanceof User && onUserClickListener != null){
                User user = (User) getItem(position);
                onUserClickListener.onUserClick(user);
            }
            if(item instanceof ChatRoom && onRoomClickListener != null){
                onRoomClickListener.onRoomClick((ChatRoom) getItem(position));
            }
        }
    }

    @Override
    public String getHeaderTitle(int i) {
        switch (i) {
            case 0:
                return null;
            case 1:
                return "Rooms";
            case 2:
                return "Users";
        }
        return null;
    }

    @Override
    public int getNumLists() {
        return 3;
    }

    public void setUsers(List<User> users) {
        setList(USERS_LIST_INDEX, users);
        notifyDataSetChanged();
    }

    public void setRooms(List<ChatRoom> rooms) {
        setList(ROOMS_LIST_INDEX, rooms);
        notifyDataSetChanged();
    }

    public void setUserClickListener(OnUserClickListener listener) {
        onUserClickListener = listener;
    }

    public void setRoomClickListener(OnRoomClickListener listener) {
        onRoomClickListener = listener;
    }


    public class ItemVH extends VH<Object> {
        //protected RelativeLayout vSearchItemLinear;
        protected ImageView vUserImage;
        protected TextView vUserName;

        public ItemVH(View itemView) {
            super(itemView);
            vUserImage = (ImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);
            vUserName.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            vUserName.setGravity(Gravity.CENTER_VERTICAL);
            vUserName.requestLayout();

            /*TextView vUserCollege = (TextView) */
            View college = itemView.findViewById(R.id.search_users_list_college);
            if (college != null)
                college.setVisibility(View.GONE);

        }


        @Override
        public void bind(Object object, ItemStatus status) {
            if (object instanceof User) {
                bind((User) object, status);
            } else if (object instanceof ChatRoom) {
                bind((ChatRoom) object);
            }
        }

        public void bind(User user, ItemStatus status) {

            boolean isSelected = status == ItemStatus.Selected;
            boolean isLocked = status == ItemStatus.Locked;

            itemView.setBackgroundColor((isLocked ? 0x3384CFDF : isSelected ? 0x5584CFDF : 0));


            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .dontAnimate()
                    .signature(mImageSign)
                    .placeholder(R.color.seperator_color)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);


            String name = user.firstName + " " + user.lastName;
            vUserName.setText(name);
        }

        public void bind(ChatRoom room) {

            itemView.setBackgroundColor(0);

            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(room.roomImage))
                    .dontAnimate()
                    .signature(mImageSign)
                    .placeholder(R.color.seperator_color)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);


            vUserName.setText(room.getRoomName());
        }

    }


    interface OnRoomClickListener {
        void onRoomClick(ChatRoom user);
    }
}
