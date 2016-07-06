package com.linute.linute.MainContent.Global;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
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
public class GlobalFragment extends BaseFragment {

    private static final String TAG = GlobalFragment.class.getSimpleName();
    private RecyclerView vRecycler;
    private SwipeRefreshLayout vSwipe;
    private GlobalChoicesAdapter mGlobalChoicesAdapter;
    private ArrayList<GlobalChoiceItem> mGlobalChoiceItems = new ArrayList<>();
    private Toolbar vToolbar;
    private Handler mHandler = new Handler();
    private AppBarLayout vAppBarLayout;
    private View vNotificationIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGlobalChoicesAdapter  = new GlobalChoicesAdapter(getContext(), mGlobalChoiceItems);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_global_choices, container, false);

        mGlobalChoicesAdapter.setGoToTrend(new GlobalChoicesAdapter.GoToTrend() {
            @Override
            public void goToTrend(String id, String title) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null){
                    activity.addFragmentToContainer(TrendingPostsFragment.newInstance(id, title), "TREND");
                }
            }
        });

        vAppBarLayout = (AppBarLayout) root.findViewById(R.id.appbar_layout);
        vRecycler = (RecyclerView)root.findViewById(R.id.recycler_view);
        vSwipe = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);
        vRecycler.setAdapter(mGlobalChoicesAdapter);
        vRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
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

        View chatActionView = vToolbar.getMenu().findItem(R.id.menu_chat).getActionView();

        mHasMessage = NotificationsCounterSingleton.getInstance().hasMessage();
        mHasNotifications = NotificationsCounterSingleton.getInstance().hasNotifications();

        View updatesActionView = vToolbar.getMenu().findItem(R.id.menu_updates).getActionView();


        updatesActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if(activity != null){
                    activity.addFragmentToContainer(new UpdatesFragment());
                }
            }
        });

        vToolbar.setNavigationIcon(mHasNotifications ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

        vNotificationIndicator = chatActionView.findViewById(R.id.notification);
        vNotificationIndicator.setVisibility(mHasMessage ? View.VISIBLE : View.GONE);


        chatActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.addFragmentToContainer(new RoomsActivityFragment(), RoomsActivityFragment.TAG);
                }
            }
        });

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
        if (getFragmentState() == FragmentState.NEEDS_UPDATING){
            vSwipe.post(new Runnable() {
                @Override
                public void run() {
                    vSwipe.setRefreshing(true);
                }
            });
            getChoices();
        }

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
        vAppBarLayout.setExpanded(true,false);

        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }
    }

    public void getChoices(){

        if (getActivity() == null) return;

        setFragmentState(FragmentState.LOADING_DATA);

        new LSDKGlobal(getActivity()).getTrending(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null){
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
                if (response.isSuccessful()){

                    try {
                        JSONArray trends = new JSONObject(response.body().string()).getJSONArray("trends");

                        //Log.i(TAG, "onResponse: "+trends.toString(4));
                        ArrayList<GlobalChoiceItem> tempList = new ArrayList<>();
                        JSONObject trend;

                        for (int i = 0; i < trends.length() ; i++){
                            try{
                                trend = trends.getJSONObject(i);
                                tempList.add(
                                        new GlobalChoiceItem(
                                                trend.getString("name"),
                                                trend.getString("image"),
                                                trend.getString("id")
                                        )
                                );
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }

                        mGlobalChoiceItems.clear();
                        mGlobalChoiceItems.addAll(tempList);

                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    vSwipe.setRefreshing(false);
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mGlobalChoicesAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                    vSwipe.setRefreshing(false);
                                }
                            });
                        }
                    }
                }else {
                    if (getActivity() != null){
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
    public void resetFragment(){
        vRecycler.scrollToPosition(0);
    }

    private boolean mHasNotifications;
    private boolean mHasMessage;

    private Subscription mChatSubscription;
    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            if (event.hasNewMessage() != mHasMessage) {
                vNotificationIndicator.setVisibility(event.hasNewMessage() ? View.VISIBLE : View.GONE);
                mHasMessage = event.hasNewMessage();
            }
        }
    };


    private Subscription mNotificationSubscription;
    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.hasNotification() != mHasNotifications) {
                vToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
                mHasNotifications = notificationEvent.hasNotification();
            }
        }
    };
}
