package com.linute.linute.MainContent.Chat;


import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;


import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends BaseFragment implements LoadMoreViewHolder.OnLoadMore {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String ROOM_ID = "room";
    private static final String OTHER_PERSON_NAME = "username";
    private static final String OTHER_PERSON_ID = "userid";

    private static final DateFormat DATE_DIVIDER_DATE_FORMAT = new SimpleDateFormat("MMMM d");

    private int mSkip = 0;
    private boolean mCanLoadMore = true;

    //private static final String USER_COUNT = "usercount";
    //private static final String CHAT_HEADS = "chatheads";
    //private static final int TYPING_TIMER_LENGTH = 600;

    private String mRoomId;

    private String mOtherPersonId;
    private String mOtherPersonName; //name of person youre talking to
    private String mOtherPersonProfileImage;

    private String mUserId; //our user id

    private JSONObject newMessage;
    private JSONObject typingJson;
    private JSONObject joinLeft;
    private JSONObject delivered;

    private RecyclerView recList;
    private EditText mInputMessageView;
    private TextView mTopDateHeaderTV;
    private ChatAdapter mChatAdapter;


    private boolean mOtherUserTyping = false;
    private boolean mAmAlreadyTyping = false;

    private View vSendButton;


    private List<Chat> mChatList = new ArrayList<>();

    private View vEmptyChatView;

    private View mProgressBar;

    private static final int ATTACH_PHOTO_OR_IMAGE = 32;
    private Uri mVideoUri;
    private Uri mImageUri;
    private String mMessageText;
    private int mAttachType = -1;


    private Handler mHandler = new Handler();
    private LinearLayoutManager mLinearLayoutManager;

    //private SharedPreferences mSharedPreferences;

    //private int mRoomUsersCnt;
    //private Handler mTypingHandler = new Handler();
    //private Socket mSocket;
    //private int mLastRead;
    //private String mLastReadId;
    //private List<ChatHead> mChatHeadList;
    //private List<ChatHead> mChatHeadAddedList;
    //private Map<String, Integer> mChatHeadPos = new HashMap<String, Integer>();


    public ChatFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mChatAdapter = new ChatAdapter(getActivity(), mChatList);
        //mChatHeadList = new ArrayList<>();
        //mChatHeadAddedList = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomId          id of room.
     * @param otherPersonName full name of person youre talking to.
     * @param otherPersonId   id of person youre talking to
     * @return A new instance of fragment ChatFragment.
     */
    //, int roomUsersCnt, ArrayList<ChatHead> chatHeadList
    public static ChatFragment newInstance(String roomId,
                                           String otherPersonName,
                                           String otherPersonId) {

        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();

        args.putString(ROOM_ID, roomId);
        args.putString(OTHER_PERSON_NAME, otherPersonName);
        args.putString(OTHER_PERSON_ID, otherPersonId);

        //args.putInt(USER_COUNT, roomUsersCnt);
        //args.putParcelableArrayList(CHAT_HEADS, chatHeadList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
            mOtherPersonName = getArguments().getString(OTHER_PERSON_NAME);
            mOtherPersonId = getArguments().getString(OTHER_PERSON_ID);
            //mRoomUsersCnt = getArguments().getInt(USER_COUNT);
            //mChatHeadList = getArguments().getParcelableArrayList(CHAT_HEADS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.chat_fragment_toolbar);

        View otherPersonHeader = inflater.inflate(R.layout.toolbar_chat, toolbar, false);
        TextView otherPersonNameTV = (TextView) otherPersonHeader.findViewById(R.id.toolbar_chat_user_name);
        otherPersonNameTV.setText(mOtherPersonName);
        updateRoomIconView();
        toolbar.addView(otherPersonHeader);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.addFragmentToContainer(TaptUserProfileFragment.newInstance(mOtherPersonName, mOtherPersonId));
                }
            }
        });

        mTopDateHeaderTV = (TextView) view.findViewById(R.id.top_date_header);

        //when reaches end of list, we want to try to load more
        mChatAdapter.setLoadMoreListener(this);

        vEmptyChatView = view.findViewById(R.id.empty_view_messanger);
        mProgressBar = view.findViewById(R.id.chat_load_progress);

        mUserId = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", null);

        //when press attach photo or video: start intent
        view.findViewById(R.id.attach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), CameraActivity.class);
                i.putExtra(CameraActivity.CAMERA_TYPE, CameraActivity.CAMERA_AND_VIDEO_AND_GALLERY);
                i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.RETURN_URI);
                startActivityForResult(i, ATTACH_PHOTO_OR_IMAGE);
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.chat_list);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLinearLayoutManager.setStackFromEnd(true);
        recList.setLayoutManager(mLinearLayoutManager);
        recList.setAdapter(mChatAdapter);
        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateTopHeader();
            }
        });
        recList.setOnTouchListener(new View.OnTouchListener() {
            private float lastX = 0;
            private float lastY = 0;

            private float startX = 0;
            private float startY = 0;


            boolean isDragging = false;
            private int preDrag = 9;

            private final int MIN_PULL = 0;
            private final int MAX_PULL = (int) (100 * getActivity().getResources().getDisplayMetrics().density);
            private final int THRESHOLD = (int) (0 * getActivity().getResources().getDisplayMetrics().density);


            private int totalOffset = 0;

            ValueAnimator animator;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(animator != null && animator.isRunning()){
                            animator.cancel();
//                            Log.i("TimeAnimation", "canceled : "+totalOffset);
                            isDragging = true;
                            preDrag = THRESHOLD;
                        }else{
                            totalOffset = 0;
                            isDragging = false;
                            preDrag = 0;
                        }

                        startX = lastX = motionEvent.getRawX();
                        startY = lastY = motionEvent.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        float x = motionEvent.getRawX();
                        float y = motionEvent.getRawY();
                        int dX = (int) (x - lastX);
                        int dY = (int) (y - lastY);
                        lastX = x;
                        lastY = y;
//                        Log.i(TAG, "dX:" + dX + " dY:" + dY);

                        if(!isDragging && Math.abs(dX/5) < Math.abs(dY)){return false;}

                        if (preDrag >= THRESHOLD) {
                            isDragging = true;
                        }

                        if (isDragging) {
                            recList.stopScroll();
                            if (totalOffset + dX > MAX_PULL) {
                                mLinearLayoutManager.offsetChildrenHorizontal(MAX_PULL - totalOffset);
                                totalOffset = MAX_PULL;
                            } else if (totalOffset + dX < MIN_PULL) {
                                mLinearLayoutManager.offsetChildrenHorizontal(MIN_PULL - totalOffset);
                                totalOffset = MIN_PULL;
                            } else {
                                totalOffset += dX;
                                mLinearLayoutManager.offsetChildrenHorizontal(dX);
                            }
//                            Log.i("TimeAnimation", "Dragging: "+totalOffset);
                            return true;

                        }else{
                            preDrag += dX;
                        }
                        //returns false to allow natural scrolling to occur
                        return false;
                    case MotionEvent.ACTION_UP:
                        if(totalOffset > 0) {
                            animator = ValueAnimator.ofInt(0, -totalOffset);
                            animator.setDuration(250)
                                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        int lastVal = 0;

                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            recList.stopScroll();
                                            int val = (Integer) valueAnimator.getAnimatedValue();
                                            mLinearLayoutManager.offsetChildrenHorizontal(val - lastVal);
                                            totalOffset += (val - lastVal);
//                                            Log.i("TimeAnimation", "Animation: " + totalOffset);
                                            lastVal = val;
                                        }
                                    });
                            animator.start();
                        }

                        //mLinearLayoutManager.offsetChildrenHorizontal(-totalOffset);
                        isDragging = false;
                        preDrag = 0;
                        //totalOffset = 0;
                        return false;
                    default:
                        return false;
                }
            }
        });

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

        //we send socket for when user starts and stops typing
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 && mAmAlreadyTyping) { //stopped typing
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity == null || mUserId == null || !activity.socketConnected()) return;
                    activity.emitSocket(API_Methods.VERSION + ":messages:stop typing", typingJson);
                    mAmAlreadyTyping = false;
                } else if (s.length() != 0 && !mAmAlreadyTyping) { //started typing
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity == null || mUserId == null || !activity.socketConnected()) return;
                    activity.emitSocket(API_Methods.VERSION + ":messages:typing", typingJson);
                    mAmAlreadyTyping = true;
                }

                //change alpha of send button
                if (s.length() > 0) vSendButton.setAlpha(1);
                else vSendButton.setAlpha(0.25f);
            }
        });

        mInputMessageView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mAmAlreadyTyping) { //lost focus. stopped typing
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity == null || mUserId == null || !activity.socketConnected()) return;
                    activity.emitSocket(API_Methods.VERSION + ":messages:stop typing", typingJson);
                    mAmAlreadyTyping = false;
                }
            }
        });


        vSendButton = view.findViewById(R.id.send_button);
        vSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });

        //listen for attach photo or video
        view.findViewById(R.id.attach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;
                Intent i = new Intent(getActivity(), CameraActivity.class);
                i.putExtra(CameraActivity.CAMERA_TYPE, CameraActivity.CAMERA_AND_VIDEO_AND_GALLERY);
                i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.RETURN_URI);
                startActivityForResult(i, ATTACH_PHOTO_OR_IMAGE);
            }
        });


        //show keyboard when fragment appears
        mInputMessageView.requestFocus();
        mInputMessageView.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(mInputMessageView, 0);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //this gets called before onresume. we need socket connection before we can send
        //we'll have to save the info and send after we get connection
        if (requestCode == ATTACH_PHOTO_OR_IMAGE) {
            if (resultCode == RESULT_OK) {
                mAttachType = data.getIntExtra("type", -1);
                mImageUri = data.getParcelableExtra("image");
                mMessageText = data.getStringExtra("title");
                if (mAttachType == CameraActivity.VIDEO) {
                    mVideoUri = data.getParcelableExtra("video");
                }
            } else {
                mAttachType = -1;
                mImageUri = null;
                mVideoUri = null;
                mMessageText = "";
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateRoomIconView();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;

        if (mUserId == null) {
            Utils.showServerErrorToast(activity);
            return;
        }

        if (mRoomId == null) { //occurs when we didn't come from room fragment
            getRoomAndChat();

        } else if (getFragmentState() == FragmentState.NEEDS_UPDATING) {

            getChat();

            joinRoom(activity, false);
        } else {
            joinRoom(activity, true);

            //finished loading and there were no messages
            if (getFragmentState() == FragmentState.FINISHED_UPDATING && mChatList.isEmpty()) {
                vEmptyChatView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void joinRoom(BaseTaptActivity activity, boolean emitRefresh) {

        activity.connectSocket(Socket.EVENT_CONNECT_ERROR, onConnectError);
        activity.connectSocket(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        activity.connectSocket("new message", onNewMessage);
        activity.connectSocket("typing", onTyping);
        activity.connectSocket("stop typing", onStopTyping);
        activity.connectSocket("read", onRead);
        activity.connectSocket("joined", onJoin);
        activity.connectSocket("left", onLeave);
        activity.connectSocket("error", onError);
        activity.connectSocket("delivered", onDelivered);
        activity.connectSocket("messages refresh", onRefresh);

        typingJson = new JSONObject();
        joinLeft = new JSONObject();
        delivered = new JSONObject();

        try {
            typingJson.put("room", mRoomId);
            typingJson.put("user", mUserId);

            joinLeft.put("room", mRoomId);
            joinLeft.put("user", mUserId);

            delivered.put("user", mUserId);
            delivered.put("room", mRoomId);

            activity.emitSocket(API_Methods.VERSION + ":messages:joined", joinLeft);

            if (emitRefresh) {
                //check if we have new messages
                //we'll have to refresh fragment if thats the case
                JSONObject refresh = new JSONObject();
                refresh.put("room", mRoomId);
                activity.emitSocket(API_Methods.VERSION + ":messages:refresh", refresh);
            }

            //we have item we need to send
            if (mAttachType == CameraActivity.IMAGE) {
                sendImage(activity);
            } else if (mAttachType == CameraActivity.VIDEO) {
                sendVideo(activity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(activity);
        }
    }


    private void sendVideo(final BaseTaptActivity activity) {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    JSONObject postData = new JSONObject();
                    postData.put("message", mMessageText);
                    JSONArray images = new JSONArray();

                    //need thumbnail, get first frame
                    images.put(Utils.encodeImageBase64(
                            MediaStore.Images.Media.getBitmap(getActivity().getContentResolver()
                                    , Uri.fromFile(new File(mImageUri.getPath())))));

                    postData.put("images", images);

                    JSONArray video = new JSONArray();
                    video.put(Utils.encodeFileBase64(new File(mVideoUri.getPath())));

                    postData.put("videos", video);
                    postData.put("type", "2");
                    postData.put("owner", mUserId);

                    JSONArray coord = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    coord.put(0);
                    coord.put(0);
                    jsonObject.put("coordinates", coord);

                    postData.put("geo", jsonObject);
                    postData.put("room", mRoomId);

                    activity.emitSocket(API_Methods.VERSION + ":messages:new message", postData);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                mAttachType = -1;
                mImageUri = null;
            }
        }).subscribeOn(Schedulers.newThread())
                .subscribe();
    }


    private void sendImage(final BaseTaptActivity activity) {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    JSONObject postData = new JSONObject();
                    postData.put("message", mMessageText);
                    JSONArray images = new JSONArray();

                    //get bitmap from uri
                    images.put(Utils.encodeImageBase64(
                            MediaStore.Images.Media.getBitmap(getActivity().getContentResolver()
                                    , Uri.fromFile(new File(mImageUri.getPath())))));

                    postData.put("images", images);
                    postData.put("type", "1");
                    postData.put("owner", mUserId);

                    JSONArray coord = new JSONArray();
                    JSONObject jsonObject = new JSONObject();
                    coord.put(0);
                    coord.put(0);
                    jsonObject.put("coordinates", coord);

                    postData.put("geo", jsonObject);
                    postData.put("room", mRoomId);

                    activity.emitSocket(API_Methods.VERSION + ":messages:new message", postData);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

                mAttachType = -1;
                mImageUri = null;
            }
        }).subscribeOn(Schedulers.newThread())
                .subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity != null && mUserId != null && mRoomId != null) {
            activity.emitSocket(API_Methods.VERSION + ":messages:left", joinLeft);

            activity.disconnectSocket(Socket.EVENT_CONNECT_ERROR, onConnectError);
            activity.disconnectSocket(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);

            activity.disconnectSocket(Socket.EVENT_CONNECT_ERROR, onConnectError);
            activity.disconnectSocket(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
            activity.disconnectSocket("new message", onNewMessage);
            activity.disconnectSocket("typing", onTyping);
            activity.disconnectSocket("stop typing", onStopTyping);
            activity.disconnectSocket("read", onRead);
            activity.disconnectSocket("joined", onJoin);
            activity.disconnectSocket("left", onLeave);
            activity.disconnectSocket("error", onError);
            activity.disconnectSocket("delivered", onDelivered);
            activity.disconnectSocket("messages refresh", onRefresh);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        //hide keyboard
        if (mInputMessageView.hasFocus() && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) return;
            imm.hideSoftInputFromWindow(mInputMessageView.getWindowToken(), 0);
        }
    }

    private void updateRoomIconView() {
        View rootV = getView();
        if (rootV == null) return;
        Toolbar toolbar = (Toolbar) rootV.findViewById(R.id.chat_fragment_toolbar);
        ImageView otherPersonIconIV = (ImageView) toolbar.findViewById(R.id.toolbar_chat_user_icon);


        if (mOtherPersonProfileImage == null) {
            otherPersonIconIV.setVisibility(View.GONE);

        } else {
            Context context = rootV.getContext();
            Glide.with(context)
                    .load(Utils.getImageUrlOfUser(mOtherPersonProfileImage))
                    .dontAnimate()
                    .signature(new StringSignature(context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000")))
                    .placeholder(R.drawable.image_loading_background)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                    .listener(mGlideListener)
                    .into(otherPersonIconIV);
        }
    }

    private RequestListener<String, GlideDrawable> mGlideListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            View rootV = getView();
            if (rootV == null) return false;
            Toolbar toolbar = (Toolbar) rootV.findViewById(R.id.chat_fragment_toolbar);
            ImageView otherPersonIconIV = (ImageView) toolbar.findViewById(R.id.toolbar_chat_user_icon);
            otherPersonIconIV.setVisibility(View.VISIBLE);
            return false;
        }
    };


    private void getRoomAndChat() {
        if (getActivity() == null ||
                mUserId == null ||
                mOtherPersonId == null ||
                getFragmentState() == FragmentState.LOADING_DATA) return;

        setFragmentState(FragmentState.LOADING_DATA);
        mProgressBar.setVisibility(View.VISIBLE);

        JSONArray users = new JSONArray();
        users.put(mUserId);
        users.put(mOtherPersonId);

        new LSDKChat(getActivity()).getPastMessages(users, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        Log.i(TAG, "onResponse: " + object.toString(4));
                        mRoomId = object.getString("id");
//                        mOtherPersonProfileImage = object.getJSONObject("room").getJSONObject("owner").getString("profileImage");

                        JSONArray messages = object.getJSONArray("messages");
                        mSkip = object.getInt("totalCount") - 20;

                        final ArrayList<Chat> tempChatList = new ArrayList<>();
                        JSONArray listOfUnreadMessages = new JSONArray();

                        parseMessagesJSON(messages, tempChatList, listOfUnreadMessages);

                        if (mSkip <= 0) {
                            mCanLoadMore = false;
                            mChatAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                        }

                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            joinRoom(activity, false);

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mChatList.clear();
                                    mChatList.addAll(tempChatList);
                                    mSkip -= 20;

                                    //show empty view
                                    if (mChatList.isEmpty()) {
                                        vEmptyChatView.setVisibility(View.VISIBLE);
                                    } else {
                                        updateTopHeader();
                                    }

                                    mProgressBar.setVisibility(View.GONE);
                                    mChatAdapter.notifyDataSetChanged();
                                    scrollToBottom();
                                }
                            });

                            //there were messages we need to mark as read
                            if (listOfUnreadMessages.length() > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("room", mRoomId);
                                obj.put("messages", listOfUnreadMessages);
                                activity.emitSocket(API_Methods.VERSION + ":messages:read", obj);
                            }
                        }
                    } catch (JSONException e) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        });
    }

    private void getChat() {

        if (getActivity() == null || getFragmentState() == FragmentState.LOADING_DATA) return;

        Map<String, String> chat = new HashMap<>();
        chat.put(ROOM_ID, mRoomId);


        setFragmentState(FragmentState.LOADING_DATA);
        if (mChatList.isEmpty()) mProgressBar.setVisibility(View.VISIBLE);

        new LSDKChat(getActivity()).getChat(chat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setFragmentState(FragmentState.FINISHED_UPDATING);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        Log.i(TAG, "onResponse: " + object.toString(4));
                        JSONArray messages = object.getJSONArray("messages");

                        mSkip = object.getInt("skip");

                        final ArrayList<Chat> tempChatList = new ArrayList<>();
                        JSONArray listOfUnreadMessages = new JSONArray();
                        parseMessagesJSON(messages, tempChatList, listOfUnreadMessages);

                        JSONArray users = object.getJSONObject("room").getJSONArray("users");
                        if (mOtherPersonProfileImage == null && mUserId != null) {
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                if (!mUserId.equals(user.getString("id"))) {
                                    mOtherPersonProfileImage = user.getString("profileImage");
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateRoomIconView();
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                        }

                        if (mSkip <= 0) {
                            mCanLoadMore = false;
                            mChatAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                        }

                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mChatList.clear();
                                    mChatList.addAll(tempChatList);
                                    mSkip -= 20;

                                    //show empty view
                                    if (mChatList.isEmpty()) {
                                        vEmptyChatView.setVisibility(View.VISIBLE);
                                    } else {

                                        updateTopHeader();
                                    }

                                    mProgressBar.setVisibility(View.GONE);

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mChatAdapter.notifyDataSetChanged();
                                            scrollToBottom();
                                        }
                                    });

                                }
                            });

                            //there were messages we need to mark as read
                            if (listOfUnreadMessages.length() > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("room", mRoomId);
                                obj.put("messages", listOfUnreadMessages);
                                activity.emitSocket(API_Methods.VERSION + ":messages:read", obj);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(activity);
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                setFragmentState(FragmentState.FINISHED_UPDATING);
            }
        });
    }


    private boolean haveRead(JSONArray peopleWhoRead) throws JSONException {
        if (mUserId == null || peopleWhoRead == null)
            return false;

        for (int i = 0; i < peopleWhoRead.length(); i++) {
            if (peopleWhoRead.getString(i).equals(mUserId))
                return true;
        }

        return false;
    }


    private void addMessage(JSONObject data) throws JSONException {
//        Log.d(TAG, "addMessage: " + data.toString(4));
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;

        Date time;

        try {
            time = Utils.getDateFormat().parse(data.getString("date"));
        } catch (ParseException | JSONException e) {
            time = null;
        }

        String owner = data.getJSONObject("owner").getString("id");
        String messageId = data.getString("id");


        final Chat chat = new Chat(
                mRoomId,
                time,
                owner,
                messageId,
                data.getString("text"),
                false,
                owner.equals(mUserId)
        );

        JSONArray imageAndVideo = data.getJSONArray("images");
        if (imageAndVideo.length() > 0) {
            chat.setImageId(imageAndVideo.getString(0));
            imageAndVideo = data.getJSONArray("videos");
            if (imageAndVideo.length() > 0) {
                chat.setVideoId(imageAndVideo.getString(0));
                chat.setMessageType(Chat.MESSAGE_VIDEO);
            } else {
                chat.setMessageType(Chat.MESSAGE_IMAGE);
            }
        }


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatList.add(chat);
                mChatAdapter.notifyItemInserted(mChatList.size() - 1);
                scrollToBottom();
            }
        });


        if (!owner.equals(mUserId)) {//not our message, then mark as read
            JSONObject read = new JSONObject();
            JSONArray readArray = new JSONArray();
            readArray.put(messageId);

            read.put("messages", readArray);
            read.put("room", mRoomId);

            activity.emitSocket(API_Methods.VERSION + ":messages:read", read);
        }
    }

    private void addTyping() {
        if (mOtherUserTyping) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatList.add(new Chat(Chat.TYPE_ACTION_TYPING));
                mChatAdapter.notifyItemInserted(mChatList.size() - 1);
                scrollToBottom();
            }
        });

        mOtherUserTyping = true;
    }

    private void removeTyping() {
        if (getActivity() == null || !mOtherUserTyping) return;

        mOtherUserTyping = false;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int pos = mChatList.size() - 1;

                if (pos >= 0 && mChatList.get(pos).getType() == Chat.TYPE_ACTION_TYPING) {
                    mChatList.remove(pos);
                    mChatAdapter.notifyItemRemoved(pos);
                }
            }
        });
    }

