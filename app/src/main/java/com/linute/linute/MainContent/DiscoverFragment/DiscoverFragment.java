package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by QiFeng on 11/17/15.
 */
public class DiscoverFragment extends UpdatableFragment {
    private static final String TAG = DiscoverFragment.class.getSimpleName();
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private TextView mEmptyText;

    private SwipeRefreshLayout refreshLayout;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private CheckBoxQuestionAdapter mCheckBoxChoiceCapableAdapters = null;
    private boolean feedDone;

    private boolean mFriendsOnly = false;

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
        if (getArguments() != null){
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


        mEmptyText = (TextView) rootView.findViewById(R.id.discoverFragment_no_found);

        recList = (RecyclerView) rootView.findViewById(R.id.eventList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));
        //recList.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.feed_divider));

        mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext());
        mCheckBoxChoiceCapableAdapters.setGetMoreFeed(new CheckBoxQuestionAdapter.GetMoreFeed() {
            @Override
            public void getMoreFeed() {
                loadMoreFeed();
            }
        });

        Log.i(TAG, "onCreateView: "+mPosts.size());
        recList.setAdapter(mCheckBoxChoiceCapableAdapters);

        //if floating button expanded, collapse it
        recList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((MainActivity) getActivity()).toggleFam();
                return false;
            }
        });

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                feedDone = false;
                getFeed(0);
            }
        });

        Log.i(TAG, "onCreateView: fragment created");
        //NOTE: don't remember what it does. uncomment if somethings happens
//        recList.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                refreshLayout.setEnabled(firstVisibleItem == 0);
//            }
//        });

        return rootView;
    }


    public static final String POST_PARCEL_KEY = "post_parcel_items";
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(POST_PARCEL_KEY, mPosts);
        outState.putBoolean("friendsOnly", mFriendsOnly);
        outState.putBoolean("feedDone", feedDone);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null){
            mFriendsOnly = savedInstanceState.getBoolean("friendsOnly");
            feedDone = savedInstanceState.getBoolean("feedDone");
            mPosts = savedInstanceState.getParcelableArrayList(POST_PARCEL_KEY);

            mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext());
            mCheckBoxChoiceCapableAdapters.setGetMoreFeed(new CheckBoxQuestionAdapter.GetMoreFeed() {
                @Override
                public void getMoreFeed() {
                    loadMoreFeed();
                }
            });

            mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
        }
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
                && !mFriendsOnly
                && fragment.getCampusFeedNeedsUpdating()) {
            getFeed(0);
            fragment.setCampusFeedNeedsUpdating(false);
        }

        else if (!fragment.getInitiallyPresentedFragmentWasCampus()
                && mFriendsOnly
                && fragment.getFriendsFeedNeedsUpdating()) {
            getFeed(0);
            fragment.setFriendsFeedNeedsUpdating(false);
        }
    }

    private int mSkip = 0;

    public void loadMoreFeed() {
        if (feedDone) {
            Toast.makeText(getActivity(), "Sorry Bro, feed is done", Toast.LENGTH_SHORT).show();
        } else {
            getFeed(1);
        }
    }

    public void getFeed(int type) {
        if (type == 1) {
            mSkip += 25;
        } else {
            mSkip = 0;
        }

        final int skip = mSkip;
        if (getActivity() == null) return;

        if (!refreshLayout.isRefreshing()){
            refreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(true);
                }
            });
        }
        Log.i(TAG, "getFeed: jjj");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Map<String, String> events = new HashMap<>();
        events.put("college", sharedPreferences.getString("collegeId", ""));
        events.put("skip", skip + "");
        events.put("limit", "25");
        LSDKEvents events1 = new LSDKEvents(getActivity());
        events1.getEvents(mFriendsOnly, events, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                noInternet();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("HEY", response.body().string());
                    cancelRefresh();
                    return;
                }

                if (skip == 0) {
                    mPosts.clear();
                }
                String json = response.body().string();
                Log.i(TAG, "onResponse: "+json);
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(json);
                    jsonArray = jsonObject.getJSONArray("events");

                    if (!feedDone) {
                        if (jsonArray.length() != 25)
                            feedDone = true;
                    }
                    Post post = null;
                    String postImage = "";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date myDate;
                    String postString;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        if (jsonObject.getJSONArray("images").length() > 0)
                            postImage = (String) jsonObject.getJSONArray("images").get(0);

                        myDate = simpleDateFormat.parse(jsonObject.getString("date"));

                        postString = Utils.getTimeAgoString(myDate.getTime());

//                        Log.d("-TAG-", myDate + " " + postString);
                        post = new Post(
                                jsonObject.getJSONObject("owner").getString("id"),
                                jsonObject.getJSONObject("owner").getString("fullName"),
                                jsonObject.getJSONObject("owner").getString("profileImage"),
                                jsonObject.getString("title"),
                                postImage,
                                jsonObject.getInt("privacy"),
                                jsonObject.getInt("numberOfLikes"),
                                jsonObject.getBoolean("isLiked"),
                                postString,
                                jsonObject.getString("id"),
                                jsonObject.getInt("numberOfComments"));
                        mPosts.add(post);
                        postImage = "";
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                if (getActivity() == null)
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelRefresh();

                        if (mPosts.isEmpty()) {
                            mEmptyText.setText(mFriendsOnly ? getActivity().getString(R.string.no_friends_posts) : getActivity().getString(R.string.no_campus_post));
                            mEmptyText.setVisibility(View.VISIBLE);
                        }else if (mEmptyText.getVisibility() == View.VISIBLE){
                            mEmptyText.setVisibility(View.GONE);
                        }

                        mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void noInternet() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) getActivity()).noInternet();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void cancelRefresh() {
        if (refreshLayout.isRefreshing()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshLayout.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DiscoverHolderFragment holderFragment = (DiscoverHolderFragment) getParentFragment();
        if (holderFragment != null)
            holderFragment.setFragmentNeedUpdating(true);
    }
}
