package com.linute.linute.MainContent.Chat;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arman on 1/16/16.
 */
public class Chat {
    public static final int TYPE_MESSAGE_ME = 0;
    public static final int TYPE_MESSAGE_OTHER_PERSON = 1;
    public static final int TYPE_ACTION_TYPING = 2;
    //public static final int TYPE_CHAT_HEAD = 3;

    private String mRoomId;
    private long mDate;
    private String mOwnerId;
    private String mMessageId;
    private String mMessage;
    private int mType;
    private boolean mIsRead;


    /**
     *
     * @param type  - use this constructer for others
     *
     */
    public Chat(int type) {
        mRoomId = null;
        mDate = 0;
        mOwnerId = "";
        mMessageId = "";
        mMessage = "";
        mIsRead = false;
        mType = type;
    }


    /** Use this constructer if you want a message item
     *
     * @param roomId    -   id of room
     * @param date      -   date of message or other
     * @param messageId -   id of message
     * @param message   -   text of message
     * @param read      -   read
     * @param isOwner   -   are you the owner of the message
     */
    public Chat(String roomId,
                long date,
                String ownerId,
                String messageId,
                String message,
                boolean read,
                boolean isOwner)
    {
        mRoomId = roomId;
        mDate = date;
        mOwnerId = ownerId;
        mMessageId = messageId;
        mMessage = message;
        mIsRead = read;
        mType = isOwner ? TYPE_MESSAGE_ME : TYPE_MESSAGE_OTHER_PERSON;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public long getDate() {
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

    public void setIsRead(boolean isRead) {
        mIsRead = isRead;
    }
}
