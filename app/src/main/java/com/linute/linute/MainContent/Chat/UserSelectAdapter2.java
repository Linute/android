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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikhail on 9/17/16.
 */
public class UserSelectAdapter2 extends HeadedSelectableListAdapter {


    public static final int FOCUSED_USERS_LIST_INDEX = 0;
    public static final int USERS_LIST_INDEX = 1;



    protected List<User> selectedUserList;
    protected List<User> lockedUserList;
    private final List<User> focusedUserList;
    private final StringSignature mImageSign;
    private OnUserClickListener onUserClickListener;

    public UserSelectAdapter2(Context aContext) {
        super();
        focusedUserList = new ArrayList<>(1);
        setList(FOCUSED_USERS_LIST_INDEX, focusedUserList);
        mImageSign = new StringSignature(aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000"));
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        HeadedListAdapter.VH vh = super.onCreateViewHolder(parent, viewType);
        if(vh != null){
            return vh;
        }
        return new UserVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_search_user_list_item, parent, false));

    }

    @Override
    public void onItemSelected(int position, int type, Object item) {
        if(type != HeadedSelectableListAdapter.TYPE_HEADER && onUserClickListener != null){
            User user = (User) getItem(position);
            onUserClickListener.onUserClick(user);
        }
    }

    @Override
    public String getHeaderTitle(int i) {
        switch (i){
            case 0: return null;
            case 1: return "Users";
        }
        return null;
    }

    @Override
    public ItemStatus getItemStatus(int position) {
        if(getItemViewType(position) == TYPE_HEADER){
            return ItemStatus.Locked;
        }

        Object item = getItem(position);
        if(item instanceof User) {
            if (User.findUser(lockedUserList, (User) item) > -1) {
                return ItemStatus.Locked;
            }
            if (User.findUser(selectedUserList, (User) item) > -1) {
                return ItemStatus.Selected;
            }
        }
        return ItemStatus.None;
    }

    @Override
    public int getNumLists() {
        return 2;
    }

    public void setUsers(List<User> users){
        setList(USERS_LIST_INDEX, users);
        notifyDataSetChanged();
    }

    public void setUserSelectListener(OnUserClickListener listener){
        onUserClickListener = listener;
    }

    public void setFocusedUser(User user){
        if(focusedUserList.size() > 0) {
            focusedUserList.set(0, user);
            notifyItemChanged(1);
        }else{
            focusedUserList.add(user);
            notifyItemRangeInserted(0,2);
        }
    }

    public void clearFocusedUser(){
        focusedUserList.clear();
        notifyItemRangeRemoved(0,2);

    }

    public boolean hasFocusedUser(){
        return focusedUserList.size() > 0;
    }

    public User getFocusedUser(){
        if(focusedUserList.size() > 0){
            return focusedUserList.get(0);
        }else{
            return null;
        }
    }

    public void setSelectedUserList(List<User> selectedUserList) {
        this.selectedUserList = selectedUserList;
    }

    public void setLockedUserList(List<User> lockedUserList) {
        this.lockedUserList = lockedUserList;
    }


    public class UserVH extends VH<User> {
        //protected RelativeLayout vSearchItemLinear;
        protected ImageView vUserImage;
        protected TextView vUserName;

        public UserVH(View itemView) {
            super(itemView);
            vUserImage = (ImageView) itemView.findViewById(R.id.search_users_list_image);
            vUserName = (TextView) itemView.findViewById(R.id.search_users_list_name);
            vUserName.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            vUserName.setGravity(Gravity.CENTER_VERTICAL);
            vUserName.requestLayout();

            /*TextView vUserCollege = (TextView) */
            View college = itemView.findViewById(R.id.search_users_list_college);
            if(college != null)
                college.setVisibility(View.GONE);

        }


        public void bind(User user, HeadedListAdapter.ItemStatus status) {

            boolean isSelected = status == ItemStatus.Selected;
            boolean isLocked = status == ItemStatus.Locked;

            itemView.setBackgroundColor((isLocked ? 0x33CCCCCC : isSelected ? 0x5584CFDF : 0));

            Glide.with(itemView.getContext())
                    .load(Utils.getImageUrlOfUser(user.userImage))
                    .dontAnimate()
                    .signature(mImageSign)
                    .placeholder(R.color.seperator_color)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);


            String name = user.firstName+" "+user.lastName;
            vUserName.setText(name);
        }


    }

    interface OnUserClickListener {
        void onUserClick(User user);
    }

}
