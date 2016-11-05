package com.linute.linute.MainContent.FeedDetailFragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.linute.linute.MainContent.DiscoverFragment.Poll;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleTouchListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.makeramen.roundedimageview.RoundedImageView;

import static android.text.util.Linkify.EMAIL_ADDRESSES;
import static android.text.util.Linkify.WEB_URLS;
import static com.linute.linute.MainContent.DiscoverFragment.Post.POST_TYPE_IMAGE;

/**
 * Created by Arman on 1/13/16.
 */

public class FeedDetailAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {
    private static final int TYPE_IMAGE_HEADER = 0;
    private static final int TYPE_STATUS_HEADER = 1;
    private static final int TYPE_COMMENT_TEXT = 2;
    private static final int TYPE_COMMENT_IMAGE = 3;
    private static final int TYPE_NO_COMMENTS = 4;
    private static final int TYPE_VIDEO_HEADER = 5;
    private static final int TYPE_LOAD_MORE = 6;
    private static final int TYPE_POLL_HEADER = 7;

    private Context context;

    private BaseFeedDetail mFeedDetail;

    private MentionedTextAdder mMentionedTextAdder;
    private CommentActions mCommentActions;

    private String mViewerUserId; //userId of person currently viewing page
    private String mImageSignature;

    private RequestManager mRequestManager;

    private int contextMenuPosition = -1;
    private String contextMenuId = null;

