package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 8/30/16.
 */
public class PostSize implements Parcelable{

    public final int width;
    public final int height;

    public PostSize(int width, int height){
        this.width = width;
        this.height = height;
    }

    protected PostSize(Parcel in) {
        width = in.readInt();
        height = in.readInt();
    }

    public static final Creator<PostSize> CREATOR = new Creator<PostSize>() {
        @Override
        public PostSize createFromParcel(Parcel in) {
            return new PostSize(in);
        }

        @Override
        public PostSize[] newArray(int size) {
            return new PostSize[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
    }
}
