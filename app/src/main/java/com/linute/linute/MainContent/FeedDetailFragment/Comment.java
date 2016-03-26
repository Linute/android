package com.linute.linute.MainContent.FeedDetailFragment;


import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */
public class Comment {
    private String mCommentUserId;
    private String mCommentUserProfileImage;
    private String mCommentUserName;
    private String mCommentPostText;
    private String mCommentPostId;
    private long mDateLong;
    private boolean mIsAnon;
    private String mAnonImage;


    private List<MentionedPersonLight> mMentionedPeople;


    public Comment() {
        mCommentUserId = "";
        mCommentUserProfileImage = "";
        mCommentUserName = "";
        mCommentPostText = "";
        mCommentPostId = "";
        mAnonImage = "";
        mMentionedPeople = new ArrayList<>();
        mDateLong = 0;
    }

    public Comment(String commentUserId,
                   String commentUserProfileImage,
                   String commentUserName,
                   String commentPostText,
                   String commentPostId,
                   boolean isAnon,
                   String anonImage,
                   List<MentionedPersonLight> mentionedPeople,
                   long date
    ) {
        mCommentUserId = commentUserId;
        mCommentUserProfileImage = commentUserProfileImage;
        mCommentUserName = commentUserName;
        mCommentPostText = commentPostText;
        mCommentPostId = commentPostId;
        mIsAnon = isAnon;
        mAnonImage = anonImage;
        mMentionedPeople = mentionedPeople;
        mDateLong = date;

    }

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

    public boolean isAnon(){
        return mIsAnon;
    }

    public List<MentionedPersonLight> getMentionedPeople(){
        return mMentionedPeople;
    }

    public String getAnonImage(){
        return mAnonImage;
    }

    public void setIsAnon(boolean anon) {
        mIsAnon = anon;
    }

    public String getDateString(){
        return mDateLong == 0 ? "" : Utils.getTimeAgoString(mDateLong);
    }


    public static class MentionedPersonLight{

        private String mFullname;
        private String mId;
        private String mFormattedName;


        public MentionedPersonLight(String fullName, String id){
            mFullname = fullName;
            mId = id;
            mFormattedName = "@"+fullName.replace(" ", "");
        }


        public String getId(){
            return mId;
        }

        public String getFullName(){
            return mFullname;
        }

        public String getFormatedFullName(){
            return mFormattedName;
        }
    }
}
