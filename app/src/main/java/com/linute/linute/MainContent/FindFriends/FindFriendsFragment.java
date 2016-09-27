package com.linute.linute.MainContent.FindFriends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.HashMap;

/**
 * Created by QiFeng on 1/16/16.
 */

public class FindFriendsFragment extends BaseFindFriendsFragment {

    public static final String TAG = FindFriendsFragment.class.getSimpleName();
    public static final String ARG_FILTERS= "filters_arg";

    private SearchFilter[] mSearchFilters;

    public static FindFriendsFragment newInstance(SearchFilter[] filters) {
        FindFriendsFragment fragment = new FindFriendsFragment();
        Bundle args = new Bundle();
        args.putParcelableArray(ARG_FILTERS, filters);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            mSearchFilters = (SearchFilter[]) getArguments().getParcelableArray(ARG_FILTERS);
        }
    }

    @Override
    protected void setUpPresenter() {
        mFindFriendsSearchPresenter = new FindFriendsSearchPresenter(this, FindFriendsSearchPresenter.TYPE_SEARCH);
    }

    @Override
    protected void initScreen() {
        if(!mFindFriendsSearchPresenter.originalListLoaded()){
            mFindFriendsRationale.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mFindFriendsSearchPresenter.request(getContext(), getParms(""));
        }else {
            mEmptyText.setVisibility(mFriendFoundList.isEmpty() ? View.VISIBLE : View.GONE);
        }

        mFindFriendsRationale.setVisibility(View.GONE);
        mReloadButton.setVisibility(View.GONE);
    }

    @Override
    public void searchWithQuery(final String query) {
        mSearchHandler.removeCallbacksAndMessages(null);
        mSearchHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null) return;
                mFindFriendsSearchPresenter.request(getContext(), getParms(query));
            }
        }, 300);
    }

    private HashMap<String, Object> getParms(String name){
        HashMap<String, Object> params = new HashMap<>();
        params.put("fullName", name);
        for (SearchFilter f : mSearchFilters)
            params.put(f.key, f.value);

        return params;
    }
}