    public FeedDetailAdapter(BaseFeedDetail feedDetail, RequestManager manager, Context context) {
        this.context = context;
        mFeedDetail = feedDetail;
        mRequestManager = manager;
        SharedPreferences mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mViewerUserId = mSharedPreferences.getString("userID", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public void setRequestManager(RequestManager requestManager) {
        mRequestManager = requestManager;
    }

    public void setCommentActions(CommentActions actions) {
        mCommentActions = actions;
    }

    public boolean getDenySwipe() {
        return mDenySwipe;
    }

    public int getContextMenuPosition() {
        return contextMenuPosition;
    }

    public String getContextMenuCommentId() {
        return contextMenuId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_COMMENT_TEXT:
                return new FeedDetailViewHolderText(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.fragment_feed_detail_page_list_item, parent, false));
            case TYPE_COMMENT_IMAGE:
                return new FeedDetailViewHolderImage(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.fragment_feed_detail_page_list_item, parent, false));
            case TYPE_IMAGE_HEADER:
                return new FeedDetailHeaderImageViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.feed_detail_header_image, parent, false),
                        context,
                        mRequestManager);
            case TYPE_STATUS_HEADER:
                return new FeedDetailHeaderStatusViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.feed_detail_header_status, parent, false),
                        context,
                        mRequestManager);
            case TYPE_VIDEO_HEADER:
                return new FeedDetailHeaderVideoViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.feed_detail_header_video, parent, false),
                        context,
                        mRequestManager);
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.feed_detail_load, parent, false));
            case TYPE_POLL_HEADER:
                return  new FeedDetailHeaderPollViewHolder(
                        LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.feed_detail_header_poll, parent, false));
            case TYPE_NO_COMMENTS:
                return new NoCommentsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_comments_item, parent, false));
        }

        return new NoCommentsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_comments_item, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView((LoadMoreItem) mFeedDetail.getComments().get(0));
        } else if (holder instanceof BaseFeedDetailViewHolder) {
            ((BaseFeedDetailViewHolder) holder).bindModel((Comment) mFeedDetail.getComments().get(position - 1));
            mItemManger.bindView(holder.itemView, position);
        } else if (holder instanceof FeedDetailHeaderImageViewHolder) {
            ((FeedDetailHeaderImageViewHolder) holder).bindModel((Post) mFeedDetail.getFeedItem());
        } else if (holder instanceof FeedDetailHeaderStatusViewHolder) {
            ((FeedDetailHeaderStatusViewHolder) holder).bindModel((Post) mFeedDetail.getFeedItem());
        } else if (holder instanceof FeedDetailHeaderVideoViewHolder) {
            ((FeedDetailHeaderVideoViewHolder) holder).bindModel((Post) mFeedDetail.getFeedItem());
        } else if (holder instanceof FeedDetailHeaderPollViewHolder){
            ((FeedDetailHeaderPollViewHolder) holder).bindView((Poll) mFeedDetail.getFeedItem());
        }
    }

    @Override
    public int getItemCount() {
        return mFeedDetail.getComments().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            if (mFeedDetail instanceof PostFeedDetail) {
                Post p = (Post) mFeedDetail.getFeedItem();
                if (p.isImagePost()) {
                    if (p.isVideoPost()) return TYPE_VIDEO_HEADER;
                    return TYPE_IMAGE_HEADER;
                }
                return TYPE_STATUS_HEADER;
            }
            else if (mFeedDetail instanceof PollFeedDetail){
                return TYPE_POLL_HEADER;
            }
        }

        if (mFeedDetail.getComments().get(position - 1) == null)  //first item is null, means no comments
            return TYPE_NO_COMMENTS;

        if (mFeedDetail.getComments().get(position - 1) instanceof Comment) {
            if (((Comment) mFeedDetail.getComments().get(position - 1)).getType() == Comment.COMMENT_IMAGE)
                return TYPE_COMMENT_IMAGE;
            else
                return TYPE_COMMENT_TEXT;

        } else {
            return TYPE_LOAD_MORE;
        }
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.comment_swipe_layout;
    }


    public void setMentionedTextAdder(MentionedTextAdder mentioned) {
        mMentionedTextAdder = mentioned;
    }

    public void setDenySwipe(boolean deny) {
        mDenySwipe = deny;
    }

    public void clearContext() {
        context = null;
    }

    private boolean mDenySwipe = false;

    //holder for comments
    public abstract class BaseFeedDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private SwipeLayout mSwipeLayout;

        protected ImageView vCommentUserImage;
        protected View vPrivacyChanged;
        protected TextView vCommentUserName;
        protected TextView vTimeStamp;
        protected TextView vLikesText;
        private ImageView vFireIcon;

        protected Comment mComment;
        private final View vTopLayer;

        protected abstract void bindContent(Comment comment);

        public BaseFeedDetailViewHolder(View itemView) {
            super(itemView);

            vTopLayer = itemView.findViewById(R.id.feed_detail_hidden_animation);

            itemView.findViewById(R.id.feed_detail_touch).setOnTouchListener(new DoubleTouchListener(750) {
                @Override
                public void onDoubleTouch(int x, int y) {
                    boolean isLiked = mComment.toggleLiked();
                    if (isLiked) {
                        mComment.incrementLikes();
                        setUpLikes(mComment);
                    } else {
                        mComment.decrementLikes();
                        setUpLikes(mComment);
                    }

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        animateLollipop(vTopLayer,
                                (int) x,
                                (int) y,
                                (float) getMax(Math.hypot(x, y),
                                        Math.hypot(x, vTopLayer.getHeight() - y),
                                        Math.hypot(vTopLayer.getWidth() - x, y),
                                        Math.hypot(vTopLayer.getWidth() - x, vTopLayer.getHeight() - y)
                                ));
                    } else {
                        animatePreLollipop(vTopLayer);
                    }

                    mCommentActions.likeComment(isLiked, mComment.getCommentPostId());
                }
            });

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    contextMenuPosition = getAdapterPosition();
                    contextMenuId = mComment.getCommentPostId();

                    if (mComment.getCommentUserId() != null && mViewerUserId.equals(mComment.getCommentUserId())) {
                        menu.add(Menu.NONE, FeedDetailPage.MENU_DELETE, 0, "Delete");
                        if(mComment.isAnon()){
                            menu.add(Menu.NONE, FeedDetailPage.MENU_REVEAL, 0, "Reveal Yourself");
                        }else{
                            menu.add(Menu.NONE, FeedDetailPage.MENU_GO_ANON, 0, "Make Anon");
                        }
                    }else
                    if(mComment.isAnon()){
                        menu.add(Menu.NONE, FeedDetailPage.MENU_LIKE, 0, (mComment.isLiked() ? "Unlike" : "Like"));
                        menu.add(Menu.NONE, FeedDetailPage.MENU_REPORT, 0, "Report");
                    }else{
                        menu.add(Menu.NONE, FeedDetailPage.MENU_LIKE, 0, (mComment.isLiked() ? "Unlike" : "Like"));
                        menu.add(Menu.NONE, FeedDetailPage.MENU_REPORT, 0, "Report");
                        menu.add(Menu.NONE, FeedDetailPage.MENU_REPLY, 0, "Reply");
                    }
                }
            });




            mSwipeLayout = (SwipeLayout) itemView.findViewById(R.id.comment_swipe_layout);

            View leftControls = mSwipeLayout.findViewById(R.id.left_controls);
            View rightControls = mSwipeLayout.findViewById(R.id.right_controls);

            mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, rightControls);
            mSwipeLayout.addDrag(SwipeLayout.DragEdge.Left, leftControls);
            mSwipeLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mDenySwipe;
                }
            });

            vCommentUserImage = (ImageView) itemView.findViewById(R.id.comment_user_image);
            vCommentUserName = (TextView) itemView.findViewById(R.id.comment_user_name);
            vTimeStamp = (TextView) itemView.findViewById(R.id.comment_time_ago);
            vLikesText = (TextView) itemView.findViewById(R.id.num_likes);
            vFireIcon = (ImageView) itemView.findViewById(R.id.fire_icon);
            vPrivacyChanged = itemView.findViewById(R.id.privacy_changed);

            vCommentUserName.setOnClickListener(this);
            vCommentUserImage.setOnClickListener(this);
            rightControls.findViewById(R.id.comment_delete).setOnClickListener(this);
            rightControls.findViewById(R.id.comment_reply).setOnClickListener(this);
            rightControls.findViewById(R.id.comment_reveal).setOnClickListener(this);
            rightControls.findViewById(R.id.comment_report).setOnClickListener(this);
            leftControls.setOnClickListener(this);

            ((ImageView) leftControls.findViewById(R.id.like))
                    .setColorFilter(ContextCompat.getColor(context, R.color.pure_white));

        }


        void bindModel(Comment comment) {
            mComment = comment;
//            mIsAnon = comment.isAnon();
//            mCommenterUserId = comment.getCommentUserId();
//            mUserName = comment.getCommentUserName();
//            mCommentId = comment.getCommentPostId();
//            mIsLiked = comment.isLiked();

            //close when rebind
            mSwipeLayout.close(false);

            //if owner of comment, don't allow them to like
            mSwipeLayout.setLeftSwipeEnabled(comment.getCommentUserId() == null || !comment.getCommentUserId().equals(mViewerUserId));

            if (comment.isAnon()) {
                setAnonImage(comment.getAnonImage());
                vCommentUserName.setText("Anonymous");
            } else {
                setProfileImage(comment.getCommentUserProfileImage());
                vCommentUserName.setText(Utils.stripUnsupportedCharacters(comment.getCommentUserName()));
            }

            vPrivacyChanged.setVisibility(comment.hasPrivacyChanged ? View.VISIBLE : View.GONE);

            setUpPulloutButtons();

            vTimeStamp.setText(comment.getDateString());

            if (comment.getCommentUserId() != null && comment.getCommentUserId().equals(mViewerUserId)) {
                vCommentUserName.setTextColor(ContextCompat.getColor(context, R.color.user_comment_color));
            } else {
                vCommentUserName.setTextColor(ContextCompat.getColor(context, R.color.user_name_blue));
            }

            setUpLikes(comment);
            bindContent(comment);
        }

        private void setUpLikes(Comment comment) {
            if (comment.getNumberOfLikes() > 0) {
                vLikesText.setText(comment.getNumberOfLikes() + "");
                vFireIcon.setColorFilter(
                        comment.isLiked() ?
                                ContextCompat.getColor(context, R.color.red) :
                                ContextCompat.getColor(context, R.color.inactive_grey)
                );

                vLikesText.setVisibility(View.VISIBLE);
                vFireIcon.setVisibility(View.VISIBLE);
            } else {
                vLikesText.setText("");
                vLikesText.setVisibility(View.INVISIBLE);
                vFireIcon.setVisibility(View.INVISIBLE);
            }
        }

        private void setUpPulloutButtons() {
            if (mComment.getCommentUserId() != null && mViewerUserId.equals(mComment.getCommentUserId())) {
                mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(
                        mFeedDetail.isAnonCommentsDisabled() ? View.GONE : View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.GONE);
            } else {
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.VISIBLE);

                if (mComment.isAnon()) { //comment is anonymous
                    mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);

                    //viewer is owner of post? then can delete anon comments
                    if (mFeedDetail.getPostUserId() != null && mFeedDetail.getPostUserId().equals(mViewerUserId)) {
                        mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.VISIBLE);
                    } else {
                        mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.GONE);
                    }
                } else {
                    mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.VISIBLE);
                    mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.GONE);
                }
            }

        }

        private double getMax(double a, double b, double c, double d) {
            return Math.max(Math.max(a, b), Math.max(c, d));
        }


        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private void animateLollipop(final View v, int x, int y, float radius) {
            Animator animator = ViewAnimationUtils.createCircularReveal(v, x, y, 0, radius);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.animate().alpha(0).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            v.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    v.setVisibility(View.INVISIBLE);
                }
            });

            v.setVisibility(View.VISIBLE);
            animator.start();
        }

        private void animatePreLollipop(final View layer) {
            AlphaAnimation a = new AlphaAnimation(0.0f, 0.75f);
            a.setDuration(400);

            final AlphaAnimation a2 = new AlphaAnimation(0.75f, 0.0f);
            a2.setDuration(200);

            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    layer.startAnimation(a2);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            layer.startAnimation(a);
        }

        @Override
        public void onClick(View v) {
            if (v == vCommentUserName || v == vCommentUserImage) { //take them to user profile
                if (!mComment.isAnon() && mComment.getCommentUserId() != null) {
                    BaseTaptActivity activity = (BaseTaptActivity) context;
                    if (activity != null) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mComment.getCommentUserName(), mComment.getCommentUserId()));
                    }
                }
            } else {
                if (mDenySwipe || mCommentActions == null) return;

                mSwipeLayout.close();

                switch (v.getId()) {
                    case R.id.comment_reply:
                        if (!mComment.isAnon() && mComment.getCommentUserId() != null)
                            mMentionedTextAdder.addMentionedPerson(new MentionedPerson(mComment.getCommentUserName(), mComment.getCommentUserId(), ""), getAdapterPosition());
                        break;
                    case R.id.comment_delete:
                        mCommentActions.deleteComment(getAdapterPosition(), mComment.getCommentPostId());
                        break;
                    case R.id.comment_report:
                        mCommentActions.reportComment(mComment.getCommentPostId());
                        break;
                    case R.id.comment_reveal:
                        mCommentActions.revealComment(getAdapterPosition(), mComment.getCommentPostId(), mComment.isAnon());
                        break;
                    case R.id.left_controls:
                        boolean isLiked = mComment.toggleLiked();
                        if (isLiked) {
                            mComment.incrementLikes();
                            setUpLikes(mComment);
                        } else {
                            mComment.decrementLikes();
                            setUpLikes(mComment);
                        }

                        mCommentActions.likeComment(isLiked, mComment.getCommentPostId());
                }
            }
        }


        public void setProfileImage(String image) {
            mRequestManager
                    .load(Utils.getImageUrlOfUser(image))
                    .asBitmap()
                    .signature(new StringSignature(mImageSignature))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vCommentUserImage);
        }

        public void setAnonImage(String image) {
            mRequestManager
                    .load(image == null || image.isEmpty() ? R.drawable.profile_picture_placeholder : Utils.getAnonImageUrl(image))
                    .asBitmap()
                    .signature(new StringSignature(mImageSignature))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vCommentUserImage);
        }
    }

    public class FeedDetailViewHolderText extends BaseFeedDetailViewHolder {

        protected TextView vCommentText;
        protected SpannableStringBuilder mSpannableStringBuilder;

        public FeedDetailViewHolderText(View itemView) {
            super(itemView);

            //TODO Viewholder should not setup text view, moev this to xml file or oncCeateViewholder
            vCommentText = new TextView(context);
            vCommentText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            vCommentText.setAutoLinkMask(WEB_URLS | EMAIL_ADDRESSES);
            vCommentText.setTextColor(ContextCompat.getColor(context, R.color.eighty_black));
            vCommentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            vCommentText.setMovementMethod(LinkMovementMethod.getInstance());
            //vCommentText.setTextIsSelectable(true);
            ((ViewGroup) itemView.findViewById(R.id.content)).addView(vCommentText);
            mSpannableStringBuilder = new SpannableStringBuilder();

        }

        @Override
        protected void bindContent(Comment comment) {
            //setting mentions and comment text
            if (comment.getMentionedPeople() != null && !comment.getMentionedPeople().isEmpty()) {
                setUpMentionedOnClicks(comment);
            }else {
                vCommentText.setText(Utils.stripUnsupportedCharacters(comment.getCommentPostText()));
            }
        }



        private void setUpMentionedOnClicks(Comment comment) {

            String commentText = comment.getCommentPostText();
            mSpannableStringBuilder.clear();
            mSpannableStringBuilder.clearSpans();

            mSpannableStringBuilder.append(commentText);

            int startSearchAtIndex = 0; //start search from

            // assumes the list comes back in the order people were tagged.
            // i.e. if text is: "@AndrewBee @JonathanI hello there" , the List will have the order {Andrew, Jonathan}
            // this way we won't look for @JonathanI at index 0 when we know it will come after @Andrew

            final BaseTaptActivity activity = (BaseTaptActivity) context;
            if (activity == null) {
                vCommentText.setText(Utils.stripUnsupportedCharacters(comment.getCommentPostText()));
                return;
            }

            int index = 0;
            while (startSearchAtIndex + 1 < commentText.length()) {
                int start = commentText.indexOf('@', startSearchAtIndex);

                //doesn't exist
                if (start != -1) {
                    int end = commentText.indexOf(' ', start);
                    if (end == -1){
                        //mentions is last item in list, then there wont be a ' ' so set end to very end
                        if (start != commentText.length() - 1){
                            end = commentText.length();
                        }else {
                            break;
                        }
                    }
                    startSearchAtIndex = end + 1;
                    mSpannableStringBuilder.setSpan(new MentionClickSpan(commentText.substring(start, end), index), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    index++;

                }else {
                    break;
                }
            }

            vCommentText.setText(mSpannableStringBuilder);
        }




        private class MentionClickSpan extends ClickableSpan{
            private String mName;

            //keep track of index
            //for example, we have the text : "@Andi @John @Max
            //if mName is "@John", our index is 1
            //used for faster look ups
            private int mIndex;

            public MentionClickSpan(String name, int index){
                super();
                this.mName = name;
                this.mIndex = index;
            }

            @Override
            public void onClick(View widget) {
                //out of bounds
                if (mIndex  >= mComment.getMentionedPeople().size())
                    mIndex = 0;

                Comment.MentionedPersonLight personLight = mComment.getMentionedPeople().get(mIndex);
                if (mName.equals(personLight.getFormattedFullName())){ // was correct index
                    clickedName(personLight);
                }else { //incorrect index
                    for (int i = 0; i < mComment.getMentionedPeople().size(); i++){
                        personLight = mComment.getMentionedPeople().get(i);
                        if (mName.equals(personLight.getFormattedFullName())){
                            mIndex = i; //update for faster future look ups
                            clickedName(personLight);
                            return;
                        }
                    }
                }
            }

            public void clickedName(Comment.MentionedPersonLight person){
                BaseTaptActivity activity = (BaseTaptActivity) context;
                if (activity != null){
                    activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(person.getFullName(), person.getId()));
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) { //so name isn't underlined
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }
    }


    public class FeedDetailViewHolderImage extends BaseFeedDetailViewHolder {

        protected RoundedImageView vImageView;
        private String mImageUrl;


        public FeedDetailViewHolderImage(View itemView) {
            super(itemView);
            vImageView = new RoundedImageView(context);
            int size = (int) context.getResources().getDimension(R.dimen.comment_image_size);
            vImageView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
            vImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ((ViewGroup) itemView.findViewById(R.id.content)).addView(vImageView);

            vImageView.setCornerRadius(itemView.getResources().getDimensionPixelSize(R.dimen.message_bubble_corner_radius));

            vImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseTaptActivity activity = (BaseTaptActivity) context;
                    if (activity != null && mImageUrl != null) {
                        //full screen view
                        activity.addFragmentOnTop(
                                ViewFullScreenFragment.newInstance(
                                        Uri.parse(mImageUrl),
                                        POST_TYPE_IMAGE,
                                        0
                                ),
                                "full_view"
                        );
                    }
                }
            });

        }

        @Override
        protected void bindContent(Comment comment) {
            mImageUrl = Utils.getCommentImageUrl(comment.getImageUrl());
            mRequestManager.load(mImageUrl)
                    .asBitmap()
                    .placeholder(R.color.seperator_color)
                    .into(vImageView);
        }
    }


    private Runnable mLoadMoreCommentsRunnable;

    public void setLoadMoreCommentsRunnable(Runnable r) {
        mLoadMoreCommentsRunnable = r;
    }


    private class LoadMoreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View vLoadMoreText;
        View vLoadMoreProgressBar;
        LoadMoreItem mLoadMoreItem;


        public LoadMoreViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            vLoadMoreText = itemView.findViewById(R.id.load_more_text);
            vLoadMoreProgressBar = itemView.findViewById(R.id.load_more_progress_bar);
        }


        public void bindView(LoadMoreItem item) {
            mLoadMoreItem = item;
            if (item.isLoading()) {
                vLoadMoreProgressBar.setVisibility(View.VISIBLE);
                vLoadMoreText.setVisibility(View.INVISIBLE);
            } else {
                vLoadMoreProgressBar.setVisibility(View.INVISIBLE);
                vLoadMoreText.setVisibility(View.VISIBLE);
            }
        }


        @Override
        public void onClick(View v) {
            //don't allow load if currently loading
            if (!mDenySwipe && !mLoadMoreItem.isLoading() && mLoadMoreCommentsRunnable != null) {
                if (mCommentActions != null) mCommentActions.closeAllDialogs();
                vLoadMoreProgressBar.setVisibility(View.VISIBLE);
                vLoadMoreText.setVisibility(View.INVISIBLE);
                mLoadMoreItem.setLoading(true);
                mLoadMoreCommentsRunnable.run();
            }
        }
    }

    public interface MentionedTextAdder {
        void addMentionedPerson(MentionedPerson person, int pos);
    }

    public void closeAllItems() {
        mItemManger.closeAllItems();
    }


    public interface CommentActions {
        void deleteComment(final int pos, final String id);

        void revealComment(final int pos, final String id, final boolean isAnon);

        void reportComment(final String id);

        void likeComment(final boolean like, final String id);

        void closeAllDialogs();
    }

}
