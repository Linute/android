package com.linute.linute.MainContent.TaptUser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.DiscoverFragment.BlockedUsersSingleton;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.ProfileFragment.ProfileAdapter;
import com.linute.linute.MainContent.SendTo.SendToFragment;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 1/9/16.
 */
public class TaptUserProfileFragment extends BaseFragment implements ProfileAdapter.OnClickFollow, BaseFeedAdapter.PostAction {
    public static final String TAG = TaptUserProfileFragment.class.getSimpleName();

    private ProfileAdapter mProfileAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Post> mPosts = new ArrayList<>();

    private LinuteUser mLinuteUser = new LinuteUser();
    private String mUserName;
    private String mTaptUserId;
    private String mViewerId;



    private Toolbar mToolbar;

    private AlertDialog mAlertDialog;

    private boolean mOtherSectionUpdated = false;

    private int mSkip;
    private boolean mCanLoadMore = false;

    private boolean mOwnerIsViewer; //viewer viewing own profile
    private Handler mHandler = new Handler();

    //private boolean mUserNameVisible = false;

    public TaptUserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * @param userName   - full name of user. this is used to set the actionbar
     * @param taptUserId - id of the user
     * @return
     */

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

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final RecyclerView recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recList.setLayoutManager(llm);

        mLinuteUser.setUserID(mTaptUserId);
        mLinuteUser.setFirstName(mUserName);
        mLinuteUser.setLastName("");

        if (mProfileAdapter == null) {
            mProfileAdapter = new ProfileAdapter(mPosts, mLinuteUser, getActivity());
            mProfileAdapter.setTitleTextListener(new ProfileAdapter.TitleTextListener() {
                @Override
                protected void showTitle(boolean show) {
                    mToolbar.setTitle(show ? mUserName : "");
                }
            });
        }

