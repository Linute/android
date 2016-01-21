package com.linute.linute.MainContent.Chat;

/**
 * Created by Arman on 1/16/16.
 */
public class Chat {
    private String mRoomId;
    private String mUserImage;
    private String mUserName;
    private String mShortDate;
    private String mOwnerId;
    private String mMessageId;
    private String mMessage;

    public Chat() {
        mRoomId = "";
        mUserImage = "";
        mUserName = "";
        mShortDate = "";
        mOwnerId = "";
        mMessageId = "";
        mMessage = "";
    }

    public Chat(String roomId, String userImage, String userName, String shortDate, String ownerId, String messageId, String message) {
        mRoomId = roomId;
        mUserImage = userImage;
        mUserName = userName;
        mShortDate = shortDate;
        mOwnerId = ownerId;
        mMessageId = messageId;
        mMessage = message;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public String getUserImage() {
        return mUserImage;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getShortDate() {
        return mShortDate;
    }

    public String getOwnerId() {
        return mOwnerId;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public String getMessage() {
        return mMessage;
    }
}
