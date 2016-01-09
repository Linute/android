package com.linute.linute.MainContent.TaptUser;

import android.app.Dialog;
import android.app.DialogFragment;
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
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
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
public class TaptUserProfileFragment extends DialogFragment {
    public static final String TAG = TaptUserProfileFragment.class.getSimpleName();

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private ProfileAdapter mProfileAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;

    private LinuteUser user = new LinuteUser();
    public boolean hasSetTitle;


    public TaptUserProfileFragment() {
        // Required empty public constructor
    }

    public static TaptUserProfileFragment newInstance() {
        TaptUserProfileFragment fragment = new TaptUserProfileFragment();
        Bundle args = new Bundle();
        args.putInt("DEMO", 10);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mCurrentPage = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        mUser = new LSDKUser(getContext());
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

//        ((MainActivity) getActivity()).resetToolbar();

        recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext());
        recList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                updateAndSetHeader();
                setActivities(); //get activities

            }
        });


        return rootView;
    }

    public void setActivities() {
        LSDKUser user = new LSDKUser(getActivity());
        user.getUserActivities(new Callback() {
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
//                        Log.d("TAG", body);


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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void onBackPressed() {
        this.dismiss();
    }
}
