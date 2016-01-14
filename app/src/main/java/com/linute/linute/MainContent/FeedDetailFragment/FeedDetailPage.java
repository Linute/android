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
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
            String taptUserPostId) {
        FeedDetailPage fragment = new FeedDetailPage();
        Bundle args = new Bundle();
        args.putString("TITLE", prevFragmentTitle);
        args.putString("TAPTPOST", taptUserPostId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPrevTitle = getArguments().getString("TITLE");
            mTaptPostId = getArguments().getString("TAPTPOST");
            mFeedDetail = new FeedDetail();
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
        LSDKEvents event = new LSDKEvents(getActivity());
        event.getEventWithId(mTaptPostId, new Callback() {
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
                JSONObject jsonObject = null;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date myDate;
                String postString;
                try {
                    jsonObject = new JSONObject(response.body().string());

                    myDate = simpleDateFormat.parse(jsonObject.getString("date"));
                    postString = Utils.getEventTime(myDate);

                    mFeedDetail.setFeedDetail(
                            jsonObject.getJSONArray("images").getString(0),
                            jsonObject.getString("title"),
                            jsonObject.getJSONObject("owner").getString("profileImage"),
                            jsonObject.getJSONObject("ownder").getString("fullName"),
                            Integer.parseInt(jsonObject.getString("privacy")),
                            postString,
                            !jsonObject.getString("likeID").equals(""),
                            jsonObject.getString("numberOfLikes"));
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFeedDetailAdapter.notifyDataSetChanged();
                    }
                });
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
