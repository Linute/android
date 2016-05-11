package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.Chat.NewChatEvent;
import com.linute.linute.MainContent.Chat.NewMessageBus;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.PostCreatePage;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.linute.linute.MainContent.MainActivity.PHOTO_STATUS_POSTED;


/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends UpdatableFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;
    private boolean mInitiallyPresentedFragmentWasCampus = true; //first fragment presented by viewpager was campus fragment

    private DiscoverFragment[] mDiscoverFragments;

    private FloatingActionsMenu mFloatingActionsMenu;
    private AppBarLayout mAppBarLayout;

    private Toolbar mToolbar;
    private boolean mHasMessage;

    //makes sure only one video is playing at a time
    private SingleVideoPlaybackManager mSingleVideoPlaybackManager = new SingleVideoPlaybackManager();

    public DiscoverHolderFragment() {

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.openDrawer();
            }
        });
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDiscoverFragments[mViewPager.getCurrentItem()].scrollUp();
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        mHasMessage = activity.hasMessage();
        mToolbar.inflateMenu(activity.hasMessage() ? R.menu.people_fragment_menu_noti : R.menu.people_fragment_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MainActivity activity = (MainActivity) getActivity();
                switch (item.getItemId()) {
                    case R.id.menu_find_friends:
                        if (activity != null){
                            activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                        }
                        return true;
                    case R.id.people_fragment_menu_chat:
                        if (activity != null){
                            activity.addFragmentToContainer(new RoomsActivityFragment());
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });

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


        mFloatingActionsMenu = (FloatingActionsMenu) rootView.findViewById(R.id.fabmenu);
        FloatingActionButton fabImage = (FloatingActionButton) rootView.findViewById(R.id.fabImage);
        fabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;
                toggleFab();
                Intent i = new Intent(getActivity(), CameraActivity.class);
                i.putExtra(CameraActivity.CAMERA_TYPE, CameraActivity.CAMERA_AND_VIDEO_AND_GALLERY);
                i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.SEND_POST);
                getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
            }
        });
        FloatingActionButton fabText = (FloatingActionButton) rootView.findViewById(R.id.fabText);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;
                toggleFab();
                Intent i = new Intent(getActivity(), PostCreatePage.class);
                getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
            }
        });

        return rootView;
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
        mChatSubscription = NewMessageBus.getInstance().getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNewMessageSubscriber);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mChatSubscription != null && !mChatSubscription.isUnsubscribed()){
            mChatSubscription.unsubscribe();
        }

        toggleFab();
        mAppBarLayout.setExpanded(true, false);
        mSingleVideoPlaybackManager.stopPlayback();
    }


    //returns if success
    public boolean addPostToFeed(Object post) {

        JSONObject obj = (JSONObject) post;

        if (obj != null && getActivity() != null && mDiscoverFragments[0] != null) {
            try {
                Post post1 = new Post(obj);

                return mDiscoverFragments[0].addPostToTop(post1);

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.i(TAG, "addPostToFeed: obj was null");
            return false;
        }
    }

    public void toggleFab() {
        if (mFloatingActionsMenu.isExpanded()) {
            mFloatingActionsMenu.collapse();
        }
    }


    public SingleVideoPlaybackManager getSinglePlaybackManager() {
        return mSingleVideoPlaybackManager;
    }



    private Subscription mChatSubscription;

    private Action1<NewChatEvent> mNewMessageSubscriber = new Action1<NewChatEvent>() {
        @Override
        public void call(NewChatEvent event) {
            if (event.hasNewMessage() != mHasMessage){
                mToolbar.getMenu().findItem(R.id.people_fragment_menu_chat).setIcon(event.hasNewMessage() ?
                        R.drawable.notify_mess_icon :
                        R.drawable.ic_chat81
                );
                mHasMessage = event.hasNewMessage();
            }
        }
    };
}
