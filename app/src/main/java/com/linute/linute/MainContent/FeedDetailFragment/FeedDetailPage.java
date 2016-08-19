package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CameraType;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

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

import static android.app.Activity.RESULT_OK;

/**
 * Created by Arman on 1/11/16.
 */

public class FeedDetailPage extends BaseFragment implements QueryTokenReceiver,
        SuggestionsResultListener, SuggestionsVisibilityManager, FeedDetailAdapter.CommentActions {

    private static final int CAMERA_REQUEST = 65;
    private static final String TAG = FeedDetail.class.getSimpleName();
    private RecyclerView recList;

    private FeedDetail mFeedDetail;

    private FeedDetailAdapter mFeedDetailAdapter;
    private MentionsEditText mCommentEditText;

    private View mAnonCheckBoxContainer;

    private RecyclerView mMentionedList;

    private MentionedPersonAdapter mMentionedPersonAdapter;

    private View mProgressbar;
    private ToggleImageView mSendButton;

    private CheckBox mCheckBox;

    private String mViewId;
    private String mImageSignature;

    private Handler mHandler = new Handler();

    private int mSkip = 0;

    private AlertDialog mAlertDialog;

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

        SharedPreferences pref = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mImageSignature = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000");
        mViewId = pref.getString("userID", "");

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.feed_detail_toolbar);
        toolbar.setTitle("Comments");
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recList != null && mMentionedList.getVisibility() != View.VISIBLE)
                    recList.smoothScrollToPosition(0);
            }
        });
        toolbar.inflateMenu(R.menu.feed_detail_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.feed_detail_options) {
                    if (mCommentsRetrieved)
                        showOptionsDialog();
                    return true;
                }
                return false;
            }
        });

        mProgressbar = rootView.findViewById(R.id.comment_progressbar);
        mSendButton = (ToggleImageView) rootView.findViewById(R.id.feed_detail_send_comment);
        mSendButton.setImageViews(R.drawable.ic_upload_picture, R.drawable.ic_send);

        //comments recyclerview
        recList = (RecyclerView) rootView.findViewById(R.id.feed_detail_recyc);
        final LinearLayoutManager llm = new CustomLinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mFeedDetailAdapter = new FeedDetailAdapter(mFeedDetail, getActivity(), Glide.with(this));
        mFeedDetailAdapter.setCommentActions(this);
        recList.setAdapter(mFeedDetailAdapter);
        mFeedDetailAdapter.setLoadMoreCommentsRunnable(new Runnable() {
            @Override
            public void run() {
                loadMoreComments();
            }
        });

        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //when passing last item and scrolling upward
                if (llm.findLastCompletelyVisibleItemPosition() == RecyclerView.NO_POSITION && dy < 0) {
                    //close keyboard
                    showKeyboard(recyclerView, false);
                }
            }

        });


        //mention recyclerview
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mMentionedList = (RecyclerView) rootView.findViewById(R.id.feed_detail_mentions);
        mMentionedList.setLayoutManager(manager);
        mMentionedList.setHasFixedSize(true);
        mMentionedPersonAdapter = new MentionedPersonAdapter(new ArrayList<MentionedPerson>());
        mMentionedList.setAdapter(mMentionedPersonAdapter);

        mCommentEditText = (MentionsEditText) rootView.findViewById(R.id.comment_field);
        mCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (llm != null) {
                        llm.scrollToPosition(llm.getItemCount() - 1);
                    }
                }
            }
        });
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mSendButton.setActive(!s.toString().trim().isEmpty());
            }
        });

