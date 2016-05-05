package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.MainContent.ProfileFragment.EnlargePhotoViewer;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Arman on 1/20/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context aContext;
    private List<Chat> aChatList;
    private static final DateFormat mDateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    private static final SimpleDateFormat mLongFormat = new SimpleDateFormat("M/dd h:mm a");
    private LoadMoreListener mLoadMoreListener;

    static {
        mDateFormat.setTimeZone(TimeZone.getDefault());
    }

    //private HashMap<Integer, ArrayList<ChatHead>> aChatHeadsMap;
    //private SharedPreferences aSharedPreferences;

    public ChatAdapter(Context aContext, List<Chat> aChatList) {
        this.aContext = aContext;
        this.aChatList = aChatList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Chat.TYPE_MESSAGE_ME:
                return new ChatViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_chat_list_item_me, parent, false));
            case Chat.TYPE_MESSAGE_OTHER_PERSON:
                return new ChatViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_chat_list_item_you, parent, false));
            case Chat.TYPE_ACTION_TYPING:
                return new ChatActionHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_chat_list_item_action_typing, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatViewHolder) {
            ((ChatViewHolder)holder).bindModel(aChatList.get(position));
        }

        if (position == 0){
            if (mLoadMoreListener != null){
                mLoadMoreListener.loadMore();
            }
        }
    }

    public void setLoadMoreListener(LoadMoreListener l){
        mLoadMoreListener = l;
    }

    @Override
    public int getItemCount() {
        return aChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return aChatList.get(position).getType();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        protected TextView vUserMessage;
        protected TextView vUserTime;
        protected ImageView vActionImage;

        protected ImageView vImage;
        protected View vFrame;

        private int mType;
        private String mUrl;

        public ChatViewHolder(View itemView) {
            super(itemView);
            vUserMessage = (TextView) itemView.findViewById(R.id.chat_user_message);
            vUserTime = (TextView) itemView.findViewById(R.id.chat_user_time);
            vActionImage = (ImageView) itemView.findViewById(R.id.message_action_icon);
            vFrame = itemView.findViewById(R.id.frame);
            vImage = (ImageView) itemView.findViewById(R.id.image);

            vImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RoomsActivity activity = (RoomsActivity) aContext;
                    if (activity != null) {
                        if (mType == Chat.MESSAGE_IMAGE) {
                            EnlargePhotoViewer.newInstance(EnlargePhotoViewer.IMAGE, mUrl)
                                    .show(activity.getSupportFragmentManager(), "ImageOrVideo");
                        } else if (mType == Chat.MESSAGE_VIDEO) {
                            EnlargePhotoViewer.newInstance(EnlargePhotoViewer.VIDEO, mUrl)
                                    .show(activity.getSupportFragmentManager(), "ImageOrVideo");
                        }
                       // Log.i("test", "onClick: "+mType);
                    }
                }
            });
        }

        void bindModel(Chat chat) {
            mType = chat.getMessageType();

            switch (chat.getMessageType()){
                case Chat.MESSAGE_IMAGE:
                    vFrame.findViewById(R.id.cinema_icon).setVisibility(View.GONE);
                    vFrame.setVisibility(View.VISIBLE);
                    vUserMessage.setVisibility(View.GONE);
                    setImage(chat.getImageId());
                    mUrl = chat.getImageId();
                    break;
                case Chat.MESSAGE_VIDEO:
                    vFrame.findViewById(R.id.cinema_icon).setVisibility(View.VISIBLE);
                    vFrame.setVisibility(View.VISIBLE);
                    vUserMessage.setVisibility(View.GONE);
                    setImage(chat.getImageId());
                    mUrl = chat.getVideoId();
                    break;
                default:
                    vFrame.setVisibility(View.GONE);
                    vUserMessage.setVisibility(View.VISIBLE);
                    vUserMessage.setText(chat.getMessage());
                    mUrl = "";
                    break;
            }


            if (chat.getType() == Chat.TYPE_MESSAGE_ME){
                vActionImage.setImageResource(chat.isRead() ? R.drawable.ic_chat_read : R.drawable.delivered_chat);
            }

            if (chat.getDate() != null) {
                vUserTime.setText(new Date().getTime() - chat.getDate().getTime() > DateUtils.DAY_IN_MILLIS ?
                        mLongFormat.format(chat.getDate()) :
                        mDateFormat.format(chat.getDate())
                );
            }
        }


        private void setImage(String image) {
            Glide.with(aContext)
                    .load(Utils.getMessageImageURL(image))
                    .asBitmap()
                    .placeholder(R.drawable.chat_backgrounds)
                    .into(vImage);
        }
    }



    public class ChatActionHolder extends RecyclerView.ViewHolder {

        public ChatActionHolder(View itemView) {
            super(itemView);
        }
    }


    public interface LoadMoreListener{
        void loadMore();
    }


}
