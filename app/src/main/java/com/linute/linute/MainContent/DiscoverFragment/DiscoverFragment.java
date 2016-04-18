package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
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

/**
 * Created by QiFeng on 11/17/15.
 */

public class DiscoverFragment extends UpdatableFragment {
    private static final String TAG = DiscoverFragment.class.getSimpleName();
    private RecyclerView recList;

    private View mEmptyView;

    private SwipeRefreshLayout refreshLayout;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private CheckBoxQuestionAdapter mCheckBoxChoiceCapableAdapters = null;
    private boolean feedDone;

    private boolean mFriendsOnly = false;

    private String mCollegeId;

    public static DiscoverFragment newInstance(boolean friendsOnly) {
        DiscoverFragment fragment = new DiscoverFragment();
        Bundle args = new Bundle();
        args.putBoolean("friendsOnly", friendsOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");

        if (getArguments() != null) {
            mFriendsOnly = getArguments().getBoolean("friendsOnly");
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

        LinearLayoutManager llm = new CustomLinearLayoutManager(getActivity());

        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recList.setLayoutManager(llm);

        recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));

        mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext(), ((DiscoverHolderFragment) getParentFragment()).getSinglePlaybackManager());
        mCheckBoxChoiceCapableAdapters.setGetMoreFeed(new CheckBoxQuestionAdapter.GetMoreFeed() {
            @Override
            public void getMoreFeed() {
                if(!mRefreshing) {
                    mRefreshing = true;
                    loadMoreFeed();
                }
            }
        });

        recList.setAdapter(mCheckBoxChoiceCapableAdapters);

        //if floating button expanded, collapse it
        recList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((DiscoverHolderFragment) getParentFragment()).toggleFab();
                return false;
            }
        });


        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFeed();
            }
        });

//        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            private boolean stoppedVideos = false;
//            private int deltaY = 0;
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (!stoppedVideos && (deltaY > 100 || deltaY < -100)) {
//                    mVideoPlayerManager.stopAnyPlayback();
//                    stoppedVideos = true;
//                }else {
//                    deltaY += dy;
//                }
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE){
//                    stoppedVideos = false;
//                    deltaY = 0;
//                }
//            }
//        });


        return rootView;
    }


    //public static final String POST_PARCEL_KEY = "post_parcel_items";

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        //outState.putParcelableArrayList(POST_PARCEL_KEY, mPosts);
//        outState.putBoolean("friendsOnly", mFriendsOnly);
//        outState.putBoolean("feedDone", feedDone);
//        super.onSaveInstanceState(outState);
//    }

