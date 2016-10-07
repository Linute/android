package com.linute.linute.MainContent.ProfileFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.SendTo.SendToFragment;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedAdapter;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
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

public class Profile extends BaseFragment implements BaseFeedAdapter.PostAction {
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

    private AlertDialog mAlertDialog;

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

    private String mUserid;
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
        mUserid = mSharedPreferences.getString("userID", null);

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
        mProfileAdapter.setPostAction(this);

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
        new LSDKUser(getActivity()).getProfileInfo(mUserid, new Callback() {
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

        if (mUserid == null) return;

        HashMap<String, Object> params = new HashMap<>();
        params.put("owner", mUserid);
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


                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(rServerErrorAction);
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

        HashMap<String, Object> params = new HashMap<>();
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
    public void onStop() {
        super.onStop();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
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

    @Override
    public void clickedOptions(final Post p, final int position) {
        if (getContext() == null || mUserid == null || mSwipeRefreshLayout.isRefreshing()) return;

        final boolean isOwner = p.getUserId().equals(mUserid);
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
                                else confirmToggleHidden(p, position);
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
        if (getActivity() == null || !mUserid.equals(p.getUserId())) return;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Deleting", true, false);

        new LSDKEvents(getActivity()).deleteEvent(p.getPostId(), new Callback() {
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
        new LSDKEvents(getActivity()).reportEvent(reason, p.getPostId(), new Callback() {
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
        if (getActivity() == null || !mUserid.equals(p.getUserId())) return;
        final boolean isAnon = p.getPrivacy() == 1;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, isAnon ? "Revealing post..." : "Making post anonymous...", true, false);
        new LSDKEvents(getActivity()).revealEvent(p.getPostId(), !isAnon, new Callback() {
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


    private void confirmToggleHidden(final Post p, final int pos) {
        if (getActivity() == null) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(p.isPostHidden() ? "Unhide post" : "Hide it")
                .setMessage(p.isPostHidden() ? "This will make this post viewable on your feed. Still want to go ahead with it?" : "This will remove this post from your feed, go ahead with it?")
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleHidden(p, pos);
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

    private void toggleHidden(Post p, int position) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;

        if (!TaptSocket.getInstance().socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        Toast.makeText(activity,
                p.isPostHidden() ? "Post unhidden" : "Post hidden",
                Toast.LENGTH_SHORT).show();

        activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);

        JSONObject emit = new JSONObject();
        try {
            emit.put("hide", !p.isPostHidden());
            emit.put("room", p.getPostId());
            TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:hide", emit);
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }

    private void sharePost(Post p) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null)
            activity.addFragmentOnTop(SendToFragment.newInstance(p.getPostId()), "send_to");
    }
}
