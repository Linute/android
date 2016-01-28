package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;

/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends UpdatableFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;
    private boolean mInitiallyPresentedFragmentWasCampus = true; //first fragment presented by viewpager was campus fragment

    private FragmentHolderPagerAdapter mFragmentHolderPagerAdapter;

    public DiscoverHolderFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentHolderPagerAdapter = new FragmentHolderPagerAdapter(getChildFragmentManager());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.discover_sliding_tabs);

        mViewPager = (ViewPager) rootView.findViewById(R.id.discover_hostViewPager);
        mViewPager.setAdapter(mFragmentHolderPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected: " + position);
                //we will only load the other fragment if it is needed
                //ex. we start on the campus tab. we won't load the friends tab until we swipe left
                loadFragmentAtPositionIfNeeded(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("campusNeedsUpdate", mCampusFeedNeedsUpdating);
        outState.putBoolean("friendsNeedUpdate", mFriendsFeedNeedsUpdating);
        outState.putInt("viewPagerIndex", mViewPager.getCurrentItem());

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null){
            int index = savedInstanceState.getInt("viewPagerIndex");
            mInitiallyPresentedFragmentWasCampus = index == 0;
            mCampusFeedNeedsUpdating = savedInstanceState.getBoolean("campusNeedsUpdate");
            mFriendsFeedNeedsUpdating = savedInstanceState.getBoolean("friendsNeedUpdate");
            mViewPager.setCurrentItem(index);
        }
    }

    private boolean mCampusFeedNeedsUpdating = true;
    private boolean mFriendsFeedNeedsUpdating = true;

    @Override
    public void setFragmentNeedUpdating(boolean needsUpdating) {
        mCampusFeedNeedsUpdating = needsUpdating;
        mFriendsFeedNeedsUpdating = needsUpdating;
    }

    public boolean getCampusFeedNeedsUpdating() {
        return mCampusFeedNeedsUpdating;
    }

    public boolean getFriendsFeedNeedsUpdating() {
        return mFriendsFeedNeedsUpdating;
    }

    public void setFriendsFeedNeedsUpdating(boolean needsUpdating) {
        mFriendsFeedNeedsUpdating = needsUpdating;
    }

    public void setCampusFeedNeedsUpdating(boolean needsUpdating) {
        mCampusFeedNeedsUpdating = needsUpdating;
    }

    public boolean getInitiallyPresentedFragmentWasCampus(){
        return mInitiallyPresentedFragmentWasCampus;
    }

    //checks the fragment at a position in the viewpager and checks if it needs to be updated
    //if it needs to be updated, update it
    private void loadFragmentAtPositionIfNeeded(int position){
        DiscoverFragment fragment = (DiscoverFragment) mFragmentHolderPagerAdapter.instantiateItem(mViewPager, position);
        //only load when fragment comes into view
        if (fragment != null) {
            if (position == 0 ? mCampusFeedNeedsUpdating : mFriendsFeedNeedsUpdating) {
                fragment.getFeed(0);
                if (position == 0) mCampusFeedNeedsUpdating = false;
                else mFriendsFeedNeedsUpdating = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            mainActivity.setTitle("FEED");
            mainActivity.lowerAppBarElevation(); //app bars elevation must be 0 or there will be a shadow on top of the tabs
            mainActivity.showFAB(true); //show the floating button
            mainActivity.resetToolbar();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.raiseAppBarLayoutElevation();
            mainActivity.showFAB(false);
        }
    }
}
