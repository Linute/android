package com.linute.linute.SquareCamera;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 7/14/16.
 */
public class CameraType implements Parcelable{

    public final static int CAMERA_PICTURE = 0b1;
    public final static int CAMERA_VIDEO = 0b10;
    public final static int CAMERA_GALLERY = 0b100;
    public final static int CAMERA_STATUS = 0b1000;
    public final static int CAMERA_EVERYTHING = 0b1111;

    private int mType;

    public CameraType(int start){
        mType = start;
    }

    protected CameraType(Parcel in) {
        mType = in.readInt();
    }

    public static final Creator<CameraType> CREATOR = new Creator<CameraType>() {
        @Override
        public CameraType createFromParcel(Parcel in) {
            return new CameraType(in);
        }

        @Override
        public CameraType[] newArray(int size) {
            return new CameraType[size];
        }
    };

    public CameraType add (int type){
        mType |= type;
        return this;
    }

    public boolean contains(int type){
        return (mType & type) > 0;
    }

    public int getType(){
        return mType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
    }
}
