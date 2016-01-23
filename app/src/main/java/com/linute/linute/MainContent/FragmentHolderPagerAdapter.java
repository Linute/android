package com.linute.linute.MainContent;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.linute.linute.MainContent.DiscoverFragment.DiscoverFragment;

/**
 * Created by QiFeng on 1/20/16.
 */
public class FragmentHolderPagerAdapter extends FragmentPagerAdapter {
    public FragmentHolderPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return DiscoverFragment.newInstance(false);
            case 1:
                return DiscoverFragment.newInstance(true);
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
