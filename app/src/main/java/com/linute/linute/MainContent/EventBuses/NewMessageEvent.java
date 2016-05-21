package com.linute.linute.MainContent.EventBuses;

/**
 * Created by QiFeng on 4/16/16.
 */
public class NewMessageEvent {
    private boolean mHasNewMessage;

    private String mRoomId;
    private String mMessage;
    private String mOtherUserId;
    private String mOtherUserName;

    public NewMessageEvent(boolean hasNewMessageMessage){
        mHasNewMessage = hasNewMessageMessage;
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
        return mOtherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        mOtherUserName = otherUserName;
    }

    public boolean hasNewMessage(){
        return mHasNewMessage;
    }
}
