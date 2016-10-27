package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String mTrendId;
    private boolean mShowTrend;
    private ArrayList<PollChoiceItem> mPollChoiceItems;
    private int mPosition;
    private boolean mHasVoted = false;


    public Poll(JSONObject object) throws JSONException{
        super(object.getString("id"));

        mTitle = object.getString("title");
        mDescription = object.getString("description");
        mPosition = object.getInt("position");
        mTrendId = object.getString("trend");

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

    public String getTrendId() {
        return mTrendId;
    }

    public void setTrendId(String trendId) {
        mTrendId = trendId;
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

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }

    public ArrayList<PollChoiceItem> getPollChoiceItems() {
        return mPollChoiceItems;
    }

    public void setPollChoiceItems(ArrayList<PollChoiceItem> pollChoiceItems) {
        mPollChoiceItems = pollChoiceItems;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(mTitle);
        parcel.writeString(mDescription);
        parcel.writeInt(mTotalCount);
        parcel.writeString(mTrendId);
        parcel.writeByte((byte) (mShowTrend ? 1 : 0));
        parcel.writeList(mPollChoiceItems);
        parcel.writeInt(mPosition);
        parcel.writeByte((byte) (mHasVoted ? 1 : 0));
    }

    protected Poll(Parcel in) {
        super(in);
        mTitle = in.readString();
        mDescription = in.readString();
        mTotalCount = in.readInt();
        mTrendId = in.readString();
        mShowTrend = in.readByte() == 1;
        mPollChoiceItems = new ArrayList<>();
        in.readList(mPollChoiceItems, PollChoiceItem.class.getClassLoader());
        mPosition = in.readInt();
        mHasVoted = in.readByte() == 1;
    }
}
