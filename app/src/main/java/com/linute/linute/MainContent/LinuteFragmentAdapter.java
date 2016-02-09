package com.linute.linute.MainContent;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.linute.linute.R;

/**
 * Created by QiFeng on 11/17/15.
 */


public class LinuteFragmentAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private Fragment[] mFragments;

    //icons we will use
    //these are currently 30 pxl
    private int[] TAB_ICON_IDS = {
//            R.drawable.ic_time_line,
//            R.drawable.ic_friends,
//            R.drawable.ic_notification,
//            R.drawable.ic_profile
    };


    public LinuteFragmentAdapter(FragmentManager fm, Context context, Fragment[] pageFragments) {
        super(fm);
        this.mContext = context;

        mFragments = pageFragments;
    }

    @Override
    public int getCount() {
        return TAB_ICON_IDS.length;
    }

    //returns correct fragment
    @Override
    public Fragment getItem(int position) {
        //add others later
        //TODO: ADD OTHER FRAGMENTS
        switch (position) {
            case 0:
                return mFragments[0];
            case 1:
                return mFragments[1];
            case 2:
                return mFragments[2];
            case 3:
                return mFragments[3];
            default:
                return mFragments[0];
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public int getDrawableIconId(int position) {
        return TAB_ICON_IDS[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }
}
