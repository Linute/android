package com.linute.linute.MainContent.FeedDetailFragment;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mikhail on 9/30/16.
 */
public class MentionedAnon extends MentionedPerson {


    private String mCommentId;

    public MentionedAnon(String fullname, String commentId, String profileImage) {
        super(fullname, "", profileImage);
        this.mCommentId = commentId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MentionedAnon && ((MentionedAnon) o).getCommentId().equals(this.mCommentId);
    }

    @Override
    public int hashCode() {
        return mCommentId.hashCode();
    }

    public String getCommentId(){
        return mCommentId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

    }

    public MentionedAnon(Parcel in) {
        super(in);
    }

    public static final Parcelable.Creator<MentionedPerson> CREATOR = new Parcelable.Creator<MentionedPerson>() {
        public MentionedPerson createFromParcel(Parcel in) {
            return new MentionedAnon(in);
        }

        @Override
        public MentionedPerson[] newArray(int size) {
            return new MentionedPerson[size];
        }
    };


}
