package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;

import static com.linute.linute.MainContent.DiscoverFragment.Post.POST_TYPE_IMAGE;
import static com.linute.linute.MainContent.DiscoverFragment.Post.POST_TYPE_VIDEO;

/**
 * Created by Arman on 1/20/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context aContext;
    private List<Chat> aChatList;
    private static final DateFormat mDateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    private LoadMoreViewHolder.OnLoadMore mLoadMoreListener;
    private short mFooterState = LoadMoreViewHolder.STATE_LOADING;

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
                return new ChatViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_chat_list_item_me, parent, false));
            case Chat.TYPE_MESSAGE_OTHER_PERSON:
                return  new ChatViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_chat_list_item_you, parent, false));

            case Chat.TYPE_ACTION_TYPING:
                return new ChatActionHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.fragment_chat_list_item_action_typing, parent, false));
            case LoadMoreViewHolder.FOOTER:
                return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wrapping_footer_light, parent, false),
                        "", ""
                );
            case Chat.TYPE_DATE_HEADER:
                return new DateHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_list_item_date_header, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatViewHolder) {
            ((ChatViewHolder) holder).bindModel(aChatList.get(position - 1));
        }else if (holder instanceof LoadMoreViewHolder){
            if (mLoadMoreListener != null) mLoadMoreListener.loadMore();
            ((LoadMoreViewHolder) holder).bindView(mFooterState);
        }else if(holder instanceof  DateHeaderHolder){
            ((DateHeaderHolder)holder).dateTV.setText(aChatList.get(position-1).getMessage());
        }
    }

    public void setLoadMoreListener(LoadMoreViewHolder.OnLoadMore l) {
        mLoadMoreListener = l;
    }

    //+1 for load more loader
    @Override
    public int getItemCount() {
        return aChatList.size() == 0 ? 0 : aChatList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? LoadMoreViewHolder.FOOTER : aChatList.get(position - 1).getType();
    }

    public void setFooterState(short footerState) {
        mFooterState = footerState;
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
                    BaseTaptActivity activity = (BaseTaptActivity) aContext;
                    if (activity != null) {
                        if (mType == Chat.MESSAGE_IMAGE) {
                            activity.addFragmentOnTop(
                                    ViewFullScreenFragment.newInstance(
                                            Uri.parse(Utils.getMessageImageURL(mUrl)),
                                            POST_TYPE_IMAGE
                                    )
                            );
                        } else if (mType == Chat.MESSAGE_VIDEO) {
                           activity.addFragmentOnTop(
                                   ViewFullScreenFragment.newInstance(
                                           Uri.parse(Utils.getMessageVideoURL(mUrl)),
                                           POST_TYPE_VIDEO
                                   )
                           );
                        }
                    }
                }
            });
        }

        void bindModel(Chat chat) {
            mType = chat.getMessageType();

            switch (chat.getMessageType()) {
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


            if (chat.getType() == Chat.TYPE_MESSAGE_ME) {
                vActionImage.setImageResource(chat.isRead() ? R.drawable.ic_chat_read : R.drawable.delivered_chat);
            }

            if (chat.getDate() != null) {
                vUserTime.setText(
                        /*new Date().getTime() - chat.getDate().getTime() > DateUtils.DAY_IN_MILLIS ?
                        mLongFormat.format(chat.getDate()) :
                        mDateFormat.format(chat.getDate())*/
                        mDateFormat.format(chat.getDate())
                );
            }
        }


        private void setImage(String image) {
            Glide.with(aContext)
                    .load(Utils.getMessageImageURL(image))
                    .dontAnimate()
                    .placeholder(R.drawable.chat_backgrounds)
                    .into(vImage);
        }
    }

    public class DateHeaderHolder extends RecyclerView.ViewHolder{
        TextView dateTV;

        public DateHeaderHolder(View itemView) {
            super(itemView);
            dateTV = (TextView)itemView.findViewById(R.id.text_date);
        }
    }


    public class ChatActionHolder extends RecyclerView.ViewHolder {
        public ChatActionHolder(View itemView) {
            super(itemView);
        }
    }
}
