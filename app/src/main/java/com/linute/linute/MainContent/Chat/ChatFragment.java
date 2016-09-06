package com.linute.linute.MainContent.Chat;


import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.MainContent.CreateContent.Gallery.GalleryActivity;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.EditScreen.EditFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.linute.linute.SquareCamera.CameraType;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.CustomSnackbar;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private static final String ARG_ROOM_ID = "room";
    //    private static final String OTHER_PERSON_NAME = "username";
//    private static final String OTHER_PERSON_ID = "userid";
    private static final String ARG_USERS = "users";
    private static final String ARG_ROOM = "chatroom";

    private static final DateFormat DATE_DIVIDER_DATE_FORMAT = new SimpleDateFormat("MMMM d");

    private int mSkip = 0;
    private boolean mCanLoadMore = true;

    //private static final String USER_COUNT = "usercount";
    //private static final String CHAT_HEADS = "chatheads";
    //private static final int TYPING_TIMER_LENGTH = 600;

    private String mRoomId;
    private boolean mRoomExists = false;

    //    private String mOtherPersonId;
    private ArrayList<User> mUsers;

    public static final int CHAT_TYPE_DM = 0;
    public static final int CHAT_TYPE_GROUP = 1;

    private ChatRoom mChatRoom;

    private int mChatType;
    private String mChatName;
    private String mChatImage;
    //    private String mOtherPersonName; //name of person youre talking to
    private String mOtherPersonProfileImage;

    private String mUserId; //our user id

    private JSONObject newMessage;
    private JSONObject typingJson;
    private JSONObject joinLeft;
    private JSONObject delivered;

    private RecyclerView recList;
    private EditText mInputMessageView;
    //    private TextView mTopDateHeaderTV;
    private ChatAdapter mChatAdapter;


    private boolean mOtherUserTyping = false;
    private boolean mAmAlreadyTyping = false;

    private View vSendButton;

    View vPreChat;

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
    private Map<String, User> mUserMap;

    private boolean mSocketConnected = false;

    private AlertDialog mAlertDialog;


    public ChatFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mUserMap = new HashMap<>();
        mChatAdapter = new ChatAdapter(getActivity(), mChatList, mUserMap);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomId               id of room.
     * @param otherPersonFirstName first name of person youre talking to.
     * @param otherPersonLastName  last name
     * @param otherPersonId        id of person youre talking to
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance(String roomId,
                                           String otherPersonFirstName,
                                           String otherPersonLastName,
                                           String otherPersonId) {

        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();

        args.putString(ARG_ROOM_ID, roomId);

        ArrayList<User> users = new ArrayList<>(1);
        users.add(new User(otherPersonId, otherPersonFirstName, otherPersonLastName, ""));
        args.putParcelableArrayList(ARG_USERS, users);
        fragment.setArguments(args);
        return fragment;
    }


    public static ChatFragment newInstance(String roomId, ArrayList<User> userList) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putParcelableArrayList(ARG_USERS, userList);
        fragment.setArguments(args);
        return fragment;
    }

    public static ChatFragment newInstance(ChatRoom chatRoom) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ROOM, chatRoom);
        args.putString(ARG_ROOM_ID, chatRoom.roomId);
        args.putParcelableArrayList(ARG_USERS, chatRoom.users);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {

            mChatRoom = arguments.getParcelable(ARG_ROOM);

            if (mChatRoom != null) {
                mRoomId = mChatRoom.roomId;
                mUsers = mChatRoom.users;
                mChatName = mChatRoom.getRoomName();
            } else {
                mRoomId = arguments.getString(ARG_ROOM_ID);
                mUsers = arguments.getParcelableArrayList(ARG_USERS);
                if (mUsers != null) {
                    for (User user : mUsers) {
                        mUserMap.put(user.userId, user);
                    }
                }
            }
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.chat_fragment_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        //updateToolbar();

       /* if (isDM()) {
            toolbar.setOnItemTouchListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
//                        activity.addFragmentToContainer(TaptUserProfileFragment.newInstance());
                    }
                }
            });
        }*/


        View settingsButton = toolbar.findViewById(R.id.toolbar_chat_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).addFragmentToContainer(ChatSettingsFragment.newInstance(mChatRoom));
            }
        });


