package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by QiFeng on 10/22/16.
 */

public class Poll extends BaseFeedItem implements Parcelable{

    private String mTitle;
    private int mTotalCount;
    private String mTrendId;
    private boolean mShowTrend;
    private boolean mIsHidden;
    private ArrayList<PollChoiceItem> mPollChoiceItems;


    public Poll(JSONObject object){
        super("");
    }


    public boolean isHidden() {
        return mIsHidden;
    }

    public void setHidden(boolean hidden) {
        mIsHidden = hidden;
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
        parcel.writeInt(mTotalCount);
        parcel.writeString(mTrendId);
        parcel.writeByte((byte) (mShowTrend ? 1 : 0));
        parcel.writeByte((byte) (mIsHidden ? 1 : 0));
        parcel.writeList(mPollChoiceItems);
    }

    protected Poll(Parcel in) {
        super(in);
        mTitle = in.readString();
        mTotalCount = in.readInt();
        mTrendId = in.readString();
        mShowTrend = in.readByte() == 1;
        mIsHidden = in.readByte() == 1;
        mPollChoiceItems = new ArrayList<>();
        in.readList(mPollChoiceItems, PollChoiceItem.class.getClassLoader());
    }
}
