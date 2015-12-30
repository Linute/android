package com.linute.linute.MainContent.DiscoverFragment;

import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.LinuteFragmentAdapter;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by QiFeng on 11/17/15.
 */
public class DiscoverFragment extends Fragment {


    private RecyclerView recList;
    private LinearLayoutManager llm;
    private EditText postBox;

    private SwipeRefreshLayout refreshLayout;


    private List<Post> mPosts;
    private ChoiceCapableAdapter<?> mCheckBoxChoiceCapableAdapters = null;

    //called when fragment drawn the first time
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_discover_feed, container, false); //setContent

        mPosts = new ArrayList<>();

        ((MainActivity) getActivity()).setTitle("My Campus");
        recList = (RecyclerView) rootView.findViewById(R.id.eventList);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext());
        recList.setAdapter(mCheckBoxChoiceCapableAdapters);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!Utils.isNetworkAvailable(getActivity())) {
                    ((MainActivity) getActivity()).noInternet();
                    refreshLayout.setRefreshing(false);
                    return;
                }
                getFeed();
            }
        });

        refreshLayout.setRefreshing(true);
        getFeed();
        if (!Utils.isNetworkAvailable(getActivity())) {
            ((MainActivity) getActivity()).noInternet();
            refreshLayout.setRefreshing(false);

        }

//        postBox = (EditText) rootView.findViewById(R.id.postBox);
//        postBox.setFocusable(false);
//        postBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((MainActivity) getActivity()).newPost();
//            }
//        });


//        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        recList.setOnScrollListener(new HidingScrollListener() {
//            @Override
//            public void onHide() {
//                hideViews();
//            }
//
//            @Override
//            public void onShow() {
//                showViews();
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                refreshLayout.setEnabled(firstVisibleItem == 0);
//            }
//        });

        recList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                refreshLayout.setEnabled(firstVisibleItem == 0);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Button but = (Button) getActivity().findViewById(R.id.button);
        /*
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            }

        });

        Button but2 = (Button)getActivity().findViewById(R.id.button2);
        but2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            }
        });*/

    }

    public void getFeed() {
        Map<String, String> events = new HashMap<String, String>();
        events.put("college", "564a46ff8ac4a559174247af");
        events.put("skip", "0");
        LSDKEvents events1 = new LSDKEvents(getActivity());
        events1.getEvents(events, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful())
                    Log.d("HEY", "STOP IT");

                mPosts.clear();
                String json = response.body().string();
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(json);
                    jsonArray = jsonObject.getJSONArray("events");
                    Post post = null;
                    String postImage = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        if (jsonObject.getJSONArray("images").length() > 0)
                            postImage = (String) jsonObject.getJSONArray("images").get(0);
                        post = new Post(
                                jsonObject.getJSONObject("owner").getString("fullName"),
                                jsonObject.getJSONObject("owner").getString("profileImage"),
                                jsonObject.getString("title"),
                                postImage,
                                jsonObject.getInt("privacy"),
                                jsonObject.getInt("numberOfLikes"),
                                jsonObject.getString("likeID"));

                        Log.d("TAG", post.getImage());
                        Log.d("TAG", post.getUserImage());
                        Log.d("TAG", post.getTitle());
                        Log.d("TAG", post.getNumLike() + "");
                        Log.d("TAG", post.getPrivacy() + "");
                        mPosts.add(post);
                        postImage = "";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing())
                            refreshLayout.setRefreshing(false);

//                        mCheckBoxChoiceCapableAdapters = new CheckBoxQuestionAdapter(mPosts, getContext());

                        mCheckBoxChoiceCapableAdapters.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void hideViews() {
        postBox.animate().translationY(-postBox.getHeight() - 50).setInterpolator(new AccelerateInterpolator(2)).start();
        postBox.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                postBox.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void showViews() {
        postBox.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        postBox.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                postBox.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


}
