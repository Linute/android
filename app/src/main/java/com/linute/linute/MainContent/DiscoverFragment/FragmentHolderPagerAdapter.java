package com.linute.linute.MainContent.DiscoverFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.linute.linute.MainContent.DiscoverFragment.DiscoverFragment;

/**
 * Created by QiFeng on 1/20/16.
 */
public class FragmentHolderPagerAdapter extends FragmentPagerAdapter {

    public DiscoverFragment mCampusFeed;
    public DiscoverFragment mFriendsFeed;


    public FragmentHolderPagerAdapter(FragmentManager fm) {
        super(fm);
        mCampusFeed = DiscoverFragment.newInstance(false);
        mFriendsFeed = DiscoverFragment.newInstance(true);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return mCampusFeed;
            case 1:
                return mFriendsFeed;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Campus";
            case 1:
                return "Friends";
            default:
                return super.getPageTitle(position);
        }
    }
}
