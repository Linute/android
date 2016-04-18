package com.linute.linute.MainContent.PeopleFragment;


import java.util.List;

/**
 * Created by Arman on 1/8/16.
 */
public class People {
    private String mProfileImage;
    private String mName;
    private String mID;
    private String mDate; //will we used as distance as well

    private boolean mFriend;
    private String mStatus;
    private int mRank = -1;
    private String mSchoolName;

    private List<PersonRecentPost> mPersonRecentPosts;

    public People(String profileImage, String name, String ID, String date, boolean friend, String status) {
        mProfileImage = profileImage;
        mName = name;
        mID = ID;
        mDate = date;
        mFriend = friend;
        mStatus = status;
    }

    public String getStatus(){
        return mStatus;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public String getName() {
        return mName;
    }

    public String getID() {
        return mID;
    }

    public String getDate() {
        return mDate;
    }

    public boolean isFriend() {
        return mFriend;
    }

    public void setFriend(boolean friend) {
        mFriend = friend;
    }

    public int getRank() {
        return mRank;
    }

    public void setRank(int rank){
        mRank = rank;
    }

    public List<PersonRecentPost> getPersonRecentPosts() {
        return mPersonRecentPosts;
    }

    public void setPersonRecentPosts(List<PersonRecentPost> personRecentPosts) {
        mPersonRecentPosts = personRecentPosts;
    }

    public String getSchoolName() {
        return mSchoolName;
    }

    public void setSchoolName(String schoolName) {
        mSchoolName = schoolName;
    }

    public static class PersonRecentPost {
        private String mImage;
        private String mPostId;

        public PersonRecentPost (String image, String postId) {
            mImage = image;
            mPostId = postId;
        }

        public String getImage() {
            return mImage;
        }

        public String getPostId() {
            return mPostId;
        }
    }
}
