package com.linute.linute.MainContent.FindFriends;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.linute.linute.MainContent.FindFriends.FindFriendsFragment.BaseFindFriendsFragment;

/**
 * Created by QiFeng on 4/6/16.
 */
public class FindFriendsFragmentAdapter extends FragmentPagerAdapter {

    private BaseFindFriendsFragment[] mFindFriendsFragments;

    public FindFriendsFragmentAdapter(FragmentManager fm, BaseFindFriendsFragment[] array) {
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
        switch (position) {
            case 0:
                return "Name";
            case 1:
                return "Campus";
            case 2:
                return "Facebook";
        }

        return super.getPageTitle(position);
    }
}
