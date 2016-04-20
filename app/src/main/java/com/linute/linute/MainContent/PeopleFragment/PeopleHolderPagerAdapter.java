package com.linute.linute.MainContent.PeopleFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by QiFeng on 1/27/16.
 */
public class PeopleHolderPagerAdapter extends FragmentPagerAdapter {

    private PeopleFragment[] mPeopleFragments;

    public PeopleHolderPagerAdapter(FragmentManager fm, PeopleFragment[] fragments) {
        super(fm);
        mPeopleFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mPeopleFragments[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Nearby";
            case 1:
                return "Top Players";
            default:
                return super.getPageTitle(position);
        }
    }
}
