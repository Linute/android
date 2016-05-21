package com.linute.linute.MainContent.EventBuses;

/**
 * Created by QiFeng on 5/13/16.
 */
public class NotificationEvent {

    private boolean mHasNotification;

    public NotificationEvent (boolean hasNotification){
        mHasNotification = hasNotification;
    }

    public boolean hasNotification(){
        return mHasNotification;
    }
}