//        mTopDateHeaderTV = (TextView) view.findViewById(R.id.top_date_header);

        //when reaches end of list, we want to try to load more
        mChatAdapter.setLoadMoreListener(this);

        vEmptyChatView = view.findViewById(R.id.empty_view_messanger);
        mProgressBar = view.findViewById(R.id.chat_load_progress);

        mUserId = Utils.getMyId(getContext());

        return view;
    }


    private void showCameraGalleryOption() {
        if (getContext() == null) return;
        mAlertDialog = new AlertDialog.Builder(getContext()).setItems(
                new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i;
                        if (which == 0) {
                            i = new Intent(getContext(), CameraActivity.class);
                            i.putExtra(CameraActivity.CAMERA_TYPE, new CameraType(CameraType.CAMERA_PICTURE).add(CameraType.CAMERA_STATUS).add(CameraType.CAMERA_VIDEO));
                            i.putExtra(CameraActivity.CONTENT_SUB_TYPE, EditFragment.ContentSubType.Chat);
                        }
                        else {
                            i = new Intent(getContext(), GalleryActivity.class);
                            i.putExtra(GalleryActivity.ARG_GALLERY_TYPE, CameraActivity.ALL);
                        }

                        i.putExtra(CameraActivity.RETURN_TYPE, CameraActivity.RETURN_URI);

                        startActivityForResult(i, ATTACH_PHOTO_OR_IMAGE);
                    }
                }).show();
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
        recList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if(view.getHeight() >= ((View)view.getParent()).getHeight()){
                    view.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
                }else{
                    view.setOverScrollMode(View.OVER_SCROLL_NEVER);
                }
            }
        });
       /* recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateTopTimeHeader();
            }
        });*/
        final int width = getResources().getDisplayMetrics().widthPixels;
