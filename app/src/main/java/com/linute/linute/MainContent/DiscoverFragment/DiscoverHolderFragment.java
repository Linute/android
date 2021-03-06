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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.CreateContent.CreateStatusActivity;
import com.linute.linute.MainContent.CreateContent.Gallery.GalleryActivity;
import com.linute.linute.MainContent.EditScreen.PostOptions;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.ModesDisabled;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CameraType;
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

    private FeedFragment[] mFeedFragments;
    private AppBarLayout mAppBarLayout;

    private View mUpdateNotification;
    private TextView mUpdatesCounter;

    private View mNotificationIndicator;

    private FloatingActionsMenu mFloatingActionsMenu;
    private TextView mNotificationCount;

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
                mFeedFragments[mViewPager.getCurrentItem()].scrollUp();
            }
        });

        mToolbar.inflateMenu(R.menu.people_fragment_menu);

        mToolbar.setNavigationIcon(NotificationsCounterSingleton.getInstance().hasNewPosts() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

        View chat = mToolbar.getMenu().findItem(R.id.menu_chat).getActionView();
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addFragmentToContainer(RoomsActivityFragment.getInstance(), RoomsActivityFragment.TAG);
            }
        });

        mNotificationIndicator = chat.findViewById(R.id.notification);
        mNotificationCount = (TextView) chat.findViewById(R.id.notification_count);

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

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.discover_sliding_tabs);

        if (mFeedFragments == null || mFeedFragments.length != 2) {
            mFeedFragments = new FeedFragment[]{FeedFragment.newInstance(false), FeedFragment.newInstance(true)};
        }

        FragmentHolderPagerAdapter fragmentHolderPagerAdapter = new FragmentHolderPagerAdapter(getChildFragmentManager(), mFeedFragments);
        mViewPager = (ViewPager) rootView.findViewById(R.id.discover_hostViewPager);
        mViewPager.setAdapter(fragmentHolderPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //we will only load the other fragment if it is needed
                //ex. we start on the campus tab. we won't load the friends tab until we swipe left
                VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
                if (mFloatingActionsMenu.isExpanded()) mFloatingActionsMenu.collapse();
                loadFragmentAtPositionIfNeeded(position);
                mInitiallyPresentedFragmentWasCampus = position == 0;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_fire1);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mFeedFragments[mViewPager.getCurrentItem()].scrollUp();
            }
        });

        mFloatingActionsMenu = (FloatingActionsMenu) rootView.findViewById(R.id.create_menu);
        final View fabCloseOverlay = rootView.findViewById(R.id.fab_close_overlay);
        fabCloseOverlay.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (mFloatingActionsMenu.isExpanded())
                            mFloatingActionsMenu.collapse();
                        return false;
                    }
                });

        mFloatingActionsMenu.findViewById(R.id.create_camera).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() == null) return;
                        if (denyPost()) {
                            Toast.makeText(getContext(), "You have been banned from posting", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent i = new Intent(getActivity(), CameraActivity.class);
                        PostOptions options = new PostOptions(PostOptions.ContentType.None, PostOptions.ContentSubType.Post, null);
                        i.putExtra(CameraActivity.EXTRA_CAMERA_TYPE, new CameraType(CameraType.CAMERA_EVERYTHING));
                        i.putExtra(CameraActivity.EXTRA_POST_OPTIONS, options);
                        i.putExtra(CameraActivity.EXTRA_RETURN_TYPE, CameraActivity.SEND_POST);
                        getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                    }
                }
        );

        mFloatingActionsMenu.findViewById(R.id.create_upload).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() == null) return;
                        if (denyPost()) {
                            Toast.makeText(getContext(), "You have been banned from posting", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent i = new Intent(getActivity(), GalleryActivity.class);
                        PostOptions options = new PostOptions(PostOptions.ContentType.None, PostOptions.ContentSubType.Post, null);
                        i.putExtra(GalleryActivity.ARG_RETURN_TYPE, CameraActivity.SEND_POST);
                        i.putExtra(GalleryActivity.ARG_POST_OPTIONS, options);
                        getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                    }
                }
        );

        mFloatingActionsMenu.findViewById(R.id.create_text).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() == null) return;
                        if (denyPost()) {
                            Toast.makeText(getContext(), "You have been banned from posting", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent i = new Intent(getActivity(), CreateStatusActivity.class);
                        getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                    }
                });


        return rootView;
    }

    private boolean mCampusFeedNeedsUpdating = true;
    private boolean mFriendsFeedNeedsUpdating = true;


    private boolean denyPost() {
        ModesDisabled modesDisabled = ModesDisabled.getInstance();
        return modesDisabled.anonPosts() && modesDisabled.realPosts();
    }

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
            mFeedFragments[position].getPosts();
            if (position == 0) mCampusFeedNeedsUpdating = false;
            else mFriendsFeedNeedsUpdating = false;
        } else if (notifyChange) {
            mFeedFragments[position].notifyChange();
            notifyChange = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        NotificationsCounterSingleton singleton = NotificationsCounterSingleton.getInstance();
        mNotificationIndicator.setVisibility(singleton.hasMessage() ? View.VISIBLE : View.GONE);
        int count = singleton.getNumMessages();

        mNotificationCount.setText(count < 100 ? count + "" : "+");

        count = singleton.getNumOfNewActivities();
        mUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        mUpdatesCounter.setText(count < 100 ? count + "" : "+");

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
        mFloatingActionsMenu.collapse();
    }


    //returns if success
    public boolean addPostToFeed(Post post) {
        return getActivity() != null &&
                mFeedFragments[0] != null &&
                mFeedFragments[0].addPostToTop(post);
    }

    @Override
    public void resetFragment() {
        mAppBarLayout.setExpanded(true, false);
        mViewPager.setCurrentItem(0, true);
        mFeedFragments[0].scrollUp();
    }

    private Subscription mChatSubscription;

    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            mNotificationIndicator.setVisibility(event.hasNewMessage() ? View.VISIBLE : View.GONE);
            int count = NotificationsCounterSingleton.getInstance().getNumMessages();
            mNotificationCount.setText(count < 100 ? count + "" : "+");
        }
    };

    private Subscription mNotificationSubscription;

    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.getType() == NotificationEvent.ACTIVITY) {
                int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
                mUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                mUpdatesCounter.setText(count < 100 ? count + "" : "+");
            } else if (notificationEvent.getType() == NotificationEvent.DISCOVER) {
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
            }
        }
    };

    private boolean notifyChange = false;

    void notifyFragmentChanged() {
        notifyChange = true;
    }
}
