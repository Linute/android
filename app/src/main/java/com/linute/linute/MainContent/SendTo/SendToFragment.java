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
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.Database.TaptUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by QiFeng on 7/15/16.
 */
public class SendToFragment extends BaseFragment {

    public static final String TAG = SendToFragment.class.getSimpleName();
    public static final String POST_ID_KEY = "send_post_id_key";

    private ArrayList<SendToItem> mUnfilteredList = new ArrayList<>();
    private ArrayList<SendToItem> mSendToItems = new ArrayList<>();

    private SendToAdapter mSendToAdapter;

    private String mPostId;

    private Handler mHandler = new Handler();

    private View vProgress;
    private TextView vErrorText;

    private EditText vSearch;

    private Button vSendButton;

    //private PendingUploadPost mPendingUploadPost;

    // we currently have to make 2 api calls : one to retrieve list of trends and one to retrieve list
    //   friends. We won't show list until both api calls have finished
    //private boolean mGotResponseForApiCall = false;

    private Realm mRealm;
    private RealmResults<TaptUser> mRealmResults;


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

        mRealm = Realm.getDefaultInstance();
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
                    filterList();
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
            getSendToList();
        } else if (mSendToItems.isEmpty()) {
            showEmpty(true);
        }
    }


    private void getSendToList() {
        if (getContext() == null) return;
        getFriends();
    }


    private void getFriends() {
        mRealmResults = mRealm.where(TaptUser.class)
                .equalTo("isFriend", true)
                .findAllAsync();

        //gets called when we get the results from async call
        mRealmResults.addChangeListener(new RealmChangeListener<RealmResults<TaptUser>>() {
            @Override
            public void onChange(RealmResults<TaptUser> element) {
                ArrayList<SendToItem> items = new ArrayList<>();
                for (TaptUser user1 : mRealmResults) {
                    items.add(new SendToItem(
                            SendToItem.TYPE_PERSON,
                            user1.getFullName(),
                            user1.getId(),
                            user1.getProfileImage()
                    ));
                }

                mUnfilteredList.clear();
                mUnfilteredList.addAll(items);
                mRealmResults.removeChangeListeners();
                filterList();
            }
        });
    }

    private void filterList() {

        final String filter = vSearch.getText().toString().toLowerCase();

        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (filter.isEmpty()) {
                    mSendToItems.clear();
                    mSendToItems.addAll(mUnfilteredList);
                } else {
                    ArrayList<SendToItem> items = new ArrayList<>();
                    for (SendToItem i : mUnfilteredList) {
                        if (i.getName().toLowerCase().contains(filter)) {
                            items.add(i);
                        }
                    }
                    mSendToItems.clear();
                    mSendToItems.addAll(items);
                }

                mSendToAdapter.notifyDataSetChanged();
                showEmpty(mSendToItems.isEmpty());
                showProgress(false);
                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        }, 200);
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

            activity.emitSocket(API_Methods.VERSION + ":posts:share", send);
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
        mRealmResults.removeChangeListeners();
        hideKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
