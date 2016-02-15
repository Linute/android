package com.linute.linute.MainContent.FeedDetailFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arman on 1/11/16.
 */
public class FeedDetailPage extends UpdatableFragment {

    private static final String TAG = FeedDetail.class.getSimpleName();
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private FeedDetail mFeedDetail;

    private boolean mIsImage;
    private String mTaptPostId;
    private String mTaptPostUserId;
    private FeedDetailAdapter mFeedDetailAdapter;
    private String mCommentText;
    private EditText mCommentEditText;

    private View mAnonCheckBoxContainer;

    private View mSendButtonContainer;
    private View mSendButton;

    private boolean mOpenKeyBoard = false;

    private boolean mCommentPosted = false;


    public FeedDetailPage() {
    }


    public static FeedDetailPage newInstance(boolean openKeyBoard,
                                             boolean isImage,
                                             String taptUserPostId,
                                             String taptPostUserId) {
        FeedDetailPage fragment = new FeedDetailPage();
        Bundle args = new Bundle();
        args.putBoolean("TITLE", isImage);
        args.putString("TAPTPOST", taptUserPostId);
        args.putString("TAPTPOSTUSERID", taptPostUserId);
        args.putBoolean("OPEN_KEYBOARD", openKeyBoard);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String POST_KEY = "INPUT_POST";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFeedDetail = new FeedDetail();


            mIsImage = getArguments().getBoolean("TITLE");
            mTaptPostId = getArguments().getString("TAPTPOST");
            mTaptPostUserId = getArguments().getString("TAPTPOSTUSERID");
            mOpenKeyBoard = getArguments().getBoolean("OPEN_KEYBOARD");

            mFeedDetail.setPostId(mTaptPostId);
            mFeedDetail.setPostUserId(mTaptPostUserId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_feed_detail_page, container, false);

        setHasOptionsMenu(true);

        recList = (RecyclerView) rootView.findViewById(R.id.feed_detail_recyc);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mFeedDetailAdapter = new FeedDetailAdapter(mFeedDetail, getActivity(), mIsImage);
        recList.setAdapter(mFeedDetailAdapter);

//        recList.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                rootView.findViewById(R.id.comment_field).clearFocus();
//                imm.hideSoftInputFromWindow(rootView.findViewById(R.id.comment_field).getWindowToken(), 0);
//                return false;
//            }
//        });

        mSendButtonContainer = rootView.findViewById(R.id.comment_send_button_container);
        mSendButtonContainer.setEnabled(false);

        mSendButton = rootView.findViewById(R.id.feed_detail_send_comment);

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
                if (!s.toString().trim().equals("")) {
                    mSendButtonContainer.setEnabled(true);
                    mSendButton.setAlpha((float) 1);

                    mCommentText = s.toString();
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
                    recList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recList.smoothScrollToPosition(mFeedDetailAdapter.getItemCount() - 1);
                        }
                    }, 500);
                return false;
            }
        });


        mAnonCheckBoxContainer = rootView.findViewById(R.id.comment_checkbox_container);

        final CheckBox checkBox = (CheckBox) mAnonCheckBoxContainer.findViewById(R.id.comment_anon_checkbox);

        mAnonCheckBoxContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    ((TextView) mAnonCheckBoxContainer.findViewById(R.id.comment_anon_checkbox_text))
                            .setText("OFF");
                    checkBox.setChecked(false);
                } else {
                    ((TextView) mAnonCheckBoxContainer.findViewById(R.id.comment_anon_checkbox_text))
                            .setText("ON");
                    checkBox.setChecked(true);
                }
            }
        });


        rootView.findViewById(R.id.comment_send_button_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCommentText.trim().isEmpty() || getActivity() == null) return;

                final View progressBar = rootView.findViewById(R.id.comment_progressbar);
                final View sendButton = rootView.findViewById(R.id.feed_detail_send_comment);

                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mCommentEditText.getWindowToken(), 0);

                LSDKEvents lsdkEvents = new LSDKEvents(getActivity());
                Map<String, Object> comment = new HashMap<String, Object>();
                JSONArray jsonArray = new JSONArray();
                comment.put("image", jsonArray);
                comment.put("text", mCommentText);
                comment.put("event", mTaptPostId);
                comment.put("privacy", checkBox.isChecked() ? 1 : 0);
                recList.smoothScrollToPosition(mFeedDetailAdapter.getItemCount() - 1);

                setCommentViewEditable(false);
                sendButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                lsdkEvents.postComment(comment, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showBadConnectionToast(getActivity());
                                    sendButton.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    setCommentViewEditable(true);

                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d(TAG, "onResponseNotSuccessful: " + response.body().string());
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showBadConnectionToast(getActivity());
                                        sendButton.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        setCommentViewEditable(true);
                                    }
                                });
                            }

                        } else {
                            Log.d(TAG, "onResponseSuccessful: " + response.body().string());

                            if (getActivity() == null) return;
                            mCommentPosted = true;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCommentEditText.setText("");
                                    sendButton.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    setCommentViewEditable(true);
                                    displayCommentsAndPost();
                                }
                            });
                        }
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.setTitle(mIsImage ? "Image" : "Status");
            activity.resetToolbar();
            activity.enableBarScrolling(false);
            activity.setToolbarOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recList != null)
                        recList.smoothScrollToPosition(0);
                }
            });
        }

        //only updates first time it is created
        if (fragmentNeedsUpdating()) {
            displayCommentsAndPost();
            setFragmentNeedUpdating(false);
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
        event.getEventWithId(mTaptPostId, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
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
            public void onResponse(Response response) throws IOException {
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
                JSONObject jsonObject = null;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date myDate;
                String postString;
                String res = response.body().string();
                Log.i(TAG, "onResponse: " + res);
                try {
                    jsonObject = new JSONObject(res);

                    myDate = simpleDateFormat.parse(jsonObject.getString("date"));
                    postString = Utils.getTimeAgoString(myDate.getTime());

                    String imageName = jsonObject.getJSONArray("images").length() != 0 ? jsonObject.getJSONArray("images").getString(0) : "";

                    mFeedDetail.setFeedDetail(
                            imageName,
                            jsonObject.getString("title"),
                            jsonObject.getJSONObject("owner").getString("profileImage"),
                            jsonObject.getJSONObject("owner").getString("fullName"),
                            Integer.parseInt(jsonObject.getString("privacy")),
                            postString,
                            jsonObject.getBoolean("isLiked"),
                            jsonObject.getString("numberOfLikes"),
                            jsonObject.getString("numberOfComments"),
                            jsonObject.getString("anonymousImage")
                    );
                } catch (JSONException | ParseException e) {
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
                    public void onResponse(Response response) throws IOException {
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

                        ArrayList<Comment> tempComments = new ArrayList<>();
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            comments = jsonObject.getJSONArray("comments");
                            for (int i = 0; i < comments.length(); i++) {
                                Log.d(TAG, "comment: " + comments.get(i).toString());
                                tempComments
                                        .add(new Comment(
                                                ((JSONObject) comments.get(i)).getJSONObject("owner").getString("id"),
                                                ((JSONObject) comments.get(i)).getJSONObject("owner").getString("profileImage"),
                                                ((JSONObject) comments.get(i)).getJSONObject("owner").getString("fullName"),
                                                ((JSONObject) comments.get(i)).getString("text"),
                                                ((JSONObject) comments.get(i)).getString("id"),
                                                ((JSONObject) comments.get(i)).getInt("privacy") == 1,
                                                ((JSONObject) comments.get(i)).getString("anonymousImage")
                                        ));
                            }

                            if (comments.length() == 0) {
                                tempComments.add(null);
                            }

                            mFeedDetail.getComments().clear();
                            mFeedDetail.getComments().addAll(tempComments);

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
                        getActivity().runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mFeedDetailAdapter.notifyDataSetChanged();

                                        if (mCommentPosted) {
                                            mCommentPosted = false;
                                            recList.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    recList.smoothScrollToPosition(mFeedDetailAdapter.getItemCount() - 1);
                                                }
                                            }, 500);
                                        }

                                    }
                                }

                        );
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() != null) {
            SharedPreferences pref = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            inflater.inflate(mTaptPostUserId.equals(pref.getString("userID", "")) ? R.menu.feed_detail_delete_toolbar : R.menu.feed_detail_report_toolbar, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void showReportOptionsDialog() {
        if (getActivity() == null) return;
        final CharSequence options[] = new CharSequence[]{"Spam", "Inappropriate", "Harassment", "Suspected Parent", "Suspected Professor", "Cancel"};
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Report As")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == options.length - 1) { //cancel
                            dialog.dismiss();
                        } else {
                            reportEvent(which);
                        }
                    }
                })
                .create();
        alert.show();

    }


    private void reportEvent(final int reason) {
        if (getActivity() == null) return;

        new LSDKEvents(getActivity()).reportEvent(reason, mTaptPostId, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
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
            public void onResponse(Response response) throws IOException {
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
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
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

        new LSDKEvents(getActivity()).deleteEvent(mTaptPostId, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
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
            public void onResponse(Response response) throws IOException {
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
}
