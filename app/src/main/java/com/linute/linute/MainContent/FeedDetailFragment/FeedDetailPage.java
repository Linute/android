package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Arman on 1/11/16.
 */
public class FeedDetailPage extends UpdatableFragment {

    private static final String TAG = FeedDetail.class.getSimpleName();
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;
    private FeedDetail mFeedDetail;

    private LinuteUser user = new LinuteUser();
    private boolean mIsImage;
    private String mTaptPostId;
    private String mTaptPostUserId;
    private FeedDetailAdapter mFeedDetailAdapter;
    private String mCommentText;
    private EditText mCommentEditText;

    public FeedDetailPage() {
    }

    public static FeedDetailPage newInstance(
            boolean isImage,
            String taptUserPostId,
            String taptPostUserId) {
        FeedDetailPage fragment = new FeedDetailPage();
        Bundle args = new Bundle();
        args.putBoolean("TITLE", isImage);
        args.putString("TAPTPOST", taptUserPostId);
        args.putString("TAPTPOSTUSERID", taptPostUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsImage = getArguments().getBoolean("TITLE");
            mTaptPostId = getArguments().getString("TAPTPOST");
            mTaptPostUserId = getArguments().getString("TAPTPOSTUSERID");

            mFeedDetail = new FeedDetail();
            mFeedDetail.setPostId(mTaptPostId);
            mFeedDetail.setPostUserId(mTaptPostUserId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_feed_detail_page, container, false);

        mUser = new LSDKUser(getActivity());
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        recList = (RecyclerView) rootView.findViewById(R.id.feed_detail_recyc);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mFeedDetailAdapter = new FeedDetailAdapter(mFeedDetail, getActivity());
        recList.setAdapter(mFeedDetailAdapter);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        recList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rootView.findViewById(R.id.comment_field).clearFocus();
                imm.hideSoftInputFromWindow(rootView.findViewById(R.id.comment_field).getWindowToken(), 0);
                return false;
            }
        });

        rootView.findViewById(R.id.feed_detail_send_comment).setEnabled(false);

        mCommentEditText = (EditText) rootView.findViewById(R.id.comment_field);
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    rootView.findViewById(R.id.feed_detail_send_comment).setEnabled(true);
                    Log.d(TAG, "onEditorAction: " + s.toString());
                    mCommentText = s.toString();
                } else {
                    rootView.findViewById(R.id.feed_detail_send_comment).setEnabled(false);
                }
            }
        });

        mCommentEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recList.smoothScrollToPosition(mFeedDetailAdapter.getItemCount() - 1);
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });

//        ((EditText) rootView.findViewById(R.id.comment_field)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (!v.getText().toString().equals("")) {
//                    rootView.findViewById(R.id.feed_detail_send_comment).setEnabled(true);
//                    Log.d(TAG, "onEditorAction: " + v.getText().toString());
//                    mCommentText = v.getText().toString();
//                } else {
//                    rootView.findViewById(R.id.feed_detail_send_comment).setEnabled(false);
//                }
//                return false;
//            }
//        });

        rootView.findViewById(R.id.feed_detail_send_comment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LSDKEvents lsdkEvents = new LSDKEvents(getActivity());
                Map<String, Object> comment = new HashMap<String, Object>();
                JSONArray jsonArray = new JSONArray();
                comment.put("image", jsonArray);
                comment.put("text", mCommentText);
                comment.put("event", mTaptPostId);
                lsdkEvents.postComment(comment, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.d(TAG, "onFailure: " + request.body().toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d(TAG, "onResponseNotSuccessful: " + response.body().string());
                        } else {
                            Log.d(TAG, "onResponseSuccessful: " + response.body().string());

                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCommentEditText.setText("");
                                    displayCommentsAndPost();
                                }
                            });
                        }
                    }
                });

            }
        });

//        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                updateAndSetHeader();
//                setActivities(); //get activities
//
//            }
//        });



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseTaptActivity activity = (BaseTaptActivity)getActivity();
        if (activity != null){
            activity.setTitle(mIsImage ? "Image" : "Status");
            activity.resetToolbar();
        }

        //only updates first time it is created
        if (fragmentNeedsUpdating()){
            displayCommentsAndPost();
            setFragmentNeedUpdating(false);
        }

    }

    private void displayCommentsAndPost() {
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

                    String imageName = jsonObject.getJSONArray("images").length() != 0 ? jsonObject.getJSONArray("images").getString(0) : "";

                    mFeedDetail.setFeedDetail(
                            imageName,
                            jsonObject.getString("title"),
                            jsonObject.getJSONObject("owner").getString("profileImage"),
                            jsonObject.getJSONObject("owner").getString("fullName"),
                            Integer.parseInt(jsonObject.getString("privacy")),
                            postString,
                            !jsonObject.getString("likeID").equals(""),
                            jsonObject.getString("numberOfLikes"),
                            jsonObject.getString("likeID"));
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                if ( getActivity() == null) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFeedDetailAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        Map<String, String> eventComments = new HashMap<>();
        eventComments.put("event", mTaptPostId);
        eventComments.put("skip", "0");
        event.getComments(eventComments, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: " + request.body().toString());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.body().string());
                }
                JSONObject jsonObject;
                JSONArray comments;
                mFeedDetail.getComments().clear();
                try {
                    jsonObject = new JSONObject(response.body().string());
                    comments = jsonObject.getJSONArray("comments");
                    for (int i = 0; i < comments.length(); i++) {
                        Log.d(TAG, "onResponse: " + comments.get(i).toString());
                        mFeedDetail.getComments()
                                .add(new Comment(
                                        ((JSONObject) comments.get(i)).getJSONObject("owner").getString("id"),
                                        ((JSONObject) comments.get(i)).getJSONObject("owner").getString("profileImage"),
                                        ((JSONObject) comments.get(i)).getJSONObject("owner").getString("fullName"),
                                        ((JSONObject) comments.get(i)).getString("text"), ((JSONObject) comments.get(i)).getString("id")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFeedDetailAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }





}