//        recList.getLayoutParams().width = width;
        recList.setOnTouchListener(new View.OnTouchListener() {
            private float lastX = 0;
            private float lastY = 0;

            boolean isDragging = false;
            private int preDrag = 0;

            private final int MIN_PULL = (int) (0 * getActivity().getResources().getDisplayMetrics().density);

            private final int MAX_PULL = (int) (100 * getActivity().getResources().getDisplayMetrics().density);
            private final int THRESHOLD = (int) (0 * getActivity().getResources().getDisplayMetrics().density);


            private int totalOffset = 0;

            ValueAnimator animator;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (animator != null && animator.isRunning()) {
                            animator.cancel();
                            isDragging = true;
                            preDrag = THRESHOLD;
                        } else {
                            totalOffset = 0;
                            isDragging = false;
                            preDrag = 0;
                        }

                        lastX = motionEvent.getRawX();
                        lastY = motionEvent.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:

                        float x = motionEvent.getRawX();
                        float y = motionEvent.getRawY();
                        int dX = (int) (x - lastX);
                        int dY = (int) (y - lastY);
                        lastX = x;
                        lastY = y;
//                        Log.i(TAG, "dX:" + dX + " dY:" + dY);

                        if (!isDragging && Math.abs(dX / 5) < Math.abs(dY)) {
                            return false;
                        }

                        if (preDrag >= THRESHOLD) {
                            isDragging = true;
                        }

                        if (isDragging) {
                            if (totalOffset + dX > MAX_PULL) {
                                totalOffset = MAX_PULL;
                            } else if (totalOffset + dX < MIN_PULL) {
                                totalOffset = MIN_PULL;
                            } else {
                                totalOffset += dX;
                            }

//                            recList.getLayoutParams().width = width - totalOffset;
                            recList.setX(totalOffset);
//                            recList.requestLayout();


                            if (totalOffset == 0) {
                                isDragging = false;
                            }
                        } else {
                            preDrag += dX;
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        if (totalOffset != 0) {
                            animator = ValueAnimator.ofInt(totalOffset, 0);
                            animator.setDuration(250)
                                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            recList.stopScroll();
                                            totalOffset = (Integer) valueAnimator.getAnimatedValue();

                                            Activity a = getActivity();

//                                            recList.getLayoutParams().width = width - totalOffset;
                                            recList.setX(totalOffset);
//                                            recList.requestLayout();
                                        }
                                    });
                            animator.start();
                        }

                        isDragging = false;
                        preDrag = 0;
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
                if (s.toString().matches("^[\\n\\s]+$")) {
                    mInputMessageView.setText("");
                }

                final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (s.length() == 0 && mAmAlreadyTyping) { //stopped typing
                    if (activity == null || mUserId == null || !activity.socketConnected())
                        return;
                    activity.emitSocket(API_Methods.VERSION + ":messages:stop typing", typingJson);
                    mAmAlreadyTyping = false;
                } else if (s.length() != 0 && !mAmAlreadyTyping) { //started typing
                    if (activity == null || mUserId == null || !activity.socketConnected())
                        return;
                    activity.emitSocket(API_Methods.VERSION + ":messages:typing", typingJson);
                    mAmAlreadyTyping = true;
                }

                //change alpha of send button
                if (!s.toString().trim().isEmpty()) vSendButton.setAlpha(1);
                else vSendButton.setAlpha(0.25f);

                if (mAmAlreadyTyping) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            activity.emitSocket(API_Methods.VERSION + ":messages:stop typing", typingJson);
                        }
                    }, 3000);
                }

            }
        });

        mInputMessageView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mAmAlreadyTyping) { //lost focus. stopped typing
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    if (activity == null || mUserId == null || !activity.socketConnected())
                        return;
                    activity.emitSocket(API_Methods.VERSION + ":messages:stop typing", typingJson);
                    mAmAlreadyTyping = false;
                }
            }
        });


        vSendButton = view.findViewById(R.id.send_button);
        vSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mInputMessageView.getText().toString().trim();
                if (message.isEmpty()) return;
                attemptSend(message);
                mInputMessageView.setText("");
            }
        });


        //show keyboard when fragment appears
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
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

        vPreChat = view.findViewById(R.id.pre_chat);
        vPreChat.setVisibility(View.GONE); //TODO some condition for prechat

        vPreChat.findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        vPreChat.findViewById(R.id.button_decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //when press attach photo or video: start intent
        view.findViewById(R.id.attach).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraGalleryOption();
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

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;

        if (mUserId == null) {
            Utils.showServerErrorToast(activity);
            return;
        }

        if (mRoomId == null) { //occurs when we didn't come from room fragment
            mRoomExists = false;
            getRoomAndChat();


        } else if (getFragmentState() == FragmentState.NEEDS_UPDATING) {

            getChat();//Chat();
            mRoomExists = true;

//            joinRoom(activity, false);
        } else {
            mRoomExists = true;
            getChat();
//            joinRoom(activity, true);

            //finished loading and there were no messages
        }

        if (getFragmentState() == FragmentState.FINISHED_UPDATING && mChatList.isEmpty()) {
            vEmptyChatView.setVisibility(View.VISIBLE);
        }

        updateToolbar();

    }

    public void joinRoom(BaseTaptActivity activity, boolean emitRefresh) {

        if (mSocketConnected) {
            leaveRooms();
        }

        mSocketConnected = true;


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

        activity.connectSocket("add users", onAddUsers);


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

            if (mAttachType < 0) return;
            //we have item we need to send
            if (mAttachType != CameraActivity.VIDEO) {
                sendImage(activity);
            } else {
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
                                    , mImageUri)));

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
                                    , mImageUri)));

                    postData.put("images", images);
                    postData.put("type", mAttachType);
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

        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }

        leaveRooms();
    }

    protected void leaveRooms() {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (joinLeft != null && activity != null && mUserId != null && mRoomId != null) {

            //because Max keeps complaining the we send him nulls into :messages:left
            try {
                joinLeft.put("room", mRoomId);
                joinLeft.put("user", mUserId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            activity.disconnectSocket("add users", onAddUsers);

            mSocketConnected = false;
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

    private void updateToolbar() {
        View rootV = getView();
        if (rootV == null) return;

        Toolbar toolbae = (Toolbar) rootV.findViewById(R.id.chat_fragment_toolbar);
        toolbae.setTitle(getChatName());
        View chatSettingsbutton = toolbae.findViewById(R.id.toolbar_chat_settings);
        chatSettingsbutton.setVisibility(mRoomExists ? View.VISIBLE : View.GONE);
    }


    private void getRoomAndChat() {
        if (getActivity() == null ||
                mUserId == null ||
                mUsers == null ||
                getFragmentState() == FragmentState.LOADING_DATA) return;

        setFragmentState(FragmentState.LOADING_DATA);
        mProgressBar.setVisibility(View.VISIBLE);

        final JSONArray users = new JSONArray();
        for (User user : mUsers) {
            users.put(user.userId);
        }
        users.put(mUserId);

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
                    BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                    try {
                        JSONObject object = new JSONObject(response.body().string());
                        //Log.d(TAG, "getroomandchat onResponse: " + object.toString(4));
                        mRoomId = object.getString("id");


                        //room doesn't exist
                        if (mRoomId.equals("null")) {
                            mRoomExists = false;
                            mRoomId = ObjectId.get().toString();
                            joinRoom(activity, true);
                            mCanLoadMore = false;
                            mChatAdapter.setFooterState(LoadMoreViewHolder.STATE_END);

                            //room doesn't exist, we stop loading.
                            //next message sent will create the room
                            if (activity != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                            setFragmentState(FragmentState.FINISHED_UPDATING);
                            return;
                        } else {
                            mRoomExists = true;
                            mChatType = object.getInt("type");
                            mChatName = object.getString("name");
                            mChatImage = object.getJSONObject("profileImage").getString("original");

                            if (activity != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateToolbar();
                                    }
                                });
                            }

                        }
//                        mOtherPersonProfileImage = object.getJSONObject("room").getJSONObject("owner").getString("profileImage");

                        JSONArray users = object.getJSONArray("users");
                        JSONObject user;
                        for (int i = 0; i < users.length(); i++) {
                            user = users.getJSONObject(i);
                            mUserMap.get(user.getString("id")).userImage = user.getString("profileImage");
                        }

                        JSONArray messages = object.getJSONArray("messages");
                        mSkip = object.getInt("totalCount") - 20;

                        final ArrayList<Chat> tempChatList = new ArrayList<>();
                        JSONArray listOfUnreadMessages = new JSONArray();

                        parseMessagesJSON(messages, tempChatList, listOfUnreadMessages);

                        if (mSkip <= 0) {
                            mCanLoadMore = false;
                            mChatAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                        }

                        sortLists(tempChatList);

                        activity = (BaseTaptActivity) getActivity();

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
//                                        updateTopTimeHeader();
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

                            Date date = null;
                            try {
                                Utils.getDateFormat().parse(object.getString("date"));
                            } catch (ParseException e) {

                            }

                            String unMuteAtString = object.getString("unMuteAt");
                            mChatRoom = new ChatRoom(
                                    mRoomId,
                                    mChatType,
                                    mChatName,
                                    mChatImage,
                                    mUsers,
                                    "",
                                    false,
                                    date == null ? 0 : ~date.getTime(),
                                    object.getBoolean("isMuted"),
                                    unMuteAtString.equals("null") ? 0 : Long.parseLong(unMuteAtString)
                            );

                            mChatAdapter.setIsDM(mChatRoom.isDM());


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
        chat.put(ARG_ROOM_ID, mRoomId);


        setFragmentState(FragmentState.LOADING_DATA);
        if (mChatList.isEmpty()) mProgressBar.setVisibility(View.VISIBLE);

        new LSDKChat(getActivity()).getChat(chat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "getchat failure");

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
                final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                if (response.isSuccessful()) {
                    try {

                        //Log.i(TAG, "getchat get chat");

                        JSONObject object = new JSONObject(response.body().string());

                        //Log.i(TAG, "get chat onResponse: "+object.toString(4));

                        JSONArray messages = object.getJSONArray("messages");

                        mSkip = object.getInt("skip");

                        final ArrayList<Chat> tempChatList = new ArrayList<>();
                        JSONArray listOfUnreadMessages = new JSONArray();
                        parseMessagesJSON(messages, tempChatList, listOfUnreadMessages);

                        JSONObject room = object.getJSONObject("room");
                        JSONArray users = room.getJSONArray("users");
                        mUsers.clear();
//                        if (mOtherPersonProfileImage == null && mUserId != null) {
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject userJSON = users.getJSONObject(i);
                            User user = new User(
                                    userJSON.getString("id"),
                                    userJSON.getString("firstName"),
                                    userJSON.getString("lastName"),
                                    userJSON.getString("profileImage"),
                                    userJSON.getJSONObject("college").getString("name")

                            );
                            mUsers.add(user);
                            mUserMap.put(userJSON.getString("id"), user);


                        }

                        mChatType = room.getInt("type");
                        mChatName = room.getString("name");
                        mChatImage = room.getJSONObject("profileImage").getString("original");


                        sortLists(tempChatList);

                        if (activity != null) {
                            joinRoom(activity, false);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateToolbar();
                                }
                            });
                        }
//                        }

                        if (mSkip <= 0) {
                            mCanLoadMore = false;
                            mChatAdapter.setFooterState(LoadMoreViewHolder.STATE_END);
                        }

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
//                                        updateTopTimeHeader();
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
                        Date date = null;
                        try {
                            Utils.getDateFormat().parse(room.getString("date"));
                        } catch (ParseException e) {

                        }

                        String unMuteAtString = room.getString("unMuteAt");
                        mChatRoom = new ChatRoom(
                                mRoomId,
                                mChatType,
                                mChatName,
                                mChatImage,
                                mUsers,
                                "",
                                false,
                                date == null ? 0 : ~date.getTime(),
                                room.getBoolean("isMuted"),
                                unMuteAtString.equals("null") ? 0 : Long.parseLong(unMuteAtString)
                        );

                        mChatAdapter.setIsDM(mChatRoom.isDM());


                    } catch (JSONException e) {
                        e.printStackTrace();
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
        //Log.d(TAG, "addMessage: " + data.toString(4));
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;


        JSONObject owner = data.getJSONObject("owner");
        String ownerId = owner.getString("id");
        boolean viewerIsOwnerOfMessage = ownerId.equals(mUserId);


        if (!mUserMap.containsKey(ownerId)) {
            mUserMap.put(ownerId,
                    new User(
                            ownerId,
                            owner.getString("firstName"),
                            owner.getString("lastName"),
                            owner.getString("profileImage")
                    ));
        }

        Date time;
        try {
            time = Utils.getDateFormat().parse(data.getString("date"));
        } catch (ParseException | JSONException e) {
            time = null;
        }

        final Chat chat = new Chat(
                data.getString("room"),
                time,
                ownerId,
                data.getString("id"),
                data.getString("text"),
                viewerIsOwnerOfMessage,
                viewerIsOwnerOfMessage
        );


        JSONArray unreadArray = new JSONArray();
        if (!viewerIsOwnerOfMessage) {
            unreadArray.put(chat.getMessageId());
        }


        JSONObject post = null;
        try {
            post = data.getJSONObject("post");
            if (post != null) {
                chat.setPost(getPost(post));
                chat.setMessageType(
                        chat.getPost().getType() == Post.POST_TYPE_VIDEO ?
                                Chat.MESSAGE_SHARE_VIDEO :
                                Chat.MESSAGE_SHARE_IMAGE
                );
            }
        } catch (JSONException e) {
            //e.printStackTrace();
//  Log.i(TAG, "parseMessagesJSON: none");
            //Log.d(TAG, "parseMessagesJSON: no value for post");
        }


        if (post == null) {
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
        }

        if (data.getInt("type") == 1) {
            chat.setType(Chat.TYPE_SYSTEM_MESSAGE);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mChatList.size() > 0) {
                    Chat previousMessage = mChatList.get(mChatList.size() - 1);
                    if (chat.getDate().getDate() != previousMessage.getDate().getDate()) {
                        Date date = getStartOfDay(chat.getDate());
                        Chat header = new Chat(
                                previousMessage.getRoomId(),
                                date,
                                chat.getOwnerId(),
                                "-1",
                                (sameDay(date, new Date()) ? "Today" : DATE_DIVIDER_DATE_FORMAT.format(date)),
                                true,
                                true
                        );
                        header.setType(Chat.TYPE_DATE_HEADER);
                        mChatList.add(header);
                    }
                } else {
                    Date date = getStartOfDay(chat.getDate());
                    Chat header = new Chat(
                            chat.getRoomId(),
                            date,
                            chat.getOwnerId(),
                            "-1",
                            (sameDay(date, new Date()) ? "Today" : DATE_DIVIDER_DATE_FORMAT.format(date)),
                            true,
                            true
                    );
                    header.setType(Chat.TYPE_DATE_HEADER);
                    mChatList.add(header);
                }


                mChatList.add(chat);
                mChatAdapter.notifyItemInserted(mChatList.size());
                scrollToBottom();
//                updateTopTimeHeader();
            }
        });


        if (unreadArray.length() > 0) {//not our message, then mark as read
            JSONObject read = new JSONObject();
            read.put("room", mRoomId);
            read.put("messages", unreadArray);
            activity.emitSocket(API_Methods.VERSION + ":messages:read", read);
        }
    }

    private void addTyping() {
        if (mOtherUserTyping) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatList.add(new Chat(Chat.TYPE_ACTION_TYPING));
                mChatAdapter.notifyItemInserted(mChatList.size());
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
                    mChatAdapter.notifyItemRemoved(pos + 1);
                }
            }
        });
    }

    private void attemptSend(String message) {

        message = message.replaceAll("[\\n\\s]+$", "");

        if (!mRoomExists) {
            createRoom(message);
            return;
        }

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null || !activity.socketConnected() || mUserId == null
                || mRoomId == null || mProgressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        newMessage = new JSONObject();

        JSONArray users = new JSONArray();
        for (User user : mUsers) {
            users.put(user.userId);
        }
        users.put(mUserId);

        try {
            newMessage.put("id", ObjectId.get().toString());
            newMessage.put("room", mRoomId);
//            newMessage.put("users", users);
            newMessage.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // perform the sending message attempt.
        activity.emitSocket(API_Methods.VERSION + ":messages:new message", newMessage);
    }

    private void createRoom(final String message) {
//        roomId = ObjectId.get().toString();

        final BaseTaptActivity activity = (BaseTaptActivity) getActivity();
//        joinRoom(activity, false);


      /*  if (!activity.socketConnected() || mUserId == null
                || mRoomId == null || mProgressBar.getVisibility() == View.VISIBLE) {
            return;
        }*/

        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        newMessage = new JSONObject();

        JSONArray users = new JSONArray();
        for (User user : mUsers) {
            users.put(user.userId);
        }
        users.put(mUserId);

        try {
            newMessage.put("id", ObjectId.get().toString());
            newMessage.put("room", mRoomId);
            newMessage.put("users", users);
            newMessage.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // perform the sending message attempt.
        activity.emitSocket(API_Methods.VERSION + ":messages:new message", newMessage);
        mRoomExists = true;
        mChatRoom = new ChatRoom(mRoomId, (mUsers.size() == 1 ? ChatRoom.ROOM_TYPE_DM : ChatRoom.ROOM_TYPE_GROUP), null, null, mUsers, "", false, 0, false, 0);
        updateToolbar();
        joinRoom(activity, false);
//        setFragmentState(FragmentState.FINISHED_UPDATING);
    }


    private void scrollToBottom() {

        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLinearLayoutManager.scrollToPositionWithOffset(mChatAdapter.getItemCount() - 1, Integer.MIN_VALUE);
                }
            });
        }

