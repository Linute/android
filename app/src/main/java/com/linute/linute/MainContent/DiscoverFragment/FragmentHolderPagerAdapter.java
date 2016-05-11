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

    public DiscoverFragment[] mDiscoverFragments;


    public FragmentHolderPagerAdapter(FragmentManager fm, DiscoverFragment[] fragments) {
        super(fm);
        mDiscoverFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mDiscoverFragments[position];
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
                return "Hot";
            default:
                return super.getPageTitle(position);
        }
    }
}
