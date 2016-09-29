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
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKActivity;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

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
public class UpdatesFragment extends BaseFragment {

    public static final String TAG = UpdatesFragment.class.getSimpleName();
    private RecyclerView mUpdatesRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private UpdatesAdapter mUpdatesAdapter;

    private ArrayList<Update> mRecentUpdates = new ArrayList<>();
    private ArrayList<Update> mOldUpdates = new ArrayList<>();

    private View mEmptyView;

    private AppBarLayout mAppBarLayout;

    private View mNotificationIndicator;
    private TextView vNotificationCounter;

    private JSONArray mUnreadArray = new JSONArray();

    private Handler mHandler = new Handler();


    private boolean mCanLoadMore = true;
    private int mSkip = 0;

    public UpdatesFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mUpdatesAdapter == null)
            mUpdatesAdapter = new UpdatesAdapter(getContext(), mRecentUpdates, mOldUpdates);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);

        mUpdatesRecyclerView = (RecyclerView) rootView.findViewById(R.id.updatesFragment_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updatesFragment_swipe_refresh);
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_layout);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpdatesRecyclerView.scrollToPosition(0);
                if (NotificationsCounterSingleton.getInstance().updatesNeedsRefreshing() && !mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    getUpdatesInformation();
                }
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        toolbar.inflateMenu(R.menu.menu_fragment_updates);
        View chatActionView = toolbar.getMenu().findItem(R.id.menu_chat).getActionView();

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);

        mNotificationIndicator = chatActionView.findViewById(R.id.notification);
        vNotificationCounter = (TextView) chatActionView.findViewById(R.id.notification_count);

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

        mUpdatesAdapter.setOnLoadMore(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (mCanLoadMore && !mSwipeRefreshLayout.isRefreshing() && !mLoadingMore) {
                    loadMoreUpdates();
                }
            }
        });

        mUpdatesAdapter.setRequestManager(Glide.with(this));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUpdatesInformation();
            }
        });


        mUpdatesRecyclerView.setAdapter(mUpdatesAdapter);

        mEmptyView = rootView.findViewById(R.id.empty_view);
        Button emptyButton = (Button)mEmptyView.findViewById(R.id.empty_view_button);
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MainActivity mainActivity = (MainActivity)getActivity();
                Handler handler = new Handler();

                mainActivity.getSupportFragmentManager().popBackStack();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.openDrawer();
                    }
                },600);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.FIND_FRIENDS);
                    }
                },1200);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.closeDrawer();
                    }
                },1600);

            }
        });


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        NotificationsCounterSingleton singleton = NotificationsCounterSingleton.getInstance();
        mNotificationIndicator.setVisibility(singleton.hasMessage() ? View.VISIBLE : View.GONE);
        int count = singleton.getNumMessages();
        vNotificationCounter.setText(count < 100 ? count + "" : "+");

        mChatSubscription = NewMessageBus
                .getInstance()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNewMessageSubscriber);

        if (getFragmentState() == FragmentState.NEEDS_UPDATING || NotificationsCounterSingleton.getInstance().updatesNeedsRefreshing()) {
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
                NotificationsCounterSingleton.getInstance().setNumOfNewActivities(0);
                NotificationsCounterSingleton.getInstance().setUpdatesNeedsRefreshing(false);

                if (mUnreadArray.length() > 0) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("activities", mUnreadArray);
                        TaptSocket.getInstance().emit(API_Methods.VERSION + ":activities:read", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mUnreadArray = new JSONArray();
            }

            if (mRecentUpdates.isEmpty() && mOldUpdates.isEmpty()) {
                if (mEmptyView.getVisibility() == View.GONE) {
                    showEmptyView(true);
                }
            }
        }
    }

    private void showEmptyView(boolean show) {
        if(show) {
            mEmptyView.setVisibility(View.VISIBLE);
            //mSwipeRefreshLayout.setVisibility(View.GONE);
        }else{
            mEmptyView.setVisibility(View.GONE);
            //mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        mAppBarLayout.setExpanded(true, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUpdatesAdapter.getRequestManager() != null)
            mUpdatesAdapter.getRequestManager().onDestroy();
        mUpdatesAdapter.setRequestManager(null);
    }

    private void getUpdatesInformation() {
        if (getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA) return;

        setFragmentState(FragmentState.LOADING_DATA);

        new LSDKActivity(getActivity()).getActivities(-1, 50, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
                showBadConnectiontToast();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());

                        JSONArray activities = obj.getJSONArray("activities");

                        // Log.i(TAG, "onResponse: " + obj.toString(4));

                        final ArrayList<Update> oldItems = new ArrayList<>();
                        final ArrayList<Update> newItems = new ArrayList<>();

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
                        if (activity != null) {

                            if (unread.length() > 0) {
                                JSONObject read = new JSONObject();
                                read.put("activities", unread);
                                TaptSocket.getInstance().emit(API_Methods.VERSION + ":activities:read", read);
                            }

                            mSkip = obj.getInt("skip");

                            //no more information to load
                            if (mSkip <= 0) {
                                mCanLoadMore = false;
                                mUpdatesAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                            } else {
                                mCanLoadMore = true;
                                mUpdatesAdapter.setFooterState(LoadMoreViewHolder.STATE_LOADING);
                            }

                            mSkip -= 50;

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationsCounterSingleton.getInstance().setNumOfNewActivities(0);
                                    NotificationsCounterSingleton.getInstance().setUpdatesNeedsRefreshing(false);

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            mOldUpdates.clear();
                                            mOldUpdates.addAll(oldItems);

                                            mRecentUpdates.clear();
                                            mRecentUpdates.addAll(newItems);

                                            mUpdatesAdapter.notifyDataSetChanged();

                                            if (mRecentUpdates.size() + mOldUpdates.size() == 0) {
                                                if (mEmptyView.getVisibility() == View.GONE)
                                                    showEmptyView(true);
                                            } else {
                                                if (mEmptyView.getVisibility() == View.VISIBLE)
                                                    showEmptyView(false);
                                            }
                                        }
                                    });

                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast();
                    }
                } else {
                    showServerErrorToast();
                    Log.e(TAG, "onResponse: " + response.body().string());
                }

                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        });
    }



    private boolean mLoadingMore = false;

    private void loadMoreUpdates() {
        if (getActivity() == null) return;

        mLoadingMore = true;

        int limit = 50;

        int skip = mSkip;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;
        new LSDKActivity(getActivity()).getActivities(skip1, limit, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());

                        JSONArray activities = obj.getJSONArray("activities");

                        // Log.i(TAG, "onResponse: " + obj.toString(4));

                        final ArrayList<Update> oldItems = new ArrayList<>();
                        final ArrayList<Update> newItems = new ArrayList<>();

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
                        if (activity != null) {
                            if (unread.length() > 0) {
                                JSONObject read = new JSONObject();
                                read.put("activities", unread);
                                TaptSocket.getInstance().emit(API_Methods.VERSION + ":activities:read", read);
                            }

                            //no more information to load
                            if (skip1 <= 0) {
                                mCanLoadMore = false;
                                mUpdatesAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                            }

                            mSkip -= 50;

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mOldUpdates.addAll(oldItems);
                                            mRecentUpdates.addAll(newItems);
                                            mUpdatesAdapter.notifyDataSetChanged();
                                        }
                                    });

                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                } else {
                    Log.i(TAG, "onResponseError: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
                mLoadingMore = false;
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
        if (getFragmentState() == FragmentState.FINISHED_UPDATING && !mSwipeRefreshLayout.isRefreshing()) {
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
                        showEmptyView(false);
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
                        TaptSocket.getInstance().emit(API_Methods.VERSION + ":activities:read", obj);
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

    @Override
    public void resetFragment() {
        mUpdatesRecyclerView.scrollToPosition(0);
    }

    private Subscription mChatSubscription;
    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            int count = NotificationsCounterSingleton.getInstance().getNumMessages();
            mNotificationIndicator.setVisibility(event.hasNewMessage() ? View.VISIBLE : View.GONE);
            vNotificationCounter.setText(count < 100 ? count + "" : "+");

        }
    };

}
