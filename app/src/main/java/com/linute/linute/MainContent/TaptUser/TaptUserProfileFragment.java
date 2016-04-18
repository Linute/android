package com.linute.linute.MainContent.TaptUser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.ProfileFragment.EmptyUserActivityItem;
import com.linute.linute.MainContent.ProfileFragment.ProfileAdapter;
import com.linute.linute.MainContent.ProfileFragment.UserActivityItem;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 1/9/16.
 */
public class TaptUserProfileFragment extends UpdatableFragment {
    public static final String TAG = TaptUserProfileFragment.class.getSimpleName();

    private ProfileAdapter mProfileAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;

    private LinuteUser mLinuteUser = new LinuteUser();
    private String mUserName;
    private String mTaptUserId;

    private Toolbar mToolbar;

    private AlertDialog mDialog;

    private boolean mOtherSectionUpdated = false;

    private int mSkip;
    private boolean mCanLoadMore = false;

    private boolean mOwnerIsViewer; //viewer viewing own profile

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

        mUser = new LSDKUser(getActivity());
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        final RecyclerView recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);

        final GridLayoutManager llm = new GridLayoutManager(getActivity(), 3);
        llm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0 || position == 1) return 3;
                else if (position == 2 && mUserActivityItems.get(0) instanceof EmptyUserActivityItem)
                    return 3;
                else return 1;
            }
        });

        recList.setLayoutManager(llm);

        mLinuteUser.setUserID(mTaptUserId);
        mLinuteUser.setFirstName(mUserName);
        mLinuteUser.setLastName("");
        mProfileAdapter = new ProfileAdapter(mUserActivityItems, mLinuteUser, getActivity());
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

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        mOwnerIsViewer = mTaptUserId.equals(mSharedPreferences.getString("userID", ""));
        mToolbar.inflateMenu(mOwnerIsViewer ? R.menu.my_profile_action_bar : R.menu.tapt_user_profile_menu);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                MainActivity activity = (MainActivity) getActivity();
                switch (item.getItemId()) {

                    case R.id.more_options:
                        if (mProfileInfoHasLoaded && activity != null) {

                            String[] options = new String[]{
                                    mLinuteUser.isBlocked() ? "Unblock user" : "Block User",
                                    "Report",
                                    mLinuteUser.isSubscribed() ? "Unsubscribe from user" : "Subscribe to user"
                            };

                            mDialog = new AlertDialog
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
                    case R.id.add_friend:
                        if (activity != null)
                            activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                        return true;
                    case R.id.settings:
                        if (activity != null)
                            activity.startEditProfileActivity(SettingActivity.class);
                        return true;

                }

                return false;
            }
        });

        mToolbar.setTitle(mUserName);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recList.scrollToPosition(0);
            }
        });
        if (fragmentNeedsUpdating()) {
            mToolbar.getBackground().mutate().setAlpha(0);
        }

        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                        @Override
                                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                            super.onScrolled(recyclerView, dx, dy);

                                            if (llm.findFirstVisibleItemPosition() == 0) {
                                                View view = recyclerView.getChildAt(0);
                                                if (view != null) {
                                                    //doing the maths
                                                    int alpha = (int) ((1 - (((float) (view.getBottom() - mToolbar.getHeight())) / (view.getHeight() - mToolbar.getHeight()))) * 255);
                                                    if (alpha > 255) {
                                                        alpha = 255;
                                                    }
                                                    if (alpha < 0) {
                                                        alpha = 0;
                                                    }
                                                    mToolbar.getBackground().mutate().setAlpha(alpha);
                                                }
                                            }
                                        }
                                    }
        );

        mProfileAdapter.setLoadMorePosts(new ProfileAdapter.LoadMorePosts() {
            @Override
            public void loadMorePosts() {
                if (mCanLoadMore && !mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    getMoreActivities();
                }
            }
        });
        return rootView;
    }

    private boolean mProfileInfoHasLoaded = false;

    @Override
    public void onResume() {
        super.onResume();

        //if first time creating this fragment
        //won't be loaded again is user gets here using onBack
        if (fragmentNeedsUpdating()) {
            mOtherSectionUpdated = false;
            mSwipeRefreshLayout.setRefreshing(true);
            updateAndSetHeader();
            setActivities(); //get activities
            setFragmentNeedUpdating(false);
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    //get user information from server
    public void updateAndSetHeader() {
        mUser.getProfileInfo(mTaptUserId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) { //attempt to update view with response
                    final String body = response.body().string();

                    //Log.i(TAG, "onResponse: "+body);

                    if (getActivity() == null) return;
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

                    if (!mOtherSectionUpdated) {
                        mOtherSectionUpdated = true;
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mToolbar.setTitle(mLinuteUser.getFirstName() + " " + mLinuteUser.getLastName());
                                mOtherSectionUpdated = false;
                                mSwipeRefreshLayout.setRefreshing(false);
                                mProfileAdapter.notifyDataSetChanged();

                            }
                        });
                    }
                } else {//else something went
                    Log.v(TAG, response.code() + response.body().string());
                }
            }
        });
    }

    public void setActivities() {
        LSDKUser user = new LSDKUser(getActivity());
        user.getUserActivities(mTaptUserId, -1, 24, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                                if (mSwipeRefreshLayout.isRefreshing())
                                    mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) { //got response
                            try { //try to grab needed information from response
                                String body = response.body().string();

                                JSONObject jsonObject = new JSONObject(body);

                                mSkip = jsonObject.getInt("skip");

                                final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response
//                        Log.d(TAG, body);

                                if (activities == null) return;

                                ArrayList<UserActivityItem> tempActivies = new ArrayList<>();

                                for (int i = activities.length() - 1; i >= 0; i--) { //add each activity into our array
                                    try {
                                        tempActivies.add(
                                                new UserActivityItem(
                                                        activities.getJSONObject(i)
                                                )); //create activity objects and add to array
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                mUserActivityItems.clear();
                                mUserActivityItems.addAll(tempActivies);

                                //can load more if skip isn't 0 yet
                                mCanLoadMore = mSkip > 0;
                                mSkip -= 24;

                                if (mUserActivityItems.isEmpty()) {
                                    mUserActivityItems.add(new EmptyUserActivityItem());
                                }

                                if (getActivity() == null) return;

                                //turn refresh off if it's on and notify ListView we might have updated information
                                if (!mOtherSectionUpdated) {
                                    mOtherSectionUpdated = true;
                                } else {
                                    mOtherSectionUpdated = false;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() { //update view
                                            mSwipeRefreshLayout.setRefreshing(false);
                                            mProfileAdapter.notifyDataSetChanged();

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
                                            mSwipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }
                            }
                        } else

                        { //unable to connect with DB
                            Log.v(TAG, response.body().string());
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                });
                            }
                        }
                    }
                }

        );
    }


    private void subscribeConfirmation() {
        if (getActivity() == null || mOwnerIsViewer || !mProfileInfoHasLoaded) return;
        mDialog = new AlertDialog.Builder(getActivity())
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

        if (!Utils.isNetworkAvailable(activity) || !activity.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isSubscribed = mLinuteUser.isSubscribed();
        mLinuteUser.setSubscribed(!isSubscribed);

        JSONObject emit = new JSONObject();
        try {
            emit.put("subscribe", !isSubscribed);
            emit.put("user", mTaptUserId);
            activity.emitSocket(API_Methods.VERSION + ":users:subscribe", emit);
            Toast.makeText(activity,
                    isSubscribed ? "Unsubscribed from " + mLinuteUser.getFirstName() : "Subscribed to " + mLinuteUser.getFirstName(),
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

        mDialog = new AlertDialog.Builder(getActivity())
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
        mDialog = new AlertDialog.Builder(getActivity())
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

        if (!Utils.isNetworkAvailable(activity) || !activity.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        JSONObject emit = new JSONObject();
        try {
            emit.put("block", !mLinuteUser.isBlocked());
            emit.put("user", mTaptUserId);
            activity.emitSocket(API_Methods.VERSION + ":users:block:real", emit);
            Toast.makeText(activity,
                    mLinuteUser.isBlocked() ? "You will now see this user, and they will see you"
                            : "You will no longer see this user and they won't be able to see you",
                    Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();

        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }


    public void getMoreActivities() {

        int limit = 24;

        if (mSkip < 0) {
            limit += mSkip;
            mSkip = 0;
        }

        new LSDKUser(getContext()).getUserActivities(mTaptUserId, mSkip, limit, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
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

                        mUserActivityItems.addAll(userActItems);

                        //if we got 24 back, there might still be more
                        mCanLoadMore = mSkip > 0;

                        mSkip -= 24; //skip 24 posts

                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() { //update view
                                mProfileAdapter.notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                    } catch (JSONException e) { //unable to grab needed info
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
}
