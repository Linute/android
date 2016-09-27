package com.linute.linute.MainContent.FindFriends;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 9/27/16.
 */
public class SearchFilter implements Parcelable {
    public final String key;
    public final String value;

    public SearchFilter(String key, String value){
        this.key = key;
        this.value = value;
    }

    protected SearchFilter(Parcel in) {
        key = in.readString();
        value = in.readString();
    }

    public static final Creator<SearchFilter> CREATOR = new Creator<SearchFilter>() {
        @Override
        public SearchFilter createFromParcel(Parcel in) {
            return new SearchFilter(in);
        }

        @Override
        public SearchFilter[] newArray(int size) {
            return new SearchFilter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }
}