//    private void removeChatHead(String username) {
//        for (int i = mChatList.size() - 1; i >= 0; i--) {
//            Chat message = mChatList.get(i);
//            // chat type head ; message get list size == 0 get list 0 element username
//            if (message.getType() == Chat.TYPE_ACTION && message.getUserName().equals(username)) {
//                mChatList.remove(i);
//                mChatAdapter.notifyItemRemoved(i);
//            }
//            // else if
//            // chat type head ; message get list size > 0
//            // for -> if uesrname = username
//            // list remove username
//            // update adapter
//        }
//    }

    private void attemptSend() {

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null || !activity.socketConnected() || mUserId == null
                || mRoomId == null || mProgressBar.getVisibility() == View.VISIBLE)
            return;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        newMessage = new JSONObject();

        try {
            newMessage.put("room", mRoomId);
            newMessage.put("user", mUserId);
            newMessage.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // perform the sending message attempt.
        activity.emitSocket(API_Methods.VERSION + ":messages:new message", newMessage);
    }


    private void scrollToBottom() {
        recList.post(new Runnable() {
            @Override
            public void run() {
                recList.scrollToPosition(mChatAdapter.getItemCount() - 1);
            }
        });
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            final BaseTaptActivity activity = (BaseTaptActivity) getActivity();

            if (activity == null) return;

            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {

                            final JSONObject data = (JSONObject) args[0];
                            removeTyping();

                            try {
                                addMessage(data);
                                delivered.put("id", data.getString("id"));
                                activity.emitSocket(API_Methods.VERSION + ":messages:delivered", delivered);

                                if (vEmptyChatView.getVisibility() == View.VISIBLE)
                                    vEmptyChatView.setVisibility(View.GONE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
            );
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addTyping();
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeTyping();
                }
            });
        }
    };


    private Emitter.Listener onLeave = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.length != 0) {
                        try {
                            Log.d(TAG, "run: " + ((JSONObject) args[0]).toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.length != 0) {
                        try {
                            Log.d(TAG, "run: " + ((JSONObject) args[0]).toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showServerErrorToast(getActivity());
                }
            });
        }
    };

    private Emitter.Listener onRead = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;

            for (int i = mChatList.size() - 1; i >= 0; i--) {
                if (!mChatList.get(i).isRead()) {
                    mChatList.get(i).setIsRead(true);
                } else { //stop when reached one already marked as read
                    break;
                }
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mChatAdapter.notifyDataSetChanged();
                        }
                    });
