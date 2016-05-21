package com.linute.linute.MainContent.EventBuses;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by QiFeng on 5/7/16.
 */
public class NewMessageBus {

    private PublishSubject<NewMessageEvent> mNewChatEventPublishSubject;

    private static NewMessageBus mNewMessageBus;

    public static NewMessageBus getInstance(){
        if (mNewMessageBus == null){
            mNewMessageBus = new NewMessageBus();
        }
        return mNewMessageBus;
    }

    private NewMessageBus(){
        mNewChatEventPublishSubject = PublishSubject.create();
    }

    public void setNewMessage(NewMessageEvent message){
        mNewChatEventPublishSubject.onNext(message);
    }

    public Observable<NewMessageEvent> getObservable(){
        return mNewChatEventPublishSubject;
    }

}
