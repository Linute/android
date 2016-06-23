package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.MainContent.UpdateFragment.UpdatesFragment;
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

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();
    private SharedPreferences mSharedPreferences;
    private boolean mHasNotification;

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
        final GridLayoutManager llm = new GridLayoutManager(getActivity(), 3);

        llm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //header is size 3
                //0 for image and 1 for profile info
                if (position == 0 || position == 1)
                    return 3;
                else if (position == 2 && mUserActivityItems.get(0) instanceof EmptyUserActivityItem)
                    return 3; //empty view size 3
                else if (position == mUserActivityItems.size() + 2)  // view that shows loading indicator
                    return 3;
                else return 1;
            }
        });

        vRecList.setLayoutManager(llm);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

        if (user == null || mProfileAdapter == null) {
            //get data from sharedpref
            user = LinuteUser.getDefaultUser(getContext());
            mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext());
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

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vRecList.scrollToPosition(0);
            }
        });
        if (mProfileAdapter.titleShown())
            mToolbar.setTitle(user.getFirstName() + " " + user.getLastName());


        mHasNotification = NotificationsCounterSingleton.getInstance().hasNotifications();
        mToolbar.setNavigationIcon(mHasNotification ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
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
                switch (item.getItemId()){
                    case R.id.settings:
                        if (activity != null)
                            activity.startEditProfileActivity(SettingActivity.class);
                        return true;
                    case R.id.menu_updates:
                        if (activity != null)
                            activity.addFragmentToContainer(new UpdatesFragment());
                        return true;
                }
                return false;
            }
        });

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

                                //// TODO: 5/29/16  shit that's a lot of casting. I'm sure there's a way to minimize
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

        //skip  = -1 so we don't add skip to query
        new LSDKUser(getContext()).getUserActivities(mSharedPreferences.getString("userID", null), -1, 24, new Callback() {
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

                        final JSONArray activities = bodyJson.getJSONArray("activities"); //try to get activities from response

                        if (activities == null) return;

                        ArrayList<UserActivityItem> userActItems = new ArrayList<>();

                        for (int i = activities.length() - 1; i >= 0; i--) { //add each activity into our array
                            try {
                                userActItems.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i)
                                        )); //create activity objects and add to array
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        //if skip was 0, we have reached end of db
                        mCanLoadMore = mSkip > 0;

                        mProfileAdapter.setLoadState(mCanLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);

                        mUserActivityItems.clear();
                        mUserActivityItems.addAll(userActItems);

                        if (mUserActivityItems.isEmpty()) {
                            mUserActivityItems.add(new EmptyUserActivityItem());
                        }

                        mSkip -= 24;
                        if (getActivity() == null) return;

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
                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(rServerErrorAction);
                        }
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());
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

        mLoadingMore = true;

        int limit = 24;

        int skip = mSkip;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        new LSDKUser(getContext()).getUserActivities(mSharedPreferences.getString("userID", null), skip1, limit, new Callback() {
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
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        String body = response.body().string();
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response

                        if (activities == null) return;

                        final ArrayList<UserActivityItem> userActItems = new ArrayList<>();

                        for (int i = activities.length() - 1; i >= 0; i--) { //add each activity into our array
                            try {
                                userActItems.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i)
                                        )); //create activity objects and add to array
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        //if skip isn't 0, we can still load more
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
                                            int size = mUserActivityItems.size() + 2;
                                            mUserActivityItems.addAll(userActItems);
                                            mProfileAdapter.notifyItemRangeInserted(size, userActItems.size());
                                        }
                                    });
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
                            }
                        });
                    }
                }

                mLoadingMore = false;
            }
        });
    }

    @Override
    public void resetFragment(){
        vRecList.scrollToPosition(0);
    }

    private Subscription mNotificationSubscription;

    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.hasNotification() != mHasNotification) {
                mToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
                mHasNotification = notificationEvent.hasNotification();
            }
        }
    };
}
