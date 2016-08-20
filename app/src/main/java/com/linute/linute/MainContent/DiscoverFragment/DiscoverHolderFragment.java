package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.PostStatus.CreateStatusActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.linute.linute.MainContent.MainActivity.PHOTO_STATUS_POSTED;


/**
 * Created by QiFeng on 1/20/16.
 */
public class DiscoverHolderFragment extends BaseFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private boolean mInitiallyPresentedFragmentWasCampus = true; //first fragment presented by viewpager was campus fragment

    private DiscoverFragment[] mDiscoverFragments;
    private AppBarLayout mAppBarLayout;

    private View mUpdateNotification;
    private TextView mUpdatesCounter;

    private boolean mHasMessage;

    private View mNotificationIndicator;

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

        mHasMessage = NotificationsCounterSingleton.getInstance().hasMessage();
        mToolbar.inflateMenu(R.menu.people_fragment_menu);

        mToolbar.setNavigationIcon(NotificationsCounterSingleton.getInstance().hasNewPosts() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

        View chat = mToolbar.getMenu().findItem(R.id.menu_chat).getActionView();
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addFragmentToContainer(new RoomsActivityFragment(), RoomsActivityFragment.TAG);
            }
        });
        mNotificationIndicator = chat.findViewById(R.id.notification);
        mNotificationIndicator.setVisibility(mHasMessage ? View.VISIBLE : View.GONE);

        View update = mToolbar.getMenu().findItem(R.id.menu_updates).getActionView();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addActivityFragment();
            }
        });
        mUpdateNotification = update.findViewById(R.id.notification);

        mUpdatesCounter = (TextView) mUpdateNotification.findViewById(R.id.notification_count);
        int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
        mUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        mUpdatesCounter.setText(count < 100 ? count + "" : "+");

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
                VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_fire_on);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        mDiscoverFragments[mViewPager.getCurrentItem()].scrollUp();
                    }
                }
        );


        rootView.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;
//                Intent i = new Intent(getActivity(), CameraActivity.class);
//                i.putExtra(CameraActivity.CAMERA_TYPE, new CameraType(CameraType.CAMERA_EVERYTHING));
//                i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.SEND_POST);
//                getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                Intent i = new Intent(getActivity(), CreateStatusActivity.class);
                getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
            }
        });

        return rootView;
    }


    private boolean mCampusFeedNeedsUpdating = true;
    private boolean mFriendsFeedNeedsUpdating = true;

    @Override
    public void setFragmentState(FragmentState state) {
        super.setFragmentState(state);
        if (state == FragmentState.NEEDS_UPDATING) {
            mCampusFeedNeedsUpdating = true;
            mFriendsFeedNeedsUpdating = true;
        } else {
            mCampusFeedNeedsUpdating = false;
            mFriendsFeedNeedsUpdating = false;
        }
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

        mNotificationSubscription = NotificationEventBus.getInstance().getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNotificationEventAction1);

    }


    @Override
    public void onPause() {
        super.onPause();

        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();

        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }

        mAppBarLayout.setExpanded(true, false);
    }


    //returns if success
    public boolean addPostToFeed(Post post) {
        return getActivity() != null &&
                mDiscoverFragments[0] != null &&
                mDiscoverFragments[0].addPostToTop(post);
    }

    @Override
    public void resetFragment() {
        mAppBarLayout.setExpanded(true, false);
        mViewPager.setCurrentItem(0, true);
        mDiscoverFragments[0].scrollUp();
    }


    /*public SingleVideoPlaybackManager getSinglePlaybackManager() {
        return mSingleVideoPlaybackManager;
    }
*/
    private Subscription mChatSubscription;

    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            if (event.hasNewMessage() != mHasMessage) {
                mNotificationIndicator.setVisibility(event.hasNewMessage() ? View.VISIBLE : View.GONE);
                mHasMessage = event.hasNewMessage();
            }
        }
    };

    private Subscription mNotificationSubscription;

    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.getType() == NotificationEvent.ACTIVITY) {
                int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
                mUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE );
                mUpdatesCounter.setText(count < 100 ? count + "" : "+");
            } else if (notificationEvent.getType() == NotificationEvent.DISCOVER) {
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
            }
        }
    };
}
