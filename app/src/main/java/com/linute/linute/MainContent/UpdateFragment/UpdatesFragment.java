package com.linute.linute.MainContent.UpdateFragment;

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
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by QiFeng on 1/6/16.
 */
public class UpdatesFragment extends UpdatableFragment {

    public static final String TAG = UpdatesFragment.class.getSimpleName();
    private RecyclerView mUpdatesRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private UpdatesAdapter mUpdatesAdapter;

    private ArrayList<Update> mRecentUpdates = new ArrayList<>();
    private ArrayList<Update> mOldUpdates = new ArrayList<>();

    private View mEmptyView;

    //private SharedPreferences mSharedPreferences;
    //private Integer mSkip = 25;
    //private boolean mCanLoadMore = true;

    public UpdatesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);

        mUpdatesRecyclerView = (RecyclerView) rootView.findViewById(R.id.updatesFragment_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updatesFragment_swipe_refresh);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mUpdatesRecyclerView.setLayoutManager(llm);

        mUpdatesAdapter = new UpdatesAdapter(getContext(), mRecentUpdates, mOldUpdates);
        mUpdatesRecyclerView.setAdapter(mUpdatesAdapter);

        mEmptyView = rootView.findViewById(R.id.updateFragment_empty);
        //NOTE: Code for load more
        /*
        mUpdatesAdapter.setOnLoadMoreListener(new UpdatesAdapter.onLoadMoreListener() {
            @Override
            public void loadMore() {
                if (mCanLoadMore) {
                    UpdatesFragment.this.loadMore();
                } else {
                    //remove the progress bar
                    if (!mOldUpdates.isEmpty()) { //add progress bar to end
                        mOldUpdates.remove(mOldUpdates.size() - 1);
                    } else if (!mRecentUpdates.isEmpty()) //old was empty but new wasn't
                        mRecentUpdates.remove(mRecentUpdates.size() - 1);
                }
            }
        });*/

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateUpdatesInformation();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setTitle("Updates");
            mainActivity.resetToolbar();
        }
        if (fragmentNeedsUpdating()) {

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            getUpdatesInformation();
            setFragmentNeedUpdating(false);
        }
    }




    private void updateUpdatesInformation() {
        JSONArray unread = new JSONArray();
        for (Update update : mRecentUpdates) {
            unread.put(update.getActionID());
        }

        if (unread.length() == 0) { //don't need to mark anything as read
            getUpdatesInformation();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("activities", unread);

        new LSDKActivity(getContext()).readActivities(params, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                showBadConnectiontToast();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();
                    getUpdatesInformation();
                } else {
                    showServerErrorToast();
                    Log.e(TAG, "onResponse: " + response.body().string());
                }
            }
        });
    }

    private void getUpdatesInformation() {

        new LSDKActivity(getContext()).getActivities(0, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBadConnectiontToast();
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resString = response.body().string();
                        Log.i(TAG, "onResponse: "+resString);

                        JSONArray activities = Update.getJsonArrayFromJson(new JSONObject(resString), "activities");

                        if (activities == null) {
                            Log.i(TAG, "onResponse: activities was null");
                            showServerErrorToast();
                            return;
                        }

                        mOldUpdates.clear();
                        mRecentUpdates.clear();

                        //no more information to load
                        //if (activities.length() < 25) mCanLoadMore = false;

                        //mSkip = 25;

                        //iterate through array of activities
                        for (int i = 0; i < activities.length(); i++) {
                            Update update = new Update(activities.getJSONObject(i));
                            if (update.isRead()) mOldUpdates.add(update); //if read, it's old
                            else mRecentUpdates.add(update); //else recent
                        }

                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mOldUpdates.isEmpty() && mRecentUpdates.isEmpty())
                                    if (mEmptyView.getVisibility() == View.GONE) mEmptyView.setVisibility(View.VISIBLE);
                                else
                                    if (mEmptyView.getVisibility() == View.VISIBLE) mEmptyView.setVisibility(View.GONE);

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
                    Log.e(TAG, "onResponse: " + response.body().string());
                }
            }
        });

    }


    /* NOTE: LOAD MORE
    private void loadMore() {
        new LSDKActivity(getContext()).getActivities(mSkip, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                showBadConnectiontToast();
                mUpdatesAdapter.setAutoLoadMore(false);
                notifyUpdate();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray activities = Update.getJsonArrayFromJson(new JSONObject(response.body().string()), "activities");

                        if (activities == null) {
                            mUpdatesAdapter.setAutoLoadMore(false);
                            showServerErrorToast();
                            notifyUpdate();
                            return;
                        }

                        mSkip += 25;

                        Log.i(TAG, "onResponse:" + activities.length());

                        //remove the progress bar
                        if (!mOldUpdates.isEmpty()) { //add progress bar to end
                            mOldUpdates.remove(mOldUpdates.size() - 1);
                        } else if (!mRecentUpdates.isEmpty()) //old was empty but new wasn't
                            mRecentUpdates.remove(mRecentUpdates.size() - 1);

                        //iterate through array of activities
                        for (int i = 0; i < activities.length(); i++) {
                            Update update = new Update(activities.getJSONObject(i));
                            if (update.isRead()) { //TODO: use contains to check repeat?
                                mOldUpdates.add(update); //if read, it's old
                            } else {
                                mRecentUpdates.add(update); //else recent
                            }
                        }

                        mCanLoadMore = activities.length() == 25;

                        if (mCanLoadMore) {
                            if (!mOldUpdates.isEmpty()) { //add progress bar to end
                                mOldUpdates.add(null);
                            } else if (!mRecentUpdates.isEmpty()) //old was empty but new wasn't
                                mRecentUpdates.add(null);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast();
                        mUpdatesAdapter.setAutoLoadMore(false);
                    }
                } else {
                    showServerErrorToast();//TODO reloadbutton
                    Log.e(TAG, "onResponse: " + response.body().string());
                    mUpdatesAdapter.setAutoLoadMore(false);
                }

                notifyUpdate();
            }
        });
    }*/
/*
    private void notifyUpdate() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpdatesAdapter.notifyDataSetChanged();
            }
        });
    }*/


    private void showServerErrorToast() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Utils.showServerErrorToast(getContext());
            }
        });
    }

    private void showBadConnectiontToast() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showBadConnectionToast(getContext());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}
