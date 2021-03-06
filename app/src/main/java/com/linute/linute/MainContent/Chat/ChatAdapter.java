package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;
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

    private String mUserId;

    private boolean isDM = false;

    private RequestManager mRequestManager;

    private Map<String, User> mUsers;

    private static final int MESSAGE_ME_MEDIA = 20;
    private static final int MESSAGE_OTHER_PERSON_MEDIA = 21;

    static {
        mDateFormat.setTimeZone(TimeZone.getDefault());
    }

    //private HashMap<Integer, ArrayList<ChatHead>> aChatHeadsMap;
    //private SharedPreferences aSharedPreferences;

    public ChatAdapter(Context aContext, List<Chat> aChatList, Map<String, User> users) {
        this.aContext = aContext;
        this.aChatList = aChatList;
        this.mUsers = users;

        mUserId = aContext.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", "");
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    public void setIsDM(boolean isdm) {
        this.isDM = isdm;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Chat.TYPE_MESSAGE_ME:
            case MESSAGE_ME_MEDIA:
                return new ChatViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_chat_list_item_me, parent, false));
            case Chat.TYPE_MESSAGE_OTHER_PERSON:
            case MESSAGE_OTHER_PERSON_MEDIA:
                return new ChatViewHolder(LayoutInflater.from(parent.getContext())
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
            case Chat.TYPE_SYSTEM_MESSAGE:
                return new DateHeaderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chat_list_item_date_header, parent, false));

        }


        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatViewHolder) {
            Chat chat = aChatList.get(position - 1);
            ((ChatViewHolder) holder).bindModel(chat, isHead(position - 1));
        } else if (holder instanceof LoadMoreViewHolder) {
            if (mLoadMoreListener != null) mLoadMoreListener.loadMore();
            ((LoadMoreViewHolder) holder).bindView(mFooterState);
        } else if (holder instanceof DateHeaderHolder) {
            ((DateHeaderHolder) holder).dateTV.setText(aChatList.get(position - 1).getMessage());
        }
    }

    public void setLoadMoreListener(LoadMoreViewHolder.OnLoadMore l) {
        mLoadMoreListener = l;
    }

    //+1 for load more loader
    @Override
    public int getItemCount() {
        return aChatList.size() == 0 ? 0 : aChatList.size() + 1;

    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) return LoadMoreViewHolder.FOOTER;
        else{
            int type = aChatList.get(position - 1).getType();
            int mtype = aChatList.get(position - 1).getMessageType();
            if(type == Chat.TYPE_MESSAGE_ME){
                if(mtype == Chat.MESSAGE_TEXT){
                    return Chat.TYPE_MESSAGE_ME;
                }else{
                    return MESSAGE_ME_MEDIA;
                }
            }else if(type == Chat.TYPE_MESSAGE_OTHER_PERSON){
                if(mtype == Chat.MESSAGE_TEXT){
                    return Chat.TYPE_MESSAGE_OTHER_PERSON;
                }else{
                    return MESSAGE_OTHER_PERSON_MEDIA;
                }
            }else{
                return type;
            }
        }
    }

    public boolean isHead(int position) {
        if (position == 0) return true;
        Chat chat1 = aChatList.get(position);
        Chat chat2 = aChatList.get(position - 1);
        return !chat2.getOwnerId().equals(chat1.getOwnerId()) || chat2.getType() == Chat.TYPE_DATE_HEADER;
    }

    public void setFooterState(short footerState) {
        mFooterState = footerState;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        protected TextView vUserMessage;
        protected TextView vUserTime;
        protected ImageView vActionImage;
        protected ImageView vReadReceipt;
        protected ImageView vProfileImage;
        protected TextView vUserName;

        protected ImageView vImage;
        protected View vFrame;
        protected View vLikeBar;

        protected View vDeleted;

        protected View vMessageBubble;

        private int mType;
        private String mUrl;

        private Post mPost;

        public ChatViewHolder(View itemView) {
            super(itemView);
            vUserMessage = (TextView) itemView.findViewById(R.id.chat_user_message);
            vUserTime = (TextView) itemView.findViewById(R.id.chat_user_time);
            vActionImage = (ImageView) itemView.findViewById(R.id.message_action_icon);
            vReadReceipt = (ImageView) itemView.findViewById(R.id.read_receipt);
            vFrame = itemView.findViewById(R.id.frame);
            vImage = (ImageView) itemView.findViewById(R.id.image);
            vProfileImage = (ImageView) itemView.findViewById(R.id.profile_image);
            vDeleted = itemView.findViewById(R.id.deleted);

            vUserName = (TextView) itemView.findViewById(R.id.user_name);
            vLikeBar = vFrame.findViewById(R.id.action_bar);

            final ToggleImageView checkbox = (ToggleImageView) vLikeBar.findViewById(R.id.checkbox);
            checkbox.setImageViews(R.drawable.ic_fire_off, R.drawable.ic_fire);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likePost(checkbox);
                }
            });
            vMessageBubble = itemView.findViewById(R.id.message_content);


            vImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageClicked();
                }
            });

            vLikeBar.findViewById(R.id.post_profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPost != null && mPost.getPrivacy() == 0)
                        goToProfile(mPost.getUserName(), mPost.getUserId());
                }
            });

            vLikeBar.findViewById(R.id.post_name).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPost != null && mPost.getPrivacy() == 0)
                        goToProfile(mPost.getUserName(), mPost.getUserId());
                }
            });
        }


        private void likePost(ToggleImageView checkbox) {
            BaseTaptActivity activity = (BaseTaptActivity) aContext;
            if (activity != null && mPost != null) {
                try {
                    boolean emit = false;

                    if (checkbox.isActive() && mPost.isPostLiked()) {
                        checkbox.setActive(false);
                        mPost.setPostLiked(false);
                        emit = true;
                    } else if (!checkbox.isActive() && !mPost.isPostLiked()) {
                        checkbox.setActive(true);
                        mPost.setPostLiked(true);
                        emit = true;
                    }

                    if (emit) {
                        JSONObject body = new JSONObject();
                        body.put("user", mUserId);
                        body.put("room", mPost.getId());
                        TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:like", body);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        private void imageClicked() {
            if (mPost != null) {
                ((BaseTaptActivity) aContext).addFragmentToContainer(
                        FeedDetailPage.newInstance(mPost),
                        "full_view"
                );
            } else {
                int type = -1;

                if (mType == Chat.MESSAGE_IMAGE)
                    type = POST_TYPE_IMAGE;
                else if (mType == Chat.MESSAGE_VIDEO)
                    type = POST_TYPE_VIDEO;

                if (type != -1) {
                    ((BaseTaptActivity) aContext).addFragmentOnTop(
                            ViewFullScreenFragment.newInstance(
                                    Uri.parse(mUrl),
                                    type,
                                    0
                            ),
                            "full_view"
                    );
                }
            }
        }


        private void goToProfile(String name, String id) {
            ((BaseTaptActivity) aContext).addFragmentToContainer(
                    TaptUserProfileFragment.newInstance(name, id),
                    "full_view"
            );
        }

        void bindModel(Chat chat, boolean isHead) {
            mType = chat.getMessageType();

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();


            switch (chat.getState()) {
                case Pending:
                    itemView.setAlpha(.5f);
                    break;
                default:
                    itemView.setAlpha(1);
            }

            if (chat.getType() == Chat.TYPE_MESSAGE_OTHER_PERSON) {
                if (isHead) {

                    params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, itemView.getResources().getDisplayMetrics());

                    final User u = mUsers.get(chat.getOwnerId());
                    if (u != null) {
                        if (!isDM) {
                            vUserName.setVisibility(View.VISIBLE);
                            vUserName.setText(u.firstName);
                        } else {
                            vUserName.setVisibility(View.GONE);
                        }
                        vProfileImage.setVisibility(View.VISIBLE);
                        // Log.i("TEST", "bindModel: "+u.userImage);
                        mRequestManager
                                .load(Utils.getImageUrlOfUser(u.userImage))
                                .asBitmap()
                                .placeholder(R.color.seperator_color)
                                .into(vProfileImage);

                        vProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                goToProfile(u.getFullName(), u.userId);
                            }
                        });
                    } else {
                        vUserName.setVisibility(View.GONE);
                        vProfileImage.setVisibility(View.INVISIBLE);
                    }
                } else {
                    params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, itemView.getResources().getDisplayMetrics());
                    vUserName.setVisibility(View.GONE);
                    vProfileImage.setVisibility(View.INVISIBLE);
                }

            }


            switch (chat.getMessageType()) {
                case Chat.MESSAGE_SHARE_IMAGE:
                    mPost = chat.getPost();
                    vUserMessage.setVisibility(View.GONE);

                    if (mPost.isDeleted()) {
                        vFrame.setVisibility(View.GONE);
                        vDeleted.setVisibility(View.VISIBLE);
                    } else {
                        vDeleted.setVisibility(View.GONE);
                        vFrame.setVisibility(View.VISIBLE);
                        vFrame.findViewById(R.id.cinema_icon).setVisibility(View.GONE);
                        showShareStuff(chat.getPost());
                        setImage(mPost.getImage());
                        mUrl = mPost.getImage();
                    }
                    break;
                case Chat.MESSAGE_SHARE_VIDEO:
                    mPost = chat.getPost();
                    vUserMessage.setVisibility(View.GONE);

                    if (mPost.isDeleted()) {
                        vFrame.setVisibility(View.GONE);
                        vDeleted.setVisibility(View.VISIBLE);
                    } else {
                        vFrame.setVisibility(View.VISIBLE);
                        vDeleted.setVisibility(View.GONE);
                        vFrame.findViewById(R.id.cinema_icon).setVisibility(View.VISIBLE);
                        showShareStuff(chat.getPost());
                        setImage(mPost.getImage());
                        mUrl = mPost.getVideoUrl();
                    }
                    break;
                case Chat.MESSAGE_IMAGE:
                    vFrame.setVisibility(View.VISIBLE);
                    vUserMessage.setVisibility(View.GONE);
                    vDeleted.setVisibility(View.GONE);
                    vFrame.findViewById(R.id.cinema_icon).setVisibility(View.GONE);
                    hideShareStuff();
                    mUrl = Utils.getMessageImageURL(chat.getImageId());
                    setImage(mUrl);
                    mPost = null;
                    break;
                case Chat.MESSAGE_VIDEO:
                    vFrame.setVisibility(View.VISIBLE);
                    vUserMessage.setVisibility(View.GONE);
                    vDeleted.setVisibility(View.GONE);
                    vFrame.findViewById(R.id.cinema_icon).setVisibility(View.VISIBLE);
                    hideShareStuff();
                    setImage(Utils.getMessageImageURL(chat.getImageId()));
                    mUrl = Utils.getMessageVideoURL(chat.getVideoId());
                    mPost = null;
                    break;
                default:
                    vFrame.setVisibility(View.GONE);
                    vDeleted.setVisibility(View.GONE);
                    vUserMessage.setVisibility(View.VISIBLE);
                    vUserMessage.setText(chat.getMessage());
                    mUrl = "";
                    mPost = null;
                    break;
            }


            if (chat.getType() == Chat.TYPE_MESSAGE_ME) {
                vActionImage.setImageResource(chat.isRead() ? R.drawable.ic_chat_read : R.drawable.delivered_chat);
//                vReadReceipt.setImageResource(chat.isRead() ? R.drawable.chat_read_receipt_read : R.drawable.chat_read_receipt_delivered);
                params.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, isHead ? 4 : 1, itemView.getResources().getDisplayMetrics());

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

        private void hideShareStuff() {
            vLikeBar.setVisibility(View.GONE);
//            vImage.setCornerRadius(vImage.getCornerRadius(Corner.TOP_RIGHT));
        }

        private void showShareStuff(Post post) {
            vLikeBar.setVisibility(View.VISIBLE);

//            vImage.setCornerRadius(Corner.BOTTOM_RIGHT, 0);
//            vImage.setCornerRadius(Corner.BOTTOM_LEFT, 0);

            String url;
            String name;

            if (post.getPrivacy() == 1) {
                url = post.getAnonImage();
                name = "Anonymous";
            } else {
                url = post.getUserImage();
                name = post.getUserName();
            }

            mRequestManager
                    .load(url)
                    .asBitmap()
                    .dontAnimate()
                    .placeholder(R.color.seperator_color)
                    .into((ImageView) vLikeBar.findViewById(R.id.post_profile));

            ((TextView) vLikeBar.findViewById(R.id.post_name)).setText(name);
            ((ToggleImageView) vLikeBar.findViewById(R.id.checkbox)).setActive(post.isPostLiked());
        }

        private void setImage(String image) {
            vImage.setImageResource(android.R.color.transparent);
            mRequestManager
                    .load(image)
                    .asBitmap()
                    //.centerCrop()
                    .placeholder(R.color.seperator_color)
                    .into(vImage);
        }
    }

    public class DateHeaderHolder extends RecyclerView.ViewHolder {
        TextView dateTV;

        public DateHeaderHolder(View itemView) {
            super(itemView);
            dateTV = (TextView) itemView.findViewById(R.id.text_date);
        }
    }


    public class ChatActionHolder extends RecyclerView.ViewHolder {
        public ChatActionHolder(View itemView) {
            super(itemView);

            //loads gif
            mRequestManager
                    .load(R.drawable.typing_three_dots_android)
                    .into(new GlideDrawableImageViewTarget((ImageView) itemView.findViewById(R.id.gif)));
        }
    }
}
