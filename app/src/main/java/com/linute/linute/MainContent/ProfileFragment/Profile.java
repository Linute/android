package com.linute.linute.MainContent.ProfileFragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.Settings.ChangeProfileImageFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Profile extends UpdatableFragment {
    public static final String TAG = Profile.class.getSimpleName();

    public static final String PARCEL_DATA_KEY = "profileFragmentArrayOfActivities";

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private ProfileAdapter mProfileAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;

    public static final int IMAGE_CHANGED = 1234;

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
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        user = LinuteUser.getDefaultUser(getContext()); //get data from sharedpref

        mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext(), Profile.this);
        recList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateAndSetHeader();
                setActivities(); //get activities

            }
        });

        // onCreateView isnt called so this only happens once
        user = LinuteUser.getDefaultUser(getContext());
        mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext(), Profile.this);
        recList.setAdapter(mProfileAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            String name = mSharedPreferences.getString("firstName", "")+ " "+mSharedPreferences.getString("lastName", "");
            mainActivity.setTitle(name);
            mainActivity.resetToolbar();
        }

        //only update this fragment when it is first created or set to reupdate from outside
        if (fragmentNeedsUpdating()) {
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
    public void onSaveInstanceState(Bundle outState) { //saves fragment state
        outState.putParcelableArrayList(PARCEL_DATA_KEY, mUserActivityItems); //list of activities is saved
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) { //gets saved frament state
        if (savedInstanceState != null) {
            mUserActivityItems = savedInstanceState.getParcelableArrayList(PARCEL_DATA_KEY);
        }

        super.onViewStateRestored(savedInstanceState);
    }

    //get user information from server
    public void updateAndSetHeader() {
        mUser.getProfileInfo(mSharedPreferences.getString("userID", null), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(rFailedConnectionAction);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProfileAdapter.notifyDataSetChanged();
                        }
                    });
                } else {//else something went
                    Log.v(TAG, response.body().string());
                    getActivity().runOnUiThread(rServerErrorAction);
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
        LSDKUser user = new LSDKUser(getContext());
        user.getUserActivities(mSharedPreferences.getString("userID", null), "posted status", "posted photo", new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
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
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        String body = response.body().string();
                        Log.i(TAG, "onResponse: "+body);
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response
//                        Log.d(TAG, "onResponse getActivities" + body);

                        if (activities == null || getActivity() == null) return;

                        mUserActivityItems.clear(); //clear so we don't have duplicates
                        for (int i = 0; i < activities.length(); i++) { //add each activity into our array
                            mUserActivityItems.add(
                                    new UserActivityItem(
                                            activities.getJSONObject(i),
                                            activities.getJSONObject(i).getJSONObject("owner").getString("profileImage"),
                                            mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", "")
                                    )); //create activity objects and add to array

                        }
                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                        getActivity().runOnUiThread(rServerErrorAction);
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getContext());
                        }
                    });
                }

                //turn refresh off if it's on and notify ListView we might have updated information
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //update view
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);

                        mProfileAdapter.notifyDataSetChanged();

                    }
                });

            }
        });
    }

}
