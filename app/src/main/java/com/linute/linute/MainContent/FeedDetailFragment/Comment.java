package com.linute.linute.MainContent.FeedDetailFragment;


import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/13/16.
 */
public class Comment {

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
        mNumberOfLikes = 0;
        mType = 0;
    }

    public Comment(String commentUserId,
                   String commentUserProfileImage,
                   String commentUserName,
                   String commentPostText,
                   String commentPostId,
                   boolean isAnon,
                   String anonImage,
                   List<MentionedPersonLight> mentionedPeople,
                   long date,
                   boolean isLiked,
                   int numberOfLikes,
                   String imageUrl
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
        mIsLiked = isLiked;
        mNumberOfLikes = numberOfLikes;
        mImageUrl = imageUrl;
        mType = imageUrl == null ? 0 : 1;
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

    public boolean isLiked(){
        return mIsLiked;
    }

    public boolean toggleLiked(){
        mIsLiked = !mIsLiked;
        return mIsLiked;
    }

    public int getNumberOfLikes(){
        return mNumberOfLikes;
    }

    public int decrementLikes(){
        return --mNumberOfLikes;
    }

    public int incrementLikes(){
        return ++mNumberOfLikes;
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

    public void setAnonImage(String image){
        mAnonImage = image;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public int getType() {
        return mType;
    }



    public static class MentionedPersonLight{

        private String mFullname;
        private String mId;
        private String mFormattedName;


        public MentionedPersonLight(String fullName, String id){
            mFullname = fullName;
            mId = id;
            mFormattedName = "@"+fullName.replaceAll("[^a-zA-Z]", "");
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
