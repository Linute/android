package com.linute.linute.MainContent.TaptUser;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.ProfileFragment.ProfileAdapter;
import com.linute.linute.MainContent.ProfileFragment.UserActivityItem;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Arman on 1/9/16.
 */
public class TaptUserProfileFragment extends UpdatableFragment {
    public static final String TAG = TaptUserProfileFragment.class.getSimpleName();

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private ProfileAdapter mProfileAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;

    private LinuteUser mLinuteUser = new LinuteUser();
    private String mUserName;
    private String mTaptUserId;


    public TaptUserProfileFragment() {
        // Required empty public constructor
    }

    public static TaptUserProfileFragment newInstance(String userName, String taptUserId) {
        TaptUserProfileFragment fragment = new TaptUserProfileFragment();
        Bundle args = new Bundle();
        args.putString("NAME", userName);
        args.putString("TAPT", taptUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserName = getArguments().getString("NAME");
            mTaptUserId = getArguments().getString("TAPT");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        mUser = new LSDKUser(getActivity());
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mLinuteUser.setUserID(mTaptUserId);
        mProfileAdapter = new ProfileAdapter(mUserActivityItems, mLinuteUser, getActivity());
        recList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateAndSetHeader();
                setActivities(); //get activities

            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        BaseTaptActivity activity = (BaseTaptActivity)getActivity();

        if (activity != null){ //changes app bar title to user's name
            activity.setTitle(mUserName);
            activity.resetToolbar();
        }

        //if first time creating this fragment
        //won't be loaded again is user gets here using onBack
        if (fragmentNeedsUpdating()){
            mSwipeRefreshLayout.setRefreshing(true);
            updateAndSetHeader();
            setActivities(); //get activities
            setFragmentNeedUpdating(false);
        }
    }

    //get user information from server
    public void updateAndSetHeader() {
        mUser.getProfileInfo(mTaptUserId, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
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
                    mLinuteUser.updateUserInformation(jsonObject); //container for new information
                    Log.d(TAG, "onResponse: " + jsonObject);

//                    Log.d(TAG, body);
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProfileAdapter.notifyDataSetChanged();
                        }
                    });
                } else {//else something went
                    Log.v(TAG, response.body().string());
                }
            }
        });
    }

    public void setActivities() {
        LSDKUser user = new LSDKUser(getActivity());
        user.getUserActivities(mTaptUserId, "posted status", "posted photo", new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        String body = response.body().string();
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response
                        Log.d(TAG, body);


                        //i only update the list of activities if their are new values
                        //array has problems if I continuously update with new values too quickly
                        if (activities.length() != mUserActivityItems.size()) { //we have an updated set of info

                            mUserActivityItems.clear(); //clear so we don't have duplicates
                            for (int i = 0; i < activities.length(); i++) { //add each activity into our array
                                mUserActivityItems.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i),
                                                activities.getJSONObject(i).getJSONObject("owner").getString("profileImage"),
                                                mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", "")
                                        )); //create activity objects and add to array
                            }
                        }
                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());

                }

                if (getActivity() == null) return;

                //turn refresh off if it's on and notify ListView we might have updated information
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //update view
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);

                        //NOTE: maybe move this inside try.
                        //      should we update date text if no connection?
                        mProfileAdapter.notifyDataSetChanged();

                    }
                });

            }
        });
    }

}
