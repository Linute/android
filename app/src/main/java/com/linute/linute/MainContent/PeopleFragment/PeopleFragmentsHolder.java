package com.linute.linute.MainContent.PeopleFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.List;

/**
 * Created by QiFeng on 1/27/16.
 */
public class PeopleFragmentsHolder extends UpdatableFragment {


    public static final String TAG = PeopleFragmentsHolder.class.getSimpleName();
    private ViewPager mViewPager;
    private boolean mInitiallyPopularPeople = true; //first fragment presented by viewpager was popular users
    private PeopleHolderPagerAdapter mPeopleHolderPagerAdapter;


    public PeopleFragmentsHolder() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPeopleHolderPagerAdapter = new PeopleHolderPagerAdapter(getChildFragmentManager());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people_holder, container, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.people_sliding_tabs);

        mViewPager = (ViewPager) rootView.findViewById(R.id.people_hostViewPager);

        mViewPager.setAdapter(mPeopleHolderPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //we will only load the other fragment if it is needed
                //ex. we start on the campus tab. we won't load the near me tab until we swipe left
                loadFragmentAtPositionIfNeeded(position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected: " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }


    private boolean mActiveNeedsUpdating = true;
    private boolean mNearMeNeedsUpdating = true;
    private boolean mInitiallyPresentedFragmentWasActive = true;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("activeNeedsUpdate", mActiveNeedsUpdating);
        outState.putBoolean("nearMeNeedsUpdate", mNearMeNeedsUpdating);
        outState.putInt("viewPagerIndex", mViewPager.getCurrentItem());
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null){
            int index = savedInstanceState.getInt("viewPagerIndex");
            mInitiallyPresentedFragmentWasActive = index == 0;
            mActiveNeedsUpdating = savedInstanceState.getBoolean("activeNeedsUpdate");
            mNearMeNeedsUpdating = savedInstanceState.getBoolean("nearMeNeedsUpdate");
            mViewPager.setCurrentItem(index);
        }
    }

    @Override
    public void setFragmentNeedUpdating(boolean needsUpdating) {
        mActiveNeedsUpdating = needsUpdating;
        mNearMeNeedsUpdating = needsUpdating;
    }

    @Override
    public boolean fragmentNeedsUpdating(){
        return mActiveNeedsUpdating && mNearMeNeedsUpdating;
    }

    public boolean nearMeFragmentNeedsUpdating(){
        return mNearMeNeedsUpdating;
    }

    public boolean activeNeedsUpdating(){
        return mActiveNeedsUpdating;
    }

    public void setNearMeNeedsUpdating(boolean needsUpdating){
        mNearMeNeedsUpdating = needsUpdating;
    }

    public void setActiveNeedsUpdating(boolean needsUpdating){
        mActiveNeedsUpdating = needsUpdating;
    }

    public boolean getInitiallyPresentedFragmentWasActive(){
        return mInitiallyPresentedFragmentWasActive;
    }

    //checks the fragment at a position in the viewpager and checks if it needs to be updated
    //if it needs to be updated, update it
    private void loadFragmentAtPositionIfNeeded(int position){
        PeopleFragment fragment = (PeopleFragment) mPeopleHolderPagerAdapter.instantiateItem(mViewPager, position);
        //only load when fragment comes into view
        if (fragment != null) {
            if (position == 0 ? mActiveNeedsUpdating : mNearMeNeedsUpdating) {
                if (position == 0) {
                    mActiveNeedsUpdating = false;
                    fragment.getPeople();
                }
                else{
                    mNearMeNeedsUpdating = false;
                    fragment.getPeopleNearMe();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            mainActivity.setTitle("People");
            mainActivity.lowerAppBarElevation(); //app bars elevation must be 0 or there will be a shadow on top of the tabs
            mainActivity.resetToolbar();
        }
    }



    public void onStop() {
        super.onStop();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.raiseAppBarLayoutElevation();
        }
        setFragmentNeedUpdating(true);
    }

    //there's problems with nested fragments
    public boolean hasLocationPermissions(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PeopleFragment.LOCATION_REQUEST);
            return false;
        }else {
           return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PeopleFragment fragment = (PeopleFragment) mPeopleHolderPagerAdapter.instantiateItem(mViewPager, 1);
        fragment.gotPermissionResults();
    }
}
