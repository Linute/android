package com.linute.linute.MainContent.Global;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.FeedDetailFragment.ViewFullScreenFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinearLayoutManagerWithSmoothScroller;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VerticalSnappingRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 5/14/16.
 */
public class TrendingPostsFragment extends BaseFragment implements TrendingItemAdapter.OnLongPress{

    private static final String ID_KEY = "id_key";
    private static final String TAG = TrendingPostsFragment.class.getSimpleName();
    private VerticalSnappingRecyclerView vRecView;

    private ArrayList<Post> mPostList = new ArrayList<>();

    private String mTrendId;
    private View mProgressBar;
    private View mRetry;

    TrendingItemAdapter mTrendingAdapter;
    Handler mHandler = new Handler();


    public static TrendingPostsFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(ID_KEY, id);
        TrendingPostsFragment fragment = new TrendingPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrendId = getArguments().getString(ID_KEY);
        }
        mTrendingAdapter = new TrendingItemAdapter(mPostList, getContext(),
                new LoadMoreViewHolder.OnLoadMore() {
                    @Override
                    public void loadMore() {
                        if (getFragmentState() == FragmentState.LOADING_DATA || feedDone) return;
                        if (mTrendingAdapter.getFooterState() == LoadMoreViewHolder.STATE_LOADING) {
                            loadMoreFeedFromServer();
                        }
                    }
                }, mTrendId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trending, container, false);

        vRecView = (VerticalSnappingRecyclerView) root.findViewById(R.id.recycler_view);
        mRetry = root.findViewById(R.id.retry);
        mRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mRetry.setVisibility(View.GONE);
                getPosts();
            }
        });

        vRecView.setOnScrollStoppedListener(new VerticalSnappingRecyclerView.OnScrollStoppedListener() {
            @Override
            public void scrollStarted() {
                if (vRecView.getHolder() == null) {
                    vRecView.setHolder(vRecView.findViewHolderForAdapterPosition(0));
                    if (vRecView.getHolder() == null) return;
                }
                if (vRecView.getHolder() instanceof TrendingItemAdapter.BaseTrendViewHolder) {
                    ((TrendingItemAdapter.BaseTrendViewHolder) vRecView.getHolder()).lostFocus();
                }
            }

            @Override
            public void scrollStopped(int position) {
                RecyclerView.ViewHolder h = vRecView.findViewHolderForAdapterPosition(position);
                if (h != null) {
                    vRecView.setHolder(h);
                    if (h instanceof TrendingItemAdapter.BaseTrendViewHolder) {
                        ((TrendingItemAdapter.BaseTrendViewHolder) h).gainedFocus();
                    }
                }
            }
        });

        vRecView.enableTouchListener();

        vRecView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(getContext(), LinearLayoutManager.VERTICAL, false));
        mTrendingAdapter.setScrollToPosition(new TrendingItemAdapter.ScrollToPosition() {
            @Override
            public void scrollToPosition(int position) {
                vRecView.customScrollToPosition(position);
            }
        });
        mTrendingAdapter.setOnLongPress(this);

        vRecView.setAdapter(mTrendingAdapter);
        vRecView.addItemDecoration(new SpaceItemDecoration(2));

        mProgressBar = root.findViewById(R.id.progress_bar);

        root.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        return root;
    }



    @Override
    public void onResume() {
        super.onResume();
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            getPosts();
        }else {
            if (mPostList.isEmpty()){
                mRetry.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
    }

    private int mSkip;
    private boolean feedDone = false;

    public void getPosts() {
        if (getActivity() == null) return;

        setFragmentState(FragmentState.LOADING_DATA);

        mProgressBar.setVisibility(View.VISIBLE);

        Map<String, String> params = new HashMap<>();
        params.put("trend", mTrendId);
        params.put("limit", "20");

        new LSDKGlobal(getActivity()).getPosts(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        mProgressBar.setVisibility(View.GONE);
                        mRetry.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    Log.d("HEY", response.body().string());
                    setFragmentState(FragmentState.FINISHED_UPDATING);
                    if (getActivity() != null) { //shows server error toast
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mProgressBar.setVisibility(View.GONE);
                                mRetry.setVisibility(View.VISIBLE);

                            }
                        });
                    }
                    return;
                }

                JSONObject jsonObject;
                JSONArray jsonArray;
                try {

                    jsonObject = new JSONObject(response.body().string());
                    //Log.i(TAG, "onResponse: "+jsonObject.toString(4));

                    mSkip = jsonObject.getInt("skip");

                    jsonArray = jsonObject.getJSONArray("posts");

                    if (mSkip == 0) {
                        feedDone = true; //no more feed to load
                        mTrendingAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                    }

                    final ArrayList<Post> refreshedPosts = new ArrayList<>();

                    for (int i = jsonArray.length() - 1; i >= 0; i--) {
                        try {
                            refreshedPosts.add(new Post(jsonArray.getJSONObject(i)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    final MainActivity activity = (MainActivity) getActivity();
                    if (activity == null) {
                        setFragmentState(FragmentState.FINISHED_UPDATING);
                        return;
                    }

                    activity.runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mPostList.clear();
                                            mPostList.addAll(refreshedPosts);
                                            mTrendingAdapter.notifyDataSetChanged();
                                        }
                                    });

                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mProgressBar.setVisibility(View.GONE);
                                mRetry.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        });
    }


    public void loadMoreFeedFromServer() {

        if (getFragmentState() == FragmentState.LOADING_DATA || getActivity() == null) return;

        setFragmentState(FragmentState.LOADING_DATA);

        int skip = mSkip;

        skip -= 20;
        int limit = 20;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        final int skip1 = skip;

        Map<String, String> params = new HashMap<>();
        params.put("trend", mTrendId);
        params.put("skip", skip + "");
        params.put("limit", limit + "");

        new LSDKGlobal(getContext()).getPosts(params, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        setFragmentState(FragmentState.FINISHED_UPDATING);
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
                        if (!response.isSuccessful()) {
                            Log.d("HEY", response.body().string());
                            setFragmentState(FragmentState.FINISHED_UPDATING);
                            if (getActivity() != null) { //shows server error toast
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                            return;
                        }

                        String json = response.body().string();
                        //Log.i(TAG, "onResponse: " + json);
                        JSONObject jsonObject;
                        JSONArray jsonArray;
                        final ArrayList<Post> temp = new ArrayList<>();
                        try {
                            jsonObject = new JSONObject(json);
                            jsonArray = jsonObject.getJSONArray("posts");

                            if (skip1 == 0) {
                                mTrendingAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                                feedDone = true; //no more feed to load
                            }

                            for (int i = jsonArray.length() - 1; i >= 0; i--) {
                                try {
                                    temp.add(new Post(jsonArray.getJSONObject(i)));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            mSkip = skip1;

                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mPostList.addAll(temp);
                                                    mTrendingAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    }

                            );

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

                        setFragmentState(FragmentState.FINISHED_UPDATING);
                    }
                }
        );
    }

    @Override
    public void onLongPress(Post post, int pos) {
        LinearLayoutManager manager = (LinearLayoutManager) vRecView.getLayoutManager();

        Log.i(TAG, "onLongPress: "+pos + " "+manager.findFirstVisibleItemPosition());
        if (manager.findFirstVisibleItemPosition() != pos) return;
        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.addFragmentOnTop(
                    ViewFullScreenFragment.newInstance(
                            Uri.parse(post.getType() == Post.POST_TYPE_VIDEO ? post.getVideoUrl() : post.getImage()),
                            post.getType()
                    )
            );
        }
    }
}
