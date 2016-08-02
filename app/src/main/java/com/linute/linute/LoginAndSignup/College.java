package com.linute.linute.LoginAndSignup;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by QiFeng on 1/12/16.
 */
public class College implements Parcelable {
    private String mCollegeName;
    private String mCollegeId;
    private String mAddress;
    private ArrayList<String> mExcludedEmails;
    private ArrayList<String> mIncludedEmails;

    public College (JSONObject json) throws JSONException {
        mCollegeName = json.getString("name");
        mCollegeId = json.getString("id");

        try {
            mAddress = "";
            mAddress += json.getString("address") + ", " + json.getString("city")+ ", " + json.getString("state") + " "+json.getString("zip");
        }catch (JSONException e){
            e.printStackTrace();
            mAddress = "";
        }
    }

    public College(String name,
                   String id,
                   String address,
                   ArrayList<String> includedEmails,
                   ArrayList<String> excludedEmails){
        mCollegeName = name;
        mCollegeId = id;
        mAddress = address;
        mExcludedEmails = excludedEmails;
        mIncludedEmails = includedEmails;
    }

    protected College(Parcel in) {
        mCollegeName = in.readString();
        mCollegeId = in.readString();
        mAddress = in.readString();
        mExcludedEmails = in.createStringArrayList();
        mIncludedEmails = in.createStringArrayList();
    }

    public static final Creator<College> CREATOR = new Creator<College>() {
        @Override
        public College createFromParcel(Parcel in) {
            return new College(in);
        }

        @Override
        public College[] newArray(int size) {
            return new College[size];
        }
    };

    public ArrayList<String> getExcludedEmails() {
        return mExcludedEmails;
    }

    public ArrayList<String> getIncludedEmails() {
        return mIncludedEmails;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getCollegeName() {
        return mCollegeName;
    }

    public String getCollegeId() {
        return mCollegeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCollegeName);
        dest.writeString(mCollegeId);
        dest.writeString(mAddress);
        dest.writeStringList(mExcludedEmails);
        dest.writeStringList(mIncludedEmails);
    }
}
