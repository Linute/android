package com.linute.linute.MainContent.SendTo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKFriends;
import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.MainContent.Uploading.PendingUploadPost;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
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
 * Created by QiFeng on 7/15/16.
 */
public class SendToFragment extends BaseFragment {

    public static final String TAG = SendToFragment.class.getSimpleName();
    public static final String POST_ID_KEY = "send_post_id_key";
    public static final String PENDING_POST = "show_trending";

    private ArrayList<SendToItem> mSendToItems = new ArrayList<>();
    private ArrayList<SendToItem> mTrendsItems = new ArrayList<>();

    private SendToAdapter mSendToAdapter;

    private String mPostId;
    private String mUserId;

    private Handler mHandler = new Handler();

    private View vProgress;
    private TextView vErrorText;

    //private int mSkip = 0;
    //private boolean mCanLoadMore = true;

    private Button vSendButton;

    private PendingUploadPost mPendingUploadPost;

    // we currently have to make 2 api calls : one to retrieve list of trends and one to retrieve list
    //   friends. We won't show list until both api calls have finished
    private boolean mGotResponseForApiCall = false;


    /**
     * Use this constructor!
     *
     * @param postId - the id of the post being shared
     * @return fragment
     */

    public static SendToFragment newInstance(String postId) {
        SendToFragment fragment = new SendToFragment();
        Bundle bundle = new Bundle();
        bundle.putString(POST_ID_KEY, postId);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static SendToFragment newInstance(PendingUploadPost post) {
        SendToFragment fragment = new SendToFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PENDING_POST, post);
        fragment.setArguments(bundle);
        return fragment;
    }


    /**
     * Please don't use this constructor,
     * use newInstance(postId, showTrend) instead
     */
    public SendToFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPostId = getArguments().getString(POST_ID_KEY);
            mPendingUploadPost = getArguments().getParcelable(PENDING_POST);
        }

        SharedPreferences preferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        mUserId = preferences.getString("userID", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_to, container, false);

        vProgress = root.findViewById(R.id.progress);
        vErrorText = (TextView)root.findViewById(R.id.error_text);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vSendButton = (Button) root.findViewById(R.id.send_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (mPendingUploadPost != null)
            toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);

        if (mSendToAdapter == null) {
            mSendToAdapter = new SendToAdapter(getContext(), Glide.with(this), mSendToItems);
        } else {
            mSendToAdapter.setRequestManager(Glide.with(this));
        }

