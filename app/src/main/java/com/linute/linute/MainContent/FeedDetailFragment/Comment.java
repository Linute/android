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
    private String mCommentUserPostText;
    private String mCommentUserPostId;
    private long mDateLong;
    private boolean mIsAnon;
    private String mAnonImage;

    private List<MentionedPersonLight> mMentionedPeople;


    public Comment() {
        mCommentUserId = "";
        mCommentUserProfileImage = "";
        mCommentUserName = "";
        mCommentUserPostText = "";
        mCommentUserPostId = "";
        mAnonImage = "";
        mMentionedPeople = new ArrayList<>();
        mDateLong = 0;
    }

    public Comment(String commentUserId,
                   String commentUserProfileImage,
                   String commentUserName,
                   String commentUserPostText,
                   String commentUserPostId, boolean isAnon,
                   String anonImage,
                   List<MentionedPersonLight> mentionedPeople,
                   long date
    ) {
        mCommentUserId = commentUserId;
        mCommentUserProfileImage = commentUserProfileImage;
        mCommentUserName = commentUserName;
        mCommentUserPostText = commentUserPostText;
        mCommentUserPostId = commentUserPostId;
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

    public String getCommentUserPostText() {
        return mCommentUserPostText;
    }

    public String getCommentUserPostId() {
        return mCommentUserPostId;
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