//    @Override
//    public void onViewStateRestored(Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        if (savedInstanceState != null) {
//            mFriendsOnly = savedInstanceState.getBoolean("friendsOnly");
//            feedDone = savedInstanceState.getBoolean("feedDone");
//            //mPosts = savedInstanceState.getParcelableArrayList(POST_PARCEL_KEY);
//
////            DiscoverHolderFragment fragment = (DiscoverHolderFragment) getParentFragment();
////            if (fragment == null) return;
////
////            mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext(), fragment.getSinglePlaybackManager());
////            mCheckBoxChoiceCapableAdapters.setGetMoreFeed(new CheckBoxQuestionAdapter.GetMoreFeed() {
////                @Override
////                public void getMoreFeed() {
////                    loadMoreFeed();
////                }
////            });
////
////            mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
//        }
    //}


    @Override
    public void onResume() {
        super.onResume();

        DiscoverHolderFragment fragment = (DiscoverHolderFragment) getParentFragment();

        if (fragment == null) return;

        //initially presented fragment by discoverHolderFragment doesn't get loaded by discoverholderfragment
        //do it in on resume
        //if initial fragment was campus feed, we are in campus feed, and it needs to be updated
        if (fragment.getInitiallyPresentedFragmentWasCampus()
                && !mFriendsOnly
                && fragment.getCampusFeedNeedsUpdating()) {
            refreshFeed();
            fragment.setCampusFeedNeedsUpdating(false);
        } else if (!fragment.getInitiallyPresentedFragmentWasCampus()
                && mFriendsOnly
                && fragment.getFriendsFeedNeedsUpdating()) {
            refreshFeed();
            fragment.setFriendsFeedNeedsUpdating(false);
        } else {
            if (!mFriendsOnly && !fragment.getCampusFeedNeedsUpdating() && mPosts.isEmpty()) {
                ((ImageView) mEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(R.drawable.campus);
                ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(R.string.discover_no_posts_campus);
                mEmptyView.requestLayout();
                mEmptyView.setVisibility(View.VISIBLE);

            } else if (mFriendsOnly && !fragment.getFriendsFeedNeedsUpdating() && mPosts.isEmpty()) {
                ((ImageView) mEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(R.drawable.loser_512);
                ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(R.string.discover_no_posts_friends);
                mEmptyView.requestLayout();
                mEmptyView.setVisibility(View.VISIBLE);

            }
        }
    }

    private int mSkip = 0;

    public void loadMoreFeed() {

        if (feedDone) {
            Toast.makeText(getActivity(), "Sorry Bro, feed is done", Toast.LENGTH_SHORT).show();
            mRefreshing = false;
        } else {
            loadMoreFeedFromServer();
        }
    }

    public void loadMoreFeedFromServer() {

        mSkip -= 25;
        int limit = 25;

        if (mSkip < 0) {
            limit += mSkip;
            mSkip = 0;
        }

        final int skip = mSkip;

        if (getActivity() == null) return;

        if (!refreshLayout.isRefreshing()) {
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            });
        }


        Map<String, String> events = new HashMap<>();
        if (!mFriendsOnly) {
            events.put("college", mCollegeId);
        }

        events.put("skip", skip + "");
        events.put("limit", limit + "");
        LSDKEvents events1 = new LSDKEvents(getActivity());
        events1.getEvents(mFriendsOnly, events, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        noInternet();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            //Log.d("HEY", response.body().string());
                            if (getActivity() != null) { //shows server error toast
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshLayout.setRefreshing(false);
                                        mRefreshing = false;
                                        Log.i(TAG, "run: 5"+mRefreshing);
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
                        try {
                            jsonObject = new JSONObject(json);
                            jsonArray = jsonObject.getJSONArray("events");

                            if (mSkip == 0)
                                feedDone = true; //no more feed to load

                            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                                try {
                                    mPosts.add(new Post(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mPosts.isEmpty()) {
                                                if (mEmptyView.getVisibility() == View.GONE) {
                                                    ((ImageView) mEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(mFriendsOnly ? R.drawable.loser_512 : R.drawable.campus);
                                                    ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(mFriendsOnly ? R.string.discover_no_posts_friends : R.string.discover_no_posts_campus);
                                                    mEmptyView.setVisibility(View.VISIBLE);
                                                }
                                            } else if (mEmptyView.getVisibility() == View.VISIBLE) {
                                                mEmptyView.setVisibility(View.GONE);
                                            }
                                            mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
                                            mRefreshing = false;
                                            cancelRefresh();
                                        }
                                    }

                            );

                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                        refreshLayout.setRefreshing(false);
                                        mRefreshing = false;
                                    }
                                });
                            }
                        }
                    }
                }
        );
    }


    private boolean mRefreshing = true;

    public void refreshFeed() {

        if (getActivity() == null) return;

        if (!refreshLayout.isRefreshing()) {
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            });
        }

        mRefreshing = true;

        Map<String, String> events = new HashMap<>();
        events.put("college", mCollegeId);
        events.put("limit", "25");
        LSDKEvents events1 = new LSDKEvents(getActivity());

        events1.getEvents(mFriendsOnly, events, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        noInternet();
                        mRefreshing = false;
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d("HEY", response.body().string());
                            if (getActivity() != null) { //shows server error toast
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (refreshLayout.isRefreshing()) {
                                            refreshLayout.setRefreshing(false);
                                            mRefreshing = false;
                                        }
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

                            if (mSkip == 0)
                                feedDone = true; //no more feed to load

                            ArrayList<Post> refreshedPosts = new ArrayList<>();

                            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                                try {
                                    refreshedPosts.add(new Post(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }


                            mPosts.clear();
                            mPosts.addAll(refreshedPosts);

                            if (getActivity() == null) {
                                return;
                            }

                            getActivity().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            cancelRefresh();

                                            if (mPosts.isEmpty()) {
                                                if (mEmptyView.getVisibility() == View.GONE) {
                                                    ((ImageView) mEmptyView.findViewById(R.id.discover_no_posts)).setImageResource(mFriendsOnly ? R.drawable.loser_512 : R.drawable.campus);
                                                    ((TextView) mEmptyView.findViewById(R.id.dicover_no_posts_text)).setText(mFriendsOnly ? R.string.discover_no_posts_friends : R.string.discover_no_posts_campus);
                                                    mEmptyView.setVisibility(View.VISIBLE);
                                                }
                                            } else if (mEmptyView.getVisibility() == View.VISIBLE) {
                                                mEmptyView.setVisibility(View.GONE);
                                            }
                                            mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();

                                            mRefreshing = false;
                                        }
                                    }

                            );


                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                        refreshLayout.setRefreshing(false);
                                        mRefreshing = false;
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

    private void cancelRefresh() {
        if (getActivity() == null) return;

        if (refreshLayout.isRefreshing()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }

    public void scrollUp() {
        if (recList != null) {
            mCheckBoxChoiceCapableAdapters.setSendImpressions(false);
            recList.scrollToPosition(0);
        }
    }

    public boolean addPostToTop(final Post post) {
        if (mRefreshing) return false;

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mPosts.add(0, post);
                mCheckBoxChoiceCapableAdapters.notifyItemInserted(0);
                if (mEmptyView.getVisibility() == View.VISIBLE) mEmptyView.setVisibility(View.GONE);
            }
        });

        return true;
    }
}
