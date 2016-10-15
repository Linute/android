package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by Arman on 1/20/16.
 */
public class RoomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = RoomsAdapter.class.getSimpleName();
    public static final int COLOR_READ = 0xFF000000;
    public static final int COLOR_UNREAD = 0xFF444444;

    private Context aContext;
    private List<ChatRoom> mRoomsList;
    private SharedPreferences mSharedPreferences;
    private LoadMoreViewHolder.OnLoadMore mOnLoadMore;
    private short mLoadingMoreState = LoadMoreViewHolder.STATE_LOADING;
    private RoomContextMenuCreator mRoomContextMenuCreator;
    protected RequestManager mRequestManager;

    public RoomsAdapter(Context aContext, List<ChatRoom> roomsList) {
        this.aContext = aContext;
        mRoomsList = roomsList;
        mSharedPreferences = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }


    public void setContextMenuCreator(RoomContextMenuCreator creator) {
        mRoomContextMenuCreator = creator;
    }

    public void setLoadMore(LoadMoreViewHolder.OnLoadMore load) {
        mOnLoadMore = load;
    }

    //should footer load
    public void setLoadingMoreState(short state) {
        mLoadingMoreState = state;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == LoadMoreViewHolder.FOOTER) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wrapping_footer_dark, parent, false),
                    "", ""
            );
        }
        return new RoomsViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_rooms_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RoomsViewHolder) {
            ((RoomsViewHolder) holder).bindModel(mRoomsList.get(position));
            if (mRoomContextMenuCreator != null) {
                holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        mRoomContextMenuCreator.onCreateContextMenu(contextMenu, mRoomsList.get(holder.getAdapterPosition()), holder.getAdapterPosition(), contextMenuInfo);
                    }
                });
            }
        } else {
            //bindMenuItem view, tells it which to show: footer or text
            ((LoadMoreViewHolder) holder).bindView(mLoadingMoreState);
            if (mOnLoadMore != null) mOnLoadMore.loadMore();
        }
    }

    @Override
    public int getItemViewType(int position) {
        //last item is the footer
        return position == mRoomsList.size() ? LoadMoreViewHolder.FOOTER : 0;
    }


    @Override
    public int getItemCount() {
        //+1 for the footer
        return mRoomsList.isEmpty() ? 0 : mRoomsList.size() + 1;
    }

    class RoomsViewHolder extends RecyclerView.ViewHolder {
        protected ImageView vUserImage;
        protected TextView vUserName;
        protected TextView vLastMessage;
        protected TextView vTimeStamp;
        protected ChatRoom mRoom;
        protected View vIsMuted;
        private StringSignature mSignature;

        public RoomsViewHolder(View itemView) {
            super(itemView);

            mSignature = new StringSignature(mSharedPreferences.getString("imageSigniture", "000"));
            vUserImage = (ImageView) itemView.findViewById(R.id.rooms_user_image);
            vUserName = (TextView) itemView.findViewById(R.id.rooms_user_name);
            vLastMessage = (TextView) itemView.findViewById(R.id.rooms_user_last_message);
            vTimeStamp = (TextView) itemView.findViewById(R.id.room_time_stamp);
            vIsMuted = itemView.findViewById(R.id.room_is_muted);

            //when room clicked, takes user to chat fragment
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseTaptActivity activity = (BaseTaptActivity) aContext;
                    if (activity != null) {
                        //mark all as read
                        mRoom.hasUnread = false;
                        activity.addFragmentToContainer(
                                ChatFragment.newInstance(
                                        mRoom
                                )
                        );
                    }
                }
            });


        }

        void bindModel(ChatRoom room) {

            mRoom = room;
            mRequestManager
                    .load(room.roomImage)
                    .dontAnimate()
                    .signature(mSignature)
                    .placeholder(R.color.seperator_color)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) //only cache the scaled image
                    .into(vUserImage);

            vUserName.setText(Utils.stripUnsupportedCharacters(room.getRoomName()));
            vLastMessage.setText(Utils.stripUnsupportedCharacters(room.lastMessage));
            vTimeStamp.setText(Utils.getRoomDateFormat(room.time));
            vIsMuted.setVisibility(mRoom.isMuted ? View.VISIBLE : View.INVISIBLE);

            vUserName.setTypeface((mRoom.hasUnread ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT));
            vTimeStamp.setTypeface((mRoom.hasUnread ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT));
            vTimeStamp.setTextColor(mRoom.hasUnread ? COLOR_READ : COLOR_UNREAD);
            vLastMessage.setTextColor(mRoom.hasUnread ? COLOR_READ : COLOR_UNREAD);
        }
    }


    public interface RoomContextMenuCreator {
        void onCreateContextMenu(ContextMenu contextMenu, ChatRoom room, int position, ContextMenu.ContextMenuInfo contextMenuInfo);
    }
}
