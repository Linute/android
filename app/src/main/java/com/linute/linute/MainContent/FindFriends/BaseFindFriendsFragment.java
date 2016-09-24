package com.linute.linute.MainContent.FindFriends;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 9/24/16.
 */
public abstract class BaseFindFriendsFragment extends BaseFragment implements RequestCallbackView {

    public static final String TAG = BaseFindFriendsFragment.class.getSimpleName();

    protected FriendSearchAdapter mFriendSearchAdapter;
    protected String mQueryString; //what's in the search view

    // filtered list of users
    protected List<FriendSearchUser> mFriendFoundList = new ArrayList<>();

    private CallbackManager mCallbackManager;
    private ProgressBar mProgressBar;
    private View mFindFriendsRationale;

    private View mEmptyText;

    private Handler mMainHandler = new Handler();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_friends, container, false);

        TextView rationaleText = (TextView) rootView.findViewById(R.id.findFriends_rat_text);
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.findFriends_recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.findFriends_progressbar);
        mFindFriendsRationale = rootView.findViewById(R.id.findFriends_rationale_text);

        Button reloadButton = (Button) rootView.findViewById(R.id.findFriends_turn_on);

        mEmptyText = rootView.findViewById(R.id.findFriends_text);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mFriendSearchAdapter = new FriendSearchAdapter(getActivity(), mFriendFoundList);
        mFriendSearchAdapter.setRequestManager(Glide.with(this));

        recyclerView.setAdapter(mFriendSearchAdapter);
        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    FindFriendsChoiceFragment fragment = (FindFriendsChoiceFragment) getParentFragment();
                    if (fragment != null) fragment.hideKeyboard();
                }
            }
        });


        return rootView;
    }

}