        mProfileAdapter.setRequestManager(Glide.with(this));

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
        mSwipeRefreshLayout.setProgressViewOffset(false, -200, 200);

        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        mViewerId = sharedPreferences.getString("userID", "");
        mOwnerIsViewer = mTaptUserId.equals(mViewerId);
        mToolbar.inflateMenu(mOwnerIsViewer ? R.menu.my_profile_action_bar : R.menu.tapt_user_profile_menu);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                MainActivity activity = (MainActivity) getActivity();
                switch (item.getItemId()) {

                    case R.id.more_options:
                        if (mProfileInfoHasLoaded && activity != null) {

                            String[] options = new String[]{
                                    mLinuteUser.isBlocked() ? "Unblock user" : "Block user",
                                    "Report",
                                    mLinuteUser.isSubscribed() ? "Stop post notifications" : "Get post notifications"
                            };

                            mAlertDialog = new AlertDialog
                                    .Builder(getActivity())
                                    .setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0:
                                                    blockConfirmation();
                                                    return;
                                                case 1:
                                                    reportUserConfirmation();
                                                    return;
                                                default:
                                                    subscribeConfirmation();
                                            }
                                        }
                                    })
                                    .show();

                        }
                        return true;
                    case R.id.settings:
                        if (activity != null)
                            activity.startEditProfileActivity(SettingActivity.class);
                        return true;
                    case R.id.menu_updates:
                        if (activity != null) {
                            activity.addFragmentToContainer(new UpdatesFragment());
                        }
                        return true;

                }

                return false;
            }
        });

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recList.scrollToPosition(0);
            }
        });
        if (mProfileAdapter.titleShown()) mToolbar.setTitle(mUserName);

        mProfileAdapter.setOnClickFollow(this);
        mProfileAdapter.setPostAction(this);

        recList.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (llm.findFirstVisibleItemPosition() == 0) {
                            View view = recyclerView.getChildAt(0);
                            if (view != null) {
                                //doing the maths
                                int alpha = (int) ((1 - (((float) (view.getBottom() - mToolbar.getHeight())) / (view.getHeight() - mToolbar.getHeight()))) * 255);
                                if (alpha >= 255) {
                                    alpha = 255;
                                } else {
                                    if (alpha <= 0) alpha = 0;
                                }
                                mToolbar.getBackground().mutate().setAlpha(alpha);
                            }
                        }
                    }
                }
        );

        mProfileAdapter.setLoadMorePosts(
                new LoadMoreViewHolder.OnLoadMore() {
                    @Override
                    public void loadMore() {
                        if (mCanLoadMore && !mSwipeRefreshLayout.isRefreshing() && !mLoadingMore) {
                            getMoreActivities();
                        }
                    }
                }
        );


        return rootView;
    }

    private boolean mLoadingMore = false;
    private boolean mProfileInfoHasLoaded = false;

    @Override
    public void onResume() {
        super.onResume();

        //if first time creating this fragment
        //won't be loaded again is user gets here using onBack
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            mOtherSectionUpdated = false;

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            updateAndSetHeader();
            setActivities(); //get activities
            setFragmentState(FragmentState.FINISHED_UPDATING);
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAlertDialog != null){
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    //get user information from server

    public void updateAndSetHeader() {
        if (getActivity() == null) return;

        new LSDKUser(getActivity()).getProfileInfo(mTaptUserId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //attempt to update view with response
                    final String body = response.body().string();
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(body);
                        //Log.i(TAG, "onResponse: " + jsonObject.toString(4));
                        mLinuteUser.updateUserInformation(jsonObject); //container for new information

                        mProfileInfoHasLoaded = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Log.d(TAG, "onResponse: setting header -- " + jsonObject);

//                    Log.d(TAG, body);
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mUserName = (mLinuteUser.getFirstName() + " " + mLinuteUser.getLastName());

                            if (!mOtherSectionUpdated) {
                                mOtherSectionUpdated = true;
                            } else {
                                mOtherSectionUpdated = false;
                                mSwipeRefreshLayout.setRefreshing(false);
                                mHandler.removeCallbacksAndMessages(null);
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProfileAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                } else {//else something went
                    Log.v(TAG, response.code() + response.body().string());
                }
            }
        });
    }

    public void setActivities() {
        if (getContext() == null) return;

        HashMap<String, Object> params = new HashMap<>();
        params.put("owner", mTaptUserId);
        params.put("limit", "20");

        new LSDKEvents(getContext()).getEvents("profile", params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showBadConnectionToast(getContext());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        JSONObject bodyJson = new JSONObject(response.body().string());

                        mSkip = bodyJson.getInt("skip");
                        ArrayList<Post> tempPosts = getPosts(bodyJson.getJSONArray("events"));

                        mCanLoadMore = mSkip > 0;
                        mProfileAdapter.setLoadState(mCanLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);

                        if (tempPosts.isEmpty())
                            tempPosts.add(null); //for empty view

                        if (getActivity() == null) return;
                        mSkip -= 24;

                        mPosts.clear();
                        mPosts.addAll(tempPosts);

                        if (!mOtherSectionUpdated) {
                            mOtherSectionUpdated = true;
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() { //update view
                                    mOtherSectionUpdated = false;

                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProfileAdapter.notifyDataSetChanged();

                                        }
                                    });

                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
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

                } else {
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


    private void subscribeConfirmation() {
        if (getActivity() == null || mOwnerIsViewer || !mProfileInfoHasLoaded) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mLinuteUser.isSubscribed() ? "Unsubscribe" : "Subscribe")
                .setMessage(mLinuteUser.isSubscribed() ? "Disabling this removes you from future updates when " + mLinuteUser.getFirstName() + " posts something"
                        : "Enabling this gives you updates when " + mLinuteUser.getFirstName() + " posts something")
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleSubscribe();
                    }
                })
                .setNegativeButton("no, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void toggleSubscribe() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity == null || mOwnerIsViewer) return;

        if (!Utils.isNetworkAvailable(activity) || !TaptSocket.getInstance().socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isSubscribed = mLinuteUser.isSubscribed();
        mLinuteUser.setSubscribed(!isSubscribed);

        JSONObject emit = new JSONObject();
        try {
            emit.put("subscribe", !isSubscribed);
            emit.put("user", mTaptUserId);
            TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:subscribe", emit);
            Toast.makeText(activity,
                    isSubscribed ? "Unsubscribed from user" : "Subscribed to user",
                    Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }

    private void reportUserConfirmation() {
        if (!mProfileInfoHasLoaded || getActivity() == null) return;

        String[] reasons = new String[]{
                "Spam",
                "Inappropriate",
                "Harassment",
                "Suspected parent",
                "Suspected professor"
        };

        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Report as")
                .setItems(reasons, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reportUser(which);
                    }
                })
                .show();
    }


    private void reportUser(int reason) {
        if (getActivity() != null) {
            new LSDKUser(getActivity()).reportUser(reason, mLinuteUser.getUserID(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        response.body().close();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "User has been reported", Toast.LENGTH_SHORT).show();
                                }
                            });

                    } else {
                        Log.i(TAG, "onResponse: " + response.body().string());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                            }
                        });
                    }

                }
            });
        }

    }

    private void blockConfirmation() {
        if (getActivity() == null || mOwnerIsViewer || !mProfileInfoHasLoaded) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mLinuteUser.isBlocked() ? "Unblock user?" : "Block user?")
                .setMessage(mLinuteUser.isBlocked() ? R.string.unblock_user : R.string.block_user_android)
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleBlock();
                    }
                })
                .setNegativeButton("no, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void toggleBlock() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity == null || mOwnerIsViewer) return;

        if (!Utils.isNetworkAvailable(activity) || !TaptSocket.getInstance().socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        JSONObject emit = new JSONObject();
        try {
            emit.put("block", !mLinuteUser.isBlocked());
            emit.put("user", mTaptUserId);
            TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:block:real", emit);

            String message;
            if (mLinuteUser.isBlocked()) {
                message = "You will now see this user, and they will see you";
                BlockedUsersSingleton.getBlockedListSingletion().remove(mLinuteUser.getUserID());
            } else {
                message = "You will no longer see this user and they won't be able to see you";
                BlockedUsersSingleton.getBlockedListSingletion().add(mLinuteUser.getUserID());
            }

            ((MainActivity) getActivity()).setFragmentOfIndexNeedsUpdating(
                    FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);

            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();

        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProfileAdapter.getRequestManager() != null)
            mProfileAdapter.getRequestManager().onDestroy();

        mProfileAdapter.setRequestManager(null);
    }

    public void getMoreActivities() {

        if (getActivity() == null) return;

        mLoadingMore = true;
        int limit = 20;
        int skip = mSkip;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        HashMap<String, Object> params = new HashMap<>();
        params.put("owner", mTaptUserId);
        params.put("skip", skip1 + "");
        params.put("limit", limit + "");

        new LSDKEvents(getContext()).getEvents("profile", params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLoadingMore = false;
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getContext());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        final ArrayList<Post> tempPosts = getPosts(new JSONObject(response.body().string()).getJSONArray("events"));
                        mCanLoadMore = skip1 > 0;

                        if (!mCanLoadMore) {
                            mProfileAdapter.setLoadState(LoadMoreViewHolder.STATE_END);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() { //update view
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSkip -= 24; //skip 24 posts
                                            mPosts.addAll(tempPosts);
                                            mProfileAdapter.notifyDataSetChanged();
                                        }
                                    });
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
                                }
                            });
                        }
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }

                mLoadingMore = false;
            }
        });
    }

    private ArrayList<Post> getPosts(JSONArray posts) {
        ArrayList<Post> tempPosts = new ArrayList<>();

        for (int i = posts.length() - 1; i >= 0; i--) {
            try {
                tempPosts.add(new Post(posts.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return tempPosts;
    }

    @Override
    public void followUser(final TextView followingText, final LinuteUser user, boolean follow) {
        if (getContext() == null) return;

        if (follow) followUser(followingText, user);
        else {
            mAlertDialog = new AlertDialog.Builder(getContext()).setTitle("Unfollow")
                    .setMessage("Unfollow " + user.getFirstName() + " " + user.getLastName() + "?")
                    .setPositiveButton("Unfollow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getContext() == null) return;
                            unFollowUser(followingText, user);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

        }
    }

    private void followUser(final TextView followingText, final LinuteUser user) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("user", user.getUserID());
        followingText.setText("loading");
        new LSDKPeople(getContext()).postFollow(postData, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final BaseTaptActivity activity = (BaseTaptActivity) getContext();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(activity);
                            followingText.setText("follow");
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final BaseTaptActivity activity = (BaseTaptActivity) getContext();

                if (activity == null) return;

                if (!response.isSuccessful()) {
                    Log.d(TAG, response.body().string());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followingText.setText("follow");
                        }
                    });
                    return;
                }

                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followingText.setText("following");
                            try {
                                user.setFriendship(jsonObject.getString("id"));
                                user.setFriend("NotEmpty");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followingText.setText("follow");
                            Utils.showServerErrorToast(activity);
                        }
                    });
                }
            }
        });
    }


    private void unFollowUser(final TextView followingText, final LinuteUser user) {
        Map<String, Object> putData = new HashMap<>();
        putData.put("isDeleted", true);
        followingText.setText("loading");

        new LSDKPeople(getContext()).putUnfollow(putData, user.getFriendship(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final BaseTaptActivity activity = (BaseTaptActivity) getContext();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followingText.setText("following");
                            Utils.showBadConnectionToast(activity);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final BaseTaptActivity activity1 = (BaseTaptActivity) getContext();

                if (!response.isSuccessful()) {
                    Log.d(TAG, response.body().string());
                    if (activity1 != null) {
                        activity1.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(activity1);
                                followingText.setText("following");
                            }
                        });
                    }
                } else {
                    response.body().close();

                    if (activity1 == null) return;

                    activity1.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followingText.setText("follow");
                            user.setFriend("");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void clickedOptions(final Post p, final int position) {
        if (getContext() == null || mViewerId == null || mSwipeRefreshLayout.isRefreshing()) return;

        final boolean isOwner = p.getUserId().equals(mViewerId);
        String[] options;
        if (isOwner) {
            options = new String[]{"Delete post", p.getPrivacy() == 1 ? "Reveal identity" : "Make anonymous", "Share post"};
        } else {
            options = new String[]{"Report post", p.isPostHidden() ? "Unhide post" : "Hide post", "Share post"};
        }
        mAlertDialog = new AlertDialog.Builder(getContext())
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (isOwner) confirmDeletePost(p, position);
                                else confirmReportPost(p);
                                return;
                            case 1:
                                if (isOwner) confirmToggleAnon(p, position);
                                else confirmToggleHidden(p);
                                return;
                            case 2:
                                sharePost(p);
                        }
                    }
                }).show();
    }

    private void confirmDeletePost(final Post p, final int position) {
        if (getContext() == null) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Delete your post")
                .setMessage("Are you sure you want to delete what you've created?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost(p, position);
                    }
                })
                .show();
    }


    private void deletePost(final Post p, final int pos) {
        if (getActivity() == null || !mViewerId.equals(p.getUserId())) return;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Deleting", true, false);

        new LSDKEvents(getActivity()).deleteEvent(p.getId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();

                    final BaseTaptActivity activity = (BaseTaptActivity) getActivity();

                    if (activity == null) return;
                    activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);
                    activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.PROFILE);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Post deleted", Toast.LENGTH_SHORT).show();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mPosts.get(pos - 2).equals(p)) { //check this is correct post
                                        int position = mPosts.indexOf(p);
                                        if (position >= 0) {
                                            mPosts.remove(position);
                                            mProfileAdapter.notifyItemRemoved(position + 2);
                                        }
                                    } else {
                                        mPosts.remove(pos - 2);
                                        mProfileAdapter.notifyItemRemoved(pos);
                                    }
                                }
                            });
                        }
                    });

                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }
        });
    }


    private void confirmReportPost(final Post p) {
        if (getActivity() == null) return;
        final CharSequence options[] = new CharSequence[]{"Spam", "Inappropriate", "Harassment"};
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Report As")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reportPost(p, which);
                    }
                })
                .create();
        mAlertDialog.show();
    }

    private void reportPost(Post p, int reason) {
        if (getActivity() == null) return;
        new LSDKEvents(getActivity()).reportEvent(reason, p.getId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Post reported", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
            }
        });
    }


    private void confirmToggleAnon(final Post p, final int pos) {
        if (getActivity() == null) return;

        boolean isAnon = p.getPrivacy() == 1;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(isAnon ? "Reveal" : "Wear a mask")
                .setMessage(isAnon ? "Are you sure you want to turn anonymous off for this post?" : "Are you sure you want to make this post anonymous?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleAnon(p, pos);
                    }
                })
                .show();
    }


    private void toggleAnon(final Post p, final int position) {
        if (getActivity() == null || !mViewerId.equals(p.getUserId())) return;
        final boolean isAnon = p.getPrivacy() == 1;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, isAnon ? "Revealing post..." : "Making post anonymous...", true, false);
        new LSDKEvents(getActivity()).revealEvent(p.getId(), !isAnon, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();

                if (response.isSuccessful()) {
                    try {

                        p.setPrivacyChanged(true);

                        if (!isAnon) {
                            JSONObject obj = new JSONObject(res);
                            p.setAnonImage(Utils.getAnonImageUrl(obj.getString("anonymousImage")));
                        }

                        BaseTaptActivity act = (BaseTaptActivity) getActivity();

                        if (act != null) {
                            act.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);
                            act.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.PROFILE);

                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    p.setPostPrivacy(isAnon ? 0 : 1);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (!mPosts.get(position - 2).equals(p)) {
                                                int pos = mPosts.indexOf(p);
                                                if (pos >= 0) {
                                                    mProfileAdapter.notifyItemChanged(pos + 2);
                                                }
                                            } else {
                                                mProfileAdapter.notifyItemChanged(position);
                                            }
                                        }

                                    });
                                    Toast.makeText(getActivity(), isAnon ? "Post revealed" : "Post made anonymous", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponse: " + res);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: " + res);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }
        });
    }


    private void confirmToggleHidden(final Post p) {
        if (getActivity() == null) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(p.isPostHidden() ? "Unhide post" : "Hide it")
                .setMessage(p.isPostHidden() ? "This will make this post viewable on your feed. Still want to go ahead with it?" : "This will remove this post from your feed, go ahead with it?")
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleHidden(p);
                    }
                })
                .setNegativeButton("no, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void toggleHidden(Post p) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;

        if (!TaptSocket.getInstance().socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        p.setPostHidden(!p.isPostHidden());
        Toast.makeText(activity,
                p.isPostHidden() ?  "Post hidden on feed" : "Post unhidden on feed",
                Toast.LENGTH_SHORT).show();

        activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);

        JSONObject emit = new JSONObject();
        try {
            emit.put("hide", p.isPostHidden());
            emit.put("room", p.getId());
            TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:hide", emit);
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }

    private void sharePost(Post p) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null)
            activity.addFragmentOnTop(SendToFragment.newInstance(p.getId()), "send_to");
    }
}
