package com.linute.linute.MainContent.DiscoverFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;

/**
 * Created by QiFeng on 6/20/16.
 */
public class BlockedUsersSingleton {

    private static BlockedUsersSingleton mBlockedUsersSingleton;
    private HashSet<String> mBlockedList;


    private BlockedUsersSingleton(){}

    public static BlockedUsersSingleton getBlockedListSingletion(){
        if (mBlockedUsersSingleton == null){
            mBlockedUsersSingleton = new BlockedUsersSingleton();
        }
        return mBlockedUsersSingleton;
    }

    public void setBlockedList(JSONArray real, JSONArray anon) throws JSONException{
        mBlockedList = new HashSet<>();
        for (int i = 0; i < real.length() ; i++)
            mBlockedList.add(real.getString(i));
        for (int j = 0; j < anon.length(); j++){
            mBlockedList.add(anon.getString(j));
        }
    }

    public boolean contains(String id){
        return mBlockedList != null && mBlockedList.contains(id);
    }

    public void add(String id){
        if (mBlockedList != null)
            mBlockedList.add(id);
    }

    public void remove(String id){
        if (mBlockedList != null)
            mBlockedList.remove(id);
    }

    public HashSet<String> getBlockedList(){
        return mBlockedList;
    }
}
