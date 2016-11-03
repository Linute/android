package com.linute.linute.MainContent.DiscoverFragment;

import java.util.HashMap;

/**
 * Created by QiFeng on 11/3/16.
 */

public class PollsSingleton {
    private static PollsSingleton mPollsSingleton;
    private HashMap<String, Poll> mPollHashMap;


    public static void init(){
        if (mPollsSingleton == null)
            mPollsSingleton = new PollsSingleton();
    }

    public static PollsSingleton getInstance(){
        if (mPollsSingleton == null)
            init();

        return mPollsSingleton;
    }

    private PollsSingleton(){
        mPollHashMap = new HashMap<>();
    }


    public boolean contains(Poll p){
        return mPollHashMap.containsKey(p.getId());
    }

    public Poll updateOrAddPoll(Poll p){
        if (contains(p)) {
            Poll p1 = mPollHashMap.get(p.getId());
            p1.update(p);
            return p1;
        }else {
            mPollHashMap.put(p.getId(), p);
            return p;
        }
    }
}
