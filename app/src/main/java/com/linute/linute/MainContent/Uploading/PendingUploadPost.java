package com.linute.linute.MainContent.Uploading;

import android.os.Parcel;
import android.os.Parcelable;

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

    public PendingUploadPost(String id,
                             String collegeId,
                             int privacy,
                             int isAnonymousCommentsDisabled,
                             String title,
                             int type,
                             String imagePath,
                             String videoPath,
                             String owner
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
        mOwner = owner;
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

    public String getImagePath() {
        return mImagePath;
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
