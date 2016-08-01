package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.os.Parcel;

/**
 * Created by QiFeng on 7/31/16.
 */
public class FBSignUpInfo extends SignUpInfo {


    private int mSex;
    private String mUserId;
    private String mSocialFB;
    private String mDob;
    private String mRegistrationType;


    public FBSignUpInfo(String userId,
                        int sex,
                        String socialFB,
                        String dob,
                        String registrationType) {
        super();
        mSex = sex;
        mUserId = userId;
        mSocialFB = socialFB;
        mDob = dob;
        mRegistrationType = registrationType;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mSex);
        dest.writeString(mUserId);
        dest.writeString(mSocialFB);
        dest.writeString(mDob);
        dest.writeString(mRegistrationType);
    }

    public int getSex() {
        return mSex;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getSocialFB() {
        return mSocialFB;
    }

    public String getDob() {
        return mDob;
    }

    public String getRegistrationType() {
        return mRegistrationType;
    }


    protected FBSignUpInfo(Parcel in) {
        super(in);
        mSex = in.readInt();
        mUserId = in.readString();
        mSocialFB = in.readString();
        mDob = in.readString();
        mRegistrationType = in.readString();
    }

    public static final Creator<FBSignUpInfo> CREATOR = new Creator<FBSignUpInfo>() {
        @Override
        public FBSignUpInfo createFromParcel(Parcel in) {
            return new FBSignUpInfo(in);
        }

        @Override
        public FBSignUpInfo[] newArray(int size) {
            return new FBSignUpInfo[size];
        }
    };
}
