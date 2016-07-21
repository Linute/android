package com.linute.linute.MainContent.Chat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Arman on 1/19/16.
 */
public class User implements Parcelable{
    public final String userId;
    public final String userImage;
    public final String userName;

//    public User() {
//        userId = userImage = userName = "";
//    }

    public User(String userId, String userName, String userImage) {
        this.userId = userId;
        this.userImage = userImage;
        this.userName = userName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(userName);
        parcel.writeString(userImage);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(
                    parcel.readString(),
                    parcel.readString(),
                    parcel.readString()
            );
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };

    public static int findUser(List<User> users, User searchUser){
        return findUser(users, searchUser.userId);
    }

    public static int findUser(List<User> users, String id){
        if(users == null) return -1;
        for(int i=0;i<users.size();i++){
            User user = users.get(i);
            if(user.userId.equals(id)){
                return i;
            }
        }
        return -1;
    }

}
