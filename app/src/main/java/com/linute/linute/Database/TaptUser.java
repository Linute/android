package com.linute.linute.Database;

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

    public TaptUser(){

    }

    public TaptUser(String id, String fullName, String profileImage, boolean isFriend){
        this.id = id;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.isFriend = isFriend;
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
}
