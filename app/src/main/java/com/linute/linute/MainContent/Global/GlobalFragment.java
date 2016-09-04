package com.linute.linute.MainContent.Global;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
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
 * Created by QiFeng on 5/14/16.
 */
public class GlobalFragment extends BaseFragment implements GlobalChoicesAdapter.GoToTrend {

    private static final String TAG = GlobalFragment.class.getSimpleName();

    private RecyclerView vRecycler;
    private SwipeRefreshLayout vSwipe;
    private GlobalChoicesAdapter mGlobalChoicesAdapter;
    private ArrayList<GlobalChoiceItem> mGlobalChoiceItems = new ArrayList<>();
    private Toolbar vToolbar;
    private Handler mHandler = new Handler();
    private AppBarLayout vAppBarLayout;

    private View vNotificationIndicator;
    private TextView vNotificationCounter;

    private TextView vUpdateCounter;
    private View vUpdateNotification;

    private View vEmpty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_global_choices, container, false);

        if (mGlobalChoicesAdapter == null) {
            mGlobalChoicesAdapter = new GlobalChoicesAdapter(getContext(), mGlobalChoiceItems);
        }
        mGlobalChoicesAdapter.setGoToTrend(this);

        vEmpty = root.findViewById(R.id.empty_view);
        vAppBarLayout = (AppBarLayout) root.findViewById(R.id.appbar_layout);
        vRecycler = (RecyclerView) root.findViewById(R.id.recycler_view);
        vSwipe = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);

        mGlobalChoicesAdapter.setRequestManager(Glide.with(this));

        vRecycler.setAdapter(mGlobalChoicesAdapter);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //headers are 1 span
                return mGlobalChoiceItems.get(position).type == GlobalChoiceItem.TYPE_TREND ? 2 : 1;
            }
        });

        vRecycler.setLayoutManager(manager);
        vRecycler.setHasFixedSize(true);

        vToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        vToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vRecycler.scrollToPosition(0);
            }
        });
        vToolbar.inflateMenu(R.menu.people_fragment_menu);
        vToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.openDrawer();
            }
        });

        View update = vToolbar.getMenu().findItem(R.id.menu_updates).getActionView();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addActivityFragment();
            }
        });

        vUpdateNotification = update.findViewById(R.id.notification);
        vUpdateCounter = (TextView) vUpdateNotification.findViewById(R.id.notification_count);

        vToolbar.setNavigationIcon(NotificationsCounterSingleton.getInstance().hasNewPosts() ?
                R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

        View chat = vToolbar.getMenu().findItem(R.id.menu_chat).getActionView();
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addFragmentToContainer(new RoomsActivityFragment(), RoomsActivityFragment.TAG);
            }
        });
        vNotificationIndicator = chat.findViewById(R.id.notification);
        vNotificationCounter = (TextView) chat.findViewById(R.id.notification_count);

        vSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChoices();
            }
        });


        return root;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addHotAndFriends(mGlobalChoiceItems);
                    mGlobalChoicesAdapter.notifyDataSetChanged();
                }
            });
            vSwipe.post(new Runnable() {
                @Override
                public void run() {
                    vSwipe.setRefreshing(true);
                }
            });
            getChoices();
        } else if (mGlobalChoiceItems.isEmpty()) {
            vEmpty.setVisibility(View.VISIBLE);
        }


        NotificationsCounterSingleton singleton = NotificationsCounterSingleton.getInstance();

        int count = singleton.getNumOfNewActivities();
        vUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        vUpdateCounter.setText(count < 100 ? count + "" : "+");

        count = singleton.getNumMessages();

        vNotificationIndicator.setVisibility(singleton.hasMessage() ? View.VISIBLE : View.GONE);
        vNotificationCounter.setText(count < 100 ? count + "" : "+");

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

    }

    @Override
    public void onPause() {
        super.onPause();
        vAppBarLayout.setExpanded(true, false);

        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }
    }

    public void getChoices() {

        if (getActivity() == null) return;

        setFragmentState(FragmentState.LOADING_DATA);

        new LSDKGlobal(getActivity()).getTrending(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vSwipe.setRefreshing(false);
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        JSONArray trends = new JSONObject(response.body().string()).getJSONArray("trends");

                        // Log.d(TAG, "onResponse: " + trends.toString(4));
                        final ArrayList<GlobalChoiceItem> tempList = new ArrayList<>();
                        JSONObject trend;

                        addHotAndFriends(tempList);
                        GlobalChoiceItem item;

                        for (int i = 0; i < trends.length(); i++) {
                            try {

                                trend = trends.getJSONObject(i);
                                item = new GlobalChoiceItem(
                                        trend.getString("name"),
                                        trend.getString("description"),
                                        trend.getString("image"),
                                        trend.getString("id")
                                );
                                item.setUnread(trend.getInt("badge"));
                                tempList.add(item);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        GlobalChoiceItem.sort(tempList);


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    vSwipe.setRefreshing(false);
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mGlobalChoiceItems.clear();
                                            mGlobalChoiceItems.addAll(tempList);
                                            mGlobalChoicesAdapter.notifyDataSetChanged();
                                            vEmpty.setVisibility(mGlobalChoiceItems.isEmpty() ? View.VISIBLE : View.GONE);
                                        }
                                    });
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
                                    vSwipe.setRefreshing(false);
                                }
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                vSwipe.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void resetFragment() {
        vRecycler.scrollToPosition(0);
    }


    private Subscription mChatSubscription;
    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            int count = NotificationsCounterSingleton.getInstance().getNumMessages();
            vNotificationIndicator.setVisibility(event.hasNewMessage() ? View.VISIBLE : View.GONE);
            vNotificationCounter.setText(count < 100 ? count + "" : "+");
        }
    };


    private Subscription mNotificationSubscription;
    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.getType() == NotificationEvent.DISCOVER) {
                vToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
            } else if (notificationEvent.getType() == NotificationEvent.ACTIVITY) {
                int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
                vUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                vUpdateCounter.setText(count < 100 ? count + "" : "+");
            }
        }
    };

    @Override
    public void goToTrend(GlobalChoiceItem item) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.addFragmentToContainer(TrendingPostsFragment.newInstance(item), "TREND");
        }
    }

    private void addHotAndFriends(ArrayList<GlobalChoiceItem> items) {
        items.add(new GlobalChoiceItem("hottest", null, GlobalChoiceItem.TYPE_HEADER_HOT));
        items.add(new GlobalChoiceItem("friends", null, GlobalChoiceItem.TYPE_HEADER_FRIEND));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGlobalChoicesAdapter.getRequestManager() != null)
            mGlobalChoicesAdapter.getRequestManager().onDestroy();

        mGlobalChoicesAdapter.setRequestManager(null);
    }
}
