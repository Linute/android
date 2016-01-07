package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.linute.linute.API.API_Methods;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

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
    private String mSocialTwitter;
    private String mStatus;
    private int mSex;
    private String mIsDeleted;
    private String mIsBanned;
    private String mProfileImage;
    private String mRegistrationDate;
    private String mDob;
    private int mFriendsNumber;
    private int mAttendedNumber;
    private int mHostedNumber;
    private String mCollege;
    private String mCampus;

    private int mPosts;
    private int mFollowers;
    private int mFollowing;
    //private String mPointsNumber;
    //private Map<String,String> mFriendships;


    public LinuteUser() {
    }


    public static LinuteUser getDefaultUser(Context context){

        LinuteUser user = new LinuteUser();
        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        user.setProfileImage(sharedPreferences.getString("profileImage", ""));
        user.setFirstName(sharedPreferences.getString("firstName", ""));
        user.setLastName(sharedPreferences.getString("lastName", ""));
        user.setStatus(sharedPreferences.getString("status", ""));
        user.setPosts(sharedPreferences.getInt("posts", 0));
        user.setFollowers(sharedPreferences.getInt("followers", 0));
        user.setFollowing(sharedPreferences.getInt("following", 0));

        return user;
    }

    public LinuteUser(JSONObject userInfo){

        mUserID = getStringFromJson("id", userInfo);
        mUserName = getStringFromJson("userName", userInfo);
        mRegistrationType = getStringFromJson("registrationType", userInfo);
        mFirstName = getStringFromJson("firstName", userInfo);
        mLastName = getStringFromJson("lastName", userInfo);
        mEmail = getStringFromJson("email", userInfo);
        mPhone = getStringFromJson("phone", userInfo);
        mSocialFacebook = getStringFromJson("socialFaceBook", userInfo);
        mSocialTwitter = getStringFromJson("socialTwitter", userInfo);
        mStatus = getStringFromJson("status", userInfo);
        mSex = getIntFromJson("sex", userInfo);
        mIsDeleted = getStringFromJson("isDeleted", userInfo);
        mIsBanned = getStringFromJson("isBanned", userInfo);
        mProfileImage = getStringFromJson("profileImage", userInfo);
        mRegistrationDate = getStringFromJson("registrationDate", userInfo);
        mDob = getStringFromJson("dob", userInfo);
        // OLD
        mFriendsNumber = getIntFromJson("numberOfFriends", userInfo);
        mHostedNumber = getIntFromJson("numberOfEvents", userInfo);
        mAttendedNumber = getIntFromJson("numberOfAttended", userInfo);
        // NEW start
        mPosts = getIntFromJson("numberOfEvents", userInfo);
        mFollowers = getIntFromJson("numberOfFollowers", userInfo);
        mFollowing = getIntFromJson("numberOfFollowing", userInfo);
        // NEW end
        mCollege = getStringFromJson("college", userInfo);
        mCampus = getStringFromJson("campus", userInfo);
    }

    private static String getStringFromJson(String key, JSONObject userInfo){
        String value;
        try {
            value = userInfo.getString(key);
        } catch (JSONException e) {
            value = null;
        }
        return value;
    }

    private static int getIntFromJson(String key, JSONObject userInfo){
        int value;
        try {
            value = userInfo.getInt(key);
        }catch (JSONException e){
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

    public String getSocialTwitter() {
        return mSocialTwitter;
    }

    public void setSocialTwitter(String socialTwitter) {
        mSocialTwitter = socialTwitter;
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


    public int getFriendsNumber() {
        return mFriendsNumber;
    }

    public void setFriendsNumber(int friendsNumber) {
        mFriendsNumber = friendsNumber;
    }

    public int getAttendedNumber() {
        return mAttendedNumber;
    }

    public void setAttendedNumber(int attendedNumber) {
        mAttendedNumber = attendedNumber;
    }

    public int getHostedNumber() {
        return mHostedNumber;
    }

    public void setHostedNumber(int hostedNumber) {
        mHostedNumber = hostedNumber;
    }

    /*
    public Map<String, String> getFriendships() {
        return mFriendships;
    }

    public void setFriendships(Map<String, String> friendships) {
        mFriendships = friendships;
    }

    public String getPointsNumber() {
        return mPointsNumber;
    }

    public void setPointsNumber(String pointsNumber) {
        mPointsNumber = pointsNumber;
    }*/

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

    public String getCollege() {
        return mCollege;
    }

    public int getPosts() {
        return mPosts;
    }

    public void setPosts(int posts) {mPosts = posts;}

    public int getFollowers() {
        return mFollowers;
    }

    public void setFollowers(int followers){mFollowers = followers;}

    public int getFollowing() {
        return mFollowing;
    }

    public void setFollowing(int following){ mFollowing =following;}


    /*TODO: SEARCH
        searchUserByName
        searchUserByFacebook
        searchUserByContacts
        searchFriends
        searchFriendsForInvite

     */

    /*TODO: RESET PASSWORDS
        resetPassword
        resetPasswordByPhone
     */

    /*TODO: CHANGE FRIENDSHIPS
        changeFriendStatus
        changeFriendStatusDelete
        confirmPhone
     */

    /* TODO: ACTIVITIES
        getActivities
        readActivities
     */

}
