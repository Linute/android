package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Arman on 1/20/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    //private Context aContext;
    private List<Chat> aChatList;
    private static final DateFormat mDateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

    private LoadMoreListener mLoadMoreListener;

    //private HashMap<Integer, ArrayList<ChatHead>> aChatHeadsMap;
    //private SharedPreferences aSharedPreferences;

    public ChatAdapter(Context aContext, List<Chat> aChatList) {
        //this.aContext = aContext;
        this.aChatList = aChatList;
        //aSharedPreferences = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Chat.TYPE_MESSAGE_ME:
                layout = R.layout.fragment_chat_list_item_me;
                break;
            case Chat.TYPE_MESSAGE_OTHER_PERSON:
                layout = R.layout.fragment_chat_list_item_you;
                break;
            case Chat.TYPE_ACTION_TYPING:
                layout = R.layout.fragment_chat_list_item_action_typing;
                break;
            //case Chat.TYPE_CHAT_HEAD:
            //break;
        }

        return new ChatViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.bindModel(aChatList.get(position));
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

        public ChatViewHolder(View itemView) {
            super(itemView);
            vUserMessage = (TextView) itemView.findViewById(R.id.chat_user_message);
            vUserTime = (TextView) itemView.findViewById(R.id.chat_user_time);
            vActionImage = (ImageView) itemView.findViewById(R.id.message_action_icon);
        }

        void bindModel(Chat chat) {

            if (chat.getType() == Chat.TYPE_ACTION_TYPING) return; //if typing action, do nothing

            if (chat.getType() == Chat.TYPE_MESSAGE_ME) //set icons for read and delivered if it's our message
                vActionImage.setImageResource(chat.isRead() ? R.drawable.ic_chat_read : R.drawable.delivered_chat );

            vUserMessage.setText(chat.getMessage());

            if (chat.getDate() != 0)
                vUserTime.setText(mDateFormat.format(new Date(chat.getDate())));

        }
    }


    public interface LoadMoreListener{
        void loadMore();
    }

}
