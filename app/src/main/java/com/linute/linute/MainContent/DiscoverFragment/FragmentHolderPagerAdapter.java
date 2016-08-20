package com.linute.linute.MainContent.DiscoverFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by QiFeng on 1/20/16.
 */
public class FragmentHolderPagerAdapter extends FragmentPagerAdapter {

    public FeedFragment[] mFeedFragments;


    public FragmentHolderPagerAdapter(FragmentManager fm, FeedFragment[] fragments) {
        super(fm);
        mFeedFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFeedFragments[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "New";
            case 1:
                return null;
            default:
                return super.getPageTitle(position);
        }
    }
}
