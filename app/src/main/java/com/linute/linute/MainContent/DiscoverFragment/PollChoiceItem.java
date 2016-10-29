package com.linute.linute.MainContent.DiscoverFragment;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 10/21/16.
 */
public class PollChoiceItem implements Parcelable {

    public final String mOptionText;
    private int mVotes;
    public final int mColor;
    public final String id;

    public PollChoiceItem(JSONObject object) throws JSONException {
        mOptionText = object.getString("text");
        mVotes = object.getInt("votes");
        id = object.getString("id");
        if(object.getString("color").length() == 6) {
            mColor = Color.parseColor("#" + object.getString("color"));
        }else {
            mColor = 0;
        }
    }

    protected PollChoiceItem(Parcel in) {
        mOptionText = in.readString();
        mVotes = in.readInt();
        mColor = in.readInt();
        id = in.readString();
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

    public void incrementVotes() {
        mVotes++;
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
        parcel.writeString(id);
    }
}
