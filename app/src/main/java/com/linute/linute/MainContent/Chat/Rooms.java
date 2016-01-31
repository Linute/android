package com.linute.linute.MainContent.Chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/16/16.
 */
public class Rooms {
    private String mOwnerId;
    private String mRoomId;
    private String mLastMessageOwnerId;
    private String mLastMessageUserName;
    private String mLastMessage;
    private String mLastMessageUserImage;
    private int mUsersCount;
    private ArrayList<ChatHead> mChatHeadList;

    public Rooms() {
        mOwnerId = "";
        mRoomId = "";
        mLastMessageOwnerId = "";
        mLastMessageUserName = "";
        mLastMessage = "";
        mLastMessageUserImage = "";
        mChatHeadList = new ArrayList<>();
    }

    public Rooms(String ownerId, String roomId, String lastMessageOwnerId, String lastMessageUserName, String lastMessage, String lastMessageUserImage, int usersCount, ArrayList<ChatHead> chatHeadList) {
        mOwnerId = ownerId;
        mRoomId = roomId;
        mLastMessageOwnerId = lastMessageOwnerId;
        mLastMessageUserName = lastMessageUserName;
        mLastMessage = lastMessage;
        mLastMessageUserImage = lastMessageUserImage;
        mUsersCount = usersCount;
        mChatHeadList = chatHeadList;
    }

    public String getOwner() {
        return mOwnerId;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getLastMessageOwnerId() {
        return mLastMessageOwnerId;
    }

    public String getLastMessageUserName() {
        return mLastMessageUserName;
    }

    public String getLastMessage() {
        return mLastMessage;
    }

    public String getLastMessageUserImage() {
        return mLastMessageUserImage;
    }

    public int getUsersCount() {
        return mUsersCount;
    }

    public ArrayList<ChatHead> getChatHeadList() {
        return mChatHeadList;
    }
}
