package com.linute.linute.MainContent.FindFriends.FindFriendsFragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.MvpBaseClasses.RequestCallbackView;
import com.linute.linute.UtilsAndHelpers.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by QiFeng on 9/24/16.
 */
public abstract class BaseFindFriendsFragment extends BaseFragment implements RequestCallbackView {

    public static final String TAG = BaseFindFriendsFragment.class.getSimpleName();

    protected FriendSearchAdapter mFriendSearchAdapter;
    protected String mQueryString; //what's in the search view

    // filtered list of users
    protected List<FriendSearchUser> mFriendFoundList = new ArrayList<>();

    protected ProgressBar mProgressBar;
    protected View mFindFriendsRationale;
    protected View mReloadButton;

    protected View mEmptyText;

    protected Handler mMainHandler = new Handler();
    protected Handler mSearchHandler = new Handler(); //handles filtering and searchign

    protected FindFriendsSearchPresenter mFindFriendsSearchPresenter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_friends, container, false);
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.findFriends_recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.findFriends_progressbar);
        mFindFriendsRationale = rootView.findViewById(R.id.findFriends_rationale_text);
        mReloadButton = rootView.findViewById(R.id.findFriends_turn_on);

        mEmptyText = rootView.findViewById(R.id.findFriends_text);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mFriendSearchAdapter = new FriendSearchAdapter(getActivity(), mFriendFoundList);
        mFriendSearchAdapter.setRequestManager(Glide.with(this));

        recyclerView.setAdapter(mFriendSearchAdapter);

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

        setUpPresenter();
        initScreen();

        return rootView;
    }

    protected abstract void setUpPresenter();
    protected abstract void initScreen();
    public abstract void searchWithQuery(String query);

    @Override
    public void onSuccess(final ArrayList<FriendSearchUser> list, boolean canLoadMore) {
        if (getActivity() == null) return;

        mProgressBar.setVisibility(View.GONE);
        mEmptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        setFragmentState(FragmentState.FINISHED_UPDATING);

        Log.d(TAG, "onSuccess: ");

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mFriendFoundList.clear();
                mFriendFoundList.addAll(list);
                mMainHandler.removeCallbacksAndMessages(null);
                mFriendSearchAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onError(String response) {
        Log.d(TAG, "onError: "+response);
        if (getActivity() == null) return;
        mProgressBar.setVisibility(View.GONE);
        Utils.showServerErrorToast(getActivity());
        if (mFriendFoundList.isEmpty()){
            mFindFriendsRationale.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFailure() {
        if (getActivity() == null) return;
        mProgressBar.setVisibility(View.GONE);
        Utils.showBadConnectionToast(getActivity());
        if (mFriendFoundList.isEmpty()){
            mFindFriendsRationale.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mFriendSearchAdapter.getRequestManager() != null)
            mFriendSearchAdapter.getRequestManager().onDestroy();

        mFriendSearchAdapter.setRequestManager(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFriendSearchAdapter != null) mFriendSearchAdapter.clearContext();
        if (mFindFriendsSearchPresenter != null) mFindFriendsSearchPresenter.cancelRequest();
        mSearchHandler.removeCallbacksAndMessages(null);
    }
}
