package com.linute.linute.MainContent.UpdateFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKActivity;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

    private boolean mSafeToAddToTop = false;

    public UpdatesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);

        setHasOptionsMenu(true);

        mUpdatesRecyclerView = (RecyclerView) rootView.findViewById(R.id.updatesFragment_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.updatesFragment_swipe_refresh);

        LinearLayoutManager llm = new CustomLinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mUpdatesRecyclerView.setLayoutManager(llm);
        mUpdatesRecyclerView.setHasFixedSize(true);

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

        mHasMessages = false;

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setTitle("Updates");
            mainActivity.resetToolbar();
            mainActivity.setToolbarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUpdatesRecyclerView != null) {
                        mUpdatesRecyclerView.smoothScrollToPosition(0);
                    }
                }
            });

            mainActivity.connectSocket("unread", haveUnread);

            JSONObject object = new JSONObject();
            mainActivity.emitSocket(API_Methods.VERSION+":messages:unread", object);

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mainActivity
                        .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .getString("userID", ""));
                obj.put("action", "active");
                obj.put("screen", "Updates");
                mainActivity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        } else {
            if (mRecentUpdates.isEmpty() && mOldUpdates.isEmpty()) {
                if (mEmptyView.getVisibility() == View.GONE) {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
//            if (mainActivity != null) {
//                mainActivity.setUpdateNotification(0);
//                mainActivity.setNumNewActivities(0);
//
//            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.disconnectSocket("unread", haveUnread);

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", activity
                        .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .getString("userID", ""));
                obj.put("action", "inactive");
                obj.put("screen", "Updates");
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

    private void updateUpdatesInformation() {

        if (getActivity() == null) return;
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
            public void onFailure(Call call, IOException e) {
                showBadConnectiontToast();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
        if (getActivity() == null) return;

        new LSDKActivity(getActivity()).getActivities(0, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showBadConnectiontToast();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resString = response.body().string();
                        //Log.i(TAG, "onResponse: " + resString);

                        JSONArray activities = Update.getJsonArrayFromJson(new JSONObject(resString), "activities");

                        if (activities == null) {
                            showServerErrorToast();
                            return;
                        }

//                        mOldUpdates.clear();
//                        mRecentUpdates.clear();
                        ArrayList<Update> oldItems = new ArrayList<>();
                        ArrayList<Update> newItems = new ArrayList<>();

                        //no more information to load
                        //if (activities.length() < 25) mCanLoadMore = false;

                        //mSkip = 25;

                        Update update;
                        //iterate through array of activities
                        for (int i = 0; i < activities.length(); i++) {
                            try {
                                update = new Update(activities.getJSONObject(i));
                                if (update.isRead()) oldItems.add(update); //if read, it's old
                                else newItems.add(update); //else recent
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        if (getActivity() == null) return;

                        mOldUpdates.clear();
                        mOldUpdates.addAll(oldItems);

                        mRecentUpdates.clear();
                        mRecentUpdates.addAll(newItems);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mUpdatesAdapter.getItemCount(0) + mUpdatesAdapter.getItemCount(1) == 0) {
                                    if (mEmptyView.getVisibility() == View.GONE)
                                        mEmptyView.setVisibility(View.VISIBLE);
                                } else {
                                    if (mEmptyView.getVisibility() == View.VISIBLE)
                                        mEmptyView.setVisibility(View.GONE);
                                }

                                mUpdatesAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                                mSafeToAddToTop = true;
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


    private Emitter.Listener haveUnread = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mHasMessages = (boolean) args[0];
            if (mCreateActionMenu){
                final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                if (act != null) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            act.invalidateOptionsMenu();
                        }
                    });
                }
            }
        }
    };


    private void showServerErrorToast() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Utils.showServerErrorToast(getActivity());
            }
        });
    }

    private void showBadConnectiontToast() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showBadConnectionToast(getActivity());
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    private boolean mCreateActionMenu = false;
    private boolean mHasMessages = false;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(mHasMessages? R.menu.people_fragment_menu_noti : R.menu.people_fragment_menu, menu);
        mCreateActionMenu = true;
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (getActivity() != null) {
            switch (item.getItemId()) {
                case R.id.people_fragment_menu_chat:
                    Intent enterRooms = new Intent(getActivity(), RoomsActivity.class);
                    enterRooms.putExtra("CHATICON", true);
                    startActivity(enterRooms);
                    return true;
                case R.id.menu_find_friends:
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity != null) {
                        activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                    }
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    public void addItemToRecents(Update update) {
        if (mSafeToAddToTop && !mSwipeRefreshLayout.isRefreshing()) {
            mRecentUpdates.add(0, update);
            mUpdatesAdapter.notifyItemInserted(0);
            if (mEmptyView.getVisibility() == View.VISIBLE) {
                mEmptyView.setVisibility(View.GONE);
            }
        }
    }

}
