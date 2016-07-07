package com.linute.linute.MainContent.EventBuses;

/**
 * Created by QiFeng on 5/13/16.
 */
public class NotificationEvent {
    public static final short ACTIVITY = 0;
    public static final short DISCOVER = 1;

    private short mType;
    private boolean mHasNotification;

    public NotificationEvent (short type, boolean hasNotification){
        mType = type;
        mHasNotification = hasNotification;
    }

    public short getType(){
        return mType;
    }

    public boolean hasNotification() {
        return mHasNotification;
    }
}
