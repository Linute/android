package com.linute.linute.MainContent.FeedDetailFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Arman on 1/13/16.
 */
public class FeedDetailAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> {
    private static final int TYPE_IMAGE_HEADER = 0;
    private static final int TYPE_STATUS_HEADER = 1;
    private static final int TYPE_ITEM = 2;
    private static final int TYPE_NO_COMMENTS = 3;
    private static final int TYPE_VIDEO_HEADER = 4;
    private static final int TYPE_LOAD_MORE = 5;

    private Context context;

    private FeedDetail mFeedDetail;

    private MentionedTextAdder mMentionedTextAdder;
    private CommentActions mCommentActions;

    private String mViewerUserId; //userId of person currently viewing page
    private String mImageSignature;

    public FeedDetailAdapter(FeedDetail feedDetail, Context context) {
        this.context = context;
        mFeedDetail = feedDetail;


        SharedPreferences mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mViewerUserId = mSharedPreferences.getString("userID", "");
        mImageSignature = mSharedPreferences.getString("imageSigniture", "000");
    }

    public void setCommentActions(CommentActions actions) {
        mCommentActions = actions;
    }

    public boolean getDenySwipe() {
        return mDenySwipe;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_ITEM:
                return new FeedDetailViewHolder(LayoutInflater.
                        from(parent.getContext()).
                        inflate(R.layout.fragment_feed_detail_page_list_item, parent, false));
            case TYPE_IMAGE_HEADER:
                return new FeedDetailHeaderImageViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.feed_detail_header_image, parent, false), context);
            case TYPE_STATUS_HEADER:
                return new FeedDetailHeaderStatusViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.feed_detail_header_status, parent, false), context);
            case TYPE_VIDEO_HEADER:
                return new FeedDetailHeaderVideoViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.feed_detail_header_video, parent, false), context);
            case TYPE_LOAD_MORE:
                return new LoadMoreViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.feed_detail_load, parent, false));
            case TYPE_NO_COMMENTS:
                return new NoCommentsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_comments_item, parent, false));
        }

        return new NoCommentsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_comments_item, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).bindView((LoadMoreItem) mFeedDetail.getComments().get(0));

        } else if (holder instanceof FeedDetailViewHolder) {
            ((FeedDetailViewHolder) holder).bindModel((Comment) mFeedDetail.getComments().get(position - 1));
            mItemManger.bindView(holder.itemView, position);
        } else if (holder instanceof FeedDetailHeaderImageViewHolder) {
            ((FeedDetailHeaderImageViewHolder) holder).bindModel(mFeedDetail.getPost());
        } else if (holder instanceof FeedDetailHeaderStatusViewHolder) {
            ((FeedDetailHeaderStatusViewHolder) holder).bindModel(mFeedDetail.getPost());
        } else if (holder instanceof FeedDetailHeaderVideoViewHolder) {
            ((FeedDetailHeaderVideoViewHolder) holder).bindModel(mFeedDetail.getPost());
        }
    }

    @Override
    public int getItemCount() {
        return mFeedDetail.getComments().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            if (mFeedDetail.getPost().isImagePost()) {
                if (mFeedDetail.getPost().isVideoPost()) return TYPE_VIDEO_HEADER;
                return TYPE_IMAGE_HEADER;
            }
            return TYPE_STATUS_HEADER;
        }

        if (mFeedDetail.getComments().get(position - 1) == null)  //first item is null, means no comments
            return TYPE_NO_COMMENTS;

        if (mFeedDetail.getComments().get(position - 1) instanceof Comment) {
            return TYPE_ITEM;
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

    private boolean mDenySwipe = false;

    //holder for comments
    public class FeedDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private SwipeLayout mSwipeLayout;

        protected CircleImageView vCommentUserImage;
        protected TextView vCommentUserName;
        protected TextView vCommentUserText;
        protected TextView vTimeStamp;

        protected ImageView vLikeIcon;
        protected TextView vLikesText;

        private String mCommenterUserId;
        private String mUserName;
        private String mCommentId;
        private boolean mIsAnon;
        private boolean mIsLiked;


        public FeedDetailViewHolder(View itemView) {
            super(itemView);
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

            vCommentUserImage = (CircleImageView) itemView.findViewById(R.id.comment_user_image);
            vCommentUserName = (TextView) itemView.findViewById(R.id.comment_user_name);
            vCommentUserText = (TextView) itemView.findViewById(R.id.comment);
            vTimeStamp = (TextView) itemView.findViewById(R.id.comment_time_ago);
            vLikesText = (TextView) itemView.findViewById(R.id.num_likes);

            vCommentUserName.setOnClickListener(this);
            vCommentUserImage.setOnClickListener(this);
            rightControls.findViewById(R.id.comment_delete).setOnClickListener(this);
            rightControls.findViewById(R.id.comment_reply).setOnClickListener(this);
            rightControls.findViewById(R.id.comment_reveal).setOnClickListener(this);
            rightControls.findViewById(R.id.comment_report).setOnClickListener(this);
            leftControls.setOnClickListener(this);

            vLikeIcon = (ImageView) leftControls.findViewById(R.id.like);
            leftControls.setOnClickListener(this);
        }

        void bindModel(Comment comment) {
            mIsAnon = comment.isAnon();
            mCommenterUserId = comment.getCommentUserId();
            mUserName = comment.getCommentUserName();
            mCommentId = comment.getCommentPostId();
            mIsLiked = comment.isLiked();

            if (mIsAnon) {
                setAnonImage(comment.getAnonImage());
                vCommentUserName.setText("Anonymous");
            } else {
                setProfileImage(comment.getCommentUserProfileImage());
                vCommentUserName.setText(comment.getCommentUserName());
            }

            setUpPulloutButtons();

            vTimeStamp.setText(comment.getDateString());

            if (mCommenterUserId.equals(mViewerUserId)) {
                vCommentUserName.setTextColor(ContextCompat.getColor(context, R.color.user_comment_color));
            } else {
                vCommentUserName.setTextColor(ContextCompat.getColor(context, R.color.user_name_blue));
            }

            //setting mentions and comment text
            if (comment.getMentionedPeople() != null && !comment.getMentionedPeople().isEmpty()) {
                setUpMentionedOnClicks(comment);
            } else { //set text to comment's text
                vCommentUserText.setText(comment.getCommentPostText());
            }

            vLikeIcon.setImageResource(mIsLiked ? R.drawable.ic_fire_on : R.drawable.ic_fire_off);
            vLikesText.setText(comment.getNumberOfLikes() + "");
        }

        private void setUpPulloutButtons() {
            if (mViewerUserId.equals(mCommenterUserId)) {
                mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(
                        mFeedDetail.getPost().isCommentAnonDisabled() ? View.GONE : View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.GONE);
            } else {
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.VISIBLE);

                if (mIsAnon) { //comment is anonymous
                    mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);

                    //viewer is owner of post? then can delete anon comments
                    if (mFeedDetail.getPost().getUserId().equals(mViewerUserId)) {
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

        private void setUpMentionedOnClicks(Comment comment) {
            String commentText = comment.getCommentPostText();
            SpannableString commentSpannable = new SpannableString(commentText);

            int startSearchAtIndex = 0; //start search from

            // assumes the list comes back in the order people were tagged.
            // i.e. if text is: "@AndrewBee @JonathanI hello there" , the List will have the order {Andrew, Jonathan}
            // this way we won't look for @JonathanI at index 0 when we know it will come after @Andrew

            final BaseTaptActivity activity = (BaseTaptActivity) context;
            if (activity == null) {
                vCommentUserText.setText(comment.getCommentPostText());
                return;
            }

            ForegroundColorSpan fcs = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.secondaryColor)); //color of span

            for (final Comment.MentionedPersonLight person : comment.getMentionedPeople()) {
                int start = commentText.indexOf(person.getFormatedFullName(), startSearchAtIndex);

                if (start != -1) { //-1 if string not found

                    int end = person.getFormatedFullName().length() + start;
                    startSearchAtIndex = end; //next mention will come after the end of this one

                    ClickableSpan clickableSpan = new ClickableSpan() { //what happens when clicked
                        @Override
                        public void onClick(View widget) {
                            activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(person.getFullName(), person.getId()));
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                        }
                    };

                    commentSpannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    commentSpannable.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            vCommentUserText.setText(commentSpannable);
            vCommentUserText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @Override
        public void onClick(View v) {
            if (v == vCommentUserName || v == vCommentUserImage) {
                if (!mIsAnon) {
                    BaseTaptActivity activity = (BaseTaptActivity) context;
                    if (activity != null) {
                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mUserName, mCommenterUserId));
                    }
                }
            } else {
                if (mDenySwipe || mCommentActions == null) return;
                switch (v.getId()) {
                    case R.id.comment_reply:
                        mMentionedTextAdder.addMentionedPerson(new MentionedPerson(mUserName, mCommenterUserId, ""));
                        break;
                    case R.id.comment_delete:
                        mCommentActions.deleteComment(getAdapterPosition(), mCommentId);
                        break;
                    case R.id.comment_report:
                        mCommentActions.reportComment(mCommentId);
                        break;
                    case R.id.comment_reveal:
                        mCommentActions.revealComment(getAdapterPosition(), mCommentId, mIsAnon);
                        break;
                    case R.id.left_controls:
                        Comment c = (Comment)mFeedDetail.getComments().get(getAdapterPosition() - 1);
                        mIsLiked = c.toggleLiked();
                        if (mIsLiked){
                            c.incrementLikes();
                            vLikeIcon.setImageResource(R.drawable.ic_fire_on);
                            vLikesText.setText(c.getNumberOfLikes() + "");
                        } else{
                            c.decrementLikes();
                            vLikeIcon.setImageResource(R.drawable.ic_fire_off);
                            vLikesText.setText(c.getNumberOfLikes()+ "");
                        }

                        mCommentActions.likeComment(mIsLiked, mCommentId);
                        return;
                }
                mSwipeLayout.close();
            }
        }





        public void setProfileImage(String image) {
            Glide.with(context)
                    .load(Utils.getImageUrlOfUser(image))
                    .asBitmap()
                    .signature(new StringSignature(mImageSignature))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vCommentUserImage);
        }

        public void setAnonImage(String image) {
            Glide.with(context)
                    .load(image == null || image.equals("") ? R.drawable.profile_picture_placeholder : Utils.getAnonImageUrl(image))
                    .asBitmap()
                    .signature(new StringSignature(mImageSignature))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .into(vCommentUserImage);
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
        void addMentionedPerson(MentionedPerson person);
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
