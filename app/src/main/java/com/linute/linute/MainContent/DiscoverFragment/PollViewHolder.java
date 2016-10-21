package com.linute.linute.MainContent.DiscoverFragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.RatingBar;

import java.util.LinkedList;

/**
 * Created by QiFeng on 10/21/16.
 */
public class PollViewHolder extends RecyclerView.ViewHolder {

    private TextView vTitle;
    private LinearLayout vRatingBarsContainer;
    private LinkedList<RatingBar> mRatingBars;

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
            mRatingBars.addLast(b);
            vRatingBarsContainer.addView(b);
        }

        while (p.getPollChoices().size() < mRatingBars.size()){
            b = new RatingBar(itemView.getContext());
            mRatingBars.removeLast();
            vRatingBarsContainer.removeViewAt(vRatingBarsContainer.getChildCount() - 1);
        }

        Log.i("test", "bindView: "+p.getPollChoices().size() + " " + vRatingBarsContainer.getChildCount());

        PollChoiceItem item;
        for (int i = 0 ; i < p.getPollChoices().size(); i++){
            item = p.getPollChoices().get(i);
            b = mRatingBars.get(i);
            b.setProgressColor(item.mColor);
            b.setOptionText(item.mOptionText);
            b.setProgress(item.getVotes() / p.getTotalVotes());
        }

    }
}
