package com.linute.linute.MainContent.EventBuses;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by QiFeng on 5/13/16.
 */
public class NotificationEventBus {

    private PublishSubject<NotificationEvent> mNotificationEventPublishSubject;

    private static NotificationEventBus mNotificationEventBus;

    public static NotificationEventBus getInstance(){
        if (mNotificationEventBus == null){
            mNotificationEventBus = new NotificationEventBus();
        }
        return mNotificationEventBus;
    }

    private NotificationEventBus(){
        mNotificationEventPublishSubject = PublishSubject.create();
    }

    public void setNotification(NotificationEvent event){
        mNotificationEventPublishSubject.onNext(event);
    }

    public Observable<NotificationEvent> getObservable(){
        return mNotificationEventPublishSubject;
    }
}



