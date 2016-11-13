package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
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
    private String[] mTrendIds;
    private boolean mShowTrend;
    private ArrayList<PollChoiceItem> mPollChoiceItems;
    private int mPosition;
    private String mVotedFor;

    //for comment section
    private String mPostId;
    private boolean mIsAnonymousCommentsDisabled;
    private int mNumberOfComments;

    private boolean mHidden;
    private boolean mMuted;


    public Poll(JSONObject object) throws JSONException{
        super(object.getString("id"));

       // Log.d("POLL", "Poll: "+object.toString(4));

        mTitle = object.getString("title");
        mDescription = object.getString("description");
        mPosition = object.getInt("position");

        JSONArray trends = object.getJSONArray("trends");
        mTrendIds = new String[trends.length()];
        for (int i = 0; i < trends.length(); i++)
            mTrendIds[i] = trends.getJSONObject(i).getString("id");

        mPollChoiceItems = new ArrayList<>();
        JSONArray options = object.getJSONArray("options");
        for (int i = 0; i < options.length(); i++){
            mPollChoiceItems.add(new PollChoiceItem(options.getJSONObject(i)));
        }

        mHidden = object.getBoolean("isHidden");
        try{
            mMuted = object.getBoolean("isMuted");
        }catch (JSONException e){
            mMuted = false;
        }

        mTotalCount = object.getInt("totalVotes");
        mVotedFor = object.isNull("vote") ? null : object.getString("vote");

        object = object.getJSONObject("post");
        mPostId = object.getString("id");
        mIsAnonymousCommentsDisabled = object.getBoolean("isAnonymousCommentsDisabled");
        mNumberOfComments = object.getInt("numberOfComments");
    }

    public void update(JSONObject object) throws JSONException{
        mTotalCount = object.getInt("totalVotes");
        mVotedFor = object.isNull("vote") ? null : object.getString("vote");
        try{
            mMuted = object.getBoolean("isMuted");
        }catch (JSONException e){
            mMuted = false;
        }

        try {
            mNumberOfComments = object.getJSONObject("post").getInt("numberOfComments");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void update(Poll p){
        mTotalCount = p.mTotalCount;
        mVotedFor = p.mVotedFor;
        mNumberOfComments = p.mNumberOfComments;
        mMuted = p.mMuted;
        mHidden = p.mHidden;
    }


    public void setHidden(boolean hidden) {
        mHidden = hidden;
    }

    public void setMuted(boolean muted) {
        mMuted = muted;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public boolean isMuted() {
        return mMuted;
    }

    public String getPostId() {
        return mPostId;
    }

    public boolean isAnonymousCommentsDisabled() {
        return mIsAnonymousCommentsDisabled;
    }

    public int getNumberOfComments() {
        return mNumberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        mNumberOfComments = numberOfComments;
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

    public String getVotedFor() {
        return mVotedFor;
    }

    public void setVotedFor(String votedFor) {
        mVotedFor = votedFor;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Poll && ((Poll)obj).getId().equals(getId());
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
        parcel.writeString(mVotedFor);

        parcel.writeString(mPostId);
        parcel.writeByte((byte)(mIsAnonymousCommentsDisabled ? 1 : 0));
        parcel.writeInt(mNumberOfComments);

        parcel.writeByte((byte)(mHidden ? 1 : 0));
        parcel.writeByte((byte)(mMuted ? 1 : 0));

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
        mVotedFor = in.readString();

        mPostId = in.readString();
        mIsAnonymousCommentsDisabled = in.readByte() == 1;
        mNumberOfComments = in.readInt();

        mHidden = in.readByte() == 1;
        mMuted = in.readByte() == 1;
    }

    @Override
    public void getShareUri(Context context, OnUriReadyListener callback) {
        callback.onUriFail(new IllegalArgumentException("Polls cannot be shared"));
    }
}
