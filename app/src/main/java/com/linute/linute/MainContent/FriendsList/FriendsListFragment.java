package com.linute.linute.MainContent.FriendsList;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKFriends;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 2/6/16.
 */
public class FriendsListFragment extends UpdatableFragment {

    public static final String TAG = FriendsListFragment.class.getSimpleName();

    private static final String FOLLOWING_OR_FOLLOWER_KEY = "following_or_follower";
    private static final String USER_ID_KEY = "user_id";

    private List<Friend> mFriendList = new ArrayList<>();

    private FriendsListAdapter mFriendsListAdapter;

    private boolean mFollowing;
    private String mUserId;

    private TextView mEmptyView;

    private View mProgressBar;

    public FriendsListFragment() {

    }


    public static FriendsListFragment newInstance(boolean following, String userId) {
        FriendsListFragment friendsListFragment = new FriendsListFragment();
        Bundle b = new Bundle();
        b.putBoolean(FOLLOWING_OR_FOLLOWER_KEY, following);
        b.putString(USER_ID_KEY, userId);
        friendsListFragment.setArguments(b);
        return friendsListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFollowing = getArguments().getBoolean(FOLLOWING_OR_FOLLOWER_KEY);
            mUserId = getArguments().getString(USER_ID_KEY);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        mProgressBar = rootView.findViewById(R.id.progress_bar);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.friendsList_recycler_view);
        mEmptyView = (TextView) rootView.findViewById(R.id.friendsList_no_res);

        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmptyView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                getFriendsList();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mFriendsListAdapter = new FriendsListAdapter(mFriendList, getActivity(), mFollowing);
        mFriendsListAdapter.setOnLoadMoreListener(new FriendsListAdapter.OnLoadMoreListener() {
            @Override
            public void loadMore() {
                FriendsListFragment.this.loadMore();
            }
        });

        recyclerView.setAdapter(mFriendsListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));


        if (fragmentNeedsUpdating()) {
            getFriendsList();
            setFragmentNeedUpdating(false);
        }

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.setTitle(mFollowing ? "Following" : "Followers");
            activity.resetToolbar();

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", activity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID",""));
                obj.put("action", "active");
                obj.put("screen", "Followers");
                activity.emitSocket(API_Methods.VERSION+":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null){
            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", activity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID",""));
                obj.put("action", "inactive");
                obj.put("screen", "Followers");
                activity.emitSocket(API_Methods.VERSION+":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getFriendsList() {
        if (getActivity() == null) return;
        new LSDKFriends(getActivity()).getFriends(mUserId, mFollowing, "0", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mProgressBar.setVisibility(View.GONE);
                            mEmptyView.setText("Tap to reload");
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject actions = new JSONObject(response.body().string());
                        final JSONArray friendsList = actions.getJSONArray("activities");

                        for (int i = 0; i < friendsList.length(); i++) {
                            mFriendList.add(new Friend(friendsList.getJSONObject(i)));
                        }

                        mSkip += 25;


                        if (friendsList.length() < 25) mCanLoadMore = false;
                        else mFriendList.add(null); //add progressbar


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    mFriendsListAdapter.notifyDataSetChanged();
                                    if (mFriendList.isEmpty()){
                                        mEmptyView.setText("No results found");
                                        mEmptyView.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                Utils.showServerErrorToast(getActivity());
                                mEmptyView.setText("Tap to reload");
                                mEmptyView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }

        });
    }

    private int mSkip = 0;
    private boolean mCanLoadMore = true;

    private void loadMore() {
        if (!mCanLoadMore && getActivity() == null) return;
        new LSDKFriends(getActivity()).getFriends(mUserId, mFollowing, mSkip + "", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mFriendsListAdapter.setAutoLoad(false);
                            mFriendsListAdapter.notifyItemChanged(mFriendsListAdapter.getItemCount() - 1);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
                        Log.i(TAG, "onResponse: "+res);
                        JSONObject actions = new JSONObject(res);
                        JSONArray friendsList = actions.getJSONArray("activities");

                        mFriendList.remove(mFriendList.size() - 1);

                        for (int i = 0; i < friendsList.length(); i++) {
                            mFriendList.add(new Friend(friendsList.getJSONObject(i)));
                        }

                        Log.i(TAG, "onResponse: skip  " + mSkip);

                        mSkip += 25;

                        Log.i(TAG, "onResponse: length returned  " + friendsList.length());

                        if (friendsList.length() < 25) mCanLoadMore = false;
                        else mFriendList.add(null); //add progressbar


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mFriendsListAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                    mFriendsListAdapter.setAutoLoad(false);
                                    mFriendsListAdapter.notifyItemChanged(mFriendsListAdapter.getItemCount() - 1);
                                }
                            });

                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mFriendsListAdapter.setAutoLoad(false);
                                mFriendsListAdapter.notifyItemChanged(mFriendsListAdapter.getItemCount() - 1);
                            }
                        });
                    }
                }
            }
        });
    }
}
