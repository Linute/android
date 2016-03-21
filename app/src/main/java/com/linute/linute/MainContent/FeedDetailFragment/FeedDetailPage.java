package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linkedin.android.spyglass.mentions.MentionSpan;
import com.linkedin.android.spyglass.suggestions.SuggestionsResult;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener;
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager;
import com.linkedin.android.spyglass.tokenization.QueryToken;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizer;
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig;
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver;
import com.linkedin.android.spyglass.ui.MentionsEditText;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.API.LSDKFriends;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.linute.linute.UtilsAndHelpers.VideoClasses.SingleVideoPlaybackManager;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Arman on 1/11/16.
 */

public class FeedDetailPage extends UpdatableFragment implements QueryTokenReceiver, SuggestionsResultListener, SuggestionsVisibilityManager {

    private static final String TAG = FeedDetail.class.getSimpleName();
    private RecyclerView recList;

    private FeedDetail mFeedDetail;

    private FeedDetailAdapter mFeedDetailAdapter;
    private MentionsEditText mCommentEditText;

    private View mAnonCheckBoxContainer;

    private View mSendButtonContainer;

    private RecyclerView mMentionedList;

    private SharedPreferences mSharedPreferences;

    private MentionedPersonAdapter mMentionedPersonAdapter;

    private View mProgressbar;
    private View mSendButton;

    private SingleVideoPlaybackManager mSingleVideoPlaybackManager = new SingleVideoPlaybackManager();

    private CheckBox mCheckBox;


    // feedDetail is divided into 2 parts, the header and the comments. We load them seperatly so one might
    // load before the other. i will call notifydataset changed after both have loaded
    private boolean mOtherHasUpdated = false;


    public FeedDetailPage() {
    }


