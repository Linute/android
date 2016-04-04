package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

import org.json.JSONException;
import org.json.JSONObject;


import io.socket.emitter.Emitter;

/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends UpdatableFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;
    private boolean mInitiallyPresentedFragmentWasCampus = true; //first fragment presented by viewpager was campus fragment

    private DiscoverFragment[] mDiscoverFragments;

    //makes sure only one video is playing at a time
    private SingleVideoPlaybackManager mSingleVideoPlaybackManager = new SingleVideoPlaybackManager();

    public DiscoverHolderFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.discover_sliding_tabs);

        if (mDiscoverFragments == null || mDiscoverFragments.length != 2) {
            mDiscoverFragments = new DiscoverFragment[]{DiscoverFragment.newInstance(false), DiscoverFragment.newInstance(true)};
        }

        FragmentHolderPagerAdapter fragmentHolderPagerAdapter = new FragmentHolderPagerAdapter(getChildFragmentManager(), mDiscoverFragments);
        mViewPager = (ViewPager) rootView.findViewById(R.id.discover_hostViewPager);
        mViewPager.setAdapter(fragmentHolderPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //we will only load the other fragment if it is needed
                //ex. we start on the campus tab. we won't load the friends tab until we swipe left
                loadFragmentAtPositionIfNeeded(position);
                mInitiallyPresentedFragmentWasCampus = position == 0;
            }

            @Override
            public void onPageSelected(int position) {
                mSingleVideoPlaybackManager.stopPlayback();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean("campusNeedsUpdate", mCampusFeedNeedsUpdating);
//        outState.putBoolean("friendsNeedsUpdate", mFriendsFeedNeedsUpdating);
//        if (mViewPager != null) outState.putInt("viewPagerIndex", mViewPager.getCurrentItem());
//    }
//
//    @Override
//    public void onViewStateRestored(Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        if (savedInstanceState != null) {
//            int index = savedInstanceState.getInt("viewPagerIndex",0);
//            mInitiallyPresentedFragmentWasCampus = index == 0;
//            mCampusFeedNeedsUpdating = savedInstanceState.getBoolean("campusNeedsUpdate", true);
//            mFriendsFeedNeedsUpdating = savedInstanceState.getBoolean("friendsNeedsUpdate", true);
//            if (mViewPager != null) mViewPager.setCurrentItem(index);
//        }
//    }

    private boolean mCampusFeedNeedsUpdating = true;
    private boolean mFriendsFeedNeedsUpdating = true;

    @Override
    public void setFragmentNeedUpdating(boolean needsUpdating) {
        mCampusFeedNeedsUpdating = needsUpdating;
        mFriendsFeedNeedsUpdating = needsUpdating;
    }

    @Override
    public boolean fragmentNeedsUpdating() {
        return mCampusFeedNeedsUpdating && mFriendsFeedNeedsUpdating;
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

    public boolean getInitiallyPresentedFragmentWasCampus() {
        return mInitiallyPresentedFragmentWasCampus;
    }

    //checks the fragment at a position in the viewpager and checks if it needs to be updated
    //if it needs to be updated, update it
    private void loadFragmentAtPositionIfNeeded(int position) {
        //only load when fragment comes into view
        if (position == 0 ? mCampusFeedNeedsUpdating : mFriendsFeedNeedsUpdating) {
            mDiscoverFragments[position].refreshFeed();
            if (position == 0) mCampusFeedNeedsUpdating = false;
            else mFriendsFeedNeedsUpdating = false;
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setTitle("Feed");
            mainActivity.lowerAppBarElevation(); //app bars elevation must be 0 or there will be a shadow on top of the tabs
            mainActivity.showFAB(true); //show the floating button
            mainActivity.resetToolbar();

            mainActivity.setToolbarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDiscoverFragments[mViewPager.getCurrentItem()].scrollUp();
                }
            });

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mainActivity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID",""));
                obj.put("action", "active");
                obj.put("screen", "Discover");
                mainActivity.emitSocket(API_Methods.VERSION+":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mSingleVideoPlaybackManager.stopPlayback();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mainActivity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID",""));
                obj.put("action", "inactive");
                obj.put("screen", "Discover");
                mainActivity.emitSocket(API_Methods.VERSION+":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.raiseAppBarLayoutElevation();
            mainActivity.showFAB(false);
            mainActivity.setToolbarOnClickListener(null);
        }
    }


    //returns if success
    public boolean addPostToFeed(Object post){

        JSONObject obj = (JSONObject) post;

        if (obj != null && getActivity() != null &&  mDiscoverFragments[0] != null){
            try {
                Post post1 = new Post(obj);

                return mDiscoverFragments[0].addPostToTop(post1);

            }catch (JSONException e){
                e.printStackTrace();
                return false;
            }
        }else {
            Log.i(TAG, "addPostToFeed: obj was null");
            return false;
        }
    }


    public SingleVideoPlaybackManager getSinglePlaybackManager(){
        return mSingleVideoPlaybackManager;
    }
}
