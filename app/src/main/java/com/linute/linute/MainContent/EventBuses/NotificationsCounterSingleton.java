package com.linute.linute.MainContent.EventBuses;

/**
 * Created by QiFeng on 5/14/16.
 */
public class NotificationsCounterSingleton {

    private int mNumOfNewPosts;
    private int mNumOfNewActivities;
    private int mMessageCount;

    private boolean mDiscoverNeedsRefreshing;
    private boolean mUpdatesNeedsRefreshing;

    private static NotificationsCounterSingleton mNotificationsCounterSingleton;


    private NotificationsCounterSingleton(){
        mNumOfNewActivities = 0;
        mNumOfNewPosts = 0;
        mMessageCount = 0;
    }

    public static NotificationsCounterSingleton getInstance(){
        if (mNotificationsCounterSingleton == null){
            mNotificationsCounterSingleton = new NotificationsCounterSingleton();
        }
        return mNotificationsCounterSingleton;
    }

    public int getNumOfNewActivities() {
        return mNumOfNewActivities;
    }

    public void setNumOfNewActivities(int numOfNewActivities) {
        mNumOfNewActivities = numOfNewActivities;
    }

    public void setNumOfNewPosts(int i){
        mNumOfNewPosts = i;
    }

    public boolean hasMessage() {
        return mMessageCount > 0;
    }

    public int getNumMessages() { return mMessageCount;}

    public void setNumMessages(int count) {
        mMessageCount = count;
    }

    public int incrementActivities(){
        return ++mNumOfNewActivities;
    }

    public int incrementPosts(){
        return ++mNumOfNewPosts;
    }

    public int incrementPosts(int i){
        return (mNumOfNewPosts+=i);
    }

    public boolean updatesNeedsRefreshing() {
        return mUpdatesNeedsRefreshing;
    }

    public void setUpdatesNeedsRefreshing(boolean updatesNeedsRefreshing) {
        mUpdatesNeedsRefreshing = updatesNeedsRefreshing;
    }

    public boolean discoverNeedsRefreshing() {
        return mDiscoverNeedsRefreshing;
    }

    public void setDiscoverNeedsRefreshing(boolean discoverNeedsRefreshing) {
        mDiscoverNeedsRefreshing = discoverNeedsRefreshing;
    }

    public boolean hasNewActivities() {
        return mNumOfNewActivities > 0;
    }
    public boolean hasNewPosts() {
        return mNumOfNewPosts > 0;
    }
}