/*
        recList.post(new Runnable() {
            @Override
            public void run() {
                mLinearLayoutManager.scrollToPositionWithOffset(mChatAdapter.getItemCount()-1,Integer.MIN_VALUE);
            }
        });
*/
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
//            if (getActivity() == null) return;
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (args.length != 0) {
//                        try {
//                            Log.d(TAG, "run: " + ((JSONObject) args[0]).toString(4));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            if (getActivity() == null) return;
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (args.length != 0) {
//                        try {
//                            Log.d(TAG, "run: " + ((JSONObject) args[0]).toString(4));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
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
            for (int i = mChatList.size() - 1; i >= 0; i--) {
                if (!mChatList.get(i).isRead()) {
                    mChatList.get(i).setIsRead(true);
                } else { //stop when reached one already marked as read
                    break;
                }
            }

            if (getActivity() == null) return;
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
                                getRoomAndChat();
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
//            if (getActivity() == null) return;
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (args.length != 0) {
//                        try {
//                            Log.d(TAG, "runDelivered: " + ((JSONObject) args[0]).toString(4));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
        }
    };

    private Emitter.Listener onAddUsers = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
//            Log.d(TAG, args[0].toString());
            try {
                JSONObject event = new JSONObject(args[0].toString());
                JSONArray users = event.getJSONArray("users");
                if (getView() != null) {
                    StringBuilder names = new StringBuilder();
                    for (int i = 0; i < users.length(); i++) {
                        names.append(users.getJSONObject(i).getString("firstName"));
                    }
                    CustomSnackbar.make(getView(), " Added", CustomSnackbar.LENGTH_SHORT).show();

                }

            } catch (JSONException e) {

                if (getView() != null)
                    CustomSnackbar.make(getView(), "User(s) Added", CustomSnackbar.LENGTH_SHORT).show();
            }

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

    private boolean isDM() {
        return mChatType == CHAT_TYPE_DM;
    }

    @Override
    public void loadMore() {
        if (!mCanLoadMore || mLoadingMoreMessages || mRoomId == null) return;


        mLoadingMoreMessages = true;

        Map<String, String> chat = new HashMap<>();
        chat.put(ARG_ROOM_ID, mRoomId);

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
                                                updateToolbar();
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

                        sortLists(tempChatList);

                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mChatList.addAll(0, tempChatList);
//                                            updateTopTimeHeader();
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

    private void parseMessagesJSON(JSONArray messages, final List<Chat> intoChatList, JSONArray listOfUnreadMessages) {
        // final ArrayList<Chat> intoChatList = new ArrayList<>();
        JSONObject message;
        Chat chat;
        JSONObject owner;
        String ownerId;
        Date time;
        JSONArray imageAndVideo;
        JSONObject post = null;

        boolean viewerIsOwnerOfMessage;
        boolean messageBeenRead;

        SimpleDateFormat format = Utils.getDateFormat();

        for (int i = 0; i < messages.length(); i++) {
            try {
                message = messages.getJSONObject(i);

                //Log.d(TAG, "parseMessagesJSON: " + message.toString(4));
                owner = message.getJSONObject("owner");
                ownerId = owner.getString("id");
                viewerIsOwnerOfMessage = ownerId.equals(mUserId);


                if (!mUserMap.containsKey(ownerId)) {
                    mUserMap.put(ownerId,
                            new User(
                                    ownerId,
                                    owner.getString("firstName"),
                                    owner.getString("lastName"),
                                    owner.getString("profileImage")

                            ));
                }


                messageBeenRead = true;

                if (!viewerIsOwnerOfMessage) { //other person's message. we need to check if we read it
                    messageBeenRead = haveRead(message.getJSONArray("read"));
                }

                try {
                    time = format.parse(message.getString("date"));
                } catch (ParseException | JSONException e) {
                    time = null;
                }

                String text;
                try {
                    text = message.getString("text");
                }catch (JSONException e){
                    e.printStackTrace();
                    text = "";
                }
                chat = new Chat(
                        message.getString("room"),
                        time,
                        ownerId,
                        message.getString("id"),
                        text,
                        messageBeenRead,
                        viewerIsOwnerOfMessage
                );


                if (!messageBeenRead) {
                    listOfUnreadMessages.put(chat.getMessageId());
                }


                try {
                    post = message.getJSONObject("post");
                    if (post != null) {
                        chat.setPost(getPost(post));
                        chat.setMessageType(
                                chat.getPost().getType() == Post.POST_TYPE_VIDEO ?
                                        Chat.MESSAGE_SHARE_VIDEO :
                                        Chat.MESSAGE_SHARE_IMAGE
                        );
                    }
                } catch (JSONException e) {
//                    e.printStackTrace();
//                    //Log.i(TAG, "parseMessagesJSON: none");
                    //Log.d(TAG, "parseMessagesJSON: no value for post");
                }

                if (post == null) {
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
                }

                if (message.getInt("type") == 1) {
                    chat.setType(Chat.TYPE_SYSTEM_MESSAGE);
                }


                if (intoChatList.size() > 0) {
                    Chat previousMessage = intoChatList.get(intoChatList.size() - 1);
                    if (chat.getDate().getDate() != previousMessage.getDate().getDate()) {
                        Date date = getStartOfDay(chat.getDate());
                        Chat header = new Chat(
                                previousMessage.getRoomId(),
                                date,
                                chat.getOwnerId(),
                                "-1",
                                (sameDay(date, new Date()) ? "Today" : DATE_DIVIDER_DATE_FORMAT.format(date)),
                                true,
                                true
                        );
                        header.setType(Chat.TYPE_DATE_HEADER);
                        intoChatList.add(header);
                    }
                } else {
                    Date date = getStartOfDay(chat.getDate());
                    Chat header = new Chat(
                            chat.getRoomId(),
                            date,
                            chat.getOwnerId(),
                            "-1",
                            (sameDay(date, new Date()) ? "Today" : DATE_DIVIDER_DATE_FORMAT.format(date)),
                            true,
                            true
                    );
                    header.setType(Chat.TYPE_DATE_HEADER);
                    intoChatList.add(header);
                }

                intoChatList.add(chat);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sortLists(ArrayList<Chat> chatList) {
        Collections.sort(chatList, new Comparator<Chat>() {
            @Override
            public int compare(Chat lhs, Chat rhs) {
                return lhs.getDate().compareTo(rhs.getDate());
            }
        });
    }

    private Post getPost(JSONObject obj) throws JSONException {

        Post p = new Post(obj.getString("id"));

        JSONObject owner = obj.getJSONObject("owner");
        p.setUserId(owner.getString("id"));
        p.setAnonImage(Utils.getAnonImageUrl(obj.getString("anonymousImage")));
        p.setUserImage(Utils.getImageUrlOfUser(owner.getString("profileImage")));
        p.setUserName(owner.getString("fullName"));

        p.setPostPrivacy(obj.getInt("privacy"));

        int type = obj.getInt("type");
        p.setType(type);

        JSONArray images = obj.getJSONArray("images");
        if (images != null && images.length() > 0) {
            p.setImage(Utils.getEventImageURL(images.getString(0)));
        }

        images = obj.getJSONArray("videos");
        if (images != null && images.length() > 0) {
            p.setVideoURL(Utils.getVideoURL(images.getString(0)));
        }

        p.setPostLiked(obj.getBoolean("isLiked"));
        p.setTitle(obj.getString("title"));

        return p;
    }

  /*  private void updateTopTimeHeader() {
        //-1 to compensate for footer
        int topItemIndex = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition() - 1;
        if (topItemIndex > 0 && topItemIndex < mChatList.size()) {
            //sets top date header to date of first visible item
            mTopDateHeaderTV.setVisibility(View.VISIBLE);
            Date date = mChatList.get(topItemIndex).getDate();
            mTopDateHeaderTV.setText((sameDay(date, new Date()) ? "Today" : DATE_DIVIDER_DATE_FORMAT.format(date)));
        }else{
            mTopDateHeaderTV.setVisibility(View.INVISIBLE);
        }
    }*/

    public String getChatName() {
        StringBuilder builder = new StringBuilder();
        if (mUsers.size() <= 1) {
            for (User user : mUsers) {
                if (!user.userId.equals(mUserId)) {
                    builder.append(user.firstName);
                    builder.append(" ");
                    builder.append(user.lastName);
                }
            }
            return builder.toString();
        } else {

            if (!"".equals(mChatName) && mChatName != null) {
                return mChatName;
            } else {
                for (int i = 0; i < mUsers.size(); i++) {
                    builder.append(mUsers.get(i).firstName);
                    if (i != mUsers.size() - 1) {
                        builder.append(", ");
                    }
                }
            }
        }
        return builder.toString();
    }


    public boolean sameDay(Date date, Date date2) {
        return (date.getDate() == date2.getDate() && date.getMonth() == date2.getMonth() && date.getYear() == date2.getYear());
    }

    public Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
