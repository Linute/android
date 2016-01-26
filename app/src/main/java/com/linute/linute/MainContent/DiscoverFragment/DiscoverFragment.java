package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by QiFeng on 11/17/15.
 */
public class DiscoverFragment extends UpdatableFragment {
    private static final String TAG = DiscoverFragment.class.getSimpleName();
    private RecyclerView recList;
    private LinearLayoutManager llm;
    private EditText postBox;

    private TextView mEmptyText;

    private SwipeRefreshLayout refreshLayout;

    private List<Post> mPosts;
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

        mPosts = new ArrayList<>();

        mEmptyText = (TextView) rootView.findViewById(R.id.discoverFragment_no_found);

//        ((MainActivity) getActivity()).setTitle("My Campus");
//        ((MainActivity) getActivity()).resetToolbar();

        recList = (RecyclerView) rootView.findViewById(R.id.eventList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext());
        mCheckBoxChoiceCapableAdapters.setGetMoreFeed(new CheckBoxQuestionAdapter.GetMoreFeed() {
            @Override
            public void getMoreFeed() {
                getFeed(1);
            }
        });

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

//        refreshLayout.setRefreshing(true);
//        getFeed(0);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fragmentNeedsUpdating() && getActivity() != null){
            Log.i(TAG, "onResume: ");
            getFeed(0);
        }
    }

    private int mSkip = 0;

    public void getFeed(int type) {
        if (feedDone) {
            Toast.makeText(getActivity(), "Sorry Bro, feed is done", Toast.LENGTH_SHORT).show();
            return;
        }
        if (type == 1) {
            mSkip += 25;
        } else {
            mSkip = 0;
        }

        final int skip = mSkip;

        SharedPreferences sharedPreferences = getParentFragment().getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Map<String, String> events = new HashMap<>();
        events.put("college", sharedPreferences.getString("collegeId", ""));
        events.put("skip", skip + "");
        LSDKEvents events1 = new LSDKEvents(getActivity());
        events1.getEvents(mFriendsOnly, events, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                noInternet();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d("HEY", "STOP IT");
//                    Toast.makeText(getActivity(), "Oops, looks like something went wrong", Toast.LENGTH_SHORT).show();
                    cancelRefresh();
                    return;
                }

                if (skip == 0) {
                    mPosts.clear();
                }
                String json = response.body().string();
//                Log.d(TAG, json);
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
                                jsonObject.getString("likeID"),
                                postString,
                                jsonObject.getString("id"));
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

}
