package com.linute.linute.MainContent.TaptUser;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.ProfileFragment.EmptyUserActivityItem;
import com.linute.linute.MainContent.ProfileFragment.ProfileAdapter;
import com.linute.linute.MainContent.ProfileFragment.UserActivityItem;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
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

/**
 * Created by Arman on 1/9/16.
 */
public class TaptUserProfileFragment extends UpdatableFragment {
    public static final String TAG = TaptUserProfileFragment.class.getSimpleName();

    private RecyclerView recList;
    private GridLayoutManager llm;
    private ProfileAdapter mProfileAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;

    private LinuteUser mLinuteUser = new LinuteUser();
    private String mUserName;
    private String mTaptUserId;

    private boolean mOtherSectionUpdated = false;


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
        llm = new GridLayoutManager(getActivity(), 3);
        llm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) return 3;
                else if (position == 1 && mUserActivityItems.get(0) instanceof EmptyUserActivityItem) return 3;
                else return 1;
            }
        });
        recList.setLayoutManager(llm);
        //recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mLinuteUser.setUserID(mTaptUserId);
        mProfileAdapter = new ProfileAdapter(mUserActivityItems, mLinuteUser, getActivity());
        recList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOtherSectionUpdated = false;
                updateAndSetHeader();
                setActivities(); //get activities

            }
        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity != null) { //changes app bar title to user's name
            activity.setTitle(mUserName);
            activity.resetToolbar();
            activity.setToolbarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recList != null)
                        recList.smoothScrollToPosition(0);
                }
            });

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID",""));
                obj.put("action", "active");
                obj.put("screen", "Visitor");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //if first time creating this fragment
        //won't be loaded again is user gets here using onBack
        if (fragmentNeedsUpdating()) {
            mSwipeRefreshLayout.setRefreshing(true);
            updateAndSetHeader();
            setActivities(); //get activities
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
                obj.put("owner", mSharedPreferences.getString("userID",""));
                obj.put("action", "inactive");
                obj.put("screen", "Visitor");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null){
            activity.setToolbarOnClickListener(null);
        }
    }

    //get user information from server
    public void updateAndSetHeader() {
        mUser.getProfileInfo(mTaptUserId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
                    mLinuteUser.updateUserInformation(jsonObject); //container for new information
                    Log.d(TAG, "onResponse: setting header -- " + jsonObject);

//                    Log.d(TAG, body);
                    if (getActivity() == null) return;

                    if (!mOtherSectionUpdated){
                        mOtherSectionUpdated = true;
                    }else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mOtherSectionUpdated = false;
                                mSwipeRefreshLayout.setRefreshing(false);
                                mProfileAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } else {//else something went
                    Log.v(TAG, response.code() + response.body().string());
                }
            }
        });
    }

    public void setActivities() {
        LSDKUser user = new LSDKUser(getActivity());
        user.getUserActivities(mTaptUserId, "posted status", "posted photo", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        String body = response.body().string();
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response
//                        Log.d(TAG, body);

                        if (activities == null || getActivity() == null) return;

                        ArrayList<UserActivityItem> tempActivies = new ArrayList<>();

                        for (int i = 0; i < activities.length(); i++) { //add each activity into our array
                            try {
                                tempActivies.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i)
                                        )); //create activity objects and add to array
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }

                        mUserActivityItems.clear();
                        mUserActivityItems.addAll(tempActivies);
                        if (mUserActivityItems.isEmpty()) {
                            mUserActivityItems.add(new EmptyUserActivityItem());
                        }

                        if (getActivity() == null) return;

                        //turn refresh off if it's on and notify ListView we might have updated information
                        if (!mOtherSectionUpdated){
                            mOtherSectionUpdated = true;
                        }else {
                            mOtherSectionUpdated = false;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() { //update view
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    mProfileAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
    }

}
