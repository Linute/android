package com.linute.linute.UtilsAndHelpers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;


/**
 * Created by QiFeng on 1/22/16.
 */

/**
 * Base fragment that all our fragments will be based off of
 */
public abstract class BaseFragment extends Fragment {

    /*
     * Our fragment has states to help us manage when we need to query server for info
     */
    public enum FragmentState {
        NEEDS_UPDATING,   // needs to query for information ; when fragment first created
        LOADING_DATA,     // currently querying for data ; that's so we don't query twice
        FINISHED_UPDATING // we got a response. There could have been error so we will have to check that ourselves
    }

    private FragmentState mFragmentState = FragmentState.NEEDS_UPDATING;


    public BaseFragment() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("NEEDS_UPDATING", mFragmentState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            mFragmentState = (FragmentState) savedInstanceState.getSerializable("NEEDS_UPDATING");
    }

    public void setFragmentState(FragmentState state){
        mFragmentState = state;
    }

    public FragmentState getFragmentState(){
        return mFragmentState;
    }

    public void resetFragment(){}

    boolean isCacheLoaded = false;

    public void loadDataFromCache(){}
    public void loadDataFromNetwork(){}
    public void loadMoreDataFromNetwork(){};
    public void displayDataFromCache(){}
    public void displayDataFromNetwork(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadDataFromCache();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadDataFromNetwork();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
