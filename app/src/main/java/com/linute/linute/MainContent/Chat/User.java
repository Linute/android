package com.linute.linute.MainContent.Chat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Arman on 1/19/16.
 */
public class User implements Parcelable {
    public final String userId;
    public String userImage;
    public final String firstName;
    public final String lastName;
    public final String collegeName;


    public User(String userId, String firstName, String lastName, String userImage) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userImage = userImage;
        this.collegeName = null;
    }

    public User(String userId, String firstName, String lastName, String userImage, String collegeName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userImage = userImage;
        this.collegeName = collegeName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userId);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(userImage);
        parcel.writeString(collegeName);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(
                    parcel.readString(),
                    parcel.readString(),
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

    public static int findUser(List<User> users, User searchUser) {
        return findUser(users, searchUser.userId);
    }

    public static int findUser(List<User> users, String id) {
        if (users == null) return -1;
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.userId.equals(id)) {
                return i;
            }
        }
        return -1;
    }

}
