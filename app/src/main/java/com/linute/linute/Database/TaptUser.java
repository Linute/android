package com.linute.linute.Database;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by QiFeng on 8/11/16.
 */


/* Really light weight at the moment.
   Things that can be added:
        - College realm object
 */


public class TaptUser extends RealmObject {

    @PrimaryKey
    private String id;

    private String fullName;
    private String profileImage;
    private boolean isFriend;   // friends with this person
    private int numTimesSharedWith;

    public TaptUser(){

    }

    public static TaptUser getUser(JSONObject object, boolean insert) throws JSONException {
        return new TaptUser(
                object.getString("id"),
                object.getString("fullName"),
                object.getString("profileImage"),
                insert
        );
    }

    public TaptUser(String id, String fullName, String profileImage, boolean isFriend){
        this.id = id;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.isFriend = isFriend;
        this.numTimesSharedWith = 0;
    }

    public void update(TaptUser user){
        fullName = user.fullName;
        profileImage = user.profileImage;
        isFriend = user.isFriend;
    }


    public String getId() {
        return id;
    }


    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public String getFullName(){
        return fullName;
    }


    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void incrementNumTimeShared(){
        ++numTimesSharedWith;
    }

    public int getNumTimesSharedWith(){
        return numTimesSharedWith;
    }

}
