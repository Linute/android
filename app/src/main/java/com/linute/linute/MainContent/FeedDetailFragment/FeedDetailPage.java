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
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import com.linute.linute.MainContent.CreateContent.Gallery.GalleryActivity;
import com.linute.linute.MainContent.DiscoverFragment.BaseFeedItem;
import com.linute.linute.MainContent.DiscoverFragment.Poll;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.DiscoverFragment.VideoPlayerSingleton;
import com.linute.linute.MainContent.EditScreen.PostOptions;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.Socket.TaptSocket;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CameraType;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.ImpressionHelper;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.SocketListener;
import com.linute.linute.UtilsAndHelpers.ToggleImageView;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

public class FeedDetailPage extends BaseFragment implements QueryTokenReceiver, SocketListener,
        SuggestionsResultListener, SuggestionsVisibilityManager, FeedDetailAdapter.CommentActions {

    private static final int CAMERA_GALLERY_REQUEST = 65;
    private static final String TAG = BaseFeedDetail.class.getSimpleName();
    private RecyclerView recList;

    private BaseFeedDetail mFeedDetail;

    private FeedDetailAdapter mFeedDetailAdapter;
    private MentionsEditText mCommentEditText;

    //private View mAnonCheckBoxContainer;

    private RecyclerView mMentionedList;

    private MentionedPersonAdapter mMentionedPersonAdapter;

    private View mProgressbar;
    private ToggleImageView mSendButton;

    private CheckBox mCheckBox;
    private View mDisabledImage;

    private String mViewId;
    private String mImageSignature;

    private Handler mHandler = new Handler();

    private int mSkip = 0;

    private AlertDialog mAlertDialog;
    private LinearLayoutManager mFeedDetailLLM;

    private Snackbar mNewCommentSnackbar;

    protected TaptSocket mTaptSocket = TaptSocket.getInstance();

    public FeedDetailPage() {
    }

    //used in displaying bottom snackbar on new comment socket
    private int mNewCommentCount = 0;


    public static FeedDetailPage newInstance(BaseFeedItem post) {
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
            BaseFeedItem item =  getArguments().getParcelable("POST");
            mFeedDetail = item instanceof Poll ? new PollFeedDetail((Poll) item) : new PostFeedDetail((Post) item);
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

        recList = (RecyclerView) rootView.findViewById(R.id.feed_detail_recyc);
        mFeedDetailLLM = new CustomLinearLayoutManager(getActivity());
        mFeedDetailLLM.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(mFeedDetailLLM);

        mFeedDetailAdapter = new FeedDetailAdapter(mFeedDetail, Glide.with(this), getContext());
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
                if (mFeedDetailLLM.findLastCompletelyVisibleItemPosition() == RecyclerView.NO_POSITION && dy < 0) {
                    //close keyboard
                    showKeyboard(recyclerView, false);
                }

                if (mFeedDetailLLM.findLastVisibleItemPosition() >= mFeedDetailAdapter.getItemCount() - mNewCommentCount) {
                    mNewCommentCount = 0;
                    if (mNewCommentSnackbar != null && mNewCommentSnackbar.isShown())
                        mNewCommentSnackbar.dismiss();
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
//        mCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    if (mFeedDetailLLM != null) {
//                        mFeedDetailLLM.scrollToPosition(mFeedDetailLLM.getItemCount() - 1);
//                    }
//                }
//            }
//        });
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

        mCommentEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction())
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
            public void addMentionedPerson(MentionedPerson person, final int pos) {
                mCommentEditText.append(" @");
                mCommentEditText.insertMention(person);
                mCommentEditText.requestFocus();
                showKeyboard(mCommentEditText, true);
            }
        });

        View mAnonCheckBoxContainer = rootView.findViewById(R.id.comment_checkbox_container);


        mDisabledImage = mAnonCheckBoxContainer.findViewById(R.id.disabled_icon);
        mCheckBox = (CheckBox) mAnonCheckBoxContainer.findViewById(R.id.comment_anon_checkbox);
        updateAnonCheckboxState();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommentsRetrieved) return;
                if (mSendButton.isActive()) {
                    sendComment();
                } else {
                    showCameraGalleryOption();
                }
            }
        });

        ImpressionHelper.sendImpressionsAsync(pref.getString("collegeId", ""), mViewId, mFeedDetail.getPostId());

        return rootView;
    }


    private void updateAnonCheckboxState() {
        if (mFeedDetail.isAnonCommentsDisabled()) {
            mDisabledImage.setVisibility(View.VISIBLE);
            mCheckBox.setVisibility(View.GONE);
        } else {
            mDisabledImage.setVisibility(View.GONE);
            mCheckBox.setVisibility(View.VISIBLE);
        }
    }

    private void showCameraGalleryOption() {
        if (getContext() == null) return;
        mAlertDialog = new AlertDialog.Builder(getContext()).setItems(
                new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i;
                        PostOptions options = new PostOptions();
                        if (which == 0) {
                            i = new Intent(getContext(), CameraActivity.class);
                            i.putExtra(CameraActivity.EXTRA_CAMERA_TYPE, new CameraType(CameraType.CAMERA_PICTURE));
                            options.subType =  mFeedDetail.isAnonCommentsDisabled() ? PostOptions.ContentSubType.Comment_No_Anon : PostOptions.ContentSubType.Comment;
                        } else {
                            i = new Intent(getContext(), GalleryActivity.class);
                            i.putExtra(GalleryActivity.ARG_GALLERY_TYPE, GalleryActivity.PICK_IMAGE);
                            options.subType = mFeedDetail.isAnonCommentsDisabled() ? PostOptions.ContentSubType.Comment_No_Anon : PostOptions.ContentSubType.Comment;
                        }

                        if (mFeedDetail.isAnonCommentsDisabled()) {
                            i.putExtra(CameraActivity.EXTRA_RETURN_TYPE, CameraActivity.RETURN_URI);
                            i.putExtra(CameraActivity.EXRTA_ANON, false);
                        } else {
                            i.putExtra(CameraActivity.EXTRA_RETURN_TYPE, CameraActivity.RETURN_URI_AND_PRIVACY);
                            i.putExtra(CameraActivity.EXRTA_ANON, mCheckBox.isChecked());
                        }

                        i.putExtra(CameraActivity.EXTRA_POST_OPTIONS, options);
                        startActivityForResult(i, CAMERA_GALLERY_REQUEST);
                    }
                }).show();
    }


    @Override
    public void onResume() {
        super.onResume();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            joinRoomSocket(activity);
            mTaptSocket.on("new comment", newComment);
            activity.setSocketListener(this);
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
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
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
        if (requestCode == CAMERA_GALLERY_REQUEST) {
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
            mTaptSocket.emit(API_Methods.VERSION + ":comments:new comment", comment);
        } catch (JSONException | IOException | NullPointerException e) { //nullpointer when creating bitmap
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
            activity.setSocketListener(null);
            JSONObject leaveParam = new JSONObject();
            try {
                leaveParam.put("room", mFeedDetail.getPostId());
                leaveParam.put("user", mViewId);
                mTaptSocket.emit(API_Methods.VERSION + ":comments:left", leaveParam);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mTaptSocket.off("new comment", newComment);
            activity.setSocketErrorResponse(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getActivity() == null) return;
        showKeyboard(mCommentEditText, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFeedDetailAdapter.getRequestManager() != null)
            mFeedDetailAdapter.getRequestManager().onDestroy();

        mFeedDetailAdapter.setRequestManager(null);
    }

    private void displayCommentsAndPost() {
        if (getActivity() == null) return;


        Callback callback = new Callback() {
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

                try {
                    jsonObject = new JSONObject(response.body().string());

                    //Log.d(TAG, "onResponse: "+jsonObject.toString(4));

                    mFeedDetail.updateFeedItem(jsonObject);
                    if (mFeedDetail instanceof PostFeedDetail) {
                        mSkip = mFeedDetail.getNumOfComments() - 20;
                    }else {
                        mSkip = jsonObject.getInt("skip");
                    }

                    final ArrayList<Object> tempComments = new ArrayList<>();
                    comments = jsonObject.getJSONArray("comments");

                    //Log.d(TAG, "onResponse: " + comments.toString(4));

                    if (mSkip > 0) {
                        tempComments.add(new LoadMoreItem());
                    }

                    for (int i = 0; i < comments.length(); i++) {
                        //Log.i(TAG, "onResponse: "+comments.getJSONObject(i).toString());
                        tempComments.add(new Comment(comments.getJSONObject(i)));
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
                                    updateAnonCheckboxState();

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

        };

        if (mFeedDetail instanceof PostFeedDetail)
            new LSDKEvents(getActivity()).getEventWithId(mFeedDetail.getPostId(), callback);
        else {
            Map<String, Object> eventComments = new HashMap<>();
            eventComments.put("event", mFeedDetail.getPostId());
            new LSDKEvents(getActivity()).getComments(eventComments, callback);
        }
    }


    private void loadMoreComments() {

        if (!mCommentsRetrieved || mSkip < 0) {
            return;
        }

        mFeedDetailAdapter.setDenySwipe(true);
        mFeedDetailAdapter.closeAllItems();
        //final int lastPos = mFeedDetailLLM.findFirstVisibleItemPosition();

        int skip = mSkip - 20;
        int limit = 20;

        Map<String, Object> eventComments = new HashMap<>();
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

                        final ArrayList<Object> tempComments = new ArrayList<>();

                        try {
                            jsonObject = new JSONObject(response.body().string());
                            comments = jsonObject.getJSONArray("comments");
                            if (skip1 > 0) {
                                tempComments.add(new LoadMoreItem());
                            }

                            for (int i = 0; i < comments.length(); i++) {
                                //Log.i(TAG, "onResponse: "+comments.getJSONObject(i).toString());
                                tempComments.add(new Comment(comments.getJSONObject(i)));
                            }


                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSkip = skip1;
                                            mFeedDetail.getComments().remove();
                                            mFeedDetailAdapter.notifyItemRemoved(1);
                                            mFeedDetail.getComments().addAll(0, tempComments);
                                            mFeedDetailAdapter.notifyItemRangeInserted(1, tempComments.size());
                                            mFeedDetailAdapter.setDenySwipe(false);
                                            mFeedDetailLLM.scrollToPosition(tempComments.size() + 1);
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
        if (getActivity() == null) return;

        if (mFeedDetail.getPostUserId() != null && mFeedDetail.getPostUserId().equals(mViewId)) { //is the viewers post
            String[] ops = new String[]{
                    "Delete post",
                    mFeedDetail.isAnon() ? "Reveal post" : "Become anonymous",
                    mFeedDetail.isMuted() ? "Unmute post" : "Mute post"};

            mAlertDialog = new AlertDialog.Builder(getActivity())
                    .setItems(ops, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                showConfirmDeleteDialog();
                            } else if ((which == 1)) {
                                showRevealConfirm();
                            } else {
                                toggleMute();
                            }
                        }
                    }).show();
        } else {
            String[] ops = new String[]{
                    mFeedDetail.isMuted() ? "Unmute post" : "Mute post",
                    mFeedDetail.isHidden() ? "Unhide post" : "Hide post",
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
                .setTitle("Delete your post")
                .setMessage("Are you sure you want to delete what you've created?")
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
        boolean isAnon = mFeedDetail.isAnon();
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(isAnon ? "Reveal yourself" : "Wear a mask")
                .setMessage(isAnon ? "Show everyone the person behind the mask! Would you like to reveal your identity for this post?" : "Are you sure you want to become anonymous for this comment?")
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

        if (!Utils.isNetworkAvailable(activity) || !mTaptSocket.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isMuted = mFeedDetail.isMuted();
        mFeedDetail.setMuted(!isMuted);

        JSONObject emit = new JSONObject();
        try {
            emit.put("mute", !isMuted);
            emit.put("room", mFeedDetail.getPostId());
            mTaptSocket.emit(API_Methods.VERSION + ":posts:mute", emit);
            Toast.makeText(activity,
                    isMuted ? "You will start getting updates" : "Notifications muted",
                    Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }


    public void showHideConfirmation() {
        if (getActivity() == null || !mCommentsRetrieved) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(mFeedDetail.isHidden() ? "Unhide post" : "Hide it")
                .setMessage(mFeedDetail.isHidden() ? "This will make this post viewable on your feed. Still want to go ahead with it?" : "This will remove this post from your feed, go ahead with it?")
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

        if (!Utils.isNetworkAvailable(activity) || !mTaptSocket.socketConnected()) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        final boolean isHidden = mFeedDetail.isHidden();
        mFeedDetail.setHidden(!isHidden);

        JSONObject emit = new JSONObject();
        try {
            emit.put("hide", !isHidden);
            emit.put("room", mFeedDetail.getPostId());
            mTaptSocket.emit(API_Methods.VERSION + ":posts:hide", emit);
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
        final boolean isAnon = mFeedDetail.isAnon();
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
                        ((Post)mFeedDetail.getFeedItem()).setPrivacyChanged(true);

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

    @Override
    public void onReconnect() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            joinRoomSocket(activity);
        }
    }

    private void joinRoomSocket(BaseTaptActivity activity) {
        //user -- user Id
        //room -- id of event

        JSONObject joinParam = new JSONObject();

        try {
            joinParam.put("room", mFeedDetail.getPostId());
            joinParam.put("user", mViewId);
            mTaptSocket.emit(API_Methods.VERSION + ":comments:joined", joinParam);
        } catch (JSONException e) {
            e.printStackTrace();
            if (getActivity() != null) {
                Utils.showServerErrorToast(getActivity());
            }
        }
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

//            boolean canScrollDown = false;
//            try {
//                canScrollDown = recList != null &&
//                        recList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE &&
//                        recList.canScrollVertically(1);
//
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//            }

            //final boolean mCanScrollDown = canScrollDown;

            try {
                final Comment com = new Comment(object);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (com.getCommentUserId() != null && com.getCommentUserId().equals(mViewId)) { //was the user that posted the comment
                                mSendButton.setVisibility(View.VISIBLE);
                                mProgressbar.setVisibility(View.GONE);
                                //smoothScroll = true;
                            }

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
                                    mFeedDetail.addComment();

                                    if (notifyChange) {
                                        mFeedDetailAdapter.notifyDataSetChanged();
                                    } else {
                                        mFeedDetailAdapter.notifyItemInserted(mFeedDetail.getComments().size());
                                    }

                                    if (mFeedDetailLLM.findLastVisibleItemPosition() != mFeedDetail.getComments().size()) {
                                        mNewCommentCount++;
                                    }


                                   /* if (finalSmoothScroll || !mCanScrollDown)
                                        recList.scrollToPosition(mFeedDetail.getComments().size());*/

                                    int pos = mFeedDetail.getComments().size() - 1;
                                    if (mFeedDetailLLM.findLastVisibleItemPosition() < pos && (com.getCommentUserId() == null || !com.getCommentUserId().equals(mViewId))) {
                                        mNewCommentSnackbar = Snackbar.make(mCommentEditText, (mNewCommentCount > 1 ? mNewCommentCount + " New Comments" : "New Comment"), Snackbar.LENGTH_LONG);
                                        TextView snackbarTV = (TextView) mNewCommentSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                                        snackbarTV.setTextColor(ContextCompat.getColor(getContext(), R.color.secondaryColor));
                                        snackbarTV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        snackbarTV.setGravity(Gravity.CENTER);
                                        mNewCommentSnackbar.getView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.pure_white));
                                        mNewCommentSnackbar.getView().setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mFeedDetailLLM.scrollToPositionWithOffset(mFeedDetail.getComments().size(), 0);
                                                mNewCommentSnackbar.dismiss();
                                            }
                                        });
                                        mNewCommentSnackbar.show();
                                    } else {
                                        recList.scrollToPosition(mFeedDetail.getComments().size());
                                    }

                                }
                            });

                        }
                    });
                }

            } catch (JSONException e) {
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

        if (!mTaptSocket.socketConnected() || !Utils.isNetworkAvailable(activity)) {
            Utils.showBadConnectionToast(activity);
            return;
        }

        try {
            JSONObject comment = new JSONObject();
            comment.put("user", mViewId);
            comment.put("text", commentText);
            comment.put("room", mFeedDetail.getPostId());

            comment.put("privacy", mFeedDetail.isAnonCommentsDisabled() ?
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

            comment.put("userMentions", mentions);
            mSendButton.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);
            mCommentEditText.setText("");
            mTaptSocket.emit(API_Methods.VERSION + ":comments:new comment", comment);
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
            mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle("Delete comment")
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
        if (!com.getCommentPostId().equals(id) ||
                (!com.isAnon() && (com.getCommentUserId() == null || !mViewId.equals(com.getCommentUserId()))) ||
                (com.isAnon() && com.getCommentUserId() == null && !mViewId.equals(mFeedDetail.getPostUserId())))
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
                                        mFeedDetailAdapter.notifyItemRemoved(in);
                                        mFeedDetailAdapter.notifyItemRangeChanged(in, mFeedDetail.getComments().size());
                                        mFeedDetail.removeComment();

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
            mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle(isAnon ? "Reveal" : "Wear a mask")
                    .setMessage(isAnon ? "Are you sure you want to turn anonymous off for this comment?" : "Are you sure you want to become anonymous for this comment?")
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
        if (comment.getCommentUserId() == null || !comment.getCommentUserId().equals(mViewId) || !comment.getCommentPostId().equals(id))
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
                                            Toast.makeText(activity, isAnon ? "You've taken off your mask!" : "Comment made anonymous", Toast.LENGTH_SHORT).show();
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
            mAlertDialog = new AlertDialog.Builder(getActivity()).setTitle("Report comment")
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
                mTaptSocket.emit(API_Methods.VERSION + ":comments:liked", obj);
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

