package com.linute.linute.MainContent.Chat;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final String ROOM_ID = "room";
    private static final String USERNAME = "username";
    private static final String USERID = "userid";
    private static final String USER_COUNT = "usercount";
    private static final String CHAT_HEADS = "chatheads";
    private static final int TYPING_TIMER_LENGTH = 600;

    // TODO: Rename and change types of parameters
    private String mRoomId;
    private String mUserId;
    private int mRoomUsersCnt;
    private JSONObject newMessage;
    private JSONObject typingJson;
    private JSONObject joinLeft;
    private JSONObject delivered;

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private EditText mInputMessageView;
    private ChatAdapter mChatAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private Socket mSocket;
    private JSONArray mIsReadMessageJSONArray;

    private List<Chat> mChatList = new ArrayList<>();
    private SharedPreferences mSharedPreferences;
    private int mLastRead;
    private List<ChatHead> mChatHeadList;
    private List<ChatHead> mChatHeadAddedList;


    public ChatFragment() {
        // Required empty public constructor
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mChatAdapter = new ChatAdapter(getActivity(), mChatList);
        mChatHeadList = new ArrayList<>();
        mChatHeadList = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomId    Parameter 1.
     * @param ownerName Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String roomId, String ownerName, String ownerId, int roomUsersCnt, ArrayList<ChatHead> chatHeadList) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ROOM_ID, roomId);
        args.putString(USERNAME, ownerName);
        args.putString(USERID, ownerId);
        args.putInt(USER_COUNT, roomUsersCnt);
        args.putParcelableArrayList(CHAT_HEADS, chatHeadList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString(ROOM_ID);
            mUsername = getArguments().getString(USERNAME);
            mUserId = getArguments().getString(USERID);
            mRoomUsersCnt = getArguments().getInt(USER_COUNT);
            mChatHeadList = getArguments().getParcelableArrayList(CHAT_HEADS);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        getChat();
//        ((RoomsActivity) getActivity()).toggleFab(false);
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        {
            try {
                mSocket = IO.socket(getString(R.string.CHAT_SERVER_URL));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.on("read", onRead);
        mSocket.on("joined", onJoin);
        mSocket.on("left", onLeave);
        mSocket.on("error", onError);
        mSocket.on("delivered", onDelivered);
        mSocket.connect();
        mIsReadMessageJSONArray = new JSONArray();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("joined", joinLeft);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        mSocket.emit("left", joinLeft);

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        mSocket.off("read", onRead);
        mSocket.off("joined", onJoin);
        mSocket.off("left", onLeave);
        mSocket.off("error", onError);
        mSocket.off("delivered", onDelivered);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recList = (RecyclerView) view.findViewById(R.id.chat_list);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        recList.setAdapter(mChatAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing", typingJson);
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // TODO: create menu for chat
        inflater.inflate(R.menu.people_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_leave) {
//            leave();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void getChat() {
        Map<String, String> chat = new HashMap<>();
        chat.put(ROOM_ID, mRoomId);
        LSDKChat getChat = new LSDKChat(getActivity());
        getChat.getChat(chat, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: " + request.body());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponseNotSuccessful: " + response.body().string());
                } else {
//                    Log.d(TAG, "onResponseSuccessful: " + response.body().string());
                    mChatList.clear();
                    JSONObject jsonObject = null;
                    JSONArray messages = null;
                    JSONObject message = null;
                    Chat chat = null;

                    try {
                        jsonObject = new JSONObject(response.body().string());
//                        Log.d(TAG, "onResponse: " + jsonObject.keys());
                        messages = jsonObject.getJSONArray("messages");
                        for (int i = 0; i < messages.length(); i++) {
                            message = (JSONObject) messages.get(i);
                            chat = new Chat(
                                    message.getString("room"),
                                    message.getJSONObject("owner").getString("profileImage"),
                                    message.getJSONObject("owner").getString("fullName"),
                                    Utils.formatDateToReadableString(message.getString("date")),
                                    message.getJSONObject("owner").getString("id"),
                                    message.getString("id"),
                                    message.getString("text"));
//                            Log.d(TAG, "onResponse: " + message.toString(4));
//                            if (mLastRead == -1) {
//                                if (checkRead(mUserId, message)) {
//                                    mLastRead = i;
//                                }
//                            }

                            // change logic to chataddedlist != 0
//                            if (mChatHeadList.size() != mChatHeadAddedList.size()) {
//                                if (checkChatHead(message)) {
//                                    if (mLastRead != -1 && mRoomUsersCnt - 1 == message.getJSONArray("isRead").length()) {
//                                        // do nothing
//                                    } else {
//                                        boolean found = false;
//                                        for (int j = 0; j < mChatHeadList.size(); j++) {
//                                            for (int k = 0; k < message.getJSONArray("isRead").length(); k++) {
//                                                if (mChatHeadList.get(j).getUsername().equals(
//                                                        ((JSONObject) message.getJSONArray("isRead").get(k)).getString("fullName"))) {
//                                                    found = true;
//                                                }
//                                            }
//                                            if (!found) {
//                                                // add chathead to queue; if chathead in chataddedlist skip
//                                            }
//                                            found = false;
//                                        }
//                                        // add queue to layout; add chatheads to addedchatheadlist
//                                    }
//                                }
//                            }
////                            chat.setIsRead(message.getJSONObject(""));
                            chat.setType(Chat.TYPE_MESSAGE);
                            mChatList.add(chat);
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mChatAdapter.notifyDataSetChanged();
                                    if (mLastRead != -1) {
                                        recList.scrollToPosition(mLastRead);
                                    } else {
                                        scrollToBottom();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean checkChatHead(JSONObject message) throws JSONException {
        return message.getJSONArray("read").length() != mRoomUsersCnt;
    }

    private boolean checkRead(String userId, JSONObject message) throws JSONException {
        String id = "";
        boolean read = false;
        for (int i = 0; i < message.getJSONArray("read").length(); i++) {
            id = ((JSONObject) message.getJSONArray("read").get(i)).getString("id");
            if (userId.equals(id)) {
                read = true;
            } else {
                mIsReadMessageJSONArray.put(message.getString("id"));
            }
        }
        return read;
    }

    private void addMessage(String username, String message) {
        Chat chat = new Chat(
                mRoomId,
                mSharedPreferences.getString("userImage", ""),
                username, "", mUserId, "", message);
        chat.setType(Chat.TYPE_MESSAGE);
        mChatList.add(chat);
        mChatAdapter.notifyItemInserted(mChatList.size() - 1);
        scrollToBottom();
    }

    private void addMessage(JSONObject data) throws JSONException {
//        Log.d(TAG, "addMessage: " + data.toString(4));
        Chat chat = new Chat(
                mRoomId,
                data.getJSONObject("owner").getString("profileImage"),
                data.getJSONObject("owner").getString("fullName"),
                Utils.formatDateToReadableString(data.getString("date")),
                data.getJSONObject("owner").getString("id"),
                data.getString("id"),
                data.getString("text"));
        chat.setType(Chat.TYPE_MESSAGE);
        mChatList.add(chat);
        mChatAdapter.notifyItemInserted(mChatList.size() - 1);
        scrollToBottom();

        JSONObject read = new JSONObject();
        read.put("user", mUserId);
        JSONArray readArray = new JSONArray();
        readArray.put(data.getString("id"));
        read.put("read", readArray);
        read.put("room", mRoomId);

        if (!checkRead(mUserId, data))
            mSocket.emit("read", read);
    }

    private void addTyping(String username) {
        Chat chat = new Chat(mRoomId, "", username, "1s", "", "", "");
        chat.setType(Chat.TYPE_ACTION);
        mChatList.add(chat);
        mChatAdapter.notifyItemInserted(mChatList.size() - 1);
        scrollToBottom();
    }

    private void removeTyping(String username) {
        for (int i = mChatList.size() - 1; i >= 0; i--) {
            Chat message = mChatList.get(i);
            if (message.getType() == Chat.TYPE_ACTION && message.getUserName().equals(username)) {
                mChatList.remove(i);
                mChatAdapter.notifyItemRemoved(i);
            }
        }
    }

    private void removeChatHead(String username) {
        for (int i = mChatList.size() - 1; i >= 0; i--) {
            Chat message = mChatList.get(i);
            // chat type head ; message get list size == 0 get list 0 element username
            if (message.getType() == Chat.TYPE_ACTION && message.getUserName().equals(username)) {
                mChatList.remove(i);
                mChatAdapter.notifyItemRemoved(i);
            }
            // else if
            // chat type head ; message get list size > 0
            // for -> if uesrname = username
            // list remove username
            // update adapter
        }
    }

    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
//        addMessage(mUsername, message);
        newMessage = new JSONObject();

        try {
            newMessage.put("room", mRoomId);
            newMessage.put("user", mUserId);
            newMessage.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // perform the sending message attempt.
        mSocket.emit("new message", newMessage);
    }

    private void leave() {
        mUsername = null;
        mSocket.disconnect();
        mSocket.connect();
//        startSignIn();
    }

    private void scrollToBottom() {
        recList.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
//                    String message;
                    try {
                        username = data.getJSONObject("owner").getString("fullName");
//                        if (owner and id same keep id as mLastId)
//                        Log.d(TAG, "run: " + data.toString(4));
//                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(username);
//                    addMessage(username, message);

                    try {
                        addMessage(data);
                        delivered.put("id", data.getString("id"));
                        mSocket.emit("delivered", delivered);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        Log.d(TAG, "run: " + data.toString(4));
                        username = data.getJSONObject("user").getString("fullName");
                    } catch (JSONException e) {
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        Log.d(TAG, "run: " + data.toString(4));
                        username = data.getJSONObject("user").getString("fullName");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing", typingJson);
        }
    };

    private Emitter.Listener onLeave = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.length != 0) {
//                        try {
//                            Log.d(TAG, "run: " + ((JSONObject) args[0]).toString(4));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            });
        }
    };

    private Emitter.Listener onRead = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
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
}
