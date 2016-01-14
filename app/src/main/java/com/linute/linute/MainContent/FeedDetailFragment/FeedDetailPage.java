package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arman on 1/11/16.
 */
public class FeedDetailPage extends DialogFragment {

    private static final String TAG = FeedDetail.class.getSimpleName();
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;
    private FeedDetail mFeedDetail;

    private LinuteUser user = new LinuteUser();
    private String mPrevTitle;
    private String mTaptPostId;
    private FeedDetailAdapter mFeedDetailAdapter;

    public FeedDetailPage() {
    }

    public static FeedDetailPage newInstance(
            String prevFragmentTitle,
            String taptUserPostId,
            String taptUserPostImage,
            String taptUserPostText,
            String taptUserImage,
            String taptUserName,
            int taptUserPrivacy,
            String taptUserPostTime,
            boolean taptUserPostLiked,
            String taptUserPostLikeNum) {
        FeedDetailPage fragment = new FeedDetailPage();
        Bundle args = new Bundle();
        args.putString("TITLE", prevFragmentTitle);
        args.putString("TAPTPOST", taptUserPostId);
        args.putString("TAPTIMAGE", taptUserPostImage);
        args.putString("TAPTTEXT", taptUserPostText);
        args.putString("TAPTUSERIMAGE", taptUserImage);
        args.putString("TAPTUSERNAME", taptUserName);
        args.putInt("TAPTUSERPRIVACY", taptUserPrivacy);
        args.putString("TAPTUSERPOSTTIME", taptUserPostTime);
        args.putBoolean("TAPTUSERPOSTLIKED", taptUserPostLiked);
        args.putString("TAPTUSERNAMEPOSTLIKENUM", taptUserPostLikeNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPrevTitle = getArguments().getString("TITLE");
            mTaptPostId = getArguments().getString("TAPTPOST");
            mFeedDetail = new FeedDetail(
                    getArguments().getString("TAPTIMAGE"),
                    getArguments().getString("TAPTTEXT"),
                    getArguments().getString("TAPTUSERIMAGE"),
                    getArguments().getString("TAPTUSERNAME"),
                    getArguments().getInt("TAPTUSERPRIVACY"),
                    getArguments().getString("TAPTUSERPOSTTIME"),
                    getArguments().getBoolean("TAPTUSERPOSTLIKED"),
                    getArguments().getString("TAPTUSERNAMEPOSTLIKENUM"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        mUser = new LSDKUser(getActivity());
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);


        recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mFeedDetailAdapter = new FeedDetailAdapter(mFeedDetail, getActivity());
        recList.setAdapter(mFeedDetailAdapter);

//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                updateAndSetHeader();
//                setActivities(); //get activities
//
//            }
//        });

//        updateAndSetHeader();
//        setActivities(); //get activities

        displayComments();

        return rootView;
    }

    private void displayComments() {
        Map<String, String> event = new HashMap<>();
        event.put("event", mTaptPostId);
        event.put("skip", "0");
        LSDKEvents event1 = new LSDKEvents(getActivity());
        event1.getComments(event, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                Toast.makeText(getActivity(), "Couldn't access server", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "STOP IT - onFailure");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "STOP IT - !onResponse");
//                    Toast.makeText(getActivity(), "Oops, looks like something went wrong", Toast.LENGTH_SHORT).show();

                }
                Log.d(TAG, response.body().string());
            }
        });
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void onBackPressed() {
        ((MainActivity) getActivity()).setTitle(mPrevTitle);
        this.dismiss();
    }
}
