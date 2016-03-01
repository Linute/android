package com.linute.linute.MainContent.PeopleFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by QiFeng on 1/27/16.
 */
public class PeopleHolderPagerAdapter extends FragmentPagerAdapter {

    public PeopleHolderPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0){
            return PeopleFragment.newInstance(true);
        }else
            return PeopleFragment.newInstance(false);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Nearby";
            case 1:
                return "Top Players";
            default:
                return super.getPageTitle(position);
        }
    }
}
