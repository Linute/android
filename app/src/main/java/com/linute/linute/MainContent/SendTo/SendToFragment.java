package com.linute.linute.MainContent.SendTo;

import android.content.Context;
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
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKFriends;
import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 7/15/16.
 */
public class SendToFragment extends BaseFragment {

    public static final String TAG = SendToFragment.class.getSimpleName();
    public static final String POST_ID_KEY = "send_post_id_key";
    public static final String SHOW_TRENDING ="show_trending";

    private ArrayList<SendToItem> mSendToItems = new ArrayList<>();
    private SendToAdapter mSendToAdapter;

    private String mPostId;
    private String mUserId;
    private boolean mShowTrend;

    private Handler mHandler = new Handler();

    private View vProgress;
    private View vErrorText;

    private int mSkip = 0;
    private boolean mCanLoadMore = true;
    private OnSendItems mOnSendItems;

    // we currently have to make 2 api calls : one to retrieve list of trends and one to retrieve list
    //   friends. We won't show list until both api calls have finished
    private boolean mGotResponseForApiCall = false;


    /**
     * Use this constructor!
     *
     * @param postId - the id of the post being shared
     * @return fragment
     */

    public static SendToFragment newInstance(String postId, boolean showTrending) {
        SendToFragment fragment = new SendToFragment();
        Bundle bundle = new Bundle();
        bundle.putString(POST_ID_KEY, postId);
        bundle.putBoolean(SHOW_TRENDING, showTrending);
        fragment.setArguments(bundle);
        return fragment;
    }


    /**
     * Please don't use this constructor,
     * use newInstance(postId, showTrend) instead
     */
    public SendToFragment() {

    }


    public void setOnSendItems(OnSendItems onSendItems){
        mOnSendItems = onSendItems;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPostId = getArguments().getString(POST_ID_KEY);
            mShowTrend = getArguments().getBoolean(SHOW_TRENDING);
        }
        mUserId = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_to, container, false);

        vProgress = root.findViewById(R.id.progress);
        vErrorText = root.findViewById(R.id.error_text);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        final Button vSendButton = (Button) root.findViewById(R.id.send_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        ((Toolbar) root.findViewById(R.id.toolbar)).setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (mSendToAdapter == null) {
            mSendToAdapter = new SendToAdapter(getContext(), Glide.with(this), mSendToItems);
        } else {
            mSendToAdapter.setRequestManager(Glide.with(this));
        }

        mSendToAdapter.setOnLoadMore(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (mCanLoadMore) {
                    loadMoreUsers();
                }
            }
        });

        mSendToAdapter.setButtonAction(new SendToAdapter.ButtonAction() {
            @Override
            public void turnOnButton(boolean turnOn) {
                vSendButton.setAlpha(turnOn ? 1f : 0.5f);
            }
        });

        vSendButton.setAlpha(mSendToAdapter.getCheckedItems().isEmpty() ? 0.5f : 1f);

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
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            getSendToList();
        } else if (mSendToItems.isEmpty()) {
            showErrorText(true);
        }
    }

    private void getSendToList() {
        if (getContext() == null) return;

        if (mShowTrend)
            getTrends();
        else
            mGotResponseForApiCall = true;

        getFriends();

    }


    private void getTrends(){
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
                                            mSendToItems.addAll(0, tempTrends);
                                            if (mGotResponseForApiCall) {
                                                showProgress(false);
                                                mSendToAdapter.notifyDataSetChanged();
                                                setFragmentState(FragmentState.FINISHED_UPDATING);

                                                if (mSendToItems.isEmpty())
                                                    showErrorText(true);
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

    private void getFriends(){
        //get friends
        new LSDKFriends(getActivity()).getSendTo(mUserId, mSkip, 20, new Callback() {
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
                        mCanLoadMore = users.length() >= 20;

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
                                            mSendToAdapter.setLoadState(mCanLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);
                                            mSkip += 20;

                                            if (mGotResponseForApiCall) {
                                                showProgress(false);
                                                mSendToAdapter.notifyDataSetChanged();
                                                setFragmentState(FragmentState.FINISHED_UPDATING);

                                                if (mSendToItems.isEmpty())
                                                    showErrorText(true);
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


    private void loadMoreUsers() {
        if (getContext() == null) return;



        new LSDKFriends(getContext()).getSendTo(mUserId, mSkip, 20, new Callback() {
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
                    try {
                        JSONArray users = new JSONObject(response.body().string()).getJSONArray("friends");

                        final ArrayList<SendToItem> tempItems = new ArrayList<>();

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
                                            int start = mSendToItems.size();
                                            mSendToItems.addAll(tempItems);
                                            mCanLoadMore = tempItems.size() >= 20;
                                            mSendToAdapter.setLoadState(mCanLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);
                                            mSkip += 20;

                                            mSendToAdapter.notifyItemRangeInserted(start, tempItems.size());
                                            setFragmentState(FragmentState.FINISHED_UPDATING);
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
            }
        });
    }


    private void showProgress(boolean show) {
        vProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showErrorText(boolean show) {
        vErrorText.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
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
        if (mSendToAdapter == null || mSendToAdapter.getCheckedItems().isEmpty()) return;

        if (mOnSendItems != null){
            mOnSendItems.sendItems(mSendToAdapter.getCheckedItems());
        } else {
            BaseTaptActivity activity = (BaseTaptActivity) getActivity();
            if (activity == null) return;

            JSONArray people = new JSONArray();
            JSONArray trends = new JSONArray();
            for (SendToItem sendToItem : mSendToAdapter.getCheckedItems()) {
                if (sendToItem.getType() == SendToItem.TYPE_PERSON) {
                    people.put(sendToItem.getId());
                } else if (sendToItem.getType() == SendToItem.TYPE_TREND) {
                    trends.put(sendToItem.getId());
                }
            }

            JSONObject send = new JSONObject();
            try {
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




    public interface OnSendItems{
        public void sendItems(HashSet<SendToItem> items);
    }


}
