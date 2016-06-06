package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
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

/**
 * Created by QiFeng on 11/17/15.
 */

public class DiscoverFragment extends BaseFragment {
    private static final String TAG = DiscoverFragment.class.getSimpleName();
    private static final String SECTION_KEY = "section";
    private RecyclerView recList;

    private View mEmptyView;

    private SwipeRefreshLayout refreshLayout;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private CheckBoxQuestionAdapter mCheckBoxChoiceCapableAdapters;
    private boolean feedDone;

    private boolean mSectionTwo = false;

    private String mCollegeId;

    private Handler mHandler = new Handler();

    public static DiscoverFragment newInstance(boolean friendsOnly) {
        DiscoverFragment fragment = new DiscoverFragment();
        Bundle args = new Bundle();
        args.putBoolean(SECTION_KEY, friendsOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");

        if (getArguments() != null) {
            mSectionTwo = getArguments().getBoolean(SECTION_KEY);
        }

        if (mCheckBoxChoiceCapableAdapters == null) {
            mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(
                    mPosts,
                    getContext(),
                    ((DiscoverHolderFragment) getParentFragment()).getSinglePlaybackManager(),
                    mSectionTwo
            );
        }
    }

    //called when fragment drawn the first time
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //mFriendOnly determines which layout we're using
        //they are identical, but we need to differentiate them for things like setting background

        final View rootView = inflater.inflate(
                R.layout.fragment_discover_feed,
                container, false); //setContent

        mEmptyView = rootView.findViewById(R.id.discover_no_posts_frame);

        recList = (RecyclerView) rootView.findViewById(R.id.eventList);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());

        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recList.setLayoutManager(llm);

        recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));

        mCheckBoxChoiceCapableAdapters.setGetMoreFeed(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (!feedDone)
                    loadMoreFeedFromServer();
            }
        });

        recList.setAdapter(mCheckBoxChoiceCapableAdapters);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        DiscoverHolderFragment fragment = (DiscoverHolderFragment) getParentFragment();

        if (fragment == null) return;

        //initially presented fragment by discoverHolderFragment doesn't get loaded by discoverholderfragment
        //do it in on resume
        //if initial fragment was campus feed, we are in campus feed, and it needs to be updated
        if (fragment.getInitiallyPresentedFragmentWasCampus()
                && !mSectionTwo
                && fragment.getCampusFeedNeedsUpdating()) {
            refreshFeed();
            fragment.setCampusFeedNeedsUpdating(false);
        } else if (!fragment.getInitiallyPresentedFragmentWasCampus()
                && mSectionTwo
                && fragment.getFriendsFeedNeedsUpdating()) {
            refreshFeed();
            fragment.setFriendsFeedNeedsUpdating(false);
        } else {
            if (!mSectionTwo && !fragment.getCampusFeedNeedsUpdating() && mPosts.isEmpty()) {
                ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(R.string.discover_no_posts_campus);
                mEmptyView.requestLayout();
                mEmptyView.setVisibility(View.VISIBLE);

            } else if (mSectionTwo && !fragment.getFriendsFeedNeedsUpdating() && mPosts.isEmpty()) {
                ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(R.string.discover_no_posts_hot);
                mEmptyView.requestLayout();
                mEmptyView.setVisibility(View.VISIBLE);

            }
        }
    }

    private int mSkip = 0;

    private boolean mLoadingMore = false;

    public void loadMoreFeedFromServer() {
        if (getActivity() == null || mLoadingMore || refreshLayout.isRefreshing()) return;

        mLoadingMore = true;

        int skip = mSkip;
        skip -= 25;
        int limit = 25;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        Map<String, String> events = new HashMap<>();

        events.put("college", mCollegeId);
        events.put("skip", skip + "");
        events.put("limit", limit + "");
        LSDKEvents events1 = new LSDKEvents(getActivity());
        events1.getEvents(mSectionTwo, events, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        noInternet();
                        mLoadingMore = false;
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d(TAG, response.body().string());
                            mLoadingMore = false;
                            if (getActivity() != null) { //shows server error toast
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                            return;
                        }

                        String json = response.body().string();
                        //Log.i(TAG, "onResponse: " + json);
                        JSONObject jsonObject;
                        JSONArray jsonArray;

                        final ArrayList<Post> tempList = new ArrayList<>();

                        try {
                            jsonObject = new JSONObject(json);
                            jsonArray = jsonObject.getJSONArray("events");

                            if (skip1 == 0) {
                                feedDone = true; //no more feed to load
                                mCheckBoxChoiceCapableAdapters.setLoadState(LoadMoreViewHolder.STATE_END);
                            }


                            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                                try {
                                    tempList.add(new Post(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mSkip = skip1;
                                                        int size = mPosts.size();
                                                        mPosts.addAll(tempList);
                                                        mCheckBoxChoiceCapableAdapters.notifyItemRangeInserted(size, tempList.size());
                                                        //mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                        }
                                );
                            }
                            mLoadingMore = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mLoadingMore = false;
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                        }

                    }
                }
        );
    }


    public void refreshFeed() {
        if (getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA) return;

        if (!refreshLayout.isRefreshing()){
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            });
        }

        setFragmentState(FragmentState.LOADING_DATA);
        Map<String, String> events = new HashMap<>();

        events.put("college", mCollegeId);
        events.put("limit", "25");
        LSDKEvents events1 = new LSDKEvents(getActivity());

        events1.getEvents(mSectionTwo, events, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        setFragmentState(FragmentState.FINISHED_UPDATING);
                        noInternet();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d(TAG, response.body().string());
                            setFragmentState(FragmentState.FINISHED_UPDATING);
                            if (getActivity() != null) { //shows server error toast
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshLayout.setRefreshing(false);
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                            return;
                        }

                        String json = response.body().string();
                        //Log.i(TAG, "onResponse: "+json);
                        JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {

                            jsonObject = new JSONObject(json);
                            mSkip = jsonObject.getInt("skip");

                            jsonArray = jsonObject.getJSONArray("events");

                            if (mSkip == 0) {
                                feedDone = true; //no more feed to load
                                mCheckBoxChoiceCapableAdapters.setLoadState(LoadMoreViewHolder.STATE_END);
                            }else {
                                feedDone = false;
                                mCheckBoxChoiceCapableAdapters.setLoadState(LoadMoreViewHolder.STATE_LOADING);
                            }

                            final ArrayList<Post> refreshedPosts = new ArrayList<>();

                            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                                try {
                                    refreshedPosts.add(new Post(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }


                            final MainActivity activity = (MainActivity) getActivity();
                            if (activity == null) {
                                setFragmentState(FragmentState.FINISHED_UPDATING);
                                return;
                            }

                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {

                                            if (refreshedPosts.isEmpty()) {
                                                if (mEmptyView.getVisibility() == View.GONE) {
                                                    ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(mSectionTwo ?
                                                            R.string.discover_no_posts_hot : R.string.discover_no_posts_campus);
                                                    mEmptyView.setVisibility(View.VISIBLE);
                                                }
                                            } else if (mEmptyView.getVisibility() == View.VISIBLE) {
                                                mEmptyView.setVisibility(View.GONE);
                                            }

                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mPosts.clear();
                                                    mPosts.addAll(refreshedPosts);
                                                    mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
                                                }
                                            });

                                            NotificationsCounterSingleton t = NotificationsCounterSingleton.getInstance();
                                            t.setDiscoverNeedsRefreshing(false);
                                            if (t.hasNewPosts()) {
                                                t.setNumOfNewPosts(0);
                                                activity.setFeedNotification(0);
                                                if (!t.hasNotifications()) {
                                                    NotificationEventBus.getInstance().setNotification(new NotificationEvent(false));
                                                }
                                            }

                                            refreshLayout.setRefreshing(false);
                                        }
                                    }
                            );

                            setFragmentState(FragmentState.FINISHED_UPDATING);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setFragmentState(FragmentState.FINISHED_UPDATING);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                        refreshLayout.setRefreshing(false);
                                    }
                                });
                            }
                        }
                    }
                }
        );
    }

    private void noInternet() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).noInternet();
                refreshLayout.setRefreshing(false);
            }
        });
    }


    public void scrollUp() {
        recList.scrollToPosition(0);

        if (!mSectionTwo && NotificationsCounterSingleton.getInstance().discoverNeedsRefreshing() && !refreshLayout.isRefreshing()) {
            refreshFeed();
        }
    }

    //when we get new post from socket, try adding to top
    public boolean addPostToTop(final Post post) {
        if (getFragmentState() == FragmentState.LOADING_DATA) return true;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPosts.add(0, post);
                mCheckBoxChoiceCapableAdapters.notifyItemInserted(0);
            }
        });

        if (mEmptyView.getVisibility() == View.VISIBLE) mEmptyView.setVisibility(View.GONE);

        return true;
    }
}
