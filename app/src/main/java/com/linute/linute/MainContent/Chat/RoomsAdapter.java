package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Arman on 1/20/16.
 */
public class RoomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = RoomsAdapter.class.getSimpleName();

    private Context aContext;
    private List<Rooms> mRoomsList;
    private SharedPreferences mSharedPreferences;

    private Runnable mLoadMore;

    public RoomsAdapter(Context aContext, List<Rooms> roomsList) {
        this.aContext = aContext;
        mRoomsList = roomsList;
        mSharedPreferences = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }


    public void setLoadMore(Runnable load){
        mLoadMore = load;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RoomsViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_rooms_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((RoomsViewHolder) holder).bindModel(mRoomsList.get(position));

        //load more
        if (position == mRoomsList.size() - 1 && mLoadMore != null){
            mLoadMore.run();
        }
    }

    @Override
    public int getItemCount() {
        return mRoomsList.size();
    }

    class RoomsViewHolder extends RecyclerView.ViewHolder {
        protected View vRoomsListLinear;
        protected CircleImageView vUserImage;
        protected TextView vUserName;
        protected TextView vLastMessage;
        protected View vHasUnreadIcon;
        protected TextView vTimeStamp;
        protected Rooms mRooms;

        public RoomsViewHolder(View itemView) {
            super(itemView);

            vRoomsListLinear = itemView.findViewById(R.id.rooms_list_linear);
            vUserImage = (CircleImageView) itemView.findViewById(R.id.rooms_user_image);
            vUserName = (TextView) itemView.findViewById(R.id.rooms_user_name);
            vLastMessage = (TextView) itemView.findViewById(R.id.rooms_user_last_message);
            vHasUnreadIcon = itemView.findViewById(R.id.room_unread);
            vTimeStamp = (TextView) itemView.findViewById(R.id.room_time_stamp);

            vRoomsListLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseTaptActivity activity = (BaseTaptActivity) aContext;
                    if (activity != null) {
                        mRooms.setHasUnread(false);

                        activity.addFragmentToContainer(
                                ChatFragment.newInstance(
                                        mRooms.getRoomId(),
                                        mRooms.getUserName(),
                                        mRooms.getUserId()
                                )
                        );
                    }
                }
            });
        }

        void bindModel(Rooms room) {

            mRooms = room;

            Glide.with(aContext)
                    .load(Utils.getImageUrlOfUser(room.getUserImage()))
                    .asBitmap()
                    .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vUserImage);

            vUserName.setText(room.getUserName());
            vLastMessage.setText(room.getLastMessage());
            vTimeStamp.setText(room.getTime() == 0 ? "" : Utils.getTimeAgoString(room.getTime()));

            if (room.hasUnread() && vHasUnreadIcon.getVisibility() == View.INVISIBLE)
                vHasUnreadIcon.setVisibility(View.VISIBLE);
            else if (!room.hasUnread() && vHasUnreadIcon.getVisibility() == View.VISIBLE)
                vHasUnreadIcon.setVisibility(View.INVISIBLE);
        }
    }


}
