package com.linute.linute.MainContent.DiscoverFragment;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.RatingBar;

import java.util.LinkedList;

/**
 * Created by QiFeng on 10/21/16.
 */
public class PollViewHolder extends RecyclerView.ViewHolder implements RatingBar.OnClickChoice{

    //TODO: on click listeners

    private TextView vTitle;
    private LinearLayout vRatingBarsContainer;
    private LinkedList<RatingBar> mRatingBars;
    private boolean mHasVoted;

    public PollViewHolder(View itemView) {
        super(itemView);
        mRatingBars = new LinkedList<>();
        vRatingBarsContainer = (LinearLayout) itemView.findViewById(R.id.rating_content);
        vTitle = (TextView) itemView.findViewById(R.id.title);
    }


    public void bindView(Post p) {
        RatingBar b;
        vTitle.setText(p.getTitle());

        while (p.getPollChoices().size() > mRatingBars.size()) {
            b = new RatingBar(itemView.getContext());
            b.setOnClickChoice(this);
            mRatingBars.addLast(b);
            vRatingBarsContainer.addView(b);
        }

        while (p.getPollChoices().size() < mRatingBars.size()){
            mRatingBars.removeLast();
            vRatingBarsContainer.removeViewAt(vRatingBarsContainer.getChildCount() - 1);
        }


        PollChoiceItem item;
        for (int i = 0 ; i < p.getPollChoices().size(); i++){
            item = p.getPollChoices().get(i);
            b = mRatingBars.get(i);
            b.setChoice(i);
            b.setProgressColor(item.mColor);
            b.setOptionText(item.mOptionText);
            b.setProgress((int)((float)item.getVotes() / p.getTotalVotes() * 100));
        }

    }

    @Override
    public void click(int choice) {
        if(!mHasVoted){
            mHasVoted = true;
            Toast.makeText(itemView.getContext(), choice+" selected", Toast.LENGTH_SHORT).show();
        }
    }
}
