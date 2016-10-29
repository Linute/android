package com.linute.linute.MainContent.EditScreen;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mikhail on 10/28/16.
 */

public final class PostOptions implements Parcelable {


    public enum ContentType {
        None, Photo, Video, UploadedPhoto, UploadedVideo
    }

    public enum ContentSubType {
        None, Post, Chat, Comment, Comment_No_Anon
    }

    public ContentType type;
    public ContentSubType subType;
    public String trendId;


    public PostOptions() {
        type = ContentType.None;
        subType = ContentSubType.None;
        trendId = null;
    }

    public PostOptions(
            ContentType type,
            ContentSubType subType,
            String trendId
    ) {
        this.type = type;
        this.subType = subType;
        this.trendId = trendId;
    }

    protected PostOptions(Parcel in) {
        type = (ContentType) in.readSerializable();
        subType = (ContentSubType) in.readSerializable();
        trendId = in.readString();
    }

    public static final Creator<PostOptions> CREATOR = new Creator<PostOptions>() {
        @Override
        public PostOptions createFromParcel(Parcel in) {
            return new PostOptions(in);
        }

        @Override
        public PostOptions[] newArray(int size) {
            return new PostOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(type);
        dest.writeSerializable(subType);
        dest.writeString(trendId);
    }


}
