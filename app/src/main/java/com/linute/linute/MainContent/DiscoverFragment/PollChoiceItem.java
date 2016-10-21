package com.linute.linute.MainContent.DiscoverFragment;

import android.support.annotation.ColorInt;

/**
 * Created by QiFeng on 10/21/16.
 */
public class PollChoiceItem {

    public final String mOptionText;
    private int mVotes;
    public final  @ColorInt int mColor;

    public PollChoiceItem(String text, int votes, @ColorInt int color){
        mOptionText = text;
        mVotes = votes;
        mColor = color;
    }

    public int getVotes() {
        return mVotes;
    }

    public void setVotes(int votes) {
        mVotes = votes;
    }

}
