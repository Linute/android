package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 10/22/16.
 */

public class BaseFeedItem implements Parcelable{

    private String mId;         // id of post

    public BaseFeedItem(){
        mId = null;
    }
    public BaseFeedItem(String id){
        mId = id;
    }

    protected BaseFeedItem(Parcel in) {
        mId = in.readString();
    }

    public static final Creator<BaseFeedItem> CREATOR = new Creator<BaseFeedItem>() {
        @Override
        public BaseFeedItem createFromParcel(Parcel in) {
            return new BaseFeedItem(in);
        }

        @Override
        public BaseFeedItem[] newArray(int size) {
            return new BaseFeedItem[size];
        }
    };

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
    }
}
