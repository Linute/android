package com.linute.linute.MainContent.FeedDetailFragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Arman on 1/13/16.
 */
public class Comment implements Parcelable {

    public static final short COMMENT_TEXT = 0;
    public static final short COMMENT_IMAGE = 1;

    private String mCommentUserId;
    private String mCommentUserProfileImage;
    private String mCommentUserName;
    private String mCommentPostText;
    private String mCommentPostId;
    private long mDateLong;
    private boolean mIsAnon;
    private boolean mIsLiked;
    private String mAnonImage;
    private int mNumberOfLikes;

    private String mImageUrl;
    private int mType;

    public boolean hasPrivacyChanged;


    private ArrayList<MentionedPersonLight> mMentionedPeople;


    public Comment() {
        mCommentUserId = "";
        mCommentUserProfileImage = "";
        mCommentUserName = "";
        mCommentPostText = "";
        mCommentPostId = "";
        mAnonImage = "";
        mMentionedPeople = new ArrayList<>();
        mDateLong = 0;
        mNumberOfLikes = 0;
        mType = 0;
    }

    public Comment(JSONObject comment) throws JSONException  {
        ArrayList<Comment.MentionedPersonLight> tempMentioned = new ArrayList<>();
        JSONArray mentionedPeople = comment.getJSONArray("userMentions");

        for (int j = 0; j < mentionedPeople.length(); j++) { //get all the mentioned people
            tempMentioned.add(
                    new Comment.MentionedPersonLight( //just need fullname and id
                            mentionedPeople.getJSONObject(j).getString("fullName"),
                            mentionedPeople.getJSONObject(j).getString("id")
                    )
            );
        }

        mMentionedPeople = tempMentioned;
        mCommentPostText = comment.getString("text");
        mCommentPostId = comment.getString("id");
        mIsAnon = comment.getInt("privacy") == 1;
        mAnonImage = comment.getString("anonymousImage");

        hasPrivacyChanged = comment.has("isPrivacyChanged") && comment.getBoolean("isPrivacyChanged");

        try{
            mNumberOfLikes = comment.getInt("numberOfLikes");
            mIsLiked = comment.getBoolean("isLiked");
        }catch (JSONException e){
            mNumberOfLikes = 0;
            mIsLiked = false;
        }

        try {
            mDateLong = Utils.getDateFormat().parse(comment.getString("date")).getTime();
        } catch (ParseException|JSONException e) {
            mDateLong = 0;
        }

        mImageUrl = getImageUrl(comment.getJSONArray("images"));
        mType = mImageUrl == null ? 0 : 1;

        setUpOwner(comment);
    }


    private String getImageUrl(JSONArray images) {
        if (images == null || images.length() == 0) return null;
        try {
            return images.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setUpOwner(JSONObject comment){
        try {
            JSONObject owner = comment.getJSONObject("owner");
            mCommentUserName = owner.getString("fullName");
            mCommentUserId = owner.getString("id");
            mCommentUserProfileImage = owner.getString("profileImage");
        }catch (JSONException e){
            mCommentUserId = null;
            mCommentUserName = "";
            mCommentUserProfileImage = "";
        }
    }


    protected Comment(Parcel in) {
        mCommentUserId = in.readString();
        mCommentUserProfileImage = in.readString();
        mCommentUserName = in.readString();
        mCommentPostText = in.readString();
        mCommentPostId = in.readString();
        mDateLong = in.readLong();
        mIsAnon = in.readByte() != 0;
        mIsLiked = in.readByte() != 0;
        mAnonImage = in.readString();
        mNumberOfLikes = in.readInt();
        mImageUrl = in.readString();
        mType = in.readInt();
        hasPrivacyChanged = in.readByte() != 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Nullable
    public String getCommentUserId() {
        return mCommentUserId;
    }

    public String getCommentUserProfileImage() {
        return mCommentUserProfileImage;
    }

    public String getCommentUserName() {
        return mCommentUserName;
    }

    public String getCommentPostText() {
        return mCommentPostText;
    }

    public String getCommentPostId() {
        return mCommentPostId;
    }

    public boolean isAnon() {
        return mIsAnon;
    }

    public boolean isLiked() {
        return mIsLiked;
    }

    public boolean toggleLiked() {
        mIsLiked = !mIsLiked;
        return mIsLiked;
    }

    public int getNumberOfLikes() {
        return mNumberOfLikes;
    }

    public int decrementLikes() {
        return --mNumberOfLikes;
    }

    public int incrementLikes() {
        return ++mNumberOfLikes;
    }

    public ArrayList<MentionedPersonLight> getMentionedPeople() {
        return mMentionedPeople;
    }

    public String getAnonImage() {
        return mAnonImage;
    }

    public void setIsAnon(boolean anon) {
        mIsAnon = anon;
    }

    public String getDateString() {
        return mDateLong == 0 ? "" : Utils.getTimeAgoString(mDateLong);
    }

    public void setAnonImage(String image) {
        mAnonImage = image;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public int getType() {
        return mType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCommentUserId);
        dest.writeString(mCommentUserProfileImage);
        dest.writeString(mCommentUserName);
        dest.writeString(mCommentPostText);
        dest.writeString(mCommentPostId);
        dest.writeLong(mDateLong);
        dest.writeByte((byte) (mIsAnon ? 1 : 0));
        dest.writeByte((byte) (mIsLiked ? 1 : 0));
        dest.writeString(mAnonImage);
        dest.writeInt(mNumberOfLikes);
        dest.writeString(mImageUrl);
        dest.writeInt(mType);
        dest.writeByte((byte) (hasPrivacyChanged ? 1 : 0));
    }


    public static class MentionedPersonLight {

        private String mFullname;
        private String mId;
        private String mFormattedName;


        public MentionedPersonLight(String fullName, String id) {
            mFullname = fullName;
            mId = id;
            mFormattedName = "@" + fullName.replaceAll("[^a-zA-Z]", "");
        }

        public String getId() {
            return mId;
        }

        public String getFullName() {
            return mFullname;
        }

        public String getFormattedFullName() {
            return mFormattedName;
        }
    }
}

