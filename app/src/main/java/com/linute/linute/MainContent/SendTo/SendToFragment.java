package com.linute.linute.MainContent.SendTo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.BaseRequestPresenter;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.RequestCallbackView;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by QiFeng on 7/15/16.
 */
public class SendToFragment extends BaseFragment implements RequestCallbackView<SendToItem>{

    public static final String TAG = SendToFragment.class.getSimpleName();
    public static final String POST_ID_KEY = "send_post_id_key";

    private ArrayList<SendToItem> mSendToItems = new ArrayList<>();

    private SendToAdapter mSendToAdapter;

    private String mPostId;

    private View vProgress;
    private TextView vErrorText;
    private EditText vSearch;
    private Button vSendButton;
    private BaseRequestPresenter mRequestPresenter;
    private Handler mSearchHandler = new Handler();

    //private PendingUploadPost mPendingUploadPost;

    // we currently have to make 2 api calls : one to retrieve list of trends and one to retrieve list
    //   friends. We won't show list until both api calls have finished
    //private boolean mGotResponseForApiCall = false;


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
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send_to, container, false);

        vProgress = root.findViewById(R.id.progress);
        vErrorText = (TextView) root.findViewById(R.id.error_text);

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

        vSearch = (EditText) toolbar.findViewById(R.id.search_view);

        vSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (getFragmentState() == FragmentState.FINISHED_UPDATING) {
                    mSearchHandler.removeCallbacksAndMessages(null);
                    mSearchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            search();
                        }
                    }, 250);
                }
            }
        });

        if (mSendToAdapter == null) {
            mSendToAdapter = new SendToAdapter(getContext(), Glide.with(this), mSendToItems);
        } else {
            mSendToAdapter.setRequestManager(Glide.with(this));
        }

        mSendToAdapter.setButtonAction(new SendToAdapter.ButtonAction() {
            @Override
            public void turnOnButton(boolean turnOn) {
                vSendButton.setBackgroundResource(turnOn ? R.color.yellow_color : R.color.twentyfive_black);
            }
        });

        //mSendToAdapter.setOnLoadMore(this);
        mRequestPresenter = new SendToPresenter(this);

        vSendButton.setBackgroundResource(mSendToAdapter.checkedItemsIsEmpty()
                ? R.color.twentyfive_black : R.color.yellow_color);

        recyclerView.setAdapter(mSendToAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }
        });

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
            search();
            setFragmentState(FragmentState.FINISHED_UPDATING);
        } else if (mSendToItems.isEmpty()) {
            showEmpty(true);
            showProgress(false);
        }
    }


    private void search() {
        if (getContext() == null) return;
        HashMap<String, Object> params = new HashMap<>();
        params.put("fullName", vSearch.getText().toString());
        mRequestPresenter.request(getContext(), params, false);
    }


    private void showProgress(boolean show) {
        vProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void showEmpty(boolean show) {
        if (show) {
            vErrorText.setText("No friends found");
            vErrorText.setVisibility(View.VISIBLE);
            vErrorText.setOnClickListener(null);
        } else {
            vErrorText.setVisibility(View.GONE);
        }
    }

    public void sendItems() {

        if (mSendToAdapter == null || mSendToAdapter.checkedItemsIsEmpty()) return;

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;


        if (!TaptSocket.getInstance().socketConnected()) {
            Toast.makeText(activity, "Failed to share post", Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();
            return;
        }

        JSONArray people = new JSONArray();
        JSONArray trends = new JSONArray();

        for (SendToItem sendToItem : mSendToAdapter.getCheckedItems().get(SendToAdapter.COLLEGE_AND_TRENDS))
            if (sendToItem.getType() == SendToItem.TYPE_TREND)
                trends.put(sendToItem.getId());

        String firstPersonName = null;
        for (SendToItem sendToItem : mSendToAdapter.getCheckedItems().get(SendToAdapter.PEOPLE)) {
            people.put(sendToItem.getId());
            if (firstPersonName == null)
                firstPersonName = sendToItem.getName();
        }

        JSONObject send = new JSONObject();
        try {
            send.put("college", null);
            send.put("users", people);
            send.put("trends", trends);
            send.put("post", mPostId);

            TaptSocket.getInstance().emit(API_Methods.VERSION + ":posts:share", send);

            String text;
            if (people.length() > 1) {
                text = String.format(Locale.US, "Sent to %d people", people.length());
            } else if (!mSendToAdapter.getCheckedItems().get(SendToAdapter.PEOPLE).isEmpty() && firstPersonName != null) {
                text = String.format(Locale.US, "Sent to %s", firstPersonName);
            } else {
                text = "Post has been shared";
            }

            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            getFragmentManager().popBackStack();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //}
    }


    private void hideKeyboard() {
        if (vSearch.hasFocus() && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(vSearch.getWindowToken(), 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRequestPresenter != null) mRequestPresenter.cancelRequest();
    }

    @Override
    public void onSuccess(final ArrayList<SendToItem> list, boolean canLoadMore, boolean addToBack) {
        if (getActivity() == null) return;

        //mSendToAdapter.setLoadState(canLoadMore ? LoadMoreViewHolder.STATE_LOADING : LoadMoreViewHolder.STATE_END);

        if (addToBack) {
            if (list.isEmpty()) return;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int start = mSendToItems.size();
                    mSendToItems.addAll(list);
                    mSendToAdapter.notifyItemRangeInserted(start, list.size());
                    mSendToAdapter.notifyItemChanged(mSendToItems.size());
                }
            });
        }else {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendToItems.clear();
                    mSendToItems.addAll(list);
                    mSendToAdapter.notifyDataSetChanged();
                    showProgress(false);
                    showEmpty(mSendToItems.isEmpty());
                }
            });
        }
    }

    @Override
    public void onError(String response) {
        if (getActivity() == null) return;
        showProgress(false);
        Utils.showServerErrorToast(getActivity());

        if (mSendToItems.isEmpty())
            showEmpty(true);
    }

    @Override
    public void onFailure() {
        if (getActivity() == null) return;

        showProgress(false);
        Utils.showBadConnectionToast(getActivity());

        if (mSendToItems.isEmpty())
            showEmpty(true);
    }

}
