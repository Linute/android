package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
    private Dialog mDialog;

    private FeedDetail mFeedDetail;

    private SingleVideoPlaybackManager mSingleVideoPlaybackManager;

    private MentionedTextAdder mMentionedTextAdder;

    public FeedDetailAdapter(FeedDetail feedDetail, Context context, SingleVideoPlaybackManager manager) {
        this.context = context;
        mFeedDetail = feedDetail;
        mSingleVideoPlaybackManager = manager;
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
                        .inflate(R.layout.feed_detail_header_video, parent, false), context, mSingleVideoPlaybackManager);
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

        if (mFeedDetail.getComments().get(position - 1) == null)  //first item is no, means no comments
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

    public FeedDetail getFeedDetail() {
        return mFeedDetail;
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
    public class FeedDetailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private SwipeLayout mSwipeLayout;

        protected CircleImageView vCommentUserImage;
        protected TextView vCommentUserName;
        protected TextView vCommentUserText;
        protected TextView vTimeStamp;

        private String mCommenterUserId;
        private String mUserName;
        private String mCommentId;

        private String mViewerUserId; //userId of person currently viewing page
        private String mImageSignature; //
        private boolean mIsAnon;


        public FeedDetailViewHolder(View itemView) {
            super(itemView);

            SharedPreferences mSharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

            mViewerUserId = mSharedPreferences.getString("userID", "");
            mImageSignature = mSharedPreferences.getString("imageSigniture", "000");

            mSwipeLayout = (SwipeLayout) itemView.findViewById(R.id.comment_swipe_layout);
            mSwipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, mSwipeLayout.findViewById(R.id.comment_bottom_wrapper));
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
        }

        void bindModel(Comment comment) {
            mIsAnon = comment.isAnon();
            mCommenterUserId = comment.getCommentUserId();
            mUserName = comment.getCommentUserName();
            mCommentId = comment.getCommentPostId();

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


            vCommentUserName.setOnClickListener(this);
            vCommentUserImage.setOnClickListener(this);
            mSwipeLayout.findViewById(R.id.comment_delete).setOnClickListener(this);
            mSwipeLayout.findViewById(R.id.comment_reply).setOnClickListener(this);
            mSwipeLayout.findViewById(R.id.comment_reveal).setOnClickListener(this);
            mSwipeLayout.findViewById(R.id.comment_report).setOnClickListener(this);

            //setting mentions and comment text
            if (comment.getMentionedPeople() != null && !comment.getMentionedPeople().isEmpty()) {
                setUpMentionedOnClicks(comment);
            } else { //set text to comment's text
                vCommentUserText.setText(comment.getCommentPostText());
            }

        }

        private void setUpPulloutButtons() {
            if (mViewerUserId.equals(mCommenterUserId)) {
                mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(
                        mFeedDetail.getPost().isCommentAnonDisabled() ? View.GONE : View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.GONE);
            } else {
                //mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.VISIBLE);

                if (mIsAnon) { //comment is anonymous
                    mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);

                    //viewer is owner of post? then can delete anon comments
                    if (mFeedDetail.getPost().getUserId().equals(mViewerUserId)){
                        mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.VISIBLE);
                    }else {
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
                if (mDenySwipe) return;
                switch (v.getId()) {
                    case R.id.comment_reply:
                        mMentionedTextAdder.addMentionedPerson(new MentionedPerson(mUserName, mCommenterUserId, ""));
                        break;
                    case R.id.comment_delete:
                        showConfirmDelete(getAdapterPosition());
                        break;
                    case R.id.comment_report:
                        showConfirmReportDialog();
                        break;
                    case R.id.comment_reveal:
                        showConfirmRevealDialog(getAdapterPosition());
                        break;
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


        private void showConfirmDelete(final int pos) {
            if (mDenySwipe) return;

            if (context != null) {
                mDialog = new AlertDialog.Builder(context).setTitle("Delete")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            final int mPos = pos;

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteComment(mPos);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }

        private void deleteComment(int in) {
            if (mDenySwipe) return;

            final int pos = in - 1;
            final Comment com = (Comment) mFeedDetail.getComments().get(pos);

            //if viewer is not the owner of the comment, return
            // exception: anon comments can be deleted by post owner
            if (!com.getCommentPostId().equals(mCommentId) || (!com.getCommentUserId().equals(mViewerUserId) && !com.isAnon())) return;

            mDenySwipe = true;
            final ProgressDialog progressDialog = ProgressDialog.show(context, null, "Deleting comment...", true, false);

            new LSDKEvents(context).deleteComment(mCommentId, new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    mDenySwipe = false;
                    progressDialog.dismiss();

                    final BaseTaptActivity act = (BaseTaptActivity) context;
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "Failed to delete comment. Could not find connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        response.body().close();

                        final BaseTaptActivity act = (BaseTaptActivity) context;

                        if (act != null){
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mItemManger.removeShownLayouts(mSwipeLayout);
                                    mFeedDetail.getComments().remove(pos);
                                    notifyItemRemoved(pos + 1);
                                    notifyItemRangeChanged(pos + 1, mFeedDetail.getComments().size() + 1);
                                    mFeedDetail.refreshCommentCount();

                                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.i("Comment item delete", "onResponse: " + response.body().string());
                        final BaseTaptActivity act = (BaseTaptActivity) context;

                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(context);
                                }
                            });
                        }
                    }

                    progressDialog.dismiss();
                    mDenySwipe = false;
                }
            });
        }

        private void showConfirmRevealDialog(final int pos) {

            if (context != null)
                mDialog = new AlertDialog.Builder(context).setTitle(mIsAnon ? "Reveal" : "Hide")
                        .setMessage(mIsAnon ? "Are you sure you want to turn anonymous off for this comment?" : "Are you sure you want to make this comment anonymous?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            final int mPos = pos;

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                revealComment(mPos);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
        }

        private void revealComment(final int in) {

            if (mDenySwipe) return;

            final int pos = in - 1;
            final Comment comment = (Comment) mFeedDetail.getComments().get(pos);

            //safe check
            //double check that they are revealing their own comment
            if (!comment.getCommentUserId().equals(mViewerUserId) || !comment.getCommentPostId().equals(mCommentId)) return;

            final ProgressDialog progressDialog = ProgressDialog.show(context, null, mIsAnon ? "Revealing comment..." : "Making comment anonymous...", true, false);
            mDenySwipe = true;

            new LSDKEvents(context).revealComment(mCommentId, !mIsAnon, new Callback() {
                final boolean anon = mIsAnon;

                @Override
                public void onFailure(Call call, IOException e) {
                    final BaseTaptActivity act = (BaseTaptActivity) context;
                    progressDialog.dismiss();
                    mDenySwipe = false;

                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "Failed to change comment. Could not find connection.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String res = response.body().string();

                    if (response.isSuccessful()) {

                        try {

                            if (!mIsAnon) { //set new anon image
                                comment.setAnonImage(new JSONObject(res).getString("anonymousImage"));
                            }

                            BaseTaptActivity activity = (BaseTaptActivity) context;

                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        comment.setIsAnon(!anon);
                                        notifyItemChanged(pos + 1);
                                        Toast.makeText(context, anon ? "Comment revealed" : "Comment made anonymous", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }catch (JSONException e){
                            final BaseTaptActivity act = (BaseTaptActivity) context;
                            if (act != null) {
                                act.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(act, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } else {
                        Log.i("Comment item reveal", "onResponse: " + response.body().string());
                        final BaseTaptActivity act = (BaseTaptActivity) context;
                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(act, "Failed to change comment. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    progressDialog.dismiss();
                    mDenySwipe = false;
                }
            });
        }


        private void showConfirmReportDialog() {
            if (context != null)
                mDialog = new AlertDialog.Builder(context).setTitle("Report")
                        .setMessage("Are you sure you want to report this comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reportComment();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
        }

        private void reportComment() {
            if (mDenySwipe) return;

            new LSDKEvents(context).reportComment(mCommentId, mViewerUserId, new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    final BaseTaptActivity act = (BaseTaptActivity) context;
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(act);
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        response.body().close();
                        final BaseTaptActivity act = (BaseTaptActivity) context;
                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(act, "Comment reported", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.i("Comment item report", "onResponse: " + response.body().string());
                        final BaseTaptActivity act = (BaseTaptActivity) context;
                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showBadConnectionToast(act);
                                }
                            });
                        }
                    }
                }
            });
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
                closeAllDialogs();
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

    public void closeAllDialogs(){
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
    }

}
