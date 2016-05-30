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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.PostCreatePage;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
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
public class DiscoverHolderFragment extends BaseFragment {

    public static final String TAG = DiscoverHolderFragment.class.getSimpleName();

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private boolean mInitiallyPresentedFragmentWasCampus = true; //first fragment presented by viewpager was campus fragment

    private DiscoverFragment[] mDiscoverFragments;

    private FloatingActionsMenu mFloatingActionsMenu;
    private AppBarLayout mAppBarLayout;

    private View mBackgroundView;
    private boolean mHasMessage;
    private boolean mHasNotification;

    private View mNotificationIndicator;

    //makes sure only one video is playing at a time
    private SingleVideoPlaybackManager mSingleVideoPlaybackManager = new SingleVideoPlaybackManager();

    public DiscoverHolderFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover_holder, container, false);

        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_layout);

        mBackgroundView = rootView.findViewById(R.id.background);
        mBackgroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFab();
            }
        });

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

        mHasNotification = NotificationsCounterSingleton.getInstance().hasNotifications();
        mToolbar.setNavigationIcon(mHasNotification ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

        View chatActionView = mToolbar.getMenu().getItem(1).getActionView();

        mNotificationIndicator = chatActionView.findViewById(R.id.notification);
        mNotificationIndicator.setVisibility(mHasMessage ? View.VISIBLE : View.GONE);

        mToolbar.getMenu()
                .getItem(0)
                .getActionView()
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                        }
                    }
                });

        chatActionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            activity.addFragmentToContainer(new RoomsActivityFragment(), RoomsActivityFragment.TAG);
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
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_fire_on);

        mFloatingActionsMenu = (FloatingActionsMenu) rootView.findViewById(R.id.fabmenu);
        mFloatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                fadeInBackground(true);
            }

            @Override
            public void onMenuCollapsed() {
                fadeInBackground(false);
            }
        });

        rootView.findViewById(R.id.fabImage).setOnClickListener(new View.OnClickListener() {
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
        rootView.findViewById(R.id.fabText).setOnClickListener(new View.OnClickListener() {
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
    public void setFragmentState(FragmentState state) {
        super.setFragmentState(state);
        if (state == FragmentState.NEEDS_UPDATING) {
            mCampusFeedNeedsUpdating = true;
            mFriendsFeedNeedsUpdating = true;
        }else{
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

        mSingleVideoPlaybackManager.stopPlayback();

        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        if (mNotificationSubscription != null){
            mNotificationSubscription.unsubscribe();
        }

        toggleFab();
        mAppBarLayout.setExpanded(true, false);
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


    private void fadeInBackground(final boolean show) {
        mBackgroundView.clearAnimation();

        AlphaAnimation alphaAnimation = show ?
                new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(200);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBackgroundView.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mBackgroundView.startAnimation(alphaAnimation);
    }

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
            if (notificationEvent.hasNotification() != mHasNotification){
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
                mHasNotification = notificationEvent.hasNotification();
            }
        }
    };
}
