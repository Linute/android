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

    //default to true, if something happens, it's better if the user can't rate, then having the
    // abilty to rate again
    private boolean mAlreadyRated = true;
    private int mTotalRatings;
    private List<RatingObject> mRatingObjects;

    private List<PersonRecentPost> mPersonRecentPosts;
    private int mCurrentRecyPosition = 0;

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

    public boolean isAlreadyRated() {
        return mAlreadyRated;
    }

    public void setAlreadyRated(boolean alreadyRated) {
        mAlreadyRated = alreadyRated;
    }

    public List<RatingObject> getRatingObjects() {
        return mRatingObjects;
    }

    public void incrementRateAtPosition(int pos){
        mRatingObjects.get(pos).incrementNumOfRates();
        mTotalRatings++;
    }

    public void setRatingObjects(List<RatingObject> ratingObjects) {
        mRatingObjects = ratingObjects;
        mTotalRatings = 0;
        for (RatingObject obj : ratingObjects){
            mTotalRatings += obj.getNumOfRates();
        }
    }

    public int getCurrentRecyPosition() {
        return mCurrentRecyPosition;
    }

    public void setCurrentRecyPosition(int currentRecyPosition) {
        mCurrentRecyPosition = currentRecyPosition;
    }

    public int getTotalRatings(){
        return mTotalRatings;
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

    public static class RatingObject {
        private String mKey;
        private int mNumOfRates;
        private String mName;

        public RatingObject(String key, String name, int ratings){
            mKey = key;
            mNumOfRates = ratings;
            mName = name;
        }

        public int getNumOfRates() {
            return mNumOfRates;
        }

        public void incrementNumOfRates() {
            mNumOfRates++;
        }

        public String getKey() {
            return mKey;
        }

        public String getName() {
            return mName;
        }
    }
}
