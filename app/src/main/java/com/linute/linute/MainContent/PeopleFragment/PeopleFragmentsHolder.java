package com.linute.linute.MainContent.PeopleFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.MainContent.Chat.NewChatEvent;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomViewPager;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * Created by QiFeng on 1/27/16.
 */
public class PeopleFragmentsHolder extends UpdatableFragment {

    public static final String TAG = PeopleFragmentsHolder.class.getSimpleName();
    private CustomViewPager mViewPager;
    private PeopleHolderPagerAdapter mPeopleHolderPagerAdapter;

    private AppBarLayout mAppBarLayout;

    private Toolbar mToolbar;

    private boolean mHasMessage;

    private PeopleFragment[] mPeopleFragments;

    public PeopleFragmentsHolder() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people_holder, container, false);

        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_layout);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.people_sliding_tabs);


        if (mPeopleFragments == null || mPeopleFragments.length != 2) {
            mPeopleFragments = new PeopleFragment[]{PeopleFragment.newInstance(true), PeopleFragment.newInstance(false)};
        }

        mPeopleHolderPagerAdapter = new PeopleHolderPagerAdapter(getChildFragmentManager(), mPeopleFragments);

        mViewPager = (CustomViewPager) rootView.findViewById(R.id.people_hostViewPager);
        mViewPager.setPagingEnabled(false);
        mViewPager.setAdapter(mPeopleHolderPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //we will only load the other fragment if it is needed
                //ex. we start on the campus tab. we won't load the near me tab until we swipe left
                loadFragmentAtPositionIfNeeded(position);
                mInitiallyPresentedFragmentWasNearby = position == 0;
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(mViewPager);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null){
                    activity.openDrawer();
                }
            }
        });
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollViewRecListUp(mViewPager.getCurrentItem());
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        mHasMessage = activity.hasMessage();
        mToolbar.inflateMenu(activity.hasMessage() ? R.menu.people_fragment_menu_noti : R.menu.people_fragment_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_find_friends:
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null){
                            activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                        }
                        return true;
                    case R.id.people_fragment_menu_chat:
                        Intent enterRooms = new Intent(getActivity(), RoomsActivity.class);
                        startActivity(enterRooms);
                        return true;
                    default:
                        return false;
                }
            }
        });

        return rootView;
    }

    private boolean mActiveNeedsUpdating = true;
    private boolean mNearMeNeedsUpdating = true;
    private boolean mInitiallyPresentedFragmentWasNearby = true;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("activeNeedsUpdate", mActiveNeedsUpdating);
        outState.putBoolean("nearMeNeedsUpdate", mNearMeNeedsUpdating);
        if (mViewPager != null) outState.putInt("viewPagerIndex", mViewPager.getCurrentItem());
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt("viewPagerIndex", 0);
            mInitiallyPresentedFragmentWasNearby = index == 0;
            mActiveNeedsUpdating = savedInstanceState.getBoolean("activeNeedsUpdate", true);
            mNearMeNeedsUpdating = savedInstanceState.getBoolean("nearMeNeedsUpdate", true);
            if (mViewPager != null) mViewPager.setCurrentItem(index);
        }
    }

    @Override
    public void setFragmentNeedUpdating(boolean needsUpdating) {
        mActiveNeedsUpdating = needsUpdating;
        mNearMeNeedsUpdating = needsUpdating;
    }

    @Override
    public boolean fragmentNeedsUpdating() {
        return mActiveNeedsUpdating && mNearMeNeedsUpdating;
    }

    public boolean nearMeFragmentNeedsUpdating() {
        return mNearMeNeedsUpdating;
    }

    public boolean activeNeedsUpdating() {
        return mActiveNeedsUpdating;
    }

    public void setNearMeNeedsUpdating(boolean needsUpdating) {
        mNearMeNeedsUpdating = needsUpdating;
    }

    public void setActiveNeedsUpdating(boolean needsUpdating) {
        mActiveNeedsUpdating = needsUpdating;
    }

    public boolean getInitiallyPresentedFragmentWasNearby() {
        return mInitiallyPresentedFragmentWasNearby;
    }

    //checks the fragment at a position in the viewpager and checks if it needs to be updated
    //if it needs to be updated, update it
    private void loadFragmentAtPositionIfNeeded(int position) {
        PeopleFragment fragment = (PeopleFragment) mPeopleHolderPagerAdapter.instantiateItem(mViewPager, position);

        if (fragment == null) return;
        if (position == 0 ? mNearMeNeedsUpdating : mActiveNeedsUpdating) {
            if (position == 0) {
                mNearMeNeedsUpdating = false;
                fragment.getPeopleNearMe();
            } else {
                mActiveNeedsUpdating = false;
                fragment.getPeople();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mAppBarLayout.setExpanded(true,false);
    }

    //there's problems with nested fragments
    public boolean hasLocationPermissions() {
        if (getActivity() == null) return false;
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PeopleFragment.LOCATION_REQUEST);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PeopleFragment.LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PeopleFragment fragment = (PeopleFragment) mPeopleHolderPagerAdapter.instantiateItem(mViewPager, 0);
                fragment.gotPermissionResults();
            }
        }
    }

    private void scrollViewRecListUp(int position) {
        PeopleFragment fragment = (PeopleFragment) mPeopleHolderPagerAdapter.instantiateItem(mViewPager, position);
        if (fragment != null) {
            fragment.scrollUp();
        }
    }

    @Subscribe
    public void onEvent(NewChatEvent event){
        // your implementation
        if (event.hasNewMessage() != mHasMessage){
            mToolbar.getMenu().findItem(R.id.people_fragment_menu_chat).setIcon(event.hasNewMessage() ?
                    R.drawable.notify_mess_icon :
                    R.drawable.ic_chat81
            );
            mHasMessage = event.hasNewMessage();
        }
    }
}
