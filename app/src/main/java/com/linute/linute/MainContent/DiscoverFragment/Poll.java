package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by QiFeng on 10/22/16.
 */

public class Poll extends BaseFeedItem implements Parcelable{

    private String mTitle;
    private String mDescription;
    private int mTotalCount;
    private String[] mTrendIds;
    private boolean mShowTrend;
    private ArrayList<PollChoiceItem> mPollChoiceItems;
    private int mPosition;
    private boolean mHasVoted = false;


    public Poll(JSONObject object) throws JSONException{
        super(object.getString("id"));

       // Log.d("POLL", "Poll: "+object.toString(4));

        mTitle = object.getString("title");
        mDescription = object.getString("description");
        mPosition = object.getInt("position");

        JSONArray trends = object.getJSONArray("trends");
        mTrendIds = new String[trends.length()];
        for (int i = 0; i < trends.length(); i++)
            mTrendIds[i] = trends.getString(i);

        mPollChoiceItems = new ArrayList<>();
        JSONArray options = object.getJSONArray("options");
        for (int i = 0; i < options.length(); i++){
            mPollChoiceItems.add(new PollChoiceItem(options.getJSONObject(i)));
        }

        mTotalCount = object.getInt("totalVotes");

        mHasVoted = !object.isNull("vote");
    }

    public boolean isShowTrend() {
        return mShowTrend;
    }

    public void setShowTrend(boolean showTrend) {
        mShowTrend = showTrend;
    }

    public String[] getTrendIds() {
        return mTrendIds;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public void incrementTotalCount(){
        mTotalCount++;
    }

    public ArrayList<PollChoiceItem> getPollChoiceItems() {
        return mPollChoiceItems;
    }

    public void setPollChoiceItems(ArrayList<PollChoiceItem> pollChoiceItems) {
        mPollChoiceItems = pollChoiceItems;
    }

    public boolean hasVoted() {
        return mHasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        mHasVoted = hasVoted;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(mTitle);
        parcel.writeString(mDescription);
        parcel.writeInt(mTotalCount);
        parcel.writeStringArray(mTrendIds);
        parcel.writeByte((byte) (mShowTrend ? 1 : 0));
        parcel.writeList(mPollChoiceItems);
        parcel.writeInt(mPosition);
        parcel.writeByte((byte) (mHasVoted ? 1 : 0));
    }

    public static final Creator<Poll> CREATOR = new Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel in) {
            return new Poll(in);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };

    protected Poll(Parcel in) {
        super(in);
        mTitle = in.readString();
        mDescription = in.readString();
        mTotalCount = in.readInt();
        mTrendIds = in.createStringArray();
        mShowTrend = in.readByte() == 1;
        mPollChoiceItems = new ArrayList<>();
        in.readList(mPollChoiceItems, PollChoiceItem.class.getClassLoader());
        mPosition = in.readInt();
        mHasVoted = in.readByte() == 1;
    }
}
