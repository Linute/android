package com.linute.linute.MainContent.EventBuses;

/**
 * Created by QiFeng on 4/16/16.
 */
public class NewMessageEvent {
    private boolean mHasNewMessage;
    private int mNewMessageCount;


    private String mRoomId;
    private String mMessage;
    private String mOtherUserId;
    private String mOtherUserFirstName;
    private String mOtherUserLastName;

    public NewMessageEvent(boolean hasNewMessageMessage, int newMessageCount){
        mHasNewMessage = hasNewMessageMessage;
        mNewMessageCount = newMessageCount;
    }


    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getOtherUserId() {
        return mOtherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        mOtherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return mOtherUserFirstName+ " "+mOtherUserLastName;
    }

    public void setOtherUserFirstName(String otherUserName) {
        mOtherUserFirstName = otherUserName;
    }


    public void setOtherUserLastName(String otherUserName) {
        mOtherUserLastName = otherUserName;
    }

    public String getOtherUserFirstName() {
        return mOtherUserFirstName;
    }

    public String getOtherUserLastName() {
        return mOtherUserLastName;
    }

    public boolean hasNewMessage(){
        return mHasNewMessage;
    }
    public int getmNewMessageCount() {return mNewMessageCount;}
}
