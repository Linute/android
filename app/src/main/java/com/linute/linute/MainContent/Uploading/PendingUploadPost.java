package com.linute.linute.MainContent.Uploading;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by QiFeng on 6/25/16.
 */
public class PendingUploadPost implements Parcelable {

    public static final String PENDING_POST_KEY = "pending_post_key";

    private String mId;
    private String mCollegeId;
    private int mPrivacy;
    private int mIsAnonymousCommentsDisabled;
    private String mTitle;
    private int mLatitude;
    private int mLongitude;
    private int mType;
    private String mImagePath;
    private String mVideoPath;
    private String mOwner;
    private String mUserToken;

    private ArrayList<String> mTrends;
    private ArrayList<String> mPeople;



    private ArrayList<String> mStickers;
    private ArrayList<String> mFilters;
    private String mTrendId;

    public PendingUploadPost(String id,
                             String collegeId,
                             int privacy,
                             int isAnonymousCommentsDisabled,
                             String title,
                             int type,
                             String imagePath,
                             String videoPath,
                             ArrayList<String> stickers,
                             ArrayList<String> filters,
                             String owner,
                             String userToken,
                             String trendId
    ){
        mId = id;
        mCollegeId = collegeId;
        mPrivacy = privacy;
        mIsAnonymousCommentsDisabled = isAnonymousCommentsDisabled;
        mTitle = title;
        mLatitude = 0;
        mLongitude = 0;
        mType = type;
        mImagePath = imagePath;
        mVideoPath = videoPath;
        mStickers = stickers;
        mFilters = filters;
        mOwner = owner;
        mUserToken = userToken;
        mTrendId = trendId;
    }



    public void setCollege(String collegeId){
        mCollegeId = collegeId;
    }


    public ArrayList<String> getPeople() {
        return mPeople;
    }

    public ArrayList<String> getTrends() {
        return mTrends;
    }

    public void setShareParams(ArrayList<String> people, ArrayList<String> trends){
        mPeople = people;
        mTrends = trends;
    }

    public String getId() {
        return mId;
    }

    public String getCollegeId() {
        return mCollegeId;
    }

    public int getPrivacy() {
        return mPrivacy;
    }

    public int getIsAnonymousCommentsDisabled() {
        return mIsAnonymousCommentsDisabled;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getLatitude() {
        return mLatitude;
    }

    public int getLongitude() {
        return mLongitude;
    }

    public int getType() {
        return mType;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public String getOwner() {
        return mOwner;
    }

    public String getUserToken() {
        return mUserToken;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public ArrayList<String> getStickers() {
        return mStickers;
    }

    public ArrayList<String> getFilters() {
        return mFilters;
    }

    public String getTrendId() {
        return mTrendId;
    }

    protected PendingUploadPost(Parcel in) {
        mId = in.readString();
        mCollegeId = in.readString();
        mPrivacy = in.readInt();
        mIsAnonymousCommentsDisabled = in.readInt();
        mTitle = in.readString();
        mLatitude = in.readInt();
        mLongitude = in.readInt();
        mType = in.readInt();
        mImagePath = in.readString();
        mVideoPath = in.readString();
        mOwner = in.readString();
        mUserToken = in.readString();
        mPeople = new ArrayList<>();
        in.readStringList(mPeople);
        mTrends = new ArrayList<>();
        in.readStringList(mTrends);
        mStickers = new ArrayList<>();
        in.readStringList(mStickers);
        mFilters = new ArrayList<>();
        in.readStringList(mFilters);
        mTrendId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mCollegeId);
        dest.writeInt(mPrivacy);
        dest.writeInt(mIsAnonymousCommentsDisabled);
        dest.writeString(mTitle);
        dest.writeInt(mLatitude);
        dest.writeInt(mLongitude);
        dest.writeInt(mType);
        dest.writeString(mImagePath);
        dest.writeString(mVideoPath);
        dest.writeString(mOwner);
        dest.writeString(mUserToken);
        dest.writeStringList(mPeople);
        dest.writeStringList(mTrends);
        dest.writeStringList(mStickers);
        dest.writeStringList(mFilters);
        dest.writeString(mTrendId);
    }

    public static final Creator<PendingUploadPost> CREATOR = new Creator<PendingUploadPost>() {
        @Override
        public PendingUploadPost createFromParcel(Parcel in) {
            return new PendingUploadPost(in);
        }

        @Override
        public PendingUploadPost[] newArray(int size) {
            return new PendingUploadPost[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }



}
