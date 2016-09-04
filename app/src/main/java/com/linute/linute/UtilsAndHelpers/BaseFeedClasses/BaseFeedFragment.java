package com.linute.linute.UtilsAndHelpers.BaseFeedClasses;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;

/**
 * Created by QiFeng on 9/2/16.
 */
public abstract class BaseFeedFragment extends BaseFragment {

    public final static String TAG = BaseFeedFragment.class.getSimpleName();

    protected RecyclerView vRecyclerView;
    protected View vEmptyView;

    protected BaseFeedAdapter mFeedAdapter;

    //for loading more feed
    protected boolean mFeedDone;
    protected int mSkip = 0;
    protected boolean mLoadingMore = false;


    protected Handler mHandler = new Handler(); //handler for recview

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(getLayout(), container, false); //setContent
        initAdapter();
        mFeedAdapter.setRequestManager(Glide.with(this));

        vEmptyView = rootView.findViewById(R.id.empty_view);

        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        vRecyclerView.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        vRecyclerView.setLayoutManager(llm);

      /*  recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));*/

        mFeedAdapter.setGetMoreFeed(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (getFragmentState() == FragmentState.LOADING_DATA || mFeedDone)
                    return;
                if (mFeedAdapter.getLoadState() == LoadMoreViewHolder.STATE_LOADING) {
                    getMorePosts();
                }
            }
        });

        vRecyclerView.setAdapter(mFeedAdapter);
        return rootView;
    }

    protected abstract void initAdapter();

    protected abstract int getLayout();

    protected abstract void getPosts();
    protected abstract void getMorePosts();


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mFeedAdapter.getRequestManager() != null)
            mFeedAdapter.getRequestManager().onDestroy();

        mFeedAdapter.setRequestManager(null);
    }
}
