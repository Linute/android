package com.linute.linute.MainContent.UpdateFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by QiFeng on 1/6/16.
 */
public class UpdatesFragment extends Fragment {

    public static final String TAG = UpdatesFragment.class.getSimpleName();
    private RecyclerView mUpdatesRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private UpdatesAdapter mUpdatesAdapter;

    private ArrayList<Update> mRecentUpdates = new ArrayList<>();
    private ArrayList<Update> mOldUpdates = new ArrayList<>();

    private SharedPreferences mSharedPreferences;


    public UpdatesFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);

        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mUpdatesRecyclerView = (RecyclerView) rootView.findViewById(R.id.updatesFragment_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updatesFragment_swipe_refresh);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mUpdatesRecyclerView.setLayoutManager(llm);

        mUpdatesAdapter = new UpdatesAdapter(getContext(), mRecentUpdates, mOldUpdates);
        mUpdatesRecyclerView.setAdapter(mUpdatesAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUpdatesInformation();
            }
        });

        updateUpdatesInformation();
        return rootView;
    }

    private void updateUpdatesInformation(){

        new LSDKActivity(getContext()).getActivities(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getContext());
                        Log.e(TAG, "onFailure: no connection");
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
//                    Log.i(TAG, "onResponse: "+response.body().string());
                    try {
                        JSONArray activities = Update.getJsonArrayFromJson(new JSONObject(response.body().string()), "activities");

                        if (activities == null){
                            Log.i(TAG, "onResponse: activities was null");
                            showServerErrorToast();
                            return;
                        }

                        mOldUpdates.clear();
                        mRecentUpdates.clear();

                        //iterate through array of activities
                        for (int i = 0; i < activities.length(); i++){
                            Update update = new Update(activities.getJSONObject(i));
                            if (update.isRead()) mOldUpdates.add(update); //if read, it's old
                            else mRecentUpdates.add(update); //else recent
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUpdatesAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast();
                    }
                } else {
                    showServerErrorToast();
                }
            }
        });

    }


    private void showServerErrorToast(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Utils.showServerErrorToast(getContext());
            }
        });
    }


}