//        mSendToAdapter.setOnLoadMore(new LoadMoreViewHolder.OnLoadMore() {
//            @Override
//            public void loadMore() {
//                if (mCanLoadMore) {
//                    loadMoreUsers();
//                }
//            }
//        });

        mSendToAdapter.setButtonAction(new SendToAdapter.ButtonAction() {
            @Override
            public void turnOnButton(boolean turnOn) {
                vSendButton.setBackgroundResource(turnOn ? R.color.yellow_color : R.color.twentyfive_black);
            }
        });

        mSendToAdapter.setOnCollegeViewHolderTouched(new SendToAdapter.OnCollegeViewHolderTouched() {
            @Override
            public void viewHolderTouched(final boolean active) {
                if (mTrendsItems.isEmpty()) return;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (active) {
                            mSendToItems.addAll(1, mTrendsItems);
                            mSendToAdapter.notifyItemRangeInserted(1, mTrendsItems.size());
                        } else {
                            for (int i = 0; i < mTrendsItems.size(); i++)
                                mSendToItems.remove(1);
                            mSendToAdapter.notifyItemRangeRemoved(1, mTrendsItems.size());
                        }
                    }
                });
            }
        });

        vSendButton.setBackgroundResource(mSendToAdapter.checkedItemsIsEmpty()
                ? R.color.twentyfive_black : R.color.yellow_color);

        recyclerView.setAdapter(mSendToAdapter);

        vSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendItems();
            }
        });

        if (getFragmentState() == FragmentState.NEEDS_UPDATING)
            showProgress(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPendingUploadPost != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            getSendToList();
        } else if (mSendToItems.isEmpty()) {
            showEmpty(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mPendingUploadPost != null)
            getActivity().getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void getSendToList() {
        if (getContext() == null) return;

        if (mPendingUploadPost != null)
            getTrends();
        else
            mGotResponseForApiCall = true;

        getFriends();

    }


    private void getTrends() {
        //get trends
        new LSDKGlobal(getContext()).getTrending(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null && mGotResponseForApiCall) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());

                            if (mSendToItems.isEmpty())
                                showErrorText(true);

                            setFragmentState(FragmentState.FINISHED_UPDATING);
                        }
                    });
                }
                mGotResponseForApiCall = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray trends = new JSONObject(response.body().string()).getJSONArray("trends");
                        final ArrayList<SendToItem> tempTrends = new ArrayList<>();

                        JSONObject trend;

                        for (int i = 0; i < trends.length(); i++) {
                            trend = trends.getJSONObject(i);
                            tempTrends.add(
                                    new SendToItem(
                                            SendToItem.TYPE_TREND,
                                            trend.getString("name"),
                                            trend.getString("id"),
                                            trend.getString("image")
                                    )
                            );
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            final SendToItem item = new SendToItem(
                                                    SendToItem.TYPE_CAMPUS,
                                                    "My Campus",
                                                    mPendingUploadPost.getCollegeId(),
                                                    ""
                                            );
                                            item.setChecked(true);

                                            vSendButton.setBackgroundResource(R.color.yellow_color);

                                            mSendToItems.add(0, item);
                                            mSendToItems.addAll(1, tempTrends);
                                            mTrendsItems = tempTrends;

                                            mSendToAdapter.getCheckedItems()
                                                    .get(SendToAdapter.COLLEGE_AND_TRENDS)
                                                    .add(item);

                                            if (mGotResponseForApiCall) {
                                                showProgress(false);
                                                mSendToAdapter.notifyDataSetChanged();
                                                setFragmentState(FragmentState.FINISHED_UPDATING);

                                                if (mSendToItems.isEmpty())
                                                    showEmpty(true);
                                            } else
                                                mGotResponseForApiCall = true;
                                        }
                                    });
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runServerError();
                    }

                } else {
                    Log.d(TAG, "onResponse: " + response.body().string());
                    runServerError();
                }
            }
        });
    }

    private void getFriends() {
        //get friends
        new LSDKFriends(getActivity()).getSendTo("", mUserId, /*mSkip, 20,*/ new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null && mGotResponseForApiCall) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());

                            if (mSendToItems.isEmpty())
                                showErrorText(true);

                            setFragmentState(FragmentState.FINISHED_UPDATING);
                        }
                    });
                }
                mGotResponseForApiCall = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());

                        // Log.i(TAG, "onResponse: "+object.toString(4));
                        //mSkip = object.getInt("skip");

                        final ArrayList<SendToItem> tempItems = new ArrayList<>();

                        JSONArray users = object.getJSONArray("friends");
                        //mCanLoadMore = users.length() >= 20;

                        JSONObject user;

                        for (int i = 0; i < users.length(); i++) {
                            user = users.getJSONObject(i).getJSONObject("user");
                            tempItems.add(
                                    new SendToItem(
                                            SendToItem.TYPE_PERSON,
                                            user.getString("fullName"),
                                            user.getString("id"),
                                            user.getString("profileImage")
                                    )
                            );
                        }


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSendToItems.addAll(tempItems);
//                                            mSendToAdapter.setLoadState(mCanLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);
//                                            mSkip += 20;

                                            if (mGotResponseForApiCall) {
                                                showProgress(false);
                                                mSendToAdapter.notifyDataSetChanged();
                                                setFragmentState(FragmentState.FINISHED_UPDATING);

                                                if (mSendToItems.isEmpty())
                                                    showEmpty(true);
                                            } else
                                                mGotResponseForApiCall = true;
                                        }
                                    });
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runServerError();
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response.body().string());
                    runServerError();
                }
            }
        });
    }


