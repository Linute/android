package com.linute.linute.MainContent.DiscoverFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.Global.GlobalChoiceItem;
import com.linute.linute.MainContent.Global.TrendingPostsFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedFragment;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.TutorialAnimations;
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

public class FeedFragment extends BaseFeedFragment {
    private static final String TAG = FeedFragment.class.getSimpleName();
    private static final String SECTION_KEY = "section";

    private ArrayList<BaseFeedItem> mPosts = new ArrayList<>();
    protected SwipeRefreshLayout vSwipeRefreshLayout;
    private boolean mSectionTwo = false;

    public static FeedFragment newInstance(boolean friendsOnly) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putBoolean(SECTION_KEY, friendsOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectionTwo = getArguments().getBoolean(SECTION_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        vSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_layout);
        vSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPosts();
            }
        });

        //code for empty view button 'tutorial'
        final View emptyButton = vEmptyView.findViewById(R.id.empty_view_button);
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if(!TutorialAnimations.isEmptyFeedPlayed(getContext())) {
                    TutorialAnimations.animateFeedToHottest(activity, view);
                    TutorialAnimations.setIsEmptyFeedPlayed(getContext(),true);
                }else{
                    activity.selectDrawerItem(MainActivity.FRAGMENT_INDEXES.GLOBAL);
                    activity.replaceContainerWithFragment(activity.getFragment(MainActivity.FRAGMENT_INDEXES.GLOBAL));

                    GlobalChoiceItem item = new GlobalChoiceItem("Hottest", null, GlobalChoiceItem.TYPE_HEADER_HOT);
                    activity.addFragmentToContainer(TrendingPostsFragment.newInstance(item), "TREND");
                }
            }
        });


        return root;
    }

    /*@Override
    protected int getEmptyLayout() {
        return 0;
    }*/

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
            getPosts();
            fragment.setCampusFeedNeedsUpdating(false);
        } else if (!fragment.getInitiallyPresentedFragmentWasCampus()
                && mSectionTwo
                && fragment.getFriendsFeedNeedsUpdating()) {
            getPosts();
            fragment.setFriendsFeedNeedsUpdating(false);
        } else {
            if (!mSectionTwo && !fragment.getCampusFeedNeedsUpdating() && mPosts.isEmpty()) {

                ((ImageView) vEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(
                        R.drawable.ic_rocket
                );

                ((TextView) vEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(R.string.discover_no_posts_campus);
                ((Button) vEmptyView.findViewById(R.id.empty_view_button)).setText(R.string.empty_feed_button_campus);

                showEmptyView();

            } else if (mSectionTwo && !fragment.getFriendsFeedNeedsUpdating() && mPosts.isEmpty()) {

                ((ImageView) vEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(
                        R.drawable.ic_fire1
                );

                ((TextView) vEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(R.string.discover_no_posts_hot);
                ((Button) vEmptyView.findViewById(R.id.empty_view_button)).setText(R.string.empty_feed_button_hot);

                showEmptyView();
            }
        }
    }

    private void showEmptyView() {
        vEmptyView.requestLayout();
        vEmptyView.setVisibility(View.VISIBLE);
        //vSwipeRefreshLayout.setVisibility(View.GONE);
    }

    @Override
    protected void getMorePosts() {
        if (getActivity() == null || mLoadingMore || vSwipeRefreshLayout.isRefreshing()) return;

        mLoadingMore = true;

        int skip = mSkip;
        skip -= 25;
        int limit = 25;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        Map<String, Object> events = new HashMap<>();

        events.put("college", mCollegeId);
        events.put("skip", skip + "");
        events.put("limit", limit + "");

        new LSDKEvents(getActivity()).getEvents(mSectionTwo ? "hot" : "discover", events, new Callback() {
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

                        JSONObject jsonObject;
                        JSONArray jsonArray;

                        final ArrayList<BaseFeedItem> tempList = new ArrayList<>();

                        try {
                            jsonObject = new JSONObject(response.body().string());
                            //Log.i(TAG, "onResponse: "+jsonObject.toString(4));
                            jsonArray = jsonObject.getJSONArray("events");

                            if (skip1 == 0) {
                                mFeedDone = true; //no more feed to load
                                mFeedAdapter.setLoadState(LoadMoreViewHolder.STATE_END);
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
                                                        mFeedAdapter.notifyItemRangeInserted(size, tempList.size());
                                                        //mFeedAdapter.notifyDataSetChanged();
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

    //NOTE: TEST FUNCTION
    private Poll getPollItem(){
        Poll p = new Poll(new JSONObject());
        p.setTotalCount(100);
        p.setTitle("TITLE");
        ArrayList<PollChoiceItem> items = new ArrayList<>();
        items.add(new PollChoiceItem("choice 1", 10, "#FBB72E"));
        items.add(new PollChoiceItem("choice 2", 70, "#D0021B"));
        items.add(new PollChoiceItem("choice 3", 10, "#56bb1d"));
        items.add(new PollChoiceItem("choice 4", 10, "#48BEF7"));
        p.setPollChoiceItems(items);
        return p;
    }

    @Override
    public ArrayList<BaseFeedItem> getFeedArray() {
        return mPosts;
    }

    @Override
    protected void initAdapter() {
        if (mFeedAdapter == null) {
            mFeedAdapter = new FeedAdapter(mPosts, getContext(), mSectionTwo);
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_discover_feed;
    }

    @Override
    protected void getPosts() {
        if (getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA) return;

        if (!vSwipeRefreshLayout.isRefreshing()) {
            vSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    vSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        setFragmentState(FragmentState.LOADING_DATA);
        Map<String, Object> events = new HashMap<>();

        events.put("college", mCollegeId);
        events.put("limit", "25");
        LSDKEvents events1 = new LSDKEvents(getActivity());

        events1.getEvents(mSectionTwo ? "hot" : "discover", events, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        setFragmentState(FragmentState.FINISHED_UPDATING);
                        noInternet();
                        if (mPosts.isEmpty())
                            mFeedAdapter.setLoadState(LoadMoreViewHolder.STATE_END);
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
                                        vSwipeRefreshLayout.setRefreshing(false);
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                            return;
                        }

                        JSONObject jsonObject;
                        JSONArray jsonArray;
                        try {

                            jsonObject = new JSONObject(response.body().string());
                            Log.d(TAG, "onResponse: "+jsonObject.toString(4));
                            mSkip = jsonObject.getInt("skip");

                            jsonArray = jsonObject.getJSONArray("events");

                            if (mSkip == 0) {
                                mFeedDone = true; //no more feed to load
                                mFeedAdapter.setLoadState(LoadMoreViewHolder.STATE_END);
                            } else {
                                mFeedDone = false;
                                mFeedAdapter.setLoadState(LoadMoreViewHolder.STATE_LOADING);
                            }

                            final ArrayList<BaseFeedItem> refreshedPosts = new ArrayList<>();

                            // TODO: USED FOR TESTING. REMOVE LATER
                            refreshedPosts.add(getPollItem());

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
                                                if (vEmptyView.getVisibility() == View.GONE) {
                                                    ((ImageView) vEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(
                                                            mSectionTwo ? R.drawable.ic_fire1 : R.drawable.ic_rocket
                                                    );
                                                    ((TextView) vEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(mSectionTwo ?
                                                            R.string.discover_no_posts_hot : R.string.discover_no_posts_campus);

                                                    ((Button) vEmptyView.findViewById(R.id.empty_view_button)).setText((mSectionTwo ? R.string.empty_feed_button_hot : R.string.empty_feed_button_campus));

                                                    showEmptyView();
                                                }
                                            } else if (vEmptyView.getVisibility() == View.VISIBLE) {
                                                vEmptyView.setVisibility(View.GONE);
                                            }

                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mPosts.clear();
                                                    mPosts.addAll(refreshedPosts);
                                                    mFeedAdapter.notifyDataSetChanged();
                                                }
                                            });

                                            if (!mSectionTwo) {
                                                NotificationsCounterSingleton t = NotificationsCounterSingleton.getInstance();
                                                t.setDiscoverNeedsRefreshing(false);

                                                t.setNumOfNewPosts(0);
                                                activity.setFeedNotification(0);
                                                NotificationEventBus.getInstance().setNotification(new NotificationEvent(NotificationEvent.DISCOVER, false));
                                            }
                                            vSwipeRefreshLayout.setRefreshing(false);
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
                                        vSwipeRefreshLayout.setRefreshing(false);
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
                vSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    public void scrollUp() {
        vRecyclerView.scrollToPosition(0);
        if (!mSectionTwo && NotificationsCounterSingleton.getInstance().discoverNeedsRefreshing() && !vSwipeRefreshLayout.isRefreshing()) {
            getPosts();
        }
    }

    //when we get new post from socket, try adding to top
    public boolean addPostToTop(final Post post) {
        if (getFragmentState() == FragmentState.LOADING_DATA) return true;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPosts.add(0, post);
                if (mFeedAdapter != null) {
                    mFeedAdapter.notifyItemInserted(0);
                }
            }
        });

        if (vEmptyView != null && vEmptyView.getVisibility() == View.VISIBLE)
            vEmptyView.setVisibility(View.GONE);


        return true;
    }

    @Override
    protected boolean notifyFeedNeedsUpdating() {
        return false;
    }

    @Override
    protected boolean disableOptions() {
        return vSwipeRefreshLayout.isRefreshing();
    }
}
