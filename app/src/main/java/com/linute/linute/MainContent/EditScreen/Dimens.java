package com.linute.linute.MainContent.EditScreen;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mikhail on 8/23/16.
 */
public class Dimens implements Parcelable {

    int height;                         //vid height
    int width;                          //vid width
    boolean isFrontFacing;              //image was taken with front facing camera
    int rotation;                       //video rotation
    boolean deleteVideoWhenFinished;    //delete cached video
    boolean needsCropping;


    //typically used when capturing video
    public Dimens(int width, int height, boolean isFrontFacing) {
        this.height = height;
        this.width = width;
        this.isFrontFacing = isFrontFacing;
        this.rotation = 90;
        this.deleteVideoWhenFinished = true;
        this.needsCropping = true;
    }


    //uploading image from galley
    public Dimens(int width, int height){
        this.height = height;
        this.width = width;
        this.isFrontFacing = false;
        this.rotation = 0;
        //should not delete gallery's video
        this.deleteVideoWhenFinished = false;
        //width will be height
        needsCropping = false;
    }


    public Dimens(int width, int height, int rotation) {
        this.height = height;
        this.width = width;
        this.isFrontFacing = false;
        this.rotation = rotation;

        //should not delete gallery's video
        this.deleteVideoWhenFinished = false;

        //width will be height
        needsCropping = (rotation == 90 || rotation == 270) && ((float) width / height) > 1.2;
    }

    public void setNeedsCropping(boolean needsCropping) {
        this.needsCropping = needsCropping;
    }

    protected Dimens(Parcel in) {
        height = in.readInt();
        width = in.readInt();
        isFrontFacing = in.readByte() == 1;
        rotation = in.readInt();
        deleteVideoWhenFinished = in.readByte() == 1;
        needsCropping = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeByte((byte) (isFrontFacing ? 1 : 0));
        dest.writeInt(rotation);
        dest.writeByte((byte) (deleteVideoWhenFinished ? 1 : 0));
        dest.writeByte((byte) (needsCropping ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Dimens> CREATOR = new Creator<Dimens>() {
        @Override
        public Dimens createFromParcel(Parcel in) {
            return new Dimens(in);
        }

        @Override
        public Dimens[] newArray(int size) {
            return new Dimens[size];
        }
    };
}

