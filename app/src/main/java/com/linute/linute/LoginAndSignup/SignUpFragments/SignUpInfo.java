package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.linute.linute.LoginAndSignup.College;

/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpInfo implements Parcelable{

    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private College mCollege;
    private Uri mImage;
    private String mPassword;

    public SignUpInfo() {
        mFirstName = "";
        mLastName = "";
        mCollege = null;
        mEmail = "";
        mImage = null;
        mPassword = "";
    }

    protected SignUpInfo(Parcel in) {
        mFirstName = in.readString();
        mLastName = in.readString();
        mEmail = in.readString();
        mCollege = in.readParcelable(College.class.getClassLoader());
        mImage = in.readParcelable(Uri.class.getClassLoader());
        mPassword = in.readString();
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

    public Uri getImage() {
        return mImage;
    }

    public void setImage(Uri image) {
        mImage = image;
    }

    public College getCollege() {
        return mCollege;
    }

    public void setCollege(College college) {
        mCollege = college;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
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
        dest.writeParcelable(mCollege, 0);
        dest.writeParcelable(mImage, 0);
        dest.writeString(mPassword);
    }
}
