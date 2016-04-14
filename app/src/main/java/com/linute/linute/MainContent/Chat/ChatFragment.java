package com.linute.linute.MainContent.Chat;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements ChatAdapter.LoadMoreListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String ROOM_ID = "room";
    private static final String OTHER_PERSON_NAME = "username";
    private static final String OTHER_PERSON_ID = "userid";
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private int mSkip = 0;
    private boolean mCanLoadMore = true;

    //private static final String USER_COUNT = "usercount";
    //private static final String CHAT_HEADS = "chatheads";
    //private static final int TYPING_TIMER_LENGTH = 600;

    private String mRoomId;

    private String mOtherPersonId;
    private String mOtherPersonName; //name of person youre talking to

    private String mUserId; //our user id

    private JSONObject newMessage;
    private JSONObject typingJson;
    private JSONObject joinLeft;
    private JSONObject delivered;

    private RecyclerView recList;
    private EditText mInputMessageView;
    private ChatAdapter mChatAdapter;


    private boolean mOtherUserTyping = false;
    private boolean mAmAlreadyTyping = false;

    private View vSendButton;


    private List<Chat> mChatList = new ArrayList<>();

    private boolean mLoadingMessages = true;

    private View vEmptyChatView;

    private View mLoadmoreProgressBar;
    private View mProgressBar;

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
    public static ChatFragment newInstance(String roomId, String otherPersonName, String otherPersonId) {
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

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.chat_fragment_toolbar);
        toolbar.setTitle(mOtherPersonName);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().onBackPressed();
            }
        });

        mChatAdapter.setLoadMoreListener(this);

        vEmptyChatView = view.findViewById(R.id.empty_view_messanger);
        mLoadmoreProgressBar = view.findViewById(R.id.load_more_progress_bar);
        mProgressBar = view.findViewById(R.id.chat_load_progress);

        mUserId = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", null);
        getChat(); //note : move to onResume ?

        return view;
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

            //tracking info
            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mUserId);
                obj.put("action", "active");
                obj.put("screen", "Chat");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showServerErrorToast(activity);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.chat_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setStackFromEnd(true);
        recList.setLayoutManager(llm);
        recList.setAdapter(mChatAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);

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

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //when there is text present, change alpha of send button
                if (s.length() > 0) vSendButton.setAlpha(1);
                else vSendButton.setAlpha(0.25f);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();

        if (activity != null && mUserId != null) {
            activity.emitSocket(API_Methods.VERSION + ":messages:left", joinLeft);

            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", mUserId);
                obj.put("action", "inactive");
                obj.put("screen", "Chat");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
        }
    }

    private void getChat() {
        if (getActivity() == null) return;

        Map<String, String> chat = new HashMap<>();
        chat.put(ROOM_ID, mRoomId);

        new LSDKChat(getActivity()).getChat(chat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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
                        //Log.i(TAG, "onResponse: "+object);
                        JSONArray messages = object.getJSONArray("messages");

                        mSkip = object.getInt("skip");

                        final ArrayList<Chat> tempChatList = new ArrayList<>();
                        JSONObject message;
                        Chat chat;
                        String owner;
                        long time;

                        boolean viewerIsOwnerOfMessage;
                        boolean messageBeenRead;

                        JSONArray listOfUnreadMessages = new JSONArray();

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
                                    time = simpleDateFormat.parse(message.getString("date")).getTime();
                                } catch (ParseException | JSONException e) {
                                    time = 0;
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

                                tempChatList.add(chat);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (mSkip <= 0) mCanLoadMore = false;

                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    mChatList.clear();
                                    mChatList.addAll(tempChatList);
                                    mLoadingMessages = false;
                                    mSkip -= 20;

                                    //show empty view
                                    if (mChatList.isEmpty()) {
                                        vEmptyChatView.setVisibility(View.VISIBLE);
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
                }
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

        long time;

        try {
            time = simpleDateFormat.parse(data.getString("date")).getTime();
        } catch (ParseException | JSONException e) {
            time = 0;
        }

        String owner = data.getJSONObject("owner").getString("id");
        String messageId = data.getString("id");
        Chat chat = new Chat(
                mRoomId,
                time,
                owner,
                messageId,
                data.getString("text"),
                false,
                owner.equals(mUserId)
        );

        mChatList.add(chat);
        mChatAdapter.notifyItemInserted(mChatList.size() - 1);
        scrollToBottom();

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

        mChatList.add(new Chat(Chat.TYPE_ACTION_TYPING));
        mChatAdapter.notifyItemInserted(mChatList.size() - 1);
        mOtherUserTyping = true;
        scrollToBottom();
    }

    private void removeTyping() {
        if (getActivity() == null || !mOtherUserTyping) return;

        mOtherUserTyping = false;

        int pos = mChatList.size() - 1;
        if (pos >= 0 && mChatList.get(pos).getType() == Chat.TYPE_ACTION_TYPING) {
            mChatList.remove(pos);
            mChatAdapter.notifyItemRemoved(pos);
        }
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
        if (activity == null || !activity.socketConnected() || mUserId == null) return;

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
            if (activity == null || mLoadingMessages) return;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final JSONObject data = (JSONObject) args[0];

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
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
                    });
                }

            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (getActivity() == null || mLoadingMessages) return;
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
            if (getActivity() == null || mLoadingMessages) return;
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        Log.d(TAG, "runRead: " + data.toString(4));
//                        removeChatHead(data.getOwner)
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
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
//                            if(mLastid == id setcheck message)
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    };

    private boolean mLoadingMoreMessages = false;

    @Override
    public void loadMore() {
        if (!mCanLoadMore || mLoadingMessages || mLoadingMoreMessages) return;

        mLoadingMoreMessages = true;

        Map<String, String> chat = new HashMap<>();
        chat.put(ROOM_ID, mRoomId);

        if (mSkip < 0){
            chat.put("skip", "0");
            chat.put("limit", (20 + mSkip) + "");
            mSkip = 0;
        }else {
            chat.put("skip", mSkip + "");
        }

        mLoadmoreProgressBar.setVisibility(View.VISIBLE);

        new LSDKChat(getActivity()).getChat(chat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mLoadmoreProgressBar.setVisibility(View.GONE);
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
                        JSONObject message;
                        Chat chat;
                        String owner;
                        long time;

                        boolean viewerIsOwnerOfMessage;
                        boolean messageBeenRead;

                        JSONArray listOfUnreadMessages = new JSONArray();

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
                                    time = simpleDateFormat.parse(message.getString("date")).getTime();
                                } catch (ParseException | JSONException e) {
                                    time = 0;
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

                                tempChatList.add(chat);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (mSkip == 0) mCanLoadMore = false;

                        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mChatList.addAll(0, tempChatList);
                                            mSkip -= 20;
                                            mChatAdapter.notifyItemRangeInserted(0, messages.length());
                                            mLoadingMoreMessages = false;
                                        }
                                    });

                                    mLoadmoreProgressBar.setVisibility(View.GONE);
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
                                    mLoadmoreProgressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}
