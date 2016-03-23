package com.linute.linute.MainContent.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/16/16.
 */
public class Rooms {

    private String mRoomId;

    private String mUserId; //id of person youre talking to
    private String mUserName; //person youre talking to
    private String mUserImage; //image of user youre talking to

    private String mLastMessage; //last message

    private boolean mHasUnread;
    //private int mUsersCount;
    //private ArrayList<ChatHead> mChatHeadList;

    private long mTime;

    public Rooms() {
        mRoomId = "";
        mUserId = "";
        mUserName = "";
        mLastMessage = "";
        mUserImage = "";
        mHasUnread = false;
        mTime = 0;
        //mChatHeadList = new ArrayList<>();
    }


    //, int usersCount, ArrayList<ChatHead> chatHeadList

    public Rooms(String roomId,
                 String userId,
                 String userName,
                 String lastMessage,
                 String userImage,
                 boolean hasUnread,
                 long time
    ) {

        mRoomId = roomId;
        mUserId = userId;
        mUserName = userName;
        mLastMessage = lastMessage;
        mUserImage = userImage;
        mHasUnread = hasUnread;
        mTime = time;

        //mUsersCount = usersCount;
        //mChatHeadList = chatHeadList;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public String getUserImage() {
        return mUserImage;
    }

    public boolean hasUnread() {
        return mHasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        mHasUnread = hasUnread;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    //    public int getUsersCount() {
//        return mUsersCount;
//    }
//
//    public ArrayList<ChatHead> getChatHeadList() {
//        return mChatHeadList;
//    }
}
