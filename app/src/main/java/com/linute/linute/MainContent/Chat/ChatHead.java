package com.linute.linute.MainContent.Chat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arman on 1/24/16.
 */
public class ChatHead implements Parcelable {
    private String mUsername;
    private String mUserImage;

    public ChatHead(String username, String userImage) {
        mUsername = username;
        mUserImage = userImage;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getUserImage() {
        return mUserImage;
    }

    protected ChatHead(Parcel in) {
        mUsername = in.readString();
        mUserImage = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUsername);
        dest.writeString(mUserImage);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChatHead> CREATOR = new Parcelable.Creator<ChatHead>() {
        @Override
        public ChatHead createFromParcel(Parcel in) {
            return new ChatHead(in);
        }

        @Override
        public ChatHead[] newArray(int size) {
            return new ChatHead[size];
        }
    };
}
