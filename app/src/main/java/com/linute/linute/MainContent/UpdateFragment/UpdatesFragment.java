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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKActivity;
import com.linute.linute.MainContent.Chat.NewChatEvent;
import com.linute.linute.MainContent.Chat.NewMessageBus;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        mToolbar .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpdatesRecyclerView.scrollToPosition(0);
            }
        });
        mToolbar .setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.openDrawer();
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        mHasMessage = activity.hasMessage();
        mToolbar .inflateMenu(activity.hasMessage() ? R.menu.people_fragment_menu_noti : R.menu.people_fragment_menu);
        mToolbar .setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
                updateUpdatesInformation();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();


         mChatSubscription = NewMessageBus.getInstance()
                 .getObservable()
                 .subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(mNewMessageSubscriber);

        if (fragmentNeedsUpdating()) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            getUpdatesInformation();
            setFragmentNeedUpdating(false);
        } else {
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
        if (mChatSubscription != null && !mChatSubscription.isUnsubscribed()){
            mChatSubscription.unsubscribe();
        }

        mAppBarLayout.setExpanded(true,false);
    }


    private void updateUpdatesInformation() {

        if (getActivity() == null) return;
        JSONArray unread = new JSONArray();

        mSafeToAddToTop = false;

        for (Update update : mRecentUpdates) {
            unread.put(update.getActionID());
        }

        if (unread.length() == 0) { //don't need to mark anything as read
            getUpdatesInformation();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("activities", unread);

        new LSDKActivity(getContext()).readActivities(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showBadConnectiontToast();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();
                    getUpdatesInformation();
                } else {
                    showServerErrorToast();
                    Log.e(TAG, "onResponse: " + response.body().string());
                }
            }
        });
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

                        //Log.i(TAG, "onResponse: "+activities.getJSONObject(activities.length()-1).toString());

                        ArrayList<Update> oldItems = new ArrayList<>();
                        ArrayList<Update> newItems = new ArrayList<>();

                        //no more information to load
                        //if (activities.length() < 25) mCanLoadMore = false;

                        //mSkip = 25;

                        if (getActivity() == null) return;

                        Update update;
                        //iterate through array of activities
                        for (int i = activities.length() - 1; i >= 0; i--) {
                            try {
                                update = new Update(activities.getJSONObject(i));
                                if (update.isRead()) oldItems.add(update); //if read, it's old
                                else newItems.add(update); //else recent
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        if (getActivity() == null) return;

                        mOldUpdates.clear();
                        mOldUpdates.addAll(oldItems);

                        mRecentUpdates.clear();
                        mRecentUpdates.addAll(newItems);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mUpdatesAdapter.getItemCount(0) + mUpdatesAdapter.getItemCount(1) == 0) {
                                    if (mEmptyView.getVisibility() == View.GONE)
                                        mEmptyView.setVisibility(View.VISIBLE);
                                } else {
                                    if (mEmptyView.getVisibility() == View.VISIBLE)
                                        mEmptyView.setVisibility(View.GONE);
                                }

                                mUpdatesAdapter.notifyDataSetChanged();
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



    public void addItemToRecents(final Update update) {
        if (mSafeToAddToTop && !mSwipeRefreshLayout.isRefreshing()) {
             new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (mRecentUpdates.isEmpty()){
                        mRecentUpdates.add(update);
                        mUpdatesAdapter.notifyDataSetChanged();
                    }else {
                        mRecentUpdates.add(0, update);
                        mUpdatesAdapter.notifyItemInserted(1);
                    }
                    if (mEmptyView.getVisibility() == View.VISIBLE) {
                        mEmptyView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    private Subscription mChatSubscription;

    private Action1 <NewChatEvent> mNewMessageSubscriber = new Action1<NewChatEvent>() {
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