//                        removeChatHead(data.getOwner)
                }
            });
        }
    };


    private Emitter.Listener onRefresh = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                if (new JSONObject(args[0].toString()).getBoolean("reload"))
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getChat();
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onDelivered = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.length != 0) {
                        try {
                            Log.d(TAG, "runDelivered: " + ((JSONObject) args[0]).toString(4));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getFragmentManager().findFragmentByTag(RoomsActivityFragment.TAG) == null) {
            BaseTaptActivity activity = (BaseTaptActivity) getActivity();
            if (activity != null) {
                activity.emitSocket(API_Methods.VERSION + ":messages:unread", new JSONObject());
            }
        }
    }

    private boolean mLoadingMoreMessages = false;

    @Override
    public void loadMore() {
        if (!mCanLoadMore || mLoadingMoreMessages) return;

        mLoadingMoreMessages = true;

        Map<String, String> chat = new HashMap<>();
        chat.put(ROOM_ID, mRoomId);

        if (mSkip < 0) {
            chat.put("skip", "0");
            chat.put("limit", (20 + mSkip) + "");
            mSkip = 0;
        } else {
            chat.put("skip", mSkip + "");
        }

        new LSDKChat(getActivity()).getChat(chat, new Callback() {
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
                    try {
                        JSONObject object = new JSONObject(response.body().string());

                        //Log.i(TAG, "onResponse: "+object);
                        final JSONArray messages = object.getJSONArray("messages");

                        final ArrayList<Chat> tempChatList = new ArrayList<>();
                        JSONArray listOfUnreadMessages = new JSONArray();
                        parseMessagesJSON(messages, tempChatList, listOfUnreadMessages);

                        JSONArray users = object.getJSONObject("room").getJSONArray("users");
                        if (mOtherPersonProfileImage == null && mUserId != null) {
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject user = users.getJSONObject(i);
                                if (!mUserId.equals(user.getString("id"))) {
                                    mOtherPersonProfileImage = user.getString("profileImage");
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateRoomIconView();
                                            }
                                        });
                                    }
                                    break;
                                }
                            }
                        }


                        if (mSkip == 0) {
                            mCanLoadMore = false;
                            mChatAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                        }

                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mChatList.addAll(0, tempChatList);

                                            updateTopHeader();

                                            mSkip -= 20;
                                            mChatAdapter.notifyItemRangeInserted(0, tempChatList.size());
                                            mLoadingMoreMessages = false;
                                        }
                                    });
                                }
                            });

                            //there were messages we need to mark as read
                            if (listOfUnreadMessages.length() > 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("room", mRoomId);
                                obj.put("messages", listOfUnreadMessages);
                                activity.emitSocket(API_Methods.VERSION + ":messages:read", obj);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(activity);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    /**
     * Parses JSON containing message data, adding in date headers as necessary
     *
     * @param messages             JSONArray of messages
     * @param intoChatList         ArrayList to add Chat objects to
     * @param listOfUnreadMessages JSONArray to populate with unread message data
     */

    private void parseMessagesJSON(JSONArray messages, final ArrayList<Chat> intoChatList, JSONArray listOfUnreadMessages) {
        // final ArrayList<Chat> intoChatList = new ArrayList<>();
        JSONObject message;
        Chat chat;
        String owner;
        Date time;
        JSONArray imageAndVideo;

        boolean viewerIsOwnerOfMessage;
        boolean messageBeenRead;

        SimpleDateFormat format = Utils.getDateFormat();

        for (int i = 0; i < messages.length(); i++) {
            try {
                message = messages.getJSONObject(i);
                owner = message.getJSONObject("owner").getString("id");
                viewerIsOwnerOfMessage = owner.equals(mUserId);

                messageBeenRead = true;

                if (!viewerIsOwnerOfMessage) { //other person's message. we need to check if we read it
                    messageBeenRead = haveRead(message.getJSONArray("read"));
                }

                try {
                    time = format.parse(message.getString("date"));
                } catch (ParseException | JSONException e) {
                    time = null;
                }

                chat = new Chat(
                        message.getString("room"),
                        time,
                        owner,
                        message.getString("id"),
                        message.getString("text"),
                        messageBeenRead,
                        viewerIsOwnerOfMessage
                );

                if (!messageBeenRead) {
                    listOfUnreadMessages.put(chat.getMessageId());
                }

                imageAndVideo = message.getJSONArray("images");
                if (imageAndVideo.length() > 0) {
                    chat.setImageId(imageAndVideo.getString(0));
                    imageAndVideo = message.getJSONArray("videos");
                    if (imageAndVideo.length() > 0) {
                        chat.setVideoId(imageAndVideo.getString(0));
                        chat.setMessageType(Chat.MESSAGE_VIDEO);
                    } else {
                        chat.setMessageType(Chat.MESSAGE_IMAGE);
                    }
                }

                if (intoChatList.size() > 0) {
                    Chat previousMessage = intoChatList.get(intoChatList.size() - 1);
                    if (chat.getDate().getDate() != previousMessage.getDate().getDate()) {
                        Date date = chat.getDate();
                        Chat header = new Chat(
                                previousMessage.getRoomId(),
                                previousMessage.getDate(),
                                chat.getOwnerId(),
                                "-1",
                                (new Date().getDate() != date.getDate() ? DATE_DIVIDER_DATE_FORMAT.format(date) : "Today"),
                                true,
                                true
                        );
                        header.setType(Chat.TYPE_DATE_HEADER);
                        intoChatList.add(header);
                    }
                }

                intoChatList.add(chat);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void updateTopHeader() {
        //-1 to compensate for footer
        int topItemIndex = mLinearLayoutManager.findFirstVisibleItemPosition() - 1;

        if (topItemIndex >= 0 && topItemIndex < mChatList.size()) {
            //sets top date header to date of first visible item
            mTopDateHeaderTV.setVisibility(View.VISIBLE);
            Date date = mChatList.get(topItemIndex).getDate();
            mTopDateHeaderTV.setText(new Date().getDate() != date.getDate() ? DATE_DIVIDER_DATE_FORMAT.format(date) : "Today");
        }
    }
}
