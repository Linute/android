package com.linute.linute.MainContent.ProfileFragment;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by QiFeng on 2/9/16.
 */
public class EmptyUserActivityItem extends UserActivityItem {
    public EmptyUserActivityItem() {
    }

    public static final Creator<UserActivityItem> CREATOR = new Creator<UserActivityItem>() {
        @Override
        public UserActivityItem createFromParcel(Parcel source) {
            return new EmptyUserActivityItem();
        }

        @Override
        public UserActivityItem[] newArray(int size) {
            return new UserActivityItem[size];
        }
    };
}