//    private void loadMoreUsers() {
//        if (getContext() == null) return;
//
//        new LSDKFriends(getContext()).getSendTo("", mUserId, mSkip, 20, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Utils.showBadConnectionToast(getActivity());
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    try {
//                        JSONArray users = new JSONObject(response.body().string()).getJSONArray("friends");
//
//                        final ArrayList<SendToItem> tempItems = new ArrayList<>();
//
//                        JSONObject user;
//                        for (int i = 0; i < users.length(); i++) {
//                            user = users.getJSONObject(i).getJSONObject("user");
//                            tempItems.add(
//                                    new SendToItem(
//                                            SendToItem.TYPE_PERSON,
//                                            user.getString("fullName"),
//                                            user.getString("id"),
//                                            user.getString("profileImage")
//                                    )
//                            );
//                        }
//
//                        if (getActivity() != null) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mHandler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            int start = mSendToItems.size();
//                                            mSendToItems.addAll(tempItems);
//                                            mCanLoadMore = tempItems.size() >= 20;
//                                            mSendToAdapter.setLoadState(mCanLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);
//                                            mSkip += 20;
//                                            mSendToAdapter.notifyItemRangeInserted(start, tempItems.size());
//                                            setFragmentState(FragmentState.FINISHED_UPDATING);
//                                        }
//                                    });
//                                }
//                            });
//                        }
//
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        if (getActivity() != null) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Utils.showServerErrorToast(getActivity());
//                                }
//                            });
//                        }
//                    }
//
//                } else {
//                    Log.d(TAG, "onResponse: " + response.body().string());
//                    if (getActivity() != null) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Utils.showServerErrorToast(getActivity());
//                            }
//                        });
//                    }
//                }
//            }
//        });
//    }


    private void showProgress(boolean show) {
        vProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showErrorText(boolean show) {
        if (show) {
            vErrorText.setText("Tap to reload");
            vErrorText.setVisibility(View.VISIBLE);
        }else {
            vErrorText.setVisibility(View.INVISIBLE);
        }

    }

    private void showEmpty(boolean show) {
        if (show) {
            vErrorText.setText("Empty list");
            vErrorText.setVisibility(View.VISIBLE);
        }else {
            vErrorText.setVisibility(View.GONE);
        }
    }


    private void runServerError() {
        if (getActivity() != null && mGotResponseForApiCall) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showServerErrorToast(getActivity());

                    if (mSendToItems.isEmpty())
                        showErrorText(true);

                    setFragmentState(FragmentState.FINISHED_UPDATING);
                }
            });
        }
        mGotResponseForApiCall = true;
    }

    public void sendItems() {

        if (mSendToAdapter == null || mSendToAdapter.checkedItemsIsEmpty()) return;


        if (mPendingUploadPost != null) {
            ArrayList<String> mTrends = new ArrayList<>();
            ArrayList<String> mPeople = new ArrayList<>();
            boolean collegeIsChecked = false;


            for (SendToItem item : mSendToAdapter.getCheckedItems().get(SendToAdapter.COLLEGE_AND_TRENDS)) {
                if (item.getType() == SendToItem.TYPE_CAMPUS)
                    collegeIsChecked = true;
                else
                    mTrends.add(item.getId());
            }

            for (SendToItem item : mSendToAdapter.getCheckedItems().get(SendToAdapter.PEOPLE))
                mPeople.add(item.getId());

            if (collegeIsChecked) {
                mPendingUploadPost.setShareParams(mPeople, mTrends);
            } else {
                mPendingUploadPost.setCollege(null);
                mPendingUploadPost.setShareParams(mPeople, new ArrayList<String>());
            }

            Intent result = new Intent();
            result.putExtra(PendingUploadPost.PENDING_POST_KEY, mPendingUploadPost);
            Toast.makeText(getActivity(), "Uploading in background...", Toast.LENGTH_SHORT).show();

            getActivity().setResult(Activity.RESULT_OK, result);
            getActivity().finish();

        } else {
            BaseTaptActivity activity = (BaseTaptActivity) getActivity();
            if (activity == null) return;

            JSONArray people = new JSONArray();
            JSONArray trends = new JSONArray();

            for (SendToItem sendToItem : mSendToAdapter.getCheckedItems().get(SendToAdapter.COLLEGE_AND_TRENDS))
                if (sendToItem.getType() == SendToItem.TYPE_TREND)
                    trends.put(sendToItem.getId());


            for (SendToItem sendToItem : mSendToAdapter.getCheckedItems().get(SendToAdapter.PEOPLE))
                people.put(sendToItem.getId());


            JSONObject send = new JSONObject();
            try {
                send.put("college", null);
                send.put("users", people);
                send.put("trends", trends);
                send.put("post", mPostId);

                if (!activity.socketConnected()) {
                    Utils.showBadConnectionToast(activity);
                } else {
                    activity.emitSocket(API_Methods.VERSION + ":posts:share", send);
                    Toast.makeText(activity, "Post has been shared", Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
