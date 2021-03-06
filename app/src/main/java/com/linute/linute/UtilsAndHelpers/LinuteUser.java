package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiFeng on 11/17/15.
 */
public class LinuteUser {

    private String mUserID;
    private String mUserName;
    private String mRegistrationType;
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPhone;
    private String mSocialFacebook;
    private String mStatus;
    private int mSex;
    private String mIsDeleted;
    private String mIsBanned;
    private String mProfileImage;
    private String mRegistrationDate;
    private String mDob;
    private String mCollegeName;
    private String mCollegeId;
    private String mCampus;
    private int mPosts;
    private int mFollowers;
    private int mFollowing;
    private String mFriend;
    private String mFriendship;
    private boolean mIsSubscribed;
    private boolean mIsBlocked;
    private String mPoints;
    private String mUserToken;
    private boolean mInformationLoaded;

    private boolean mIsCompany;

    public LinuteUser() {
        mFriend = "";
        mFriendship = "";
        mFirstName = "";
        mIsSubscribed = false;
        mInformationLoaded = false;
    }


    public static LinuteUser getDefaultUser(Context context) {

        LinuteUser user = new LinuteUser();
        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        user.setProfileImage(sharedPreferences.getString("profileImage", ""));
        user.setFirstName(sharedPreferences.getString("firstName", ""));
        user.setLastName(sharedPreferences.getString("lastName", ""));
        user.setStatus(sharedPreferences.getString("status", ""));
        user.setPosts(sharedPreferences.getInt("posts", 0));
        user.setFollowers(sharedPreferences.getInt("followers", 0));
        user.setFollowing(sharedPreferences.getInt("following", 0));
        user.setCollegeName(sharedPreferences.getString("collegeName", ""));
        user.setCollegeId(sharedPreferences.getString("collegeId", ""));
        user.setUserID(sharedPreferences.getString("userID", ""));

        user.setUserToken(sharedPreferences.getString("userToken",""));
        user.setPoints(sharedPreferences.getString("points", "0"));

        user.setSubscribed(false);
        user.setBlocked(false);
        user.setInformationLoaded(true);

        return user;
    }

    public static CollegeNameAndID getCollegeFromJson(JSONObject userInfo) {
        JSONObject college = getJsonObjectFromJson("college", userInfo);
        if (college == null) return null;

        String collegeName = getStringFromJson("name", college);
        String collegeId = getStringFromJson("id", college);
        if (collegeName == null || collegeId == null) return null;

        return new CollegeNameAndID(collegeName, collegeId);
    }