    public static FeedDetailPage newInstance(Post post) {
        FeedDetailPage fragment = new FeedDetailPage();
        Bundle args = new Bundle();
        args.putParcelable("POST", post);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFeedDetail = new FeedDetail((Post) getArguments().getParcelable("POST"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_feed_detail_page, container, false);

        setHasOptionsMenu(true);

        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        recList = (RecyclerView) rootView.findViewById(R.id.feed_detail_recyc);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new CustomLinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mFeedDetailAdapter = new FeedDetailAdapter(mFeedDetail, getActivity(), mSingleVideoPlaybackManager);

        recList.setAdapter(mFeedDetailAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        mMentionedList = (RecyclerView) rootView.findViewById(R.id.feed_detail_mentions);
        mMentionedList.setLayoutManager(manager);
        mMentionedList.setHasFixedSize(true);
        mMentionedList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        mMentionedPersonAdapter = new MentionedPersonAdapter(new ArrayList<MentionedPerson>());
        mMentionedList.setAdapter(mMentionedPersonAdapter);


        mSendButtonContainer = rootView.findViewById(R.id.comment_send_button_container);
        mSendButtonContainer.setEnabled(false);

        mCommentEditText = (MentionsEditText) rootView.findViewById(R.id.comment_field);
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().trim().equals("")) {
                    mSendButtonContainer.setEnabled(true);
                    mSendButton.setAlpha((float) 1);
                } else {
                    mSendButtonContainer.findViewById(R.id.comment_send_button_container).setEnabled(false);
                    mSendButton.setAlpha((float) 0.25);
                }
            }
        });

        mCommentEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction())
                    //recList.scrollToPosition(mFeedDetailAdapter.getItemCount() - 1);
                    recList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recList.smoothScrollToPosition(mFeedDetailAdapter.getItemCount() - 1);
                        }
                    }, 500);

                return false;
            }
        });

        mCommentEditText.setTokenizer(new WordTokenizer(new WordTokenizerConfig.Builder().setMaxNumKeywords(4).setThreshold(2).build()));
        mCommentEditText.setQueryTokenReceiver(this);
        mCommentEditText.setSuggestionsVisibilityManager(this);

        mFeedDetailAdapter.setMentionedTextAdder(new FeedDetailAdapter.MentionedTextAdder() {
            @Override
            public void addMentionedPerson(MentionedPerson person) {
                mCommentEditText.append(" @");
                mCommentEditText.insertMention(person);
                mCommentEditText.requestFocus();
            }
        });


        mAnonCheckBoxContainer = rootView.findViewById(R.id.comment_checkbox_container);

        mCheckBox = (CheckBox) mAnonCheckBoxContainer.findViewById(R.id.comment_anon_checkbox);

        mAnonCheckBoxContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) mAnonCheckBoxContainer.findViewById(R.id.comment_anon_checkbox_text);
                if (mCheckBox.isChecked()) {

                    text.setText("OFF");
                    text.setTextColor(ContextCompat.getColor(getActivity(), R.color.twentyfive_black));
                    mCheckBox.setChecked(false);

                } else {

                    text.setText("ON");
                    text.setTextColor(ContextCompat.getColor(getActivity(), R.color.twentyfive_black));
                    mCheckBox.setChecked(true);
                }
            }
        });


        mProgressbar = rootView.findViewById(R.id.comment_progressbar);
        mSendButton = rootView.findViewById(R.id.feed_detail_send_comment);

        rootView.findViewById(R.id.comment_send_button_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.setTitle("Comments");
            activity.resetToolbar();
            activity.enableBarScrolling(false);
            activity.setToolbarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recList != null && mMentionedList.getVisibility() != View.VISIBLE)
                        recList.smoothScrollToPosition(0);
                }
            });

            //user -- user Id
            //room -- id of event

            JSONObject joinParam = new JSONObject();

            try {
                joinParam.put("room", mFeedDetail.getPostId());
                joinParam.put("user", mSharedPreferences.getString("userID", ""));
                activity.emitSocket(API_Methods.VERSION + ":comments:joined", joinParam);
            } catch (JSONException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Utils.showServerErrorToast(getActivity());
                }
            }

            activity.connectSocket("new comment", newComment);
            activity.setSocketErrorResponse(new BaseTaptActivity.SocketErrorResponse() {
                @Override
                public void runSocketError() {
                    mProgressbar.setVisibility(View.GONE);
                    mSendButton.setVisibility(View.VISIBLE);
                }
            });

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID", ""));
                obj.put("action", "active");
                obj.put("screen", "Details");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //only updates first time it is created
        if (fragmentNeedsUpdating()) {
            mOtherHasUpdated = false;
            displayCommentsAndPost();
            setFragmentNeedUpdating(false);
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        mSingleVideoPlaybackManager.stopPlayback();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity != null) {

            JSONObject leaveParam = new JSONObject();
            try {
                leaveParam.put("room", mFeedDetail.getPostId());
                leaveParam.put("user", mSharedPreferences.getString("userID", ""));
                activity.emitSocket(API_Methods.VERSION + ":comments:left", leaveParam);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mSharedPreferences.getString("userID", ""));
                obj.put("action", "inactive");
                obj.put("screen", "Details");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            activity.disconnectSocket("new comment", newComment);
            activity.setSocketErrorResponse(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mCommentEditText.getWindowToken(), 0);

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.enableBarScrolling(true);
            activity.setToolbarOnClickListener(null);
        }
    }

    private void displayCommentsAndPost() {
        if (getActivity() == null) return;

        LSDKEvents event = new LSDKEvents(getActivity());
        event.getEventWithId(mFeedDetail.getPostId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
                    Log.d(TAG, response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                    return;
//                    Toast.makeText(getActivity(), "Oops, looks like something went wrong", Toast.LENGTH_SHORT).show();

                }
                JSONObject jsonObject;

                String res = response.body().string();
               // Log.i(TAG, "onResponse: " + res);
                try {
                    jsonObject = new JSONObject(res);

                    mFeedDetail.setPostPrivacy(jsonObject.getInt("privacy"));
                    mFeedDetail.setIsPostLiked(jsonObject.getBoolean("isLiked"));
                    mFeedDetail.setNumComments(jsonObject.getInt("numberOfComments"));
                    mFeedDetail.setPostLikeNum(jsonObject.getInt("numberOfLikes") + "");
                    String anonImage = jsonObject.getString("anonymousImage");
                    mFeedDetail.setAnonImage(anonImage == null || anonImage.equals("") ? "" : Utils.getAnonImageUrl(anonImage));

                    JSONObject owner = jsonObject.getJSONObject("owner");

                    mFeedDetail.getPost().setUserName(owner.getString("fullName"));
                    mFeedDetail.getPost().setProfileImage(Utils.getImageUrlOfUser(owner.getString("profileImage")));

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

                if (getActivity() == null) return;


                if (mOtherHasUpdated) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFeedDetailAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    mOtherHasUpdated = true;
                }
            }

        });

        Map<String, String> eventComments = new HashMap<>();
        eventComments.put("event", mFeedDetail.getPostId());
        eventComments.put("skip", "0");

        event.getComments(eventComments, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
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
                            Log.d(TAG, "onResponse: " + response.body().string());
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showServerErrorToast(getActivity());
                                    }
                                });
                            }
                            return;
                        }
                        JSONObject jsonObject;
                        JSONArray comments;
                        JSONArray mentionedPeople;

                        ArrayList<Comment> tempComments = new ArrayList<>();

                        try {
                            jsonObject = new JSONObject(response.body().string());
                            comments = jsonObject.getJSONArray("comments");

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            Date myDate;

                            for (int i = 0; i < comments.length(); i++) {
                                //Log.i(TAG, "onResponse: "+comments.getJSONObject(i).toString());

                                //get date
                                try {
                                    myDate = simpleDateFormat.parse(comments.getJSONObject(i).getString("date"));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    myDate = null;
                                }

                                List<Comment.MentionedPersonLight> mentionedPersonLightArrayList = new ArrayList<>();
                                mentionedPeople = comments.getJSONObject(i).getJSONArray("mentions");

                                for (int j = 0; j < mentionedPeople.length(); j++) { //get all the mentioned people
                                    mentionedPersonLightArrayList.add(
                                            new Comment.MentionedPersonLight( //just need fullname and id
                                                    mentionedPeople.getJSONObject(j).getString("fullName"),
                                                    mentionedPeople.getJSONObject(j).getString("id")
                                            )
                                    );
                                }

                                tempComments
                                        .add(new Comment(
                                                        ((JSONObject) comments.get(i)).getJSONObject("owner").getString("id"),
                                                        ((JSONObject) comments.get(i)).getJSONObject("owner").getString("profileImage"),
                                                        ((JSONObject) comments.get(i)).getJSONObject("owner").getString("fullName"),
                                                        ((JSONObject) comments.get(i)).getString("text"),
                                                        ((JSONObject) comments.get(i)).getString("id"),
                                                        ((JSONObject) comments.get(i)).getInt("privacy") == 1,
                                                        ((JSONObject) comments.get(i)).getString("anonymousImage"),
                                                        mentionedPersonLightArrayList,
                                                        myDate == null ? 0 : myDate.getTime()
                                                )
                                        );
                            }

                            if (comments.length() == 0) {
                                tempComments.add(null);
                            }

                            mFeedDetail.getComments().clear();
                            mFeedDetail.getComments().addAll(tempComments);

                            if (getActivity() == null) return;

                            mCommentsRetrieved = true;

                            if (mOtherHasUpdated) {
                                getActivity().runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mFeedDetailAdapter.notifyDataSetChanged();
                                            }
                                        }
                                );
                            } else {
                                mOtherHasUpdated = true;
                            }
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
                    }
                }

        );
    }


    private void setCommentViewEditable(boolean editable) {
        if (editable) {
            mCommentEditText.setFocusableInTouchMode(true);
            mAnonCheckBoxContainer.setEnabled(true);
        } else {
            mCommentEditText.setFocusable(false);
            mAnonCheckBoxContainer.setEnabled(false);
        }
    }

    private Menu mFeedDetailMenu;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() != null) {

            boolean isOwner = mFeedDetail.getPostUserId().equals(mSharedPreferences.getString("userID", ""));
            inflater.inflate(isOwner ? R.menu.feed_detail_delete_toolbar : R.menu.feed_detail_report_toolbar, menu);
            mFeedDetailMenu = menu;

            if (isOwner) { //set correct titles
                menu.findItem(R.id.feed_detail_reveal).setTitle(mFeedDetail.isAnon() ? "Reveal post" : "Make anonymous");
            }else {
                menu.findItem(R.id.feed_detail_hide_post).setTitle(mFeedDetail.getPost().isPostHidden() ? "Unhide post" : "Hide post");
                menu.findItem(R.id.feed_detail_mute_post).setTitle(mFeedDetail.getPost().isPostMuted() ? "Unmute post" : "Mute post");
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.feed_detail_report:
                showReportOptionsDialog();
                return true;
            case R.id.feed_detail_delete:
                showConfirmDeleteDialog();
                return true;
            case R.id.feed_detail_reveal:
                showRevealConfirm();
                return true;
            case R.id.feed_detail_mute_post:
                showMuteConfirmation();
                return true;
            case R.id.feed_detail_hide_post:
                showHideConfirmation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showReportOptionsDialog() {
        if (getActivity() == null) return;
        final CharSequence options[] = new CharSequence[]{"Spam", "Inappropriate", "Harassment"};
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Report As")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reportEvent(which);
                    }
                })
                .create();
        alert.show();

    }


    private void reportEvent(final int reason) {
        if (getActivity() == null) return;

        new LSDKEvents(getActivity()).reportEvent(reason, mFeedDetail.getPostId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
                if (response.isSuccessful()) {
                    response.body().close();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Post reported", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
            }
        });
    }


    private void showConfirmDeleteDialog() {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost();
                    }
                })
                .show();
    }

    private void deletePost() {
        if (getActivity() == null) return;

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Deleting", true);

        new LSDKEvents(getActivity()).deleteEvent(mFeedDetail.getPostId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            progressDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();

                    final BaseTaptActivity activity = (BaseTaptActivity) getActivity();

                    if (activity == null) return;

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(activity, "Post deleted", Toast.LENGTH_SHORT).show();
                            activity.setFragmentOfIndexNeedsUpdating(true, MainActivity.FRAGMENT_INDEXES.FEED);
                            getFragmentManager().popBackStack();
                        }
                    });

                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                progressDialog.dismiss();
                            }
                        });
                    }
                }
            }
        });
    }


    public void showRevealConfirm() {
        if (getActivity() == null) return;
        boolean isAnon = mFeedDetail.getPostPrivacy() == 1;
        new AlertDialog.Builder(getActivity())
                .setTitle(isAnon ? "Reveal" : "Hide")
                .setMessage(isAnon ? "Are you sure you want to turn anonymous off for this post?" : "Are you sure you want to make this post anonymous?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        revealPost();
                    }
                })
                .show();
    }


    public void showMuteConfirmation() {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle(mFeedDetail.getPost().isPostMuted() ? "Unsilence" : "Silence")
                .setMessage(mFeedDetail.getPost().isPostMuted() ? "This will turn on future notifications for this post." : "This will turn off future notifications for any activity on this post.")
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleMute();
                    }
                })
                .setNegativeButton("no, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void toggleMute() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity == null) return;

        if (!Utils.isNetworkAvailable(activity) || !activity.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isMuted = mFeedDetail.getPost().isPostMuted();
        mFeedDetail.getPost().setPostMuted(!isMuted);

        mFeedDetailMenu.findItem(R.id.feed_detail_mute_post).setTitle(isMuted ? "Mute post" : "Unmute post");

        JSONObject emit = new JSONObject();
        try {
            emit.put("mute", !isMuted);
            emit.put("room", mFeedDetail.getPostId());
            activity.emitSocket(API_Methods.VERSION + ":posts:mute", emit);
            Toast.makeText(activity,
                    isMuted ? "Unmuted post" : "Muted post",
                    Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }


    public void showHideConfirmation() {
        if (getActivity() == null) return;
        new AlertDialog.Builder(getActivity())
                .setTitle(mFeedDetail.getPost().isPostHidden() ? "Unhide post" : "Hide it"  )
                .setMessage(mFeedDetail.getPost().isPostHidden() ? "This will make this post viewable on your feed. Still want to go ahead with it?" : "This will remove this post from your feed. Still want to go ahead with it?")
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleHide();
                    }
                })
                .setNegativeButton("no, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void toggleHide() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity == null) return;

        if (!Utils.isNetworkAvailable(activity) || !activity.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isHidden = mFeedDetail.getPost().isPostHidden();
        mFeedDetail.getPost().setPostHidden(!isHidden);

        mFeedDetailMenu.findItem(R.id.feed_detail_hide_post).setTitle(isHidden ? "Hide post" : "Unhide post" );

        JSONObject emit = new JSONObject();
        try {
            emit.put("hide", !isHidden);
            emit.put("room", mFeedDetail.getPostId());
            activity.emitSocket(API_Methods.VERSION + ":posts:hide", emit);
            Toast.makeText(activity,
                    isHidden ? "Post unhidden" : "Post hidden",
                    Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }


    private void revealPost() {
        final boolean isAnon = mFeedDetail.getPostPrivacy() == 1;

        mFeedDetail.setPostPrivacy(isAnon ? 0 : 1);
        mFeedDetailAdapter.notifyItemChanged(0);

        mFeedDetailMenu.findItem(R.id.feed_detail_reveal).setTitle(isAnon ? "Make anonymous" : "Reveal post");


        new LSDKEvents(getActivity()).revealEvent(mFeedDetail.getPostId(), !isAnon, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mFeedDetail.setPostPrivacy(isAnon ? 0 : 1);
                            mFeedDetailAdapter.notifyItemChanged(0);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mFeedDetail.setPostPrivacy(isAnon ? 0 : 1);
                                mFeedDetailAdapter.notifyItemChanged(0);
                            }
                        });
                    }
                }
            }
        });
    }



    /* MENTIONS CODE */

    private final String BUCKET = "MENTIONS";
    private Handler mSearchHandler = new Handler();
    private String mQueryString = "";
    private boolean mDisplayList = false;

    private Runnable mSearchRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;
            new LSDKFriends(getActivity()).getFriendsForMention(mSharedPreferences.getString("userID", ""), mQueryString, "0", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {

                            String res = response.body().string();
                            //Log.i(TAG, "onResponse: " + res);

                            JSONArray friends = new JSONObject(res).getJSONArray("friends");

                            final ArrayList<MentionedPerson> personList = new ArrayList<>();

                            for (int i = 0; i < friends.length(); i++) {
                                try {
                                    personList.add(
                                            new MentionedPerson(
                                                    friends.getJSONObject(i).getJSONObject("user").getString("fullName"),
                                                    friends.getJSONObject(i).getJSONObject("user").getString("id"),
                                                    friends.getJSONObject(i).getJSONObject("user").getString("profileImage")
                                            )
                                    );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMentionedPersonAdapter = new MentionedPersonAdapter(personList);
                                    mMentionedList.swapAdapter(mMentionedPersonAdapter, true);
                                    if (mDisplayList) {
                                        displaySuggestions(!personList.isEmpty());
                                    } else if (personList.isEmpty()) {
                                        displaySuggestions(false);
                                    }

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }


                    } else {
                        Log.e(TAG, "onResponse: " + response.body().string());
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
            });
        }
    };

    @Override
    public List<String> onQueryReceived(@NonNull QueryToken queryToken) {
        mSearchHandler.removeCallbacks(mSearchRunnable);
        mDisplayList = false;

        if (queryToken.isExplicit()) {
            String text = queryToken.getKeywords(); //words inputted

            if (text.length() > 0) {
                mDisplayList = true;
                mQueryString = text;
                mSearchHandler.postDelayed(mSearchRunnable, 350);
            }
        }
        return Collections.singletonList(BUCKET);
    }

    @Override
    public void onReceiveSuggestionsResult(@NonNull SuggestionsResult result, @NonNull String bucket) {
    }

    @Override
    public void displaySuggestions(boolean display) {
        mMentionedList.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isDisplayingSuggestions() {
        return mMentionedList.getVisibility() == View.VISIBLE;
    }


    private class MentionedPersonAdapter extends RecyclerView.Adapter<MentionedPersonAdapter.MentionedPersonViewHolder> {

        private ArrayList<MentionedPerson> mMentionedPersons;

        public MentionedPersonAdapter(ArrayList<MentionedPerson> personList) {
            mMentionedPersons = personList;
        }

        @Override
        public MentionedPersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MentionedPersonViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_mentioned_person, parent, false));
        }

        @Override
        public void onBindViewHolder(MentionedPersonViewHolder holder, final int position) {
            holder.bindView(mMentionedPersons.get(position));
        }

        @Override
        public int getItemCount() {
            return mMentionedPersons.size();
        }

        public class MentionedPersonViewHolder extends RecyclerView.ViewHolder {

            public CircleImageView mProfileImageView;
            public TextView mName;

            public MentionedPersonViewHolder(View itemView) {
                super(itemView);
                mProfileImageView = (CircleImageView) itemView.findViewById(R.id.mentioned_person_profile_image);
                mName = (TextView) itemView.findViewById(R.id.mentioned_person_name);
            }

            public void bindView(final MentionedPerson person) {


                mName.setText(person.getFullname());
                Glide.with(mProfileImageView.getContext())
                        .load(Utils.getImageUrlOfUser(person.getProfileImage()))
                        .asBitmap()
                        .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                        .placeholder(R.drawable.image_loading_background)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                        .into(mProfileImageView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCommentEditText.insertMention(person);
                        mMentionedList.swapAdapter(new MentionedPersonAdapter(new ArrayList<MentionedPerson>()), true);
                        displaySuggestions(false);
                        mSearchHandler.removeCallbacks(mSearchRunnable);
                        mCommentEditText.requestFocus();
                    }
                });

            }


        }
    }

    private boolean mCommentsRetrieved = false;

    private Emitter.Listener newComment = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            if (!mCommentsRetrieved || !mOtherHasUpdated) return;

            JSONObject object = (JSONObject) args[0];

            if (object == null) {
                Log.i(TAG, "call: Error retrieving new comment");
                return;
            }

            try {

                Date myDate;
                SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                myDate = fm.parse(object.getString("date"));

                List<Comment.MentionedPersonLight> mentionedPersonLightArrayList = new ArrayList<>();
                JSONArray mentionedPeople = object.getJSONArray("mentions");

                for (int j = 0; j < mentionedPeople.length(); j++) { //get all the mentioned people
                    mentionedPersonLightArrayList.add(
                            new Comment.MentionedPersonLight( //just need fullname and id
                                    mentionedPeople.getJSONObject(j).getString("fullName"),
                                    mentionedPeople.getJSONObject(j).getString("id")
                            )
                    );
                }

                if (!mFeedDetail.getComments().isEmpty() && mFeedDetail.getComments().get(0) == null) {
                    mFeedDetail.getComments().clear();
                }

                final Comment com = new Comment(
                        object.getJSONObject("owner").getString("id"),
                        object.getJSONObject("owner").getString("profileImage"),
                        object.getJSONObject("owner").getString("fullName"),
                        object.getString("text"),
                        object.getString("id"),
                        object.getInt("privacy") == 1,
                        object.getString("anonymousImage"),
                        mentionedPersonLightArrayList,
                        myDate.getTime()
                );

                mFeedDetail.getComments().add(com);

                mFeedDetail.refreshCommentCount();


                if (getActivity() != null) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean smoothScroll = false;
                            if (com.getCommentUserId().equals(mSharedPreferences.getString("userID", ""))) { //was the user that posted the comment
                                mCommentEditText.setText("");
                                mSendButton.setVisibility(View.VISIBLE);
                                mProgressbar.setVisibility(View.GONE);
                                setCommentViewEditable(true);
                                smoothScroll = true;
                            }

                            //because of header we can use size, change if decide to add it to array
                            mFeedDetailAdapter.notifyDataSetChanged();
                            if (smoothScroll)
                                recList.smoothScrollToPosition(mFeedDetail.getComments().size());

                        }
                    });
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    };


    private void sendComment() {

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();


        String commentText = mCommentEditText.getText().toString().trim();

        if (commentText.isEmpty() || getActivity() == null || mFeedDetail.getPostId() == null || activity == null)
            return;

        if (!activity.socketConnected() || !Utils.isNetworkAvailable(activity)) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        try {
            JSONObject comment = new JSONObject();
            comment.put("user", mSharedPreferences.getString("userID", ""));
            comment.put("text", commentText);
            comment.put("room", mFeedDetail.getPostId());
            comment.put("privacy", mCheckBox.isChecked() ? 1 : 0);

            //add people mentioned in comment
            List<MentionSpan> spanList = mCommentEditText.getMentionsText().getMentionSpans();
            JSONArray mentions = new JSONArray();

            if (!spanList.isEmpty()) {
                for (MentionSpan s : spanList) {
                    mentions.put(((MentionedPerson) s.getMention()).getUserId()); //add user ids
                }
            }

            comment.put("mentions", mentions);

            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mCommentEditText.getWindowToken(), 0);

            setCommentViewEditable(false);
            mSendButton.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);


            activity.emitSocket(API_Methods.VERSION + ":comments:new comment", comment);
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(getActivity());
        }
    }
}
