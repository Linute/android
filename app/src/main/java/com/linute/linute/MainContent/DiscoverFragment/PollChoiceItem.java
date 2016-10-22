package com.linute.linute.MainContent.DiscoverFragment;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;

/**
 * Created by QiFeng on 10/21/16.
 */
public class PollChoiceItem implements Parcelable {

    public final String mOptionText;
    private int mVotes;
    public final int mColor;

    public PollChoiceItem(String text, int votes, String color){
        mOptionText = text;
        mVotes = votes;
        mColor = Color.parseColor(color);
    }

    protected PollChoiceItem(Parcel in) {
        mOptionText = in.readString();
        mVotes = in.readInt();
        mColor = in.readInt();
    }

    public static final Creator<PollChoiceItem> CREATOR = new Creator<PollChoiceItem>() {
        @Override
        public PollChoiceItem createFromParcel(Parcel in) {
            return new PollChoiceItem(in);
        }

        @Override
        public PollChoiceItem[] newArray(int size) {
            return new PollChoiceItem[size];
        }
    };

    public int getVotes() {
        return mVotes;
    }

    public void setVotes(int votes) {
        mVotes = votes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mOptionText);
        parcel.writeInt(mVotes);
        parcel.writeInt(mColor);
    }
}