    public void updateUserInformation(JSONObject userInfo) {
        mUserID = getStringFromJson("id", userInfo);
        mUserName = getStringFromJson("userName", userInfo);
        mRegistrationType = getStringFromJson("registrationType", userInfo);
        mFirstName = getStringFromJson("firstName", userInfo);
        mLastName = getStringFromJson("lastName", userInfo);
        mEmail = getStringFromJson("email", userInfo);
        mPhone = getStringFromJson("phone", userInfo);
        mSocialFacebook = getStringFromJson("socialFaceBook", userInfo);
        mStatus = getStringFromJson("status", userInfo);
        mSex = getIntFromJson("sex", userInfo);
        mIsDeleted = getStringFromJson("isDeleted", userInfo);
        mIsBanned = getStringFromJson("isBanned", userInfo);
        mProfileImage = getStringFromJson("profileImage", userInfo);
        mRegistrationDate = getStringFromJson("registrationDate", userInfo);
        mDob = getStringFromJson("dob", userInfo);
        // OLD
//        mFriendsNumber = getIntFromJson("numberOfFriends", userInfo);
//        mHostedNumber = getIntFromJson("numberOfEvents", userInfo);
//        mAttendedNumber = getIntFromJson("numberOfAttended", userInfo);
        // NEW start
        mPosts = getIntFromJson("numberOfEvents", userInfo);
        mFollowers = getIntFromJson("numberOfFollowers", userInfo);
        mFollowing = getIntFromJson("numberOfFollowing", userInfo);
        mIsBlocked = getBooleon("isBlocked", userInfo);
        mInformationLoaded = true;

        mPoints = getStringFromJson("points", userInfo);
        mUserToken = getStringFromJson("token", userInfo);

        try {
            mIsSubscribed = userInfo.getBoolean("isSubscribed");
        }catch (JSONException e){
            mIsSubscribed = false;
        }

        mFriend = "";
        mFriendship = "";
        try {
            if (!userInfo.isNull("friend") && !userInfo.getJSONObject("friend").getString("status").equals("removed")) {
                mFriend = userInfo.getJSONObject("friend").getString("user");
                mFriendship = userInfo.getJSONObject("friend").getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // NEW end

        JSONObject college = getJsonObjectFromJson("college", userInfo);

        if (college != null) {
            mCollegeName = getStringFromJson("name", college);
            mCollegeId = getStringFromJson("id", college);
        }
        mCampus = getStringFromJson("campus", userInfo);

        mIsCompany = getBooleon("isCompany", userInfo);
    }

    public LinuteUser(JSONObject userInfo) {
        mUserID = getStringFromJson("id", userInfo);
        mUserName = getStringFromJson("userName", userInfo);
        mRegistrationType = getStringFromJson("registrationType", userInfo);
        mFirstName = getStringFromJson("firstName", userInfo);
        mLastName = getStringFromJson("lastName", userInfo);
        mEmail = getStringFromJson("email", userInfo);
        mPhone = getStringFromJson("phone", userInfo);
        mSocialFacebook = getStringFromJson("socialFacebook", userInfo);
        mStatus = getStringFromJson("status", userInfo);
        mSex = getIntFromJson("sex", userInfo);
        mIsDeleted = getStringFromJson("isDeleted", userInfo);
        mIsBanned = getStringFromJson("isBanned", userInfo);
        mProfileImage = getStringFromJson("profileImage", userInfo);
        mRegistrationDate = getStringFromJson("registrationDate", userInfo);
        mDob = getStringFromJson("dob", userInfo);
        mPosts = getIntFromJson("numberOfEvents", userInfo);
        mFollowers = getIntFromJson("numberOfFollowers", userInfo);
        mFollowing = getIntFromJson("numberOfFollowing", userInfo);

        try {
            mIsSubscribed = userInfo.getBoolean("isSubscribed");
        }catch (JSONException e){
            mIsSubscribed = false;
        }

        JSONObject college = getJsonObjectFromJson("college", userInfo);

        if (college != null) {
            mCollegeName = getStringFromJson("name", college);
            mCollegeId = getStringFromJson("id", college);
        }
        mInformationLoaded = true;
        mPoints = getStringFromJson("points", userInfo);
        mUserToken = getStringFromJson("token", userInfo);
        mFriend = "";
        mFriendship = "";
        try {
            if (!userInfo.getString("friend").equals("") || !userInfo.getJSONObject("friend").getString("status").equals("removed")) {
                mFriend = userInfo.getJSONObject("friend").getString("user");
                mFriendship = userInfo.getJSONObject("friend").getString("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mCampus = getStringFromJson("campus", userInfo);

        mIsCompany = getBooleon("isCompany", userInfo);

    }

    private static boolean getBooleon(String key, JSONObject object){
        try {
            return object.getBoolean(key);
        }catch (JSONException e){
            e.printStackTrace();
            return false;
        }
    }

    private static JSONObject getJsonObjectFromJson(String key, JSONObject json) {
        try {
            return json.getJSONObject(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getStringFromJson(String key, JSONObject userInfo) {
        String value;
        try {
            value = userInfo.getString(key);
        } catch (JSONException e) {
            value = null;
        }
        return value;
    }

    private static int getIntFromJson(String key, JSONObject userInfo) {
        int value;
        try {
            value = userInfo.getInt(key);
        } catch (JSONException e) {
            value = 0;
        }
        return value;
    }


    /* Getters and Setters */

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getSocialFacebook() {
        return mSocialFacebook;
    }

    public void setSocialFacebook(String socialFacebook) {
        mSocialFacebook = socialFacebook;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public int getSex() {
        return mSex;
    }

    public void setSex(int sex) {
        mSex = sex;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String profileImage) {
        mProfileImage = profileImage;
    }

    public String getDob() {
        return mDob;
    }

    public void setDob(String dob) {
        mDob = dob;
    }

    public String getRegistrationDate() {
        return mRegistrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        mRegistrationDate = registrationDate;
    }

    public String getIsBanned() {
        return mIsBanned;
    }

    public void setIsBanned(String isBanned) {
        mIsBanned = isBanned;
    }

    public String getIsDeleted() {
        return mIsDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        mIsDeleted = isDeleted;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getRegistrationType() {
        return mRegistrationType;
    }

    public void setRegistrationType(String registrationType) {
        mRegistrationType = registrationType;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    public String getCampus() {
        return mCampus;
    }

    public String getCollegeName() {
        return mCollegeName;
    }

    public void setCollegeName(String college) {
        mCollegeName = college;
    }

    public String getCollegeId() {
        return mCollegeId;
    }

    public void setCollegeId(String id) {
        mCollegeId = id;
    }

    public int getPosts() {
        return mPosts;
    }

    public void setPosts(int posts) {
        mPosts = posts;
    }

    public int getFollowers() {
        return mFollowers;
    }

    public void setFollowers(int followers) {
        mFollowers = followers;
    }

    public int getFollowing() {
        return mFollowing;
    }

    public void setFollowing(int following) {
        mFollowing = following;
    }


    public boolean isSubscribed() {
        return mIsSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        mIsSubscribed = subscribed;
    }

    public boolean isBlocked() {
        return mIsBlocked;
    }

    public void setBlocked(boolean blocked) {
        mIsBlocked = blocked;
    }

    public String getFriend() {
        return mFriend;
    }

    public void setFriend(String friend) {
        mFriend = friend;
    }

    public String getFriendship() {
        return mFriendship;
    }

    public void setFriendship(String friendship) {
        mFriendship = friendship;
    }

    public String getPoints(){
        return mPoints;
    }

    public String getUserToken() {
        return mUserToken;
    }

    public void setUserToken(String userToken) {
        mUserToken = userToken;
    }

    public void setPoints(String points) {
        mPoints = points;
    }

    public boolean isInformationLoaded() {
        return mInformationLoaded;
    }

    public void setInformationLoaded(boolean informationLoaded) {
        mInformationLoaded = informationLoaded;
    }


    public boolean getIsCompany(){ return mIsCompany;}


    public static class CollegeNameAndID {
        private String mCollegeName;
        private String mCollegeId;

        public CollegeNameAndID(String name, String id) {
            mCollegeId = id;
            mCollegeName = name;
        }

        public String getCollegeName() {
            return mCollegeName;
        }

        public String getCollegeId() {
            return mCollegeId;
        }
    }
}
