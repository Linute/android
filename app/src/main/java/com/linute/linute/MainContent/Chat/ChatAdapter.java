package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

/**
 * Created by Arman on 1/20/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private Context aContext;
    private List<Chat> aChatList;
    private SharedPreferences aSharedPreferences;

    public ChatAdapter(Context aContext, List<Chat> chatList) {
        this.aContext = aContext;
        aChatList = chatList;
        aSharedPreferences = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_chat_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.bindModel(aChatList.get(position));
    }

    @Override
    public int getItemCount() {
        return aChatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout vOwnerLinear;
        protected RelativeLayout vUserRelative;
        protected CircularImageView vOwnerImage;
        protected CircularImageView vUserImage;
        protected TextView vOwnerName;
        protected TextView vUserName;
        protected TextView vOwnerMessage;
        protected TextView vUserMessage;
        protected TextView vOwnerTime;
        protected TextView vUserTime;

        public ChatViewHolder(View itemView) {
            super(itemView);

            vOwnerLinear = (LinearLayout) itemView.findViewById(R.id.chat_owner);
            vUserRelative = (RelativeLayout) itemView.findViewById(R.id.chat_user);
            vOwnerImage = (CircularImageView) itemView.findViewById(R.id.chat_owner_image);
            vUserImage = (CircularImageView) itemView.findViewById(R.id.chat_user_image);
            vOwnerName = (TextView) itemView.findViewById(R.id.chat_owner_name);
            vUserName = (TextView) itemView.findViewById(R.id.chat_user_name);
            vOwnerMessage = (TextView) itemView.findViewById(R.id.chat_owner_message);
            vUserMessage = (TextView) itemView.findViewById(R.id.chat_user_message);
            vOwnerTime = (TextView) itemView.findViewById(R.id.chat_owner_time);
            vUserTime = (TextView) itemView.findViewById(R.id.chat_user_time);
        }

        void bindModel(Chat chat) {
            if (aChatList.get(getAdapterPosition()).getOwnerId().equals(aSharedPreferences.getString("userID", null))) {
                vOwnerLinear.setVisibility(View.VISIBLE);
                vUserRelative.setVisibility(View.GONE);

                Glide.with(aContext)
                        .load(Utils.getImageUrlOfUser(chat.getUserImage()))
                        .asBitmap()
                        .signature(new StringSignature(aSharedPreferences.getString("imageSigniture", "000")))
                        .placeholder(R.drawable.profile_picture_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                        .into(vOwnerImage);

                vOwnerName.setText(chat.getUserName());
                vOwnerMessage.setText(chat.getMessage());
                vOwnerTime.setText(chat.getShortDate());
            } else {
                vOwnerLinear.setVisibility(View.GONE);
                vUserRelative.setVisibility(View.VISIBLE);

                Glide.with(aContext)
                        .load(Utils.getImageUrlOfUser(chat.getUserImage()))
                        .asBitmap()
                        .signature(new StringSignature(aSharedPreferences.getString("imageSigniture", "000")))
                        .placeholder(R.drawable.profile_picture_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                        .into(vUserImage);

                vUserName.setText(chat.getUserName());
                vUserMessage.setText(chat.getMessage());
                vUserTime.setText(chat.getShortDate());
            }
        }
    }
}
