package com.linute.linute.MainContent.CreateContent.Gallery;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 8/30/16.
 */
public class GalleryItem implements Parcelable{

    public final String id;
    public final String path;
    public final int type;

    public final String bucketId;

    public GalleryItem(String id, String path, int type, String bucketId){
        this.id = id;
        this.path = path;
        this.type = type;
        this.bucketId = bucketId;
    }

    protected GalleryItem(Parcel in) {
        id = in.readString();
        path = in.readString();
        type = in.readInt();
        bucketId = in.readString();
    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel in) {
            return new GalleryItem(in);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(path);
        dest.writeInt(type);
        dest.writeString(bucketId);
    }


}

