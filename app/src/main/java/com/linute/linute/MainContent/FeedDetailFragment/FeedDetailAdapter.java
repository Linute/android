package com.linute.linute.MainContent.FeedDetailFragment;

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
    private static final int TYPE_VIDEO_HEADER = 4;
    private static final int TYPE_ITEM = 2;
    private static final int TYPE_NO_COMMENTS = 3;

    private Context context;

    //    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();
    private FeedDetail mFeedDetail;

    private boolean mIsImage;
    private boolean mHasVideo;

    private MentionedTextAdder mMentionedTextAdder;

    public FeedDetailAdapter(FeedDetail feedDetail, Context context, boolean isImage, boolean hasVideo) {
        this.context = context;
        mFeedDetail = feedDetail;
        mIsImage = isImage;
        mHasVideo = hasVideo;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            return new FeedDetailViewHolder(LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.fragment_feed_detail_page_list_item, parent, false));
        }
        //image post
        else if (viewType == TYPE_IMAGE_HEADER) { //TODO: FIX ME
            //inflate your layout and pass it to view holder
            return new FeedDetailHeaderImageViewHolder(this, LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.feed_detail_header_image, parent, false), context);
        } else if (viewType == TYPE_STATUS_HEADER) { //was a status post
            return new FeedDetailHeaderStatusViewHolder(this,
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.feed_detail_header_status, parent, false), context);
        } else if (viewType == TYPE_VIDEO_HEADER){
            return new FeedDetailHeaderVideoViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.feed_detail_header_video, parent, false), context, this);
        }
        else {
            return new NoCommentsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.no_comments_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FeedDetailViewHolder) {
            ((FeedDetailViewHolder) holder).bindModel(mFeedDetail.getComments().get(position - 1));
            mItemManger.bindView(holder.itemView, position);
        } else if (holder instanceof FeedDetailHeaderImageViewHolder) {
            ((FeedDetailHeaderImageViewHolder) holder).bindModel(mFeedDetail);
        } else if (holder instanceof FeedDetailHeaderStatusViewHolder) {
            ((FeedDetailHeaderStatusViewHolder) holder).bindModel(mFeedDetail);
        } else if (holder instanceof  FeedDetailHeaderVideoViewHolder){
            ((FeedDetailHeaderVideoViewHolder) holder).bindViews(mFeedDetail);
        }

        //// TODO: 3/8/16 Video
    }

    @Override
    public int getItemCount() {
        return mFeedDetail.getComments().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            if (mIsImage) {
                if (mHasVideo) return TYPE_VIDEO_HEADER;
                return TYPE_IMAGE_HEADER;
            }

            return TYPE_STATUS_HEADER;
        }

        if (mFeedDetail.getComments().get(0) == null)  //first item is no, means no comments
            return TYPE_NO_COMMENTS;

        return TYPE_ITEM;
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

            vCommentUserImage = (CircleImageView) itemView.findViewById(R.id.comment_user_image);
            vCommentUserName = (TextView) itemView.findViewById(R.id.comment_user_name);
            vCommentUserText = (TextView) itemView.findViewById(R.id.comment);
            vTimeStamp = (TextView) itemView.findViewById(R.id.comment_time_ago);
        }

        void bindModel(Comment comment) {

            mIsAnon = comment.isAnon();
            mCommenterUserId = comment.getCommentUserId();
            mUserName = comment.getCommentUserName();
            mCommentId = comment.getCommentUserPostId();

            if (mIsAnon) {
                setAnonImage(comment.getAnonImage());
                vCommentUserName.setText("Anonymous");
            } else {
                setProfileImage(comment.getCommentUserProfileImage());
                vCommentUserName.setText(comment.getCommentUserName());
            }

            setUpPulloutButtons();

            mSwipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, mSwipeLayout.findViewById(R.id.comment_bottom_wrapper));

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
                vCommentUserText.setText(comment.getCommentUserPostText());
            }

        }

        private void setUpPulloutButtons() {
            if (mViewerUserId.equals(mCommenterUserId)) {
                mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(View.VISIBLE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.GONE);
            } else {
                mSwipeLayout.findViewById(R.id.comment_delete).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_reveal).setVisibility(View.GONE);
                mSwipeLayout.findViewById(R.id.comment_report).setVisibility(View.VISIBLE);

                if (mIsAnon) {
                    mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.GONE);
                } else {
                    mSwipeLayout.findViewById(R.id.comment_reply).setVisibility(View.VISIBLE);
                }
            }

        }

        private void setUpMentionedOnClicks(Comment comment) {
            String commentText = comment.getCommentUserPostText();
            SpannableString commentSpannable = new SpannableString(commentText);

            int startSearchAtIndex = 0; //start search from

            // assumes the list comes back in the order people were tagged.
            // i.e. if text is: "@AndrewBee @JonathanI hello there" , the List will have the order {Andrew, Jonathan}
            // this way we won't look for @JonathanI at index 0 when we know it will come after @Andrew

            final BaseTaptActivity activity = (BaseTaptActivity) context;
            if (activity == null) {
                vCommentUserText.setText(comment.getCommentUserPostText());
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
                switch (v.getId()) {
                    case R.id.comment_reply:
                        mMentionedTextAdder.addMentionedPerson(new MentionedPerson(mUserName, mCommenterUserId, ""));
                        break;
                    case R.id.comment_delete:
                        showConfirmDelete();
                        break;
                    case R.id.comment_report:
                        showConfirmReportDialog();
                        break;
                    case R.id.comment_reveal:
                        showConfirmRevealDialog();
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


        private void showConfirmDelete() {
            if (context != null) {
                new AlertDialog.Builder(context).setTitle("Delete")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteComment();
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

        private void deleteComment() {

            final int pos = getAdapterPosition() - 1;
            final Comment com = mFeedDetail.getComments().get(pos);
            mItemManger.removeShownLayouts(mSwipeLayout);
            mFeedDetail.getComments().remove(pos);
            notifyItemRemoved(pos + 1);
            mFeedDetail.refreshCommentCount();


            new LSDKEvents(context).deleteComment(mCommentId, new Callback() {

                final int position = pos;
                final Comment comment = com;

                @Override
                public void onFailure(Call call, IOException e) {
                    final BaseTaptActivity act = (BaseTaptActivity) context;
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "Failed to delete comment. Could not find connection.", Toast.LENGTH_SHORT).show();
                                mFeedDetail.getComments().add(position, comment);
                                notifyItemInserted(position + 1);
                                mFeedDetail.refreshCommentCount();
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        response.body().close();
                    } else {
                        Log.i("Comment item delete", "onResponse: " + response.body().string());
                        final BaseTaptActivity act = (BaseTaptActivity) context;
                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mFeedDetail.getComments().add(position, comment);
                                    notifyItemInserted(position + 1);
                                    mFeedDetail.refreshCommentCount();
                                }
                            });
                        }
                    }
                }
            });
        }

        private void showConfirmRevealDialog() {
            if (context != null)
                new AlertDialog.Builder(context).setTitle(mIsAnon ? "Reveal" : "Hide")
                        .setMessage(mIsAnon ? "Are you sure you want to turn anonymous off for this comment?" : "Are you sure you want to make this comment anonymous?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                revealComment();
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

        private void revealComment() {

            final int pos = getAdapterPosition() - 1;
            mFeedDetail.getComments().get(pos).setIsAnon(!mIsAnon);
            notifyItemChanged(pos + 1);

            new LSDKEvents(context).revealComment(mCommentId, !mIsAnon, new Callback() {
                final int position = pos;
                final boolean anon = mIsAnon;

                @Override
                public void onFailure(Call call, IOException e) {
                    final BaseTaptActivity act = (BaseTaptActivity) context;
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "Failed to change comment. Could not find connection.", Toast.LENGTH_SHORT).show();
                                mFeedDetail.getComments().get(position).setIsAnon(anon);
                                notifyItemChanged(position + 1);
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        response.body().close();

                    } else {
                        Log.i("Comment item reveal", "onResponse: " + response.body().string());
                        final BaseTaptActivity act = (BaseTaptActivity) context;
                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(act, "Failed to change comment. Please try again later.", Toast.LENGTH_SHORT).show();
                                    mFeedDetail.getComments().get(position).setIsAnon(anon);
                                    notifyItemChanged(position + 1);
                                }
                            });
                        }
                    }
                }
            });
        }


        private void showConfirmReportDialog() {
            if (context != null)
                new AlertDialog.Builder(context).setTitle("Report")
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


    public interface MentionedTextAdder {
        void addMentionedPerson(MentionedPerson person);
    }

}
