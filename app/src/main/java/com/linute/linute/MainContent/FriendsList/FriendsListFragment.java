package com.linute.linute.MainContent.FriendsList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.LSDKFriends;
import com.linute.linute.MainContent.EditScreen.PostOptions;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.ModesDisabled;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CameraType;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
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
public class FriendsListFragment extends BaseFragment {

    public static final String TAG = FriendsListFragment.class.getSimpleName();

    public static final String FOLLOWING_OR_FOLLOWER_KEY = "following_or_follower";
    public static final String USER_ID_KEY = "user_id";

    private List<Friend> mFriendList = new ArrayList<>();

    private FriendsListAdapter mFriendsListAdapter;

    private boolean mFollowing;
    private String mUserId;

    private ViewGroup mListEmptyLayout;
    private ViewGroup mFailLayout;

//    private View mListEmptyView;
//    private View mFailView;

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

        if (mFriendsListAdapter == null)
            mFriendsListAdapter = new FriendsListAdapter(mFriendList, getActivity(), mFollowing);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.friendsList_recycler_view);
        mListEmptyLayout = (ViewGroup) rootView.findViewById(R.id.layout_empty_list);
        mFailLayout = (ViewGroup) rootView.findViewById(R.id.layout_loading_failed);

        mFriendsListAdapter.setRequestManager(Glide.with(this));

        boolean viewIsOwner = getActivity()
                .getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getString("userID", "")
                .equals(mUserId);

        if (viewIsOwner) {
            inflater.inflate((mFollowing ? R.layout.fragment_friends_list_not_following : R.layout.fragment_friends_list_no_followers), mListEmptyLayout, true);
            mListEmptyLayout.findViewById(R.id.text_empty_action).setOnClickListener(
                    (mFollowing ?
                            //Not Following anyone
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Log.i(TAG, "Empty List Text Clicked (Not Following)");
                                    BaseTaptActivity acc = (BaseTaptActivity) getActivity();
                                    acc.addFragmentToContainer(new FindFriendsChoiceFragment());
                                }
                            }
                            :
                            //Has no Followers
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //Log.i(TAG, "Empty List Text Clicked (No Followers)");
                                    ModesDisabled disabled = ModesDisabled.getInstance();
                                    if (disabled.realPosts() && disabled.anonPosts()) {
                                        if (getActivity() != null)
                                            Toast.makeText(getActivity(), "You have been banned from posting content", Toast.LENGTH_SHORT).show();
                                    } else {
                                        BaseTaptActivity acc = (BaseTaptActivity) getActivity();
                                        Intent i = new Intent(getActivity(), CameraActivity.class);
                                        PostOptions options = new PostOptions(PostOptions.ContentType.None, PostOptions.ContentSubType.Post, null);
                                        i.putExtra(CameraActivity.EXTRA_CAMERA_TYPE, new CameraType(CameraType.CAMERA_EVERYTHING));
                                        i.putExtra(CameraActivity.EXTRA_POST_OPTIONS, options);
                                        i.putExtra(CameraActivity.EXTRA_RETURN_TYPE, CameraActivity.SEND_POST);
                                        acc.startActivityForResult(i, MainActivity.PHOTO_STATUS_POSTED);
                                    }
                                }
                            }
                    )
            );
        } else {
            inflater.inflate(R.layout.fragment_friends_list_no_results, mListEmptyLayout, true);
        }

        mFailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListEmptyLayout.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                getFriendsList();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mFriendsListAdapter.setOnLoadMoreListener(new FriendsListAdapter.OnLoadMoreListener() {
            @Override
            public void loadMore() {
                FriendsListFragment.this.loadMore();
            }
        });

        recyclerView.setAdapter(mFriendsListAdapter);

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            getFriendsList();
        } else if (getFragmentState() == FragmentState.FINISHED_UPDATING) {
            if (mFriendList.isEmpty()) {

                if (mListEmptyLayout != null)
                    mListEmptyLayout.setVisibility(View.VISIBLE);
                if (mFailLayout != null)
                    mFailLayout.setVisibility(View.GONE);
            }
        }

        return rootView;
    }


    private void getFriendsList() {
        if (getActivity() == null) return;

        setFragmentState(FragmentState.LOADING_DATA);
        mProgressBar.setVisibility(View.VISIBLE);
        new LSDKFriends(getActivity()).getFriends(mUserId, mFollowing, "0", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mProgressBar.setVisibility(View.GONE);
                            if (mListEmptyLayout != null)
                                mListEmptyLayout.setVisibility(View.GONE);
                            if (mFailLayout != null)
                                mFailLayout.setVisibility(View.VISIBLE);
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
                                    if (mFriendList.isEmpty()) {
                                        if (mListEmptyLayout != null)
                                            mListEmptyLayout.setVisibility(View.VISIBLE);
                                        if (mFailLayout != null)
                                            mFailLayout.setVisibility(View.GONE);
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
                                if (mListEmptyLayout != null)
                                    mListEmptyLayout.setVisibility(View.GONE);
                                if (mFailLayout != null)
                                    mFailLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                setFragmentState(FragmentState.FINISHED_UPDATING);
            }

        });
    }

    private int mSkip = 0;
    private boolean mCanLoadMore = true;

    private void loadMore() {
        if (!mCanLoadMore || getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA)
            return;

        setFragmentState(FragmentState.LOADING_DATA);

        new LSDKFriends(getActivity()).getFriends(mUserId, mFollowing, mSkip + "", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
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
                        JSONObject actions = new JSONObject(res);
                        JSONArray friendsList = actions.getJSONArray("activities");
                        mFriendList.remove(mFriendList.size() - 1);
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

                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFriendsListAdapter.getRequestManager() != null)
            mFriendsListAdapter.getRequestManager().onDestroy();
    }

    //    public void setEmptyView(View mEmptyView) {
//        this.mListEmptyView = mEmptyView;
//        if(mListEmptyLayout != null){
//            mListEmptyLayout.removeAllViews();
//            mListEmptyLayout.addView(mEmptyView);
//        }
//    }
//
//    public void setFailView(View mFailView) {
//        this.mFailView = mFailView;
//        if(mFailLayout != null){
//            mFailLayout.removeAllViews();
//            mFailLayout.addView(mFailView);
//        }
//    }

}
