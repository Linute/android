package com.linute.linute.MainContent.Chat;


import java.util.Date;

/**
 * Created by Arman on 1/16/16.
 */
public class Chat {
    public static final int TYPE_MESSAGE_ME = 0;
    public static final int TYPE_MESSAGE_OTHER_PERSON = 1;
    public static final int TYPE_ACTION_TYPING = 2;
    public static final int TYPE_DATE_HEADER = 3;
    public static final int TYPE_SYSTEM_MESSAGE = 4;

    public static final int MESSAGE_TEXT = 0;
    public static final int MESSAGE_VIDEO = 2;
    public static final int MESSAGE_IMAGE = 1;
    //public static final int TYPE_CHAT_HEAD = 3;

    private String mRoomId;
    private Date mDate;
    private String mOwnerId;
    private String mMessageId;
    private String mMessage;
    private int mType;
    private boolean mIsRead;
    private int mMessageType;
    private String mImageId;
    private String mVideoId;

    public boolean hasError = false;


    /**
     * @param type - use this constructer for others
     */
    public Chat(int type) {
        mRoomId = null;
        mDate = null;
        mOwnerId = "";
        mMessageId = "";
        mMessage = "";
        mIsRead = false;
        mType = type;
        mMessageType = 0;
    }


    /**
     * Use this constructer if you want a message item
     *
     * @param roomId    -   id of room
     * @param date      -   date of message or other
     * @param messageId -   id of message
     * @param message   -   text of message
     * @param read      -   read
     * @param isOwner   -   are you the owner of the message
     */
    public Chat(String roomId,
                Date date,
                String ownerId,
                String messageId,
                String message,
                boolean read,
                boolean isOwner
    ) {
        mRoomId = roomId;
        mDate = date;
        mOwnerId = ownerId;
        mMessageId = messageId;
        mMessage = message;
        mIsRead = read;
        mType = isOwner ? TYPE_MESSAGE_ME : TYPE_MESSAGE_OTHER_PERSON;
    }

    public String getImageId() {
        return mImageId;
    }

    public void setImageId(String imageId) {
        mImageId = imageId;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }

    public void setMessageType(int messageType) {
        mMessageType = messageType;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public Date getDate() {
        return mDate;
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


    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public boolean isRead() {
        return mIsRead;
    }

    public int getMessageType() {
        return mMessageType;
    }

    public void setIsRead(boolean isRead) {
        mIsRead = isRead;
    }
}
