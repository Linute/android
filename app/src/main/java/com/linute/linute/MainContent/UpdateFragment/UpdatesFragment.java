package com.linute.linute.MainContent.UpdateFragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKActivity;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by QiFeng on 1/6/16.
 */
public class UpdatesFragment extends UpdatableFragment {

    public static final String TAG = UpdatesFragment.class.getSimpleName();
    private RecyclerView mUpdatesRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private UpdatesAdapter mUpdatesAdapter;

    private ArrayList<Update> mRecentUpdates = new ArrayList<>();
    private ArrayList<Update> mOldUpdates = new ArrayList<>();

    private View mEmptyView;

    private boolean mSafeToAddToTop = false;
    private boolean mHasMessage;
    private AppBarLayout mAppBarLayout;

    private Toolbar mToolbar;

    private View mNotificationIndicator;

    private JSONArray mUnreadArray = new JSONArray();

    private Handler mHandler = new Handler();


    //private boolean mCanLoadMore = false;
    //private int mSkip = 0;

    public UpdatesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);

        mUpdatesRecyclerView = (RecyclerView) rootView.findViewById(R.id.updatesFragment_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updatesFragment_swipe_refresh);
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_layout);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpdatesRecyclerView.scrollToPosition(0);
                if (NotificationsCounterSingleton.getInstance().updatesNeedsRefreshing() && !mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    getUpdatesInformation();
                }
            }
        });

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.openDrawer();
            }
        });

        mHasMessage = NotificationsCounterSingleton.getInstance().hasMessage();
        mToolbar.inflateMenu(R.menu.people_fragment_menu);
        View chatActionView = mToolbar.getMenu().getItem(1).getActionView();

        mHasNotifications = NotificationsCounterSingleton.getInstance().hasNotifications();
        mToolbar.setNavigationIcon(mHasNotifications ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

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

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mUpdatesRecyclerView.setLayoutManager(llm);
        mUpdatesRecyclerView.setHasFixedSize(true);

        mUpdatesAdapter = new UpdatesAdapter(getContext(), mRecentUpdates, mOldUpdates);
        mUpdatesRecyclerView.setAdapter(mUpdatesAdapter);

        mEmptyView = rootView.findViewById(R.id.empty_view);

        //NOTE: Code for load more
        /*
        mUpdatesAdapter.setOnLoadMoreListener(new UpdatesAdapter.onLoadMoreListener() {
            @Override
            public void loadMore() {
                if (mCanLoadMore) {
                    UpdatesFragment.this.loadMore();
                } else {
                    //remove the progress bar
                    if (!mOldUpdates.isEmpty()) { //add progress bar to end
                        mOldUpdates.remove(mOldUpdates.size() - 1);
                    } else if (!mRecentUpdates.isEmpty()) //old was empty but new wasn't
                        mRecentUpdates.remove(mRecentUpdates.size() - 1);
                }
            }
        });*/

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUpdatesInformation();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();


        mChatSubscription = NewMessageBus
                .getInstance()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNewMessageSubscriber);

        mNotificationSubscription = NotificationEventBus
                .getInstance()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNotificationEventAction1);


        if (fragmentNeedsUpdating() || NotificationsCounterSingleton.getInstance().updatesNeedsRefreshing()) {
            setFragmentNeedUpdating(false);
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            getUpdatesInformation();
        } else {

            MainActivity activity = (MainActivity) getActivity();

            //Log.i(TAG, "onResume: ");
            if (activity != null && !NotificationsCounterSingleton.getInstance().updatesNeedsRefreshing() && NotificationsCounterSingleton.getInstance().hasNewActivities()) {

                activity.setUpdateNotification(0);
                NotificationsCounterSingleton.getInstance().setNumOfNewActivities(0);
                NotificationsCounterSingleton.getInstance().setUpdatesNeedsRefreshing(false);

                if (!NotificationsCounterSingleton.getInstance().hasNotifications()) {
                    NotificationEventBus.getInstance().setNotification(new NotificationEvent(false));
                }

                if (mUnreadArray.length() > 0) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("activities", mUnreadArray);
                        activity.emitSocket(API_Methods.VERSION + ":activities:read", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mUnreadArray = new JSONArray();
            }

            if (mRecentUpdates.isEmpty() && mOldUpdates.isEmpty()) {
                if (mEmptyView.getVisibility() == View.GONE) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }

        mAppBarLayout.setExpanded(true, false);
    }


    private void getUpdatesInformation() {
        if (getActivity() == null) return;

        mSafeToAddToTop = false;

        new LSDKActivity(getActivity()).getActivities(-1, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showBadConnectiontToast();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resString = response.body().string();

                        JSONArray activities = Update.getJsonArrayFromJson(new JSONObject(resString), "activities");

                        if (activities == null) {
                            showServerErrorToast();
                            return;
                        }
                        //Log.i(TAG, "onResponse: " + activities.getJSONObject(activities.length() - 4).toString(4));
                        ArrayList<Update> oldItems = new ArrayList<>();
                        ArrayList<Update> newItems = new ArrayList<>();

                        //no more information to load
                        //if (activities.length() < 25) mCanLoadMore = false;

                        //mSkip = 25;

                        if (getActivity() == null) return;

                        JSONArray unread = new JSONArray();

                        Update update;
                        //iterate through array of activities
                        for (int i = activities.length() - 1; i >= 0; i--) {
                            try {
                                update = new Update(activities.getJSONObject(i));
                                if (update.isRead()) oldItems.add(update); //if read, it's old
                                else {
                                    newItems.add(update); //else recent
                                    unread.put(update.getActionID());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        final MainActivity activity = (MainActivity) getActivity();
                        if (activity == null) return;


                        if (unread.length() > 0) {
                            JSONObject obj = new JSONObject();
                            obj.put("activities", unread);

                            activity.emitSocket(API_Methods.VERSION + ":activities:read", obj);
                        }

                        mOldUpdates.clear();
                        mOldUpdates.addAll(oldItems);

                        mRecentUpdates.clear();
                        mRecentUpdates.addAll(newItems);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                activity.setUpdateNotification(0);
                                NotificationsCounterSingleton.getInstance().setNumOfNewActivities(0);

                                if (!NotificationsCounterSingleton.getInstance().hasNotifications()) {
                                    NotificationEventBus.getInstance().setNotification(new NotificationEvent(false));
                                }

                                if (mUpdatesAdapter.getItemCount(0) + mUpdatesAdapter.getItemCount(1) == 0) {
                                    if (mEmptyView.getVisibility() == View.GONE)
                                        mEmptyView.setVisibility(View.VISIBLE);
                                } else {
                                    if (mEmptyView.getVisibility() == View.VISIBLE)
                                        mEmptyView.setVisibility(View.GONE);
                                }

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mUpdatesAdapter.notifyDataSetChanged();
                                    }
                                });

                                mSwipeRefreshLayout.setRefreshing(false);
                                mSafeToAddToTop = true;
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast();
                    }
                } else {
                    showServerErrorToast();
                    Log.e(TAG, "onResponse: " + response.body().string());
                }
            }
        });

    }


    private void showServerErrorToast() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Utils.showServerErrorToast(getActivity());
            }
        });
    }

    private void showBadConnectiontToast() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showBadConnectionToast(getActivity());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    public boolean addItemToRecents(final Update update) {
        if (mSafeToAddToTop && !mSwipeRefreshLayout.isRefreshing()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRecentUpdates.isEmpty()) {
                        mRecentUpdates.add(update);
                        mUpdatesAdapter.notifyDataSetChanged();
                    } else {
                        mRecentUpdates.add(0, update);
                        mUpdatesAdapter.notifyItemInserted(1);
                    }
                    if (mEmptyView.getVisibility() == View.VISIBLE) {
                        mEmptyView.setVisibility(View.GONE);
                    }
                }
            });

            mUnreadArray.put(update.getActionID());

            if (isVisible()) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("activities", mUnreadArray);
                        activity.emitSocket(API_Methods.VERSION + ":activities:read", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private boolean mHasNotifications;

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
            if (notificationEvent.hasNotification() != mHasNotifications) {
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
                mHasNotifications = notificationEvent.hasNotification();
            }
        }
    };

}
