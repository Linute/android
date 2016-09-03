package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class Profile extends BaseFragment {
    public static final String TAG = Profile.class.getSimpleName();

    private ProfileAdapter mProfileAdapter;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView vRecList;

    private View vUpdateNotification;
    private TextView vUpdatesCounter;

    private ArrayList<Post> mPosts = new ArrayList<>();
    private SharedPreferences mSharedPreferences;

    //we have 2 seperate queries, one for header and one for activities
    //we call notify only after
    private boolean mOtherComponentHasUpdated = false;

    //handler that makes sure notify adapters are called in sequential order
    //causes crashes if else
    private Handler mHandler = new Handler();

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

    private int mSkip = 0;
    private boolean mCanLoadMore = false;
    //private boolean mTitleIsVisible = false;

    public Profile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        vRecList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        vRecList.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        vRecList.setLayoutManager(llm);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        if (user == null || mProfileAdapter == null) {
            //get data from sharedpref
            user = LinuteUser.getDefaultUser(getContext());
            mProfileAdapter = new ProfileAdapter(mPosts, user, getContext());
            mProfileAdapter.setTitleTextListener(new ProfileAdapter.TitleTextListener() {
                @Override
                protected void showTitle(boolean show) {
                    mToolbar.setTitle(show ? user.getFirstName() + " " + user.getLastName() : "");
                }
            });
        }

        vRecList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOtherComponentHasUpdated = false;
                updateAndSetHeader();
                setActivities(); //get activities
            }
        });

        mProfileAdapter.setLoadMorePosts(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (mCanLoadMore && !mSwipeRefreshLayout.isRefreshing() && !mLoadingMore) {
                    getMoreActivities();
                }
            }
        });

        mProfileAdapter.setRequestManager(Glide.with(this));

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vRecList.scrollToPosition(0);
            }
        });
        if (mProfileAdapter.titleShown())
            mToolbar.setTitle(user.getFirstName() + " " + user.getLastName());

        mToolbar.setNavigationIcon(NotificationsCounterSingleton.getInstance().hasNewPosts() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((MainActivity) getActivity()).openDrawer();
            }
        });

        mToolbar.inflateMenu(R.menu.my_profile_action_bar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity == null) return false;
                switch (item.getItemId()) {
                    case R.id.settings:
                        activity.startEditProfileActivity(SettingActivity.class);
                        return true;
                }
                return false;
            }
        });

        View update = mToolbar.getMenu().findItem(R.id.menu_updates).getActionView();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addActivityFragment();
            }
        });
        vUpdateNotification = update.findViewById(R.id.notification);
        vUpdatesCounter = (TextView) vUpdateNotification.findViewById(R.id.notification_count);
        int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
        vUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        vUpdatesCounter.setText(count < 100 ? count + "" : "+");

        //lower the swipe refresh
        mSwipeRefreshLayout.setProgressViewOffset(false, -200, 200);

        //when user scrolls down, change alpha of actionbar
        vRecList.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (llm.findFirstVisibleItemPosition() == 0) {
                            View view = recyclerView.getChildAt(0);
                            if (view != null) {
                                int alpha = (int) (((1 - (((float) (view.getBottom() - mToolbar.getHeight())) / (view.getHeight() - mToolbar.getHeight())))) * 255);
                                //255 is max
                                //unpredictable actions if it's over 255
                                if (alpha >= 255) {
                                    alpha = 255;
                                } else {
                                    //unpredicatable if less than 0
                                    if (alpha < 0) alpha = 0;
                                }

                                mToolbar.getBackground().mutate().setAlpha(alpha);
                            }
                        }
                    }
                }
        );
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //the menu icon badge
        mNotificationSubscription = NotificationEventBus
                .getInstance()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNotificationEventAction1);

        //only update this fragment when it is first created or set to reupdate from outside
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            mOtherComponentHasUpdated = false;
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });

            updateAndSetHeader(); //get information from server to update profile
            setActivities();

            //a bit trickier because we have 2 things we need updated
            setFragmentState(FragmentState.FINISHED_UPDATING);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }

        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProfileAdapter.getRequestManager() != null)
            mProfileAdapter.getRequestManager().onDestroy();

        mProfileAdapter.setRequestManager(null);
    }

    //get user information from server
    public void updateAndSetHeader() {
        if (getActivity() == null) return;
        new LSDKUser(getActivity()).getProfileInfo(mSharedPreferences.getString("userID", null), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(rFailedConnectionAction);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //attempt to update view with response
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        user.updateUserInformation(jsonObject); //model for new information
                        savePreferences(user);

                        if (getActivity() == null) return;


                        //set activities and update header are different calls
                        //when we refresh, we need both to finish loading before we remove the loading indicator
                        if (!mOtherComponentHasUpdated) {
                            mOtherComponentHasUpdated = true;
                        } else {
                            mOtherComponentHasUpdated = false;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //remove all callbacks
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
                    }
                } else {//else something went
                    Log.v(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(rServerErrorAction);
                    }
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
        if (getContext() == null) return;

        String owner = mSharedPreferences.getString("userID", null);
        if (owner == null) return;

        HashMap<String, String> params = new HashMap<>();
        params.put("owner", owner);
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

                        if (!mOtherComponentHasUpdated) {
                            mOtherComponentHasUpdated = true;
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() { //update view
                                    mOtherComponentHasUpdated = false;

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


                    }catch (JSONException e){
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(rServerErrorAction);
                        }
                    }

                }else {
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


    private boolean mLoadingMore = false;

    public void getMoreActivities() {

        if (getActivity() == null) return;

        String owner = mSharedPreferences.getString("userID", null);
        if (owner == null) return;

        mLoadingMore = true;
        int limit = 20;
        int skip = mSkip;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        HashMap<String, String> params = new HashMap<>();
        params.put("owner", owner);
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
                                            int size = mPosts.size() + 2;
                                            mPosts.addAll(tempPosts);
                                            mProfileAdapter.notifyItemRangeInserted(size, tempPosts.size());
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

    private ArrayList<Post> getPosts(JSONArray posts){
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
    public void resetFragment() {
        vRecList.scrollToPosition(0);
    }

    private Subscription mNotificationSubscription;

    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.getType() == NotificationEvent.ACTIVITY) {
                int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
                vUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                vUpdatesCounter.setText(count < 100 ? count + "" : "+");
            } else if (notificationEvent.getType() == NotificationEvent.DISCOVER) {
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
            }
        }
    };
}
