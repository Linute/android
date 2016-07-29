package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpInfo implements Parcelable{

    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mCollegeId;
    private String mCollegeName;
    private Uri mImage;


    public SignUpInfo() {
        mFirstName = "";
        mLastName = "";
        mCollegeName = "";
        mEmail = "";
        mCollegeId = null;
        mImage = null;
    }


    protected SignUpInfo(Parcel in) {
        mFirstName = in.readString();
        mLastName = in.readString();
        mEmail = in.readString();
        mCollegeId = in.readString();
        mCollegeName = in.readString();
        mImage = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<SignUpInfo> CREATOR = new Creator<SignUpInfo>() {
        @Override
        public SignUpInfo createFromParcel(Parcel in) {
            return new SignUpInfo(in);
        }

        @Override
        public SignUpInfo[] newArray(int size) {
            return new SignUpInfo[size];
        }
    };

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getCollegeId() {
        return mCollegeId;
    }

    public void setCollegeId(String collegeId) {
        mCollegeId = collegeId;
    }

    public String getCollegeName() {
        return mCollegeName;
    }

    public void setCollegeName(String collegeName) {
        mCollegeName = collegeName;
    }

    public Uri getImage() {
        return mImage;
    }

    public void setImage(Uri image) {
        mImage = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mEmail);
        dest.writeString(mCollegeId);
        dest.writeString(mCollegeName);
        dest.writeParcelable(mImage, 0);
    }
}
