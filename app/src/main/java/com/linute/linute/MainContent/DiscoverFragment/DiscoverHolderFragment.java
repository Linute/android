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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.socket.emitter.Emitter;

/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends UpdatableFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;
    private boolean mInitiallyPresentedFragmentWasCampus = true; //first fragment presented by viewpager was campus fragment

    private FragmentHolderPagerAdapter mFragmentHolderPagerAdapter;

    private DiscoverFragment[] mDiscoverFragments;

    public DiscoverHolderFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.discover_sliding_tabs);

        setHasOptionsMenu(true);

        if (mDiscoverFragments == null || mDiscoverFragments.length != 2) {
            mDiscoverFragments = new DiscoverFragment[]{DiscoverFragment.newInstance(false), DiscoverFragment.newInstance(true)};
        }

        mFragmentHolderPagerAdapter = new FragmentHolderPagerAdapter(getChildFragmentManager(), mDiscoverFragments);
        mViewPager = (ViewPager) rootView.findViewById(R.id.discover_hostViewPager);
        mViewPager.setAdapter(mFragmentHolderPagerAdapter);
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
                if (mDiscoverFragments[0] != null)
                    mDiscoverFragments[0].stopVideos();


                if (mDiscoverFragments[1] != null)
                    mDiscoverFragments[1].stopVideos();
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
        outState.putBoolean("friendsNeedsUpdate", mFriendsFeedNeedsUpdating);
        outState.putInt("viewPagerIndex", mViewPager.getCurrentItem());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            int index = savedInstanceState.getInt("viewPagerIndex");
            mInitiallyPresentedFragmentWasCampus = index == 0;
            mCampusFeedNeedsUpdating = savedInstanceState.getBoolean("campusNeedsUpdate");
            mFriendsFeedNeedsUpdating = savedInstanceState.getBoolean("friendsNeedsUpdate");
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

            mainActivity.connectSocket("unread", haveUnread);
            JSONObject object = new JSONObject();
            mainActivity.emitSocket(API_Methods.VERSION+":messages:unread", object);

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

        if (mDiscoverFragments[0] != null)
            mDiscoverFragments[0].stopVideos();


        if (mDiscoverFragments[1] != null)
            mDiscoverFragments[1].stopVideos();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {

            mainActivity.disconnectSocket("unread", haveUnread);

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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(mHasMessages? R.menu.people_fragment_menu_noti : R.menu.people_fragment_menu, menu);
        mCreateActionMenu = true;
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (getActivity() != null) {
            switch (item.getItemId()) {
                case R.id.people_fragment_menu_chat:
                    Intent enterRooms = new Intent(getActivity(), RoomsActivity.class);
                    enterRooms.putExtra("CHATICON", true);
                    startActivity(enterRooms);
                    return true;
                case R.id.menu_find_friends:
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity != null){
                        activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                    }
                    return true;
            }
        }


        return super.onOptionsItemSelected(item);
    }


    //returns if success
    public boolean addPostToFeed(Object post){

        JSONObject obj = (JSONObject) post;

        if (obj != null && mDiscoverFragments[0] != null){
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


    private boolean mCreateActionMenu = false;
    private boolean mHasMessages = false;

    private Emitter.Listener haveUnread = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mHasMessages = (boolean) args[0];
            //Log.i(TAG, "call: "+mHasMessages);
            if (mCreateActionMenu){
                final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                if (act != null) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            act.invalidateOptionsMenu();
                        }
                    });
                }
            }
        }
    };
}
