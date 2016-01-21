package com.linute.linute.MainContent.Chat;

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

    public Rooms() {
        mOwnerId = "";
        mRoomId = "";
        mLastMessageOwnerId = "";
        mLastMessageUserName = "";
        mLastMessage = "";
        mLastMessageUserImage = "";
    }

    public Rooms(String ownerId, String roomId, String lastMessageOwnerId, String lastMessageUserName, String lastMessage, String lastMessageUserImage) {
        mOwnerId = ownerId;
        mRoomId = roomId;
        mLastMessageOwnerId = lastMessageOwnerId;
        mLastMessageUserName = lastMessageUserName;
        mLastMessage = lastMessage;
        mLastMessageUserImage = lastMessageUserImage;
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
}
