package com.linute.linute.MainContent.Global;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.CreateContent.CreateStatusActivity;
import com.linute.linute.MainContent.CreateContent.Gallery.GalleryActivity;
import com.linute.linute.MainContent.DiscoverFragment.BaseFeedItem;
import com.linute.linute.MainContent.DiscoverFragment.Poll;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.EditScreen.PostOptions;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CameraType;
import com.linute.linute.UtilsAndHelpers.BaseFeedClasses.BaseFeedFragment;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

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

import static com.linute.linute.MainContent.MainActivity.PHOTO_STATUS_POSTED;

/**
 * Created by QiFeng on 5/14/16.
 */
public class TrendingPostsFragment extends BaseFeedFragment {

    private static final String ARGS_KEY = "id_key";
    private static final String TAG = TrendingPostsFragment.class.getSimpleName();

    private ArrayList<BaseFeedItem> mPostList = new ArrayList<>();

    private GlobalChoiceItem mGlobalItem;

    private View mProgressBar;
    private AppBarLayout vAppBarLayout;
    private FloatingActionsMenu mFloatingActionsMenu;


    public static TrendingPostsFragment newInstance(GlobalChoiceItem item) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_KEY, item);
        TrendingPostsFragment fragment = new TrendingPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGlobalItem = getArguments().getParcelable(ARGS_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        vAppBarLayout = (AppBarLayout) root.findViewById(R.id.appbar_layout);
        Toolbar toolbar = (Toolbar) vAppBarLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        toolbar.setTitle(mGlobalItem.title);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vRecyclerView.scrollToPosition(0);
            }
        });

        vEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                vEmptyView.setVisibility(View.GONE);
                getPosts();
            }
        });


        mProgressBar = root.findViewById(R.id.progress_bar);

        mFloatingActionsMenu = (FloatingActionsMenu) root.findViewById(R.id.create_menu);
        final View fabCloseOverlay = root.findViewById(R.id.fab_close_overlay);
        fabCloseOverlay.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (mFloatingActionsMenu.isExpanded())
                            mFloatingActionsMenu.collapse();
                        return false;
                    }
                });

        mFloatingActionsMenu.findViewById(R.id.create_camera).
                setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getActivity() == null) return;
                                PostOptions postOptions = new PostOptions(PostOptions.ContentType.None, PostOptions.ContentSubType.Post, mGlobalItem.key);
                                Intent i = new Intent(getActivity(), CameraActivity.class);
                                i.putExtra(CameraActivity.EXTRA_CAMERA_TYPE, new CameraType(CameraType.CAMERA_EVERYTHING));
                                i.putExtra(CameraActivity.EXTRA_RETURN_TYPE, CameraActivity.SEND_POST);
                                i.putExtra(CameraActivity.EXTRA_POST_OPTIONS, postOptions);
                                getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                            }
                        }
                );

        mFloatingActionsMenu.findViewById(R.id.create_upload).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getActivity() == null) return;
                        Intent i = new Intent(getActivity(), GalleryActivity.class);
                        PostOptions postOptions = new PostOptions(PostOptions.ContentType.None, PostOptions.ContentSubType.Post, mGlobalItem.key);
                        i.putExtra(GalleryActivity.ARG_RETURN_TYPE, CameraActivity.SEND_POST);
                        i.putExtra(GalleryActivity.ARG_POST_OPTIONS, postOptions);
                        getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                    }
                }
        );

        mFloatingActionsMenu.findViewById(R.id.create_text).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() == null) return;
                        Intent i = new Intent(getActivity(), CreateStatusActivity.class);
                        i.putExtra(CreateStatusActivity.EXTRA_TREND_ID, mGlobalItem.key);
                        getActivity().startActivityForResult(i, PHOTO_STATUS_POSTED);
                    }
                });


        if (mGlobalItem.type == GlobalChoiceItem.TYPE_HEADER_FRIEND || mGlobalItem.type == GlobalChoiceItem.TYPE_HEADER_HOT){
            mFloatingActionsMenu.setVisibility(View.GONE);
        }


        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            getPosts();
        } else {
            if (mPostList.isEmpty()) {
                vEmptyView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();
        vAppBarLayout.setExpanded(true, false);
    }

    @Override
    protected void getPosts() {
        if (getActivity() == null) return;

        setFragmentState(FragmentState.LOADING_DATA);
        mProgressBar.setVisibility(View.VISIBLE);
        new LSDKEvents(getContext()).getEvents(getUrlPathEnding(), getParams(-1, 20), getPostsCallback());
    }

    private String getUrlPathEnding(){
        switch (mGlobalItem.type){
            case GlobalChoiceItem.TYPE_HEADER_FRIEND:
                return  "friends";
            case GlobalChoiceItem.TYPE_HEADER_HOT:
                return  "hot";
            case GlobalChoiceItem.TYPE_TREND:
                return  "trend";
            default:
                return "hot";
        }
    }

    private Map<String,Object> getParams(int skip, int limit){
        Map<String, Object> params = new HashMap<>();
        if (mGlobalItem.type == GlobalChoiceItem.TYPE_TREND){
            params.put("trend", mGlobalItem.key);
        }

        params.put("limit", limit+"");

        if (skip > -1){
            params.put("skip", skip+"");
        }

        return params;
    }

    private Callback getMorePostsCallback(final int skip1) {
        return new Callback() {
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
                    jsonArray = jsonObject.getJSONArray(mGlobalItem.type == GlobalChoiceItem.TYPE_TREND ? "posts" : "events");

                    if (skip1 == 0) {
                        mFeedAdapter.setLoadState(LoadMoreViewHolder.STATE_END);
                        mFeedDone = true; //no more feed to load
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
                                            mFeedAdapter.notifyDataSetChanged();
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
        };
    }

    private Callback getPostsCallback() {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        mProgressBar.setVisibility(View.GONE);
                        vEmptyView.setVisibility(View.VISIBLE);
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
                                vEmptyView.setVisibility(View.VISIBLE);

                            }
                        });
                    }
                    return;
                }

                JSONObject jsonObject;
                JSONArray jsonArray;
                try {

                    jsonObject = new JSONObject(response.body().string());
                    //Log.d(TAG, "onResponse: "+jsonObject.toString(4));

                    mSkip = jsonObject.getInt("skip");

                    if (mSkip == 0) {
                        mFeedDone = true; //no more feed to load
                        mFeedAdapter.setLoadState(LoadMoreViewHolder.STATE_END);
                    }

                    final ArrayList<BaseFeedItem> refreshedPosts = new ArrayList<>();

                    //get polls
                    try {
                        jsonArray = jsonObject.getJSONArray("polls");
                        for (int i = jsonArray.length() - 1; i >= 0; i++){
                            refreshedPosts.add(new Poll(jsonArray.getJSONObject(i)));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }


                    jsonArray = jsonObject.getJSONArray(mGlobalItem.type == GlobalChoiceItem.TYPE_TREND ? "posts" : "events");
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
                                            mFeedAdapter.notifyDataSetChanged();
                                        }
                                    });

                                    mProgressBar.setVisibility(View.GONE);

                                    if (mGlobalItem.type == GlobalChoiceItem.TYPE_HEADER_FRIEND &&
                                            refreshedPosts.isEmpty() && getView() != null){
                                        getView().findViewById(R.id.no_friends).setVisibility(View.VISIBLE);
                                    }
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
                                vEmptyView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        };
    }


    @Override
    protected void getMorePosts() {
        if (getFragmentState() == FragmentState.LOADING_DATA || getActivity() == null) return;
        setFragmentState(FragmentState.LOADING_DATA);

        int skip = mSkip;
        skip -= 20;
        int limit = 20;

        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        new LSDKEvents(getContext()).getEvents(getUrlPathEnding(), getParams(skip, limit), getMorePostsCallback(skip));
    }

    @Override
    public ArrayList<BaseFeedItem> getFeedArray() {
        return mPostList;
    }


    @Override
    protected void initAdapter() {
        if (mFeedAdapter == null) {
            mFeedAdapter = new TrendingItemAdapter(
                    mPostList,
                    getContext(),
                    Glide.with(this),
                    mGlobalItem
            );
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_trending;
    }
}