//        mCommentEditText.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (MotionEvent.ACTION_UP == event.getAction())
//                    recList.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            recList.smoothScrollToPosition(mFeedDetailAdapter.getItemCount() - 1);
//                        }
//                    }, 500);
//
//                return false;
//            }
//        });

        mCommentEditText.setTokenizer(new WordTokenizer(new WordTokenizerConfig.Builder().setMaxNumKeywords(4).setThreshold(2).build()));
        mCommentEditText.setQueryTokenReceiver(this);
        mCommentEditText.setSuggestionsVisibilityManager(this);

        mFeedDetailAdapter.setMentionedTextAdder(new FeedDetailAdapter.MentionedTextAdder() {
            @Override
            public void addMentionedPerson(MentionedPerson person) {
                mCommentEditText.append(" @");
                mCommentEditText.insertMention(person);
                mCommentEditText.requestFocus();
                showKeyboard(mCommentEditText, true);
            }
        });

        mAnonCheckBoxContainer = rootView.findViewById(R.id.comment_checkbox_container);
        mAnonCheckBoxContainer.setVisibility(
                mFeedDetail.getPost().isCommentAnonDisabled() ?
                        View.GONE :
                        View.VISIBLE);

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

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommentsRetrieved) return;
                if (mSendButton.isActive()) {
                    sendComment();
                } else {
                    Intent i = new Intent(getActivity(), CameraActivity.class);
                    i.putExtra(CameraActivity.CAMERA_TYPE, new CameraType(CameraType.CAMERA_PICTURE).add(CameraType.CAMERA_GALLERY));

                    i.putExtra(CameraActivity.GALLERY_TYPE, CameraActivity.IMAGE);

                    if (mFeedDetail.getPost().isCommentAnonDisabled()) {
                        i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.RETURN_URI);
                        i.putExtra(CameraActivity.ANON_KEY, false);
                    } else {
                        i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.RETURN_URI_AND_PRIVACY);
                        i.putExtra(CameraActivity.ANON_KEY, mCheckBox.isChecked());
                    }
                    startActivityForResult(i, CAMERA_REQUEST);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            //user -- user Id
            //room -- id of event

            JSONObject joinParam = new JSONObject();

            try {
                joinParam.put("room", mFeedDetail.getPostId());
                joinParam.put("user", mViewId);
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
        }

        //only updates first time it is created
        if (!mCommentsRetrieved) {
            displayCommentsAndPost();
        }

        if (mImageUri != null) sendPicture();
    }

    private void showKeyboard(View editText, boolean show) {
        if (getActivity() != null) {
            if (show) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            } else {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }


    private Uri mImageUri;
    private String mOverlayText;
    private boolean mPrivacy;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                mImageUri = data.getParcelableExtra("image");
                mOverlayText = data.getStringExtra("title");
                if (mOverlayText == null) mOverlayText = "";
                mPrivacy = data.getBooleanExtra("privacy", false);
            } else {
                mImageUri = null;
                mOverlayText = "";
                mPrivacy = false;
            }
        }
    }

    private void sendPicture() {
        try {
            JSONObject comment = new JSONObject();
            comment.put("user", mViewId);
            comment.put("text", mOverlayText);
            comment.put("room", mFeedDetail.getPostId());

            comment.put("privacy", mPrivacy ? 1 : 0);

            JSONArray images = new JSONArray();

            //get bitmap from uri
            images.put(Utils.encodeImageBase64(
                    MediaStore.Images.Media.getBitmap(getActivity().getContentResolver()
                            , mImageUri)));

            comment.put("mentions", new JSONArray());
            comment.put("images", images);
            mSendButton.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);
            mCommentEditText.setText("");

            BaseTaptActivity activity = (BaseTaptActivity) getActivity();
            if (activity == null) return;
            activity.emitSocket(API_Methods.VERSION + ":comments:new comment", comment);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        mOverlayText = "";
        mImageUri = null;
        mPrivacy = false;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        mFeedDetailAdapter.closeAllItems();

        VideoPlayerSingleton.getSingleVideoPlaybackManager().stopPlayback();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity != null) {

            JSONObject leaveParam = new JSONObject();
            try {
                leaveParam.put("room", mFeedDetail.getPostId());
                leaveParam.put("user", mViewId);
                activity.emitSocket(API_Methods.VERSION + ":comments:left", leaveParam);
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
        if (getActivity() == null) return;

        showKeyboard(mCommentEditText, false);
    }


    private void displayCommentsAndPost() {
        if (getActivity() == null) return;

        new LSDKEvents(getActivity()).getEventWithId(mFeedDetail.getPostId(), new Callback() {
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
                }

                JSONObject jsonObject;
                JSONArray comments;
                JSONObject owner;
                JSONArray mentionedPeople;

                try {
                    jsonObject = new JSONObject(response.body().string());

                    //Log.i(TAG, "onResponse: " + jsonObject.toString(4));

                    mFeedDetail.getPost().updateInfo(jsonObject);
                    mSkip = mFeedDetail.getPost().getNumOfComments() - 20;

                    final ArrayList<Object> tempComments = new ArrayList<>();
                    comments = jsonObject.getJSONArray("comments");

                    //Log.i(TAG, "onResponse: " + comments.toString(4));

                    Date myDate;
                    SimpleDateFormat format = Utils.getDateFormat();

                    if (mSkip > 0) {
                        tempComments.add(new LoadMoreItem());
                    }

                    for (int i = 0; i < comments.length(); i++) {
                        //Log.i(TAG, "onResponse: "+comments.getJSONObject(i).toString());

                        //get date
                        try {
                            myDate = format.parse(comments.getJSONObject(i).getString("date"));
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

                        owner = comments.getJSONObject(i).getJSONObject("owner");

                        tempComments
                                .add(new Comment(
                                                owner.getString("id"),
                                                owner.getString("profileImage"),
                                                owner.getString("fullName"),
                                                comments.getJSONObject(i).getString("text"),
                                                comments.getJSONObject(i).getString("id"),
                                                comments.getJSONObject(i).getInt("privacy") == 1,
                                                comments.getJSONObject(i).getString("anonymousImage"),
                                                mentionedPersonLightArrayList,
                                                myDate == null ? 0 : myDate.getTime(),
                                                comments.getJSONObject(i).getBoolean("isLiked"),
                                                comments.getJSONObject(i).getInt("numberOfLikes"),
                                                getImageUrl(comments.getJSONObject(i).getJSONArray("images"))
                                        )
                                );
                    }

                    if (comments.length() == 0) {
                        tempComments.add(null);
                    }

                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mFeedDetail.getComments().clear();
                                    mFeedDetail.getComments().addAll(tempComments);

                                    mCommentsRetrieved = true;
                                    mFeedDetailAdapter.notifyDataSetChanged();
                                    mAnonCheckBoxContainer.setVisibility(
                                            mFeedDetail.getPost().isCommentAnonDisabled() ?
                                                    View.GONE :
                                                    View.VISIBLE);

                                    recList.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            recList.scrollToPosition(tempComments.size());
                                        }
                                    });
                                }
                            });
                        }
                    });

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

        });
    }


    private String getImageUrl(JSONArray images) {
        if (images == null || images.length() == 0) return null;
        try {
            return images.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void loadMoreComments() {

        if (!mCommentsRetrieved || mSkip < 0) {
            return;
        }

        mFeedDetailAdapter.setDenySwipe(true);
        mFeedDetailAdapter.closeAllItems();

        int skip = mSkip - 20;
        int limit = 20;

        Map<String, String> eventComments = new HashMap<>();
        eventComments.put("event", mFeedDetail.getPostId());
        if (skip < 0) {
            limit += skip;
            skip = 0;
        }

        eventComments.put("skip", skip + "");
        eventComments.put("limit", limit + "");

        final int skip1 = skip;
        new LSDKEvents(getActivity()).getComments(eventComments, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mFeedDetailAdapter.setDenySwipe(false);
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
                            mFeedDetailAdapter.setDenySwipe(false);
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
                        JSONObject owner;
                        JSONArray mentionedPeople;

                        final ArrayList<Object> tempComments = new ArrayList<>();

                        try {
                            jsonObject = new JSONObject(response.body().string());

                            comments = jsonObject.getJSONArray("comments");

                            Date myDate;
                            SimpleDateFormat format = Utils.getDateFormat();

                            if (skip1 > 0) {
                                tempComments.add(new LoadMoreItem());
                            }

                            for (int i = 0; i < comments.length(); i++) {
                                //Log.i(TAG, "onResponse: "+comments.getJSONObject(i).toString());

                                //get date
                                try {
                                    myDate = format.parse(comments.getJSONObject(i).getString("date"));
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

                                owner = comments.getJSONObject(i).getJSONObject("owner");
                                tempComments
                                        .add(new Comment(
                                                        owner.getString("id"),
                                                        owner.getString("profileImage"),
                                                        owner.getString("fullName"),
                                                        comments.getJSONObject(i).getString("text"),
                                                        comments.getJSONObject(i).getString("id"),
                                                        comments.getJSONObject(i).getInt("privacy") == 1,
                                                        comments.getJSONObject(i).getString("anonymousImage"),
                                                        mentionedPersonLightArrayList,
                                                        myDate == null ? 0 : myDate.getTime(),
                                                        comments.getJSONObject(i).getBoolean("isLiked"),
                                                        comments.getJSONObject(i).getInt("numberOfLikes"),
                                                        getImageUrl(comments.getJSONObject(i).getJSONArray("images"))
                                                )
                                        );
                            }


                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSkip = skip1;
                                            mFeedDetail.getComments().remove(0);
                                            mFeedDetail.getComments().addAll(0, tempComments);
                                            mFeedDetailAdapter.setDenySwipe(false);
                                            mFeedDetailAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mFeedDetailAdapter.setDenySwipe(false);
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

    private void showOptionsDialog() {
        if (mFeedDetail.getPostUserId() == null || getActivity() == null) return;

        if (mFeedDetail.getPostUserId().equals(mViewId)) { //is the viewers post
            String[] ops = new String[]{
                    "Delete post",
                    mFeedDetail.isAnon() ? "Reveal post" : "Make anonymous",
                    mFeedDetail.getPost().isPostMuted() ? "Unmute post" : "Mute post"};

            mAlertDialog = new AlertDialog.Builder(getActivity())
                    .setItems(ops, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                showConfirmDeleteDialog();
                            } else if ((which == 1)){
                                showRevealConfirm();
                            }else {
                                toggleMute();
                            }
                        }
                    }).show();
        } else {
            String[] ops = new String[]{
                    mFeedDetail.getPost().isPostMuted() ? "Unmute post" : "Mute post",
                    mFeedDetail.getPost().isPostHidden() ? "Unhide post" : "Hide post",
                    "Report post"
            };
            mAlertDialog = new AlertDialog.Builder(getActivity())
                    .setItems(ops, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    toggleMute();
                                    break;
                                case 1:
                                    showHideConfirmation();
                                    break;
                                default:
                                    showReportOptionsDialog();
                            }
                        }
                    }).show();
        }
    }


    private void showReportOptionsDialog() {
        if (getActivity() == null || !mCommentsRetrieved) return;
        final CharSequence options[] = new CharSequence[]{"Spam", "Inappropriate", "Harassment"};
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Report As")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reportEvent(which);
                    }
                })
                .create();
        mAlertDialog.show();
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
        if (getActivity() == null || !mCommentsRetrieved) return;

        mAlertDialog = new AlertDialog.Builder(getActivity())
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
        if (getActivity() == null || !mViewId.equals(mFeedDetail.getPostUserId())) return;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Deleting", true, false);

        new LSDKEvents(getActivity()).deleteEvent(mFeedDetail.getPostId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
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

                    final BaseTaptActivity activity = (BaseTaptActivity) getActivity();

                    if (activity == null) return;

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Post deleted", Toast.LENGTH_SHORT).show();
                            activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);
                            activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.PROFILE);
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
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }
        });
    }


    public void showRevealConfirm() {
        if (getActivity() == null || !mCommentsRetrieved) return;
        boolean isAnon = mFeedDetail.getPostPrivacy() == 1;
        mAlertDialog = new AlertDialog.Builder(getActivity())
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

    private void toggleMute() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity == null) return;

        if (!Utils.isNetworkAvailable(activity) || !activity.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isMuted = mFeedDetail.getPost().isPostMuted();
        mFeedDetail.getPost().setPostMuted(!isMuted);

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
        if (getActivity() == null || !mCommentsRetrieved) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mFeedDetail.getPost().isPostHidden() ? "Unhide post" : "Hide it")
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
        if (getActivity() == null || !mViewId.equals(mFeedDetail.getPostUserId())) return;
        final boolean isAnon = mFeedDetail.getPostPrivacy() == 1;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, isAnon ? "Revealing post..." : "Making post anonymous...", true, false);
        new LSDKEvents(getActivity()).revealEvent(mFeedDetail.getPostId(), !isAnon, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
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
                String res = response.body().string();

                if (response.isSuccessful()) {
                    try {

                        if (!isAnon) {
                            JSONObject obj = new JSONObject(res);
                            mFeedDetail.setAnonImage(Utils.getAnonImageUrl(obj.getString("anonymousImage")));
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mFeedDetail.setPostPrivacy(isAnon ? 0 : 1);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFeedDetailAdapter.notifyItemChanged(0);
                                        }
                                    });
                                    Toast.makeText(getActivity(), isAnon ? "Post revealed" : "Post made anonymous", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponse: " + res);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: " + res);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }
        });
    }





    /* MENTIONS CODE */

    private Handler mSearchHandler = new Handler();
    private String mQueryString = "";
    private boolean mDisplayList = false;
    private Call mFriendSearchCall;

    private Runnable mSearchRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) return;

            if (mFriendSearchCall != null) mFriendSearchCall.cancel();

            mFriendSearchCall = new LSDKFriends(getActivity()).getFriendsForMention(mViewId, mQueryString, "0", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() == null || call.isCanceled()) return;
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
                            JSONObject user;

                            final ArrayList<MentionedPerson> personList = new ArrayList<>();

                            for (int i = 0; i < friends.length(); i++) {
                                user = friends.getJSONObject(i).getJSONObject("user");
                                try {
                                    personList.add(
                                            new MentionedPerson(
                                                    user.getString("fullName"),
                                                    user.getString("id"),
                                                    user.getString("profileImage")
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

        String BUCKET = "MENTIONS";
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
                Glide.with(FeedDetailPage.this)
                        .load(Utils.getImageUrlOfUser(person.getProfileImage()))
                        .asBitmap()
                        .signature(new StringSignature(mImageSignature))
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



    /* DEALING WITH COMMENTS */

    private boolean mCommentsRetrieved = false;

    private Emitter.Listener newComment = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            if (!mCommentsRetrieved) return;

            JSONObject object = (JSONObject) args[0];
            if (object == null) {
                Log.i(TAG, "call: Error retrieving new comment");
                return;
            }
            //will check if we can scroll down
            //if can't scroll down, we are at the bottom. when new comment comes in, move to bottom on new comment
            //neg is scroll up, positive is scroll down

            boolean canScrollDown = false;
            try {
                canScrollDown = recList != null &&
                        recList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE &&
                        recList.canScrollVertically(1);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            final boolean mCanScrollDown = canScrollDown;

            try {
                Date myDate;
                myDate = Utils.getDateFormat().parse(object.getString("date"));

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

                JSONObject owner = object.getJSONObject("owner");

                final Comment com = new Comment(
                        owner.getString("id"),
                        owner.getString("profileImage"),
                        owner.getString("fullName"),
                        object.getString("text"),
                        object.getString("id"),
                        object.getInt("privacy") == 1,
                        object.getString("anonymousImage"),
                        mentionedPersonLightArrayList,
                        myDate.getTime(),
                        false,
                        0,
                        getImageUrl(object.getJSONArray("images"))
                );

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean smoothScroll = false;
                            if (com.getCommentUserId().equals(mViewId)) { //was the user that posted the comment
                                mSendButton.setVisibility(View.VISIBLE);
                                mProgressbar.setVisibility(View.GONE);
                                smoothScroll = true;
                            }

                            //because of header we can use size, change if decide to add it to array
                            final boolean finalSmoothScroll = smoothScroll;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    //if NoComments View is showing, remove it
                                    boolean notifyChange = false;
                                    if (!mFeedDetail.getComments().isEmpty() && mFeedDetail.getComments().get(0) == null) {
                                        mFeedDetail.getComments().clear();
                                        notifyChange = true;
                                    }

                                    mFeedDetail.getComments().add(com);
                                    mFeedDetail.refreshCommentCount();

                                    if (notifyChange) {
                                        mFeedDetailAdapter.notifyDataSetChanged();
                                    } else {
                                        mFeedDetailAdapter.notifyItemInserted(mFeedDetail.getComments().size());
                                    }

                                    if (finalSmoothScroll || !mCanScrollDown)
                                        recList.scrollToPosition(mFeedDetail.getComments().size());
                                }
                            });

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

        if (mMentionedList.getVisibility() == View.VISIBLE) {
            mMentionedList.setVisibility(View.GONE);
        }

        String commentText = mCommentEditText.getText().toString().trim();

        if (commentText.isEmpty() || getActivity() == null || mFeedDetail.getPostId() == null || activity == null)
            return;

        if (!activity.socketConnected() || !Utils.isNetworkAvailable(activity)) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        try {
            JSONObject comment = new JSONObject();
            comment.put("user", mViewId);
            comment.put("text", commentText);
            comment.put("room", mFeedDetail.getPostId());

            comment.put("privacy", mFeedDetail.getPost().isCommentAnonDisabled() ?
                    0 :
                    mCheckBox.isChecked() ? 1 : 0
            );

            //add people mentioned in comment
            List<MentionSpan> spanList = mCommentEditText.getMentionsText().getMentionSpans();
            JSONArray mentions = new JSONArray();

            if (!spanList.isEmpty()) {
                for (MentionSpan s : spanList) {
                    mentions.put(((MentionedPerson) s.getMention()).getUserId()); //add user ids
                }
            }

            comment.put("mentions", mentions);
            mSendButton.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);
            mCommentEditText.setText("");
            activity.emitSocket(API_Methods.VERSION + ":comments:new comment", comment);
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(getActivity());
        }
    }


    /* DELETING COMMENT */
    @Override
    public void deleteComment(final int pos, final String id) {
        if (mFeedDetailAdapter.getDenySwipe()) return;

        if (getActivity() != null) {
            mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle("Delete")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            confirmDeleteComment(pos, id);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void confirmDeleteComment(final int in, final String id) {
        if (mFeedDetailAdapter.getDenySwipe() || getActivity() == null) return;

        final int pos = in - 1;
        final Comment com = (Comment) mFeedDetail.getComments().get(pos);

        //if viewer is not the owner of the comment, return
        // exception: anon comments can be deleted by post owner
        if (!com.getCommentPostId().equals(id) || (!com.getCommentUserId().equals(mViewId) && !com.isAnon()))
            return;

        mFeedDetailAdapter.setDenySwipe(true);
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, "Deleting comment...", true, false);

        new LSDKEvents(getActivity()).deleteComment(id, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mFeedDetailAdapter.setDenySwipe(false);
                progressDialog.dismiss();

                final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                if (act != null) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(act, "Failed to delete comment. Could not find connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();

                    final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //mItemManger.removeShownLayouts(mSwipeLayout);
                                        mFeedDetail.getComments().remove(pos);
                                        mFeedDetailAdapter.notifyItemRemoved(pos + 1);
                                        mFeedDetailAdapter.notifyItemRangeChanged(pos + 1, mFeedDetail.getComments().size() + 1);
                                        mFeedDetail.refreshCommentCount();

                                        Toast.makeText(act, "Comment deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                } else {
                    Log.i("Comment item delete", "onResponse: " + response.body().string());
                    final BaseTaptActivity act = (BaseTaptActivity) getActivity();

                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(act);
                            }
                        });
                    }
                }

                progressDialog.dismiss();
                mFeedDetailAdapter.setDenySwipe(false);
            }
        });
    }

    @Override
    public void closeAllDialogs() {
        if (mAlertDialog != null) mAlertDialog.dismiss();
    }


    /* REVEALING COMMENT */
    @Override
    public void revealComment(final int pos, final String id, final boolean isAnon) {
        if (getActivity() != null && !mFeedDetailAdapter.getDenySwipe())
            mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(isAnon ? "Reveal" : "Hide")
                    .setMessage(isAnon ? "Are you sure you want to turn anonymous off for this comment?" : "Are you sure you want to make this comment anonymous?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            confirmRevealComment(pos, id, isAnon);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
    }

    private void confirmRevealComment(final int in, final String id, final boolean isAnon) {
        if (mFeedDetailAdapter.getDenySwipe() || getActivity() == null) return;

        final int pos = in - 1;
        final Comment comment = (Comment) mFeedDetail.getComments().get(pos);

        //safe check
        //double check that they are revealing their own comment
        if (!comment.getCommentUserId().equals(mViewId) || !comment.getCommentPostId().equals(id))
            return;

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, isAnon ? "Revealing comment..." : "Making comment anonymous...", true, false);
        mFeedDetailAdapter.setDenySwipe(true);

        new LSDKEvents(getActivity()).revealComment(id, !isAnon, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                progressDialog.dismiss();
                mFeedDetailAdapter.setDenySwipe(false);

                if (act != null) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(act, "Failed to change comment. Could not find connection.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        if (!isAnon) { //set new anon image
                            comment.setAnonImage(new JSONObject(res).getString("anonymousImage"));
                        }

                        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            comment.setIsAnon(!isAnon);
                                            mFeedDetailAdapter.notifyItemChanged(pos + 1);
                                            Toast.makeText(activity, isAnon ? "Comment revealed" : "Comment made anonymous", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }

                    } catch (JSONException e) {
                        final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                        if (act != null) {
                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(act, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Log.i("Comment item reveal", "onResponse: " + response.body().string());
                    final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "Failed to change comment. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                progressDialog.dismiss();
                mFeedDetailAdapter.setDenySwipe(false);
            }
        });
    }


    /* Report Comment */

    @Override
    public void reportComment(final String id) {
        if (getActivity() != null && !mFeedDetailAdapter.getDenySwipe())
            mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle("Report")
                    .setMessage("Are you sure you want to report this comment?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            confirmReportComment(id);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
    }

    @Override
    public void likeComment(boolean like, String id) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("comment", id);
                obj.put("liked", like);
                activity.emitSocket(API_Methods.VERSION + ":comments:liked", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void confirmReportComment(String id) {
        if (getActivity() == null || mFeedDetailAdapter.getDenySwipe()) return;

        new LSDKEvents(getActivity()).reportComment(id, mViewId, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                if (act != null) {
                    act.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(act);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    response.body().close();
                    final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(act, "Comment reported", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.i("Comment item report", "onResponse: " + response.body().string());
                    final BaseTaptActivity act = (BaseTaptActivity) getActivity();
                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(act);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFeedDetailAdapter != null) mFeedDetailAdapter.clearContext();
    }
}

