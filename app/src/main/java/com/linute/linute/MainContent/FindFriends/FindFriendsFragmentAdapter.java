package com.linute.linute.MainContent.FindFriends;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by QiFeng on 4/6/16.
 */
public class FindFriendsFragmentAdapter extends FragmentPagerAdapter {

    private FindFriendsFragment[] mFindFriendsFragments;

    public FindFriendsFragmentAdapter(FragmentManager fm, FindFriendsFragment[] array) {
        super(fm);
        mFindFriendsFragments = array;
    }

    @Override
    public Fragment getItem(int position) {
        return mFindFriendsFragments[position];
    }

    @Override
    public int getCount() {
        return 3;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Name";
            case 1:
                return "Facebook";
            case 2:
                return "Contacts";
        }

        return super.getPageTitle(position);
    }
}
