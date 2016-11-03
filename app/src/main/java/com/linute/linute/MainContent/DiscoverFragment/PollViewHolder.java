package com.linute.linute.MainContent.DiscoverFragment;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.RatingBar;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by QiFeng on 10/21/16.
 */
public class PollViewHolder extends RecyclerView.ViewHolder implements RatingBar.OnClickChoice, View.OnClickListener {

    private TextView vTitle;
    private TextView vVotes;
    private LinearLayout vRatingBarsContainer;
    private LinkedList<RatingBar> mRatingBars;
    private Poll mPoll;
    private View vCommentButton;
    private TextView vCommentsText;
    private ImageView vCommentIcon;
    private OnVote mOnVote;
    //private BaseFeedAdapter.PostAction mActions;

    @ColorInt
    private int mBlackColor;

    @ColorInt
    private int mGreyColor;

    public PollViewHolder(View itemView/*, BaseFeedAdapter.PostAction actions*/, OnVote onVote) {
        super(itemView);
        mOnVote = onVote;
        mBlackColor = ContextCompat.getColor(itemView.getContext(), R.color.eighty_black);
        mGreyColor = ContextCompat.getColor(itemView.getContext(), R.color.inactive_grey);
        mRatingBars = new LinkedList<>();
        vRatingBarsContainer = (LinearLayout) itemView.findViewById(R.id.rating_content);
        vTitle = (TextView) itemView.findViewById(R.id.title);
        vVotes = (TextView) itemView.findViewById(R.id.votes);
        vCommentButton = itemView.findViewById(R.id.feed_control_bar_comments_button);
        vCommentButton.setOnClickListener(this);
        vCommentsText = (TextView) vCommentButton.findViewById(R.id.postNumComments);
        vCommentIcon = (ImageView) vCommentButton.findViewById(R.id.postComments);
        //mActions = actions;
//        itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mActions != null)
//                    mActions.clickedOptions(mPoll, getAdapterPosition());
//            }
//        });
    }


    public void bindView(Poll p) {
        mPoll = p;
        RatingBar b;
        vTitle.setText(p.getTitle());

        vVotes.setText(String.valueOf(p.getTotalCount()));
        vCommentsText.setText(String.valueOf(mPoll.getNumberOfComments()));

        while (p.getPollChoiceItems().size() > mRatingBars.size()) {
            b = new RatingBar(itemView.getContext());
            b.setOnClickChoice(this);
            mRatingBars.addLast(b);
            vRatingBarsContainer.addView(b);
        }

        while (p.getPollChoiceItems().size() < mRatingBars.size()) {
            mRatingBars.removeLast();
            vRatingBarsContainer.removeViewAt(vRatingBarsContainer.getChildCount() - 1);
        }

        if (mPoll.getNumberOfComments() > 0) {
            vCommentIcon.clearColorFilter();
        } else {
            vCommentIcon.setColorFilter(mGreyColor, PorterDuff.Mode.SRC_ATOP);
        }

        PollChoiceItem item;
        for (int i = 0; i < p.getPollChoiceItems().size(); i++) {
            item = p.getPollChoiceItems().get(i);
            b = mRatingBars.get(i);
            b.setChoice(item);

            if (mPoll.getVotedFor() != null) {
                if (mPoll.getVotedFor().equals(item.id))
                    b.setOptionTextStyle(Typeface.DEFAULT_BOLD);
                else
                    b.setOptionTextStyle(Typeface.DEFAULT);

                b.setProgressColor(item.mColor);
                b.setOptionTextColor(mBlackColor);
                b.setOptionText(String.format(Locale.US, "%s (%d)", item.mOptionText, item.getVotes()));
                b.setProgress((int) ((float) item.getVotes() / p.getTotalCount() * 100));
            } else {
                b.setProgress(0);
                b.setOptionTextStyle(Typeface.DEFAULT);
                b.setOptionTextColor(mGreyColor);
                b.removeColorFilters();
                b.setOptionText(item.mOptionText);
            }
        }
    }

    @Override
    public void click(PollChoiceItem choice) {
        if (mPoll.getVotedFor() == null) {
            try {
                JSONObject object = new JSONObject();
                object.put("poll", mPoll.getId());
                object.put("vote", choice.id);
                if (!TaptSocket.getInstance().socketConnected()) {
                    if (itemView.getContext() != null)
                        Utils.showBadConnectionToast(itemView.getContext());
                } else {
                    TaptSocket.getInstance().emit(API_Methods.VERSION + ":polls:vote", object);

                    mPoll.setVotedFor(choice.id);
                    mPoll.incrementTotalCount();
                    choice.incrementVotes();

                    vVotes.setText(String.valueOf(mPoll.getTotalCount()));

                    PollChoiceItem item;
                    RatingBar b;
                    for (int i = 0; i < mPoll.getPollChoiceItems().size(); i++) {
                        item = mPoll.getPollChoiceItems().get(i);
                        b = mRatingBars.get(i);
                        b.setChoice(item);
                        b.setProgressColor(item.mColor);
                        b.setOptionTextColor(mBlackColor);
                        b.setOptionTextStyle(choice.id.equals(item.id) ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                        b.setOptionText(String.format(Locale.US, "%s (%d)", item.mOptionText, item.getVotes()));
                        b.setProgress((int) ((float) item.getVotes() / mPoll.getTotalCount() * 100));
                    }

                    if (mOnVote != null)
                        mOnVote.onVote();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == vCommentButton){
            BaseTaptActivity activity = (BaseTaptActivity) itemView.getContext();
            vCommentButton.setAlpha(.5f);
            vCommentButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    vCommentButton.setAlpha(1);
                }
            }, 500);
            activity.addFragmentToContainer(
                    FeedDetailPage.newInstance(mPoll)
            );
        }
    }


    public interface OnVote{
        void onVote();
    }
}
