package com.linute.linute.MainContent.Chat;

/**
 * Created by QiFeng on 4/16/16.
 */
public class NewChatEvent {
    private boolean mHasNewMessage;


    public NewChatEvent(boolean hasNewMessageMessage){
        mHasNewMessage = hasNewMessageMessage;
    }

    public boolean hasNewMessage(){
        return mHasNewMessage;
    }
}
