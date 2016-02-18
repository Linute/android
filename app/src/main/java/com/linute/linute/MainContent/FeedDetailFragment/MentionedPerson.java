package com.linute.linute.MainContent.FeedDetailFragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.linkedin.android.spyglass.mentions.Mentionable;

/**
 * Created by QiFeng on 2/15/16.
 */
public class MentionedPerson implements Mentionable {

    private String mFullname;
    private String mUserId;
    private String mProfileImage;


    public MentionedPerson(String fullname, String userID, String profileImage){
        mFullname = fullname;
        mUserId = userID;
        mProfileImage = profileImage;
    }



    public String getProfileImage() {
        return mProfileImage;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getFullname() {
        return mFullname;
    }

    public String getUserName() {
        return "@"+mFullname.replace(" ","")+" ";
    }


    @NonNull
    @Override
    public String getTextForDisplayMode(MentionDisplayMode mode) {
        return getUserName();
    }

    @Override
    public MentionDeleteStyle getDeleteStyle() {
        return MentionDeleteStyle.FULL_DELETE;
    }

    @Override
    public int getSuggestibleId() {
        return getFullname().hashCode();
    }

    @Override
    public String getSuggestiblePrimaryText() {
        return "@"+mFullname.replace(" ","");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFullname);
        dest.writeString(mUserId);
        dest.writeString(mProfileImage);
    }

    public MentionedPerson(Parcel in){
        mFullname = in.readString();
        mUserId = in.readString();
        mProfileImage = in.readString();
    }

    public static final Parcelable.Creator<MentionedPerson> CREATOR = new Parcelable.Creator<MentionedPerson>(){
        public MentionedPerson createFromParcel(Parcel in){
            return new MentionedPerson(in);
        }

        @Override
        public MentionedPerson[] newArray(int size) {
            return new MentionedPerson[size];
        }
    };
}
