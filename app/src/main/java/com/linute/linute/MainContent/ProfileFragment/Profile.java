package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Profile extends UpdatableFragment {
    public static final String TAG = Profile.class.getSimpleName();

    //public static final String PARCEL_DATA_KEY = "profileFragmentArrayOfActivities";

    private RecyclerView recList;
    private ProfileAdapter mProfileAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;

    //we have 2 seperate queries, one for header and one for activities
    //we call notify only after
    private boolean mOtherCompotentHasUpdated = false;

    private Runnable rServerErrorAction = new Runnable() {
        @Override
        public void run() {
            Utils.showServerErrorToast(getContext());
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    private Runnable rFailedConnectionAction = new Runnable() {
        @Override
        public void run() {
            Utils.showBadConnectionToast(getContext());
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };
    private LinuteUser user;


    private int mSkip = 0;
    private boolean mCanLoadMore = false;

    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        mUser = new LSDKUser(getContext());
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);


        recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);
        GridLayoutManager llm = new GridLayoutManager(getActivity(), 3);

        llm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) return 3; //header is size 3
                else if (position == 1 && mUserActivityItems.get(0) instanceof EmptyUserActivityItem)
                    return 3; //empty view size 3

                else return 1;
            }
        });

        recList.setLayoutManager(llm);
        user = LinuteUser.getDefaultUser(getContext()); //get data from sharedpref
        mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext());
        recList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOtherCompotentHasUpdated = false;
                updateAndSetHeader();
                setActivities(); //get activities

            }
        });

        mProfileAdapter.setLoadMorePosts(new ProfileAdapter.LoadMorePosts() {
            @Override
            public void loadMorePosts() {
                if (mCanLoadMore && !mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    getMoreActivities();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            String name = mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", "");
            mainActivity.setTitle(name);
            mainActivity.resetToolbar();

            //scroll to top of list
            mainActivity.setToolbarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recList != null)
                        recList.smoothScrollToPosition(0);
                }
            });

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID", ""));
                obj.put("action", "active");
                obj.put("screen", "Profile");
                mainActivity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //only update this fragment when it is first created or set to reupdate from outside
        if (fragmentNeedsUpdating()) {
            mSkip = 0;
            mOtherCompotentHasUpdated = false;
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
            updateAndSetHeader(); //get information from server to update profile

            setActivities();
            setFragmentNeedUpdating(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID", ""));
                obj.put("action", "inactive");
                obj.put("screen", "Profile");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();


        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setToolbarOnClickListener(null);
        }

    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) { //saves fragment state
//        outState.putParcelableArrayList(PARCEL_DATA_KEY, mUserActivityItems); //list of activities is saved
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onViewStateRestored(Bundle savedInstanceState) { //gets saved frament state
//        if (savedInstanceState != null) {
//            mUserActivityItems = savedInstanceState.getParcelableArrayList(PARCEL_DATA_KEY);
//        }
//
//        super.onViewStateRestored(savedInstanceState);
//    }

    //get user information from server
    public void updateAndSetHeader() {
        mUser.getProfileInfo(mSharedPreferences.getString("userID", null), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(rFailedConnectionAction);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //attempt to update view with response
                    final String body = response.body().string();
                    if (getActivity() == null) return;
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    user.updateUserInformation(jsonObject); //container for new information
//                    Log.d(TAG, "onResponse: " + jsonObject);

                    savePreferences(user);
//                    Log.d(TAG, body);
                    if (getActivity() == null) return;

                    if (!mOtherCompotentHasUpdated) {
                        mOtherCompotentHasUpdated = true;
                    } else {
                        mOtherCompotentHasUpdated = false;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProfileAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } else {//else something went
                    Log.v(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(rServerErrorAction);
                    }
                }

            }
        });
    }

    private void savePreferences(LinuteUser user) {
        //save the new info
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.putString("status", user.getStatus());
        editor.putInt("posts", user.getPosts());
        editor.putInt("followers", user.getFollowers());
        editor.putInt("following", user.getFollowing());
        editor.putString("id", user.getUserID());
        editor.putString("userImage", user.getProfileImage());
        editor.apply();
    }


    public void setActivities() {

        new LSDKUser(getContext()).getUserActivities(mSharedPreferences.getString("userID", null), 0, new Callback() { //todo fix skip
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showBadConnectionToast(getContext());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response

                        String body = response.body().string();
                        //Log.i(TAG, "onResponse: " + body);
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response

                        if (activities == null) return;

                        ArrayList<UserActivityItem> userActItems = new ArrayList<>();

                        for (int i = 0; i < activities.length(); i++) { //add each activity into our array
                            try {
                                userActItems.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i)
                                        )); //create activity objects and add to array
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //if we got 24 back, there might still be more
                        mCanLoadMore = activities.length() == 24;

                        mUserActivityItems.clear();
                        mUserActivityItems.addAll(userActItems);

                        if (mUserActivityItems.isEmpty()) {
                            mUserActivityItems.add(new EmptyUserActivityItem());
                        }

                        if (getActivity() == null) return;

                        mSkip = 24;

                        if (!mOtherCompotentHasUpdated) {
                            mOtherCompotentHasUpdated = true;
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() { //update view
                                    mOtherCompotentHasUpdated = false;
                                    mProfileAdapter.notifyDataSetChanged();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(rServerErrorAction);
                        }
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
            }
        });
    }


    public void getMoreActivities() {

        new LSDKUser(getContext()).getUserActivities(mSharedPreferences.getString("userID", null), mSkip, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showBadConnectionToast(getContext());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        String body = response.body().string();
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response

                        if (activities == null) return;

                        ArrayList<UserActivityItem> userActItems = new ArrayList<>();

                        String userid = mSharedPreferences.getString("userID", "");

                        for (int i = 0; i < activities.length(); i++) { //add each activity into our array
                            try {
                                userActItems.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i)
                                        )); //create activity objects and add to array
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //if we got 24 back, there might still be more
                        mCanLoadMore = activities.length() == 24;

                        mUserActivityItems.addAll(userActItems);

                        mSkip += 24; //skip 24 posts

                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() { //update view

                                mProfileAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(rServerErrorAction);
                        }
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
            }
        });
    }

}
